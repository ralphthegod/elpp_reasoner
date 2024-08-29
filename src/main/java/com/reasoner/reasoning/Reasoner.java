package com.reasoner.reasoning;

import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.reasoner.normalization.OntologyNormalizer;
import com.reasoner.querying.OntologyAccessManager;
import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.ContextAccessManager;
import com.reasoner.saturation.OntologySaturator;

/**
 *  Reasoner class is the main class that implements the OWLReasoner interface.
 * It provides the basic functionality of a reasoner such as precomputing inferences,
 * checking consistency, checking entailment, etc.
 * It also provides indexing and normalization of the ontology.
 */
public abstract class Reasoner implements OWLReasoner {

    protected Map<InferenceType, Boolean> precomputedInferences;
    protected Map<InferenceType, InferenceCalculator> inferenceCalculators;
    private final OntologyAccessManager ontologyAccessManager;
    private OntologyNormalizer ontologyNormalizer;
    private final OntologySaturator ontologySaturator;

    /**
     * Functional interface for inference calculation.
     * This is used to calculate the inferences for a given inference type. <p>
     * The {@code compute()} method is implemented by the concrete reasoner class.
     */
    @FunctionalInterface
    protected interface InferenceCalculator {
        void compute();
    }

    /**
     * Constructor for the Reasoner class.
     * Dependency injection of the OntologyAccessor object.
     * @param ontologyAccessor The OntologyAccessor object
     */
    public Reasoner(OWLOntology ontology, boolean saturationConcurrentMode){
        ontologyAccessManager = new OntologyAccessManager(ontology);
        ontologySaturator = new OntologySaturator(ontologyAccessManager, new ContextAccessManager(), saturationConcurrentMode);

        precomputedInferences = new java.util.HashMap<>();
        inferenceCalculators = new java.util.HashMap<>();
    }

    /**
     * Set the OntologyNormalizer object.
     * Needed for normalizing the ontology.
     * Without this object, normalization will not happen.
     * @param ontologyNormalizer
     */
    public void setOntologyNormalizer(OntologyNormalizer ontologyNormalizer){
        this.ontologyNormalizer = ontologyNormalizer;
    }

    /**
     * Normalize the ontology.
     * This method normalizes the ontology using the OntologyNormalizer object.
     * The normalized ontology is set back to the OntologyAccessor object.
     */
    protected void normalizeOntology(){
        OWLOntology normalizedOntology = ontologyNormalizer.normalize(ontologyAccessManager.getOntology());
        ontologyAccessManager.setOntology(normalizedOntology);
    }

    protected void addInferenceCalculator(InferenceType inferenceType, InferenceCalculator inferenceCalculator){
        inferenceCalculators.put(inferenceType, inferenceCalculator);
        precomputedInferences.put(inferenceType, false);
    }

    /**
     * Add an inference rule to the reasoner.
     * The inference rule is registered with the OntologyAccessor object.
     * The context type for the inference rule is registered with the {@code OntologySaturator} object.
     * @param inferenceRule The inference rule
     * @param contextType The context type for the inference rule
     */
    protected void addInferenceRule(InferenceRule inferenceRule){
        ontologyAccessManager.registerRule(inferenceRule);
    }

    @Override
    public void precomputeInferences(InferenceType... inferenceTypes) {
        if(ontologyNormalizer != null){
            normalizeOntology();
        }
        else{
            //System.out.println("Normalizer not defined. Skipping normalization.");
        }
        for (InferenceType inferenceType : inferenceTypes) {
            if(isPrecomputed(inferenceType)){
                //System.out.println("Inference type already precomputed: " + inferenceType);
                continue;
            }
            if (inferenceCalculators.containsKey(inferenceType)) {
                inferenceCalculators.get(inferenceType).compute();
                precomputedInferences.put(inferenceType, true);
            }
            else{
                //System.out.println("Inference type not supported: " + inferenceType);
            }
        }
    }

    /**
     * Gets the ontology saturator.
     * @return The ontology saturator of this reasoner
     */
    public OntologySaturator getOntologySaturator() {
        return ontologySaturator;
    }


    @Override
    public OWLOntology getRootOntology() {
        return ontologyAccessManager.getOntology();
    }

    @Override
    public boolean isPrecomputed(InferenceType inferenceType) {
        Boolean precomputed = precomputedInferences.get(inferenceType);
        return precomputed != null && precomputed;
    }

    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        return inferenceCalculators.keySet();
    }
}