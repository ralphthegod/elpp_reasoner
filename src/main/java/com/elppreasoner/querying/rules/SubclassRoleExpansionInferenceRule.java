package com.elppreasoner.querying.rules;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import com.reasoner.querying.InferenceRule;
import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;

public class SubclassRoleExpansionInferenceRule extends InferenceRule<OWLClassExpression, Set<Entry<OWLObjectPropertyExpression, OWLClassExpression>>>{

    @Override
    public boolean axiomCriterion(OWLClassExpression subclass, OWLClassExpression superclass) {
        return isSubclassABasicConcept(subclass) && superclass instanceof OWLObjectSomeValuesFrom;
    }

    @Override
    public void addAxiom(OWLClassExpression subclass, OWLClassExpression superclass) {
        OWLObjectSomeValuesFrom objectSomeValuesFrom = (OWLObjectSomeValuesFrom) superclass;
        OWLObjectPropertyExpression role = objectSomeValuesFrom.getProperty();
        OWLClassExpression filler = objectSomeValuesFrom.getFiller();
        axioms.computeIfAbsent(subclass, __ -> new HashSet<>())
            .add(Map.entry(role, filler));
    }
    
}
