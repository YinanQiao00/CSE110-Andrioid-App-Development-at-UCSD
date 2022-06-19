package edu.ucsd.cse110.zooseeker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.ucsd.cse110.zooseeker.component.SearchAdapter;
import edu.ucsd.cse110.zooseeker.data.DataManager;
import edu.ucsd.cse110.zooseeker.data.ZooData;
import edu.ucsd.cse110.zooseeker.data.graph.ZooNavigation;
import edu.ucsd.cse110.zooseeker.data.search.ISearch;
import edu.ucsd.cse110.zooseeker.data.search.ZooSearch;
import edu.ucsd.cse110.zooseeker.perm.PermissionChecker;
import edu.ucsd.cse110.zooseeker.util.Constants;
import edu.ucsd.cse110.zooseeker.event.PlanUpdateListener;
import edu.ucsd.cse110.zooseeker.util.plan.PlanDatabase;
import edu.ucsd.cse110.zooseeker.util.plan.PlanUtil;
import edu.ucsd.cse110.zooseeker.util.plan.VisitedUtil;

public class MainActivity extends AppCompatActivity implements PlanUpdateListener {
    private Button planButton;
    private SearchView searchView;
    private ISearch search;
    private RecyclerView recyclerView;
    private SearchAdapter adapter;

    TextView exhibit_count;

    private DataManager dataManager;

    @Override
    protected void onResume() {
        super.onResume();
        PlanUtil.updateThisListener(this, dataManager, this);
        //goToNavigationCheck();
    }

    @Override
    public void onPlanChanged(List<ZooData.VertexInfo> newPlan) {
        runOnUiThread(() -> {
            this.exhibit_count.setText(Integer.toString(newPlan.size()));
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PlanUtil.unregisterPlanUpdateListener(adapter);
        PlanUtil.unregisterPlanUpdateListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataManager = DataManager.instantiate(this, Constants.NODE_FILE_NAME, Constants.EDGE_FILE_NAME, Constants.GRAPH_FILE_NAME, ZooSearch.class, ZooNavigation.class);
        PlanDatabase.getInstance(this);

        new PermissionChecker(this).ensureAllPermission();

        goToNavigationCheck();

        findLayoutComponents();
        setSearchResultsDisplay();
        setEventListeners();
    }

    //Move to navigation if app was closed during navigation
    private void goToNavigationCheck()
    {
        List list = VisitedUtil.getAll();
        if(list == null || !list.isEmpty())
            NavigationActivity.goToNavigation(this, Constants.MOCK_LOCATION);
    }

    private void setEventListeners() {
        DataManager dataManager = DataManager.getInstance(this);
        PlanUtil.registerPlanUpdateListener(dataManager, adapter);
        PlanUtil.registerPlanUpdateListener(dataManager, this);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.setVertices( filterList(search.getMatchingWaypoints(newText)));
                return false;
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

    private void setSearchResultsDisplay() {
        DataManager dataManager = DataManager.getInstance(this);
        search = dataManager.search;

        adapter = new SearchAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.setVertices(search.getByType(ZooData.VertexInfo.Kind.EXHIBIT));
    }

    private void findLayoutComponents() {
        exhibit_count = findViewById(R.id.numExhibits);
        planButton = (Button)findViewById(R.id.view_plan);
        searchView = (SearchView) findViewById(R.id.search_bar);
        recyclerView = findViewById(R.id.recycler_view);
    }

    private List<ZooData.VertexInfo> filterList(List<ZooData.VertexInfo> unfilteredList) {
        List<ZooData.VertexInfo> filteredList = new ArrayList<>();
        for (ZooData.VertexInfo ver : unfilteredList ) {
            if (ver.kind.equals(ZooData.VertexInfo.Kind.EXHIBIT)) {
                filteredList.add(ver);
            }
        }
        return unfilteredList.stream().filter(item -> item.kind == ZooData.VertexInfo.Kind.EXHIBIT).collect(Collectors.toList());
    }

    public void onGearClicked(View view) {
        Intent intent = new Intent(this,SettingActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}