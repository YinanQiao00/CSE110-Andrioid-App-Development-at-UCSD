package edu.ucsd.cse110.zooseeker.data.search;

import android.content.Context;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.ucsd.cse110.zooseeker.data.ZooData;

public class ZooSearch extends ISearch {

	private static final Collector<List<String>,Set<String>,Set<String>> listCollector = Collector.of(HashSet::new,(list, data) -> {
			list.addAll(data);
	},(a,b) -> {
		a.addAll(b);
		return a;
	});

	private Map<String, ZooData.VertexInfo> rawData;
	private Map<String, List<ZooData.VertexInfo>> groupedByTag;
	private Map<ZooData.VertexInfo.Kind, List<ZooData.VertexInfo>> groupedByType;

	private SuffixTree<ZooData.VertexInfo> waypointsNameTree;
	private SuffixTree<ZooData.VertexInfo> waypointsTagTree;
	private MultiWayTree<Integer> wordSuggestionTree;
	private MultiWayTree<Integer> termSuggestionTree;


	@Deprecated
	public static ZooSearch createInstance(Context context, String path) {
		ZooSearch search = new ZooSearch();
		search.reloadData(ZooData.loadVertexInfoJSON(context, path));
		return search;
	}

	/*
	@Deprecated
	private ZooSearch(Map<String, ZooData.VertexInfo> data) { reloadData(data); }
	*/

	public ISearch reloadData(Map<String, ZooData.VertexInfo> data) {
		rawData = data;
		groupedByTag = new HashMap<String, List<ZooData.VertexInfo>>();
		groupedByType = new HashMap<ZooData.VertexInfo.Kind, List<ZooData.VertexInfo>>();

		waypointsNameTree = new SuffixTree<ZooData.VertexInfo>(ZooData.VertexInfo.class);
		waypointsTagTree = new SuffixTree<ZooData.VertexInfo>(ZooData.VertexInfo.class);
		wordSuggestionTree = new MultiWayTree<Integer>(Integer.class);
		termSuggestionTree = new MultiWayTree<Integer>(Integer.class);

		Map<String,List<ZooData.VertexInfo>> nameMap = new HashMap<String,List<ZooData.VertexInfo>>();
		for(String id : rawData.keySet())
		{
			ZooData.VertexInfo info = rawData.get(id);

			for(String tag : info.tags)
				groupedByTag.compute(tag, (k,v) -> {
					if(v==null)
						v = new ArrayList<ZooData.VertexInfo>();
					v.add(info);
					return v;
				});

			groupedByType.compute(info.kind, (k,v) -> {
				if(v==null)
					v = new ArrayList<ZooData.VertexInfo>();
				v.add(info);
				return v;
			});

			String name = info.name.toLowerCase();
			waypointsNameTree.insert(name, info);

			for(String term : name.split(" ")) {
				nameMap.compute(term.toLowerCase(), (k,v) -> {
					if(v==null)
						v = new ArrayList<ZooData.VertexInfo>();
					v.add(info);
					return v;
				});
			}

			while(name.length() > 0)
			{
				termSuggestionTree.insertUseId(name);
				if(name.contains(" "))
					name = name.substring(name.indexOf(' ')+1);
				else
					name = "";
			}
		}

		Set<String> used = new HashSet<String>();
		for(String term : nameMap.keySet())
		{
			wordSuggestionTree.insertUseId(term);
			used.add(term);
		}

		for(String tag : groupedByTag.keySet())
		{
			waypointsTagTree.insert(tag, groupedByTag.get(tag));

			if(!used.contains(tag))
				wordSuggestionTree.insertUseId(tag);
		}

		return this;
	}

	@Override
	public List<ZooData.VertexInfo> getMatchingWaypoints(String str) {
		str = str.toLowerCase();

		Set<ZooData.VertexInfo> ret = new HashSet<ZooData.VertexInfo>();
		ret.addAll(waypointsNameTree.getPossibleValues(str));
		ret.addAll(waypointsTagTree.getPossibleValues(str));

		if(ret.isEmpty())
		{
			for(String sub : str.split(" ")) {
				if(sub.trim().length() > 0) {
					ret.addAll(waypointsNameTree.getPossibleValues(sub));
					ret.addAll(waypointsTagTree.getPossibleValues(sub));
				}
			}
		}

		return new ArrayList<ZooData.VertexInfo>(ret);
	}

	@Override
	public List<String> getSuggestedString(String str) {
		str = str.toLowerCase();

		Set<String> ret = new HashSet<String>();
		MultiWayTree<Integer> tree;
		if(wordSuggestionTree.contains(str))
			tree = termSuggestionTree;
		else
			tree = wordSuggestionTree;
		for (Integer i : tree.getPossibleValues(str))
			ret.add(tree.getKey(i).replace("$", ""));

		return new ArrayList<String>(ret);
	}

	@Override
	public Set<String> getAllNames(ZooData.VertexInfo.Kind... kinds) {
		Set<ZooData.VertexInfo.Kind> set = new HashSet<ZooData.VertexInfo.Kind>();
		set.addAll(Arrays.asList(kinds));
		return rawData.values().stream().filter(info -> set.contains(info.kind)).map(info -> info.name).collect(Collectors.toSet());
	}

	@Override
	public Set<String> getAllPossibleTerms(ZooData.VertexInfo.Kind... kinds) {
		Set<ZooData.VertexInfo.Kind> set = new HashSet<ZooData.VertexInfo.Kind>();
		set.addAll(Arrays.asList(kinds));
		Stream<ZooData.VertexInfo> stream = rawData.values().stream().filter(info -> set.contains(info.kind));
		Set<String> retSet = stream.map(info -> {
			List<String> ret = new ArrayList<String>();
			ret.addAll(Arrays.asList(info.name.toLowerCase().split(" ")));
			ret.addAll(info.tags);
			return ret;
		}).collect(listCollector);
		return retSet;
	}

	@Override
	public Set<String> getAllIds(ZooData.VertexInfo.Kind... kinds) {
		Set<ZooData.VertexInfo.Kind> set = new HashSet<ZooData.VertexInfo.Kind>();
		set.addAll(Arrays.asList(kinds));
		return rawData.values().stream().filter(info -> set.contains(info.kind)).map(info -> info.id).collect(Collectors.toSet());
	}

	@Override
	public ZooData.VertexInfo getById(String str) {
		return rawData.get(str);
	}
	@Override
	public List<ZooData.VertexInfo> getByType(ZooData.VertexInfo.Kind kind) { return groupedByType.get(kind); }
	@Override
	public List<ZooData.VertexInfo> getByTag(String str) {
		return groupedByTag.get(str);
	}
}
