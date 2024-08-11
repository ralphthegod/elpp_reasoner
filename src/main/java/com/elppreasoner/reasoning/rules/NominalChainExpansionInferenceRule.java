package com.elppreasoner.reasoning.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;
import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;

import com.elppreasoner.saturation.contexts.NominalChainExpansionIRContext;
import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

/**
 * {@code InferenceRule} for nominal chain expansion (CR6)
 */
public class NominalChainExpansionInferenceRule extends InferenceRule<Object, Object>{

    public NominalChainExpansionInferenceRule() {
        super(NominalChainExpansionIRContext.class);
        addEntityType(OWLIndividual.class);
    }

    @Override
    public boolean axiomCriterion(OWLClassExpression subclass, OWLClassExpression superclass) {
        return false;
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
            result.addAll(contexts.values());
        }
        if(isSubclassABasicConcept(subclass) && superclass instanceof OWLObjectSomeValuesFrom){
            result.addAll(contexts.values());
        }
        return result;
    }
}