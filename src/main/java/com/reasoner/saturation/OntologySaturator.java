package com.reasoner.saturation;

import com.reasoner.querying.OntologyAccessManager;

public class OntologySaturator {

    private OntologyAccessManager ontologyAccessManager;
    private ContextAccessManager contextManager;

    public OntologySaturator(OntologyAccessManager ontologyAccessManager, ContextAccessManager contextManager) {
        this.ontologyAccessManager = ontologyAccessManager;
        this.contextManager = contextManager;
    }
}