package com.elppreasoner.reasoning;

import java.util.List;
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
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.Version;

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


    @Override
    public String getReasonerName() {
        return "ELPP Reasoner";
    }

    @Override
    public Version getReasonerVersion() {
        return new Version(0, 0, 0, 0);
    }
    
    @Override
    public void flush() {
        throw new UnsupportedOperationException("Unimplemented method 'flush'");
    }

    @Override
    public BufferingMode getBufferingMode() {
        throw new UnsupportedOperationException("Unimplemented method 'getBufferingMode'");
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        throw new UnsupportedOperationException("Unimplemented method 'getPendingAxiomAdditions'");
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        throw new UnsupportedOperationException("Unimplemented method 'getPendingAxiomRemovals'");
    }

    @Override
    public List<OWLOntologyChange> getPendingChanges() {
        throw new UnsupportedOperationException("Unimplemented method 'getPendingChanges'");
    }

    @Override
    public void interrupt() {
        throw new UnsupportedOperationException("Unimplemented method 'interrupt'");
    }

    @Override
    public boolean isConsistent() {
        throw new UnsupportedOperationException("Unimplemented method 'isConsistent'");
    }

    @Override
    public boolean isSatisfiable(OWLClassExpression classExpression) {        
        throw new UnsupportedOperationException("Unimplemented method 'isSatisfiable'");
    }

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

    @Override
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression ce) {
        throw new UnsupportedOperationException("Unimplemented method 'getDisjointClasses'");
    }

    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        throw new UnsupportedOperationException("Unimplemented method 'getTopObjectPropertyNode'");
    }

    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {        
        throw new UnsupportedOperationException("Unimplemented method 'getBottomObjectPropertyNode'");
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression pe, boolean direct) {        
        throw new UnsupportedOperationException("Unimplemented method 'getSubObjectProperties'");
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression pe, boolean direct) {
        throw new UnsupportedOperationException("Unimplemented method 'getSuperObjectProperties'");
    }

    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression pe) {        
        throw new UnsupportedOperationException("Unimplemented method 'getEquivalentObjectProperties'");
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression pe) {        
        throw new UnsupportedOperationException("Unimplemented method 'getDisjointObjectProperties'");
    }

    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression pe) {        
        throw new UnsupportedOperationException("Unimplemented method 'getInverseObjectProperties'");
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression pe, boolean direct) {        
        throw new UnsupportedOperationException("Unimplemented method 'getObjectPropertyDomains'");
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression pe, boolean direct) {        
        throw new UnsupportedOperationException("Unimplemented method 'getObjectPropertyRanges'");
    }

    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {        
        throw new UnsupportedOperationException("Unimplemented method 'getTopDataPropertyNode'");
    }

    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {        
        throw new UnsupportedOperationException("Unimplemented method 'getBottomDataPropertyNode'");
    }

    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty pe, boolean direct) {        
        throw new UnsupportedOperationException("Unimplemented method 'getSubDataProperties'");
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty pe, boolean direct) {        
        throw new UnsupportedOperationException("Unimplemented method 'getSuperDataProperties'");
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty pe) {        
        throw new UnsupportedOperationException("Unimplemented method 'getEquivalentDataProperties'");
    }

    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression pe) {        
        throw new UnsupportedOperationException("Unimplemented method 'getDisjointDataProperties'");
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty pe, boolean direct) {        
        throw new UnsupportedOperationException("Unimplemented method 'getDataPropertyDomains'");
    }

    @Override
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual ind, boolean direct) {        
        throw new UnsupportedOperationException("Unimplemented method 'getTypes'");
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression ce, boolean direct) {        
        throw new UnsupportedOperationException("Unimplemented method 'getInstances'");
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual ind, OWLObjectPropertyExpression pe) {        
        throw new UnsupportedOperationException("Unimplemented method 'getObjectPropertyValues'");
    }

    @Override
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual ind, OWLDataProperty pe) {        
        throw new UnsupportedOperationException("Unimplemented method 'getDataPropertyValues'");
    }

    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual ind) {        
        throw new UnsupportedOperationException("Unimplemented method 'getSameIndividuals'");
    }

    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual ind) {        
        throw new UnsupportedOperationException("Unimplemented method 'getDifferentIndividuals'");
    }

    @Override
    public long getTimeOut() {        
        throw new UnsupportedOperationException("Unimplemented method 'getTimeOut'");
    }

    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {        
        throw new UnsupportedOperationException("Unimplemented method 'getFreshEntityPolicy'");
    }

    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {        
        throw new UnsupportedOperationException("Unimplemented method 'getIndividualNodeSetPolicy'");
    }

    @Override
    public void dispose() {        
        throw new UnsupportedOperationException("Unimplemented method 'dispose'");
    }
}