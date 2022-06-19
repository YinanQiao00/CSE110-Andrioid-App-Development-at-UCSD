package edu.ucsd.cse110.zooseeker.util.plan;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class VisitedUtil {
	private static final ExecutorService executor = Executors.newSingleThreadExecutor();

	public static void add(String id)
	{
		executor.execute(() -> {
			PlanDatabaseDao dao = PlanDatabase.getInstance(null).getDao();
			dao.addPathVisitedItem(new PathVisitedItem(id, dao.pathVisitedItemsCount()));
		});
	}
	public static List<String> getAll()
	{
		Future<List<String>> future = executor.submit(() -> {
			PlanDatabaseDao dao = PlanDatabase.getInstance(null).getDao();
			return dao.getAllPathVisitedItem().stream().sorted(Comparator.comparingInt(item -> item.order))
					.map(item -> item.exhibitId).collect(Collectors.toList());
		});
		while(!future.isDone());
		List<String> ret = null;
		try {
			ret = future.get();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return ret == null ? new ArrayList<>() : ret;
	}
	public static void clear()
	{
		Future<Void> future = executor.submit(() -> {
			PlanDatabaseDao dao = PlanDatabase.getInstance(null).getDao();
			dao.clearPathVisitedItems();
			return null;
		});
		while(!future.isDone());
	}

}
