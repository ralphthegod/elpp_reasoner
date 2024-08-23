package com.elppreasoner.reasoning.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;
import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;
import com.elppreasoner.saturation.contexts.ToldSuperclassesIRContext;
import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

/**
 * {@code InferenceRule} for told superclasses (CR1)
 */
public class ToldSuperclassesInferenceRule extends InferenceRule<OWLClassExpression, Set<OWLClassExpression>>{

    public ToldSuperclassesInferenceRule() {
        super(ToldSuperclassesIRContext.class);
        addEntityType(OWLClass.class);
        addEntityType(OWLIndividual.class);
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

    @Override
    public Set<InferenceRuleContext> extractContexts(Map<OWLObject, InferenceRuleContext> contexts,
            OWLClassExpression subclass, OWLClassExpression superclass) {
        if(axiomCriterion(subclass, superclass)){
            if(subclass instanceof OWLObjectOneOf){
                OWLObjectOneOf oneOfSubClass = (OWLObjectOneOf) subclass;
                InferenceRuleContext context = contexts.get(oneOfSubClass.individuals().findFirst().get());
                if(context != null){
                    return new HashSet<InferenceRuleContext>() {{
                        add(context);
                    }};
                }
            }
            else{
                InferenceRuleContext context = contexts.get(subclass);
                if(context != null){
                    return new HashSet<InferenceRuleContext>() {{
                        add(context);
                    }};
                }
            }
        }
        return new HashSet<>();
    }
    
}
