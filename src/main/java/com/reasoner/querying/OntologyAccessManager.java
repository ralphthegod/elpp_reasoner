package com.reasoner.querying;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.reasoner.reasoning.rules.InferenceRule;

@SuppressWarnings("rawtypes")

/**
 * The {@code OntologyAccessManager} class provides safe access to the ontology. <p>
 * This class provides methods to access the ontology and extract axioms based on rules. <p>
 * Indexing can be enabled to speed up the process of extracting axioms. <p>
 */
public class OntologyAccessManager {

    private OWLOntology ontology;
    private final Map<Class<? extends InferenceRule>, InferenceRule> rules = new HashMap<>();
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
     * Check if the ontology is indexed.
     * @return boolean
     */
    public boolean isIndexed() {
        return isIndexed;
    }

    /**
     * Get the axioms for the given key.
     * @param key
     * @return Map of axioms (subclass, superclass).
     */
    public void registerRule(InferenceRule inferenceRule) {
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

    /**
     * Precompute axioms for all rules. <p>
     * This method is used to speed up the process of extracting axioms.
     */
    public void precomputeAxioms(){
        rules.forEach((ruleType, rule) -> {
            rule.clearAxioms();
            getAxiomsByRule(ruleType);
        });
        isIndexed = true;
    }

    /**
     * Get the rules.
     * @return list of rules
     */ 
    public Collection<InferenceRule> getRules() {
        return rules.values();
    }
    
}
