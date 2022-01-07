package com.jo.spectrumtracking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.fragment.SetShareDeviceFragment;
import com.jo.spectrumtracking.model.Resp_SharedDevice;
import com.jo.spectrumtracking.widget.CustomSwitch;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SetSharedDeviceRecyclerViewAdapter extends RecyclerView.Adapter<SetSharedDeviceRecyclerViewAdapter.ViewHolder> {


    private List<Resp_SharedDevice> itemList;
    private int itemLayoutResID;
    public Fragment fragment;

    public SetSharedDeviceRecyclerViewAdapter(Fragment fragment, List<Resp_SharedDevice> itemList, int itemLayoutResID) {
        this.fragment = fragment;
        this.itemList = itemList;
        this.itemLayoutResID = itemLayoutResID;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);

        return new SetSharedDeviceRecyclerViewAdapter.ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Resp_SharedDevice item = itemList.get(position);

        holder.txtDeviceName.setText(item.getPlateNumber());

        if (item.getFlag().equals("1")) {
            holder.device.setChecked(true);
        } else holder.device.setChecked(false);

        holder.device.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String flag = "-1";
            if (isChecked) flag = "1";
            SetShareDeviceFragment setShareDeviceFragment = (SetShareDeviceFragment) fragment;
            setShareDeviceFragment.setShareFlag(item.getReport_id(), flag);
        });
        holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_device_name)
        TextView txtDeviceName;
        @BindView(R.id.switch_device)
        CustomSwitch device;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}