package edu.ucsd.cse110.zooseeker.event;

import java.util.List;

import edu.ucsd.cse110.zooseeker.data.ZooData;

public interface PlanUpdateListener {
	/**
	 * This method is called on a change to the plan (addition, deletion, clearing)
	 *
	 * @param newPlan an immutable list of the current exhibits in the plan
	 */
	public void onPlanChanged(List<ZooData.VertexInfo> newPlan);
}
