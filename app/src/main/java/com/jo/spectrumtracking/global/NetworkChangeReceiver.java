package com.jo.spectrumtracking.global;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jo.spectrumtracking.activity.MainActivity;
import com.jo.spectrumtracking.fragment.MonitorFragment;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        int status = Utils.getConnectivityStatusString(context);
        //Log.e("Sulod sa network reciever", "Sulod sa network reciever");
        if (!"android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
            if(status==Utils.NETWORK_STATUS_NOT_CONNECTED){
                //new ForceExitPause(context).execute();
            }else{
                try{
                    MainActivity mainActivity=(MainActivity)context;
                    MonitorFragment monitorFragment=(MonitorFragment)mainActivity.getFragment();
                    monitorFragment.loadAllDrivers();

                }
                catch (Exception ex){
                    ex.printStackTrace();
                }

                //new ResumeForceExitPause(context).execute();
            }

        }
    }
}