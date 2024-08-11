package com.reasoner.utils;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectOneOf;

import com.reasoner.reasoning.rules.OWLEntityType;

/**
 * Utility class for ontology operations.
 * <p>
 * This class provides utility methods for ontology operations.
 * </p>
 */
public final class OntologyUtilities {

    /**
     * Get the entity type.
     * @param entity
     * @return
     */
    public static OWLEntityType getEntityType(OWLEntity entity){
        if(entity instanceof OWLClass){
            return OWLEntityType.CLASS;
        } else if(entity instanceof OWLIndividual){
            return OWLEntityType.INDIVIDUAL;
        } else {
            return OWLEntityType.ENTITY;
        }
    }

    public static OWLClassExpression getEntityClassExpression(OWLEntity entity){
        if(entity instanceof OWLClass){
            return (OWLClass) entity;
        } else if(entity instanceof OWLIndividual){
            OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
            OWLObjectOneOf owlObjectOneOf = owlDataFactory.getOWLObjectOneOf((OWLIndividual) entity);
            return owlObjectOneOf;
        } else {
            return null;
        }
    }

    /**
     * Get the entity type by class.
     * @param entityType
     * @return
     */
    public static OWLEntityType getEntityTypeByClass(Class<? extends OWLObject> entityType){
        if(OWLClass.class.isAssignableFrom(entityType)){
            return OWLEntityType.CLASS;
        } else if(OWLIndividual.class.isAssignableFrom(entityType)){
            return OWLEntityType.INDIVIDUAL;
        } else {
            return OWLEntityType.ENTITY;
        }
    }
}
