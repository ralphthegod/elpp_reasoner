package com.elppreasoner.reasoning.rules;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;
import com.elppreasoner.saturation.contexts.SubclassRoleExpansionIRContext;
import com.reasoner.reasoning.rules.InferenceRule;

public class SubclassRoleExpansionInferenceRule extends InferenceRule<OWLClassExpression, Set<Entry<OWLObjectPropertyExpression, OWLClassExpression>>>{

    public SubclassRoleExpansionInferenceRule() {
        super(OWLClass.class, SubclassRoleExpansionIRContext.class);
    }

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
            .add( Map.entry(role, filler) );
    }
    
}
