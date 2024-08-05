package com.elppreasoner.reasoning;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;

import com.elppreasoner.reasoning.rules.BottomSuperclassRoleExpansionInferenceRule;
import com.elppreasoner.reasoning.rules.IntersectionSuperclassesInferenceRule;
import com.elppreasoner.reasoning.rules.NominalChainExpansionInferenceRule;
import com.elppreasoner.reasoning.rules.SubclassRoleExpansionInferenceRule;
import com.elppreasoner.reasoning.rules.SuperclassRoleExpansionInferenceRule;
import com.elppreasoner.reasoning.rules.ToldSuperclassesInferenceRule;
import com.elppreasoner.taxonomy.ELPPTaxonomyBuilder;
import com.reasoner.reasoning.Reasoner;
import com.reasoner.taxonomy.Taxonomy;
import com.reasoner.taxonomy.TaxonomyBuilder;

/**
 * ELPP reasoner implementation. <p>
 */
public class ElppReasoner extends Reasoner {
    /**
     * The {@link TaxonomyBuilder} used to build the {@code taxonomy}. Declared in the constructor.
     */
    private final ELPPTaxonomyBuilder taxonomyBuilder;

    /**
     * The {@link Taxonomy} built in {@code computeClassHierarchy()}.
     */
    private Taxonomy taxonomy = null;

    /**
     * The time elapsed to saturate the given ontology, in seconds. 
     */
    private long saturationTime;

    /**
     * The time elapsed to build the taxonomy for the given saturated ontology, in seconds.
     */
    private long taxonomyBuildingTime;

    /**
     * Uses the given ontology accessor to create a new ELPP reasoner.
     * Dependency injection is used to provide the ontology accessor.
     * @param ontologyAccessor
     */
    public ElppReasoner(OWLOntology ontology, boolean saturationConcurrentMode, boolean taxonomyConcurrentMode) {
        super(ontology, saturationConcurrentMode);
        taxonomyBuilder = new ELPPTaxonomyBuilder(taxonomyConcurrentMode);
        initializeInferenceRules();
    }

    /**
     * Gets the taxonomy computed in {@code precomputeInferences(...)}, if {@code CLASS_HIERARCHY} inference type is provided.
     * @return The computed taxonomy
     */
    public Taxonomy getTaxonomy() {
        return taxonomy;
    }

    /**
     * Gets the time elapsed to saturate the given ontology, in seconds.
     * @return The saturation time
     */
    public long saturationTime() {
        return saturationTime;
    }

    /**
     * Gets the time elapsed to build the taxonomy for the given saturated ontology, in seconds.
     * @return The taxonomy building time
     */
    public long taxonomyBuildingTime() {
        return taxonomyBuildingTime;
    }

    private void initializeInferenceRules(){
        // Add inference rules
        addInferenceRule(new ToldSuperclassesInferenceRule()); // CR1
        addInferenceRule(new IntersectionSuperclassesInferenceRule()); // CR2       
        addInferenceRule(new SubclassRoleExpansionInferenceRule()); // CR3
        addInferenceRule(new SuperclassRoleExpansionInferenceRule()); // CR4
        addInferenceRule(new BottomSuperclassRoleExpansionInferenceRule()); // CR5
        addInferenceRule(new NominalChainExpansionInferenceRule()); // CR6

        // Add inference calculators
        addInferenceCalculator(InferenceType.CLASS_HIERARCHY, this::computeClassHierarchy);
    }

    /**
     * Saturates this reasoner's ontology and consequently computes the class hierarchy for the saturated ontology. Once the computation is over,
     * the {@code taxonomy}, the {@code saturationTime} and the {@code taxonomyBuildingTime} are stored.
     */
    private void computeClassHierarchy() {
        long time;

        // Saturation
        time = System.nanoTime();
        Set<OWLSubClassOfAxiom> conclusions = getOntologySaturator().saturate();
        saturationTime = (System.nanoTime() - time) / 1_000_000_000;

        // Taxonomy
        time = System.nanoTime();
        this.taxonomy = taxonomyBuilder.build(conclusions);
        taxonomyBuildingTime = (System.nanoTime() - time) / 1_000_000_000;
    }   
}