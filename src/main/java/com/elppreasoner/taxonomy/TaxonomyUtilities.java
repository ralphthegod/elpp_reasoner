package com.elppreasoner.taxonomy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;

import com.reasoner.taxonomy.Taxonomy;

// TODO: javadoc
public final class TaxonomyUtilities {
    private TaxonomyUtilities() {
        throw new UnsupportedOperationException("Cannot instantiate this utility class.");
    }

    public static Map<OWLClassExpression, Set<OWLClassExpression>> computeTaxonomySuperConcepts(Set<OWLSubClassOfAxiom> axioms) {
        Map<OWLClassExpression, Set<OWLClassExpression>> taxonomySuperConcepts = new HashMap<>();
        
        OWLClass thing = OWLManager.getOWLDataFactory().getOWLThing();
        taxonomySuperConcepts.put(thing, new HashSet<>());

        OWLClass nothing = OWLManager.getOWLDataFactory().getOWLNothing();
        taxonomySuperConcepts.put(nothing, new HashSet<>());
        
        OWLClassExpression subClass;
        OWLClassExpression superClass;

        for (OWLSubClassOfAxiom axiom: axioms) {
            subClass = axiom.getSubClass();
            if (subClass instanceof OWLClass && !subClass.isOWLNothing()) {
                taxonomySuperConcepts.get(nothing).add(subClass);
            }
            if (subClass instanceof OWLObjectSomeValuesFrom) {
                OWLObjectSomeValuesFrom objectSomeValuesFrom = (OWLObjectSomeValuesFrom) subClass;
                OWLClassExpression filler = objectSomeValuesFrom.getFiller();
                if (!filler.isOWLNothing()) {
                    taxonomySuperConcepts.get(nothing).add(filler);
                }
                taxonomySuperConcepts.putIfAbsent(filler, new HashSet<>());
                if (!filler.isOWLThing()) {
                    taxonomySuperConcepts.get(filler).add(thing);
                }
            }

            superClass = axiom.getSuperClass();
            if (superClass instanceof OWLClass && !superClass.isOWLNothing()) {
                taxonomySuperConcepts.get(nothing).add(superClass);
            }
            if (superClass instanceof OWLObjectSomeValuesFrom) {
                OWLObjectSomeValuesFrom objectSomeValuesFrom = (OWLObjectSomeValuesFrom) superClass;
                OWLClassExpression filler = objectSomeValuesFrom.getFiller();
                if (!filler.isOWLNothing()) {
                    taxonomySuperConcepts.get(nothing).add(filler);
                }
                taxonomySuperConcepts.putIfAbsent(filler, new HashSet<>());
                if (!filler.isOWLThing()) {
                    taxonomySuperConcepts.get(filler).add(thing);
                }
            }

            if ((subClass.isClassExpressionLiteral() || subClass instanceof OWLObjectOneOf) && (superClass.isClassExpressionLiteral() || superClass instanceof OWLObjectOneOf)) {
                if (!subClass.isOWLNothing()) {
                    taxonomySuperConcepts.get(nothing).add(subClass);
                }
                taxonomySuperConcepts.putIfAbsent(subClass, new HashSet<>());
                if (!subClass.isOWLThing()) {
                    taxonomySuperConcepts.get(subClass).add(superClass);
                }

                if (!superClass.isOWLNothing()) {
                    taxonomySuperConcepts.get(nothing).add(superClass);
                }
                taxonomySuperConcepts.putIfAbsent(superClass, new HashSet<>());
                if (!superClass.isOWLThing()) {
                    taxonomySuperConcepts.get(superClass).add(thing);
                }
            }
        }

        return taxonomySuperConcepts;
    }


    public static class TaxonomyReductionPOJO {
        private final Map<OWLClassExpression,Set<OWLClassExpression>> taxonomyEquivalentConcepts;
        private final Map<OWLClassExpression,Set<OWLClassExpression>> taxonomyDirectSuperConcepts;

        public TaxonomyReductionPOJO(
            Map<OWLClassExpression,Set<OWLClassExpression>> taxonomyEquivalentConcepts,
            Map<OWLClassExpression,Set<OWLClassExpression>> taxonomyDirectSuperConcepts
            ) {
                this.taxonomyEquivalentConcepts = taxonomyEquivalentConcepts;
                this.taxonomyDirectSuperConcepts = taxonomyDirectSuperConcepts;
            }
        
        public Map<OWLClassExpression,Set<OWLClassExpression>> getTaxonomyEquivalentConcepts() {
            return taxonomyEquivalentConcepts;
        }

        public Map<OWLClassExpression,Set<OWLClassExpression>> getTaxonomyDirectSuperConcepts() {
            return taxonomyDirectSuperConcepts;
        }
    }

    public static TaxonomyReductionPOJO reduceTransitiveSubsumptions(Map<OWLClassExpression, Set<OWLClassExpression>> taxonomySuperConcepts) {
        Map<OWLClassExpression, Set<OWLClassExpression>> taxonomyEquivalentConcepts = new HashMap<>();
        Map<OWLClassExpression, Set<OWLClassExpression>> taxonomyDirectSuperConcepts = new HashMap<>();

        OWLClass nothing = OWLManager.getOWLDataFactory().getOWLNothing();
        taxonomyDirectSuperConcepts.put(nothing, new HashSet<>());

        for (Entry<OWLClassExpression, Set<OWLClassExpression>> entry: taxonomySuperConcepts.entrySet()) {
            OWLClassExpression A = entry.getKey();
            Set<OWLClassExpression> A_superConcepts = entry.getValue();
            for (OWLClassExpression C: A_superConcepts) {
                Set<OWLClassExpression> C_superConcepts = taxonomySuperConcepts.get(C);
                if (C_superConcepts.contains(A)) {
                    taxonomyEquivalentConcepts.putIfAbsent(A, new HashSet<>());
                    taxonomyEquivalentConcepts.get(A).add(C);
                } else {
                    boolean isDirect_AtoC = true;
                    taxonomyDirectSuperConcepts.putIfAbsent(A, new HashSet<>());
                    Iterator<OWLClassExpression> it = taxonomyDirectSuperConcepts.get(A).iterator();
                    while (it.hasNext()) {
                        OWLClassExpression B = it.next();
                        taxonomySuperConcepts.putIfAbsent(B, new HashSet<>());
                        if (taxonomySuperConcepts.get(B).contains(C)) {
                            isDirect_AtoC = false;
                            break;
                        }
                        if (C_superConcepts.contains(B)) {
                            it.remove();
                        }
                    }
                    if (isDirect_AtoC) {
                        taxonomyDirectSuperConcepts.putIfAbsent(A, new HashSet<>());
                        taxonomyDirectSuperConcepts.get(A).add(C);
                    }
                }
            }
        }

        return new TaxonomyReductionPOJO(taxonomyEquivalentConcepts, taxonomyDirectSuperConcepts);
    }

    public static Taxonomy buildTaxonomy(
        Map<OWLClassExpression, Set<OWLClassExpression>> taxonomySuperConcepts,
        Map<OWLClassExpression, Set<OWLClassExpression>> taxonomyEquivalentConcepts,
        Map<OWLClassExpression, Set<OWLClassExpression>> taxonomyDirectSuperConcepts
    ) {
        Map<OWLClass, OWLClassNode> classToNode = new HashMap<>(taxonomySuperConcepts.size());
        Map<OWLClassNode, OWLClassNodeSet> nodeToDirectSuperClasses = new HashMap<>(taxonomySuperConcepts.size());
        Map<OWLClassNode, OWLClassNodeSet> nodeToDirectSubClasses = new HashMap<>(taxonomySuperConcepts.size());
        Map<OWLClassNode, OWLClassNodeSet> nodeToAllSuperClasses = new HashMap<>(taxonomySuperConcepts.size());
        Map<OWLClassNode, OWLClassNodeSet> nodeToAllSubClasses = new HashMap<>(taxonomySuperConcepts.size());

        for (Entry<OWLClassExpression, Set<OWLClassExpression>> entry : taxonomyEquivalentConcepts.entrySet()) {
            OWLClassExpression concept = entry.getKey();
            if (concept instanceof OWLClass) {
                if (classToNode.containsKey(concept)) {
                    continue;
                }

                OWLClassNode classNode = new OWLClassNode();
                Set<OWLClassExpression> equivalentClasses = entry.getValue();
                for (OWLClassExpression equivalentClass: equivalentClasses) {
                    if (equivalentClass instanceof OWLClass) {
                        OWLClass owlClass = (OWLClass) equivalentClass;
                        classNode.add(owlClass);
                        classToNode.put(owlClass, classNode);
                    }
                }
            }
        }

        OWLClass thing = OWLManager.getOWLDataFactory().getOWLThing();
        OWLClassNode thingNode = new OWLClassNode(thing);
        classToNode.put(thing, thingNode);

        OWLClass nothing = OWLManager.getOWLDataFactory().getOWLNothing();
        OWLClassNode nothingNode = new OWLClassNode(nothing);
        classToNode.put(nothing, nothingNode);

        for (Entry<OWLClassExpression, Set<OWLClassExpression>> entry: taxonomyDirectSuperConcepts.entrySet()) {
            OWLClassExpression concept = entry.getKey();
            if (concept instanceof OWLClass) {
                OWLClass conceptClass = (OWLClass) concept;
                classToNode.putIfAbsent(conceptClass, new OWLClassNode(conceptClass));
                OWLClassNode conceptClassNode = classToNode.get(conceptClass);
                
                if (nodeToDirectSuperClasses.containsKey(conceptClassNode)) {
                    continue;
                }

                OWLClassNodeSet directSuperclasses = new OWLClassNodeSet();
                for (OWLClassExpression directSuperConcept: entry.getValue()) {
                    if (directSuperConcept instanceof OWLClass) {
                        OWLClass directSuperClass = (OWLClass) directSuperConcept;
                        classToNode.putIfAbsent(directSuperClass, new OWLClassNode(directSuperClass));
                        OWLClassNode directSuperClassNode = classToNode.get(directSuperClass);
                        directSuperclasses.addNode(directSuperClassNode);

                        nodeToDirectSubClasses.putIfAbsent(directSuperClassNode, new OWLClassNodeSet());
                        nodeToDirectSubClasses.get(directSuperClassNode).addNode(conceptClassNode);
                    }
                }

                nodeToDirectSuperClasses.put(conceptClassNode, directSuperclasses);
            }
        }

        for (Entry<OWLClassExpression, Set<OWLClassExpression>> entry: taxonomySuperConcepts.entrySet()) {
            OWLClassExpression concept = entry.getKey();
            if (concept instanceof OWLClass) {
                OWLClass conceptClass = (OWLClass) concept;
                classToNode.putIfAbsent(conceptClass, new OWLClassNode(conceptClass));
                OWLClassNode conceptClassNode = classToNode.get(conceptClass);

                if (!conceptClass.isOWLThing()) {
                    nodeToDirectSuperClasses.putIfAbsent(conceptClassNode, new OWLClassNodeSet(thingNode));
                }

                if (!conceptClass.isOWLNothing()) {
                    nodeToDirectSubClasses.putIfAbsent(conceptClassNode, new OWLClassNodeSet(nothingNode));
                    nodeToAllSubClasses.putIfAbsent(conceptClassNode, new OWLClassNodeSet(nothingNode));
                    nodeToAllSuperClasses.putIfAbsent(nothingNode, new OWLClassNodeSet(nothingNode));
                    nodeToAllSuperClasses.get(nothingNode).addNode(conceptClassNode);
                }

                if (nodeToAllSuperClasses.containsKey(conceptClassNode)) {
                    continue;
                }

                OWLClassNodeSet superClasses;
                if (concept.isOWLThing()) {
                    superClasses = new OWLClassNodeSet();
                } else {
                    superClasses = new OWLClassNodeSet(thingNode);
                }
                for (OWLClassExpression superConcept: entry.getValue()) {
                    if (superConcept instanceof OWLClass) {
                        OWLClass superClass = (OWLClass) superConcept;
                        classToNode.putIfAbsent(superClass, new OWLClassNode(superClass));
                        OWLClassNode superClassNode = classToNode.get(superClass);
                        if (Objects.equals(superClassNode,conceptClassNode)) {
                            continue;
                        }

                        superClasses.addNode(superClassNode);

                        if (superClass.isOWLNothing()) {
                            nodeToAllSubClasses.putIfAbsent(superClassNode, new OWLClassNodeSet());
                        } else {
                            nodeToAllSubClasses.putIfAbsent(superClassNode, new OWLClassNodeSet(nothingNode));
                        }
                        nodeToAllSubClasses.get(superClassNode).addNode(conceptClassNode);
                    }
                }

                nodeToAllSuperClasses.put(conceptClassNode, superClasses);
            }
        }

        nodeToDirectSuperClasses.putIfAbsent(thingNode, new OWLClassNodeSet());
        nodeToDirectSuperClasses.putIfAbsent(nothingNode, new OWLClassNodeSet());

        nodeToDirectSubClasses.putIfAbsent(thingNode, new OWLClassNodeSet());
        nodeToDirectSubClasses.putIfAbsent(nothingNode, new OWLClassNodeSet());
        
        nodeToAllSubClasses.putIfAbsent(thingNode, new OWLClassNodeSet());
        nodeToAllSubClasses.putIfAbsent(nothingNode, new OWLClassNodeSet());
        
        nodeToAllSuperClasses.putIfAbsent(thingNode, new OWLClassNodeSet());
        nodeToAllSuperClasses.putIfAbsent(nothingNode, new OWLClassNodeSet());


        return new Taxonomy(
            taxonomyEquivalentConcepts,
            classToNode,
            nodeToDirectSuperClasses,
            nodeToDirectSubClasses,
            nodeToAllSuperClasses,
            nodeToAllSubClasses
        );
    }
}