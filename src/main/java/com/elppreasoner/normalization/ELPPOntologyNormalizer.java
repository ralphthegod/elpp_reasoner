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

public class ELPPOntologyNormalizer implements OntologyNormalizer, OWLAxiomVisitor {

    private OWLOntology ontology;
    private OWLOntology normalizedOntology;
    private List<OWLAxiom> axiomsToNormalize;

    public ELPPOntologyNormalizer() {
        this.ontology = null;
        this.normalizedOntology = null;
        this.axiomsToNormalize = null;
    }

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

        // Normalize all non-normalized axioms. A do-while is necessary, because axioms can be made of more atomic non-normalized axioms, thus the list
        // is updated and further iteration is needed
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

    /*
     * If axiom has the form "C1 ≡ ... ≡ Cn", simply transform it into the equivalent SubClassOf axiom and just check if each subaxiom is in NF. 
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