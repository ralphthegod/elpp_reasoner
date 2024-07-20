package com.reasoner.saturation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.reasoner.querying.OntologyAccessManager;

public class OntologySaturator {

    private final OntologyAccessManager ontologyAccessManager;
    private final ContextAccessManager contextManager;
    private final boolean concurrentMode;

    public OntologySaturator(
        OntologyAccessManager ontologyAccessManager, 
        ContextAccessManager contextManager
    ) {
        this(ontologyAccessManager, contextManager, false);
    }

    public OntologySaturator(
        OntologyAccessManager ontologyAccessManager, 
        ContextAccessManager contextManager, 
        boolean isConcurrent
    ) {
        this.ontologyAccessManager = ontologyAccessManager;
        this.contextManager = contextManager;
        this.concurrentMode = isConcurrent;
    }

    public Set<OWLSubClassOfAxiom> saturate() {
        contextManager.initialize(ontologyAccessManager);

        final int cpuCount = concurrentMode ? 
            Runtime.getRuntime().availableProcessors() : 1;
        Set<SaturationThread> threads = new HashSet<>();
        for (int i = 0; i < cpuCount; i++) {
            threads.add(new SaturationThread(contextManager));
        }

        // Blocking
        threads.forEach(Thread::start);
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();

        threads.forEach(thread -> {
            Collection<InferenceRuleContext> processedContexts = thread.getProcessedContexts();
            if(!processedContexts.isEmpty()){
                processedContexts.forEach(context -> {
                    conclusions.addAll(context.getProcessedAxioms());
                });
            }
        });

        return conclusions;
    }

}