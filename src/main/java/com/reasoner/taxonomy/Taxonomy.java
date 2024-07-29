package com.reasoner.taxonomy;

import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;

// TODO: javadoc
public class Taxonomy {
    private Map<OWLClassExpression, Set<OWLClassExpression>> classToEquivalentClasses;
    private Map<OWLClass, OWLClassNode> classToNode;
    private Map<OWLClassNode, OWLClassNodeSet> nodeToDirectSuperClasses;
    private Map<OWLClassNode, OWLClassNodeSet> nodeToDirectSubClasses;
    private Map<OWLClassNode, OWLClassNodeSet> nodeToAllSuperClasses;
    private Map<OWLClassNode, OWLClassNodeSet> nodeToAllSubClasses;

    public Taxonomy() {}

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

    public Map<OWLClassExpression, Set<OWLClassExpression>> getClassToEquivalentClasses() {
        return classToEquivalentClasses;
    }

    public void setClassToEquivalentClasses(Map<OWLClassExpression, Set<OWLClassExpression>> classToEquivalentClasses) {
        this.classToEquivalentClasses = classToEquivalentClasses;
    }

    public Map<OWLClass, OWLClassNode> getClassToNode() {
        return classToNode;
    }

    public void setClassToNode(Map<OWLClass, OWLClassNode> classToNode) {
        this.classToNode = classToNode;
    }

    public Map<OWLClassNode, OWLClassNodeSet> getNodeToDirectSuperClasses() {
        return nodeToDirectSuperClasses;
    }

    public void setNodeToDirectSuperClasses(Map<OWLClassNode, OWLClassNodeSet> nodeToSuperclasses) {
        this.nodeToDirectSuperClasses = nodeToSuperclasses;
    }

    public Map<OWLClassNode, OWLClassNodeSet> getNodeToDirectSubClasses() {
        return nodeToDirectSubClasses;
    }

    public void setNodeToDirectSubClasses(Map<OWLClassNode, OWLClassNodeSet> nodeToSubclasses) {
        this.nodeToDirectSubClasses = nodeToSubclasses;
    }

    public Map<OWLClassNode, OWLClassNodeSet> getNodeToAllSuperClasses() {
        return nodeToAllSuperClasses;
    }

    public void setNodeToAllSuperClasses(Map<OWLClassNode, OWLClassNodeSet> nodeToAllSuperclasses) {
        this.nodeToAllSuperClasses = nodeToAllSuperclasses;
    }

    public Map<OWLClassNode, OWLClassNodeSet> getNodeToAllSubClasses() {
        return nodeToAllSubClasses;
    }

    public void setNodeToAllSubClasses(Map<OWLClassNode, OWLClassNodeSet> nodeToAllSubclasses) {
        this.nodeToAllSubClasses = nodeToAllSubclasses;
    }
}