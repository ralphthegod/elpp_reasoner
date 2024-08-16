package com.reasoner.taxonomy;

import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;

/**
 * <p>A {@link Taxonomy} is a class that represents a taxonomy of classes. It contains information about:
 * <ul>
 *     <li>Equivalency of concepts</li>
 *     <li>Subsumption between concepts</li>
 *     <li>Direct subsumption between concepts</li>
 * </ul>
 * It is NOT a graph structure and provides no method to build a taxonomy; instead, it provides six attributes that represent all the valid
 * information about the taxonomy, which has to be built with a compatible {@link TaxonomyBuilder} class.</p>
 * 
 * <p>This is the expected workflow involving a {@link Taxonomy}:
 * <ol>
 *     <li>Write a manager class that implements {@link TaxonomyBuilder}</li>
 *     <li>In its {@code build()} method, write your desired logic</li>
 *     <li>After the computation in 2., fill the {@link Taxonomy} fields</li>
 * </ol></p>
 */
public class Taxonomy {
    /**
     * A mapping between an OWL class and all the other OWL classes equivalent to it.
     */
    private Map<OWLClassExpression, Set<OWLClassExpression>> classToEquivalentClasses;

    /**
     * A mapping between an OWL class and its OWL node in the taxonomy.
     */
    private Map<OWLClass, OWLClassNode> classToNode;

    /**
     * A mapping between an OWL node and all of its direct superclasses. If A is a node that represents an OWL class, it is mapped to all the
     * other B-nodes (each one of them represents an OWL class) such that A ⊑ B. This is called a "direct subsumption" between A and B.
     */
    private Map<OWLClassNode, OWLClassNodeSet> nodeToDirectSuperClasses;

    /**
     * A mapping between an OWL node and all of its direct subclasses. If B is a node that represents an OWL class, it is mapped to all the
     * other A-nodes (each one of them represents an OWL class) such that A ⊑ B. This is called a "direct subsumption" between A and B.
     */
    private Map<OWLClassNode, OWLClassNodeSet> nodeToDirectSubClasses;

    /**
     * A mapping between an OWL node and all of its superclasses. If A is a node that represents an OWL class, it is mapped to all the
     * other B-nodes (each one of them represents an OWL class) such that A ⊑ ... ⊑ B. This is called a "generic subsumption" between A and B.
     */
    private Map<OWLClassNode, OWLClassNodeSet> nodeToAllSuperClasses;

    /**
     * A mapping between an OWL node and all of its subclasses. If B is a node that represents an OWL class, it is mapped to all the
     * other A-nodes (each one of them represents an OWL class) such that A ⊑ ... ⊑ B. This is called a "generic subsumption" between A and B.
     */
    private Map<OWLClassNode, OWLClassNodeSet> nodeToAllSubClasses;

    /**
     * Exception message to throw when a taxonomy has not been computed yet.
     */
    private static final String NOT_AN_OWLCLASS_INSTANCE = "The provided OWLClassExpression must be an instance of OWLClass.";


    /**
     * A simple, empty constructor for {@link Taxonomy}. This is useful when you have not computed the taxonomy information yet, but want to
     * compute it later and fill its properties later instead.
     */
    public Taxonomy() {}

    /**
     * A complete constructor for {@link Taxonomy}. This is useful when you have already computed the taxonomy information.
     * @param classToEquivalentClasses (Expected to be) A mapping between a class and all the other classes equivalent to it
     * @param classToNode (Expected to be) A mapping between a class and its taxonomy node
     * @param nodeToDirectSuperClasses (Expected to be) A mapping between a taxonomy node and all of its direct superclasses nodes
     * @param nodeToDirectSubClasses (Expected to be) A mapping between a taxonomy node and all of its direct subclasses nodes
     * @param nodeToAllSuperClasses (Expected to be) A mapping between a taxonomy node and all of its generic superclasses nodes
     * @param nodeToAllSubClasses (Expected to be) A mapping between a taxonomy node and all of its generic subclasses nodes
     */
    public Taxonomy(
        Map<OWLClassExpression, Set<OWLClassExpression>> classToEquivalentClasses,
        Map<OWLClass, OWLClassNode> classToNode,
        Map<OWLClassNode, OWLClassNodeSet> nodeToDirectSuperClasses,
        Map<OWLClassNode, OWLClassNodeSet> nodeToDirectSubClasses,
        Map<OWLClassNode, OWLClassNodeSet> nodeToAllSuperClasses,
        Map<OWLClassNode, OWLClassNodeSet> nodeToAllSubClasses
    ) {
        this.classToEquivalentClasses = classToEquivalentClasses;
        this.classToNode = classToNode;
        this.nodeToDirectSuperClasses = nodeToDirectSuperClasses;
        this.nodeToDirectSubClasses = nodeToDirectSubClasses;
        this.nodeToAllSuperClasses = nodeToAllSuperClasses;
        this.nodeToAllSubClasses = nodeToAllSubClasses;
    }

    /**
     * Gets the mapping between a class and all the other classes equivalent to it.
     * @return {@code classToEquivalentClasses}
     */
    public Map<OWLClassExpression, Set<OWLClassExpression>> getClassToEquivalentClasses() {
        return classToEquivalentClasses;
    }

    /**
     * Sets the mapping between a class and all the other classes equivalent to it.
     * @param classToEquivalentClasses
     */
    public void setClassToEquivalentClasses(Map<OWLClassExpression, Set<OWLClassExpression>> classToEquivalentClasses) {
        this.classToEquivalentClasses = classToEquivalentClasses;
    }

    /**
     * Gets the mapping between a class and its taxonomy node.
     * @return {@code classToNode}
     */
    public Map<OWLClass, OWLClassNode> getClassToNode() {
        return classToNode;
    }

    /**
     * Sets the mapping between a class and its taxonomy node.
     * @param classToNode
     */
    public void setClassToNode(Map<OWLClass, OWLClassNode> classToNode) {
        this.classToNode = classToNode;
    }

    /**
     * Gets the mapping between a taxonomy node and all of its direct superclass nodes.
     * @return {@code nodeToDirectSuperClasses}
     */
    public Map<OWLClassNode, OWLClassNodeSet> getNodeToDirectSuperClasses() {
        return nodeToDirectSuperClasses;
    }

    /**
     * Sets the mapping between a taxonomy node and all of its direct superclass nodes.
     * @param nodeToSuperclasses
     */
    public void setNodeToDirectSuperClasses(Map<OWLClassNode, OWLClassNodeSet> nodeToSuperclasses) {
        this.nodeToDirectSuperClasses = nodeToSuperclasses;
    }

    /**
     * Gets the mapping between a taxonomy node and all of its direct subclass nodes.
     * @return {@code nodeToDirectSubClasses}
     */
    public Map<OWLClassNode, OWLClassNodeSet> getNodeToDirectSubClasses() {
        return nodeToDirectSubClasses;
    }

    /**
     * Sets the mapping between a taxonomy node and all of its direct subclass nodes.
     * @param nodeToSubclasses
     */
    public void setNodeToDirectSubClasses(Map<OWLClassNode, OWLClassNodeSet> nodeToSubclasses) {
        this.nodeToDirectSubClasses = nodeToSubclasses;
    }

    /**
     * Gets the mapping between a taxonomy node and all of its generic superclass nodes.
     * @return {@code nodeToAllSuperClasses}
     */
    public Map<OWLClassNode, OWLClassNodeSet> getNodeToAllSuperClasses() {
        return nodeToAllSuperClasses;
    }

    /**
     * Sets the mapping between a taxonomy node and all of its generic superclass nodes.
     * @param nodeToAllSuperclasses
     */
    public void setNodeToAllSuperClasses(Map<OWLClassNode, OWLClassNodeSet> nodeToAllSuperclasses) {
        this.nodeToAllSuperClasses = nodeToAllSuperclasses;
    }

    /**
     * Gets the mapping between a taxonomy node and all of its generic subclass nodes.
     * @return {@code nodeToAllSubClasses}
     */
    public Map<OWLClassNode, OWLClassNodeSet> getNodeToAllSubClasses() {
        return nodeToAllSubClasses;
    }

    /**
     * Sets the mapping between a taxonomy node and all of its generic subclass nodes.
     * @param nodeToAllSubclasses
     */
    public void setNodeToAllSubClasses(Map<OWLClassNode, OWLClassNodeSet> nodeToAllSubclasses) {
        this.nodeToAllSubClasses = nodeToAllSubclasses;
    }


    /**
     * Gets the {@code Node} corresponding to the top node (containing {@code owl:Thing}) in the class hierarchy.
     * @return A {@code Node} containing {@code owl:Thing} that is the top node in the class hierarchy. This {@code Node} is essentially equal to
     * the {@code Node} returned by calling {@code getEquivalentClasses(org.semanticweb.owlapi.model.OWLClassExpression)} with a parameter of
     * {@code owl:Thing}.
     */
    public OWLClassNode getTopClassNode() {
        return classToNode.get(OWLManager.getOWLDataFactory().getOWLThing());
    }

    /**
     * Gets the {@code Node} corresponding to the bottom node (containing {@code owl:Thing}) in the class hierarchy.
     * @return A {@code Node} containing {@code owl:Nothing} that is the bottom node in the hierarchy. This {@code Node} is essentially equal to
     * the {@code Node} returned by calling {@code getEquivalentClasses(org.semanticweb.owlapi.model.OWLClassExpression)} with a parameter of
     * {@code owl:Nothing}.
     */
    public OWLClassNode getBottomClassNode() {
        return classToNode.get(OWLManager.getOWLDataFactory().getOWLNothing());
    }

    /**
     * Gets the set of named classes that are the strict (potentially direct) subclasses of the specified class expression with respect to the
     * reasoner axioms. Note that the classes are returned as a {@link org.semanticweb.owlapi.reasoner.NodeSet}.
     * @param ce The class expression whose strict (direct) subclasses are to be retrieved
     * @param direct Specifies if the direct subclasses should be retrived ({@code true}) or if the all subclasses (descendant) classes should
     * be retrieved ({@code false})
     * @return If {@code direct} is {@code true}, a {@code NodeSet} such that for each class {@code C} in the {@code NodeSet} the set of reasoner
     * axioms entails {@code DirectSubClassOf(C, ce)}. If {@code direct} is {@code false}, a {@code NodeSet} such that for each class {@code C} in
     * the {@code NodeSet} the set of reasoner axioms entails {@code StrictSubClassOf(C, ce)}. If {@code ce} is equivalent to {@code owl:Nothing}
     * then the empty {@code NodeSet} will be returned
     */
    public OWLClassNodeSet getSubClasses(OWLClassExpression ce, boolean direct) {
        if (!(ce instanceof OWLClass)) {
            throw new IllegalArgumentException(NOT_AN_OWLCLASS_INSTANCE);
        }
        OWLClassNode node = classToNode.get(ce);
        return direct ? nodeToDirectSubClasses.get(node) : nodeToAllSubClasses.get(node);
    }

    /**
     * Gets the set of named classes that are the strict (potentially direct) superclasses of the specified class expression with respect to the
     * reasoner axioms. Note that the classes are returned as a {@link org.semanticweb.owlapi.reasoner.NodeSet}.
     * @param ce The class expression whose strict (direct) superclasses are to be retrieved
     * @param direct Specifies if the direct superclasses should be retrived ({@code true}) or if the all superclasses (descendant) classes should
     * be retrieved ({@code false})
     * @return If {@code direct} is {@code true}, a {@code NodeSet} such that for each class {@code C} in the {@code NodeSet} the set of reasoner
     * axioms entails {@code DirectSubClassOf(ce, C)}. If {@code direct} is {@code false}, a {@code NodeSet} such that for each class {@code C} in
     * the {@code NodeSet} the set of reasoner axioms entails {@code StrictSubClassOf(C, ce)}. If {@code ce} is equivalent to {@code owl:Nothing}
     * then the empty {@code NodeSet} will be returned.
     */
    public OWLClassNodeSet getSuperClasses(OWLClassExpression ce, boolean direct) {
        if (!(ce instanceof OWLClass)) {
            throw new IllegalArgumentException(NOT_AN_OWLCLASS_INSTANCE);
        }
        OWLClassNode node = classToNode.get(ce);
        return direct ? nodeToDirectSuperClasses.get(node) : nodeToAllSuperClasses.get(node);
    }

    /**
     * Gets the set of named classes that are equivalent to the specified class expression with respect to the set of reasoner axioms. The classes
     * are returned as a {@link org.semanticweb.owlapi.reasoner.Node}.
     * @param ce The class expression whose equivalent classes are to be retrieved.
     * @return A node containing the named classes such that for each named class {@code C} in the node the root ontology imports closure entails
     * {@code EquivalentClasses(ce, C)}. If {@code ce} is not a class name (i.e. it is an anonymous class expression) and there are no such classes
     * {@code C} then the node will be empty. If {@code ce} is a named class then {@code ce} will be contained in the node. If {@code ce} is
     * unsatisfiable with respect to the set of reasoner axioms then the node representing and containing {@code owl:Nothing}, i.e. the bottom node,
     * will be returned. If {@code ce} is equivalent to {@code owl:Thing} with respect to the set of reasoner axioms then the node representing and
     * containing {@code owl:Thing}, i.e. the top node, will be returned.
     */
    public OWLClassNode getEquivalentClasses(OWLClassExpression ce) {
        if (!(ce instanceof OWLClass)) {
            throw new IllegalArgumentException(NOT_AN_OWLCLASS_INSTANCE);
        }
        OWLClassNode owlClassNode = new OWLClassNode();

        if (classToEquivalentClasses.get(ce) == null) {
            return owlClassNode;
        }
        
        for (OWLClassExpression c : classToEquivalentClasses.get(ce)) {
            owlClassNode.add((OWLClass) c);
        }
        return owlClassNode;
    }
}