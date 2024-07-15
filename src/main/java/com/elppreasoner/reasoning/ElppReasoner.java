package com.elppreasoner.reasoning;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;

import com.elppreasoner.reasoning.rules.BottomSuperclassRoleExpansionInferenceRule;
import com.elppreasoner.reasoning.rules.IntersectionSuperclassesInferenceRule;
import com.elppreasoner.reasoning.rules.NominalChainExpansionInferenceRule;
import com.elppreasoner.reasoning.rules.SubclassRoleExpansionInferenceRule;
import com.elppreasoner.reasoning.rules.SuperclassRoleExpansionInferenceRule;
import com.elppreasoner.reasoning.rules.ToldSuperclassesInferenceRule;
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
        initializeInferenceRules();
    }

    private void initializeInferenceRules(){
        // Add inference rules
        addInferenceRule(new ToldSuperclassesInferenceRule()); // CR1
        addInferenceRule(new IntersectionSuperclassesInferenceRule()); // CR2       
        addInferenceRule(new SubclassRoleExpansionInferenceRule()); // CR3
        addInferenceRule(new SuperclassRoleExpansionInferenceRule()); // CR4
        addInferenceRule(new BottomSuperclassRoleExpansionInferenceRule()); // CR5
        addInferenceRule(new NominalChainExpansionInferenceRule()); // CR6

        // Add inference calculators
        addInferenceCalculator(InferenceType.CLASS_HIERARCHY, this::computeClassHierarchy);
    }

    private void computeClassHierarchy() {
        // TODO: Implement the computation of the class hierarchy
    }
    
}
