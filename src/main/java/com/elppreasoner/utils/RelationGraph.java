package com.elppreasoner.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLObject;

import com.github.jsonldjava.shaded.com.google.common.graph.EndpointPair;
import com.github.jsonldjava.shaded.com.google.common.graph.GraphBuilder;
import com.github.jsonldjava.shaded.com.google.common.graph.MutableGraph;

public class RelationGraph{
    
    private final MutableGraph<OWLObject> relationGraph = GraphBuilder.directed().build();

    public void insertNode(OWLObject node){
        relationGraph.addNode(node);
    }

    public void insertEdge(OWLObject node1, OWLObject node2){
        EndpointPair<OWLObject> edge = EndpointPair.ordered(node1, node2);
        relationGraph.putEdge(edge);
    }
    
    public boolean reach(OWLObject from, OWLObject to){
        final Set<OWLObject> visited = new HashSet<>();
        final List<OWLObject> toVisit = new ArrayList<>();
        boolean find = false;

        toVisit.add(from);

        while(!toVisit.isEmpty()){
            OWLObject current = toVisit.remove(0);
            visited.add(current);
            for(OWLObject next : relationGraph.successors(current)){
                if(next.equals(to)){
                    find = true;
                    break;
                }
                if(!visited.contains(next)){
                    toVisit.add(next);
                }
            }
            if(find) break;
        }

        return find;
    }
}
