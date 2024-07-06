package com.elppreasoner.normalization;

import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.elppreasoner.normalization.normalization_rules.NormalizationRule;

public class NormalizationUtilities {
    /* 
     * Legenda of concepts, from theory to OWL implementation: {
     *     "bottom" (⊥)             ->      "owl:Nothing",
     *     "top" (⊤)                ->      "owl:Thing",
     *     "concept name"           ->      "OWLClass",
     *     "individual name"        ->      "OWLIndividual",
     *     "enum of individuals"    ->      "OWLObjectOneOf"    {a1}⊔{a2}⊔...⊔{an}
     *     "C ⊑ D"                  ->      "OWLSubclassOf"
     *     "C1 ≡ ... ≡ Cn"          ->      "EquivalentClasses"
     * }
     * 
     * Def. In our domain, an EL++ constraint box (CBox) is a a finite set of general concept inclusions (GCIs): C ⊑ D. In particular: C can be a
     * basic concept (BC), while D can be either a basic concept (BC) or a bottom (⊥). [Role inclusions excluded]
     * 
     * Def. Given a CBox C, BC_C (read "basic concepts of C") denotes the smallest set of concept descriptions that contains the top concept ⊤, all
     * concept names used in C, and all concept descriptions of the form {a} or p(f1,..., fk) appearing in C.
     * 
     * Def. In our domain, CBox C is in normal form ("normalized") if all GCIs have one of four following forms (read the paper or the if-else tree
     * in isInNormalForm() for more details). [Role inclusions excluded] 
    */
    
    // Concept names denote sets of objects -> "classes" in OWL (e.g. 'Person', 'Female')
    public static boolean isConceptName(OWLClassExpression classExpression) {
        return classExpression instanceof OWLClass &&  // is it a class?
            !classExpression.isOWLNothing() &&         // is it NOT owl:Nothing ("bottom" ⊥)?
            !classExpression.isOWLThing();             // is it NOT owl:Thing ("top" ⊤)?
    }

    // Individual names denote objects -> "individuals" in OWL (e.g. 'John', 'Mary')
    public static boolean isIndividualName(OWLClassExpression classExpression) {
        return classExpression instanceof OWLIndividual;  // is it an individual name?
    }

    // Given C ⊑ D, in this method we're considering C. In particular, C can be a basic concept (BC)
    public static boolean isSubclassABasicConcept(OWLClassExpression subClassConcept) {
        return isConceptName(subClassConcept) ||        // is it a concept name?
            isIndividualName(subClassConcept) ||        // is it an individual name?
            subClassConcept.isOWLThing() ||             // is it owl:Thing ("top" ⊤)?
            subClassConcept instanceof OWLObjectOneOf;  // is it an enumeration of individuals?
    }

    // Given C ⊑ D, in this method we're considering D. In particular, D can be a basic concept (BC) or a bottom (⊥)
    public static boolean isSuperclassABasicConcept(OWLClassExpression superClassConcept) {
        return isConceptName(superClassConcept) ||        // is it a concept name?
            isIndividualName(superClassConcept) ||        // is it an individual name?
            superClassConcept.isOWLNothing() ||           // is it owl:Nothing ("bottom" ⊥)?
            superClassConcept.isOWLThing() ||             // is it owl:Thing ("top" ⊤)?
            superClassConcept instanceof OWLObjectOneOf;  // is it an enumeration of individuals?
    }

    public static boolean isCBoxInNormalForm(OWLSubClassOfAxiom subClassOfAxiom) {
        OWLClassExpression subClass = subClassOfAxiom.getSubClass();
        OWLClassExpression superClass = subClassOfAxiom.getSuperClass();

        if (isSubclassABasicConcept(subClass) && isSuperclassABasicConcept(superClass)) {   // *C1 ⊑ D*
            return true;
        } else if (subClass instanceof OWLObjectIntersectionOf && isSuperclassABasicConcept(superClass)) { // *C1 ⊓ C2 ⊑ D*
            List<OWLClassExpression> operands = ((OWLObjectIntersectionOf) subClass).getOperandsAsList();  // the list containing C1 and C2
            if (isSubclassABasicConcept(operands.get(0)) &&
                isSubclassABasicConcept(operands.get(operands.size()-1))) {
                    return true;
                }
        } else if (isSubclassABasicConcept(subClass) && superClass instanceof OWLObjectSomeValuesFrom) {  // *C1 ⊑ ∃r.C2*
            if (isSubclassABasicConcept(((OWLObjectSomeValuesFrom) superClass).getFiller())) {
                return true;
            }
        } else if (subClass instanceof OWLObjectSomeValuesFrom && isSuperclassABasicConcept(superClass)) { // *∃r.C1 ⊑ D*
            return isSubclassABasicConcept(((OWLObjectSomeValuesFrom) subClass).getFiller());
        }

        return false;  // CBox is not in normal form
    }

    

    // TODO
    public static boolean isGenerated(OWLClass owlClass) {
        return owlClass.toString().contains(NormalizationRule.GENERATED_CLASS);
    }
}
