package edu.ucsd.cse110.zooseeker;

import static org.junit.Assert.*;

import static edu.ucsd.cse110.zooseeker.util.plan.PlanUtil.PathFormatter;

import edu.ucsd.cse110.zooseeker.data.DataManager;
import edu.ucsd.cse110.zooseeker.data.ZooData;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ucsd.cse110.zooseeker.data.graph.INavigation;
import edu.ucsd.cse110.zooseeker.data.graph.IdentifiedWeightedEdge;
import edu.ucsd.cse110.zooseeker.data.graph.ZooNavigation;
import edu.ucsd.cse110.zooseeker.data.search.ISearch;
import edu.ucsd.cse110.zooseeker.data.search.ZooSearch;
import edu.ucsd.cse110.zooseeker.data.test.EmptyActivity;
import edu.ucsd.cse110.zooseeker.util.Constants;


@RunWith(AndroidJUnit4.class)
public class PathToTextTester {

    @Rule
    public ActivityScenarioRule rule = new ActivityScenarioRule<>(EmptyActivity.class);

    private Map<String, ZooData.VertexInfo> vInfo;
    private Map<String, ZooData.EdgeInfo> eInfo;
    private Graph<String, IdentifiedWeightedEdge> g;
    private INavigation navigator;
    private DataManager dm;
    
    private static final String[] ids = new String[]{
            "entrance_exit_gate","entrance_plaza","gorillas",
            "lions","gators","elephant_odyssey","arctic_foxes"};

    private static final int GATE = 0, PLAZA = 1, GORILLAS = 2, LIONS = 3, GATORS = 4, ELEPHANTS = 5, FOXES = 6;

    @Before
    public void initializeInfo()
    {
        ActivityScenario scene = rule.getScenario();
        scene.moveToState(Lifecycle.State.CREATED);
        scene.onActivity( activity -> {
            dm = DataManager.instantiate(activity, Constants.LEGACY_NODE_FILE_NAME, Constants.LEGACY_EDGE_FILE_NAME, Constants.LEGACY_GRAPH_FILE_NAME, ZooSearch.class, ZooNavigation.class);
            vInfo = dm.vertexInfoMap;
            eInfo = dm.edgeInfoMap;
            g = dm.graph;
            navigator = dm.navigation;
        });
    }


    @Test
    public void testSimplePath() {
        String start = "entrance_exit_gate";
        String goal = "elephant_odyssey";
        String text = "";
        GraphPath<String, IdentifiedWeightedEdge> path = DijkstraShortestPath.findPathBetween(g, start, goal);

        //List<GraphPath<String, IdentifiedWeightedEdge>> paths = new ArrayList<GraphPath<String, IdentifiedWeightedEdge>>();
        //paths.add(path);

        text = "(1) Walk 10 ft along Entrance Way from 'Entrance and Exit Gate' to 'Entrance Plaza'.\n" +
                "(2) Walk 100 ft along Reptile Road from 'Entrance Plaza' to 'Alligators'.\n" +
                "(3) Walk 200 ft along Sharp Teeth Shortcut from 'Alligators' to 'Lions'.\n" +
                "(4) Walk 200 ft along Africa Rocks Street from 'Lions' to 'Elephant Odyssey'.";

        assertEquals(1+1, 2);
        assertEquals(text, PathFormatter.pathToTextDetailed(path, dm).stream().reduce((total, line) -> {return total + "\n" + line;}).get());

    }

    @Test
    public void testExhibitDistanceFormat()
    {
//        Set<String> targets = new HashSet<String>();
//        String actual = "";
//        String assume = "";
//        for(int i = 1; i < ids.length; i++) {
//            targets.add(ids[i]);
//        }
//        List<GraphPath<String, IdentifiedWeightedEdge>> paths = navigator.findShortestPaths(ids[GATE],targets);
//        List<String> vertexOrder = PathFormatter.getVertexIdOrder(paths, 0);
//        List<String> edgeOrder = PathFormatter.getEndEdgeIdOrder(paths, 0, dm);
//        List<Double> weightOrder = PathFormatter.getCumulativeDistance(paths, 0);
//
//        List<String> exhibitsOrder = PathFormatter.exhibitsDistanceFormat(vertexOrder, weightOrder, edgeOrder);
//        for (String str: exhibitsOrder) {
//            actual += str;
//        }
//        assume = "entrance_plaza,  10.0 ft,  Entrance Way\n" +
//                "gators,  110.0 ft,  Reptile Road\n" +
//                "lions,  310.0 ft,  Sharp Teeth Shortcut\n" +
//                "elephant_odyssey,  510.0 ft,  Africa Rocks Street\n" +
//                "gorillas,  910.0 ft,  Africa Rocks Street\n" +
//                "arctic_foxes,  1410.0 ft,  Arctic Avenue\n" +
//                "entrance_exit_gate,  1720.0 ft,  Entrance Way\n";
//
//        assertEquals(actual, assume);
        //assertEquals(1+1, 2);
    }

}



