import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceType;

import com.elppreasoner.normalization.ELPPOntologyNormalizer;
import com.elppreasoner.reasoning.ElppReasoner;

public class FirstTest {
    public static void main(String[] args) {
        ElppReasoner reasoner = new ElppReasoner(createOWLOntologyA(), false, false);
        
        ELPPOntologyNormalizer normalizer = new ELPPOntologyNormalizer();
        reasoner.setOntologyNormalizer(normalizer);
        
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
    }





    public static OWLOntology createEmptyOWLOntology() {
        IRI iri = IRI.create("test");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        try {
            return manager.createOntology(iri);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
    }

    public static OWLOntology createOWLOntologyA() {
        OWLOntology ontology = createEmptyOWLOntology();
        OWLOntologyManager owlOntologyManager = ontology.getOWLOntologyManager();
        OWLDataFactory df = owlOntologyManager.getOWLDataFactory();
        IRI iri = owlOntologyManager.getOntologyDocumentIRI(ontology);

        OWLClass A = df.getOWLClass(iri + "#A");
        OWLClass B = df.getOWLClass(iri + "#B");
        OWLClass C = df.getOWLClass(iri + "#C");
        OWLClass D = df.getOWLClass(iri + "#D");

        OWLObjectProperty r = df.getOWLObjectProperty(iri + "#r");

        OWLObjectSomeValuesFrom rA = df.getOWLObjectSomeValuesFrom(r, A);
        OWLObjectSomeValuesFrom rB = df.getOWLObjectSomeValuesFrom(r, B);
        OWLObjectSomeValuesFrom rC = df.getOWLObjectSomeValuesFrom(r, C);
        OWLObjectSomeValuesFrom rrB = df.getOWLObjectSomeValuesFrom(r, rB);

        OWLObjectIntersectionOf AandB = df.getOWLObjectIntersectionOf(A, B);
        OWLObjectIntersectionOf AandC = df.getOWLObjectIntersectionOf(A, C);
        OWLObjectIntersectionOf CandD = df.getOWLObjectIntersectionOf(C, D);
        OWLObjectIntersectionOf BandrC = df.getOWLObjectIntersectionOf(B, rC);
        OWLObjectIntersectionOf BandrB = df.getOWLObjectIntersectionOf(B, rB);
        OWLObjectIntersectionOf rAandB = df.getOWLObjectIntersectionOf(rA, B);
        OWLObjectIntersectionOf rrBandD = df.getOWLObjectIntersectionOf(rrB, D);
        OWLObjectSomeValuesFrom rexistsAandB = df.getOWLObjectSomeValuesFrom(r, AandB);
        OWLObjectSomeValuesFrom rexistsAandC = df.getOWLObjectSomeValuesFrom(r, AandC);

        OWLSubClassOfAxiom AsubclassofBandrC = df.getOWLSubClassOfAxiom(A, BandrC);
        OWLSubClassOfAxiom BandrBsubclassofCandD = df.getOWLSubClassOfAxiom(BandrB, CandD);
        OWLSubClassOfAxiom CsubclassofrAandB = df.getOWLSubClassOfAxiom(C, rAandB);
        OWLSubClassOfAxiom DsubclassofrexistsAandC = df.getOWLSubClassOfAxiom(D, rexistsAandC);
        OWLSubClassOfAxiom rrBandDsubclassofrexistsAandB = df.getOWLSubClassOfAxiom(rrBandD, rexistsAandB);

        OWLNamedIndividual individual = df.getOWLNamedIndividual(iri + "#IO");
        OWLObjectOneOf io = df.getOWLObjectOneOf(individual);
        OWLSubClassOfAxiom IOsubclassofaxiomC = df.getOWLSubClassOfAxiom(io, C);

        ontology.add(
            AsubclassofBandrC,
            BandrBsubclassofCandD,
            CsubclassofrAandB,
            rrBandDsubclassofrexistsAandB,
            IOsubclassofaxiomC,
            DsubclassofrexistsAandC
        );

        return ontology;
    }
}
