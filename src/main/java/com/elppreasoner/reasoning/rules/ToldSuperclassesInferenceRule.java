package com.elppreasoner.reasoning.rules;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;
import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;
import com.elppreasoner.saturation.contexts.ToldSuperclassesIRContext;
import com.reasoner.reasoning.rules.InferenceRule;

public class ToldSuperclassesInferenceRule extends InferenceRule<OWLClassExpression, Set<OWLClassExpression>>{

    public ToldSuperclassesInferenceRule() {
        super(OWLClass.class, ToldSuperclassesIRContext.class);
    }

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
