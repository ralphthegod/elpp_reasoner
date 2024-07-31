package com.elppreasoner.taxonomy;

import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.elppreasoner.taxonomy.TaxonomyUtilities.TaxonomyReductionPOJO;
import com.reasoner.taxonomy.Taxonomy;
import com.reasoner.taxonomy.TaxonomyBuilder;

/**
 * <p>{@link ELPPTaxonomyBuilder} is a helper class that makes easier to "build" a taxonomy of EL++ classes.</p>
 * 
 * <p>The saturation phase computes the full transitively closed subsumption relation. However, the expected output of classification is a taxonomy
 * which only contains direct subsumptions between nodes representing equivalence classes of atomic concepts: this means that, if the taxonomy
 * contains A ⊑ B and B ⊑ C, then it should not contain A ⊑ C, unless some of these concepts are equivalent. Therefore, the computed subsumptions
 * between atomic concepts bust be transitively reduced.</p>
 * 
 * <p>And that's what this class does: it takes a set of axioms in input and builds a taxonomy that contains information about both direct subsumptions
 * between concepts and equivalent concepts. The returned taxonomy also contains information about non-direct subsumption about concepts: this makes
 * testing (A ⊑ B | A ⊑ ... ⊑ B | A ≡ B) much easier. In fact, equivalency and direct / non-direct subsumption information are stored in three
 * hashmaps: this means that all testing comes down to an hashmap query.</p>
 * 
 * <p>Please note that all subsumptions derived by the saturation algorithm that involve non-atomic concepts are discarded. We can assume that all
 * concepts are atomic.</p> 
 * 
 * <p>This class implements one interface:
 * <ul>
 *     <li>{@link TaxonomyBuilder}, that you can find in the {@code com.reasoner.taxonomy} package. A Taxonomy Builder is an object that implements
 *       one method: {@code Taxonomy build(Set<OWLSubClassOfAxiom> axioms}).</li>
 * </ul></p>
 */
public class ELPPTaxonomyBuilder implements TaxonomyBuilder {
    /**
     * Boolean value that tells whether the taxonomy has to be built in parallel {@code (True)} or not {@code (False)}.
     */
    private boolean concurrentMode;

    /**
     * The public constructor of the taxonomy builder. The set of axioms is not required: the taxonomy builder is a helper class that, once
     * instantiated, can be used to build the taxonomy for any set of axioms by calling the method Taxonomy build(Set<OWLSubClassOfAxiom> axioms)}.
     * Similar to any Manager class.
     * @param isConcurrent Tells whether the taxonomy has to be built in parallel {@code (True)} or not {@code (False)}.
     */
    public ELPPTaxonomyBuilder(boolean isConcurrent) {
        this.concurrentMode = isConcurrent;
    }


    /**
     * <p>A method from the {@link TaxonomyBuilder} interface.</p>
     * 
     * <p>Given a set of {@code axioms}, returns a taxonomy of all the concepts involved, by computing:
     * <ul>
     *     <li>Equivalence of concepts (i.e. A ≡ B)</li>
     *     <li>Direct subsumption between concepts (i.e. A ⊑ B)</li>
     *     <li>Generic subsumption between concepts (i.e. A ⊑ ... ⊑ B)</li>
     * </ul></p>
     * 
     * <p>This is done in three steps:
     * <ol>
     *     <li>Compute all (super)concepts in the given {@code axioms} while discarding subsumptions involving non-atomic concepts</li>
     *     <li>Reduce all transitive subsumptions between concepts (e.g. if A ⊑ B and B ⊑ C, "ignore" A ⊑ C)</li>
     *     <li>Build the taxonomy based on output of the previous two steps</li>
     * </ol></p>
     * @param axioms The axioms of the ontology used to build the taxonomy
     * @return The taxonomy with information about equivalent concepts and direct / non-direct subsumptions between concepts
     */
    @Override
    public Taxonomy build(Set<OWLSubClassOfAxiom> axioms) {
        Map<OWLClassExpression, Set<OWLClassExpression>> classToAllSuperclasses = TaxonomyUtilities.computeTaxonomySuperConcepts(axioms);

        TaxonomyReductionPOJO reductionPOJO = TaxonomyUtilities.reduceTransitiveSubsumptions(classToAllSuperclasses, this.concurrentMode);
        Map<OWLClassExpression, Set<OWLClassExpression>> classToDirectSuperclasses = reductionPOJO.getTaxonomyDirectSuperConcepts();
        Map<OWLClassExpression, Set<OWLClassExpression>> classToEquivalentSuperclasses = reductionPOJO.getTaxonomyEquivalentConcepts();

        return TaxonomyUtilities.buildTaxonomy(classToAllSuperclasses, classToEquivalentSuperclasses, classToDirectSuperclasses);
    }
}