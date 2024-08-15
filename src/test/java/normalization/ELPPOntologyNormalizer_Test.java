package normalization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.elppreasoner.normalization.ELPPOntologyNormalizer;
import com.elppreasoner.normalization.NormalizationUtilities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ELPPOntologyNormalizer_Test {
    @Nested
    class ItalianFood_NormalizationTest {
        private static final int TEST1_EXPECTED = 14; // Translations from OWLEquivalentClassesAxioms to OWLSubClassOfAxioms included
        private static final int TEST2_EXPECTED = 70; // Translations from OWLEquivalentClassesAxioms to OWLSubClassOfAxioms included

        /**
         * Checks if the Italian Food ontology contains the expected amount of normalized axioms (14).
         */
        @Test
        @DisplayName("ITALIAN FOOD ONTOLOGY NORMALIZATION TEST 1 - isCBoxInNormalForm")
        void isCBoxInNormalForm_test() {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            String ontologyPath = "src/test/resources/ontologies/normalization_test/italian-food.owl";
            OWLOntology ontology;
            try {
                ontology = manager.loadOntologyFromOntologyDocument(new File(ontologyPath));
            } catch (OWLOntologyCreationException e) {
                throw new RuntimeException(e);
            }

            ArrayList<OWLSubClassOfAxiom> axioms = new ArrayList<>();
            ontology.axioms().forEach(axiom -> {
                if (axiom instanceof OWLEquivalentClassesAxiom) {
                    axioms.addAll(((OWLEquivalentClassesAxiom) axiom).asOWLSubClassOfAxioms());
                }
                if (axiom instanceof OWLSubClassOfAxiom) {
                    axioms.add((OWLSubClassOfAxiom) axiom);
                }
            });

            int normalizedAxioms = 0;
            for (OWLSubClassOfAxiom axiom : axioms) {
                if (NormalizationUtilities.isCBoxInNormalForm(axiom)) {
                    normalizedAxioms++;
                }
            }

            assertEquals(TEST1_EXPECTED, normalizedAxioms);
        }

        /**
         * Checks if the expected normalized ontology is returned when normalize() is invoked on the Italian Food ontology.
         * @throws OWLOntologyStorageException 
         * @throws IOException 
         */
        @Test
        @DisplayName("ITALIAN FOOD ONTOLOGY NORMALIZATION TEST 2 - normalize")
        void ItalianFood_normalize() throws OWLOntologyStorageException, IOException {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            String ontologyPath = "src/test/resources/ontologies/normalization_test/italian-food.owl";
            OWLOntology ontology;
            try {
                ontology = manager.loadOntologyFromOntologyDocument(new File(ontologyPath));
            } catch (OWLOntologyCreationException e) {
                throw new RuntimeException(e);
            }
            
            ELPPOntologyNormalizer normalizer = new ELPPOntologyNormalizer();
            OWLOntology normalizedOntology = normalizer.normalize(ontology);

            // Count all OWLSubClassOfAxiom axioms in the normalized ontology
            ArrayList<OWLSubClassOfAxiom> axioms = new ArrayList<>();
            normalizedOntology.axioms().forEach(axiom -> {
                if (axiom instanceof OWLSubClassOfAxiom) {
                    axioms.add((OWLSubClassOfAxiom) axiom);
                }
            });
            int normalizedAxioms = 0;
            for (OWLSubClassOfAxiom axiom : axioms) {
                if (NormalizationUtilities.isCBoxInNormalForm(axiom)) {
                    normalizedAxioms++;
                }
            }

            FileOutputStream fileOutputStream = new FileOutputStream("src/test/resources/ontologies/normalization_test/italian-food-normalized.xml");
            normalizedOntology.saveOntology(fileOutputStream);
            fileOutputStream.close();

            assertEquals(TEST2_EXPECTED, normalizedAxioms);
        }
    }
}
