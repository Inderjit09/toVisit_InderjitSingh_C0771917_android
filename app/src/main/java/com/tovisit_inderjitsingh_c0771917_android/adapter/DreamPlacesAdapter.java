package com.tovisit_inderjitsingh_c0771917_android.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.tovisit_inderjitsingh_c0771917_android.R;
import com.tovisit_inderjitsingh_c0771917_android.models.DreamPlacesModel;
import com.tovisit_inderjitsingh_c0771917_android.views.fragments.DreamPlacesFragment;
import java.util.List;

public class DreamPlacesAdapter extends RecyclerView.Adapter<DreamPlacesAdapter.ViewHolder> {
    private List<DreamPlacesModel> placesList;
    private DreamPlacesFragment mContext;

    public DreamPlacesAdapter(DreamPlacesFragment mContext, List<DreamPlacesModel> placesList) {
        this.placesList = placesList;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_dream_places, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem, mContext, placesList);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final DreamPlacesModel datum = placesList.get(position);

        holder.placeName.setText(datum.getName());
        if (datum.getVisited()) {
            holder.placeVisited.setText(mContext.getString(R.string.visited));
            holder.placeVisited.setBackgroundColor(ContextCompat.getColor(mContext.getActivity(), R.color.colorLightGrey));
        } else {
            holder.placeVisited.setText(mContext.getString(R.string.visit));
            holder.placeVisited.setBackgroundColor(ContextCompat.getColor(mContext.getActivity(), R.color.colorWhite));
        }
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView placeName, placeVisited;
        public LinearLayout linParent;

        public ViewHolder(View itemView, final DreamPlacesFragment mContext, final List<DreamPlacesModel> placesList) {
            super(itemView);
            placeName = itemView.findViewById(R.id.cell_name);
            linParent = itemView.findViewById(R.id.lin_parent);
            placeVisited = itemView.findViewById(R.id.cell_visited);

            linParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mContext.clickView(placesList.get(getAdapterPosition()));
                }
            });
            placeVisited.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!placesList.get(getAdapterPosition()).getVisited()) {
                        mContext.updateDreamPlace(placesList.get(getAdapterPosition()).getId(), placesList.get(getAdapterPosition()).getLatitude(),
                                placesList.get(getAdapterPosition()).getLongitude(), placesList.get(getAdapterPosition()).getName());
                    }
                }
            });
        }
    }
}
