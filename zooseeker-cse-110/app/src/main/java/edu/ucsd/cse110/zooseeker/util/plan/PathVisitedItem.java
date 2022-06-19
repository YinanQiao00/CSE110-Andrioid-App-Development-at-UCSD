package edu.ucsd.cse110.zooseeker.util.plan;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "visited_exhibits")
public class PathVisitedItem {
	@PrimaryKey(autoGenerate = true)
	public long id;

	@NonNull
	public String exhibitId;
	public int order;

	public PathVisitedItem(@NonNull String exhibitId, int order)
	{
		this.exhibitId = exhibitId;
		this.order = order;
	}

	@Override
	public String toString() {
		return String.format("PathVisitedItem{id:%d, exhibitId:%s, order:%d}", id, exhibitId, order);
	}
}
