package com.reasoner.querying;

import java.util.HashMap;
import java.util.Map;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

@SuppressWarnings("rawtypes")

/**
 * Class to access the ontology. <p>
 * This class provides methods to access the ontology and extract axioms based on rules. <p>
 * Indexing can be enabled to speed up the process of extracting axioms. <p>
 */
public abstract class OntologyAccessManager {

    private OWLOntology ontology;
    private Map<Class<? extends InferenceRule>, InferenceRule> rules = new HashMap<>();
    private boolean isIndexed = false;

    /**
     * Constructor for OntologyAccessor
     * @param ontology root ontology
     * @param indexing whether to index the ontology
     */
    public OntologyAccessManager(OWLOntology ontology) {
        this.ontology = ontology;
    }

    /**
     * Get the axioms for the given key.
     * @param key
     * @return Map of axioms (subclass, superclass).
     */
    protected void registerRule(InferenceRule inferenceRule) {
        rules.put(inferenceRule.getClass(), inferenceRule);
    }

    /**
     * Get the ontology. <p>
     * BE CAREFUL: Do not access the ontology for writing purposes.
     * @return OWLOntology
     */
    public OWLOntology getOntology() {
        return ontology;
    }

    /**
     * Set the ontology.
     * @param ontology
     */
    public void setOntology(OWLOntology ontology) {
        this.ontology = ontology;
        if(isIndexed) {
            precomputeAxioms();
        }
    }

    /**
     * Get the axioms for the given key.
     * @param key
     * @return Map of axioms (subclass, superclass).
     */
    public Map<?, ?> getAxiomsByRule(Class<? extends InferenceRule> inferenceRuleType) {
        if(!rules.get(inferenceRuleType).getAxioms().isEmpty()) {
            return rules.get(inferenceRuleType).getAxioms();
        }
        ontology.axioms(AxiomType.SUBCLASS_OF).forEach(axiom -> {
            OWLSubClassOfAxiom subClassOfAxiom = (OWLSubClassOfAxiom) axiom;
            OWLClassExpression subclass = subClassOfAxiom.getSubClass();
            OWLClassExpression superclass = subClassOfAxiom.getSuperClass();
            InferenceRule rule = rules.get(inferenceRuleType);
            if (rule.axiomCriterion(subclass, superclass)) {
                rule.addAxiom(subclass, superclass);
            }
        });
        return rules.get(inferenceRuleType).getAxioms();
    }

    public void precomputeAxioms(){
        rules.forEach((ruleType, rule) -> {
            rule.clearAxioms();
            getAxiomsByRule(ruleType);
        });
        isIndexed = true;
    }
    
}
