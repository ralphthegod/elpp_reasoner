package com.elppreasoner.saturation.contexts;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;
import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

public class SubclassRoleExpansionIRContext extends InferenceRuleContext<OWLClassExpression, Set<Entry<OWLObjectPropertyExpression, OWLClassExpression>>>{
    
    public SubclassRoleExpansionIRContext(
        InferenceRule inferenceRule, 
        OWLEntity entity
    ) {
        super((InferenceRule<OWLClassExpression, Set<Entry<OWLObjectPropertyExpression, OWLClassExpression>>>) inferenceRule, entity);
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
        Set<Entry<OWLObjectPropertyExpression, OWLClassExpression>> propertyFillerSet;
        if(!getInferenceRule().getAxioms().containsKey(superclass)) {
            return new HashSet<>();
        }
        OWLDataFactory factory = OWLManager.getOWLDataFactory();
        propertyFillerSet = getInferenceRule().getAxioms().get(superclass);
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
        propertyFillerSet.forEach(entry ->{
            OWLObjectPropertyExpression property = entry.getKey();
            OWLClassExpression filler = entry.getValue();
            OWLClassExpression exists = factory.getOWLObjectSomeValuesFrom(property, filler);
            conclusions.add(factory.getOWLSubClassOfAxiom(axiom.getSubClass(), exists));
        });
        return conclusions;
    }

    @Override
    public String id(){
        return "3";
    }
    
}
