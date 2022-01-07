package com.jo.spectrumtracking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.fragment.OrderServiceFragment;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.LTEData;
import com.jo.spectrumtracking.model.OrderService;
import com.jo.spectrumtracking.model.ServicePlan;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by JO on 3/17/2018.
 */

public class OrderServiceRecyclerViewAdapter extends RecyclerView.Adapter<OrderServiceRecyclerViewAdapter.ViewHolder> {


    private List<OrderService> itemList;
    private int itemLayoutResID;
    private OrderServiceFragment fragment;

    public List<OrderService> getItemList() {
        return itemList;
    }

    public OrderServiceRecyclerViewAdapter(OrderServiceFragment fragment, List<OrderService> itemList, int itemLayoutResID) {
        this.itemList = itemList;
        this.itemLayoutResID = itemLayoutResID;
        this.fragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);

        return new OrderServiceRecyclerViewAdapter.ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final OrderService item = itemList.get(position);

        final int row = position;

        if (item.getName() == null || item.getName().isEmpty()) {
            holder.txtName.setText("driverName");
        } else {
            holder.txtName.setText(item.getName());
        }

        holder.txtExpirationDate.setText(item.getExpirationDate());

        // Service Plan spinner
        List<String> servicePlanSpinnerArray = new ArrayList<>();
        for (ServicePlan servicePlan : item.getServicePlanList()) {
            servicePlanSpinnerArray.add(servicePlan.getServicePlan());
        }
//        ArrayAdapter<String> servicePlanSpinnerAdapter = new ArrayAdapter<String>(fragment.getContext(), R.layout.simple_multiline_spinner, servicePlanSpinnerArray);
        CustomAdapter servicePlanSpinnerAdapter = new CustomAdapter(fragment.getContext(),
                R.layout.simple_multiline_spinner, servicePlanSpinnerArray);
        holder.spServicePlan.setAdapter(servicePlanSpinnerAdapter);
        holder.spServicePlan.setSelection(item.getSelectedServicePlanId());
        holder.spServicePlan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemList.get(row).setSelectedServicePlanId(position);
                fragment.updateBottomPrices();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


//        holder.spServicePlan.setEnabled(item.isServicePlanEnabled());
        holder.spServicePlan.setEnabled(true);

        // LTE Data spinner
        List<String> lteDataSpinnerArray = new ArrayList<>();
        for (LTEData lteData : item.getLteDataList()) {
            lteDataSpinnerArray.add(lteData.getLteData());
        }
        ArrayAdapter<String> lteDataSpinnerAdapter = new ArrayAdapter<>(fragment.getContext(), android.R.layout.simple_spinner_item, lteDataSpinnerArray);
        holder.spLTEData.setAdapter(lteDataSpinnerAdapter);
        holder.spLTEData.setSelection(item.getSelectedLTEDataId());

        holder.spLTEData.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemList.get(row).setSelectedLTEDataId(position);
                fragment.updateBottomPrices();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        holder.spLTEData.setEnabled(item.isLteDataEnabled());

        // Auto Renew spinner
        List<String> autoRenewSpinnerArray = new ArrayList<>();
        autoRenewSpinnerArray.add("Yes");
        autoRenewSpinnerArray.add("No");
        ArrayAdapter<String> autoRenewSpinnerAdapter = new ArrayAdapter<String>(fragment.getContext(), android.R.layout.simple_spinner_item, autoRenewSpinnerArray);
        holder.spAutoReview.setAdapter(autoRenewSpinnerAdapter);
        holder.spAutoReview.setSelection(item.getAutoReview() ? 0 : 1);
        holder.spAutoReview.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemList.get(row).setAutoReview(position == 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        final ViewHolder temp_holder = holder;
        holder.btn_info.setOnClickListener(v -> {
            int selectedServicePlanId = itemList.get(row).getSelectedServicePlanId();
            ServicePlan servicePlan = itemList.get(row).getServicePlanList().get(selectedServicePlanId);

            String info_html = "";

            for (int i=0; i<servicePlan.getPlanDetail().size(); i++) {
                String detail = String.format("%d. %s\n", i + 1, servicePlan.getPlanDetail().get(i));
                info_html = info_html + detail;
            }

            if (!info_html.isEmpty()) {
                Utils.showSweetAlert(fragment.getContext(), temp_holder.spServicePlan.getSelectedItem().toString(), info_html, null, null, SweetAlertDialog.NORMAL_TYPE, null);
            }
        });

        holder.itemView.setTag(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_name)
        TextView txtName;

        @BindView(R.id.txt_expiration_date)
        TextView txtExpirationDate;

        @BindView(R.id.sp_service_plan)
        Spinner spServicePlan;

        @BindView(R.id.sp_lte_data)
        Spinner spLTEData;

        @BindView(R.id.sp_auto_review)
        Spinner spAutoReview;

        @BindView(R.id.btn_info2)
        Button btn_info;
        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }


}
