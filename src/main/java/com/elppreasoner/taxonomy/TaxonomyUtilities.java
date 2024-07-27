package com.elppreasoner.taxonomy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public final class TaxonomyUtilities {
    private TaxonomyUtilities() {
        throw new UnsupportedOperationException("Cannot instantiate this utility class.");
    }

    public static Map<OWLClassExpression, Set<OWLClassExpression>> computeTaxonomySuperConcepts(Set<OWLSubClassOfAxiom> axioms) {
        Map<OWLClassExpression, Set<OWLClassExpression>> classToSuperConcepts = new HashMap<>();
        
        OWLClass thing = OWLManager.getOWLDataFactory().getOWLThing();
        classToSuperConcepts.put(thing, new HashSet<>());

        OWLClass nothing = OWLManager.getOWLDataFactory().getOWLNothing();
        classToSuperConcepts.put(nothing, new HashSet<>());
        
        OWLClassExpression subClass;
        OWLClassExpression superClass;

        for (OWLSubClassOfAxiom axiom: axioms) {
            subClass = axiom.getSubClass();
            if (subClass instanceof OWLClass && !subClass.isOWLNothing()) {
                classToSuperConcepts.get(nothing).add(subClass);
            }
            if (subClass instanceof OWLObjectSomeValuesFrom) {
                OWLObjectSomeValuesFrom objectSomeValuesFrom = (OWLObjectSomeValuesFrom) subClass;
                OWLClassExpression filler = objectSomeValuesFrom.getFiller();
                if (!filler.isOWLNothing()) {
                    classToSuperConcepts.get(nothing).add(filler);
                }
                classToSuperConcepts.putIfAbsent(filler, new HashSet<>());
                if (!filler.isOWLThing()) {
                    classToSuperConcepts.get(filler).add(thing);
                }
            }

            superClass = axiom.getSuperClass();
            if (superClass instanceof OWLClass && !superClass.isOWLNothing()) {
                classToSuperConcepts.get(nothing).add(superClass);
            }
            if (superClass instanceof OWLObjectSomeValuesFrom) {
                OWLObjectSomeValuesFrom objectSomeValuesFrom = (OWLObjectSomeValuesFrom) superClass;
                OWLClassExpression filler = objectSomeValuesFrom.getFiller();
                if (!filler.isOWLNothing()) {
                    classToSuperConcepts.get(nothing).add(filler);
                }
                classToSuperConcepts.putIfAbsent(filler, new HashSet<>());
                if (!filler.isOWLThing()) {
                    classToSuperConcepts.get(filler).add(thing);
                }
            }

            if ((subClass.isClassExpressionLiteral() || subClass instanceof OWLObjectOneOf) && (superClass.isClassExpressionLiteral() || superClass instanceof OWLObjectOneOf)) {
                if (!subClass.isOWLNothing()) {
                    classToSuperConcepts.get(nothing).add(subClass);
                }
                classToSuperConcepts.putIfAbsent(subClass, new HashSet<>());
                if (!subClass.isOWLThing()) {
                    classToSuperConcepts.get(subClass).add(superClass);
                }

                if (!superClass.isOWLNothing()) {
                    classToSuperConcepts.get(nothing).add(superClass);
                }
                classToSuperConcepts.putIfAbsent(superClass, new HashSet<>());
                if (!superClass.isOWLThing()) {
                    classToSuperConcepts.get(superClass).add(thing);
                }
            }
        }

        return classToSuperConcepts;
    }

    // TODO: utility methods
}
