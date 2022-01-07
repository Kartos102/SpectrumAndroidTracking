package com.jo.spectrumtracking.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.api.AddressCallbak;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.fragment.MonitorFragment;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_Tracker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by JO on 3/17/2018.
 */

public class VelocityRecyclerViewAdapter extends RecyclerView.Adapter<VelocityRecyclerViewAdapter.ViewHolder> {

    private List<Resp_Tracker> trackers;
    private int itemLayoutResID;
    private MonitorFragment monitorFragment;
    private String lastACCOnTimeString;
    private String lastACCOffTimeString;


    public VelocityRecyclerViewAdapter(MonitorFragment monitorFragment, List<Resp_Tracker> trackers, int itemLayoutResID) {
        this.trackers = trackers;
        this.itemLayoutResID = itemLayoutResID;
        this.monitorFragment = monitorFragment;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View viewHolder = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResID, parent, false);
        // viewHolder.setIsRecyclable(false);
        ViewHolder view_holder = new VelocityRecyclerViewAdapter.ViewHolder(viewHolder);
        view_holder.setIsRecyclable(false);
        return view_holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        Resp_Tracker tracker = trackers.get(position);

        if(tracker.getName().equals("")) {
            holder.txtName.setText(tracker.getPlateNumber());
        }
        else {
            holder.txtName.setText(tracker.getName());
        }

        if (tracker.getACCStatus() == 0 || tracker.getSpeedInMph() == 0) {
            holder.txtStatus.setText("Park");
        } else {
            holder.txtStatus.setText("Drive");
        }

        double metricScale = Utils.getDistanceUnit().equals("miles") ? 1 : 1.60934;
//        double metricScale = (tracker.getCountry().equals("United States")) ? 1 : 1.60934;
        holder.txtSpeed.setText(String.format("%.0f", tracker.getSpeedInMph() * metricScale));

        SimpleDateFormat sdf_ = new SimpleDateFormat("MM/dd/yyyy");
        holder.txt_expiration_date.setText(sdf_.format(tracker.getExpirationDate()));

        boolean vehicleExist = false;
        boolean accOnChanged = false;
        boolean accOffChanged = false;

        Resp_Tracker oldTracker = null;

        if (MonitorFragment.oldTrackers == null) {
            MonitorFragment.oldTrackers = new ArrayList<>();
            vehicleExist = false;
        } else {
            for (Resp_Tracker _tracker : MonitorFragment.oldTrackers) {
                String oldName = _tracker.getName();
                if (oldName == null || oldName == "") continue;;
                if (oldName.equals(tracker.getName())) {
                    vehicleExist = true;
                    oldTracker = _tracker;
                    break;
                }
            }
        }
        if (!vehicleExist) {
            MonitorFragment.oldTrackers.add(tracker);
            oldTracker = tracker;
        } else {
            if (tracker.getLastACCOnTime() != null) {
                accOnChanged = !oldTracker.getLastACCOnTime().equals(tracker.getLastACCOnTime());
            } else {
                accOnChanged = false;
            }

            if (tracker.getLastACCOffTime() != null) {
                accOffChanged = !oldTracker.getLastACCOffTime().equals(tracker.getLastACCOffTime());
            } else {
                accOnChanged = false;
            }
        }

        holder.txt_fuel.setText(String.format("%.2f", tracker.getTankVolume()));
        holder.txt_battery.setText(String.format("%.2f", tracker.getVoltage()));

        if (tracker.getHotspot() == 1) {
            holder.wifidata_title.setText(monitorFragment.getString(R.string.wifi_data));
            float dataLimit = (float)((int)(tracker.getDataLimit() * 100)) / 100;
            float dataVolumecustomerCycle = (float)((int)(tracker.getDataVolumeCustomerCycle() * 100)) / 100;
            holder.txt_wifidata.setText(String.format("%.2f", Math.max(0, dataLimit - dataVolumecustomerCycle)));
        } else {
            holder.wifidata_title.setText(monitorFragment.getString(R.string.rpm));
            holder.txt_wifidata.setText(String.format("%.2f", tracker.getRPM()));
        }

        if (tracker.getSpeedInMph() * metricScale > 30) {
            holder.alert_title.setText(monitorFragment.getString(R.string.alert));
            holder.txt_lastAlert.setText(tracker.getLastAlert());
        } else {
            holder.alert_title.setText(monitorFragment.getString(R.string.last_update));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            holder.txt_lastAlert.setText(simpleDateFormat.format(tracker.getLatLngDateTime()));
        }


        // holder.txtThisTrip.setText(String.format("%.1f",val.weekMile));
        holder.txtDayTrip.setText(String.format("%.1f", tracker.getDayMile()));
        holder.txtMonthTrip.setText(String.format("%.1f", tracker.getMonthMile()));
        holder.txtYearTrip.setText(String.format("%.1f", tracker.getYearMile()));


        String lastACCOnAdd = "";
        String lastACCOffAdd = "";

        final Date lastACCOnTime = tracker.getLastACCOnTime();
        final Date lastACCOffTime = tracker.getLastACCOffTime();

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm");
        lastACCOnTimeString = "";
        lastACCOffTimeString = "";
        if (lastACCOnTime == null) {
            lastACCOnTimeString = "";
        } else {
            lastACCOnTimeString = sdf.format(lastACCOnTime);
        }
        if (lastACCOffTime == null) {
            lastACCOffTimeString = "";
        } else {
            lastACCOffTimeString = sdf.format(lastACCOffTime);
        }
        holder.txtLastStop.setText(lastACCOffTimeString);
        holder.txtLastStart.setText(lastACCOnTimeString);

        holder.itemView.setTag(tracker);
    }

    private String coord2Address2(double lat, double lng, final AddressCallbak addressCallbakCallback) {
        String address = "";

        ApiInterface apiInterface = ApiClient.getClient(monitorFragment.getContext()).create(ApiInterface.class);
        Call<ResponseBody> call = apiInterface.coord2Address(lng + "," + lat);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String responseBody = null;
                try {
                    responseBody = response.body().string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JSONObject object = null;

                try {
                    object = new JSONObject(responseBody);
                    JSONArray items = (JSONArray) object.get("features");
                    JSONObject feature = (JSONObject) items.get(0);
                    addressCallbakCallback.onSuccess(feature.getString("place_name"));

                    System.out.println("ok");

                } catch (Exception e) {
                    e.printStackTrace();

                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                addressCallbakCallback.onError(t);

            }
        });
        return address;
    }

    @Override
    public int getItemCount() {
        return trackers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.txt_name)
        TextView txtName;

        @BindView(R.id.txt_status)
        TextView txtStatus;

        @BindView(R.id.txt_speed)
        TextView txtSpeed;

        /*
         @BindView(R.id.txt_voltage)
         TextView txtVoltage;
        */

        @BindView(R.id.txt_expiration_date)
        TextView txt_expiration_date;

//        @BindView(R.id.txt_speeding)
//        TextView txt_speeding;

        @BindView(R.id.txt_fuel)
        TextView txt_fuel;

        @BindView(R.id.txt_battery)
        TextView txt_battery;

        @BindView(R.id.txt_wifidata)
        TextView txt_wifidata;


        @BindView(R.id.txt_alert)
        TextView txt_lastAlert;


        @BindView(R.id.txt_last_start)
        TextView txtLastStart;

//        @BindView(R.id.txt_last_start_address)
//        TextView txtLastStartAddress;


        @BindView(R.id.txt_last_stop)
        TextView txtLastStop;

//        @BindView(R.id.txt_last_stop_address)
//        TextView txtLastStopAddress;

        //@BindView(R.id.txt_this_trip)
        //TextView txtThisTrip;

        @BindView(R.id.txt_day_trip)
        TextView txtDayTrip;

        @BindView(R.id.txt_month_trip)
        TextView txtMonthTrip;

        @BindView(R.id.txt_year_trip)
        TextView txtYearTrip;

        @BindView(R.id.alert_title)
        TextView alert_title;

        @BindView(R.id.wifidata_title)
        TextView wifidata_title;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }

}
