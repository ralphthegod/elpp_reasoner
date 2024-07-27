package com.elppreasoner.taxonomy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.reasoner.taxonomy.Taxonomy;
import com.reasoner.taxonomy.TaxonomyBuilder;

public class ELPPTaxonomyBuilder implements TaxonomyBuilder {

    @Override
    public Taxonomy buildTaxonomy(Set<OWLSubClassOfAxiom> axioms) {
        Map<OWLClassExpression, Set<OWLClassExpression>> classToSuperConcepts = TaxonomyUtilities.computeTaxonomySuperConcepts(axioms);

        // TODO Auto-generated method stub
        //Map<OWLClassExpression, Set<OWLClassExpression>> directSuperclassesMap = new HashMap<>();
        //directSuperclassesMap.put(nothing, new HashSet<>());
        // [...]

        return null;
    }


}
