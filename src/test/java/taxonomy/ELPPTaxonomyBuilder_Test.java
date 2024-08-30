package taxonomy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.InferenceDepth;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.elppreasoner.normalization.ELPPOntologyNormalizer;
import com.elppreasoner.reasoning.ELPPReasoner;

import utils.TestingUtilities;

public class ELPPTaxonomyBuilder_Test {

    void taxonomyTest(OWLOntology ontology, boolean normalized, boolean concurrentMode) {
        if (normalized) {
            ontology = new ELPPOntologyNormalizer().normalize(ontology);
        }
        
        ELPPReasoner elppReasoner = new ELPPReasoner(ontology, false, concurrentMode);
        elppReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        OWLReasoner elk = new ElkReasonerFactory().createReasoner(ontology);
        elk.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        ontology.classesInSignature().forEach(owlClass -> {
            assertEquals(elppReasoner.getEquivalentClasses(owlClass), elk.getEquivalentClasses(owlClass));
            assertEquals(elppReasoner.getSubClasses(owlClass), elk.getSubClasses(owlClass));
            assertEquals(elppReasoner.getSubClasses(owlClass, InferenceDepth.DIRECT), elk.getSubClasses(owlClass, InferenceDepth.DIRECT));
            assertEquals(elppReasoner.getSuperClasses(owlClass), elk.getSuperClasses(owlClass));
            assertEquals(elppReasoner.getSuperClasses(owlClass, InferenceDepth.DIRECT), elk.getSuperClasses(owlClass, InferenceDepth.DIRECT));
        });
    }

    @Nested
    class ItalianFood_TaxonomyBuildingTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/italian-food.owl";

        @Test
        @DisplayName("ITALIAN FOOD ONTOLOGY TAXONOMY BUILDING TEST 1 - buildTaxonomy (normalized ontology, not concurrent)")
        void ItalianFood_buildTaxonomy_n() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            taxonomyTest(ontology, true, false);
        }

        @Test
        @DisplayName("ITALIAN FOOD ONTOLOGY TAXONOMY BUILDING TEST 2 - buildTaxonomy (normalized ontology, concurrent)")
        void ItalianFood_buildTaxonomy_nc() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            taxonomyTest(ontology, true, true);
        }
    }

    @Nested
    class SCTO_TaxonomyBuildingTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/scto-modified.owl";

        @Test
        @DisplayName("MODIFIED SNOMED CT ONTOLOGY TAXONOMY BUILDING TEST 1 - buildTaxonomy (normalized ontology, not concurrent)")
        void SCTO_buildTaxonomy_n() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            taxonomyTest(ontology, true, false);
        }

        @Test
        @DisplayName("MODIFIED SNOMED CT ONTOLOGY TAXONOMY BUILDING TEST 2 - buildTaxonomy (normalized ontology, concurrent)")
        void SCTO_buildTaxonomy_nc() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            taxonomyTest(ontology, true, true);
        }
    }

    @Nested
    class GALEN_TaxonomyBuildingTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/full-galen-modified.owl";

        @Test
        @DisplayName("MODIFIED GALEN ONTOLOGY TAXONOMY BUILDING TEST 1 - buildTaxonomy (normalized ontology, not concurrent)")
        void GALEN_buildTaxonomy_n() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            taxonomyTest(ontology, true, false);
        }

        @Test
        @DisplayName("MODIFIED SNOMED CT ONTOLOGY TAXONOMY BUILDING TEST 2 - buildTaxonomy (normalized ontology, concurrent)")
        void GALEN_buildTaxonomy_nc() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            taxonomyTest(ontology, true, true);
        }
    }

    @Nested
    class GO_TaxonomyBuildingTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/go-modified.owl";

        @Test
        @DisplayName("MODIFIED GENE ONTOLOGY TAXONOMY BUILDING TEST 1 - buildTaxonomy (normalized ontology, not concurrent)")
        void GO_buildTaxonomy_n() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            taxonomyTest(ontology, true, false);
        }

        @Test
        @DisplayName("MODIFIED GENE ONTOLOGY TAXONOMY BUILDING TEST 2 - buildTaxonomy (normalized ontology, concurrent)")
        void GO_buildTaxonomy_nc() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            taxonomyTest(ontology, true, true);
        }
    }
}