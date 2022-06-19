package edu.ucsd.cse110.zooseeker;

import static org.junit.Assert.*;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ucsd.cse110.zooseeker.data.DataManager;
import edu.ucsd.cse110.zooseeker.data.ZooData;
import edu.ucsd.cse110.zooseeker.data.ZooData.VertexInfo.Kind;
import edu.ucsd.cse110.zooseeker.data.graph.ZooNavigation;
import edu.ucsd.cse110.zooseeker.data.search.ISearch;
import edu.ucsd.cse110.zooseeker.data.search.ZooSearch;
import edu.ucsd.cse110.zooseeker.data.test.EmptyActivity;
import edu.ucsd.cse110.zooseeker.util.Constants;


@RunWith(AndroidJUnit4.class)
public class SearchTester {

	@Rule
	public ActivityScenarioRule rule = new ActivityScenarioRule<>(EmptyActivity.class);

	@Test
	public void testSampleData() {
		ActivityScenario scene = rule.getScenario();
		scene.moveToState(Lifecycle.State.CREATED);
		scene.onActivity( activity -> {
			DataManager dataManager = DataManager.instantiate(activity, Constants.LEGACY_NODE_FILE_NAME, Constants.LEGACY_EDGE_FILE_NAME, Constants.LEGACY_GRAPH_FILE_NAME, ZooSearch.class, ZooNavigation.class);
			ISearch search = dataManager.search;

			ZooData.VertexInfo info = search.getById("entrance_exit_gate");
			assertVertexInfo(info, "Entrance and Exit Gate", Kind.GATE,
					"enter", "leave", "start", "begin", "entrance", "exit");
			info = search.getById("gorillas");
			assertVertexInfo(info, "Gorillas", Kind.EXHIBIT,
					"gorilla", "monkey", "ape", "mammal");


			Set<String> has = new HashSet<String>();
			has.add("arctic_foxes");
			has.add("elephant_odyssey");
			has.add("lions");
			has.add("gorillas");
			List<ZooData.VertexInfo> infos = search.getByTag("mammal");
			for(ZooData.VertexInfo i : infos)
				assertTrue(has.contains(i.id));
			assertTrue(infos.size() == 4);

			infos = search.getByType(Kind.INTERSECTION);
			assertTrue(infos.size() == 1);
			assertEquals("entrance_plaza", infos.get(0).id);


			infos = search.getMatchingWaypoints("Entrance");
			for(ZooData.VertexInfo i : infos)
				assertTrue(i.name.equals("Entrance Plaza") || i.name.equals("Entrance and Exit Gate"));
			assertEquals(2, infos.size());
			infos = search.getMatchingWaypoints("Exit");
			for(ZooData.VertexInfo i : infos)
				assertVertexInfo(i, "Entrance and Exit Gate", Kind.GATE,
						"enter", "leave", "start", "begin", "entrance", "exit");
			assertEquals(1, infos.size());
			infos = search.getMatchingWaypoints("And");
			for(ZooData.VertexInfo i : infos)
				assertVertexInfo(i, "Entrance and Exit Gate", Kind.GATE,
						"enter", "leave", "start", "begin", "entrance", "exit");
			assertEquals(1, infos.size());

			infos = search.getMatchingWaypoints("Alliga");
			for(ZooData.VertexInfo i : infos)
				assertVertexInfo(i, "Alligators", Kind.EXHIBIT,
						"alligator", "reptile", "gator");
			assertEquals(1, infos.size());
			infos = search.getMatchingWaypoints("lliga");
			for(ZooData.VertexInfo i : infos)
				assertVertexInfo(i, "Alligators", Kind.EXHIBIT,
						"alligator", "reptile", "gator");
			assertEquals(1, infos.size());

			infos = search.getMatchingWaypoints("la");
			for(ZooData.VertexInfo i : infos)
				assertTrue(i.name.equals("Entrance Plaza") || i.name.equals("Gorillas"));
			assertEquals(2, infos.size());

			List<String> words = search.getSuggestedString("Ent");
			for(String word : words)
				assertTrue(word.equals("enter") || word.equals("entrance"));
			assertEquals(2, words.size());

			words = search.getSuggestedString("g");
			assertEquals(4, words.size());
			assertTrue(words.contains("gorillas"));
			assertTrue(words.contains("gorilla"));
			assertTrue(words.contains("gator"));
			assertTrue(words.contains("gate"));

			words = search.getSuggestedString("elephant");
			assertEquals(1, words.size());
			assertEquals("elephant odyssey", words.get(0));

			words = search.getSuggestedString("exit");
			assertEquals(1, words.size());
			assertEquals("exit gate", words.get(0));
		});
	}

	private static void assertVertexInfo(ZooData.VertexInfo info, @Nullable String name, @Nullable Kind kind, String... tags)
	{
		if(name != null)
			assertEquals(name, info.name);
		if(kind != null)
			assertEquals(kind, info.kind);
		for(String tag : tags)
			assertTrue("Does not contain " + tag, info.tags.contains(tag));
	}

	@Test
	public void testSampleDataStreamsAndCollectors() {
		ActivityScenario scene = rule.getScenario();
		scene.moveToState(Lifecycle.State.CREATED);
		scene.onActivity( activity -> {
			ISearch search = DataManager.instantiate(activity, Constants.LEGACY_NODE_FILE_NAME, Constants.LEGACY_EDGE_FILE_NAME, Constants.LEGACY_GRAPH_FILE_NAME, ZooSearch.class, ZooNavigation.class).search;

			//Tests filter and getAllIds()
			Set<String> set = new HashSet<String>();
			set.add("entrance_exit_gate");
			set.add("entrance_plaza");
			set.add("gorillas");
			set.add("gators");
			set.add("lions");
			set.add("elephant_odyssey");
			set.add("arctic_foxes");
			Set<String> ids = search.getAllIds(Kind.GATE, Kind.EXHIBIT, Kind.INTERSECTION);
			for (String s : ids)
				assertTrue(set.contains(s));
			assertEquals(set.size(), ids.size());


			set.remove("entrance_exit_gate");
			set.remove("entrance_plaza");
			ids = search.getAllIds(Kind.EXHIBIT);
			for (String s : ids)
				assertTrue(set.contains(s));
			assertEquals(set.size(), ids.size());

			//Tests getAllPossibleTerms()
			set.clear();
			set.add("entrance");
			set.add("and");
			set.add("exit");
			set.add("gate");
			set.add("enter");
			set.add("leave");
			set.add("start");
			set.add("begin");
			ids = search.getAllPossibleTerms(Kind.GATE);
			for (String s : ids)
				assertTrue(set.contains(s));
			assertEquals(set.size(), ids.size());

			//Tests getAllNames()
			set.clear();
			set.add("Gorillas");
			set.add("Alligators");
			set.add("Lions");
			set.add("Elephant Odyssey");
			set.add("Arctic Foxes");
			ids = search.getAllNames(Kind.EXHIBIT);
			for (String s : ids)
				assertTrue(set.contains(s));
			assertEquals(set.size(), ids.size());
		});
	}
}
