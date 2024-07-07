package com.elppreasoner.querying.rules;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import com.reasoner.querying.InferenceRule;

public class ToldSuperclassesInferenceRule extends InferenceRule<OWLClassExpression, Set<OWLClassExpression>>{

    @Override
    public boolean axiomCriterion(OWLClassExpression subclass, OWLClassExpression superclass) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'axiomCriterion'");
    }

    @Override
    public void addAxiom(OWLClassExpression subclass, OWLClassExpression superclass) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addAxiom'");
    }
    
}
