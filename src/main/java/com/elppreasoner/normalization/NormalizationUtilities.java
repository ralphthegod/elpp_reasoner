package com.elppreasoner.normalization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public final class NormalizationUtilities {
    /* 
     * Legenda of concepts, from theory to OWL implementation: {
     *     "bottom" (⊥)                 <->      "owl:Nothing",
     *     "top" (⊤)                    <->      "owl:Thing",
     *     "concept name"               <->      "OWLClass",
     *     "individual name | nominal"  <->      "OWLIndividual",
     *     "C ⊑ D"                      <->      "OWLSubclassOf",
     *     "C1 ≡ ... ≡ Cn"              <->      "EquivalentClasses",
     *     "enum of individuals"        <->      "OWLObjectOneOf",  // {a1}⊔{a2}⊔...⊔{an}
     *     "existential restriction ∃"  <->      "OWLObjectSomeValuesFrom",
     *     "intersection ⊓"             <->      "OWLObjectIntersectionOf"
     * }
     * 
     * Def. In our domain, an EL++ constraint box (CBox) is a finite set of general concept inclusions (GCIs): C ⊑ D. In particular: C can be a
     * basic concept (BC) or a nominal {a}, while D can be either a basic concept (BC), a nominal {a} or a bottom (⊥). [Role inclusions excluded]
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

    // Given C ⊑ D, in this method we're considering C. In particular, C can be a basic concept (BC) or a nominal {a}
    public static boolean isSubclassABasicConcept(OWLClassExpression subClassConcept) {
        return isConceptName(subClassConcept) ||        // is it a concept name?
            isIndividualName(subClassConcept) ||        // is it an individual name?
            subClassConcept.isOWLThing() ||             // is it owl:Thing ("top" ⊤)?
            subClassConcept instanceof OWLObjectOneOf;  // is it an enumeration of individuals?
    }

    // Given C ⊑ D, in this method we're considering D. In particular, D can be a basic concept (BC), a nominal {a} or a bottom (⊥)
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


    public static class NormalizationRulesManager {
        /*
         * A CBox can be converted into Normal Form (NF) using the translation rules that follow in two phases:
         *     1. exhaustively apply rules NF1 to NF4;
         *     2. exhaustively apply rules NF5 to NF7.
         * "Rule application" means that the concept inclusion on the left-hand side is replaced with the set of concept inclusions on the
         * right-hand side.
        */

        // In this enum comments: C' and D' are NOT basic concepts and A denotes a new concept name
        public enum NormalizationRule {
            // NF1,  // role inclusion, not considered in our work
            NF2,  // C ⊓ D' ⊑ E   -->   { D' ⊑ A, C ⊓ A ⊑ E }   * Please note that it can also be in the equivalent form D' ⊓ C
            NF3,  //  ∃r.C' ⊑ D   -->   { C' ⊑ A, ∃r.A ⊑ D }
            NF4,  //      ⊥ ⊑ D   -->   ∅
            NF5,  //     C' ⊑ D'  -->   { C' ⊑ A, A ⊑ D' }
            NF6,  //   B ⊑ ∃r.C'  -->   { B ⊑ ∃r.A, A ⊑ C' }
            NF7  //  B ⊑ C ⊓ D   -->   { B ⊑ C, B ⊑ D }
        }

        public static NormalizationRule identifyNF(OWLSubClassOfAxiom axiom) {
            OWLClassExpression subClass = axiom.getSubClass();
            OWLClassExpression superClass = axiom.getSuperClass();
    
            if (isSuperclassABasicConcept(superClass)) {  // NF5,NF6 excluded
                if (subClass instanceof OWLObjectIntersectionOf) {  // NF2
                    return NormalizationRule.NF2;
                }
    
                // NF2,NF5,NF6 excluded
                if (subClass instanceof OWLObjectSomeValuesFrom &&
                        !isSubclassABasicConcept(((OWLObjectSomeValuesFrom) subClass).getFiller())) {
                    return NormalizationRule.NF3;
                }
            }
            
            if (isSubclassABasicConcept(subClass)) {  // NF6 or NF7
                if (superClass instanceof OWLObjectSomeValuesFrom &&
                        !isSuperclassABasicConcept(((OWLObjectSomeValuesFrom) superClass).getFiller())) {
                    return NormalizationRule.NF6;
                }
                if (superClass instanceof OWLObjectIntersectionOf) {  // NF7
                    return NormalizationRule.NF7;
                }
            }

            if (subClass.isOWLNothing() && isSuperclassABasicConcept(superClass)) {  // NF4
                return NormalizationRule.NF4;
            }
    
            // subClass and superClass cannot be top (⊤ <-> owl:Thing), a top object/data property or a rdfs:literal
            if (subClass.isTopEntity() || superClass.isTopEntity()) {
                throw new IllegalArgumentException("Subclass and superclass cannot be top (owl:Thing), a top object/data property or a rdfs:literal.");
            }
    
            // subClass and superClass are not basic concepts (only NF5 remains)
            return NormalizationRule.NF5;
        }

        // the new concept name A
        public static OWLClass generateOWLClass(OWLOntology ontology, OWLClassExpression subclass, OWLClassExpression superclass) {
            return (new OWLDataFactoryImpl()).getOWLClass(
                ontology.getOWLOntologyManager().getOntologyDocumentIRI(ontology) +
                "#GENERATED" +
                subclass.toString().hashCode() +
                superclass.toString().hashCode()
                );
        }

        // C ⊓ D' ⊑ E   -->   { D' ⊑ A, C ⊓ A ⊑ E }   * Please note that it can also be in the equivalent form D' ⊓ C
        public static Collection<OWLSubClassOfAxiom> applyNF2(OWLOntology ontology, OWLSubClassOfAxiom axiom) {
            OWLClassExpression subClass = axiom.getSubClass();
            OWLClassExpression superClass = axiom.getSuperClass();

            if (superClass.isAnonymous()) {
                throw new IllegalArgumentException("Superclass needs to be named (have an IRI). Please, note that class entities are named (they have an IRI) while class expressions are anonymous.");
            }

            List<OWLClassExpression> intersectionOf = ((OWLObjectIntersectionOf) subClass).getOperandsAsList();
            OWLClassExpression left = intersectionOf.get(0);
            OWLClassExpression right = intersectionOf.get(intersectionOf.size()-1);

            OWLClass newClass = generateOWLClass(ontology, subClass, superClass);  // new concept name A
            OWLDataFactoryImpl owlDataFactory = new OWLDataFactoryImpl();
            List<OWLSubClassOfAxiom> normalizedAxioms = new ArrayList<>();

            if (!isSubclassABasicConcept(left)) {  // if left operand of the intersection is not a basic concept
                normalizedAxioms.add(owlDataFactory.getOWLSubClassOfAxiom(left, newClass));                           // C ⊑ A                  
                normalizedAxioms.add(                                                                                 // D' ⊓ A ⊑ E
                    owlDataFactory.getOWLSubClassOfAxiom(owlDataFactory.getOWLObjectIntersectionOf(newClass, right),
                    superClass
                ));
            } else if (!isSubclassABasicConcept(right)) {  // if right operand of the intersection is not a basic concept
                normalizedAxioms.add(owlDataFactory.getOWLSubClassOfAxiom(right, newClass));                          // D' ⊑ A
                normalizedAxioms.add(                                                                                 // C ⊓ A ⊑ E
                    owlDataFactory.getOWLSubClassOfAxiom(owlDataFactory.getOWLObjectIntersectionOf(left, newClass),
                    superClass
                ));
            } else {  // if both operands of the intersection are basic concepts (already normalized)
                return null;  // No-op: should never enter this branch
            }

            return normalizedAxioms;
        }

        // ∃r.C' ⊑ D   -->   { C' ⊑ A, ∃r.A ⊑ D }
        public static Collection<OWLSubClassOfAxiom> applyNF3(OWLOntology ontology, OWLSubClassOfAxiom axiom) {
            OWLClassExpression subClass = axiom.getSubClass();
            OWLClassExpression superClass = axiom.getSuperClass();

            if (superClass.isAnonymous()) {
                throw new IllegalArgumentException("Superclass needs to be named (have an IRI). Please, note that class entities are named (they have an IRI) while class expressions are anonymous.");
            }

            OWLObjectSomeValuesFrom existentialCE = (OWLObjectSomeValuesFrom) subClass;
            OWLObjectPropertyExpression existentialCEProperty = existentialCE.getProperty();
            OWLClassExpression filler = existentialCE.getFiller();
            if (!filler.isAnonymous() || filler.isTopEntity()) {
                throw new IllegalArgumentException("The class expression '" + filler.toString() + "' cannot be named or a top entity (top (owl:Thing), a top object/data property or a rdfs:literal).");
            }
            
            OWLClass newClass = generateOWLClass(ontology, subClass, superClass);  // new concept name A
            OWLDataFactoryImpl owlDataFactory = new OWLDataFactoryImpl();
            List<OWLSubClassOfAxiom> normalizedAxioms = new ArrayList<>();

            normalizedAxioms.add(owlDataFactory.getOWLSubClassOfAxiom(filler, newClass));  // C' ⊑ A
            normalizedAxioms.add(owlDataFactory.getOWLSubClassOfAxiom(                     // ∃r.A ⊑ D
                owlDataFactory.getOWLObjectSomeValuesFrom(existentialCEProperty, newClass),
                superClass
            ));

            return normalizedAxioms;
        }

        // ⊥ ⊑ D   -->   ∅
        public static Collection<OWLSubClassOfAxiom> applyNF4(OWLOntology ontology, OWLSubClassOfAxiom axiom) {
            List<OWLSubClassOfAxiom> normalizedAxioms = new ArrayList<>();

            return normalizedAxioms;  // ∅ is treated as an empty list and not as a null value; so, the list exists, but there are 0 elements in it
        }

        // C' ⊑ D'  -->   { C' ⊑ A, A ⊑ D' }
        public static Collection<OWLSubClassOfAxiom> applyNF5(OWLOntology ontology, OWLSubClassOfAxiom axiom) {
            OWLClassExpression subClass = axiom.getSubClass();
            OWLClassExpression superClass = axiom.getSuperClass();

            OWLClass newClass = generateOWLClass(ontology, subClass, superClass);  // new concept name A
            OWLDataFactoryImpl owlDataFactory = new OWLDataFactoryImpl();
            List<OWLSubClassOfAxiom> normalizedAxioms = new ArrayList<>();

            normalizedAxioms.add(owlDataFactory.getOWLSubClassOfAxiom(subClass, newClass));    // C' ⊑ A
            normalizedAxioms.add(owlDataFactory.getOWLSubClassOfAxiom(newClass, superClass));  // A ⊑ D'

            return normalizedAxioms;
        }

        // B ⊑ ∃r.C'  -->   { B ⊑ ∃r.A, A ⊑ C' }
        public static Collection<OWLSubClassOfAxiom> applyNF6(OWLOntology ontology, OWLSubClassOfAxiom axiom) {
            OWLClassExpression subClass = axiom.getSubClass();
            OWLClassExpression superClass = axiom.getSuperClass();

            if (subClass.isAnonymous()) {
                throw new IllegalArgumentException("Subclass needs to be named (have an IRI). Please, note that class entities are named (they have an IRI) while class expressions are anonymous.");
            }

            OWLObjectSomeValuesFrom existentialCE = (OWLObjectSomeValuesFrom) superClass;
            OWLObjectPropertyExpression existentialCEProperty = existentialCE.getProperty();
            OWLClassExpression filler = existentialCE.getFiller();
            if (!filler.isAnonymous() || filler.isTopEntity()) {
                throw new IllegalArgumentException("The class expression '" + filler.toString() + "' cannot be named or a top entity (top (owl:Thing), a top object/data property or a rdfs:literal).");
            }

            OWLClass newClass = generateOWLClass(ontology, subClass, superClass);  // new concept name A
            OWLDataFactoryImpl owlDataFactory = new OWLDataFactoryImpl();
            List<OWLSubClassOfAxiom> normalizedAxioms = new ArrayList<>();

            normalizedAxioms.add(owlDataFactory.getOWLSubClassOfAxiom(                       // B ⊑ ∃r.A
                subClass,
                owlDataFactory.getOWLObjectSomeValuesFrom(existentialCEProperty, newClass))
            );
            normalizedAxioms.add(owlDataFactory.getOWLSubClassOfAxiom(newClass, filler));    // A ⊑ C'

            return normalizedAxioms;
        }

        // B ⊑ C ⊓ D   -->   { B ⊑ C, B ⊑ D }
        public static Collection<OWLSubClassOfAxiom> applyNF7(OWLOntology ontology, OWLSubClassOfAxiom axiom) {
            OWLClassExpression subClass = axiom.getSubClass();
            OWLClassExpression superClass = axiom.getSuperClass();

            if (subClass.isAnonymous()) {
                throw new IllegalArgumentException("Subclass needs to be named (have an IRI). Please, note that class entities are named (they have an IRI) while class expressions are anonymous.");
            }

            List<OWLClassExpression> intersectionOf = ((OWLObjectIntersectionOf) superClass).getOperandsAsList();
            OWLClassExpression left = intersectionOf.get(0);
            OWLClassExpression right = intersectionOf.get(intersectionOf.size()-1);


            OWLDataFactoryImpl owlDataFactory = new OWLDataFactoryImpl();
            List<OWLSubClassOfAxiom> normalizedAxioms = new ArrayList<>();

            normalizedAxioms.add(owlDataFactory.getOWLSubClassOfAxiom(subClass, left));   // B ⊑ C
            normalizedAxioms.add(owlDataFactory.getOWLSubClassOfAxiom(subClass, right));  // B ⊑ D

            return normalizedAxioms;
        }
    }
}