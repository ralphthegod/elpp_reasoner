
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


public class ToldSuperclassesIRContext extends InferenceRuleContext<OWLClassExpression, Set<OWLClassExpression>> {
    
    public ToldSuperclassesIRContext(InferenceRule inferenceRule, OWLEntity entity) {
        super((InferenceRule<OWLClassExpression, Set<OWLClassExpression>>) inferenceRule, entity);
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
        OWLClassExpression subclass = axiom.getSubClass();
        OWLClassExpression superclass = axiom.getSuperClass();
        Map<OWLClassExpression, Set<OWLClassExpression>> supBySub = getInferenceRule().getAxioms();
        final Set<OWLClassExpression> superclasses;

        if(supBySub.containsKey(superclass)) {
            superclasses = supBySub.get(superclass);
        }
        else{
            return new HashSet<>();
        }
        
        OWLDataFactory factory = OWLManager.getOWLDataFactory();
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
        superclasses.forEach(sup -> {
            OWLSubClassOfAxiom conclusion = factory.getOWLSubClassOfAxiom(subclass, sup);
            conclusions.add(conclusion);
        });
        return conclusions;
    }
    
}
