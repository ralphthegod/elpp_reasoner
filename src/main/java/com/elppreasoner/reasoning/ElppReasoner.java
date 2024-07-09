package com.elppreasoner.reasoning;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;

import com.elppreasoner.querying.rules.IntersectionSuperclassesInferenceRule;
import com.elppreasoner.querying.rules.SubclassRoleExpansionInferenceRule;
import com.elppreasoner.querying.rules.SuperclassRoleExpansionInferenceRule;
import com.elppreasoner.querying.rules.ToldSuperclassesInferenceRule;
import com.reasoner.reasoning.Reasoner;

/**
 * ELPP reasoner implementation. <p>
 */
public class ElppReasoner extends Reasoner {

    /**
     * Uses the given ontology accessor to create a new ELPP reasoner.
     * Dependency injection is used to provide the ontology accessor.
     * @param ontologyAccessor
     */
    public ElppReasoner(OWLOntology ontology) {
        super(ontology);

        // Add inference rules
        addInferenceRule(new ToldSuperclassesInferenceRule()); // CR1
        addInferenceRule(new IntersectionSuperclassesInferenceRule()); // CR2       
        addInferenceRule(new SubclassRoleExpansionInferenceRule()); // CR3
        addInferenceRule(new SuperclassRoleExpansionInferenceRule()); // CR4
        // CR5 and CR6 do not require inference rules

        // Add inference calculators
        addInferenceCalculator(InferenceType.CLASS_HIERARCHY, this::computeClassHierarchy);
    }

    private void computeClassHierarchy() {
        // TODO: Implement the computation of the class hierarchy
    }
    
}
