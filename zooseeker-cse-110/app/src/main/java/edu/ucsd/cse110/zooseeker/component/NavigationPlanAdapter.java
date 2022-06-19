package edu.ucsd.cse110.zooseeker.component;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.tuple.Triple;

import java.util.Collections;
import java.util.List;

import edu.ucsd.cse110.zooseeker.R;


public class NavigationPlanAdapter extends RecyclerView.Adapter<NavigationPlanAdapter.ViewHolder>{
    private List<Triple<String, String, String>> strItems = Collections.emptyList();
    private List<List<String>> groupExtras = Collections.emptyList();


    public void setStrItems(List<Triple<String, String, String>> newStrItems, List<List<String>> groupExtras) {
        this.strItems = newStrItems;
        this.groupExtras = groupExtras;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.plan_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setStrItem(strItems.get(position), groupExtras == null ? null : groupExtras.get(position));
    }


    @Override
    public int getItemCount() {
        return strItems.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView exhibit;
        private TextView distance;
        private TextView street;

        private TextView groupedExhibit;

        private Triple<String, String, String> item;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.exhibit = itemView.findViewById(R.id.plan_exhibit);
            this.distance = itemView.findViewById(R.id.plan_distance);
            this.street = itemView.findViewById(R.id.plan_street);
            this.groupedExhibit = itemView.findViewById(R.id.grouped_exhibits);
        }

        public Triple<String, String, String> getStrItem() {return item;}

        public void setStrItem(Triple<String, String, String> item, @Nullable List<String> grouped) {
            this.item = item;
            this.exhibit.setText(item.getLeft());
            this.distance.setText(item.getMiddle());
            this.street.setText(item.getRight());
            if(grouped != null && !grouped.isEmpty())
            {
                this.groupedExhibit.setVisibility(View.VISIBLE);
                this.groupedExhibit.setText(grouped.stream().reduce((total, line) -> {
                    return total + "\n" + line;
                }).get());
            }
            else
                this.groupedExhibit.setVisibility(View.GONE);
        }
    }
}