package utils;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public final class TestingUtilities {
    public static OWLOntology loadOntology(String ontologyPath) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(new File(ontologyPath));
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
        return ontology;
    }
}
