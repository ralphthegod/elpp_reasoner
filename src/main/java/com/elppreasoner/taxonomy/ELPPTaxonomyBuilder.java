package com.elppreasoner.taxonomy;

import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.elppreasoner.taxonomy.TaxonomyUtilities.TaxonomyReductionPOJO;
import com.reasoner.taxonomy.Taxonomy;
import com.reasoner.taxonomy.TaxonomyBuilder;

/**
 * {@link ELPPTaxonomyBuilder} is a helper class that makes easier to "build" a taxonomy of EL++ classes.
 * 
 * The saturation phase computes the full transitively closed subsumption relation. However, the expected output of classification is a taxonomy
 * which only contains direct subsumptions between nodes representing equivalence classes of atomic concepts: this means that, if the taxonomy
 * contains A ⊑ B and B ⊑ C, then it should not contain A ⊑ C, unless some of these concepts are equivalent. Therefore, the computed subsumptions
 * between atomic concepts bust be transitively reduced.
 * 
 * And that's what this class does: it takes a set of axioms in input and builds a taxonomy that contains information about both direct subsumptions
 * between concepts and equivalent concepts. The returned taxonomy also contains information about non-direct subsumption about concepts: this makes
 * testing (A ⊑ B | A ⊑ ... ⊑ B | A ≡ B) much easier. In fact, equivalency and direct / non-direct subsumption information are stored in three
 * hashmaps: this means that all testing comes down to an hashmap query.
 * 
 * Please note that all subsumptions derived by the saturation algorithm that involve non-atomic concepts are discarded. We can assume that all
 * concepts are atomic. 
 * 
 * This class implements one interface:
 *     • {@link TaxonomyBuilder}, that you can find in the {@code com.reasoner.taxonomy} package. A Taxonomy Builder is an object that implements
 *       one method: {@code Taxonomy build(Set<OWLSubClassOfAxiom> axioms}).
 */
public class ELPPTaxonomyBuilder implements TaxonomyBuilder {

    /**
     * A method from the {@link TaxonomyBuilder} interface.
     * Given a set of @param axioms, returns a taxonomy of all the concepts involved, by computing:
     *     • Equivalence of concepts (i.e. A ≡ B)
     *     • Direct subsumption between concepts (i.e. A ⊑ B)
     *     • Generic subsumption between concepts (i.e. A ⊑ ... ⊑ B)
     * This is done in three steps:
     *     1. Compute all (super)concepts in the given @param axioms while discarding subsumptions involving non-atomic concepts
     *     2. Reduce all transitive subsumptions between concepts (e.g. if A ⊑ B and B ⊑ C, "ignore" A ⊑ C)
     *     3. Build the taxonomy based on output of the previous two steps
     * @param axioms The axioms of the ontology used to build the taxonomy
     * @return The @param taxonomy with information about equivalent concepts and direct / non-direct subsumptions between concepts
     */
    @Override
    public Taxonomy build(Set<OWLSubClassOfAxiom> axioms) {
        Map<OWLClassExpression, Set<OWLClassExpression>> classToAllSuperclasses = TaxonomyUtilities.computeTaxonomySuperConcepts(axioms);

        TaxonomyReductionPOJO reductionPOJO = TaxonomyUtilities.reduceTransitiveSubsumptions(classToAllSuperclasses);
        Map<OWLClassExpression, Set<OWLClassExpression>> classToDirectSuperclasses = reductionPOJO.getTaxonomyDirectSuperConcepts();
        Map<OWLClassExpression, Set<OWLClassExpression>> classToEquivalentSuperclasses = reductionPOJO.getTaxonomyEquivalentConcepts();

        return TaxonomyUtilities.buildTaxonomy(classToAllSuperclasses, classToEquivalentSuperclasses, classToDirectSuperclasses);
    }
}