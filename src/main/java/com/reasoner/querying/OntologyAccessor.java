package com.reasoner.querying;

import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Class to access the ontology
 */
public class OntologyAccessor {

    private OWLOntology ontology;

    /**
     * Constructor for OntologyAccessor
     * @param ontology
     */
    public OntologyAccessor(OWLOntology ontology) {
        this.ontology = ontology;
    }

    /**
     * Get the ontology. <p>
     * BE CAREFUL: Do not access the ontology for writing purposes.
     * @return OWLOntology
     */
    public OWLOntology getOntology() {
        return ontology;
    }

    /**
     * Set the ontology.
     * @param ontology
     */
    public void setOntology(OWLOntology ontology) {
        this.ontology = ontology;
    }
}
