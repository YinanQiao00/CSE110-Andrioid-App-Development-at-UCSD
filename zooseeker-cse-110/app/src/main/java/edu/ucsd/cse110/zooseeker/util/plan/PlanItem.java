package edu.ucsd.cse110.zooseeker.util.plan;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "planned_exhibits")
public class PlanItem {
	@PrimaryKey(autoGenerate = true)
	public long id;

	@NonNull
	public String exhibitId;

	public PlanItem(@NonNull String exhibitId)
	{
		this.exhibitId = exhibitId;
	}

	@Override
	public String toString() {
		return String.format("PlanItem{id:%d, exhibitId:%s}", id, exhibitId);
	}
}
