package com.elppreasoner.reasoning.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;
import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;
import com.elppreasoner.saturation.contexts.SuperclassRoleExpansionIRContext;
import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

/**
 * {@code InferenceRule} for superclass role expansion (CR4)
 */
public class SuperclassRoleExpansionInferenceRule 
    extends InferenceRule<OWLObjectPropertyExpression, Map<OWLClassExpression, Set<OWLClassExpression>>>{

    private final Map<OWLClassExpression, Map<OWLObjectPropertyExpression, Set<OWLClassExpression>>> fillerToRole = new HashMap<>();

    public SuperclassRoleExpansionInferenceRule() {
        super(OWLClass.class, SuperclassRoleExpansionIRContext.class);
    }

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

    @Override
    public Set<InferenceRuleContext> extractContexts(Map<OWLObject, InferenceRuleContext> contexts,
            OWLClassExpression subclass, OWLClassExpression superclass) {
        final HashSet<InferenceRuleContext> result = new HashSet<>();
        if(isSubclassABasicConcept(subclass) && isSuperclassABasicConcept(superclass)){
            InferenceRuleContext context = contexts.get(subclass);
            result.add(context);
        }
        if(isSubclassABasicConcept(subclass) && superclass instanceof OWLObjectSomeValuesFrom){
            OWLClassExpression filler = ((OWLObjectSomeValuesFrom) superclass).getFiller();
            InferenceRuleContext context = contexts.get(filler);
            result.add(context);
        }
        return result;
    }
    
}
