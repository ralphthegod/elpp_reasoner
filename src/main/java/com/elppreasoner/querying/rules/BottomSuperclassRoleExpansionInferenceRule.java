package com.elppreasoner.querying.rules;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import com.reasoner.querying.InferenceRule;

public class BottomSuperclassRoleExpansionInferenceRule extends InferenceRule {

    public BottomSuperclassRoleExpansionInferenceRule() {
        super(OWLClass.class);
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