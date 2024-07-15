package com.reasoner.reasoning.rules;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import com.reasoner.saturation.InferenceRuleContext;
import com.reasoner.utils.OntologyUtilities;

/**
 * Inference rule. <p>
 * This class provides methods to add axioms based on inference rules. 
 * Each inference rule groups axioms that are inferred based on a certain criterion. <p>
 * Supports caching. <p>
 */
public abstract class InferenceRule<S,T> {

    protected Map<S,T> axioms;
    private final OWLEntityType entityType;
    private final Class<? extends InferenceRuleContext> contextType;

    public InferenceRule(Class<? extends OWLObject> entityType, Class<? extends InferenceRuleContext> contextType){
        this.entityType = OntologyUtilities.getEntityTypeByClass(entityType.asSubclass(OWLEntity.class));
        this.contextType = contextType;
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
    public OWLEntityType getEntityType(){
        return entityType;
    }

    /**
     * Get the context type.
     * @return
     */
    public Class<? extends InferenceRuleContext> getContextType(){
        return contextType;
    }
}
