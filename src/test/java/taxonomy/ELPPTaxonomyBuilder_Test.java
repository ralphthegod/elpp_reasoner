package taxonomy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.liveontologies.puli.Inference;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.elk.reasoner.ReasonerFactory;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceDepth;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.owlapi.explanation.util.OntologyUtils;
import com.elppreasoner.normalization.ELPPOntologyNormalizer;
import com.elppreasoner.reasoning.ELPPReasoner;
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

public class ELPPTaxonomyBuilder_Test {
    @Nested
    class ItalianFood_TaxonomyBuildingTest {
        private static final String ONTOLOGY_PATH = "src/test/resources/ontologies/italian-food.owl";

        void taxonomyTest(OWLOntology ontology, boolean normalized, boolean concurrentMode) {
            if (normalized) {
                ontology = new ELPPOntologyNormalizer().normalize(ontology);
            }
            
            ELPPReasoner elppReasoner = new ELPPReasoner(ontology, false, concurrentMode);
            elppReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

            OWLReasoner elk = new ElkReasonerFactory().createReasoner(ontology);
            elk.precomputeInferences(InferenceType.CLASS_HIERARCHY);

            System.out.println(ontology.classesInSignature());
            ontology.classesInSignature().forEach(owlClass -> {
                System.out.println("\nCLASS: " + owlClass);
                //System.out.println(elppReasoner.getTaxonomyBuildingTime());
                //System.out.println(elppReasoner.getTaxonomy().getClassToEquivalentClasses());
                //assertEquals(elppReasoner.getEquivalentClasses(owlClass), elk.getEquivalentClasses(owlClass));
                if (elppReasoner.getEquivalentClasses(owlClass) != null) System.out.println("[ELPP] EquivalentClasses:\t" + elppReasoner.getEquivalentClasses(owlClass));
                System.out.println(" [ELK] EquivalentClasses:\t" + elk.getEquivalentClasses(owlClass));
                //assertEquals(elppReasoner.getSubClasses(owlClass), elk.getSubClasses(owlClass));
                if (elppReasoner.getSubClasses(owlClass) != null) System.out.println("[ELPP] SubClasses:\t" + elppReasoner.getSubClasses(owlClass));
                System.out.println(" [ELK] SubClasses:\t" + elk.getSubClasses(owlClass));
                //assertEquals(elppReasoner.getSubClasses(owlClass, InferenceDepth.DIRECT), elk.getSubClasses(owlClass, InferenceDepth.DIRECT));
                if (elppReasoner.getSubClasses(owlClass, InferenceDepth.DIRECT) != null) System.out.println("[ELPP] DirSubClasses:\t" + elppReasoner.getSubClasses(owlClass, InferenceDepth.DIRECT));
                System.out.println(" [ELK] DirSubClasses:\t" + elk.getSubClasses(owlClass, InferenceDepth.DIRECT));
                //assertEquals(elppReasoner.getSuperClasses(owlClass), elk.getSuperClasses(owlClass));
                if (elppReasoner.getSuperClasses(owlClass) != null) System.out.println("[ELPP] SuperClasses:\t" + elppReasoner.getSuperClasses(owlClass));
                System.out.println(" [ELK] SuperClasses:\t" + elk.getSuperClasses(owlClass));
                //assertEquals(elppReasoner.getSuperClasses(owlClass, InferenceDepth.DIRECT), elk.getSuperClasses(owlClass, InferenceDepth.DIRECT));
                if (elppReasoner.getSuperClasses(owlClass, InferenceDepth.DIRECT) != null) System.out.println("[ELPP] DirSuperClasses:\t" + elppReasoner.getSuperClasses(owlClass, InferenceDepth.DIRECT));
                System.out.println(" [ELK] DirSuperClasses:\t" + elk.getSuperClasses(owlClass, InferenceDepth.DIRECT));
            });
        }

        @Test
        @DisplayName("ITALIAN FOOD ONTOLOGY TAXONOMY BUILDING TEST 1 - buildTaxonomy (non-normalized ontology, not concurrent)")
        void ItalianFood_buildTaxonomy() {
            OWLOntology ontology = TestingUtilities.loadOntology(ONTOLOGY_PATH);
            taxonomyTest(ontology, false, false);
        }
    }
}
