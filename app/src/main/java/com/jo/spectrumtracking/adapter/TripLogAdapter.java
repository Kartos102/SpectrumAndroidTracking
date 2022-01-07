package com.jo.spectrumtracking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.model.Replay_TripLog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TripLogAdapter extends RecyclerView.Adapter<TripLogAdapter.ViewHolder> {


    private List<Replay_TripLog> itemList;
    private int itemLayoutResID;
    private double metricScale;
    private Fragment fragment;


    public TripLogAdapter(Fragment fragment, List<Replay_TripLog> itemList, int itemLayoutResID, double metricScale) {
        this.itemList = itemList;
        this.itemLayoutResID = itemLayoutResID;
        this.metricScale = metricScale;
        this.fragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);
        // viewHolder.setMinimumHeight(25);
        return new TripLogAdapter.ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        Replay_TripLog item = itemList.get(position);

        holder.trip_index.setText(item.getTrip_index());

        if (item.getRange() == "") {
            holder.range.setVisibility(View.GONE);
        } else {
            holder.range.setVisibility(View.VISIBLE);
            holder.range.setText(item.getRange());
        }

        if (item.getDetail() == "") {
            holder.detail.setVisibility(View.GONE);
        } else {
            holder.detail.setVisibility(View.VISIBLE);
            holder.detail.setText(item.getDetail());
        }

        holder.maxSpeed.setText(fragment.getString(R.string.max_speed) + ": " + String.format("%.0f", item.getMaxSpeed() * this.metricScale));

        if (item.getState() == 0) {
            holder.trip_icon.setImageResource(R.drawable.location_arrow_blue);
        } else {
            holder.trip_icon.setImageResource(R.drawable.location_arrow_red);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_trip_index)
        TextView trip_index;

        @BindView(R.id.txt_range)
        TextView range;

        @BindView(R.id.txt_detail)
        TextView detail;

        @BindView(R.id.txt_max_speed)
        TextView maxSpeed;

        @BindView(R.id.trip_icon)
        ImageView trip_icon;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}