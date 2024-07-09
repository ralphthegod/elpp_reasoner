package com.elppreasoner.querying.rules;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import com.reasoner.querying.InferenceRule;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;
import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;

public class ToldSuperclassesInferenceRule extends InferenceRule<OWLClassExpression, Set<OWLClassExpression>>{

    @Override
    public boolean axiomCriterion(OWLClassExpression subclass, OWLClassExpression superclass) {
        return isSubclassABasicConcept(subclass) && isSuperclassABasicConcept(superclass);
    }

    @Override
    public void addAxiom(OWLClassExpression subclass, OWLClassExpression superclass) {
        axioms.computeIfAbsent(subclass, __ -> new HashSet<>())
            .add(superclass);
    }
    
}