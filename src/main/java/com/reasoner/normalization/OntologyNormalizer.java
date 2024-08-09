package com.reasoner.normalization;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

public interface OntologyNormalizer {
    OWLOntology normalize(OWLOntology ontology);
    Set<OWLAxiom> normalize(Set<OWLAxiom> axioms);
}
