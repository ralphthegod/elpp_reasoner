package com.elppreasoner.reasoning;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
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
import org.semanticweb.owlapi.reasoner.UnsupportedEntailmentTypeException;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.util.Version;

import com.elppreasoner.normalization.ELPPOntologyNormalizer;
import com.elppreasoner.normalization.NormalizationUtilities;
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
public class ELPPReasoner extends Reasoner {
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
    private double saturationTime;

    /**
     * The time elapsed to build the taxonomy for the given saturated ontology, in seconds.
     */
    private double taxonomyBuildingTime;

    /**
     * Exception message to throw when a taxonomy has not been computed yet.
     */
    private static final String TAXONOMY_NOT_COMPUTED_YET = "No taxonomy has been computed yet. Please, call precomputeInferences(InferenceType.CLASS_HIERARCHY) on this reasoner.";


    /**
     * Uses the given ontology accessor to create a new ELPP reasoner.
     * Dependency injection is used to provide the ontology accessor.
     * @param ontologyAccessor
     */
    public ELPPReasoner(OWLOntology ontology, boolean saturationConcurrentMode, boolean taxonomyConcurrentMode) {
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
    public double getSaturationTime() {
        return saturationTime;
    }

    /**
     * Gets the time elapsed to build the taxonomy for the given saturated ontology, in seconds.
     * @return The taxonomy building time
     */
    public double getTaxonomyBuildingTime() {
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
        double time;

        // Saturation
        time = System.nanoTime();
        Set<OWLSubClassOfAxiom> conclusions = getOntologySaturator().saturate();
        this.saturationTime = (System.nanoTime() - time) / 1_000_000_000;

        // Taxonomy
        time = System.nanoTime();
        this.taxonomy = taxonomyBuilder.build(conclusions);
        this.taxonomyBuildingTime = (System.nanoTime() - time) / 1_000_000_000;
    }


    /**
     * Custom exception to be thrown when a taxonomy has not been computed yet.
     */
    public class NullTaxonomyException extends RuntimeException { 
        public NullTaxonomyException(String msg) {
            super(msg);
        }
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

    // TODO: implement method?
    @Override
    public boolean isConsistent() {
        throw new UnsupportedOperationException("Unimplemented method 'isConsistent'");
    }

    // TODO: implement method?
    @Override
    public boolean isSatisfiable(OWLClassExpression classExpression) {        
        throw new UnsupportedOperationException("Unimplemented method 'isSatisfiable'");
    }

    // TODO: implement method?
    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        throw new UnsupportedOperationException("Unimplemented method 'getUnsatisfiableClasses'");
    }

    /**
     * Normalizes a given axiom and checks if it is entailed.
     * @param axiom The axiom to normalize and whose entailment is checked
     * @return {@code true} if the given axiom post-normalization is entailed; {@code false} otherwise
     */
    private boolean normalizeAndCheckEntailment(OWLAxiom axiom) {
        Map<OWLClass, OWLClassNode> classToNode = taxonomy.getClassToNode();

        ELPPOntologyNormalizer normalizer = new ELPPOntologyNormalizer();
        Set<OWLAxiom> axiomsToNormalize = new HashSet<>();
        axiomsToNormalize.add(axiom);
        Set<OWLAxiom> normalizedAxiom = normalizer.normalize(axiomsToNormalize);
        
        for (OWLAxiom a : normalizedAxiom) {
            OWLSubClassOfAxiom sAxiom = (OWLSubClassOfAxiom) a;
            if (!classToNode.containsKey(sAxiom.getSubClass()) || !classToNode.containsKey(sAxiom.getSuperClass())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEntailed(OWLAxiom axiom) {
        if (!isEntailmentCheckingSupported(axiom.getAxiomType())) {
            throw new UnsupportedEntailmentTypeException(axiom);
        }

        if (taxonomy == null) {
            throw new NullTaxonomyException(TAXONOMY_NOT_COMPUTED_YET);
        }
        
        AxiomType<?> axiomType = axiom.getAxiomType();

        Map<OWLClass, OWLClassNode> classToNode = taxonomy.getClassToNode();

        if (Objects.equals(axiomType, AxiomType.SUBCLASS_OF)) {
            OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
            if (NormalizationUtilities.isGCIInNormalForm(subClassOfAxiom)) {
                return classToNode.containsKey(subClassOfAxiom.getSubClass()) && classToNode.containsKey(subClassOfAxiom.getSuperClass());
            }

            return normalizeAndCheckEntailment(axiom);
        }

        if (Objects.equals(axiomType, AxiomType.EQUIVALENT_CLASSES)) {
            OWLEquivalentClassesAxiom equivalentClassesAxioms = (OWLEquivalentClassesAxiom) axiom;
            Iterator<OWLSubClassOfAxiom> it = equivalentClassesAxioms.asOWLSubClassOfAxioms().iterator();
            
            // This is safe: an EQUIVALENT_CLASSES axiom (A ≡ B) always has two axioms (in the following code, the retrievement from the iterator is performed twice)
            return normalizeAndCheckEntailment((OWLAxiom) it.next()) && normalizeAndCheckEntailment((OWLAxiom) it.next());
        }

        return false;
    }

    @Override
    public boolean isEntailed(Set<? extends OWLAxiom> axioms) {
        for(OWLAxiom axiom : axioms){
            if(!isEntailed(axiom)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEntailmentCheckingSupported(AxiomType<?> axiomType) {
        return Objects.equals(axiomType, AxiomType.SUBCLASS_OF) || Objects.equals(axiomType, AxiomType.EQUIVALENT_CLASSES);
    }

    @Override
    public Node<OWLClass> getTopClassNode() {
        if (taxonomy == null) {
            throw new NullTaxonomyException(TAXONOMY_NOT_COMPUTED_YET);
        }
        return taxonomy.getTopClassNode();
    }

    @Override
    public Node<OWLClass> getBottomClassNode() {
        if (taxonomy == null) {
            throw new NullTaxonomyException(TAXONOMY_NOT_COMPUTED_YET);
        }
        return taxonomy.getBottomClassNode();
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression ce, boolean direct) {
        if (taxonomy == null) {
            throw new NullTaxonomyException(TAXONOMY_NOT_COMPUTED_YET);
        }
        return taxonomy.getSubClasses(ce, direct);
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression ce, boolean direct) {
        if (taxonomy == null) {
            throw new NullTaxonomyException(TAXONOMY_NOT_COMPUTED_YET);
        }
        return taxonomy.getSuperClasses(ce, direct);
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression ce) {
        if (taxonomy == null) {
            throw new NullTaxonomyException(TAXONOMY_NOT_COMPUTED_YET);
        }
        return taxonomy.getEquivalentClasses(ce);
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