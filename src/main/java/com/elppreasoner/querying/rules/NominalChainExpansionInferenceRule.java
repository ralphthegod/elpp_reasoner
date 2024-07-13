package com.elppreasoner.querying.rules;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import com.reasoner.querying.InferenceRule;

public class NominalChainExpansionInferenceRule extends InferenceRule{

    public NominalChainExpansionInferenceRule() {
        super(OWLIndividual.class);
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