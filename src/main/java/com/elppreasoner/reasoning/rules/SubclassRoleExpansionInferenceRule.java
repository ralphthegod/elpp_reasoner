package com.elppreasoner.reasoning.rules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;
import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;
import com.elppreasoner.saturation.contexts.SubclassRoleExpansionIRContext;
import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

/**
 * {@code InferenceRule} for subclass role expansion (CR3)
 */
public class SubclassRoleExpansionInferenceRule extends InferenceRule<OWLClassExpression, Set<Entry<OWLObjectPropertyExpression, OWLClassExpression>>>{

    private static class RFEntry implements Map.Entry<OWLObjectPropertyExpression,OWLClassExpression> {
        private final OWLObjectPropertyExpression role;
        private final OWLClassExpression filler;

        public RFEntry(OWLObjectPropertyExpression role, OWLClassExpression filler) {
            this.role = role;
            this.filler = filler;
        }

        @Override
        public OWLObjectPropertyExpression getKey() {
            return role;
        }

        @Override
        public OWLClassExpression getValue() {
            return filler;
        }

        @Override
        public OWLClassExpression setValue(OWLClassExpression value) {
            throw new UnsupportedOperationException();
        }
    }

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
            .add( new RFEntry(role, filler));
    }

    @Override
    public Collection<InferenceRuleContext> extractContexts(Map<OWLObject, InferenceRuleContext> contexts,
            OWLClassExpression subclass, OWLClassExpression superclass) {
        if(isSubclassABasicConcept(subclass) && isSuperclassABasicConcept(superclass)){
            InferenceRuleContext context = contexts.get(subclass);
            return new HashSet<InferenceRuleContext>() {{
                add(context);
            }};
        }
        return new HashSet<>();
    }
    
}
