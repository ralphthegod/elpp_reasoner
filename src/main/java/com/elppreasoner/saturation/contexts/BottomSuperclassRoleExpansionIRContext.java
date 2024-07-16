package com.elppreasoner.saturation.contexts;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

public class BottomSuperclassRoleExpansionIRContext extends InferenceRuleContext<Object,Object>{
    
    public BottomSuperclassRoleExpansionIRContext(InferenceRule<Object,Object> inferenceRule, OWLEntity entity) {
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
    
}
