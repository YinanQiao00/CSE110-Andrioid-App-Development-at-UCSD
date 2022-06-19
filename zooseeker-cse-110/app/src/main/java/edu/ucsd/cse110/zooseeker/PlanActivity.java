package edu.ucsd.cse110.zooseeker;

import static edu.ucsd.cse110.zooseeker.util.LocationUtil.getDistanceToVertexIds;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.ucsd.cse110.zooseeker.component.ExhibitComparator;
import edu.ucsd.cse110.zooseeker.component.PlanAdapter;
import edu.ucsd.cse110.zooseeker.component.PlanCard;
import edu.ucsd.cse110.zooseeker.data.DataManager;
import edu.ucsd.cse110.zooseeker.data.ZooData;
import edu.ucsd.cse110.zooseeker.event.PlanUpdateListener;
import edu.ucsd.cse110.zooseeker.model.LocationModel;
import edu.ucsd.cse110.zooseeker.util.Constants;
import edu.ucsd.cse110.zooseeker.util.plan.PlanUtil;

public class PlanActivity extends AppCompatActivity implements PlanUpdateListener {
    public static final String WARNING = "Are you sure you want to clear the current plan?";
    public static final String NO_EXHIBITS = "You have no exhibits in the plan yet.";
    TextView exhibit_count;
    Button btn_add;
    ImageButton btn_back;
    Button btn_clear;
    Button btn_plan_start;
    private PlanAdapter planAdapter;
    private RecyclerView planRecyclerView;
    private List<ZooData.VertexInfo> list;
    private ArrayList<PlanCard> plannedExhibits;
    Set<String> exhibitIds;
    LocationModel locationModel;
    LiveData<LatLng> position;

    DataManager dataManager;

    @Override
    protected void onResume() {
        super.onResume();
        PlanUtil.updateThisListener(this, dataManager, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PlanUtil.unregisterPlanUpdateListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onPlanChanged(List<ZooData.VertexInfo> newPlan) {
        runOnUiThread(() -> {
            exhibit_count.setText(String.valueOf(newPlan.size()));
            list = newPlan;
            addExhibits();
            getExhibitIds();
            Collections.sort(plannedExhibits, new ExhibitComparator());
            ZooData.VertexInfo gate = dataManager.search.getByType(ZooData.VertexInfo.Kind.GATE).get(0);
            locationModel.mockLocation(new LatLng(gate.lat,gate.lng));
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan);
      
        dataManager = DataManager.getInstance(this);

        loadSelectedExhibits(this);
        updatePlanUI(this);
        findLayoutComponents();
        setEventListeners(this);

        locationModel = new ViewModelProvider(this).get(LocationModel.class);

    }

    private void loadSelectedExhibits(Context context) {
        plannedExhibits = new ArrayList<>();
        list = new ArrayList<>();
    }

    private void updatePlanUI(Context context) {
        planAdapter = new PlanAdapter(context, plannedExhibits);
        planRecyclerView = findViewById(R.id.plan_exhibit_list);
        planRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        planRecyclerView.setAdapter(planAdapter);
        PlanUtil.registerPlanUpdateListener(dataManager,this);
    }

    private void findLayoutComponents() {
        exhibit_count = (TextView)findViewById(R.id.num_exhibits);
        btn_clear = findViewById(R.id.clear_button);
        btn_back = (ImageButton)findViewById(R.id.plan_back_button);
        btn_plan_start = findViewById(R.id.Start_Plan_button);
    }

    private void setEventListeners(Context context) {
        btn_clear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new AlertDialog.Builder(context).setTitle("Warning").setMessage(WARNING)
                    .setPositiveButton("Yes", (dialog, id) -> {
                        PlanUtil.clear(dataManager);
                        runOnUiThread(() -> {
                            plannedExhibits.clear();
                            planAdapter.notifyDataSetChanged();
                            exhibit_count.setText(String.valueOf(list.size()));
                        });
                    }).setNegativeButton("No", (dialog, id) -> {
                        dialog.cancel();
                }).setCancelable(true).create().show();
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        btn_plan_start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(PlanUtil.list_num() > 0) {
                    NavigationActivity.goToNavigation(v.getContext(), Constants.MOCK_LOCATION);
                }
                else
                {
                    Toast toast = Toast.makeText(context,NO_EXHIBITS, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
    }

    public void onGearClicked(View view) {
        Intent intent = new Intent(this,SettingActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
  
    private void addExhibits() {
        plannedExhibits = new ArrayList<PlanCard>();
        planAdapter = new PlanAdapter(this, plannedExhibits);
        planRecyclerView = findViewById(R.id.plan_exhibit_list);
        planRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        planRecyclerView.setAdapter(planAdapter);

        position = locationModel.getPositionLiveData();
        position.observe(this, latLng -> {
            PlanCard card;
            String id;
            String name;
            String location;
            double distance;
            Map<String, Double> exhibitDistances = getDistanceToVertexIds(dataManager,position.getValue(),PlanUtil.exhibitsToGroups(dataManager, exhibitIds));
            Log.v("MAP SIZE", exhibitDistances.size() + "");
            for(ZooData.VertexInfo exhibit : list) {
                name = exhibit.name;
                id = exhibit.id;
                location = "";
                Log.v("DISTANCE",exhibitDistances.get(name) + "");
                distance = exhibitDistances.get(exhibit.group_id == null ? exhibit.id : exhibit.group_id); // exhibitDistances.get(id);
                Log.v("PLAN CARD", "name: " + name + " location: " + location + "distance: " + distance);
                card = new PlanCard(name, location, distance);
                if(!plannedExhibits.contains(card)) {
                    plannedExhibits.add(card);
                }
            }
            rankExhibits();
        });
    }
    private void rankExhibits() {
        Collections.sort(plannedExhibits, new ExhibitComparator());
        int rank = 1;
        for(PlanCard exhibit : plannedExhibits) {
            exhibit.setRank(rank);
            rank++;
        }
    }
    private void getExhibitIds() {
        String id;
        exhibitIds = new HashSet<String>();
        for(ZooData.VertexInfo exhibit : list) {
            id = exhibit.id;
            Log.v("EXHIBIT ID: ", id);
            exhibitIds.add(id);
        }
    }
}