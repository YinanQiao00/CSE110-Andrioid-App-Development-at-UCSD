package edu.ucsd.cse110.zooseeker.data.search;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ucsd.cse110.zooseeker.data.ZooData;

public abstract class ISearch {

	public ISearch() {}

	/**
	 * @param str <b>String</b> with no spaces
	 * @return A list of <b>VertixInfo</b>s whose <i>name</i> or <i>tag</i> contains the substring <i>str</i>.
	 */
	public abstract List<ZooData.VertexInfo> getMatchingWaypoints(String str);

	/**
	 * @param str
	 * @return A list of <b>String</b>s who starts with <i>str</i>. Returns words if <i>str</i> is not a word else returns terms.
	 */
	@Deprecated
	public abstract List<String> getSuggestedString(String str);


	/**
	 *
	 * @param kinds <i>kinds</i> of <b>VertexInfo</b> to consider adding to the list
	 * @return a list of all the names of <b>VertexInfo</b> whose <b>Kind</b> is one of the ones listed in <i>kinds</i>
	 */
	public abstract Set<String> getAllNames(ZooData.VertexInfo.Kind... kinds);
	/**
	 *
	 * @param kinds <i>kinds</i> of <b>VertexInfo</b> to consider adding to the list
	 * @return a list of all the names of <b>VertexInfo</b> whose <b>Kind</b> is one of the ones listed in <i>kinds</i>
	 */
	public abstract Set<String> getAllPossibleTerms(ZooData.VertexInfo.Kind... kinds);
	/**
	 *
	 * @param kinds <i>kinds</i> of <b>VertexInfo</b> to consider adding to the list
	 * @return a list of all the names of <b>VertexInfo</b> whose <b>Kind</b> is one of the ones listed in <i>kinds</i>
	 */
	public abstract Set<String> getAllIds(ZooData.VertexInfo.Kind... kinds);


	public abstract ZooData.VertexInfo getById(String str);
	public abstract List<ZooData.VertexInfo> getByType(ZooData.VertexInfo.Kind kind);
	public abstract List<ZooData.VertexInfo> getByTag(String str);

	public abstract ISearch reloadData(Map<String, ZooData.VertexInfo> data);

}
