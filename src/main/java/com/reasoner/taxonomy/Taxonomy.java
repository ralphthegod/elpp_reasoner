package com.reasoner.taxonomy;

import java.util.Map;
import java.util.Set;

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
}