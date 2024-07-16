package com.elppreasoner.saturation.contexts;

import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

public class SubclassRoleExpansionIRContext extends InferenceRuleContext<OWLClassExpression, Set<Entry<OWLObjectPropertyExpression, OWLClassExpression>>>{
    
    public SubclassRoleExpansionIRContext(
        InferenceRule<OWLClassExpression, Set<Entry<OWLObjectPropertyExpression, OWLClassExpression>>> inferenceRule, 
        OWLEntity entity
    ) {
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
