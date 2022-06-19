package edu.ucsd.cse110.zooseeker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;
import java.util.Optional;

import edu.ucsd.cse110.zooseeker.data.ZooData;

import edu.ucsd.cse110.zooseeker.data.DataManager;
import edu.ucsd.cse110.zooseeker.event.PlanUpdateListener;
import edu.ucsd.cse110.zooseeker.util.plan.PlanUtil;

public class ExhibitActivity extends AppCompatActivity implements PlanUpdateListener {
    List<ZooData.VertexInfo> plan;
    ZooData.VertexInfo item;
    public TextView exhibitName;
    public TextView distance;
    public TextView location;
    public TextView hours;
    public TextView description;
    public TextView exhibitDistance;
    public TextView exhibitLocation;
    public TextView exhibitHours;
    public TextView exhibitDescription;
    public ImageButton backButton;
    public Button addButton;
    public Button directionsButton;
    public Button planButton;
    public Button nextButton;
    public String id;
    private DataManager dataManager;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PlanUtil.unregisterPlanUpdateListener(this);
    }

    @Override
    public void onPlanChanged(List<ZooData.VertexInfo> newPlan) {
        plan = newPlan;
        displayExhibitData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exhibit);

        dataManager = DataManager.getInstance(this);
        PlanUtil.registerPlanUpdateListener(dataManager,this);

        findLayoutComponents();

        setEventListeners();
    }

    private void displayExhibitData() {
        // load the exhibit id
        id = getIntent().getStringExtra("exhibit_id");
        item = dataManager.vertexInfoMap.get(id);
        runOnUiThread(() -> {
            exhibitName.setText(item.name);
        });
        // Change Add to Remove if exhibit exists
        if(plan.contains(item)){
            runOnUiThread(() -> {
                addButton.setText("Remove");
            });
            // referenced stackoverflow 17189715
            addButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_style_remove));
        }
        else {
            addButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_style_add));
        }
    }

    private void setEventListeners() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(plan.contains(item)){
                    PlanUtil.remove(dataManager, id);
                    addButton.setText("Add");
                    addButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_style_add));
                }else{
                    PlanUtil.add(dataManager, id);
                    addButton.setText("Remove");
                    addButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_style_remove));
                }
            }
        });
        Intent planIntent = new Intent(this, PlanActivity.class);
        planButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.getContext().startActivity(planIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    private void findLayoutComponents() {
        exhibitName = (TextView)findViewById(R.id.exhibit_name);
        exhibitDistance = (TextView)findViewById(R.id.exhibit_distance);
        exhibitLocation = (TextView)findViewById(R.id.exhibit_location);
        exhibitHours = (TextView)findViewById(R.id.exhibit_hours);
        exhibitDescription = (TextView)findViewById(R.id.exhibit_description);

        backButton = (ImageButton)findViewById(R.id.exhibit_back_button);
        addButton = (Button)findViewById(R.id.add_button);
        directionsButton = (Button)findViewById(R.id.directions_button);
        planButton = (Button)findViewById(R.id.my_exhibitions_button);
        nextButton = (Button)findViewById(R.id.next_button);
    }

    // loads the lists and updates the number of the list upon Resume
    @Override
    protected void onResume() {
        super.onResume();
        PlanUtil.updateThisListener(this, dataManager, this);

        if(plan.contains(item)){
            addButton.setText("Remove");
        }else{
            addButton.setText("Add");
        }
    }

    public void onGearClicked(View view) {
        Intent intent = new Intent(this,SettingActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}