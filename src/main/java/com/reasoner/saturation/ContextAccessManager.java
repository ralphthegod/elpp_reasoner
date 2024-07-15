package com.reasoner.saturation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLEntity;

import com.reasoner.querying.OntologyAccessManager;
import com.reasoner.reasoning.rules.InferenceRule;

public class ContextAccessManager{
    
    private final Map<Class<? extends InferenceRule>, ContextProvider> inferenceRuleContextProviders = new HashMap<>();

    public void initialize(OntologyAccessManager ontologyAccessManager) {
        initializeContextProviders(ontologyAccessManager.getRules());
        
        ontologyAccessManager.getOntology().signature().forEach((entity) -> {
            inferenceRuleContextProviders.forEach((rule, contextProvider) -> {
                InferenceRuleContext context = null;
                try{
                    contextProvider
                        .getInferenceRule()
                        .getContextType()
                        .getDeclaredConstructor(InferenceRule.class, OWLEntity.class)
                        .newInstance(rule, entity);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                contextProvider.addContext(entity, context);
            });
        });
    }

    private void initializeContextProviders(Collection<InferenceRule> rules) {
        inferenceRuleContextProviders.clear();

        rules.forEach((rule) -> {
            ContextProvider contextProvider = new ContextProvider(rule);
            inferenceRuleContextProviders.put(rule.getClass(), contextProvider);
        });
    }

}