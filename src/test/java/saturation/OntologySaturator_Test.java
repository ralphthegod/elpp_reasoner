package saturation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.elppreasoner.normalization.ELPPOntologyNormalizer;
import com.elppreasoner.reasoning.rules.BottomSuperclassRoleExpansionInferenceRule;
import com.elppreasoner.reasoning.rules.IntersectionSuperclassesInferenceRule;
import com.elppreasoner.reasoning.rules.NominalChainExpansionInferenceRule;
import com.elppreasoner.reasoning.rules.SubclassRoleExpansionInferenceRule;
import com.elppreasoner.reasoning.rules.SuperclassRoleExpansionInferenceRule;
import com.elppreasoner.reasoning.rules.ToldSuperclassesInferenceRule;
import com.reasoner.querying.OntologyAccessManager;
import com.reasoner.saturation.ContextAccessManager;
import com.reasoner.saturation.OntologySaturator;

import utils.TestingUtilities;

public class OntologySaturator_Test {
    private static final boolean EXPECTED_RESULT = true;

    void saturationTest(OWLOntology ontology, boolean normalized, boolean concurrentMode) {
        if (normalized) {
            ontology = new ELPPOntologyNormalizer().normalize(ontology);
        }

        OntologyAccessManager ontologyAccessManager = new OntologyAccessManager(ontology);
        ontologyAccessManager.registerRule(new ToldSuperclassesInferenceRule());
        ontologyAccessManager.registerRule(new IntersectionSuperclassesInferenceRule());
        ontologyAccessManager.registerRule(new SubclassRoleExpansionInferenceRule());
        ontologyAccessManager.registerRule(new SuperclassRoleExpansionInferenceRule());
        ontologyAccessManager.registerRule(new BottomSuperclassRoleExpansionInferenceRule());
        ontologyAccessManager.registerRule(new NominalChainExpansionInferenceRule());

        OntologySaturator saturator = new OntologySaturator(ontologyAccessManager, new ContextAccessManager(), concurrentMode);
        Set<OWLSubClassOfAxiom> conclusions = saturator.saturate();

        OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
        OWLReasoner elk = reasonerFactory.createReasoner(ontology);
        elk.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        for (OWLSubClassOfAxiom axiom : conclusions) {
            assertEquals(EXPECTED_RESULT, elk.isEntailed(axiom));
        }
    }

    @Nested
    class ItalianFood_SaturationTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/italian-food.owl";

        @Test
        @DisplayName("ITALIAN FOOD ONTOLOGY SATURATION TEST 1 - saturate (non-normalized ontology, not concurrent)")
        void ItalianFood_saturate() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, false, false);
        }

        @Test
        @DisplayName("ITALIAN FOOD ONTOLOGY SATURATION TEST 2 - saturate (non-normalized ontology, concurrent)")
        void ItalianFood_saturate_c() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, false, true);
        }

        @Test
        @DisplayName("ITALIAN FOOD ONTOLOGY SATURATION TEST 3 - saturate (normalized ontology, not concurrent)")
        void ItalianFood_saturate_n() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, true, false);
        }

        @Test
        @DisplayName("ITALIAN FOOD ONTOLOGY SATURATION TEST 4 - saturate (normalized ontology, concurrent)")
        void ItalianFood_saturate_nc() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, true, true);
        }
    }

    @Nested
    class SCTO_SaturationTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/scto-modified.owl";

        @Test
        @DisplayName("MODIFIED SNOMED CT ONTOLOGY SATURATION TEST 1 - saturate (non-normalized ontology, not concurrent)")
        void SCTO_saturate() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, false, false);
        }

        @Test
        @DisplayName("MODIFIED SNOMED CT ONTOLOGY SATURATION TEST 2 - saturate (non-normalized ontology, concurrent)")
        void SCTO_saturate_c() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, false, true);
        }

        @Test
        @DisplayName("MODIFIED SNOMED CT ONTOLOGY SATURATION TEST 3 - saturate (normalized ontology, not concurrent)")
        void SCTO_saturate_n() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, true, false);
        }

        @Test
        @DisplayName("MODIFIED SNOMED CT ONTOLOGY SATURATION TEST 4 - saturate (normalized ontology, concurrent)")
        void SCTO_saturate_nc() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, true, true);
        }
    }

    @Nested
    class GALEN_SaturationTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/full-galen-modified.owl";

        @Test
        @DisplayName("MODIFIED GALEN ONTOLOGY SATURATION TEST 1 - saturate (non-normalized ontology, not concurrent)")
        void GALEN_saturate() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, false, false);
        }

        @Test
        @DisplayName("MODIFIED GALEN ONTOLOGY SATURATION TEST 2 - saturate (non-normalized ontology, concurrent)")
        void GALEN_saturate_c() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, false, true);
        }

        @Test
        @DisplayName("MODIFIED GALEN ONTOLOGY SATURATION TEST 3 - saturate (normalized ontology, not concurrent)")
        void GALEN_saturate_n() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, true, false);
        }

        @Test
        @DisplayName("MODIFIED GALEN ONTOLOGY SATURATION TEST 4 - saturate (normalized ontology, concurrent)")
        void GALEN_saturate_nc() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, true, true);
        }
    }

    @Nested
    class GO_SaturationTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/go-modified.owl";

        @Test
        @DisplayName("MODIFIED GENE ONTOLOGY SATURATION TEST 1 - saturate (non-normalized ontology, not concurrent)")
        void GO_saturate() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, false, false);
        }

        @Test
        @DisplayName("MODIFIED GENE ONTOLOGY SATURATION TEST 2 - saturate (non-normalized ontology, concurrent)")
        void GO_saturate_c() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, false, true);
        }

        @Test
        @DisplayName("MODIFIED GENE ONTOLOGY SATURATION TEST 3 - saturate (normalized ontology, not concurrent)")
        void GO_saturate_n() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, true, false);
        }

        @Test
        @DisplayName("MODIFIED GENE ONTOLOGY SATURATION TEST 4 - saturate (normalized ontology, concurrent)")
        void GO_saturate_nc() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            saturationTest(ontology, true, true);
        }
    }
}