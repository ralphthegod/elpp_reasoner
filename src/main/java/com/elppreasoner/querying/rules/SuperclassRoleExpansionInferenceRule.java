package com.elppreasoner.querying.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;

import com.reasoner.querying.InferenceRule;

public class SuperclassRoleExpansionInferenceRule 
    extends InferenceRule<OWLObjectPropertyExpression, Map<OWLClassExpression, Set<OWLClassExpression>>>{

    private final Map<OWLClassExpression, Map<OWLObjectPropertyExpression, Set<OWLClassExpression>>> fillerToRole = new HashMap<>();

    @Override
    public boolean axiomCriterion(OWLClassExpression subclass, OWLClassExpression superclass) {
        return subclass instanceof OWLObjectSomeValuesFrom && isSuperclassABasicConcept(superclass);
    }

    @Override
    public void addAxiom(OWLClassExpression subclass, OWLClassExpression superclass) {
        OWLObjectSomeValuesFrom objectSomeValuesFrom = (OWLObjectSomeValuesFrom) subclass;
        OWLObjectPropertyExpression role = objectSomeValuesFrom.getProperty();
        OWLClassExpression filler = objectSomeValuesFrom.getFiller();
        axioms
            .computeIfAbsent(role, __ -> new HashMap<>())
            .computeIfAbsent(filler, __ -> new HashSet<>())
            .add(superclass);

        fillerToRole
            .computeIfAbsent(filler, __ -> new HashMap<>())
            .computeIfAbsent(role, __ -> new HashSet<>())
            .add(superclass);
    }
    
}
