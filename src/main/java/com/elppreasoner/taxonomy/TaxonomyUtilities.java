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

/**
 * {@link TaxonomyUtilities} is an utility class that implements all the methods that are useful to taxonomy construction.
 * It mainly provides three methods:
 *     • {@code computeTaxonomySuperConcepts()} computes all (super)concepts in the given @param axioms while discarding
 *       subsumptions involving non-atomic concepts
 *     • {@code reduceTransitiveSubsumptions()} reduces all transitive subsumptions between concepts (e.g. if A ⊑ B and B ⊑ C, "ignore" A ⊑ C)
 *       and also computes the equivalent concepts
 *     • {@code buildTaxonomy()} builds the taxonomy
 * A taxonomy builder should start with a set of axioms and use the three previous methods in the given order.
 */
public final class TaxonomyUtilities {
    /**
     * A simple private constructor to prevent the default parameter-less constructor from being used, as this is just a utility class.
     */
    private TaxonomyUtilities() {
        throw new UnsupportedOperationException("Cannot instantiate this utility class.");
    }

    /**
     * Computes all the superconcepts of classes involved in the given {@code axioms}. This is done by iterating over the {@code axioms} and, for
     * each one of them (e.g. A ⊑ B):
     *     1. Get its subclass A and compute bottom / top superconcepts for it
     *     2. Get its superclass B and compute bottom / top superconcepts for it
     *     3. If A ⊑ B involves atomic concepts, add A and B as superconcepts (this discards subsumptions involving non-atomic concepts)
     * @param axioms The axioms whose superconcepts need to be computed 
     * @return The superconcepts from the given {@code axioms}
     */
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


    /**
     * A {@link TaxonomyReductionPOJO} is a POJO class intended to make easier the return of multiple values by {@code reduceTransitiveSubsumptions()}.
     * It provides a simple constructor to initialize the two values that the previously mentioned method returns:
     *     • {@code taxonomyEquivalentConcepts}: the equivalent concepts computed by the method
     *     • {@code taxonomyDirectSuperConcepts}: the direct superconcepts computed by the method
     * Then, these values can be retrieved with the corresponding getter methods.
     * 
     * Warning: this class should never be used, except for retrieving the output of {@code reduceTransitiveSubsumptions()}.
     */
    public static class TaxonomyReductionPOJO {
        /**
         * The equivalent concepts computed by {@code reduceTransitiveSubsumptions()}.
         */
        private final Map<OWLClassExpression,Set<OWLClassExpression>> taxonomyEquivalentConcepts;

        /**
         * The direct superconcepts computed by {@code reduceTransitiveSubsumptions()}.
         */
        private final Map<OWLClassExpression,Set<OWLClassExpression>> taxonomyDirectSuperConcepts;

        /**
         * A simple private constructor to prevent the default parameter-less constructor from being used.
         */
        @SuppressWarnings("unused")
        private TaxonomyReductionPOJO() {
            throw new UnsupportedOperationException("Cannot instantiate this class with the default constructor, since it does not provide setter methods.");
        }

        /**
         * The constructor of {@link TaxonomyReductionPOJO}.
         * @param taxonomyEquivalentConcepts The equivalent concepts computed by {@code reduceTransitiveSubsumptions()}.
         * @param taxonomyDirectSuperConcepts The direct superconcepts computed by {@code reduceTransitiveSubsumptions()}.
         */
        public TaxonomyReductionPOJO(
            Map<OWLClassExpression,Set<OWLClassExpression>> taxonomyEquivalentConcepts,
            Map<OWLClassExpression,Set<OWLClassExpression>> taxonomyDirectSuperConcepts
        ) {
            this.taxonomyEquivalentConcepts = taxonomyEquivalentConcepts;
            this.taxonomyDirectSuperConcepts = taxonomyDirectSuperConcepts;
        }
        
        /**
         * Gets the equivalent concepts computed by {@code reduceTransitiveSubsumptions()}.
         * @return {@code taxonomyEquivalentConcepts}
         */
        public Map<OWLClassExpression,Set<OWLClassExpression>> getTaxonomyEquivalentConcepts() {
            return taxonomyEquivalentConcepts;
        }

        /**
         * Gets the direct superconcepts computed by {@code reduceTransitiveSubsumptions()}.
         * @return {@code taxonomyDirectSuperConcepts}
         */
        public Map<OWLClassExpression,Set<OWLClassExpression>> getTaxonomyDirectSuperConcepts() {
            return taxonomyDirectSuperConcepts;
        }
    }

    /**
     * {@code TransitiveSubsumptionsThread} is a thread that reduces transitive subsumptions between the given taxonomy superconcepts (e.g. if A ⊑ B
     * and B ⊑ C, "ignore" A ⊑ C) and also computes the equivalent concepts for the taxonomy. Used to parallelize this operation.
     * 
     * Please refer to {@link TaxonomyUtilities}{@code .reduceTransitiveSubsumptions()} for more information about what this thread's run() method does.
     */
    public static class TransitiveSubsumptionsThread extends Thread {
        /**
         * The starting index of superconcepts that this thread has to work on.
         */
        private final int min;
        
        /**
         * The ending index of superconcepts that this thread has to work on.
         */
        private final int max;

        /**
         * The array of {@code superConcepts} keys. This should be the same for all {@link TransitiveSubsumptionsThread} threads, to make sure that
         * all threads work on separate portions of superconcepts. It has not been computed within the thread to avoid a different order given by
         * {@code Map.keySet()} to each thread. 
         * This should always be {@code (OWLClassExpression[]) taxonomySuperConcepts.keySet().toArray()}.
         */
        private final OWLClassExpression[] concepts; // TODO: keep this attribute or it is safe to compute it within the thread? [TEST]

        /**
         * The superconcepts whose transitive subsumptions have to be reduced.
         */
        private final Map<OWLClassExpression, Set<OWLClassExpression>> superConcepts;

        /**
         * A {@link TaxonomyReductionPOJO} used to keep track of the reductions processed by this thread.
         */
        private TaxonomyReductionPOJO processedReductions = null;

        /**
         * A simple private constructor to prevent the default parameter-less constructor from being used.
         */
        @SuppressWarnings("unused")
        private TransitiveSubsumptionsThread() {
            throw new UnsupportedOperationException("Cannot instantiate this class with the default constructor, since it does not provide setter methods.");
        }

        /**
         * The main constructor of {@link TransitiveSubsumptionsThread}.
         * @param minIndex The starting index of superconcepts that this thread has to work on.
         * @param maxIndex The ending index of superconcepts that this thread has to work on.
         * @param conceptKeys The array of {@code superConcepts} keys, that should be common to all threads of this type.
         * @param taxonomySuperConcepts The superconcepts whose transitive subsumptions have to be reduced.
         */
        public TransitiveSubsumptionsThread(int minIndex, int maxIndex, OWLClassExpression[] conceptKeys, Map<OWLClassExpression, Set<OWLClassExpression>> taxonomySuperConcepts) {
            this.min = minIndex;
            this.max = maxIndex;
            this.concepts = conceptKeys;
            this.superConcepts = taxonomySuperConcepts;
        }

        /**
         * Gets the reductions processed by this thread. If this is called before {@code run()}, a {@code null} value will be returned.
         * @return {@code processedReductions}
         */
        public TaxonomyReductionPOJO getProcessedReductions() {
            return this.processedReductions;
        }

        @Override
        public void run() {
            Map<OWLClassExpression, Set<OWLClassExpression>> taxonomyEquivalentConcepts = new HashMap<>();
            Map<OWLClassExpression, Set<OWLClassExpression>> taxonomyDirectSuperConcepts = new HashMap<>();

            OWLClass nothing = OWLManager.getOWLDataFactory().getOWLNothing();
            taxonomyDirectSuperConcepts.put(nothing, new HashSet<>());

            for (int i = this.min; i < this.max; i++) {
                OWLClassExpression A = concepts[i];
                Set<OWLClassExpression> A_superConcepts = superConcepts.get(A);
                for (OWLClassExpression C: A_superConcepts) {
                    Set<OWLClassExpression> C_superConcepts = superConcepts.get(C);
                    if (C_superConcepts.contains(A)) {
                        taxonomyEquivalentConcepts.putIfAbsent(A, new HashSet<>());
                        taxonomyEquivalentConcepts.get(A).add(C);
                    } else {
                        boolean isDirect_AtoC = true;
                        taxonomyDirectSuperConcepts.putIfAbsent(A, new HashSet<>());
                        Iterator<OWLClassExpression> it = taxonomyDirectSuperConcepts.get(A).iterator();
                        while (it.hasNext()) {
                            OWLClassExpression B = it.next();
                            superConcepts.putIfAbsent(B, new HashSet<>()); // TODO: is this thread safe? [TEST]
                            if (superConcepts.get(B).contains(C)) {
                                isDirect_AtoC = false;
                                break;
                            }
                            if (C_superConcepts.contains(B)) {
                                it.remove();
                            }
                            if (isDirect_AtoC) {
                                taxonomyDirectSuperConcepts.putIfAbsent(A, new HashSet<>());
                                taxonomyDirectSuperConcepts.get(A).add(C);
                            }
                        }
                    }
                }
            }

            this.processedReductions = new TaxonomyReductionPOJO(taxonomyEquivalentConcepts, taxonomyDirectSuperConcepts);
        }
    }


    /**
     * Reduces all transitive subsumptions between the given taxonomy superconcepts (e.g. if A ⊑ B and B ⊑ C, "ignore" A ⊑ C) and also computes the
     * equivalent concepts for the taxonomy. A naive solution for computing the direct superconcepts of A iterates over all superconcepts C of A, and
     * for each of them checks if another superconcept B of A exists with A ⊑ B ⊑ C. If no such B exists, then C is a direct superconcept of A. This
     * algorithm is inefficient because it performs two nested iterations over the superconcepts of A (it also does not work correctly in the presence
     * of equivalent concepts).
     * This method computes the direct superconcepts of A by taking advantage of the fact that the number of all superconcepts for a given concept can
     * be sizeable, while the number of direct superconcepts is usually much smaller, often just one. For this reason, this algorithm performs the inner
     * iteration only over the set of direct superconcepts of A that have been found so far. Given A, the algorithm computes two sets: A.equivalentConcepts
     * and A.directSuperConcepts. The first set contains all concepts that are equivalent to A, including A itself. The second set contains exactly one
     * element from each equivalence class of direct superconcepts of A.
     * 
     * Note that it is safe to execute this algorithm in parallel for multiple concepts A.
     * 
     * Having computed A.equivalentConcepts and A.directSuperConcepts for each A, the construction of the taxonomy is straightforward. We introduce one
     * taxonomy node for each distinct class of equivalent concepts, and "connect" the nodes according to the direct superconcepts relation. Finally, we
     * put the top and the bottom node in the proper positions, even if ⊤ or ⊥ do not occur in the ontology. 
     * @param taxonomySuperConcepts The superconcepts whose transitive subsumptions have to be reduced.
     * @param concurrentMode Tells whether the taxonomy has to be built in parallel ({@code True}) or not ({@code False}). If this is {@code True},
     * then the {@code taxonomySuperConcepts} map's keys are retrieved and transformed into an array, which will be split into a number of parts that
     * are equal to the amount of processors available to the JVM, and each part will be assigned to one separate thread.
     * @return A {@link TaxonomyReductionPOJO} object that contains two variables, retrievable with the corresponding getter methods:
     *     • {@code taxonomyEquivalentConcepts}, the equivalent concepts of the taxonomy
     *     • {@code taxonomyDirectSuperConcepts}, the direct superconcepts of the taxonomy
     */
    public static TaxonomyReductionPOJO reduceTransitiveSubsumptions(Map<OWLClassExpression, Set<OWLClassExpression>> taxonomySuperConcepts, boolean concurrentMode) {
        final int cpuCount = concurrentMode ? Runtime.getRuntime().availableProcessors() : 1;

        Set<TransitiveSubsumptionsThread> threads = new HashSet<>();
        int increment = taxonomySuperConcepts.size() / cpuCount;
        OWLClassExpression[] conceptKeys = (OWLClassExpression[]) taxonomySuperConcepts.keySet().toArray();
        for (int i = 0; i < cpuCount-1; i++) {
            threads.add(new TransitiveSubsumptionsThread(i*increment, (i+1)*increment, conceptKeys, taxonomySuperConcepts));
        }
        threads.add(new TransitiveSubsumptionsThread((cpuCount-1)*increment, taxonomySuperConcepts.size(), conceptKeys, taxonomySuperConcepts));

        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Map<OWLClassExpression, Set<OWLClassExpression>> taxonomyEquivalentConcepts = new HashMap<>();
        Map<OWLClassExpression, Set<OWLClassExpression>> taxonomyDirectSuperConcepts = new HashMap<>();
        threads.forEach(thread -> {
            TaxonomyReductionPOJO taxonomyReductionPOJO = thread.getProcessedReductions();
            Map<OWLClassExpression, Set<OWLClassExpression>> processedEquivalentConcepts = taxonomyReductionPOJO.getTaxonomyEquivalentConcepts();
            if(!processedEquivalentConcepts.isEmpty()){
                taxonomyEquivalentConcepts.putAll(processedEquivalentConcepts);
            }
            Map<OWLClassExpression, Set<OWLClassExpression>> processedDirectSuperConcepts = taxonomyReductionPOJO.getTaxonomyDirectSuperConcepts();
            if(!processedDirectSuperConcepts.isEmpty()){
                taxonomyDirectSuperConcepts.putAll(processedDirectSuperConcepts);
            }
        });

        return new TaxonomyReductionPOJO(taxonomyEquivalentConcepts, taxonomyDirectSuperConcepts);
    }

    /**
     * Builds the {@link Taxonomy} based on precomputed taxonomy generic superconcepts, direct superconcepts and equivalent concepts. This is necessary to
     * adapt each one of its parameters to the {@link Taxonomy} implementation, i.e. creating concept nodes and relationships between nodes.
     * @param taxonomySuperConcepts The precomputed taxonomy generic superconcepts 
     * @param taxonomyEquivalentConcepts The precomputed taxonomy equivalent concepts
     * @param taxonomyDirectSuperConcepts The precomputed taxonomy direct superconcepts
     * @return The complete {@link Taxonomy}
     */
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