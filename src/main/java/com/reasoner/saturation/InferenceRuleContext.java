package com.reasoner.saturation;

import java.util.HashSet;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.reasoner.reasoning.rules.InferenceRule;

public abstract class InferenceRuleContext<S,T> {

    private final OWLClassExpression entity;
    private final InferenceRule<S,T> inferenceRule;
    
    private final AtomicBoolean isActiveContext = new AtomicBoolean(false);
    private boolean isInitialized = false;

    private final Queue<OWLSubClassOfAxiom> scheduledAxioms = new ConcurrentLinkedQueue<>();
    protected final Set<OWLSubClassOfAxiom> processedAxioms = new HashSet<>();

    public InferenceRuleContext(InferenceRule<S,T> inferenceRule, OWLEntity entity) {
        this.inferenceRule = inferenceRule;
        //this.entity = entity.asOWLClass();
    }

    public abstract boolean addProcessedAxiom(OWLSubClassOfAxiom axiom);
    public abstract boolean hasProcessedAxiom(OWLSubClassOfAxiom axiom);
    protected abstract boolean isTargetEntity(OWLSubClassOfAxiom axiom);
    
    /**
     * Apply the inference rule on context axioms.
     * @return {@code Set<OWLSubClassOfAxiom>} conclusions.
     */
    public abstract Set<OWLSubClassOfAxiom> compute(OWLSubClassOfAxiom axiom);

    public boolean scheduleAxiom(OWLSubClassOfAxiom axiom){
        if(!isTargetEntity(axiom)) return false;
        return scheduledAxioms.add(axiom);
    }

    public OWLSubClassOfAxiom pollScheduledAxiom(){
        return scheduledAxioms.poll();
    }

    public boolean hasScheduledAxioms(){
        return !scheduledAxioms.isEmpty();
    }

    public Set<OWLSubClassOfAxiom> initializeContext(){
        if (isInitialized) throw new IllegalStateException();

        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
        OWLSubClassOfAxiom selfSubClassOf;
        OWLSubClassOfAxiom subClassOfThing;
        Set<OWLSubClassOfAxiom> initialAxioms = new HashSet<>();

        switch(inferenceRule.getEntityType()){
            case CLASS:
                selfSubClassOf = owlDataFactory.getOWLSubClassOfAxiom(entity, entity);
                subClassOfThing = owlDataFactory.getOWLSubClassOfAxiom(entity, owlDataFactory.getOWLThing());
                initialAxioms.add(selfSubClassOf);
                initialAxioms.add(subClassOfThing);
                break;
            case INDIVIDUAL:
                OWLObjectOneOf individualObjectOneOf = owlDataFactory.getOWLObjectOneOf((OWLIndividual) entity);
                selfSubClassOf = owlDataFactory.getOWLSubClassOfAxiom(individualObjectOneOf, individualObjectOneOf);
                subClassOfThing = owlDataFactory.getOWLSubClassOfAxiom(individualObjectOneOf, owlDataFactory.getOWLThing());
                initialAxioms.add(selfSubClassOf);
                initialAxioms.add(subClassOfThing);
                break;
            case ENTITY:
                break;
            default:
                throw new IllegalStateException();
        }

        if(initialAxioms.isEmpty()) throw new IllegalStateException();

        isInitialized = true;

        return initialAxioms;
    }
    

    public Set<OWLSubClassOfAxiom> getProcessedAxioms(){
        return processedAxioms;
    }

    public AtomicBoolean isActiveContext(){
        return isActiveContext;
    }

    public boolean hasBeenInitialized(){
        return isInitialized;
    }

    public OWLClassExpression getEntity(){
        return entity;
    }

    public InferenceRule<S,T> getInferenceRule(){
        return inferenceRule;
    }

    @Override
    public int hashCode(){
        return (entity.toString() + getClass().toString()).hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof InferenceRuleContext){
            InferenceRuleContext<?,?> other = (InferenceRuleContext<?,?>) o;
            return Objects.equals(entity, other.entity) && getClass().equals(other.getClass());
        }
        return false;
    }

}