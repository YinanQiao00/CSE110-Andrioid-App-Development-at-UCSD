package edu.ucsd.cse110.zooseeker.util.plan;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlanDatabaseDao {

	//Access PlanItem entities
	@Insert
	long addPlanItem(PlanItem item);
	@Insert
	List<Long> addAllPlanItem(List<PlanItem> items);
	@Query("SELECT * FROM `planned_exhibits` WHERE `id`=:id")
	public PlanItem getPlanItem(long id);
	@Query("SELECT * FROM `planned_exhibits` WHERE `exhibitId`=:exhibitId")
	public PlanItem getPlanItem(@NonNull String exhibitId);
	@Query("SELECT * FROM `planned_exhibits` ORDER BY `exhibitId`")
	public List<PlanItem> getAllPlanItem();
	@Delete
	public int removePlanItem(PlanItem item);
	@Query("SELECT COUNT(`id`) FROM `planned_exhibits`")
	public int planItemsCount();
	@Query("DELETE FROM `planned_exhibits`")
	public void clearPlanItems();

	//Access PathVisitedItem entities
	@Insert
	long addPathVisitedItem(PathVisitedItem item);
	@Query("SELECT * FROM `visited_exhibits` ORDER BY `exhibitId`")
	public List<PathVisitedItem> getAllPathVisitedItem();
	@Query("SELECT COUNT(`id`) FROM `visited_exhibits`")
	public int pathVisitedItemsCount();
	@Query("DELETE FROM `visited_exhibits`")
	public void clearPathVisitedItems();

}
