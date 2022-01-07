package com.jo.spectrumtracking.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.model.OrderSummary;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrderSummaryRecyclerViewAdapter extends RecyclerView.Adapter <OrderSummaryRecyclerViewAdapter.ViewHolder>{
    private List<OrderSummary> itemList;
    private int itemLayoutResID;
    private Activity acitivity;
    public OrderSummaryRecyclerViewAdapter(Activity activity, List<OrderSummary> itemList, int itemLayoutResID){
        this.acitivity=activity;
        this.itemList=itemList;
        this.itemLayoutResID=itemLayoutResID;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);

        return new OrderSummaryRecyclerViewAdapter.ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        OrderSummary item=itemList.get(position);
        holder.summaryVehicleTextView.setText(item.getVehicle());
        holder.summaryTrackerTextView.setText(item.getTracker());
        holder.summaryDataplanTextView.setText(item.getDataPlan());
        holder.summaryLTEDataTextView.setText(item.getLTEData());
        holder.summaryDateTdTextView.setText(item.getDateTd());
        holder.summaryAutoRenewTextView.setText(item.getAutoRenew());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.summary_vehicle)
        TextView summaryVehicleTextView;
        @BindView(R.id.summary_tracker)
        TextView summaryTrackerTextView;
        @BindView(R.id.summary_dataplan)
        TextView summaryDataplanTextView;
        @BindView(R.id.summary_LTEData)
        TextView summaryLTEDataTextView;
        @BindView(R.id.summary_dateTd)
        TextView summaryDateTdTextView;
        @BindView(R.id.summary_autorenew)
        TextView summaryAutoRenewTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}
