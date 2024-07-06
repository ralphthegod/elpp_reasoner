package com.reasoner.normalization;

import org.semanticweb.owlapi.model.OWLOntology;

public interface OntologyNormalizer {
    OWLOntology normalize(OWLOntology ontology);
}
