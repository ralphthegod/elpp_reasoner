package com.reasoner.saturation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;

import com.reasoner.querying.InferenceRule;
import com.reasoner.querying.OntologyAccessManager;

public class ContextAccessManager{
    
    private final Map<Class<? extends InferenceRule>, ContextProvider> inferenceRuleContexts = new HashMap<>();
    
    public void initialize(OntologyAccessManager ontologyAccessManager) {
        Set<OWLEntity> signature = ontologyAccessManager.getOntology().getSignature();
        //TODO: Implement this method
    }
}