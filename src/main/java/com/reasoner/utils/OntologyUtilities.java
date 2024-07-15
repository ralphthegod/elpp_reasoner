package com.reasoner.utils;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;

import com.reasoner.reasoning.rules.OWLEntityType;

/**
 * Utility class for ontology operations.
 * <p>
 * This class provides utility methods for ontology operations.
 * </p>
 */
public final class OntologyUtilities {

    public static OWLEntityType getEntityType(OWLEntity entity){
        if(entity instanceof OWLClass){
            return OWLEntityType.CLASS;
        } else if(entity instanceof OWLIndividual){
            return OWLEntityType.INDIVIDUAL;
        } else {
            return OWLEntityType.ENTITY;
        }
    }

    public static OWLEntityType getEntityTypeByClass(Class<? extends OWLEntity> entityType){
        if(OWLClass.class.isAssignableFrom(entityType)){
            return OWLEntityType.CLASS;
        } else if(OWLIndividual.class.isAssignableFrom(entityType)){
            return OWLEntityType.INDIVIDUAL;
        } else {
            return OWLEntityType.ENTITY;
        }
    }
}
