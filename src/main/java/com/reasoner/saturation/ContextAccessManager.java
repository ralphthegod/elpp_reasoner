package com.reasoner.saturation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.reasoner.querying.OntologyAccessManager;
import com.reasoner.reasoning.rules.InferenceRule;

public class ContextAccessManager{
    
    private final Map<Class<? extends InferenceRule>, ContextProvider> inferenceRuleContextProviders = new HashMap<>();
    private final ActiveContexts activeContexts = new ActiveContexts();
    private final Set<OWLSubClassOfAxiom> discardedAxioms = new HashSet<>();

    private class ActiveContexts extends ConcurrentLinkedQueue<InferenceRuleContext> {
        
        public boolean activateContext(InferenceRuleContext context) {
            if (context.isActiveContext().compareAndSet(false, true)) {
                return this.add(context);
            }
            return false;
        }

        public boolean deactivateContext(InferenceRuleContext context) {
            context.isActiveContext().set(false);
            if (!context.hasScheduledAxioms()) {
                activateContext(context);
                return false;
            }
            return true;
        }

        public int size() {
            return this.size();
        }

    }

    public void initialize(OntologyAccessManager ontologyAccessManager) {
        // Clear all contexts and axioms
        clearActiveContexts();
        clearDiscardedAxioms();

        initializeContextProviders(ontologyAccessManager.getRules());
        
        ontologyAccessManager.getOntology().signature().forEach((entity) -> {
            inferenceRuleContextProviders.forEach((rule, contextProvider) -> {
                InferenceRuleContext<?,?> context = null;
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

        ontologyAccessManager.getOntology().axioms(AxiomType.SUBCLASS_OF).forEach(
            (axiom) -> {
                initializeAxiom((OWLSubClassOfAxiom) axiom);
            }
        );
    }

    private void initializeAxiom(OWLSubClassOfAxiom axiom){
        Collection<InferenceRuleContext> contexts = getContextsByAxiom(axiom);

        if(contexts.isEmpty()){
            discardedAxioms.add(axiom);
        }
        else{
            for(InferenceRuleContext context : contexts){
                if(!context.hasBeenInitialized()){
                    Set<OWLSubClassOfAxiom> baseAxioms = context.initializeContext();
                    for(OWLSubClassOfAxiom axiomToAdd : baseAxioms){
                        Collection<InferenceRuleContext> baseContexts = getContextsByAxiom(axiomToAdd);
                        for(InferenceRuleContext baseContext : baseContexts){
                            baseContext.scheduleAxiom(axiomToAdd);
                        }
                    }
                }
                context.scheduleAxiom(axiom);
                activeContexts.activateContext(context);
            }
        }
    }

    private void initializeContextProviders(Collection<InferenceRule> rules) {
        inferenceRuleContextProviders.clear();

        rules.forEach((rule) -> {
            ContextProvider contextProvider = new ContextProvider(rule);
            inferenceRuleContextProviders.put(rule.getClass(), contextProvider);
        });
    }

    public Collection<InferenceRuleContext> getContextsByAxiom(OWLSubClassOfAxiom axiom) {
        Set<InferenceRuleContext> contexts = new HashSet<>();
        inferenceRuleContextProviders.forEach((rule, contextProvider) -> {
            contexts.addAll(contextProvider.getContextsByAxiom(axiom));
        });
        return contexts;
    }

    public ActiveContexts getActiveContexts() {
        return activeContexts;
    }

    public void clearActiveContexts() {
        activeContexts.clear();
    }

    public Set<OWLSubClassOfAxiom> getDiscardedAxioms() {
        return discardedAxioms;
    }

    public void clearDiscardedAxioms() {
        discardedAxioms.clear();
    }

    public void addDiscardedAxiom(OWLSubClassOfAxiom axiom) {
        discardedAxioms.add(axiom);
    }
}