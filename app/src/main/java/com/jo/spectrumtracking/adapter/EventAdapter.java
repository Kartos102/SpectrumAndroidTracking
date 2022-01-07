package com.jo.spectrumtracking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.fragment.ReportsFragment;
import com.jo.spectrumtracking.model.Resp_Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by JO on 3/17/2018.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {


    private List<Resp_Event> itemList;
    private int itemLayoutResID;
    private Fragment fragment;


    public EventAdapter(Fragment fragment, List<Resp_Event> itemList, int itemLayoutResID) {
        this.itemList = itemList;
        this.itemLayoutResID = itemLayoutResID;
        this.fragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);
        viewHolder.setMinimumHeight(25);
        return new EventAdapter.ViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        final Resp_Event item = itemList.get(itemList.size() - position - 1);

        String localDateTime = item.getLocalDateTime();
        String convertedDateTime = localDateTime;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy HH:mm");
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd HH:mm");

        try {
            Date date = sdf.parse(localDateTime);
            convertedDateTime = sdf1.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }


        holder.date.setText(convertedDateTime);
        holder.event.setText(item.getAlarm().trim());
        holder.address.setTag(position);

        holder.address.setOnClickListener(v -> {
            TextView cb = (TextView) v;
            int clickedPos = (Integer) cb.getTag();
            if (fragment instanceof ReportsFragment) {
                ((ReportsFragment) fragment).setAddressClick(item.getAddress());
            }
        });

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.date)
        TextView date;

        @BindView(R.id.event)
        TextView event;

        @BindView(R.id.address)
        TextView address;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}






