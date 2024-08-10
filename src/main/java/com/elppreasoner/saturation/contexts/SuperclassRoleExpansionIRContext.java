package com.elppreasoner.saturation.contexts;

import static com.elppreasoner.normalization.NormalizationUtilities.isSubclassABasicConcept;
import static com.elppreasoner.normalization.NormalizationUtilities.isSuperclassABasicConcept;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.elppreasoner.reasoning.rules.SuperclassRoleExpansionInferenceRule;
import com.reasoner.reasoning.rules.InferenceRule;
import com.reasoner.saturation.InferenceRuleContext;

public class SuperclassRoleExpansionIRContext extends InferenceRuleContext<OWLObjectPropertyExpression, Map<OWLClassExpression, Set<OWLClassExpression>>> {
    
    private final Map<OWLObjectPropertyExpression, Set<OWLClassExpression>> subclassesByPropertyProcessedAxioms = new HashMap<>();

    public SuperclassRoleExpansionIRContext(InferenceRule inferenceRule, OWLEntity entity) {
        super((InferenceRule<OWLObjectPropertyExpression, Map<OWLClassExpression, Set<OWLClassExpression>>>) inferenceRule, entity);
    }

    @Override
    public boolean addProcessedAxiom(OWLSubClassOfAxiom axiom) {
        if(!isTargetEntity(axiom)) {
            throw new IllegalArgumentException("Axiom is not a target entity");
        }
        
        OWLClassExpression superclass = axiom.getSuperClass();
        OWLClassExpression subclass = axiom.getSubClass();

        if(superclass instanceof OWLObjectSomeValuesFrom){
            OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFrom) superclass).getProperty();
            if(!subclassesByPropertyProcessedAxioms.containsKey(property)) {
                subclassesByPropertyProcessedAxioms.put(property, new HashSet<>());
            }
            return subclassesByPropertyProcessedAxioms.get(property).add(subclass);
        }
        else{
            return processedAxioms.add(axiom);
        }
    }

    @Override
    protected boolean isTargetEntity(OWLSubClassOfAxiom axiom) {
        OWLClassExpression subclass = axiom.getSubClass();
        OWLClassExpression superclass = axiom.getSuperClass();

        if ((isSubclassABasicConcept(subclass) && isSuperclassABasicConcept(superclass) && !Objects.equals(subclass, getEntity())) || 
            !isSubclassABasicConcept(subclass) || 
            (!isSuperclassABasicConcept(superclass) && !(superclass instanceof OWLObjectSomeValuesFrom))) {
            return false;
        }

        if (superclass instanceof OWLObjectSomeValuesFrom && 
            !Objects.equals(((OWLObjectSomeValuesFrom) superclass).getProperty(), getEntity())) {
            return false;
        }

        return true;
    }

    @Override
    public boolean hasProcessedAxiom(OWLSubClassOfAxiom axiom) {
        OWLClassExpression superclass = axiom.getSuperClass();
        
        if(superclass instanceof OWLObjectSomeValuesFrom) {
            OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) superclass;
            OWLObjectPropertyExpression property = someValuesFrom.getProperty();
            if(!subclassesByPropertyProcessedAxioms.containsKey(property)) {
                return false;
            }
            return subclassesByPropertyProcessedAxioms.get(property).contains(axiom.getSubClass());
        } else{
            return processedAxioms.contains(axiom);
        }   
    }

    @Override
    public Set<OWLSubClassOfAxiom> compute(OWLSubClassOfAxiom axiom) {
        OWLClassExpression subclass = axiom.getSubClass();
        OWLClassExpression superclass = axiom.getSuperClass();
        Set<OWLSubClassOfAxiom> conclusions = new HashSet<>();
        OWLDataFactory factory = OWLManager.getOWLDataFactory();

        if(superclass instanceof OWLObjectSomeValuesFrom) { // if the superclass is an existential restriction
            OWLObjectSomeValuesFrom someValuesFrom = (OWLObjectSomeValuesFrom) superclass;
            OWLObjectPropertyExpression property = someValuesFrom.getProperty();
            if(!getInferenceRule().getAxioms().containsKey(property)) {
                return conclusions; // empty set
            }
            SuperclassRoleExpansionInferenceRule rule = (SuperclassRoleExpansionInferenceRule) getInferenceRule();
            Set<OWLClassExpression> fillers = rule.getAxioms().get(property).keySet();
            for(OWLClassExpression filler:fillers){
                OWLSubClassOfAxiom hypotheticalAxiom = factory.getOWLSubClassOfAxiom(getEntity(), filler);
                if(!processedAxioms.contains(hypotheticalAxiom)) continue;
                Set<OWLClassExpression> superclasses = rule.getAxioms().get(property).get(filler);
                superclasses.forEach( superclass_ -> {
                    conclusions.add(factory.getOWLSubClassOfAxiom(subclass, superclass_));
                });
            };
        }

        else{ // if the superclass is a basic concept
            SuperclassRoleExpansionInferenceRule rule = (SuperclassRoleExpansionInferenceRule) getInferenceRule();
            if(!rule.getFillerToRole().containsKey(superclass)){
                return conclusions; // empty set
            }
            Set<OWLObjectPropertyExpression> properties = rule.getFillerToRole().get(superclass).keySet();
            for(OWLObjectPropertyExpression property: properties){
                if(!subclassesByPropertyProcessedAxioms.containsKey(property)) continue;
                Set<OWLClassExpression> subclasses = subclassesByPropertyProcessedAxioms.get(property);
                Set<OWLClassExpression> superclasses = rule.getFillerToRole().get(superclass).get(property);
                superclasses.forEach( superclass_ -> {
                    subclasses.forEach( subclass_ -> {
                        conclusions.add(factory.getOWLSubClassOfAxiom(subclass_, superclass_));
                    });
                });
            }
        }

        return conclusions;
    }

  
}
