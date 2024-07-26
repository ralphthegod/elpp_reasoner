package com.reasoner.saturation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.utils.OntologyUtilities;


public class ContextProvider{
    
    private final InferenceRule inferenceRule;
    private final Map<OWLObject, InferenceRuleContext> contexts = new HashMap<>();

    public ContextProvider(InferenceRule inferenceRule) {
        this.inferenceRule = inferenceRule;
    }

    /**
     * Add a context for given entity.
     * @param entity
     * @param context
     */
    public void addContext(OWLEntity entity, InferenceRuleContext context) {
        if(OntologyUtilities.getEntityType(entity) == inferenceRule.getEntityType()) {
            contexts.put(entity, context);
        }
    }

    /**
     * Get the inference rule.
     * @return InferenceRule
     */
    public InferenceRule getInferenceRule() {
        return inferenceRule;
    }

    /**
     * Get the contexts.
     * @return Map of contexts.
     */
    public Set<InferenceRuleContext> getContextsByAxiom(OWLSubClassOfAxiom axiom) {
        OWLClassExpression subclass = axiom.getSubClass();
        OWLClassExpression superclass = axiom.getSuperClass();
        return inferenceRule.extractContexts(contexts, subclass, superclass);
    }


}