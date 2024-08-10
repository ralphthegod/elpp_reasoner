package com.elppreasoner.saturation.contexts;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;
import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

public class IntersectionSuperclassesIRContext extends InferenceRuleContext<OWLClassExpression, Map<OWLClassExpression, Set<OWLClassExpression>>> {

    public IntersectionSuperclassesIRContext(InferenceRule inferenceRule, OWLEntity entity) {
        super((InferenceRule<OWLClassExpression, Map<OWLClassExpression, Set<OWLClassExpression>>>)inferenceRule, entity);
    }

    @Override
    public boolean addProcessedAxiom(OWLSubClassOfAxiom axiom) {
        if(!isTargetEntity(axiom)) {
            throw new IllegalArgumentException("Axiom is not for the target entity");
        }
        return processedAxioms.add(axiom);
    }

    @Override
    protected boolean isTargetEntity(OWLSubClassOfAxiom axiom) {
        OWLClassExpression subclass = axiom.getSubClass();
        OWLClassExpression superclass = axiom.getSuperClass();
        final boolean check = (!isSubclassABasicConcept(subclass) || !isSuperclassABasicConcept(superclass)) 
            || (!Objects.equals(subclass, getEntity()));
        return !check;
    }

    @Override
    public boolean hasProcessedAxiom(OWLSubClassOfAxiom axiom) {
        return processedAxioms.contains(axiom);
    }

    @Override
    public Set<OWLSubClassOfAxiom> compute(OWLSubClassOfAxiom axiom) {
        OWLClassExpression superclass = axiom.getSuperClass();
        OWLClassExpression subclass = axiom.getSubClass();
        if(!getInferenceRule().getAxioms().containsKey(superclass)){
            return new HashSet<>();
        }
        Set<OWLClassExpression> intersectionSuperclasses = getIntersectionSuperclasses(subclass, superclass);
        OWLDataFactory factory = OWLManager.getOWLDataFactory();
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
        intersectionSuperclasses.forEach(
            sup -> conclusions.add(factory.getOWLSubClassOfAxiom(subclass, sup))
        );
        return conclusions;
    }

    private Set<OWLClassExpression> getIntersectionSuperclasses(
        OWLClassExpression subclass,
        OWLClassExpression superclass
    ){
        OWLDataFactory factory = OWLManager.getOWLDataFactory();
        Set<OWLClassExpression> superclasses = new HashSet<>();

        for(OWLClassExpression sup : getInferenceRule().getAxioms().get(superclass).keySet()){
            OWLSubClassOfAxiom newAxiom = factory.getOWLSubClassOfAxiom(subclass, sup);
            if(!Objects.equals(sup, getEntity()) && !hasProcessedAxiom(newAxiom)){
                continue;
            }
        }

        return superclasses;
    }

}
