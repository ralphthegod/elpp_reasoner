package com.elppreasoner.reasoning.rules;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;
import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import com.elppreasoner.saturation.contexts.BottomSuperclassRoleExpansionIRContext;
import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

/**
 * {@code InferenceRule} for bottom superclass role expansion (CR5)
 */
public class BottomSuperclassRoleExpansionInferenceRule extends InferenceRule<Object,Object> {

    public BottomSuperclassRoleExpansionInferenceRule() {
        super(OWLClass.class, BottomSuperclassRoleExpansionIRContext.class);
    }

    @Override
    public boolean axiomCriterion(OWLClassExpression subclass, OWLClassExpression superclass) {
        return false; //TODO: FALSE?
    }

    @Override
    public void addAxiom(OWLClassExpression subclass, OWLClassExpression superclass) {
        throw new IllegalStateException("This rule should not be used");
    }

    @Override
    public Set<InferenceRuleContext> extractContexts(Map<OWLObject, InferenceRuleContext> contexts,
            OWLClassExpression subclass, OWLClassExpression superclass) {
        HashSet<InferenceRuleContext> result = new HashSet<>();
        if(isSubclassABasicConcept(subclass) && isSuperclassABasicConcept(superclass) && superclass.isOWLNothing()){
            InferenceRuleContext context = contexts.get(subclass);
            result.add(context);
        }
        if(isSubclassABasicConcept(subclass) && superclass instanceof OWLObjectSomeValuesFrom){
            OWLClassExpression filler = ((OWLObjectSomeValuesFrom) superclass).getFiller();
            InferenceRuleContext context = contexts.get(filler);
            result.add(context);
        }
        return result;
    }

}