package com.reasoner.reasoning;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.Version;

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
        if(ontologyNormalizer == null){
            throw new IllegalStateException("Ontology normalizer not set.");
        }
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
            System.out.println("Normalizer not defined. Skipping normalization.");
        }
        for (InferenceType inferenceType : inferenceTypes) {
            if(precomputedInferences.get(inferenceType)){
                System.out.println("Inference type already precomputed: " + inferenceType);
                continue;
            }
            if (inferenceCalculators.containsKey(inferenceType)) {
                inferenceCalculators.get(inferenceType).compute();
                precomputedInferences.put(inferenceType, true);
            }
            else{
                System.out.println("Inference type not supported: " + inferenceType);
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
    public String getReasonerName() {
        return "ELPP Reasoner";
    }

    // TODO: implement method
    @Override
    public Version getReasonerVersion() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getReasonerVersion'");
    }

    // TODO: implement method
    @Override
    public BufferingMode getBufferingMode() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getBufferingMode'");
    }

    // TODO: implement method
    @Override
    public void flush() {
        
        throw new UnsupportedOperationException("Unimplemented method 'flush'");
    }

    // TODO: implement method
    @Override
    public List<OWLOntologyChange> getPendingChanges() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPendingChanges'");
    }

    // TODO: implement method
    @Override
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPendingAxiomAdditions'");
    }

    // TODO: implement method
    @Override
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPendingAxiomRemovals'");
    }

    @Override
    public OWLOntology getRootOntology() {
        return ontologyAccessManager.getOntology();
    }

    // TODO: implement method
    @Override
    public void interrupt() {
        
        throw new UnsupportedOperationException("Unimplemented method 'interrupt'");
    }

    // TODO: implement method
    @Override
    public boolean isPrecomputed(InferenceType inferenceType) {
        
        throw new UnsupportedOperationException("Unimplemented method 'isPrecomputed'");
    }

    // TODO: implement method
    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getPrecomputableInferenceTypes'");
    }

    // TODO: implement method
    @Override
    public boolean isConsistent() {
        
        throw new UnsupportedOperationException("Unimplemented method 'isConsistent'");
    }

    // TODO: implement method
    @Override
    public boolean isSatisfiable(OWLClassExpression classExpression) {
        
        throw new UnsupportedOperationException("Unimplemented method 'isSatisfiable'");
    }

    // TODO: implement method
    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getUnsatisfiableClasses'");
    }

    // TODO: implement method
    @Override
    public boolean isEntailed(OWLAxiom axiom) {
        
        throw new UnsupportedOperationException("Unimplemented method 'isEntailed'");
    }

    // TODO: implement method
    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> axioms) {
        
        throw new UnsupportedOperationException("Unimplemented method 'isEntailed'");
    }

    // TODO: implement method
    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        
        throw new UnsupportedOperationException("Unimplemented method 'isEntailmentCheckingSupported'");
    }

    // TODO: implement method
    @Override
    public Node<OWLClass> getTopClassNode() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTopClassNode'");
    }

    // TODO: implement method
    @Override
    public Node<OWLClass> getBottomClassNode() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getBottomClassNode'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSubClasses'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression ce, boolean direct) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSuperClasses'");
    }

    // TODO: implement method
    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression ce) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getEquivalentClasses'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getDisjointClasses'");
    }

    // TODO: implement method
    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTopObjectPropertyNode'");
    }

    // TODO: implement method
    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getBottomObjectPropertyNode'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression pe, boolean direct) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSubObjectProperties'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression pe,
            boolean direct) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSuperObjectProperties'");
    }

    // TODO: implement method
    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression pe) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getEquivalentObjectProperties'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression pe) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getDisjointObjectProperties'");
    }

    // TODO: implement method
    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression pe) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getInverseObjectProperties'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression pe, boolean direct) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getObjectPropertyDomains'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression pe, boolean direct) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getObjectPropertyRanges'");
    }

    // TODO: implement method
    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTopDataPropertyNode'");
    }

    // TODO: implement method
    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getBottomDataPropertyNode'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty pe, boolean direct) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSubDataProperties'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty pe, boolean direct) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSuperDataProperties'");
    }

    // TODO: implement method
    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty pe) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getEquivalentDataProperties'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression pe) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getDisjointDataProperties'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe, boolean direct) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getDataPropertyDomains'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual ind, boolean direct) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTypes'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce, boolean direct) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getInstances'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual ind, OWLObjectPropertyExpression pe) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getObjectPropertyValues'");
    }

    // TODO: implement method
    @Override
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ind, OWLDataProperty pe) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getDataPropertyValues'");
    }

    // TODO: implement method
    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual ind) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getSameIndividuals'");
    }

    // TODO: implement method
    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual ind) {
        
        throw new UnsupportedOperationException("Unimplemented method 'getDifferentIndividuals'");
    }

    // TODO: implement method
    @Override
    public long getTimeOut() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getTimeOut'");
    }

    // TODO: implement method
    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getFreshEntityPolicy'");
    }

    // TODO: implement method
    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        
        throw new UnsupportedOperationException("Unimplemented method 'getIndividualNodeSetPolicy'");
    }

    // TODO: implement method
    @Override
    public void dispose() {
        
        throw new UnsupportedOperationException("Unimplemented method 'dispose'");
    }
    
}