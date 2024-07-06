package com.elppreasoner.reasoning;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;

import com.elppreasoner.querying.IndexedOntologyAccessor;
import com.reasoner.querying.OntologyAccessor;
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
    public ElppReasoner(OntologyAccessor ontologyAccessor) {
        super(ontologyAccessor);
        addInferenceCalculator(InferenceType.CLASS_HIERARCHY, this::computeClassHierarchy);
    }

    /**
     * Uses the given ontology to create a new ELPP reasoner.<p>
     * It uses {@code IndexedOntologyAccessor} to access the ontology.
     * @param ontology
     */
    public ElppReasoner(OWLOntology ontology) {
        this(new IndexedOntologyAccessor(ontology));
    }


    private void computeClassHierarchy() {
        // TODO: Implement the computation of the class hierarchy
    }
    
}
