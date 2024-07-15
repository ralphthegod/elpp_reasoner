package com.elppreasoner.reasoning.rules;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import com.elppreasoner.saturation.contexts.NominalChainExpansionIRContext;
import com.reasoner.reasoning.rules.InferenceRule;

public class NominalChainExpansionInferenceRule extends InferenceRule<Object, Object>{

    public NominalChainExpansionInferenceRule() {
        super(OWLIndividual.class, NominalChainExpansionIRContext.class);
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