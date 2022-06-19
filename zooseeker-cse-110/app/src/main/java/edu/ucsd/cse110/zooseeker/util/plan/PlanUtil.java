package edu.ucsd.cse110.zooseeker.util.plan;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.tuple.Triple;
import org.jgrapht.GraphPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import edu.ucsd.cse110.zooseeker.data.DataManager;
import edu.ucsd.cse110.zooseeker.data.ZooData;
import edu.ucsd.cse110.zooseeker.data.graph.IdentifiedWeightedEdge;
import edu.ucsd.cse110.zooseeker.event.PlanUpdateListener;

public class PlanUtil {

	public static Set<String> exhibitsToGroups(DataManager dataManager, Set<String> exhibits)
	{
		return exhibits.stream().map(exhibit -> {
			ZooData.VertexInfo info = dataManager.vertexInfoMap.get(exhibit);
			if(info.group_id != null)
				info = dataManager.vertexInfoMap.get(info.group_id);
			return info.id;
		}).collect(Collectors.toSet());
	}

	public static String exhibitToGroup(DataManager dataManager, String exhibit)
	{
		ZooData.VertexInfo info = dataManager.vertexInfoMap.get(exhibit);
		if(info.group_id != null)
			info = dataManager.vertexInfoMap.get(info.group_id);
		return info.id;
	}


	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final List<PlanUpdateListener> listeners = new ArrayList<>();

    public static void registerPlanUpdateListener(DataManager dataManager, PlanUpdateListener listener)
    {
        if(!listeners.contains(listener)) {
            listeners.add(listener);
        }
		Future<Void> future = executor.submit(() -> {
			PlanDatabaseDao dao = PlanDatabase.getInstance(null).getDao();
			listener.onPlanChanged(Collections.unmodifiableList(getPlanFromDao(dataManager, dao)));
			return null;
		});
    }
    public static void unregisterPlanUpdateListener(PlanUpdateListener listener)
    {
        if(listeners.contains(listener))
            listeners.remove(listener);
    }
    public static void updateListeners(DataManager dataManager)
    {
		executor.execute(() -> {
			PlanDatabaseDao dao = PlanDatabase.getInstance(null).getDao();
			updateListeners(Collections.unmodifiableList(getPlanFromDao(dataManager, dao)));
		});
    }
    private static void updateListeners(List<ZooData.VertexInfo> list)
    {
        for(PlanUpdateListener listener : listeners)
            listener.onPlanChanged(list);
    }
	public static void updateThisListener(Context context, DataManager dataManager, PlanUpdateListener listener)
	{
		executor.execute(() -> {
			PlanDatabaseDao dao = PlanDatabase.getInstance(context).getDao();
			listener.onPlanChanged(Collections.unmodifiableList(getPlanFromDao(dataManager, dao)));
		});
	}

    // add exhibit to plan by ID
    public static void add(DataManager dataManager, String id) {
		executor.execute(() -> {
			PlanDatabaseDao dao = PlanDatabase.getInstance(null).getDao();
			PlanItem exists = dao.getPlanItem(id);
			if(exists == null)
				dao.addPlanItem(new PlanItem(id));
			updateListeners(getPlanFromDao(dataManager, dao));
		});
    }

	@NonNull
	private static List<ZooData.VertexInfo> getPlanFromDao(DataManager dataManager, PlanDatabaseDao dao) {
		return dao.getAllPlanItem().stream()
				.map(item -> dataManager.vertexInfoMap.get(item.exhibitId))
				.collect(Collectors.toList());
	}

	// remove exhibit from plan by ID
    public static void remove(DataManager dataManager, String id){
		executor.execute(() -> {
			PlanDatabaseDao dao = PlanDatabase.getInstance(null).getDao();
			PlanItem toRemove = dao.getPlanItem(id);
			if(toRemove != null)
				dao.removePlanItem(toRemove);

			updateListeners(getPlanFromDao(dataManager, dao));
		});
    }

    // removes all exhibits from the list
    public static void clear(DataManager dataManager){
		executor.execute(() -> {
			PlanDatabaseDao dao = PlanDatabase.getInstance(null).getDao();
			dao.clearPlanItems();

			updateListeners(getPlanFromDao(dataManager, dao));
		});
    }

    // returns the size of list
    public static int list_num(){
		Future<Integer> future = executor.submit(() -> {
			PlanDatabaseDao dao = PlanDatabase.getInstance(null).getDao();
			return dao.planItemsCount();
		});
		while(!future.isDone());

		try {
			return future.get();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return -1;
	}


	public static class PathFormatter {

		public static String lastTarget = null;	// store the last visited target node name to fix double direction bug

		public static List<String> getCumulativeDistance(List<GraphPath<String, IdentifiedWeightedEdge>> paths, int startIndex)
		{
			List<String> ret = new ArrayList<String>();

			double distance = 0;
			for(int i = startIndex; i < paths.size(); i++)
			{
				distance += paths.get(i).getWeight();
				ret.add(String.format("%dft", (int)distance));
			}
			return ret;
		}

		public static List<String> getVertexIdOrder(List<GraphPath<String, IdentifiedWeightedEdge>> paths, int startIndex)
		{
			List<String> ret = new ArrayList<String>();

			for(int i = startIndex; i < paths.size(); i++){
				ret.add(paths.get(i).getEndVertex());
			}
			return ret;
		}

		public static List<String> getVertexIdOrder(List<GraphPath<String, IdentifiedWeightedEdge>> paths, int startIndex, DataManager dataManager)
		{
			List<String> ret = new ArrayList<String>();

			for(int i = startIndex; i < paths.size(); i++){
				ret.add(dataManager.vertexInfoMap.get(paths.get(i).getEndVertex()).name);
			}
			return ret;
		}

		public static List<String> getEndEdgeIdOrder(List<GraphPath<String, IdentifiedWeightedEdge>> paths, int startIndex, DataManager dataManager) {
			List<String> ret = new ArrayList<String>();

			for(int i = startIndex; i < paths.size(); i++){
				ret.add(dataManager.edgeInfoMap.get(paths.get(i).getEdgeList().get(paths.get(i).getEdgeList().size() - 1).getId()).street);
			}
			return ret;

		}

		public static List<Triple<String, String, String>> exhibitsDistanceFormat(List<String> vertexOrder, List<String> distanceOrder, List<String> endEdgeOrder) {
			Triple<String, String, String> info;
			List<Triple<String, String, String>> ret = new ArrayList<Triple<String, String, String>>();
			for (int i = 0; i < vertexOrder.size(); i++) {
				info = Triple.of(vertexOrder.get(i), distanceOrder == null ? "n/a" : distanceOrder.get(i), endEdgeOrder.get(i));
				ret.add(info);
			}
			return ret;
		}

		public static List<List<String>> getGroupedExhibits(List<GraphPath<String, IdentifiedWeightedEdge>> paths, int startIndex, List<String> plan, DataManager dataManager)
		{
			List<List<String>> ret = new ArrayList<>();
			for(int i = startIndex; i < paths.size(); i++) {
				Set<ZooData.VertexInfo> match = dataManager.groupInfoMap.get(paths.get(i).getEndVertex());
				if(match == null)
					ret.add(null);
				else
				{
					Set<String> matchString = match.stream().map(info -> info.id).collect(Collectors.toSet());
					ret.add(plan.stream().filter(id -> matchString.contains(id)).map(id -> dataManager.vertexInfoMap.get(id).name)
							.collect(Collectors.toList()));
				}
			}
			return ret;
		}


		public static List<String> pathToTextDetailed(GraphPath<String, IdentifiedWeightedEdge> path, DataManager dataManager)
		{
			List<String> result = new ArrayList<String>();
			
			String lastTarget = dataManager.vertexInfoMap.get(path.getStartVertex()).name;
			String source, target = "";

			int i = 1;

			for (IdentifiedWeightedEdge e : path.getEdgeList()) {
				source = dataManager.vertexInfoMap.get(dataManager.graph.getEdgeSource(e).toString()).name;
				target = dataManager.vertexInfoMap.get(dataManager.graph.getEdgeTarget(e).toString()).name;
				if (source != lastTarget) {
					target = source;
					source = lastTarget;
				}
				result.add (String.format("(%d) Walk %.0f ft along %s from '%s' to '%s'.",
											i,
											dataManager.graph.getEdgeWeight(e),
											dataManager.edgeInfoMap.get(e.getId()).street,
											source,
											target));
				i++;
				lastTarget = target;		// update the last target node name
			}
			return result;
		}

		public static List<String> pathToTextBrief(GraphPath<String, IdentifiedWeightedEdge> path, DataManager dataManager)
		{
			List<String> result = new ArrayList<String>();

			String lastTarget = dataManager.vertexInfoMap.get(path.getStartVertex()).name;
			String source = "", target = "", savedSrc = null, currStreet = "", savedStreet = "";

			double currWeight = 0, accumWeight = 0;

			int i = 1;
			int index = 0;

			List<IdentifiedWeightedEdge> edgeList = path.getEdgeList();
			IdentifiedWeightedEdge e;

			while (index < edgeList.size()) {
				e = edgeList.get(index);
				source = dataManager.vertexInfoMap.get(dataManager.graph.getEdgeSource(e).toString()).name;
				target = dataManager.vertexInfoMap.get(dataManager.graph.getEdgeTarget(e).toString()).name;
				currStreet = dataManager.edgeInfoMap.get(e.getId()).street;
				currWeight = dataManager.graph.getEdgeWeight(e);


				if (source != lastTarget) {
					target = source;
					source = lastTarget;
				}

				if (savedSrc == null) {		// new edge of this new sequence
					savedSrc = source;
					savedStreet = currStreet;
					accumWeight = currWeight;
					index++;
					lastTarget = target;		// update the last target node name
				}
				else {						// has information in savedSrc, savedStreet & accumWeight, last edge in the sequence
					if (currStreet.equals(savedStreet)) {	// new edge also in the sequence
						accumWeight += currWeight;
						index++;
						lastTarget = target;		// update the last target node name
					}
					else {
						result.add(String.format("(%d) Walk %.0f ft along %s from '%s' to '%s'.",
													i,
													accumWeight,
													savedStreet,
													savedSrc,
													source));
						i++;
						savedSrc = null;
						accumWeight = 0;
						savedStreet = "";
					}
				}
			}


			// print out the last edge
			result.add(String.format("(%d) Walk %.0f ft along %s from '%s' to '%s'.",
					i,
					accumWeight,
					savedStreet,
					savedSrc,
					target));
			i++;


			return result;
		}

		public static List<String> pathToTextDetailed(GraphPath<String, IdentifiedWeightedEdge> path, DataManager dataManager, @Nullable List<String> grouped)
		{
			List<String> ret = pathToTextDetailed(path, dataManager);
			if(grouped != null) {
				String last = ret.get(ret.size() - 1);
				ret.set(ret.size() - 1, last.substring(0, last.length() - 1) + pathToTextGroupExtras(grouped));
			}
			return ret;
		}
		public static List<String> pathToTextBrief(GraphPath<String, IdentifiedWeightedEdge> path, DataManager dataManager, @Nullable List<String> grouped)
		{
			List<String> ret = pathToTextBrief(path, dataManager);
			if(grouped != null) {
				String last = ret.get(ret.size()-1);
				ret.set(ret.size() - 1, last.substring(0, last.length() - 1) + pathToTextGroupExtras(grouped));
			}
			return ret;
		}

		private static String pathToTextGroupExtras(List<String> groupedNames)
		{
			String commasOnly = String.format(" where you will find %s", groupedNames.stream().reduce((total, item) -> total + ", " + item).get());
			/*
			int ind = commasOnly.lastIndexOf(", ");
			if(ind != -1) {
				boolean hasOnlyTwoItems = ind == commasOnly.indexOf(", ");
				return commasOnly.substring(0, ind + 2 - (hasOnlyTwoItems ? 2 : 0)) + "and " + commasOnly.substring(ind + 2 - (hasOnlyTwoItems ? 1 : 0));
			}
			*/
			return commasOnly;
		}
	}
}
