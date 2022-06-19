package edu.ucsd.cse110.zooseeker.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import edu.ucsd.cse110.zooseeker.R;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanViewHolder> {

    Context context;
    ArrayList<PlanCard> planCardArrayList;
    public PlanAdapter(Context context, ArrayList<PlanCard> planCardArrayList) {
        this.context = context;
        this.planCardArrayList = planCardArrayList;
    }

    @NonNull
    @Override
    public PlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.plan_exhibit_card, parent, false);
        return new PlanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanViewHolder holder, int position) {
        PlanCard exhibit = planCardArrayList.get(position);
        holder.setExhibitView(exhibit.getExhibit());
        holder.setDistanceView(exhibit.getDistance());
        holder.setRankView(exhibit.getRank());
    }

    @Override
    public int getItemCount() {
        return planCardArrayList.size();
    }

    public class PlanViewHolder extends RecyclerView.ViewHolder {
        public TextView exhibitView;
        public TextView distanceView;
        public TextView rankView;

        public PlanViewHolder(@NonNull View itemView) {
            super(itemView);
            exhibitView = (TextView) itemView.findViewById(R.id.plan_exhibit_card_name);
            distanceView = (TextView) itemView.findViewById(R.id.plan_exhibit_card_distance);
            rankView = (TextView) itemView.findViewById(R.id.plan_exhibit_card_rank);
        }
        public void setExhibitView(String exhibit) {
            exhibitView.setText(exhibit);
        }
        public void setDistanceView(double distance) {
            if(distance < 0) {
                distanceView.setText("No Distance Available");
            }
            if(distance == 0) {
                distanceView.setText("Arrived at Exhibit");
            }
            else {
                distanceView.setText(distance + "ft");
            }
        }
        public void setRankView(int rank) {
            rankView.setText(rank + "");
        }
    }

}
