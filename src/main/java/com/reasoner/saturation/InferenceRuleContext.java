package com.reasoner.saturation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.reasoner.reasoning.rules.InferenceRule;

public abstract class InferenceRuleContext {

    private final OWLClassExpression entity;
    private final InferenceRule inferenceRule;
    
    private final AtomicBoolean isActiveContext = new AtomicBoolean(false);

    private final Queue<OWLSubClassOfAxiom> scheduledAxioms = new LinkedList<>();
    private final Set<OWLSubClassOfAxiom> processedAxioms = new HashSet<>();

    public InferenceRuleContext(InferenceRule inferenceRule, OWLEntity entity) {
        this.inferenceRule = inferenceRule;
        this.entity = entity.asOWLClass();
    }

}