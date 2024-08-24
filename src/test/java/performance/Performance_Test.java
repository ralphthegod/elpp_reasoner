package performance;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.elppreasoner.normalization.ELPPOntologyNormalizer;
import com.elppreasoner.reasoning.ELPPReasoner;

import utils.TestingUtilities;

public class Performance_Test {
    private static final int ITERATIONS = 10;

    private static final String OUTPUT_FILE = "src/test/resources/performance-results.txt";
    private static BufferedWriter writer;
    

    @BeforeAll
    static void prepareOutputFile() throws IOException {
        writer = new BufferedWriter(new FileWriter(OUTPUT_FILE));
    }

    @AfterAll
    static void closeOutputFile() throws IOException {
        writer.close();
    }

    void performanceTest(OWLOntology ontology) throws IOException {
        ontology = new ELPPOntologyNormalizer().normalize(ontology);

        double time;
        double t0;

        ELPPReasoner elppReasoner = new ELPPReasoner(ontology, false, false);
        time = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            t0 = System.nanoTime();
            elppReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            time += ((System.nanoTime() - t0) / 1_000_000_000);
        }
        writer.append("             [ELPP] Time: " + (time / ITERATIONS) + "s\n");
        
        ELPPReasoner concurrent_elppReasoner = new ELPPReasoner(ontology, true, true);
        time = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            t0 = System.nanoTime();
            concurrent_elppReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            time += ((System.nanoTime() - t0) / 1_000_000_000);
        }
        writer.append("  [Concurrent ELPP] Time: " + (time / ITERATIONS) + "s\n");

        OWLReasoner elk = new ElkReasonerFactory().createReasoner(ontology);
        time = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            t0 = System.nanoTime();
            elk.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            time += ((System.nanoTime() - t0) / 1_000_000_000);
        }
        writer.append("              [ELK] Time: " + (time / ITERATIONS) + "s\n");

        OWLReasoner hermiT = new ReasonerFactory().createReasoner(ontology);
        time = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            t0 = System.nanoTime();
            hermiT.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            time += ((System.nanoTime() - t0) / 1_000_000_000);
        }
        writer.append("           [HermiT] Time: " + (time / ITERATIONS) + "s\n");
    }

    @Nested
    class ItalianFood_PerformanceTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/italian-food.owl";

        @Test
        @DisplayName("ITALIAN FOOD ONTOLOGY PERFORMANCE TEST - ELPPReasoner vs Concurrent ELPPReasoner vs ELK vs HermiT")
        void ItalianFood_Performance() throws IOException {
            writer.append("Italian Food\n");
            performanceTest(TestingUtilities.loadOntology(ONTOLOGY_PATH));
            writer.append("\n");
        }
    }

    @Nested
    class SCTO_PerformanceTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/scto-modified.owl";

        @Test
        @DisplayName("SNOMED CT ONTOLOGY PERFORMANCE TEST - ELPPReasoner vs Concurrent ELPPReasoner vs ELK vs HermiT")
        void SCTO_Performance() throws IOException {
            writer.append("Snomed CT\n");
            performanceTest(TestingUtilities.loadOntology(ONTOLOGY_PATH));
            writer.append("\n");
        }
    }

    @Nested
    class GALEN_PerformanceTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/full-galen-modified.owl";

        @Test
        @DisplayName("GALEN ONTOLOGY PERFORMANCE TEST - ELPPReasoner vs Concurrent ELPPReasoner vs ELK vs HermiT")
        void GALEN_Performance() throws IOException {
            writer.append("GALEN\n");
            performanceTest(TestingUtilities.loadOntology(ONTOLOGY_PATH));
            writer.append("\n");
        }
    }

    @Nested
    class GO_PerformanceTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/go-modified.owl";

        @Test
        @DisplayName("GENE ONTOLOGY PERFORMANCE TEST - ELPPReasoner vs Concurrent ELPPReasoner vs ELK vs HermiT")
        void SCTO_Performance() throws IOException {
            writer.append("GO\n");
            performanceTest(TestingUtilities.loadOntology(ONTOLOGY_PATH));
            writer.append("\n");
        }
    }
}