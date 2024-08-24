package com.reasoner.saturation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.reasoner.querying.OntologyAccessManager;
import com.reasoner.reasoning.rules.InferenceRule;

/**
 * {@code OntologySaturator} is responsible for saturating the ontology. <p>
 * 
 * The <b>saturation process</b> applies exhaustively the {@link InferenceRule}s to the axioms of a given TBox. <p>
 * The result is a TBox T' called <i>saturated TBox</i>, such that: <p>
 * 
 * For each concept name A and B (appearing in TBox T) we got <p> 
 * 
 *      T |= A ⊑ B ⇐⇒ A ⊑ B ∈ T' <p>
 * 
 * It uses a {@link ContextAccessManager} to manage the contexts and a {@link SaturationThread} to process the axioms. <p>
 * The idea behind this saturator is to put the axioms in <b>contexts</b> that can be processed independently by the threads.
 * More theorically, the saturator is based on a property implying that if it's possible to apply an {@link InferenceRule} to a set of axioms, these should be in the same context. <p>
 */
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

    /**
     * Saturate the ontology.
     * @return Set of conclusions.
     */
    public Set<OWLSubClassOfAxiom> saturate() {
        if (ontologyAccessManager.getRules().isEmpty()) {
            throw new RuntimeException("No inference rule has been added to this saturator yet. You can add InferenceRules by using registerRule() on the saturator's OntologyAccessManager.");
        }

        if(!ontologyAccessManager.isIndexed()){
            ontologyAccessManager.precomputeAxioms();
        }

        contextManager.initialize(ontologyAccessManager);

        final int cpuCount = concurrentMode ? 
            Runtime.getRuntime().availableProcessors() : 1;
        Set<SaturationThread> threads = new HashSet<>();
        for (int i = 0; i < cpuCount; i++) {
            threads.add(new SaturationThread(contextManager));
        }

        //System.out.println("Saturating ontology with " + cpuCount + " thread(s)...");

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

        //System.out.println("Discarded axioms: " + contextManager.getDiscardedAxioms().size());

        conclusions.addAll(contextManager.getDiscardedAxioms());

        return conclusions;
    }

    public OntologyAccessManager getOntologyAccessManager() {
        return this.ontologyAccessManager;
    }
}