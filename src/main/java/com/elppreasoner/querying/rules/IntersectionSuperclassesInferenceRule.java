package com.elppreasoner.querying.rules;

import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;

import com.reasoner.querying.InferenceRule;

public class IntersectionSuperclassesInferenceRule extends InferenceRule<OWLClassExpression, Map<OWLClassExpression, Set<OWLClassExpression>>>{

    @Override
    public boolean axiomCriterion(OWLClassExpression subclass, OWLClassExpression superclass) {
        return subclass instanceof OWLObjectIntersectionOf & isSuperclassABasicConcept(superclass);
    }

    @Override
    public void addAxiom(OWLClassExpression subclass, OWLClassExpression superclass) {
        OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) subclass;
        List<OWLClassExpression> operands = intersection.getOperandsAsList();

        // Normalization reduces the intersection to two operands
        OWLClassExpression operand1 = operands.get(0);
        OWLClassExpression operand2 = operands.get(1);

        // Commutative property of intersection, so we add both directions
        axioms.computeIfAbsent(operand1, __ -> new HashMap()).computeIfAbsent(operand2, __ -> new HashSet<>())
            .add(superclass);
        axioms.computeIfAbsent(operand2, __ -> new HashMap()).computeIfAbsent(operand1, __ -> new HashSet<>())
            .add(superclass);    
        
    }
    
}
