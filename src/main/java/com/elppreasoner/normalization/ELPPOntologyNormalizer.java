package com.elppreasoner.normalization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;

import com.elppreasoner.normalization.NormalizationUtilities.NormalizationRulesManager.NormalizationRule;
import com.reasoner.normalization.OntologyNormalizer;


/**
 * {@code ELPPOntologyNormalizer} is a helper class that makes easier to "normalize" an ontology (knowledge base). This is also called
 * 'normalization phase'. In the following document, this object is also called "normalizer".
 * In our work, given an ontology KB, the equivalent normalized ontology KB' only contains concepts (CBoxes, i.e. sets of GCIs) that are in
 * normal form (NF). A concept C is in normal form ("normalized") if and only if all of its GCIs have either one of the four following forms:
 *     - C1 ⊑ D
 *     - C1 ⊓ C2 ⊑ D
 *     - C1 ⊑ ∃r.C2
 *     - ∃r.C1 ⊑ D
 * where C1 and C2 are basic concepts (also called "individual names" or "nominals") and D can be either a basic concept or a bottom (⊥).
 * [Role inclusions excluded]
 * 
 * This class implements two interfaces:
 *     • {@code OntologyNormalizer}, that you can find in the {@code com.reasoner.normalization} package
 *     • {@code OWLAxiomVisitor}, an interface that uses the Visitor Pattern. When the {@code OWLAxiom.accept(OWLAxiomVisitor)} method is called,
 *       the Visitor Pattern calls the visitor's {@code visit()} method, based on the type of the accepted axiom (in our case, only two types of
 *       axioms are considered, due to the subsumption: {@code OWLSubClassOfAxiom} and {@code OWLEquivalentClassesAxiom}.
 */
public class ELPPOntologyNormalizer implements OntologyNormalizer, OWLAxiomVisitor {

    private OWLOntology ontology;
    private OWLOntology normalizedOntology;
    private List<OWLAxiom> axiomsToNormalize;

    /**
     * The public constructor of the normalizer. No ontology required: the normalizer is a helper class that, once instantiated, can be used
     * to normalize any given ontology by calling the method {@code OWLOntology normalize(OWLOntology ontology)}. Similar to any Manager class.
     */
    public ELPPOntologyNormalizer() {
        this.ontology = null;
        this.normalizedOntology = null;
        this.axiomsToNormalize = null;
    }

    /**
     * A method from the {@code OntologyNormalizer} interface.
     * Given an @param ontology, returns the equivalent ontology in normal form. Each one of its CBoxes is checked and normalized if it's not in
     * normal form. This also means that any of the GCIs in a given CBox is checked and possibly queued up for normalization.
     * @param ontology The ontology to normalize
     * @return The equivalent @param ontology normalized
     */
    @SuppressWarnings("deprecation")
    @Override
    public OWLOntology normalize(OWLOntology ontology) {
        this.ontology = ontology;
        this.normalizedOntology = null;
        this.axiomsToNormalize = null;

        // Create an empty ontology and an empty list of eventual axioms to normalize
        this.axiomsToNormalize = new ArrayList<>(
            this.ontology.getTBoxAxioms(Imports.INCLUDED).size()
        );

        try {
            this.normalizedOntology = OWLManager.createOWLOntologyManager().createOntology();
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }

        // Analyze each axiom and put all non-normalized axioms in a list and all normalized axioms in the normalized ontology 
        Iterator<OWLAxiom> it = this.ontology.axioms().iterator();
        while(it.hasNext()) {
            OWLAxiom axiom = it.next();
            if (axiom instanceof OWLSubClassOfAxiom) {  // If the CBox has the form "C ⊑ D"
                if (!NormalizationUtilities.isCBoxInNormalForm((OWLSubClassOfAxiom) axiom)) {
                    this.axiomsToNormalize.add(axiom);
                } else {
                    this.normalizedOntology.add(axiom);
                }
            } else if (axiom instanceof OWLEquivalentClassesAxiom) {  // If the CBox has the form "C1 ≡ ... ≡ Cn"
                for (OWLSubClassOfAxiom subClassOfAxiom : ((OWLEquivalentClassesAxiom) axiom).asOWLSubClassOfAxioms()) {
                    if (!NormalizationUtilities.isCBoxInNormalForm(subClassOfAxiom)) {
                        this.axiomsToNormalize.add(axiom);
                    } else {
                        this.normalizedOntology.add(axiom);
                    }
                }
            }
        }

        /*
         * Normalize all non-normalized axioms. A do-while is necessary, because axioms can be made of more atomic non-normalized axioms, thus the
         * list is updated and further iteration is needed.
         */
        do {
            ListIterator<OWLAxiom> iterator = this.axiomsToNormalize.listIterator();
            while (iterator.hasNext()) {
                OWLAxiom axiomToCheck = iterator.next();
                iterator.remove();
                axiomToCheck.accept(this);  // calls the visitor(this)'s visit() method, based on its type (OWLSubClassOfAxiom / OWLEquivalentClassesAxiom)
            }
        } while (!this.axiomsToNormalize.isEmpty());

        this.ontology = null;
        this.axiomsToNormalize = null;

        return this.normalizedOntology;
    }

    /**
     * A method from the {@code OWLAxiomVisitor} interface, that uses the Visitor Pattern.
     * This method is called as a result of the call {@code axiom.accept(OWLAxiomVisitor)} on this @param axiom - a OWLSubClassOfAxiom. This should
     * be called only if the axiom is not in normal form. The given @param axiom is visited: based on its form, one of the normalization rules (NF1
     * to NF7) is applied. If any of its GCIs still is not in normal form, it is queued up for normalization.
     * @param axiom The axiom to normalize
     */
    @Override
    public void visit(OWLSubClassOfAxiom axiom) {
        NormalizationRule rule = NormalizationUtilities.NormalizationRulesManager.identifyNF(axiom);
        Collection<OWLSubClassOfAxiom> normalizedSetOfAxioms = new ArrayList<>();
        switch (rule) {
            case NF2:
                normalizedSetOfAxioms = NormalizationUtilities.NormalizationRulesManager.applyNF2(this.ontology, axiom);
                break;
            case NF3:
                normalizedSetOfAxioms = NormalizationUtilities.NormalizationRulesManager.applyNF3(this.ontology, axiom);
                break;
            case NF4:
                normalizedSetOfAxioms = NormalizationUtilities.NormalizationRulesManager.applyNF4(this.ontology, axiom);
                break;
            case NF5:
                normalizedSetOfAxioms = NormalizationUtilities.NormalizationRulesManager.applyNF5(this.ontology, axiom);
                break;
            case NF6:
                normalizedSetOfAxioms = NormalizationUtilities.NormalizationRulesManager.applyNF6(this.ontology, axiom);
                break;
            case NF7:
                normalizedSetOfAxioms = NormalizationUtilities.NormalizationRulesManager.applyNF7(this.ontology, axiom);
                break;
            default:
                break;
        }

        for (OWLSubClassOfAxiom a : normalizedSetOfAxioms) {
            if (NormalizationUtilities.isCBoxInNormalForm(a)) {
                this.normalizedOntology.addAxiom(a);
            } else {
                this.axiomsToNormalize.add(a);
            }
        }
    }

    /**
     * A method from the {@code OWLAxiomVisitor} interface, that uses the Visitor Pattern.
     * This method is called as a result of the call {@code axiom.accept(OWLAxiomVisitor)} on this @param axiom - a OWLEquivalentClassesAxiom. This
     * should be called only if the axiom is not in normal form. The given @param axiom is visited: since it has the form "C1 ≡ ... ≡ Cn", it is
     * simply transformed into the equivalent {@code OWLSubClassOfAxiom} axiom. If any of its GCIs is not in normal form, it is queued up for
     * normalization.
     * @param axiom The axiom to normalize
     */
    @Override
    public void visit(OWLEquivalentClassesAxiom axiom) {
        Collection<OWLSubClassOfAxiom> subClassOfAxioms = axiom.asOWLSubClassOfAxioms();

        for (OWLSubClassOfAxiom a : subClassOfAxioms) {
            if (NormalizationUtilities.isCBoxInNormalForm(a)) {
                this.normalizedOntology.addAxiom(a);
            } else {
                this.axiomsToNormalize.add(a);
            }
        }
    }
}