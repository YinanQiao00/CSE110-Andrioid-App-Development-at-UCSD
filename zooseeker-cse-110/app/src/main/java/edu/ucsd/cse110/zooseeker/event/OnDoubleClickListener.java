package edu.ucsd.cse110.zooseeker.event;

import android.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class OnDoubleClickListener implements View.OnClickListener {

	private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	long interval;
	private ScheduledFuture<Void> task;

	public OnDoubleClickListener(long doubleClickInterval)
	{
		interval = doubleClickInterval;
		task = null;
	}

	public void setInterval(long doubleClickInterval) { interval = doubleClickInterval; }
	public long getInterval() { return interval; }

	@Override
	public final void onClick(View view) {
		if(task == null || task.isDone())
		{
			task = executor.schedule(() -> {
				onSingleClick(view);
				return null;
			}, interval, TimeUnit.MILLISECONDS);
		}
		else
		{
			task.cancel(true);
			task = null;
			onDoubleClick(view);
		}
	}

	public abstract void onSingleClick(View view);
	public abstract void onDoubleClick(View view);
}
