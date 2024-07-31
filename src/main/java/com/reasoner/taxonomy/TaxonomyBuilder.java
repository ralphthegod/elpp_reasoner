package com.reasoner.taxonomy;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public interface TaxonomyBuilder {
    public Taxonomy build(Set<OWLSubClassOfAxiom> axioms);
}