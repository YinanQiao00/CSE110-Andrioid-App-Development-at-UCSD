package edu.ucsd.cse110.zooseeker.util;

import com.google.android.gms.maps.model.LatLng;

import org.jgrapht.alg.interfaces.ManyToManyShortestPathsAlgorithm;
import org.jgrapht.alg.shortestpath.CHManyToManyShortestPaths;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.ucsd.cse110.zooseeker.data.DataManager;
import edu.ucsd.cse110.zooseeker.data.ZooData;

public class LocationUtil {

	//Distance in ft near a vertex to be considered at that vertex
	private static final int MINIMUM_DISTANCE_OF_WITHIN = 20;

	private static final double NW_LAT = 32.750121  + 0.00005;
	private static final double NW_LNG = -117.18369 - 0.00005;
	private static final double SE_LAT = 32.721098  - 0.00005;
	private static final double SE_LNG = -117.14936 + 0.00005;

	public static boolean isInZooBounds(LatLng loc)
	{
		return loc.latitude < NW_LAT && loc.latitude > SE_LAT && loc.longitude > NW_LNG && loc.longitude < SE_LNG;
	}

	public static boolean isAtVertex(LatLng loc, ZooData.VertexInfo vertex)
	{
		return distanceSq(loc, vertex.lat, vertex.lng) < MINIMUM_DISTANCE_OF_WITHIN * MINIMUM_DISTANCE_OF_WITHIN;
	}

	public static Map<ZooData.VertexInfo,Double> getDistanceToVertices(DataManager data, LatLng start, Set<ZooData.VertexInfo> exhibits)
	{
		Map<String,Double> map = getDistanceToVertexIds(data, start, exhibits.stream().map(info -> info.id).collect(Collectors.toSet()));//.keySet().stream().collect(Collectors.toMap(String::toString,data.vertexInfoMap::get));
		return map.keySet().stream().collect(Collectors.toMap(data.vertexInfoMap::get,map::get));
	}

	public static Map<String,Double> getDistanceToVertexIds(DataManager data, LatLng start, Set<String> exhibitIds)
	{
		ManyToManyShortestPathsAlgorithm alg = new CHManyToManyShortestPaths(data.graph);
		ZooData.VertexInfo vertex = getClosestVertex(data, start);
		String source = vertex.id;
		Set<String> sourceS = new HashSet<String>();
		sourceS.add(source);
		ManyToManyShortestPathsAlgorithm.ManyToManyShortestPaths paths = alg.getManyToManyPaths(sourceS, exhibitIds);

		double distReturn = Math.sqrt(distanceSq(start, vertex.lat, vertex.lng));

		return exhibitIds.stream().collect(Collectors.toMap(Function.identity(), (id) -> {return paths.getPath(source,id).getWeight() + distReturn;}));
	}

	public static ZooData.VertexInfo getClosestVertex(DataManager data, LatLng loc)
	{
		double dist = Double.MAX_VALUE;
		ZooData.VertexInfo near = null;
		for(ZooData.VertexInfo vInfo : data.vertexInfoMap.values()) {
			if(vInfo.group_id != null)
				continue;
			double d = distanceSq(loc, vInfo.lat, vInfo.lng);
			if (d < dist)
			{
				dist = d;
				near = vInfo;
			}
		}
		return near;
	}

	private static final double LAT_DEG_TO_FT = 363843.57;
	private static final double LNG_DEG_TO_FT = 307515.50;

	public static double distanceSq(LatLng loc, double lat, double lng)
	{
		return Math.pow((lat-loc.latitude)*LAT_DEG_TO_FT,2) + Math.pow((lng-loc.longitude)*LNG_DEG_TO_FT,2);
	}
	public static double distanceSq(LatLng loc, LatLng loc2)
	{
		return Math.pow((loc2.latitude-loc.latitude)*LAT_DEG_TO_FT,2) + Math.pow((loc2.longitude-loc.longitude)*LNG_DEG_TO_FT,2);
	}

	public static List<LatLng> interpolate(LatLng start, LatLng end, int n)
	{
		List<LatLng> ret = new ArrayList<>();
		double dlat = end.latitude-start.latitude;
		double dlng = end.longitude-start.longitude;
		for(int i = 0; i < n; i++)
			ret.add(new LatLng(start.latitude + dlat*i/(n-1), start.longitude + dlng*i/(n-1)));
		return ret;
	}

}
