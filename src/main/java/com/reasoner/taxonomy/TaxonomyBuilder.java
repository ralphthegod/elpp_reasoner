package com.reasoner.taxonomy;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public interface TaxonomyBuilder {
    public Taxonomy buildTaxonomy(Set<OWLSubClassOfAxiom> axioms);
}