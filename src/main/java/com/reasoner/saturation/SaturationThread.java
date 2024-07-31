package com.reasoner.saturation;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 *  {@code SaturationThread} is a thread that processes axioms in the context of an inference rule.<p>
 */
public class SaturationThread extends Thread {
    private final ContextAccessManager contextAccessManager;
    private final Set<InferenceRuleContext> processedContexts = new HashSet<>();
    private OWLSubClassOfAxiom computingAxiom;

    public SaturationThread(ContextAccessManager contextAccessManager) {
        this.contextAccessManager = contextAccessManager;
    }
    
    @Override
    public void run() {
        for (;;) {
            InferenceRuleContext context = contextAccessManager.getActiveContexts().poll();
            if (context == null) {
                break;
            }
            for(;;){
                OWLSubClassOfAxiom axiom = context.pollScheduledAxiom();

                if (axiom == null) {
                    break;
                }

                computingAxiom = axiom;

                if (context.hasProcessedAxiom(axiom)) {
                    continue;
                }

                context.addProcessedAxiom(axiom);
                processAxiom(context);
            }

            contextAccessManager.getActiveContexts().deactivateContext(context);
        
        }
    }

    private void processAxiom(InferenceRuleContext context) {
        Set<OWLSubClassOfAxiom> conlcusions = context.compute(computingAxiom);
        processedContexts.add(context);
        conlcusions.forEach((axiom) -> {
            contextAccessManager.getContextsByAxiom(axiom).forEach((ctx) -> {
                ctx.scheduleAxiom(axiom);
                contextAccessManager.getActiveContexts().activateContext(ctx);
            });
        });
    }

    /**
     * Get the axiom that is currently being processed.
     * @return {@code OWLSubClassOfAxiom} computingAxiom.
     */
    public OWLSubClassOfAxiom getComputingAxiom() {
        return computingAxiom;
    }

    /**
     * Get the processed contexts.
     * @return {@code Set<InferenceRuleContext>} processedContexts.
     */
    public Set<InferenceRuleContext> getProcessedContexts() {
        return processedContexts;
    }

}
