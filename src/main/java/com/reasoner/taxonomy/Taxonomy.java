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
    private Map<OWLClassNode, OWLClassNodeSet> nodeToDirectSuperclasses;
    private Map<OWLClassNode, OWLClassNodeSet> nodeToDirectSubclasses;
    private Map<OWLClassNode, OWLClassNodeSet> nodeToAllSuperclasses;
    private Map<OWLClassNode, OWLClassNodeSet> nodeToAllSubclasses;

    public Taxonomy() {}

    public Taxonomy(
        Map<OWLClassExpression, Set<OWLClassExpression>> classToEquivalentClasses,
        Map<OWLClass, OWLClassNode> classToNode,
        Map<OWLClassNode, OWLClassNodeSet> nodeToSuperclasses,
        Map<OWLClassNode, OWLClassNodeSet> nodeToSubclasses,
        Map<OWLClassNode, OWLClassNodeSet> nodeToAllSuperclasses,
        Map<OWLClassNode, OWLClassNodeSet> nodeToAllSubclasses
    ) {
        this.classToEquivalentClasses = classToEquivalentClasses;
        this.classToNode = classToNode;
        this.nodeToDirectSuperclasses = nodeToSuperclasses;
        this.nodeToDirectSubclasses = nodeToSubclasses;
        this.nodeToAllSuperclasses = nodeToAllSuperclasses;
        this.nodeToAllSubclasses = nodeToAllSubclasses;
    }

    public Map<OWLClassExpression, Set<OWLClassExpression>> getClassToEquivalentClasses() {
        return classToEquivalentClasses;
    }

    //
    public Set<OWLClassExpression> getEquivalentClasses(OWLClassExpression owlClass) {
        return classToEquivalentClasses.get(owlClass);
    }

    public void setClassToEquivalentClasses(Map<OWLClassExpression, Set<OWLClassExpression>> classToEquivalentClasses) {
        this.classToEquivalentClasses = classToEquivalentClasses;
    }

    public Map<OWLClass, OWLClassNode> getClassToNode() {
        return classToNode;
    }

    //
    public OWLClassNode getNode(OWLClass owlClass) {
        return classToNode.get(owlClass);
    }

    public void setClassToNode(Map<OWLClass, OWLClassNode> classToNode) {
        this.classToNode = classToNode;
    }

    public Map<OWLClassNode, OWLClassNodeSet> getNodeToDirectSuperclasses() {
        return nodeToDirectSuperclasses;
    }

    //
    public OWLClassNodeSet getDirectSuperclasses(OWLClassNode node) {
        return nodeToDirectSuperclasses.get(node);
    }

    public void setNodeToDirectSuperclasses(Map<OWLClassNode, OWLClassNodeSet> nodeToSuperclasses) {
        this.nodeToDirectSuperclasses = nodeToSuperclasses;
    }

    public Map<OWLClassNode, OWLClassNodeSet> getNodeToDirectSubclasses() {
        return nodeToDirectSubclasses;
    }

    //
    public OWLClassNodeSet getDirectSubclasses(OWLClassNode node) {
        return nodeToDirectSubclasses.get(node);
    }

    public void setNodeToDirectSubclasses(Map<OWLClassNode, OWLClassNodeSet> nodeToSubclasses) {
        this.nodeToDirectSubclasses = nodeToSubclasses;
    }

    public Map<OWLClassNode, OWLClassNodeSet> getNodeToAllSuperclasses() {
        return nodeToAllSuperclasses;
    }

    //
    public OWLClassNodeSet getAllSuperclasses(OWLClassNode node) {
        return nodeToAllSuperclasses.get(node);
    }

    public void setNodeToAllSuperclasses(Map<OWLClassNode, OWLClassNodeSet> nodeToAllSuperclasses) {
        this.nodeToAllSuperclasses = nodeToAllSuperclasses;
    }

    public Map<OWLClassNode, OWLClassNodeSet> getNodeToAllSubclasses() {
        return nodeToAllSubclasses;
    }

    //
    public OWLClassNodeSet getAllSubclasses(OWLClassNode node) {
        return nodeToAllSubclasses.get(node);
    }

    public void setNodeToAllSubclasses(Map<OWLClassNode, OWLClassNodeSet> nodeToAllSubclasses) {
        this.nodeToAllSubclasses = nodeToAllSubclasses;
    }
}
