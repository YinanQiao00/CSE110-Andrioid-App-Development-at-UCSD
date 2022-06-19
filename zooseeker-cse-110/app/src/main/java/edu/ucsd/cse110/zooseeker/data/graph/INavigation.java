package edu.ucsd.cse110.zooseeker.data.graph;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ucsd.cse110.zooseeker.data.ZooData;

public abstract class INavigation {

    public INavigation() {}

    public abstract List<GraphPath<String, IdentifiedWeightedEdge>> findShortestPaths (String start, Set<String> targets);

    public abstract GraphPath<String,IdentifiedWeightedEdge> findShortestPath(String start, String end);

    public abstract GraphPath<String,IdentifiedWeightedEdge> findShortestPathFromMany(String start, Set<String> targets);

    public abstract INavigation reloadData(Graph<String, IdentifiedWeightedEdge> data);

}
