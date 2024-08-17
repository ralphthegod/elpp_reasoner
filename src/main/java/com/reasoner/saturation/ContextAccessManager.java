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

    protected static class ActiveContexts extends ConcurrentLinkedQueue<InferenceRuleContext> {
        
        public boolean activateContext(InferenceRuleContext context) {
            if (context.isActiveContext().compareAndSet(false, true)) {
                return this.add(context);
            }
            return false;
        }

        public boolean deactivateContext(InferenceRuleContext context) {
            context.isActiveContext().set(false);
            if (context.hasScheduledAxioms()) {
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
        
        clearActiveContexts();
        clearDiscardedAxioms();

        initializeContextProviders(ontologyAccessManager.getRules());
        
        ontologyAccessManager.getOntology().signature().forEach((entity) -> {
            System.out.println("Initializing entity: " + entity);
            inferenceRuleContextProviders.forEach((rule, contextProvider) -> {
                InferenceRuleContext<?,?> context = null;
                try{
                    context = contextProvider.createContextByEntity(entity);
                    if(context != null){
                        System.out.println("Adding context for rule: " + rule);
                        contextProvider.addContext(entity, context);
                    }
                    else{
                        System.out.println("Context not created for rule: " + rule);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            });
            System.out.println("                          ");
        });

        ontologyAccessManager.getOntology().axioms(AxiomType.SUBCLASS_OF).forEach(
            (axiom) -> {
                initializeAxiom((OWLSubClassOfAxiom) axiom);
            }
        );
    }

    private void initializeAxiom(OWLSubClassOfAxiom axiom){
        System.out.println("Initializing axiom: " + axiom);
        Collection<InferenceRuleContext> contexts = getContextsByAxiom(axiom);
        if(contexts == null || contexts.isEmpty()){
            System.out.println("(1) Discarding axiom " + axiom);
            System.out.println("                          ");
            discardedAxioms.add(axiom);
            return;
        }
        else{
            for(InferenceRuleContext context : contexts){
                if(context == null){
                    System.out.println("(2) Discarding axiom " + axiom);
                    System.out.println("                          ");
                    discardedAxioms.add(axiom);
                    return;
                }
                if(!context.hasBeenInitialized()){
                    System.out.println("Initializing context");
                    Set<OWLSubClassOfAxiom> baseAxioms = context.initializeContext();
                    for(OWLSubClassOfAxiom axiomToAdd : baseAxioms){
                        try {
                            Collection<InferenceRuleContext> baseContexts = getContextsByAxiom(axiomToAdd);
                            for(InferenceRuleContext baseContext : baseContexts){
                                baseContext.scheduleAxiom(axiomToAdd);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                context.scheduleAxiom(axiom);
                activeContexts.activateContext(context);
                System.out.println("Context activated: " + context);
            }
        }
        System.out.println("                          ");
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
            System.out.println("Getting contexts for axiom using rule: " + rule);
            Set<InferenceRuleContext> contextsByAxiom = contextProvider.getContextsByAxiom(axiom);
            System.out.println("Contexts by axiom: " + contextsByAxiom);
            contexts.addAll(contextsByAxiom);
        });
        if(contexts.isEmpty()){
            System.out.println("No contexts found for axiom");
            return new HashSet<>();
        }
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