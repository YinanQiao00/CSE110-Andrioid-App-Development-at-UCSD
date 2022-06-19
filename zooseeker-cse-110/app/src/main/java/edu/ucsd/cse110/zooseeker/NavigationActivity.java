package edu.ucsd.cse110.zooseeker;

import static edu.ucsd.cse110.zooseeker.util.plan.PlanUtil.PathFormatter.exhibitsDistanceFormat;
import static edu.ucsd.cse110.zooseeker.util.plan.PlanUtil.PathFormatter.getCumulativeDistance;
import static edu.ucsd.cse110.zooseeker.util.plan.PlanUtil.PathFormatter.getEndEdgeIdOrder;
import static edu.ucsd.cse110.zooseeker.util.plan.PlanUtil.PathFormatter.getVertexIdOrder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.lang3.tuple.Triple;
import org.jgrapht.GraphPath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import edu.ucsd.cse110.zooseeker.component.NavigationAdapter;
import edu.ucsd.cse110.zooseeker.component.NavigationPlanAdapter;
import edu.ucsd.cse110.zooseeker.data.DataManager;
import edu.ucsd.cse110.zooseeker.data.ZooData;
import edu.ucsd.cse110.zooseeker.data.graph.IdentifiedWeightedEdge;
import edu.ucsd.cse110.zooseeker.event.PlanUpdateListener;
import edu.ucsd.cse110.zooseeker.event.SettingsUpdateListener;
import edu.ucsd.cse110.zooseeker.model.LocationModel;
import edu.ucsd.cse110.zooseeker.util.Constants;
import edu.ucsd.cse110.zooseeker.util.LocationUtil;
import edu.ucsd.cse110.zooseeker.util.SettingsUtil;
import edu.ucsd.cse110.zooseeker.util.plan.PlanUtil;
import edu.ucsd.cse110.zooseeker.util.plan.VisitedUtil;

public class NavigationActivity extends AppCompatActivity implements PlanUpdateListener, SettingsUpdateListener {
	public static final String SPREF_BACK_INDEX = "back_index";

	DataManager dataManager;

	String closestUserLocation;

	int backIndex = -1;
	List<String> visited;

	List<ZooData.VertexInfo> plan;
	List<GraphPath<String, IdentifiedWeightedEdge>> unvisitedPath;
	List<String> unvisited;
	GraphPath<String, IdentifiedWeightedEdge> currPath;

	LocationModel locationModel;
	LiveData<LatLng> position;

	// for route plan summary
	List<String> vOrder;
	List<String> dOrder;
	List<String> eOrder;
	NavigationAdapter directionsAdapter;
	NavigationPlanAdapter planAdapter;

	String instructionType;

	TextView fromTitle;
	TextView toTitle;
	RecyclerView directions;
	RecyclerView plans;

	Button backBtn;
	Button skipBtn;
	Button endPlanBtn;
	Button mockBtn;

	boolean isInZoo;

	@Override
	public void onPlanChanged(List<ZooData.VertexInfo> newPlan) {
		plan = newPlan;
		if(visited.isEmpty())
			getInitRoute();
		else
			rerouteFutureFromUserLocation();
		recomputePathFromUserLocation();

		runOnUiThread(() -> {
			updateDisplayStates();
		});
	}
	@Override
	public void onSettingsUpdate(String key, String value) {
		if(key.equals(Constants.INSTRUCTION_TYPE)) {
			instructionType = value;

			runOnUiThread(() -> {
				updateDisplayStates();
			});
		}
	}

	@Override
	public void onBackPressed() {
		if(!isEnd() || !closestUserLocation.equals(dataManager.search.getByType(ZooData.VertexInfo.Kind.GATE).get(0).id)) {
			new AlertDialog.Builder(this).setTitle("Warning").setMessage("You will lose your navigation progress")
					.setPositiveButton("Ok", (dialog, id) -> {
						dialog.dismiss();
						VisitedUtil.clear();
						changeBackIndex(this, -1);
						finish();
					}).setNegativeButton("Cancel", (dialog, id) -> {
						dialog.cancel();
					}).setCancelable(true).create().show();
		}
		else
		{
			VisitedUtil.clear();
			changeBackIndex(this, -1);
			finish();
		}
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		PlanUtil.unregisterPlanUpdateListener(this);
		SettingsUtil.unregisterSettingsUpdateListener(this);

		if(future != null)
			future.cancel(true);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation);

		initializeData();

		findLayoutComponents();

		setupRecycler();		// for route plan summary

		locationModel = new ViewModelProvider(this).get(LocationModel.class);
		position = locationModel.getPositionLiveData();
		position.observe(this, latLng -> {
			Log.i("LocationModel", String.format("Location set to %s", latLng));
			isInZoo = LocationUtil.isInZooBounds(latLng);
			ZooData.VertexInfo info = LocationUtil.getClosestVertex(dataManager, latLng);
			if(info.id != closestUserLocation) {
				closestUserLocation = info.id;

				if(unvisited != null)
				{
					checkDeviationFromPlan();
					recomputePathFromUserLocation();
					updateDisplayStates();
				}
			}
		});

		setEventListeners(this);

		setupLocationTracking(!this.getIntent().getExtras().getBoolean("ignore_gps"));
	}

	private void initializeData() {
		dataManager = DataManager.getInstance(this);
		this.backIndex = loadBackIndex(this);
		this.visited = VisitedUtil.getAll();
		if(backIndex > visited.size()-1)
			backIndex = visited.size()-1;
		instructionType = SettingsUtil.get(this, Constants.INSTRUCTION_TYPE, Constants.DIRECTIONS_BRIEF);
		if(instructionType == null) {
			instructionType = Constants.DIRECTIONS_BRIEF;
			SettingsUtil.set(this, Constants.INSTRUCTION_TYPE, instructionType);
		}
	}
	private void findLayoutComponents() {
		fromTitle = findViewById(R.id.fromTitle);
		toTitle = findViewById(R.id.toTitle);
		directions = findViewById(R.id.pathDirections);
		plans = findViewById(R.id.planRecycler);
		backBtn = findViewById(R.id.backBtn);
		skipBtn = findViewById(R.id.skipBtn);
		endPlanBtn = findViewById(R.id.endPlanBtn);
		mockBtn = findViewById(R.id.mockBtn);
	}
	private void setupRecycler(){
		vOrder = new ArrayList<String>();
		eOrder = new ArrayList<String>();
		dOrder = new ArrayList<String>();

		directionsAdapter = new NavigationAdapter();
		directionsAdapter.setHasStableIds(true);
		directions.setLayoutManager(new LinearLayoutManager(this));
		directions.setAdapter(directionsAdapter);

		planAdapter = new NavigationPlanAdapter();
		planAdapter.setHasStableIds(true);
		plans.setLayoutManager(new LinearLayoutManager(this));
		plans.setAdapter(planAdapter);
	}
	private void setEventListeners(Context context) {
		PlanUtil.registerPlanUpdateListener(dataManager, this);
		SettingsUtil.registerSettingsUpdateListener(this);

		backBtn.setOnClickListener((view) -> {
			onBackward();
		});
		skipBtn.setOnClickListener((view) -> {
			if(isBacktracking()) {
				onReturnToPresent();
				recomputePathFromUserLocation();
				updateDisplayStates();
			}
			else
				onSkip();
		});
		endPlanBtn.setOnClickListener((view) -> {
			onBackPressed();
		});

		mockBtn.setOnClickListener(mockBtnClickListener());
	}
	private void setupLocationTracking(boolean useGps) {
		if(useGps)
		{
			var locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			var provider = LocationManager.GPS_PROVIDER;
			locationModel.setLocationProvider(locationManager, provider);
		}
		else
		{
			ViewGroup.LayoutParams params = mockBtn.getLayoutParams();
			params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
			mockBtn.setLayoutParams(params);
			ZooData.VertexInfo gate = dataManager.search.getByType(ZooData.VertexInfo.Kind.GATE).get(0);
			locationModel.mockLocation(new LatLng(gate.lat,gate.lng));
    	}
	}

	private void getInitRoute() {
		visited = new ArrayList<>();
		ZooData.VertexInfo gate = dataManager.search.getByType(ZooData.VertexInfo.Kind.GATE).get(0);
		getFullRouteFrom(gate);
	}
	private void getFullRouteFrom(ZooData.VertexInfo from) {
		unvisitedPath = dataManager.navigation.findShortestPaths(
				from.id,
				PlanUtil.exhibitsToGroups(dataManager, plan.stream().map(item -> item.id).collect(Collectors.toSet()))
		);
		unvisited = unvisitedPath.stream().map(path -> path.getEndVertex()).collect(Collectors.toList());
	}
	private void rerouteFutureFromUserLocation() {
		unvisitedPath = dataManager.navigation.findShortestPaths(
				closestUserLocation,
				PlanUtil.exhibitsToGroups(dataManager,
								plan.stream().map(item -> item.id).collect(Collectors.toSet()))
						.stream().filter(item -> !visited.contains(item) && !item.equals(closestUserLocation))
						.collect(Collectors.toSet())
		);
		unvisited = unvisitedPath.stream().map(path -> path.getEndVertex()).collect(Collectors.toList());
	}

	private void recomputePathFromUserLocation() {
		currPath = dataManager.navigation
				.findShortestPath(closestUserLocation, isBacktracking() ? visited.get(backIndex)
						: isEnd() ? dataManager.search.getByType(ZooData.VertexInfo.Kind.GATE).get(0).id : unvisited.get(0));
	}
	private void checkDeviationFromPlan() {
		int ind = unvisited.indexOf(closestUserLocation);

		if (!isBacktracking()) {
			if (ind == 0)
			{
				moveFromUnvisitedToVisited(0);
				rerouteFutureFromUserLocation();
			}
			else if (ind != -1)
			{
				{
					new AlertDialog.Builder(this).setTitle("Notification")
							.setMessage(String.format("You have deviated from the plan" +
											" and are already at %s. Do you wish to reroute?"
									, dataManager.vertexInfoMap.get(closestUserLocation).name))
							.setPositiveButton("Ok", (dialog, id) -> {
								moveFromUnvisitedToVisited(ind);
								rerouteFutureFromUserLocation();
								recomputePathFromUserLocation();
								updateDisplayStates();
							}).setNegativeButton("Cancel", (dialog, id) -> {
								dialog.cancel();
							}).setCancelable(true).create().show();
				}
			}
			else if (!unvisited.isEmpty() && currPath != null)
			{
				GraphPath<String, IdentifiedWeightedEdge> shortestPath = dataManager.navigation.findShortestPathFromMany(closestUserLocation, PlanUtil.exhibitsToGroups(dataManager, new HashSet(unvisited)));
				if (!shortestPath.getEndVertex().equals(unvisited.get(0)))
				{
					{
						new AlertDialog.Builder(this).setTitle("Notification")
								.setMessage(String.format("You have deviated from the plan" +
												" and are closer to %s than the intended next location %s." +
												" Do you wish to reroute?"
										, dataManager.vertexInfoMap.get(shortestPath.getEndVertex()).name
										, dataManager.vertexInfoMap.get(currPath.getEndVertex()).name))
								.setPositiveButton("Ok", (dialog, id) -> {
									rerouteFutureFromUserLocation();
									recomputePathFromUserLocation();
									updateDisplayStates();
								}).setNegativeButton("Cancel", (dialog, id) -> {
									dialog.cancel();
								}).setCancelable(true).create().show();
					}
				}
			}
		}
	}
	private void updateDisplayStates() {
		if(isBacktracking())
			skipBtn.setText("Return");
		else
			skipBtn.setText("Skip");

		boolean canNextOrSkip = !isEnd() || isBacktracking();
		skipBtn.setEnabled(canNextOrSkip);
		skipBtn.setBackgroundDrawable(canNextOrSkip ? getResources().getDrawable(R.drawable.button_style) :
				getResources().getDrawable(R.drawable.button_style_disabled));

		backBtn.setEnabled(canBackward());
		backBtn.setBackgroundDrawable(canBackward() ? getResources().getDrawable(R.drawable.button_style_add) :
				getResources().getDrawable(R.drawable.button_style_disabled));

		if(isInZoo) {
			if(currPath != null) {
				fromTitle.setText(dataManager.vertexInfoMap.get(currPath.getStartVertex()).name);
				toTitle.setText(dataManager.vertexInfoMap.get(currPath.getEndVertex()).name);
				if (currPath.getWeight() > 0) {
					Set<ZooData.VertexInfo> match = dataManager.groupInfoMap.get(currPath.getEndVertex());
					List<String> grouped;
					if(match == null)
						grouped = null;
					else
					{
						Set<String> matchString = match.stream().map(info -> info.id).collect(Collectors.toSet());
						grouped = plan.stream().filter(info -> matchString.contains(info.id))
								.map(info -> info.name)
								.collect(Collectors.toList());
					}
					List<String> dirs;
					if (instructionType.equals(Constants.DIRECTIONS_BRIEF))
						dirs = PlanUtil.PathFormatter.pathToTextBrief(currPath, dataManager, grouped);
					else
						dirs = PlanUtil.PathFormatter.pathToTextDetailed(currPath, dataManager, grouped);

					// directions change to recyclerview
					directionsAdapter.setStrItems(dirs);
				} else {
					// directions change to recyclerview
					List<String> tempListStr = new ArrayList<String>();
					tempListStr.add("You are at the location");
					directionsAdapter.setStrItems(tempListStr);
				}
			}


			// update route plan summary
			planAdapter = new NavigationPlanAdapter();
			planAdapter.setHasStableIds(true);
			plans.setLayoutManager(new LinearLayoutManager(this));
			plans.setAdapter(planAdapter);

			if(!isEnd())
			{
				List<GraphPath<String, IdentifiedWeightedEdge>> tempPath = new ArrayList<>(unvisitedPath);
				if(!isBacktracking()) {
					tempPath.remove(0);
					if(currPath.getEdgeList().size() > 0)
						tempPath.add(0, currPath);
				}
				vOrder = getVertexIdOrder(tempPath, 0, dataManager);
				eOrder = getEndEdgeIdOrder(tempPath, 0, dataManager);
				dOrder = isBacktracking() ? null : getCumulativeDistance(tempPath, 0);
				List<List<String>> groupedNames =
						PlanUtil.PathFormatter.getGroupedExhibits(tempPath,0,plan.stream().map(info -> info.id)
								.collect(Collectors.toList()), dataManager);
				planAdapter.setStrItems(exhibitsDistanceFormat(vOrder, dOrder, eOrder), groupedNames);	// where we update route plan summary
			}
			else
				planAdapter.setStrItems(new ArrayList<Triple<String, String, String>>(), null);
		}
		else
		{
			fromTitle.setText("N/A");
			toTitle.setText("N/A");
			List<String> dirs = new ArrayList<String>();
			dirs.add("You have left the confines of the zoo, return within the zoo for the app to display meaningful information");
			directionsAdapter.setStrItems(dirs);
			planAdapter.setStrItems(new ArrayList<Triple<String, String, String>>(),null);
		}
	}

	private boolean isBacktracking() { return backIndex != -1; }
	private boolean canBackward() {
		//Has places to go back to && is not currently checking out the earliest place
		return visited.size() > 0 && backIndex != 0;
	}
	private void onBackward() {
		if(backIndex == -1)
			changeBackIndex(this, visited.size()-1);
		else
			changeBackIndex(this, backIndex-1);
		recomputePathFromUserLocation();
		updateDisplayStates();
	}
	private boolean isEnd() {
		return unvisited == null /*ShortCircuit Error Catch*/ || unvisited.size() == 0;
	}
	private void onSkip() {
		String group = unvisited.get(0);
		Set<String> exhibits = plan.stream().map(item -> item.id).filter(id -> {
			ZooData.VertexInfo info = dataManager.vertexInfoMap.get(id);
			return info.id.equals(group) || group.equals(info.group_id);
		}).collect(Collectors.toSet());
		for(String exhibit : exhibits)
			PlanUtil.remove(dataManager, exhibit);
	}

	private void moveFromUnvisitedToVisited(int ind) {
		Log.i("Unvisited", unvisited.toString());
		String exhibit = unvisited.remove(ind);
		Log.i("Moved", exhibit + " " + ind);
		visited.add(exhibit);
		VisitedUtil.add(exhibit);
	}

	private void onReturnToPresent()
	{
		changeBackIndex(this, -1);
	}

	private void changeBackIndex(Context context, int index) {
		this.backIndex = index;
		saveBackIndex(context, backIndex);
	}
	public static int loadBackIndex(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("shared preferences", context.MODE_PRIVATE);
		return sharedPreferences.getInt(SPREF_BACK_INDEX,-1);
	}
	public static void saveBackIndex(Context context, int index) {
		SharedPreferences sharedPreferences = context.getSharedPreferences("shared preferences", context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(SPREF_BACK_INDEX, index);
		editor.commit();
	}

	public void onGearClicked(View view) {
		Intent intent = new Intent(this,SettingActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	public static void goToNavigation(Context context, boolean ignore_gps) {
		Intent navigationIntent = new Intent(context, NavigationActivity.class);
		navigationIntent.putExtra("ignore_gps", ignore_gps);
		context.startActivity(navigationIntent);
		((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}







	private static final ExecutorService executor = Executors.newSingleThreadExecutor();
	private Future<Void> future;

	@NonNull
	private View.OnClickListener mockBtnClickListener()
	{
		return (view) -> {var builder = new AlertDialog.Builder(view.getContext())
				.setTitle("Inject a Mock Location")
				.setPositiveButton("Choose a Exhibit", (dialog, which) -> {
					dialog.dismiss();
					showChoiceDialog(view);
				})
				.setNegativeButton("Input a Coordinate", (dialog, which) -> {
					dialog.dismiss();
					showCoordinateDialog(view);
				});
			builder.show();
		};
	}
	private void showChoiceDialog(View view)
	{
		Context activity = view.getContext();
		List<ZooData.VertexInfo> vertices = new ArrayList(dataManager.vertexInfoMap.values());
		CharSequence[] items = new CharSequence[vertices.size()];
		for(int i = 0; i < vertices.size(); i++)
			items[i] = vertices.get(i).name;

		var builder = new AlertDialog.Builder(activity)
				.setTitle("Inject a Mock Location")
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int ind) {
						var info = vertices.get(ind);
						var builder = new AlertDialog.Builder(activity)
								.setTitle("Smooth Travel?")
								.setPositiveButton("Yes", (dialog, which) -> {
									GraphPath<String,IdentifiedWeightedEdge> path = dataManager.navigation.findShortestPath(closestUserLocation, PlanUtil.exhibitToGroup(dataManager,info.id));
									List<LatLng> route = new ArrayList<>();
									for(int i = 0; i < path.getEdgeList().size(); i++)
									{
										ZooData.VertexInfo currInfo = dataManager.vertexInfoMap.get(path.getVertexList().get(i));
										if(currInfo.group_id != null)
											currInfo = dataManager.vertexInfoMap.get(currInfo.group_id);
										LatLng start = new LatLng(currInfo.lat,currInfo.lng);
										currInfo = dataManager.vertexInfoMap.get(path.getVertexList().get(i+1));
										if(currInfo.group_id != null)
											currInfo = dataManager.vertexInfoMap.get(currInfo.group_id);
										LatLng end = new LatLng(currInfo.lat,currInfo.lng);
										route.addAll(LocationUtil.interpolate(start,end,(int)(path.getEdgeList().get(i).getWeight()/50)));
									}
									Log.i("NavigationActivity", route.toString());
									future = executor.submit(() -> {
										locationModel.mockLocations(route, 100, TimeUnit.MILLISECONDS);
										return null;
									});
									dialog.dismiss();
								})
								.setNegativeButton("No", (dialog, which) -> {
									var locInfo = info;
									if(info.group_id != null)
										locInfo = dataManager.vertexInfoMap.get(info.group_id);
									locationModel.mockLocation(new LatLng(locInfo.lat,locInfo.lng));
									dialog.dismiss();
								});
						builder.show();
						dialogInterface.dismiss();
					}
				})
				.setNegativeButton("Cancel", (dialog, which) -> {
					dialog.cancel();
				});
		builder.show();
	}
	private void showCoordinateDialog(View view)
	{
		var inputType = EditorInfo.TYPE_CLASS_NUMBER
				| EditorInfo.TYPE_NUMBER_FLAG_SIGNED
				| EditorInfo.TYPE_NUMBER_FLAG_DECIMAL;

		Context activity = view.getContext();

		LatLng latLng = position.getValue();
		final EditText latInput = new EditText(activity);
		latInput.setInputType(inputType);
		latInput.setHint("Latitude");
		latInput.setText(Double.toString(latLng.latitude));

		final EditText lngInput = new EditText(activity);
		lngInput.setInputType(inputType);
		lngInput.setHint("Longitude");
		lngInput.setText(Double.toString(latLng.longitude));

		final LinearLayout layout = new LinearLayout(activity);
		layout.setDividerPadding(8);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(latInput);
		layout.addView(lngInput);

		var builder = new AlertDialog.Builder(activity)
				.setTitle("Inject a Mock Location")
				.setView(layout)
				.setPositiveButton("Submit", (dialog, which) -> {
					var lat = Double.parseDouble(latInput.getText().toString());
					var lng = Double.parseDouble(lngInput.getText().toString());
					LatLng end = new LatLng(lat,lng);
					var builderSmoothAsk = new AlertDialog.Builder(activity)
							.setTitle("Smooth Travel?")
							.setPositiveButton("Yes", (dialogSmoothAsk, whichSmoothAsk) -> {
								List<LatLng> route = LocationUtil.interpolate(position.getValue(),end,2+(int)(Math.sqrt(LocationUtil.distanceSq(position.getValue(),end))/50));
								Log.i("NavigationActivity", route.toString());
								future = executor.submit(()-> {
									locationModel.mockLocations(route, 100, TimeUnit.MILLISECONDS);
									return null;
								});
								dialogSmoothAsk.dismiss();
							})
							.setNegativeButton("No", (dialogSmoothAsk, whichSmoothAsk) -> {
								locationModel.mockLocation(end);
								dialogSmoothAsk.dismiss();
							});
					builderSmoothAsk.show();
					dialog.dismiss();
				})
				.setNegativeButton("Cancel", (dialog, which) -> {
					dialog.cancel();
				});
		builder.show();
	}
}