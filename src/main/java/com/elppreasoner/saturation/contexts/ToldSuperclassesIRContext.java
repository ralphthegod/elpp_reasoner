
package com.elppreasoner.saturation.contexts;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;


public class ToldSuperclassesIRContext extends InferenceRuleContext<OWLClassExpression, Set<OWLClassExpression>> {
    
    public ToldSuperclassesIRContext(InferenceRule<OWLClassExpression, Set<OWLClassExpression>> inferenceRule, OWLEntity entity) {
        super(inferenceRule, entity);
    }

    @Override
    public void addProcessedAxiom(OWLSubClassOfAxiom axiom) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean isTargetEntity(OWLSubClassOfAxiom axiom) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasProcessedAxiom(OWLSubClassOfAxiom axiom) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<OWLSubClassOfAxiom> compute() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'compute'");
    }
    
}
