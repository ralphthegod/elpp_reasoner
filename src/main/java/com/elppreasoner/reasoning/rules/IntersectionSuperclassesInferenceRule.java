package com.elppreasoner.reasoning.rules;

import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;
import com.elppreasoner.saturation.contexts.IntersectionSuperclassesIRContext;
import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

/**
 * {@code InferenceRule} for intersection superclasses (CR2)
 */
public class IntersectionSuperclassesInferenceRule extends InferenceRule<OWLClassExpression, Map<OWLClassExpression, Set<OWLClassExpression>>>{

    public IntersectionSuperclassesInferenceRule() {
        super(IntersectionSuperclassesIRContext.class);
        addEntityType(OWLClass.class);
        addEntityType(OWLIndividual.class);
    }

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

    @Override
    public Set<InferenceRuleContext> extractContexts(Map<OWLObject, InferenceRuleContext> contexts,
            OWLClassExpression subclass, OWLClassExpression superclass) {
        if(isSubclassABasicConcept(subclass) && isSuperclassABasicConcept(superclass)){
            InferenceRuleContext context = contexts.get(subclass);
            if(context != null){
                return new HashSet<InferenceRuleContext>() {{
                    add(context);
                }};
            }
        }
        return new HashSet<>();
    }
    
}
