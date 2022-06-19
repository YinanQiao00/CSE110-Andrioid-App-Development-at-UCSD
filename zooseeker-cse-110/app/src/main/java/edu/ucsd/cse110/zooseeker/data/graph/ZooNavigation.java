package edu.ucsd.cse110.zooseeker.data.graph;


import android.content.Context;
import android.util.Log;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm;
import org.jgrapht.alg.shortestpath.CHManyToManyShortestPaths;
import org.jgrapht.alg.shortestpath.DijkstraManyToManyShortestPaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ucsd.cse110.zooseeker.data.ZooData;
import edu.ucsd.cse110.zooseeker.data.search.ZooSearch;

public class ZooNavigation extends INavigation {
    Graph<String, IdentifiedWeightedEdge> graph;

    @Deprecated
    public static ZooNavigation createInstance(Context context, String path) {
        ZooNavigation nav = new ZooNavigation();
        nav.reloadData(ZooData.loadZooGraphJSON(context, path));
        return nav;
    }

    @Override
    public INavigation reloadData(Graph<String, IdentifiedWeightedEdge> data) {
        this.graph = data;
        return this;
    }

    @Override
    public GraphPath<String, IdentifiedWeightedEdge> findShortestPath(String start, String end) {
        return DijkstraShortestPath.findPathBetween(graph, start, end);
    }

    @Override
    public GraphPath<String, IdentifiedWeightedEdge> findShortestPathFromMany(String start, Set<String> targets) {
        ManyToManyShortestPathsAlgorithm alg = new CHManyToManyShortestPaths(graph);
        Set<String> source = new HashSet<String>();
        source.add(start);
        ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths paths = alg.getManyToManyPaths(source,targets);

        double min = Double.MAX_VALUE;
        String minId = null;
        for(String target : targets)
        {
            double w = paths.getWeight(start, target);
            if(w < min || (w==min && target.compareTo(minId) < 0))
            {
                min = w;
                minId = target;
            }
        }

        return paths.getPath(start, minId);
    }

    @Override
    public List<GraphPath<String, IdentifiedWeightedEdge>> findShortestPaths(String start, Set<String> targets) {
        ManyToManyShortestPathsAlgorithm alg = new CHManyToManyShortestPaths(graph);
        List<GraphPath<String,IdentifiedWeightedEdge>> path = new ArrayList<GraphPath<String,IdentifiedWeightedEdge>>();

        String curr = start;
        Set<String> source = new HashSet<String>();
        source.add(curr);
        while(targets.size() > 0) {
            ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths paths = alg.getManyToManyPaths(source,targets);

            double min = Double.MAX_VALUE;
            String minId = null;
            for(String target : targets)
            {
                double w = paths.getWeight(curr, target);
                if(w < min || (w==min && target.compareTo(minId) < 0))
                {
                    min = w;
                    minId = target;
                }
            }

            path.add(paths.getPath(curr,minId));
            curr = minId;
            targets.remove(minId);
            source.clear();
            source.add(curr);
        }

        return path;
    }

}
