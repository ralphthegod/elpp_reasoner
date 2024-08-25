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

import com.elppreasoner.normalization.ELPPOntologyNormalizer;
import com.elppreasoner.reasoning.ELPPReasoner;

import utils.TestingUtilities;

public class Performance_Test {
    private static final int ITERATIONS = 10;

    private static final String OUTPUT_FILE = "src/test/resources/performance-results.txt";
    private static final String SCRIPT_PROGRAM = "py";
    private static final String SCRIPT_PATH = "src/test/java/performance/performance-analysis.py";
    private static BufferedWriter writer;
    private static ProcessBuilder processBuilder;
    

    @BeforeAll
    static void prepareOutputFile() throws IOException {
        writer = new BufferedWriter(new FileWriter(OUTPUT_FILE));
    }

    @AfterAll
    static void closeOutputFile() throws IOException {
        writer.close();
        producePlots();
    }

    static void producePlots(){
        processBuilder = new ProcessBuilder(SCRIPT_PROGRAM, SCRIPT_PATH);
        processBuilder.redirectErrorStream(true);
        Process p;
        try {
            p = processBuilder.start();
            System.out.println("Running plotting script... ");
            int exitCode;
            exitCode = p.waitFor();
            System.out.println("Exit Code : "+exitCode);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    void performanceTest(OWLOntology ontology) throws IOException {
        ontology = new ELPPOntologyNormalizer().normalize(ontology);

        double time;
        double t0;

        // ELPPReasoner performance
        time = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            t0 = System.nanoTime();
            new ELPPReasoner(ontology, false, false).precomputeInferences(InferenceType.CLASS_HIERARCHY);
            time += ((System.nanoTime() - t0) / 1_000_000_000);
            System.out.println("ELPPReasoner#"+i+" total elapsed time: " + time);
        }
        writer.append("             [ELPP] Time: " + (time / ITERATIONS) + "s\n");
        
        // Concurrent ELPPReasoner performance
        time = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            t0 = System.nanoTime();
            new ELPPReasoner(ontology, true, true).precomputeInferences(InferenceType.CLASS_HIERARCHY);
            time += ((System.nanoTime() - t0) / 1_000_000_000);
            System.out.println("CCELPPReasoner#"+i+" total elapsed time: " + time);
        }
        writer.append("  [Concurrent ELPP] Time: " + (time / ITERATIONS) + "s\n");

        // ELK performance
        time = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            t0 = System.nanoTime();
            new ElkReasonerFactory().createReasoner(ontology).precomputeInferences(InferenceType.CLASS_HIERARCHY);
            time += ((System.nanoTime() - t0) / 1_000_000_000);
            System.out.println("ELK#"+i+" total elapsed time: " + time);
        }
        writer.append("              [ELK] Time: " + (time / ITERATIONS) + "s\n");

        // HermiT performance
        time = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            t0 = System.nanoTime();
            new ReasonerFactory().createReasoner(ontology).precomputeInferences(InferenceType.CLASS_HIERARCHY);
            time += ((System.nanoTime() - t0) / 1_000_000_000);
            System.out.println("HermiT#"+i+" total elapsed time: " + time);
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