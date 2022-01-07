package com.jo.spectrumtracking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.fragment.AlarmFragment;
import com.jo.spectrumtracking.fragment.EventFragment;
import com.jo.spectrumtracking.fragment.ReplayFragment;
import com.jo.spectrumtracking.fragment.ReportsFragment;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by JO on 3/17/2018.
 */

public class AssetListSingleSelectRecyclerViewAdapter extends RecyclerView.Adapter<AssetListSingleSelectRecyclerViewAdapter.ViewHolder> {


    private List<Resp_Tracker> itemList;
    private int itemLayoutResID;
    private Fragment fragment;
    private static RadioButton lastChecked = null;

    public AssetListSingleSelectRecyclerViewAdapter(Fragment fragment, List<Resp_Tracker> itemList, int itemLayoutResID) {
        this.itemList = itemList;
        this.itemLayoutResID = itemLayoutResID;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);
        int height = parent.getMeasuredHeight() / 4;
        viewHolder.setMinimumHeight(25);
        return new AssetListSingleSelectRecyclerViewAdapter.ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final Resp_Tracker item = itemList.get(position);

        holder.radioReplayRightOptions.setChecked(item.getSelected());

        if (item.getPlateNumber().isEmpty()) {
            holder.txtVehicleName.setText("plate");
        } else {
            holder.txtVehicleName.setText(item.getPlateNumber());
        }

        if (item.getDriverName().isEmpty()) {
            holder.txtDriverName.setText("plate");
        } else {
            holder.txtDriverName.setText(item.getDriverName());
        }

        holder.itemView.setTag(position);
        holder.radioReplayRightOptions.setTag(position);

        if (position == 0 && itemList.get(0).getSelected() && holder.radioReplayRightOptions.isChecked()) {
            lastChecked = holder.radioReplayRightOptions;
        }

        holder.radioReplayRightOptions.setOnClickListener(v -> {
            RadioButton cb = (RadioButton) v;
            //  if(cb.isChecked()) return;
            int clickedPos = (Integer) cb.getTag();

            for (int i = 0; i < itemList.size(); i++) {
                if (clickedPos != i) {
                    itemList.get(i).setSelected(false);
                } else itemList.get(i).setSelected(true);
            }

            if (lastChecked != null)
                if (lastChecked.equals(cb)) {
                    // return;
                }

            lastChecked = cb;

            if (fragment instanceof ReplayFragment) {
                ((ReplayFragment) fragment).setSelectedTracker(itemList.get(position), true);
            } else if (fragment instanceof ReportsFragment) {
                ((ReportsFragment) fragment).setSelectedTracker(itemList.get(position), true);
            } else if (fragment instanceof AlarmFragment) {
                ((AlarmFragment) fragment).setSelectedTracker(itemList.get(position));
            } else if (fragment instanceof EventFragment) {
                ((EventFragment) fragment).setSelectedTracker(itemList.get(position));
            }
        });

        String driverImageUrl = GlobalConstant.driverImageMap.get(item.get_id());
        if (item.isPhotoStatus()) {
            Picasso.get().load(driverImageUrl).memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.driver_empty).into(holder.driver_image);
        } else {
            holder.driver_image.setImageResource(R.drawable.driver_empty);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.radio_replay_right_options)
        RadioButton radioReplayRightOptions;

        @BindView(R.id.txt_vehicle_name)
        TextView txtVehicleName;

        @BindView(R.id.txt_driver_name)
        TextView txtDriverName;

        @BindView(R.id.driver_image)
        CircleImageView driver_image;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}






