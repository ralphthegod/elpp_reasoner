package com.elppreasoner.saturation.contexts;

import org.semanticweb.owlapi.model.OWLEntity;

import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

public class BottomSuperclassRoleExpansionIRContext extends InferenceRuleContext{
    
    public BottomSuperclassRoleExpansionIRContext(InferenceRule inferenceRule, OWLEntity entity) {
        super(inferenceRule, entity);
    }
    
}
