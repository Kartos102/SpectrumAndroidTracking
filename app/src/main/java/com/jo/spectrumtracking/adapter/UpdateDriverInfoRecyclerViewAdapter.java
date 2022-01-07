package com.jo.spectrumtracking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.fragment.UpdateDriverInfoFragment;
import com.jo.spectrumtracking.model.Resp_Driver;
import com.jo.spectrumtracking.model.Resp_Tracker;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JO on 3/18/2018.
 */

public class UpdateDriverInfoRecyclerViewAdapter extends RecyclerView.Adapter<UpdateDriverInfoRecyclerViewAdapter.ViewHolder> {


    private List<Resp_Tracker> itemList;
    private int itemLayoutResID;
    public Fragment fragment;

    public UpdateDriverInfoRecyclerViewAdapter(Fragment fragment, List<Resp_Tracker> itemList, int itemLayoutResID) {
        this.fragment = fragment;
        this.itemList = itemList;
        this.itemLayoutResID = itemLayoutResID;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);

        return new UpdateDriverInfoRecyclerViewAdapter.ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Resp_Tracker item = itemList.get(position);

        holder.editDriverName.setText(item.getDriverName());
        holder.editDriverPhone.setText(item.getDriverPhoneNumber());
        holder.editVehicleName.setText(item.getName());

        if (item.getColor() != null) {

            String selectedColor = item.getColor();
            if (selectedColor.equals("ORANGE")) {
                selectedColor = "YELLOW";
            }
            ArrayAdapter<String> colors = (ArrayAdapter<String>) holder.edit_color.getAdapter();
            int selectedIndex = -1;
            for (int i = 0; i < colors.getCount(); i++) {
                if (colors.getItem(i).equals(selectedColor)) {
                    selectedIndex = i;
                    break;
                }
            }

            if (selectedIndex >= 0) holder.edit_color.setSelection(selectedIndex);
        }


        if (item.getAutoRenew() != null) {

            String selectedRenew = item.getAutoRenew();

            ArrayAdapter<String> renews = (ArrayAdapter<String>) holder.edit_auto_renew.getAdapter();
            int selectedIndex = -1;
            for (int i = 0; i < renews.getCount(); i++) {
                if (renews.getItem(i).equals(selectedRenew)) {
                    selectedIndex = i;
                    break;
                }
            }

            if (selectedIndex >= 0) holder.edit_auto_renew.setSelection(selectedIndex);
        }


        holder.btnUpdateDriverInfo.setOnClickListener(v -> {
            if (UpdateDriverInfoRecyclerViewAdapter.this.fragment instanceof UpdateDriverInfoFragment) {
                UpdateDriverInfoFragment fragment = (UpdateDriverInfoFragment) UpdateDriverInfoRecyclerViewAdapter.this.fragment;
                String color = holder.edit_color.getSelectedItem().toString();
                if (color.equals("YELLOW")) color = "ORANGE";
                String autoRenew = holder.edit_auto_renew.getSelectedItem().toString();
                fragment.onUpdateButtonClick(item, holder.editDriverName.getText().toString(), holder.editDriverPhone.getText().toString(), holder.editVehicleName.getText().toString(), color, autoRenew);
            }
        });

        holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.edit_driver_name)
        TextView editDriverName;

        @BindView(R.id.edit_driver_phone)
        TextView editDriverPhone;

        @BindView(R.id.edit_vehicle_name)
        TextView editVehicleName;

        @BindView(R.id.btn_update_driver_info)
        ImageButton btnUpdateDriverInfo;

        @BindView(R.id.edit_color)
        Spinner edit_color;

        @BindView(R.id.edit_auto_renew)
        Spinner edit_auto_renew;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
