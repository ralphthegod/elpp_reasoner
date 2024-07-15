package com.elppreasoner.saturation.contexts;

import org.semanticweb.owlapi.model.OWLEntity;

import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

public class IntersectionSuperclassesIRContext extends InferenceRuleContext {

    public IntersectionSuperclassesIRContext(InferenceRule inferenceRule, OWLEntity entity) {
        super(inferenceRule, entity);
    }

    
    
}
