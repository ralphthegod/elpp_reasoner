package com.reasoner.querying;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Inference rule. <p>
 * This class provides methods to add axioms based on inference rules. 
 * Each inference rule groups axioms that are inferred based on a certain criterion. <p>
 * Supports caching. <p>
 */
public abstract class InferenceRule<S,T> {

    protected Map<S,T> axioms;
    private Class<? extends OWLEntity> entityType;

    public InferenceRule(Class<? extends OWLEntity> entityType){
        this.entityType = entityType;
    }

    /**
     * Check if the axiom criterion is satisfied.
     * @param subclass
     * @param superclass
     * @return
     */
    public abstract boolean axiomCriterion(OWLClassExpression subclass, OWLClassExpression superclass);

    /**
     * Add an axiom.
     * @param subclass
     * @param superclass
     */
    public abstract void addAxiom(OWLClassExpression subclass, OWLClassExpression superclass);

    /**
     * Get the axioms.
     * @return Map of axioms.
     */
    public Map<S,T> getAxioms(){
        return axioms;
    }

    /**
     * Clear the axioms.
     */
    public void clearAxioms(){
        axioms.clear();
    }

    /**
     * Get the entity type.
     * @return
     */
    public Class<? extends OWLEntity> getEntityType(){
        return entityType;
    }
}
