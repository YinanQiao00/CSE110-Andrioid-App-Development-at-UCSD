package edu.ucsd.cse110.zooseeker.component;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.ucsd.cse110.zooseeker.ExhibitActivity;
import edu.ucsd.cse110.zooseeker.R;
import edu.ucsd.cse110.zooseeker.data.DataManager;
import edu.ucsd.cse110.zooseeker.data.ZooData;
import edu.ucsd.cse110.zooseeker.event.OnDoubleClickListener;
import edu.ucsd.cse110.zooseeker.event.PlanUpdateListener;
import edu.ucsd.cse110.zooseeker.util.plan.PlanUtil;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> implements PlanUpdateListener {
    private List<ZooData.VertexInfo> vertices = Collections.emptyList();
    private Set<String> plan;
    private Activity parent;

    private DataManager dataManager;

    public SearchAdapter(Activity activity)
    {
        plan = new HashSet<String>();
        parent = activity;
        dataManager = DataManager.getInstance(activity);
    }

    public void setVertices(List<ZooData.VertexInfo> newVertices) {
        this.vertices = newVertices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.search_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder holder, int position) {
        holder.setVertex(vertices.get(position));
    }

    @Override
    public int getItemCount() {
        return vertices.size();
    }

    @Override
    public void onPlanChanged(List<ZooData.VertexInfo> newPlan) {
        plan = newPlan.stream().map(info -> info.id).collect(Collectors.toSet());
        parent.runOnUiThread(() -> {
            notifyDataSetChanged();
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView exhibitName;
        private ZooData.VertexInfo vertex;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.exhibitName = itemView.findViewById(R.id.exhibit_name);
        }

        public ZooData.VertexInfo getVertex() {
            return vertex;
        }

        public void setVertex(ZooData.VertexInfo vertex) {
            this.vertex = vertex;
            this.exhibitName.setText(vertex.name);
            this.itemView.setBackgroundDrawable(this.itemView.getResources().getDrawable(
                    plan.contains(this.vertex.id) ?
                            R.drawable.card_added:R.drawable.card_not_added
            ));


            this.itemView.setOnClickListener(new OnDoubleClickListener(450) {
                @Override
                public void onSingleClick(View view) {
                    Context context = view.getContext();
                    Intent exhibitIntent = new Intent(context, ExhibitActivity.class);
                    exhibitIntent.putExtra("exhibit_id", vertex.id);
                    context.startActivity(exhibitIntent);
                    ((Activity)context).overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }

                @Override
                public void onDoubleClick(View view) {
                    if(plan.contains(vertex.id)){
                        PlanUtil.remove(dataManager, vertex.id);
                    }else{
                        PlanUtil.add(dataManager, vertex.id);
                    }
                }
            });
        }
    }
}
