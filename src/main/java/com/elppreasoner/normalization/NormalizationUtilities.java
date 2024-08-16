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

/**
 * <p>{@link NormalizationUtilities} is an utility class that implements all the methods that are useful to axioms normalization.
 * Other than methods to identify some class expression as a concept name, an individual, a basic concept, etc., or
 * to check if a given CBox is in normal form, it also contains an inner class, {@link NormalizationRulesManager}, that provides
 * a set of methods to identify and apply the "normalization rules" (NF2,NF3,...,NF7).</p>
 * <p><em>Note</em>. NF1 is excluded because it concerns the "role inclusion", not considered in our work.</p>
 * 
 * <p>A legenda of concepts, from theory to OWL implementation, follows:
 * <ul>
 *     <li>"bottom" (⊥)                 <->      "owl:Nothing",</li>
 *     <li>"top" (⊤)                    <->      "owl:Thing",</li>
 *     <li>"concept name"               <->      "OWLClass",</li>
 *     <li>"individual name | nominal"  <->      "OWLIndividual",</li>
 *     <li>"C ⊑ D"                      <->      "OWLSubclassOf",</li>
 *     <li>"C1 ≡ ... ≡ Cn"              <->      "EquivalentClasses",</li>
 *     <li>"enum of individuals"        <->      "OWLObjectOneOf",  // {a1}⊔{a2}⊔...⊔{an}</li>
 *     <li>"existential restriction ∃"  <->      "OWLObjectSomeValuesFrom",</li>
 *     <li>"intersection ⊓"             <->      "OWLObjectIntersectionOf"</li>
 * </ul>
 * <p><br /></p>
 * 
 * <p>Here are a couple of useful definitions.</p>
 * 
 * <ul>
 * <li>Def. In our domain, an EL++ constraint box (CBox) is a finite set of general concept inclusions (GCIs): C ⊑ D. In particular: C can be a
 * basic concept (BC) or a nominal {a}, while D can be either a basic concept (BC), a nominal {a} or a bottom (⊥). [Role inclusions excluded]</li>
 * 
 * <li>Def. Given a CBox C, BC_C (read "basic concept descriptions for C") denotes the smallest set of concept descriptions that contains the top
 * concept ⊤, all concept names used in C, and all concept descriptions of the form {a} or p(f1,..., fk) appearing in C.<br>
 * In our work, any given concept that belongs to such a set will be called "basic concept". In particular, a basic concept is either:<br>
 * <ul>
 *     <li>a concept name</li>
 *     <li>an individual name</li>
 *     <li>'top' (OWLThing)</li>
 *     <li>an enum of individuals (OWLObjectOneOf)</li>
 * </ul>
 * </li>
 * 
 * <li>Def. In our domain, a CBox C is in normal form ("normalized") if and only if all of its GCIs have one of the four following forms:
 * <ul>
 *     <li>C1 ⊑ D</li>
 *     <li>C1 ⊓ C2 ⊑ D</li>
 *     <li>C1 ⊑ ∃r.C2</li>
 *     <li>∃r.C1 ⊑ D</li>
 * </ul>
 * where C1 and C2 are basic concepts (also called "individual names" or "nominals") and D can be either a basic concept or a bottom (⊥).<br />
 * [Role inclusions excluded]
 * </li>
 * </ul>
 */
public final class NormalizationUtilities {

    /**
     * Token used to identify generated classes (dummy).
     */
    public static final String GENERATED_CLASS = "#GENERATED";

    /**
     * A simple private constructor to prevent the default parameter-less constructor from being used, as this is just a utility class.
     */
    private NormalizationUtilities() {
        throw new UnsupportedOperationException("Cannot instantiate this utility class.");
    }
    
    /**
     * <p>Checks if the given class expression is a concept name.</p>
     * <p>Concept names denote sets of objects, which are called "classes" in the OWL implementation (e.g. 'Person', 'Female').</p>
     * @param classExpression The expression to be checked
     * @return {@code true} if @param classExpression is a concept name; {@code false} otherwise
     */
    public static boolean isConceptName(OWLClassExpression classExpression) {
        return classExpression instanceof OWLClass &&  // is it a class?
            !classExpression.isOWLNothing() &&         // is it NOT owl:Nothing ("bottom" ⊥)?
            !classExpression.isOWLThing();             // is it NOT owl:Thing ("top" ⊤)?
    }

    /**
     * <p>Checks if the given class expression is an individual name.</p>
     * <p>Individual names denote objects, which are called "individuals" in the OWL implementation (e.g. 'John', 'Mary').</p>
     * @param classExpression The expression to be checked
     * @return {@code true} if {@code classExpression} is an individual name; {@code false} otherwise
     */
    public static boolean isIndividualName(OWLClassExpression classExpression) {
        return classExpression instanceof OWLIndividual;  // is it an individual name?
    }

    /**
     * <p>Checks if the subclass C of the given class expression "C ⊑ D" is a basic concept. In our work, a basic concept is either:
     * <ul>
     *     <li>a concept name</li>
     *     <li>an individual name</li>
     *     <li>'top' (OWLThing)</li>
     *     <li>an enum of individuals (OWLObjectOneOf)</li>
     * </ul></p>
     * 
     * <p>*</p>
     * @param subClassConcept The expression to be checked
     * @return {@code true} if {@code subClassConcept} is a basic concept; {@code false} otherwise
     */
    public static boolean isSubclassABasicConcept(OWLClassExpression subClassConcept) {
        return isConceptName(subClassConcept) ||        // is it a concept name?
            isIndividualName(subClassConcept) ||        // is it an individual name?
            subClassConcept.isOWLThing() ||             // is it owl:Thing ("top" ⊤)?
            subClassConcept instanceof OWLObjectOneOf;  // is it an enumeration of individuals?
    }

    /**
     * <p>Checks if the superclass D of the given class expression "C ⊑ D" is a basic concept. In our work, a basic concept is either:
     * <ul>
     *     <li>a concept name</li>
     *     <li>an individual name</li>
     *     <li>'top' (OWLThing)</li>
     *     <li>an enum of individuals (OWLObjectOneOf)</li>
     * </ul></p>
     * 
     * <p>*</p>
     * @param superClassConcept The expression to be checked
     * @return {@code true} if {@code superClassConcept} is a basic concept; {@code false} otherwise
     */
    public static boolean isSuperclassABasicConcept(OWLClassExpression superClassConcept) {
        return isConceptName(superClassConcept) ||        // is it a concept name?
            isIndividualName(superClassConcept) ||        // is it an individual name?
            superClassConcept.isOWLNothing() ||           // is it owl:Nothing ("bottom" ⊥)?  * The superclass of a GCI can also be ⊥ 
            superClassConcept.isOWLThing() ||             // is it owl:Thing ("top" ⊤)?
            superClassConcept instanceof OWLObjectOneOf;  // is it an enumeration of individuals?
    }

    /**
     * <p>Checks if the given CBox is in normal form (NF). A CBox is in normal form if and only if it has one of the following forms:
     * <ul>
     *     <li>C1 ⊑ D</li>
     *     <li>C1 ⊓ C2 ⊑ D</li>
     *     <li>C1 ⊑ ∃r.C2</li>
     *     <li>∃r.C1 ⊑ D</li>
     * </ul>
     * where C1 and C2 are basic concepts (also called "individual names" or "nominals")
     * </p>
     * 
     * <p>*</p>
     * @param subClassOfAxiom The CBox to check
     * @return {@code true} if {@code subClassOfAxiom} is in normal form; {@code false} otherwise
     */
    public static boolean isCBoxInNormalForm(OWLSubClassOfAxiom subClassOfAxiom) {
        OWLClassExpression subClass = subClassOfAxiom.getSubClass();
        OWLClassExpression superClass = subClassOfAxiom.getSuperClass();

        if (isSubclassABasicConcept(subClass) && isSuperclassABasicConcept(superClass)) {   // *C1 ⊑ D*
            return true;
        } else if (subClass instanceof OWLObjectIntersectionOf && isSuperclassABasicConcept(superClass)) { // *C1 ⊓ C2 ⊑ D*
            List<OWLClassExpression> operands = ((OWLObjectIntersectionOf) subClass).getOperandsAsList();  // the list containing C1 and C2
            if (isSubclassABasicConcept(operands.get(0)) && isSubclassABasicConcept(operands.get(operands.size()-1))) {
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


    /**
     * <p>{@code NormalizationRulesManager} is an inner class that provides a set of methods to identify and apply the "normalization rules"
     * (NF2,NF3,...,NF7). It is easy to prove that any CBox can be converted into an equivalent CBox in Normal Form (NF) using the translation
     * rules (applying one of the rules) NF1 to NF7, based on the form of the CBox.
     * <ul>
     *     <li>NF1: role inclusion, not considered in our work</li>
     *     <li>NF2: C ⊓ D' ⊑ E   -->   { D' ⊑ A, C ⊓ A ⊑ E }   * Please note that it can also be in the equivalent form D' ⊓ C</li>
     *     <li>NF3: ∃r.C' ⊑ D   -->   { C' ⊑ A, ∃r.A ⊑ D }</li>
     *     <li>NF4: ⊥ ⊑ D   -->   ∅</li>
     *     <li>NF5: C' ⊑ D'  -->   { C' ⊑ A, A ⊑ D' }</li>
     *     <li>NF6: B ⊑ ∃r.C'  -->   { B ⊑ ∃r.A, A ⊑ C' }</li>
     *     <li>NF7: B ⊑ C ⊓ D   -->   { B ⊑ C, B ⊑ D }</li>
     * </ul>
     * </p>
     * 
     * <p><em>Note 1.</em> "Rule application" means that the concept inclusion on the left-hand side is replaced with the set of concept inclusions on the
     * right-hand side.</p>
     * <p><em>Note 2.</em> In any rule, C' and D' are NOT basic concepts and A denotes a newly created, dummy concept name.</p>
     */
    public static class NormalizationRulesManager {

        /**
         * The normalization rules' {@code enum}. C' and D' are NOT basic concepts and A denotes a newly created, dummy concept name.
         */
        public enum NormalizationRule {
            // NF1,  // role inclusion, not considered in our work
            NF2,  // C ⊓ D' ⊑ E   -->   { D' ⊑ A, C ⊓ A ⊑ E }   * Please note that it can also be in the equivalent form D' ⊓ C
            NF3,  //  ∃r.C' ⊑ D   -->   { C' ⊑ A, ∃r.A ⊑ D }
            NF4,  //      ⊥ ⊑ D   -->   ∅
            NF5,  //     C' ⊑ D'  -->   { C' ⊑ A, A ⊑ D' }
            NF6,  //   B ⊑ ∃r.C'  -->   { B ⊑ ∃r.A, A ⊑ C' }
            NF7  //  B ⊑ C ⊓ D   -->   { B ⊑ C, B ⊑ D }
        }

        /**
         * Identifies the Normal Form rule (NF2 to NF7) that fits the given {@code axiom}
         * @param axiom The General Concept Inclusions (GCIs) axiom to be identified
         * @return The {@link NormalizationRule} to be applied to the given {@code axiom}
         */
        public static NormalizationRule identifyNF(OWLSubClassOfAxiom axiom) {
            OWLClassExpression subClass = axiom.getSubClass();
            OWLClassExpression superClass = axiom.getSuperClass();
    
            if (isSuperclassABasicConcept(superClass)) {  // NF5,NF6 excluded
                if (subClass instanceof OWLObjectIntersectionOf) {  // NF2
                    return NormalizationRule.NF2;
                }
    
                // NF2,NF5,NF6 excluded
                if (subClass instanceof OWLObjectSomeValuesFrom &&
                        !isSubclassABasicConcept(((OWLObjectSomeValuesFrom) subClass).getFiller())) {  // NF3
                    return NormalizationRule.NF3;
                }
            }
            
            if (isSubclassABasicConcept(subClass)) {  // NF6 or NF7
                if (superClass instanceof OWLObjectSomeValuesFrom &&
                        !isSuperclassABasicConcept(((OWLObjectSomeValuesFrom) superClass).getFiller())) {  // NF6
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

        /**
         * Generates a new dummy {@code OWLClass}, that corresponds to the new concept name A in the normalization rules.
         * @param ontology The ontology of the axiom being normalized, only used to get its IRI
         * @param subclass The subclass of the axiom being normalized
         * @param superclass The superclass of the axiom being normalized
         * @return The generated class (dummy)
         */
        public static OWLClass generateOWLClass(OWLOntology ontology, OWLClassExpression subclass, OWLClassExpression superclass) {
            return (new OWLDataFactoryImpl()).getOWLClass(
                ontology.getOWLOntologyManager().getOntologyDocumentIRI(ontology) +
                GENERATED_CLASS +
                subclass.toString().hashCode() +
                superclass.toString().hashCode()
                );
        }

        /**
         * <p>Applies the normalization rule NF2 to the given {@code axiom}:</p>
         * <p>&emsp;&emsp;C ⊓ D' ⊑ E   -->   { D' ⊑ A, C ⊓ A ⊑ E }&emsp;&emsp;* Please note that it can also be in the equivalent form D' ⊓ C</p>
         * @param ontology The ontology of the axiom being normalized, only used to create the new concept name A
         * @param axiom The axiom, expected to be in the form "C ⊓ D' ⊑ E", to which the normalization rule NF2 must be applied
         * @return The set of normalized axioms, i.e. { D' ⊑ A, C ⊓ A ⊑ E }
         */
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
            } else {  // if both operands of the intersection are basic concepts, return the axiom as it is
                normalizedAxioms.add(                                                                                 // C ⊓ D ⊑ E
                    owlDataFactory.getOWLSubClassOfAxiom(owlDataFactory.getOWLObjectIntersectionOf(left, right),
                    superClass
                ));
            }

            return normalizedAxioms;
        }

        /**
         * <p>Applies the normalization rule NF3 to the given {@code axiom}</p>
         * <p>&emsp;&emsp;∃r.C' ⊑ D   -->   { C' ⊑ A, ∃r.A ⊑ D }</p>
         * @param ontology The ontology of the axiom being normalized, only used to create the new concept name A
         * @param axiom The axiom, expected to be in the form "∃r.C' ⊑ D", to which the normalization rule NF3 must be applied
         * @return The set of normalized axioms, i.e. { C' ⊑ A, ∃r.A ⊑ D }
         */
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

        /**
         * <p>Applies the normalization rule NF4 to the given {@code axiom}</p>
         * &emsp;&emsp;⊥ ⊑ D   -->   ∅
         * @param ontology The ontology of the axiom being normalized (unused, but kept to maintain the same signature)
         * @param axiom The axiom, expected to be in the form "⊥ ⊑ D", to which the normalization rule NF4 must be applied
         * @return The set of normalized axioms, i.e. ∅ (an empty set, treated as an empty list and not as a null value)
         */
        public static Collection<OWLSubClassOfAxiom> applyNF4(OWLOntology ontology, OWLSubClassOfAxiom axiom) {
            return new ArrayList<>();
        }

        /**
         * <p>Applies the normalization rule NF5 to the given {@code axiom}</p>
         * &emsp;&emsp;C' ⊑ D'  -->   { C' ⊑ A, A ⊑ D' }
         * @param ontology The ontology of the axiom being normalized, only used to create the new concept name A
         * @param axiom The axiom, expected to be in the form "C' ⊑ D'", to which the normalization rule NF5 must be applied
         * @return The set of normalized axioms, i.e. { C' ⊑ A, A ⊑ D' }
         */
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

        /**
         * <p>Applies the normalization rule NF6 to the given {@code axiom}</p>
         * &emsp;&emsp;B ⊑ ∃r.C'  -->   { B ⊑ ∃r.A, A ⊑ C' }
         * @param ontology The ontology of the axiom being normalized, only used to create the new concept name A
         * @param axiom The axiom, expected to be in the form "B ⊑ ∃r.C'", to which the normalization rule NF6 must be applied
         * @return The set of normalized axioms, i.e. { B ⊑ ∃r.A, A ⊑ C' }
         */
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

        /**
         * <p>Applies the normalization rule NF7 to the given {@code axiom}</p>
         * &emsp;&emsp;B ⊑ C ⊓ D   -->   { B ⊑ C, B ⊑ D }
         * @param ontology The ontology of the axiom being normalized (unused, but kept to maintain the same signature)
         * @param axiom The axiom, expected to be in the form "B ⊑ C ⊓ D", to which the normalization rule NF7 must be applied
         * @return The set of normalized axioms, i.e. { B ⊑ C, B ⊑ D }
         */
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