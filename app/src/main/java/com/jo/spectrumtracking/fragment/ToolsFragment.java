package com.jo.spectrumtracking.fragment;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.activity.MainActivity;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.tracking.GPSTracker;
import com.jo.spectrumtracking.tracking.GPSTracker2Plus;
import com.jo.spectrumtracking.widget.CustomSwitch;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.Style;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ToolsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ToolsFragment extends Fragment {

    @BindView(R.id.sSatelliteMap)
    CustomSwitch sSatelliteMap;
    @BindView(R.id.rb_miles)
    RadioButton rbMiles;
    @BindView(R.id.rb_kilometer)
    RadioButton rbKilometers;
    @BindView(R.id.sAutoLockStatus)
    CustomSwitch sAutoLockStatus;
    @BindView(R.id.sPhoneTrackingStatus)
    CustomSwitch sPhoneTrackingStatus;
    @BindView(R.id.edit_delay)
    EditText edit_delay;
    public ToolsFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ToolsFragment newInstance() {
        ToolsFragment fragment = new ToolsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tools, container, false);
        ButterKnife.bind(this, rootView);

        MainActivity mainActivity = (MainActivity)getActivity();
        if (mainActivity.fragment instanceof  MonitorFragment) {
            MonitorFragment monitorFragment = (MonitorFragment)mainActivity.fragment;
            if (monitorFragment.isSatelliteStyle()) {
                sSatelliteMap.setChecked(true);
            } else {
                sSatelliteMap.setChecked(false);
            }
        }
        SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        if (Utils.getDistanceUnit().equals("miles")) {
            rbMiles.setChecked(true);
            rbKilometers.setChecked(false);
        } else {
            rbMiles.setChecked(false);
            rbKilometers.setChecked(true);
        }
        boolean sAutoLockFlag = preferences.getBoolean("sAutoLock", false);
        sAutoLockStatus.setChecked(sAutoLockFlag);
        boolean sPTrackingFlag = preferences.getBoolean("sPhoneTracking", false);
        sPhoneTrackingStatus.setChecked(sPTrackingFlag);
        edit_delay.setText(String.valueOf(preferences.getInt("upload_delay", 30)));
        edit_delay.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (!s.toString().equals("") && !s.toString().equals("0")) {
                    SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt("upload_delay", Integer.parseInt(s.toString()));
                    editor.apply();
                    if (isMyServiceRunning(GPSTracker2Plus.class)) {
                        Intent intent= new Intent(getApplicationContext(), GPSTracker2Plus.class);
                        getApplicationContext().stopService(intent);
                        intent = new Intent(getApplicationContext(), GPSTracker2Plus.class);
                        intent.putExtra("flag", true);
                        intent.putExtra("delay", Integer.parseInt(s.toString()));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            getApplicationContext().startForegroundService(intent);
                        } else {
                            getApplicationContext().startService(intent);
                        }
                    }
                } else {
                    int delay=5;
                    try {
                        delay=Integer.parseInt(s.toString());
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }
                    Intent intent = new Intent(getApplicationContext(), GPSTracker2Plus.class);
                    intent.putExtra("flag", true);
                    intent.putExtra("delay", delay);
                    getApplicationContext().startService(intent);
                }
            }
        });
        return rootView;
    }

    @OnClick(R.id.back)
    public void onBack() {
        MainActivity.get().popFragment();
    }
    @OnCheckedChanged(R.id.sAutoLockStatus)
    public void onChange() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("sAutoLock", sAutoLockStatus.isChecked());
        editor.apply();
        if (sAutoLockStatus.isChecked()) {
            this.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            this.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
    public void setPhoneTracker() throws JSONException {
        ApiInterface apiInterface = ApiClient.getClient(getContext()).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();
        body.put("reportingId", GlobalConstant.app_user.getString("email"));
        body.put("userId", GlobalConstant.app_user.getString("_id"));
        apiInterface.registerPhoneTracker(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                int code = response.code();
                if (code == 201) {
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {

                        // get phone location and send locatio to server and then display map

                        object = new JSONObject(responseBody.string());
                        setAsset(object.getString("_id"));

                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.showShortToast(getContext(), "failed to create phone tracker 1", true);
                    }
                } else {
                    Utils.showShortToast(getContext(), "failed to create phone tracker 2", true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(getContext(), getResources().getString(R.string.weak_cell_signal), true);
            }
        });

    }
    private void setAsset(String trackerId) throws JSONException {

        ApiInterface apiInterface = ApiClient.getClient(getContext()).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();
        body.put("trackerId", trackerId);

        String name = "";
        if (GlobalConstant.app_user.has("firstName")) {
            name = GlobalConstant.app_user.getString("firstName");
        } else {
            name = "driver";
        }

        body.put("name", name);

        //LIAN 01/26/2020 user mail and firstName in asset field.
        //body.put("spectrumId", "phone");
        //body.put("driverName", "Me");

        body.put("spectrumId", GlobalConstant.app_user.getString("email"));
        body.put("driverName", name);

        body.put("userId", GlobalConstant.app_user.getString("_id"));

        //create phone asset
        apiInterface.createPhoneAsset(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                int code = response.code();
                if (code == 200) {

                    // get phone location and send location to server and then display
                    setUserPhoneTracker();
                } else {
                    Utils.showShortToast(getContext(), "failed to create phone asset 1", true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(getContext(), getResources().getString(R.string.weak_cell_signal), true);
            }
        });

    }
    private void setUserPhoneTracker() {
        ApiInterface apiInterface = ApiClient.getClient(getContext()).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();
        apiInterface.setPhoneTrackerFlag(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    sendLocationToServer();
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(getContext(), getResources().getString(R.string.weak_cell_signal), true);
            }
        });
    }
    private void sendLocationFor_10_Times(final double latitude, final double longitude) {

        ApiInterface apiInterface = ApiClient.getClient(getContext()).create(ApiInterface.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startTimeString = sdf.format(new Date());
        Point user_point = Point.fromLngLat(longitude, latitude);
        int accStatus = 0;

        GlobalConstant.user_point = user_point;
        HashMap<String, Object> body = new HashMap<>();
        String user_email = "";
        try {
            user_email = GlobalConstant.app_user.getString("email");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (user_email.equals("") || user_email == null) {
            SharedPreferences preferences = getApplicationContext().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
            user_email = preferences.getString("username", "");
        }
        double distance = 0;
        /*
        if(prev_location != null){
            distance = prev_location.distanceTo(location) * 2.2369;
            distance = Math.abs(prev_location.getLatitude()-location.getLatitude()) + Math.abs(prev_location.getLongitude()-location.getLongitude());
        }
        lastUpdate = System.currentTimeMillis();
        */
        body.put("reportingId", user_email);
        body.put("dateTime", startTimeString);
        body.put("lat", latitude);
        body.put("lng", longitude);
        body.put("ACCStatus", 1);
        body.put("currentTripMileage", distance);////////////////meter to miles
        body.put("speedInMph", 0);
        body.put("trackerModel", "phone");
        //body.put("lastAlert", "");


        //post to assetLogs
        apiInterface.postUserLocation("33bedd43-209c-4025-b157-d7c6df1211e3", body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 201) {
                    System.out.println("good");

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

            }
        });
    }
    private void sendLocationToServer() {
        List<ScanResult> results;
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        results = wifiManager.getScanResults();

        String message = "No internet connection. Please Check your celluar service.";

        if (results != null) {
            final int size = results.size();
            if (size == 0)
                message = "No wifi or cellular service. Try later";
            else {
                ScanResult bestSignal = results.get(0);
                int count = 1;
                for (ScanResult result : results) {
                    if (WifiManager.compareSignalLevel(bestSignal.level, result.level) < 0) {
                        bestSignal = result;
                    }
                }
            }
        }

        OkHttpClient client = new OkHttpClient();
        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        JSONObject postdata = new JSONObject();
        JSONObject childPostData = new JSONObject();
        try {
            postdata.put("considerIp", false);
            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject;
            for (int i = 0; i < results.size(); i++) {
                jsonObject = new JSONObject();
                jsonObject.put("macAddress", results.get(i).BSSID);
                jsonObject.put("signalStrength", results.get(i).level);
                jsonObject.put("signalToNoiseRatio", 0);
                jsonArray.put(jsonObject);
            }
            postdata.put("wifiAccessPoints", jsonArray);

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Send Phone's GPS location to server
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Service.LOCATION_SERVICE);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(isGPSEnabled) {
            GPSTracker gpsTracker=new GPSTracker(getApplicationContext());
            gpsTracker.getLocation();
            sendLocationFor_10_Times(gpsTracker.getLatitude(), gpsTracker.getLongitude());
        }


    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    @OnCheckedChanged(R.id.sPhoneTrackingStatus)
    public void onPhoneTrackingChange() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("sPhoneTracking", sPhoneTrackingStatus.isChecked());
        editor.apply();
        if (sPhoneTrackingStatus.isChecked()) {
            try {
                this.setPhoneTracker();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!isMyServiceRunning(GPSTracker2Plus.class)) {
                int delay = preferences.getInt("upload_delay", 30);
                Intent intent = new Intent(getApplicationContext(), GPSTracker2Plus.class);
                intent.putExtra("flag", true);
                intent.putExtra("delay", delay);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getApplicationContext().startForegroundService(intent);
                } else {
                    getApplicationContext().startService(intent);
                }
            }
        } else {
            if (isMyServiceRunning(GPSTracker2Plus.class)) {
                Intent intent = new Intent(getApplicationContext(), GPSTracker2Plus.class);
                intent.putExtra("flag", false);
                getApplicationContext().stopService(intent);
            }
        }
    }
    @OnCheckedChanged(R.id.sSatelliteMap)
    public void onSatelliteMapChanged() {
        MainActivity mainActivity = (MainActivity)getActivity();
        if (mainActivity.fragment instanceof  MonitorFragment) {
            MonitorFragment monitorFragment = (MonitorFragment)mainActivity.fragment;
            monitorFragment.changeMapStyle(sSatelliteMap.isChecked());
        }
    }


    @OnClick(R.id.rb_miles)
    public void onMilesRadioButton() {
        SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("distance_unit", "miles");
        editor.commit();
    }

    @OnClick(R.id.rb_kilometer)
    public void onKilometerRadioButton() {
        SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("distance_unit", "km");
        editor.commit();
    }
}