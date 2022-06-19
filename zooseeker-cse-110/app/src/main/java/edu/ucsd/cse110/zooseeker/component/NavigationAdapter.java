package edu.ucsd.cse110.zooseeker.component;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import edu.ucsd.cse110.zooseeker.R;


public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.ViewHolder>{
    private List<String> strItems = Collections.emptyList();


    public void setStrItems(List<String> newStrItems) {
        this.strItems.clear();
        this.strItems = newStrItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.navigation_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setStrItem(strItems.get(position));
    }


    @Override
    public int getItemCount() {
        return strItems.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView navigationText;
        private String strItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.navigationText = itemView.findViewById(R.id.navigation_item_text);
        }

        public String getStrItem() {return strItem;}

        public void setStrItem(String item) {
            this.strItem = item;
            this.navigationText.setText(item);
        }
    }
}