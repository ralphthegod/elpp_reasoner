package normalization;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import com.elppreasoner.normalization.ELPPOntologyNormalizer;
import com.elppreasoner.normalization.NormalizationUtilities;

import utils.TestingUtilities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ELPPOntologyNormalizer_Test {
    @Nested
    class ItalianFood_NormalizationTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/italian-food.owl";

        private static final int TEST1_EXPECTED = 14; // Translations from OWLEquivalentClassesAxioms to OWLSubClassOfAxioms included
        private static final int TEST2_EXPECTED = 70; // Translations from OWLEquivalentClassesAxioms to OWLSubClassOfAxioms included

        int countNormalizedAxioms(ArrayList<OWLSubClassOfAxiom> axioms) {
            int normalizedAxioms = 0;
            for (OWLSubClassOfAxiom axiom : axioms) {
                if (NormalizationUtilities.isCBoxInNormalForm(axiom)) {
                    normalizedAxioms++;
                }
            }
            return normalizedAxioms;
        }

        /**
         * Checks if the Italian Food ontology contains the expected amount of normalized axioms (14).
         */
        @Test
        @DisplayName("ITALIAN FOOD ONTOLOGY NORMALIZATION TEST 1 - isCBoxInNormalForm")
        void isCBoxInNormalForm_test() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);

            ArrayList<OWLSubClassOfAxiom> axioms = new ArrayList<>();
            ontology.axioms().forEach(axiom -> {
                if (axiom instanceof OWLEquivalentClassesAxiom) {
                    axioms.addAll(((OWLEquivalentClassesAxiom) axiom).asOWLSubClassOfAxioms());
                }
                if (axiom instanceof OWLSubClassOfAxiom) {
                    axioms.add((OWLSubClassOfAxiom) axiom);
                }
            });

            assertEquals(TEST1_EXPECTED, countNormalizedAxioms(axioms));
        }

        /**
         * Checks if the expected normalized ontology is returned when normalize() is invoked on the Italian Food ontology.
         * @throws OWLOntologyStorageException 
         * @throws IOException 
         */
        @Test
        @DisplayName("ITALIAN FOOD ONTOLOGY NORMALIZATION TEST 2 - normalize")
        void ItalianFood_normalize() throws OWLOntologyStorageException, IOException {
            OWLOntology ontology = new ELPPOntologyNormalizer().normalize(
                TestingUtilities.loadOntology(ONTOLOGY_PATH)
            );

            // Count all OWLSubClassOfAxiom axioms in the normalized ontology
            ArrayList<OWLSubClassOfAxiom> axioms = new ArrayList<>();
            ontology.axioms().forEach(axiom -> {
                if (axiom instanceof OWLSubClassOfAxiom) {
                    axioms.add((OWLSubClassOfAxiom) axiom);
                }
            });

            FileOutputStream fileOutputStream = new FileOutputStream("src/test/resources/ontologies/italian-food-normalized.xml");
            ontology.saveOntology(fileOutputStream);
            fileOutputStream.close();

            assertEquals(TEST2_EXPECTED, countNormalizedAxioms(axioms));
        }
    }
}
