package edu.ucsd.cse110.zooseeker;


import static org.junit.Assert.*;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.jgrapht.GraphPath;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ucsd.cse110.zooseeker.data.DataManager;
import edu.ucsd.cse110.zooseeker.data.graph.INavigation;
import edu.ucsd.cse110.zooseeker.data.graph.IdentifiedWeightedEdge;
import edu.ucsd.cse110.zooseeker.data.graph.ZooNavigation;
import edu.ucsd.cse110.zooseeker.data.search.ISearch;
import edu.ucsd.cse110.zooseeker.data.search.ZooSearch;
import edu.ucsd.cse110.zooseeker.data.test.EmptyActivity;
import edu.ucsd.cse110.zooseeker.util.Constants;

@RunWith(AndroidJUnit4.class)
public class NavigationTester {

	@Rule
	public ActivityScenarioRule rule = new ActivityScenarioRule<>(EmptyActivity.class);

	private INavigation navigator;

	private static final String[] ids = new String[]{
			"entrance_exit_gate","entrance_plaza","gorillas",
			"lions","gators","elephant_odyssey","arctic_foxes"};

	private static final int GATE = 0, PLAZA = 1, GORILLAS = 2, LIONS = 3, GATORS = 4, ELEPHANTS = 5, FOXES = 6;

	@Before
	public void initializeNavigator()
	{
		ActivityScenario scene = rule.getScenario();
		scene.moveToState(Lifecycle.State.CREATED);
		scene.onActivity( activity -> {
			navigator = DataManager.instantiate(activity, Constants.LEGACY_NODE_FILE_NAME, Constants.LEGACY_EDGE_FILE_NAME, Constants.LEGACY_GRAPH_FILE_NAME, ZooSearch.class, ZooNavigation.class).navigation;
		});
	}


	@Test
	public void testSimplePath() {
		Set<String> targets = new HashSet<String>();
		targets.add(ids[FOXES]);
		assertFindShortestPath(navigator,ids[GATE],targets,
				0,4,4,0);
	}

	@Test
	public void testEveryExhibitPath() {
		Set<String> targets = new HashSet<String>();
		for(int i = 1; i < ids.length; i++)
			targets.add(ids[i]);
		assertFindShortestPath(navigator,ids[GATE],targets,
				0,5,6,3,3,2,1,4,4,0);
	}

	@Test
	public void testVariedExhibitPaths() {
		Set<String> targets = new HashSet<String>();
		targets.add(ids[FOXES]);
		targets.add(ids[ELEPHANTS]);
		assertFindShortestPath(navigator,ids[GATE],targets,
				0,4,4,5,6,3,3,6,5,0);

		targets.clear();
		targets.add(ids[GORILLAS]);
		targets.add(ids[LIONS]);
		targets.add(ids[ELEPHANTS]);
		assertFindShortestPath(navigator,ids[GATE],targets,
				0,1,2,3,3,6,5,0);

		targets.clear();
		targets.add(ids[GORILLAS]);
		targets.add(ids[ELEPHANTS]);
		targets.add(ids[FOXES]);
		assertFindShortestPath(navigator,ids[GATE],targets,
				0,1,2,3,3,6,5,4,4,0);

		targets.clear();
		targets.add(ids[GORILLAS]);
		targets.add(ids[LIONS]);
		targets.add(ids[GATORS]);
		assertFindShortestPath(navigator,ids[GATE],targets,
				0,5,6,2,1,0);
	}

	private static void assertFindShortestPath(INavigation navigator, String start, Set<String> targets, int... expectedEdges)
	{
		List<GraphPath<String, IdentifiedWeightedEdge>> path = navigator.findShortestPaths(ids[GATE],targets);
		int i = 0;
		for(GraphPath<String, IdentifiedWeightedEdge> p : path)
		{
			for(IdentifiedWeightedEdge edge : p.getEdgeList())
			{
				assertEquals(String.format("The %dth edge traveled should not be \"%s\" but instead should be \"edge-%d\"",
						i+1, edge.getId(), expectedEdges[i] ), "edge-" + expectedEdges[i], edge.getId());
				i++;
			}
		}
	}

}
