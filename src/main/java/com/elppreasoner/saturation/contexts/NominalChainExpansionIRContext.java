package com.elppreasoner.saturation.contexts;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;
import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.elppreasoner.utils.RelationGraph;
import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

public class NominalChainExpansionIRContext extends InferenceRuleContext<Object, Object>{
    
    private final Set<OWLIndividual> individuals = new HashSet<>(); // contains all entities from {a} form
    private final OWLIndividual individualEntity;
    private final Set<OWLClassExpression> subclasses = new HashSet<>(); // contains A from A ⊑ {a} form
    private final Map<OWLClassExpression, Set<OWLClassExpression>> superclassesBySubclass = new HashMap<>(); // maps A1 to A2 from A1 ⊑ A2 form
    private final RelationGraph relationGraph = new RelationGraph(); // contains A1 -R-> A2 from A1 ⊑ ∃r.A2 form
    
    public NominalChainExpansionIRContext(InferenceRule inferenceRule, OWLEntity entity) {
        super((InferenceRule<Object, Object>) inferenceRule, entity);
        individualEntity = (OWLIndividual) entity;
    }

    @Override
    public boolean addProcessedAxiom(OWLSubClassOfAxiom axiom) {
        if(!isTargetEntity(axiom)){
            throw new IllegalArgumentException("Axiom is not a target entity");
        }

        OWLClassExpression subclass = axiom.getSubClass();
        OWLClassExpression superclass = axiom.getSuperClass();

        if(isSubclassABasicConcept(subclass)){
            if(isSuperclassABasicConcept(superclass)){
                if(superclassesBySubclass.containsKey(subclass)){
                    superclassesBySubclass.get(subclass).add(superclass);
                }
                else{
                    Set<OWLClassExpression> superclasses = new HashSet<>();
                    superclasses.add(superclass);
                    superclassesBySubclass.put(subclass, superclasses);
                }
            }
            else if(superclass instanceof OWLObjectSomeValuesFrom){
                OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) superclass;
                relationGraph.insertNode(someValuesFrom.getFiller());
                // Create edge from subclass to filler
                if(!Objects.equals(subclass, someValuesFrom.getFiller())){
                    // relationGraph.putEdge(subclass, someValuesFrom.getFiller()); (edge not oriented??)
                    relationGraph.insertEdge(someValuesFrom.getFiller(), subclass);
                }
            }

            relationGraph.insertNode(subclass);
        }

        if(subclass instanceof OWLObjectOneOf){
            final OWLObjectOneOf oneOf = (OWLObjectOneOf) subclass;
            if(oneOf.individuals().count() == 1){
                relationGraph.insertNode(oneOf);
            }
            else{
                throw new IllegalArgumentException("OneOf expression contains more than one individual");
            }
        }

        if(superclass instanceof OWLClass){
            final OWLClass c = (OWLClass) superclass;
            relationGraph.insertNode(c);
        }

        if(superclass instanceof OWLObjectOneOf){
            final OWLObjectOneOf oneOf = (OWLObjectOneOf) superclass;
            if(oneOf.individuals().count() == 1){
                relationGraph.insertNode(oneOf);
                oneOf.individuals().forEach(
                    individual -> {
                        individuals.add(individual);
                        final OWLIndividual i = individualEntity;
                        if(isSubclassABasicConcept(subclass) && Objects.equals(individual, i)){
                            subclasses.add(subclass);
                        }
                    }
                );
            }
        }
        return processedAxioms.add(axiom);
        
    }


    @Override
    protected boolean isTargetEntity(OWLSubClassOfAxiom axiom) {
        OWLClassExpression subclass = axiom.getSubClass();
        OWLClassExpression superclass = axiom.getSuperClass();

        if(!isSubclassABasicConcept(subclass) || (!(superclass instanceof OWLObjectSomeValuesFrom) && !isSuperclassABasicConcept(superclass))){
            return false;
        }
        
        return true;
    }

    @Override
    public boolean hasProcessedAxiom(OWLSubClassOfAxiom axiom) {
        return processedAxioms.contains(axiom);
    }

    @Override
    public Set<OWLSubClassOfAxiom> compute(OWLSubClassOfAxiom axiom) {
        if(subclasses.size() < 2) return new HashSet<>();
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
        subclasses.forEach(
            outerSubclass -> {
                for(OWLClassExpression innerSubclass : subclasses){
                    if(Objects.equals(outerSubclass, innerSubclass)) continue;
                    Set<OWLSubClassOfAxiom> localConclusions = new HashSet<>();

                    boolean found = relationGraph.reach(outerSubclass, innerSubclass);
                    if(!found){
                        for(OWLIndividual individual : individuals){
                            OWLIndividual i = individualEntity;
                            if(Objects.equals(individual, i)) break;
                            final OWLObjectOneOf oneOf = owlDataFactory.getOWLObjectOneOf(individual);
                            found = relationGraph.reach(oneOf, innerSubclass);
                            if(found) break;
                        }
                    }
                    if(found){
                        Set<OWLClassExpression> destSuperclasses;
                        if (superclassesBySubclass.containsKey(innerSubclass)) {
                            destSuperclasses = superclassesBySubclass.get(innerSubclass);
                        } else {
                            destSuperclasses = new HashSet<>();
                        }
                        Set<OWLSubClassOfAxiom> tempSet = new HashSet<>();
                            for (OWLClassExpression conclusionSuperclass : destSuperclasses) {
                                OWLSubClassOfAxiom localAxiom = owlDataFactory.getOWLSubClassOfAxiom(outerSubclass, conclusionSuperclass);
                                tempSet.add(localAxiom);
                            }
                        localConclusions.addAll(tempSet);
                    }

                    conclusions.addAll(localConclusions);
                }
            }
        );

        return conclusions;
    }
    
}
