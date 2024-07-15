package com.elppreasoner.reasoning.rules;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import com.elppreasoner.saturation.contexts.BottomSuperclassRoleExpansionIRContext;
import com.reasoner.reasoning.rules.InferenceRule;

public class BottomSuperclassRoleExpansionInferenceRule extends InferenceRule<Object,Object> {

    public BottomSuperclassRoleExpansionInferenceRule() {
        super(OWLClass.class, BottomSuperclassRoleExpansionIRContext.class);
    }

    @Override
    public boolean axiomCriterion(OWLClassExpression subclass, OWLClassExpression superclass) {
        return false;
    }

    @Override
    public void addAxiom(OWLClassExpression subclass, OWLClassExpression superclass) {
        throw new IllegalStateException("This rule should not be used");
    }

    

}