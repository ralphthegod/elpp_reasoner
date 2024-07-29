package com.elppreasoner.taxonomy;

import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.elppreasoner.taxonomy.TaxonomyUtilities.TaxonomyReductionPOJO;
import com.reasoner.taxonomy.Taxonomy;
import com.reasoner.taxonomy.TaxonomyBuilder;

// TODO: javadoc
public class ELPPTaxonomyBuilder implements TaxonomyBuilder {

    @Override
    public Taxonomy build(Set<OWLSubClassOfAxiom> axioms) {
        Map<OWLClassExpression, Set<OWLClassExpression>> classToAllSuperclasses = TaxonomyUtilities.computeTaxonomySuperConcepts(axioms);

        TaxonomyReductionPOJO reductionPOJO = TaxonomyUtilities.reduceTransitiveSubsumptions(classToAllSuperclasses);
        Map<OWLClassExpression, Set<OWLClassExpression>> classToDirectSuperclasses = reductionPOJO.getTaxonomyDirectSuperConcepts();
        Map<OWLClassExpression, Set<OWLClassExpression>> classToEquivalentSuperclasses = reductionPOJO.getTaxonomyEquivalentConcepts();

        return TaxonomyUtilities.buildTaxonomy(classToAllSuperclasses, classToEquivalentSuperclasses, classToDirectSuperclasses);
    }
}