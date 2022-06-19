package edu.ucsd.cse110.zooseeker.data;

import android.content.Context;

import androidx.annotation.Nullable;

import org.antlr.v4.runtime.misc.Array2DHashSet;
import org.jgrapht.Graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.ucsd.cse110.zooseeker.data.graph.INavigation;
import edu.ucsd.cse110.zooseeker.data.graph.IdentifiedWeightedEdge;
import edu.ucsd.cse110.zooseeker.data.graph.ZooNavigation;
import edu.ucsd.cse110.zooseeker.data.search.ISearch;
import edu.ucsd.cse110.zooseeker.data.search.ZooSearch;
import edu.ucsd.cse110.zooseeker.util.Constants;

public class DataManager {

	private static DataManager instance = null;

	public Map<String, Set<ZooData.VertexInfo>> groupInfoMap;
	public Map<String, ZooData.VertexInfo> vertexInfoMap;
	public Map<String, ZooData.EdgeInfo> edgeInfoMap;
	public Graph<String, IdentifiedWeightedEdge> graph;

	public ISearch search = null;
	public INavigation navigation = null;

	@Nullable
	public static DataManager getInstance()
	{
		return instance;
	}

	public static DataManager getInstance(Context context)
	{
		if(instance == null)
			instance = DataManager.instantiate(context, Constants.NODE_FILE_NAME, Constants.EDGE_FILE_NAME, Constants.GRAPH_FILE_NAME, ZooSearch.class, ZooNavigation.class);
		return instance;
	}


	public static DataManager instantiate(Context context, String vertexInfoPath, String edgeInfoPath, String graphPath, Class<? extends ISearch> searchClazz, Class<? extends INavigation> navigationClazz)
	{
		try {
			return new DataManager(context, vertexInfoPath, edgeInfoPath, graphPath, searchClazz, navigationClazz);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}

	private DataManager(Context context, String vertexInfoPath, String edgeInfoPath, String graphPath, Class<? extends ISearch> searchClazz, Class<? extends INavigation> navigationClazz) throws IllegalAccessException, InstantiationException {
		vertexInfoMap = ZooData.loadVertexInfoJSON(context, vertexInfoPath);
		edgeInfoMap = ZooData.loadEdgeInfoJSON(context, edgeInfoPath);
		graph = ZooData.loadZooGraphJSON(context, graphPath);

		search = searchClazz.newInstance().reloadData(vertexInfoMap);
		navigation = navigationClazz.newInstance().reloadData(graph);

		groupInfoMap = new HashMap<>();
		for(ZooData.VertexInfo info : search.getByType(ZooData.VertexInfo.Kind.EXHIBIT))
		{
			if(info.group_id == null)
				continue;
			String key = info.group_id;
			if(!groupInfoMap.containsKey(key))
				groupInfoMap.put(key, new HashSet<>());
			groupInfoMap.get(key).add(info);
		}
	}

}
