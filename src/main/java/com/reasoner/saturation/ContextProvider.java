package com.reasoner.saturation;

import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLEntity;

import com.reasoner.querying.InferenceRule;

public class ContextProvider{
    
    private InferenceRule inferenceRule;
    private final Map<Class<? extends OWLEntity>, InferenceRuleContext> contexts = new HashMap<>();

    public ContextProvider(InferenceRule inferenceRule) {
        this.inferenceRule = inferenceRule;
    }

    public void addContext(Class<? extends OWLEntity> entity, InferenceRuleContext context) {
        if(entity.getClass() == inferenceRule.getEntityType()){
            contexts.put(entity, context);
        }
    }



}