package edu.ucsd.cse110.zooseeker;

import static org.junit.Assert.assertNotNull;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.ucsd.cse110.zooseeker.data.DataManager;
import edu.ucsd.cse110.zooseeker.data.test.EmptyActivity;

@RunWith(AndroidJUnit4.class)
public class DataManagerTester {

	@Rule
	public ActivityScenarioRule rule = new ActivityScenarioRule<>(EmptyActivity.class);

	@Test
	public void testDataValid()
	{
		ActivityScenario scene = rule.getScenario();
		scene.moveToState(Lifecycle.State.CREATED);
		scene.onActivity( activity -> {
			assertNotNull(DataManager.getInstance(activity).vertexInfoMap);
			assertNotNull(DataManager.getInstance(activity).edgeInfoMap);
			assertNotNull(DataManager.getInstance(activity).graph);
			assertNotNull(DataManager.getInstance(activity).search);
			assertNotNull(DataManager.getInstance(activity).navigation);
		});
	}

}
