package com.elppreasoner.normalization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;

import com.reasoner.normalization.OntologyNormalizer;

public class ELPPOntologyNormalizer implements OntologyNormalizer, OWLAxiomVisitor {

    private OWLOntology normalizedOntology;
    private List<OWLAxiom> axiomsToNormalize;

    public ELPPOntologyNormalizer() {}

    @SuppressWarnings("deprecation")
    @Override
    public OWLOntology normalize(OWLOntology ontology) {
        // Create an empty ontology
        IRI ontologyIRI = ontology.getOWLOntologyManager().getOntologyDocumentIRI(ontology);
        List<OWLAxiom> ontologyTBoxAxioms = new ArrayList<>(
            ontology.getTBoxAxioms(Imports.INCLUDED).size()
        );


        // Analyze each axiom and put all non-normalized axioms in a list and all normalized axioms in the normalized ontology 
        Iterator<OWLAxiom> it = ontology.axioms().iterator();
        while(it.hasNext()) {  
            OWLAxiom axiom = it.next();
            if (axiom instanceof OWLSubClassOfAxiom) {  // If the CBox has the form "C ⊑ D"
                if (!NormalizationUtilities.isCBoxInNormalForm((OWLSubClassOfAxiom) axiom)) {
                    axiomsToNormalize.add(axiom);
                } else {
                    normalizedOntology.add(axiom);
                }
            } else if (axiom instanceof OWLEquivalentClassesAxiom) {  // If the CBox has the form "C1 ≡ ... ≡ Cn"
                for (OWLSubClassOfAxiom subClassOfAxiom : ((OWLEquivalentClassesAxiom) axiom).asOWLSubClassOfAxioms()) {
                    if (!NormalizationUtilities.isCBoxInNormalForm(subClassOfAxiom)) {
                        axiomsToNormalize.add(axiom);
                    } else {
                        normalizedOntology.add(axiom);
                    }
                }
            }
        }

        // Normalize each non-normalized axiom
        // TODO

        return normalizedOntology;
    }
}
