package com.elppreasoner.saturation.contexts;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;

import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

public class BottomSuperclassRoleExpansionIRContext extends InferenceRuleContext<Object,Object>{
    
    private OWLSubClassOfAxiom subclassOfBottom;

    public BottomSuperclassRoleExpansionIRContext(InferenceRule inferenceRule, OWLEntity entity) {
        super(inferenceRule, entity);
    }

    @Override
    public String id(){
        return "5";
    }

    @Override
    public boolean addProcessedAxiom(OWLSubClassOfAxiom axiom) {
        if(!isTargetEntity(axiom)){
            throw new IllegalArgumentException("Axiom is not a target entity");
        }

        if(axiom.getSuperClass().isOWLNothing()){
            if(subclassOfBottom != null){
                throw new IllegalStateException("Bottom subclass already exists");
            }
            subclassOfBottom = axiom;
            return true;
        }
        else{
            return processedAxioms.add(axiom);
        }

    }

    @Override
    protected boolean isTargetEntity(OWLSubClassOfAxiom axiom) {
        final OWLClassExpression subclass = axiom.getSubClass();
        final OWLClassExpression superclass = axiom.getSuperClass();

        final boolean isSubclassABasicConcept = isSubclassABasicConcept(subclass);
        final boolean isSuperclassSomeValuesFrom = superclass instanceof OWLObjectSomeValuesFrom;
        final boolean isSuperclassBottom = superclass.isOWLNothing();

        if(!isSubclassABasicConcept || (!isSuperclassBottom && !isSuperclassSomeValuesFrom)){
            return false;
        }

        if(isSuperclassSomeValuesFrom){
            final OWLClassExpression filler = ((OWLObjectSomeValuesFrom) superclass).getFiller();
            if(!Objects.equals(filler, getEntity())){
                return false;
            }
        }

        if(isSuperclassBottom && !Objects.equals(subclass, getEntity())){
            return false;
        }
        
        return true;
    }

    @Override
    public boolean hasProcessedAxiom(OWLSubClassOfAxiom axiom) {
        if(!isTargetEntity(axiom)){
            throw new IllegalArgumentException("Axiom is not a target entity");
        }
        if(axiom instanceof OWLObjectSomeValuesFrom){
            return processedAxioms.contains(axiom);
        }
        else{
            return subclassOfBottom != null && Objects.equals(subclassOfBottom, axiom);
        }
    }

    @Override
    public Set<OWLSubClassOfAxiom> getProcessedAxioms() {
        final Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
        if(subclassOfBottom != null){
            conclusions.add(subclassOfBottom);
        }
        return conclusions;
    }

    @Override
    public Set<OWLSubClassOfAxiom> compute(OWLSubClassOfAxiom axiom) {
        OWLDataFactory owlDataFactory = OWLManager.getOWLDataFactory();
        OWLClass bottom = owlDataFactory.getOWLNothing();
        
        if(axiom.getSuperClass().isOWLNothing()){
            final Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
            processedAxioms.forEach(processedAxiom -> {
                conclusions.add(owlDataFactory.getOWLSubClassOfAxiom(processedAxiom.getSubClass(), bottom));
            });
            return conclusions;
        } else{
            final Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
            OWLClassExpression conclusionSubclass = axiom.getSubClass();
            if(subclassOfBottom != null){
                conclusions.add(owlDataFactory.getOWLSubClassOfAxiom(conclusionSubclass, bottom));
            }
            return conclusions;
        }
    }
    
}
