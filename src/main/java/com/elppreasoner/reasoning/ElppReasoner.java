package com.elppreasoner.reasoning;

import org.semanticweb.owlapi.reasoner.InferenceType;

import com.reasoner.querying.OntologyAccessManager;
import com.reasoner.reasoning.Reasoner;

/**
 * ELPP reasoner implementation. <p>
 */
public class ElppReasoner extends Reasoner {

    /**
     * Uses the given ontology accessor to create a new ELPP reasoner.
     * Dependency injection is used to provide the ontology accessor.
     * @param ontologyAccessor
     */
    public ElppReasoner(OntologyAccessManager ontologyAccessor) {
        super(ontologyAccessor);
        addInferenceCalculator(InferenceType.CLASS_HIERARCHY, this::computeClassHierarchy);
    }

    private void computeClassHierarchy() {
        // TODO: Implement the computation of the class hierarchy
    }
    
}
