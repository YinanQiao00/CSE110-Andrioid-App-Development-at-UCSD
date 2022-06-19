package edu.ucsd.cse110.zooseeker.util.plan;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {PlanItem.class, PathVisitedItem.class}, version = 2, exportSchema = false)
public abstract class PlanDatabase extends RoomDatabase {

	private static PlanDatabase instance = null;

	public abstract PlanDatabaseDao getDao();

	public synchronized static PlanDatabase getInstance(@Nullable Context context) {
		if (instance == null) {
			if(context == null)
				throw new IllegalArgumentException("Context cannot be null when PlanDatabase instance has not been initialized");
			instance = PlanDatabase.makeDatabase(context);
		}
		return instance;
	}

	private static PlanDatabase makeDatabase(Context context) {
		return Room.databaseBuilder(context, PlanDatabase.class, "plan.db").fallbackToDestructiveMigration().build();
	}
}
