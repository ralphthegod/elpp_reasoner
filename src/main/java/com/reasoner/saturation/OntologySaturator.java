package com.reasoner.saturation;

import com.reasoner.querying.OntologyAccessManager;

public class OntologySaturator {

    private final OntologyAccessManager ontologyAccessManager;
    private final ContextAccessManager contextManager;

    public OntologySaturator(OntologyAccessManager ontologyAccessManager, ContextAccessManager contextManager) {
        this.ontologyAccessManager = ontologyAccessManager;
        this.contextManager = contextManager;
    }

    public void saturate() {
        contextManager.initialize(ontologyAccessManager);
        // TODO: Implement saturation
    }

}