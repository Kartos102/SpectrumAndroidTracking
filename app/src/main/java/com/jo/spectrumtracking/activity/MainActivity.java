package com.jo.spectrumtracking.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.fragment.ActivateTrackerFragment;
import com.jo.spectrumtracking.fragment.AlarmFragment;
import com.jo.spectrumtracking.fragment.ChatRoomFragment;
import com.jo.spectrumtracking.fragment.ChatRoomListFragment;
import com.jo.spectrumtracking.fragment.CheckoutFragment;
import com.jo.spectrumtracking.fragment.EditGeofenceFragment;
import com.jo.spectrumtracking.fragment.GeofenceFragment;
import com.jo.spectrumtracking.fragment.MonitorFragment;
import com.jo.spectrumtracking.fragment.OrderServiceFragment;
import com.jo.spectrumtracking.fragment.OrderTrackerFragment;
import com.jo.spectrumtracking.fragment.ReplayFragment;
import com.jo.spectrumtracking.fragment.ReportsFragment;
import com.jo.spectrumtracking.fragment.SetShareDeviceFragment;
import com.jo.spectrumtracking.fragment.SettingFragment;
import com.jo.spectrumtracking.fragment.ShareFragment;
import com.jo.spectrumtracking.fragment.UpdateDriverInfoFragment;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.NetworkChangeReceiver;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Landmark;
import com.jo.spectrumtracking.model.Resp_Error;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.jo.spectrumtracking.tracking.GPSTracker;
import com.jo.spectrumtracking.tracking.GPSTracker2Plus;
import com.jo.spectrumtracking.twilio.chat.ChatClientManager;
import com.jo.spectrumtracking.twilio.chat.channels.ChannelManager;
import com.jo.spectrumtracking.twilio.chat.listeners.TaskCompletionListener;
import com.jo.spectrumtracking.widget.CustomSwitch;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener;
import com.mapbox.android.telemetry.TelemetryEnabler;
import com.mapbox.geojson.Point;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class MainActivity extends AppCompatActivity {
    private static final int PLAY_SERVICE_REQUEST = 9000;
    @BindView(R.id.menuMoreLayout)
    RelativeLayout menuMoreLayout;
    @BindView(R.id.backView)
    View backView;
    @BindView(R.id.icon_activate)
    ImageView icon_activate;
    @BindView(R.id.icon_monitor)
    ImageView icon_monitor;
    @BindView(R.id.icon_replay)
    ImageView icon_replay;
    @BindView(R.id.icon_order_service)
    ImageView icon_order_service;

    @BindView(R.id.back_report)
    LinearLayout back_report;
    @BindView(R.id.back_alarm)
    LinearLayout back_alarm;
    @BindView(R.id.back_update_driver)
    LinearLayout back_update_driver;
    @BindView(R.id.back_share)
    LinearLayout back_share;
    @BindView(R.id.back_geofence)
    LinearLayout back_geofence;
    @BindView(R.id.back_family)
    LinearLayout back_family;
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
    @BindView(R.id.label_activate)
    TextView label_activate;
    @BindView(R.id.label_monitor)
    TextView label_monitor;
    @BindView(R.id.label_replay)
    TextView label_reply;
    @BindView(R.id.label_order_service)
    TextView label_pay;
    int currentSelectedNavigationItemID = -1;

    public Fragment getFragment() {
        return fragment;
    }

    public Fragment fragment = null;
    public Fragment overlayFragment = null;
    public List<Fragment> fragmentStack = new ArrayList<>();

    int upload_times = 0;
    public int totalUnreadCount = 0;

    private static MainActivity instance;
    private ChatClientManager chatClientManager;

    private NetworkChangeReceiver networkChangeReceiver;
    private GPSTracker2Plus gpsTracker = null;
    private boolean mBound = false;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GPSTracker2Plus.LocalBinder binder = (GPSTracker2Plus.LocalBinder) service;
            gpsTracker = binder.getService();
            mBound = true;
            gpsTracker.requestLocationUpdates();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            gpsTracker = null;
            mBound = false;
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Location location = intent.getParcelableExtra(GPSTracker2Plus.EXTRA_LOCATION);

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Point user_point = Point.fromLngLat(longitude, latitude);
            GlobalConstant.user_point = user_point;

            if (bundle != null) {
                if (getFragment() instanceof MonitorFragment) {
                    ((MonitorFragment) getFragment()).onUpdateCurrentLocation();
                }
            }
        }
    };

    public String uploadImageAssetId = "";
    public String uploadImageTrackerId = "";

    public static MainActivity get() { return instance; }
    public ChatClientManager getChatClientManager() { return this.chatClientManager; }

    // 1; create phone tracker
    // 2: send locatio to server
    // 3: display the monitor page



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.instance = this;
        chatClientManager = new ChatClientManager(this.getBaseContext());

        if (!isGPSEnabled()) {
            Intent callGPSSettingIntent = new Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(callGPSSettingIntent);
        }

        if (!isLocationPermissionAccepted()) {
            this.requestLocationPermission();
        }

        SharedPreferences preferences = getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        boolean sAutoLockFlag = preferences.getBoolean("sAutoLock", false);
        if (sAutoLockFlag) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        setContentView(R.layout.activity_main);

        getPushToken();

        ButterKnife.bind(this);
        final int launch_count = preferences.getInt("launch_count", 0);

        ApiInterface apiInterface = ApiClient.getClient(MainActivity.this.getBaseContext()).create(ApiInterface.class);
        apiInterface.getUserInfo(GlobalConstant.X_CSRF_TOKEN).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                int code = response.code();
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
                Gson gson = gsonBuilder.create();

                if (code == 200) {
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());
                        GlobalConstant.app_user = object;
                        if (!GlobalConstant.app_user.has("email")) {
                            GlobalConstant.app_user.put("email", GlobalConstant.email);
                        }

                        int review_number = (object.has("appReview")) ? object.getInt("appReview") : 0;

                        // invite review after 5 times of use
                        if (review_number <= 2 && launch_count>4 && launch_count % 5 == 0 && !GlobalConstant.upload_state) {
                            rateDialog();
                        }

                        // if new user create phoneTracker
//                        if (!object.has("phoneTracker") || !object.getBoolean("phoneTracker")) {
//                            setPhoneTracker();
//                        } else {
//                            showMonitorFragment();
//                        }
                        doAuth();
                        showMonitorFragment();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.showShortToast(MainActivity.this.getBaseContext(), "try again later 1", true);
                    }

                } else {
                    Utils.showShortToast(MainActivity.this.getBaseContext(), "try again later 2", true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(MainActivity.this.getBaseContext(), getResources().getString(R.string.weak_cell_signal), true);
            }
        });
        editor.putInt("launch_count", launch_count + 1);
        editor.apply();

        TelemetryEnabler.updateTelemetryState(TelemetryEnabler.State.DISABLED);

        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        this.registerReceiver(networkChangeReceiver, intentFilter);
        this.bindService(new Intent(this, GPSTracker2Plus.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        checkAndStartLocationService();
        setDefaultColor();
        icon_monitor.setColorFilter(getResources().getColor(R.color.menu_select_color));
        label_monitor.setTextColor(getResources().getColor(R.color.menu_select_color));
        startInitChatting();
        if (Utils.getDistanceUnit().equals("miles")) {
            rbMiles.setChecked(true);
            rbKilometers.setChecked(false);
        } else {
            rbMiles.setChecked(false);
            rbKilometers.setChecked(true);
        }
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

    }
    @OnCheckedChanged(R.id.sAutoLockStatus)
    public void onChange() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("sAutoLock", sAutoLockStatus.isChecked());
        editor.apply();
        if (sAutoLockStatus.isChecked()) {
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
    public void setPhoneTracker() throws JSONException {
        ApiInterface apiInterface = ApiClient.getClient(getBaseContext()).create(ApiInterface.class);
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
                        Utils.showShortToast(getBaseContext(), "failed to create phone tracker 1", true);
                    }
                } else {
                    Utils.showShortToast(getBaseContext(), "failed to create phone tracker 2", true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(getBaseContext(), getResources().getString(R.string.weak_cell_signal), true);
            }
        });

    }
    private void setAsset(String trackerId) throws JSONException {

        ApiInterface apiInterface = ApiClient.getClient(getBaseContext()).create(ApiInterface.class);
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
                    Utils.showShortToast(getBaseContext(), "failed to create phone asset 1", true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(getBaseContext(), getResources().getString(R.string.weak_cell_signal), true);
            }
        });

    }
    private void setUserPhoneTracker() {
        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
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
                Utils.showShortToast(getBaseContext(), getResources().getString(R.string.weak_cell_signal), true);
            }
        });
    }
    private void sendLocationFor_10_Times(final double latitude, final double longitude) {

        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
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
    @Override
    protected void onResume() {
        super.onResume();
        if (!checkPlayServicesInstalled()){
            return;
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(GPSTracker2Plus.ACTION_BROADCAST));
    }
    @OnClick(R.id.btn_add_geofence)
    public void onAddGeofence() {
        setDefaultColor();
        back_geofence.setBackgroundResource(R.drawable.roundedbutton_green);

        EditGeofenceFragment fragment = EditGeofenceFragment.newInstance();

        if (this.fragment instanceof  MonitorFragment) {
            MonitorFragment monitorFragment = (MonitorFragment)this.fragment;
            fragment.originalCameraPosition = monitorFragment.mapboxMap.getCameraPosition();
        }

        this.pushFragment(fragment);
        menuMoreLayout.setVisibility(View.GONE);
        backView.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_share_location)
    public void onShareLocation() {
        setDefaultColor();
        back_share.setBackgroundResource(R.drawable.roundedbutton_green);

        ShareFragment fragment = ShareFragment.newInstance();
        this.pushFragment(fragment);
        menuMoreLayout.setVisibility(View.GONE);
        backView.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_family_circle)
    public void onFamilyCirlce() {
        setDefaultColor();
        back_family.setBackgroundResource(R.drawable.roundedbutton_green);
        SetShareDeviceFragment fragment = SetShareDeviceFragment.newInstance();

        this.pushFragment(fragment);
        menuMoreLayout.setVisibility(View.GONE);
        backView.setVisibility(View.GONE);
    }
    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        if (mBound) {
            this.unbindService(mServiceConnection);
             mBound = false;
        }

        if (networkChangeReceiver != null) {
            try {
                this.unregisterReceiver(networkChangeReceiver);
            } catch (Exception ex) {

            }
        }

        super.onDestroy();
    }

    private void doAuth() {
        ApiInterface apiInterface = ApiClient.getClient(MainActivity.this.getBaseContext()).create(ApiInterface.class);
        apiInterface.doAuth().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
                Gson gson = gsonBuilder.create();

                if (code == 200) {
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());
                        String message = "";
                        if (object.has("message")) {
                            message = object.getString("message");
                        }
                        if (object.has("landmarks")){
                            JSONArray landmarks = object.getJSONArray("landmarks");
                            for(int i=0;i<landmarks.length();i++){
                                JSONObject landmark = landmarks.getJSONObject(i);
                                GlobalConstant.landmarks.add(new Landmark(landmark.getString("name"),landmark.getString("type"),landmark.getString("lat"),landmark.getString("lng")));
                            }
                            Log.d("landmark", ""+GlobalConstant.landmarks.size());
                            MonitorFragment monitorFragment = (MonitorFragment)fragment;
                            monitorFragment.showLandmarks();
                        }
                        if (!message.isEmpty()) {
                            Utils.showSweetAlert(MainActivity.this, null, message, "Cancel", null, SweetAlertDialog.WARNING_TYPE, new Utils.OnSweetAlertListener() {
                                @Override
                                public void onConfirm() {
                                    HashMap<String, Object> body1 = new HashMap<>();
                                    body1.put("email", GlobalConstant.email);
                                    apiInterface.cleanMessage(body1).enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            // do nothing
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                                            // do nothing
                                        }
                                    });
                                }

                                @Override
                                public void onCancel() {

                                }
                            });
                        } else {

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(MainActivity.this.getBaseContext(), "try again later 1", true);
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void requestLocationPermission() {
        Utils.showSweetAlert(this, getString(R.string.location_permission), getString(R.string.location_permission_description), "Ok", "Cancel", SweetAlertDialog.WARNING_TYPE, new Utils.OnSweetAlertListener() {
            @Override
            public void onConfirm() {
                View parentLayout = findViewById(android.R.id.content);
                checkForPermissions(parentLayout);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void checkForPermissions(View view) {
        MultiplePermissionsListener snackbarMultiplePermissionsListener =
                SnackbarOnAnyDeniedMultiplePermissionsListener.Builder
                        .with(view, getResources().getString(R.string.we_need_permissions_access_to_track_you))
                        .withButton("Accept", this::checkForPermissions)
                        .withCallback(new Snackbar.Callback() {
                            @Override
                            public void onShown(Snackbar snackbar) {
                                // Event handler for when the given Snackbar is visible
                            }

                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                // Event handler for when the given Snackbar has been dismissed
                            }
                        })
                        .build();

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(snackbarMultiplePermissionsListener).check();
    }

    private boolean isLocationPermissionAccepted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if PlayServices are installed on the phone.
     */
    private boolean checkPlayServicesInstalled() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(MainActivity.this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(result)) {
                googleApiAvailability.getErrorDialog(MainActivity.this, result, PLAY_SERVICE_REQUEST).show();
                return false;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }



    //1: create phone tracker
    //2: set location to server
    //3: display map





    // LIAN send location to server.
    // should use the lastLocation of the phone here for faster service
    // LIAN 01/26/2020

    // FIXME. can we use last phone location here?


    public void rateDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_rate_panel);
        dialog.setCancelable(false);

        Button btnYes = dialog.findViewById(R.id.btn_rate);
        btnYes.setOnClickListener(v -> {
            ApiInterface apiInterface = ApiClient.getClient(MainActivity.this.getBaseContext()).create(ApiInterface.class);

            apiInterface.addReviewNumber(GlobalConstant.email).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    int code = response.code();
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
                    Gson gson = gsonBuilder.create();

                    if (code == 200) {
                        ResponseBody responseBody = response.body();
                    } else {
                        ResponseBody errorBody = response.errorBody();
                        Resp_Error error = null;
                        try {
                            error = gson.fromJson(errorBody.string(), Resp_Error.class);
                            Utils.showShortToast(MainActivity.this.getBaseContext(), error.getMessage(), true);
                        } catch (Exception e) {
                            e.printStackTrace();
//                            Utils.showShortToast(MainActivity.this.getBaseContext(), "response parse error");
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                    Utils.showShortToast(MainActivity.this.getBaseContext(), getResources().getString(R.string.weak_cell_signal), true);
                }
            });

            Uri uri = Uri.parse("market://details?id=com.jo.gps.spectrumtracking");
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.jo.gps.spectrumtracking")));
            }
            dialog.dismiss();
        });

        Button btnNo = dialog.findViewById(R.id.btn_later);
        btnNo.setOnClickListener(v -> dialog.dismiss());

        Button btnCancel = dialog.findViewById(R.id.btn_nothanks);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT; // this is where the magic happens
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lWindowParams);
    }

    public void showMonitorFragment() {
//        fragment = MonitorFragment.newInstance();
        Fragment fragment = MonitorFragment.newInstance();
        fragmentStack.clear();
        fragmentStack.add(fragment);
        setFragment();
    }

    private Boolean exit = false;

    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(this, getResources().getString(R.string.press_back_agin_to_exit), Toast.LENGTH_SHORT).show();
            exit = true;
            int BACK_CHARGE_LENGTH = 1500;
            new Handler().postDelayed(() -> exit = false, BACK_CHARGE_LENGTH);
        }
    }

    public void setFragment() {
        if (fragmentStack.size() > 0) {
            Fragment firstFragment = fragmentStack.get(0);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, firstFragment);

            for (int i=1; i<fragmentStack.size(); i++) {
                Fragment f = fragmentStack.get(i);
                ft.add(R.id.content_frame, f);
            }

            ft.commitAllowingStateLoss();

            fragment = firstFragment;
        }
    }

    public void pushFragment(Fragment fragment) {
        int index = -1;
        for (int i=0; i<fragmentStack.size(); i++) {
            if (fragmentStack.get(i).getClass().equals(fragment.getClass())) {
                index = i;
                break;
            }
        }

        if (index >= fragmentStack.size() - 1) {
            return;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (index >= 0) {
            for (int i=fragmentStack.size() - 1; i>index; i--) {
                ft.remove(fragmentStack.get(i));
                fragmentStack.remove(i);
            }
        } else {
            fragmentStack.add(fragment);
            ft.add(R.id.content_frame, fragment);
        }

        ft.commitAllowingStateLoss();
    }

    public void popFragment() {
        if (fragmentStack.size() < 2) {
            return;
        }

        int index = fragmentStack.size() - 1;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(fragmentStack.get(index));
        fragmentStack.remove(index);
        ft.commitAllowingStateLoss();
    }

    public void replaceLastFragment(Fragment fragment) {
        if (fragmentStack.size() > 1) {
            popFragment();
        }

        pushFragment(fragment);
    }

    private void setDefaultColor() {
        icon_activate.setColorFilter(getResources().getColor(R.color.menu_normal_color));
        icon_monitor.setColorFilter(getResources().getColor(R.color.menu_normal_color));
        icon_order_service.setColorFilter(getResources().getColor(R.color.menu_normal_color));
        icon_replay.setColorFilter(getResources().getColor(R.color.menu_normal_color));
        label_activate.setTextColor(getResources().getColor(R.color.menu_normal_color));
        label_monitor.setTextColor(getResources().getColor(R.color.menu_normal_color));
        label_reply.setTextColor(getResources().getColor(R.color.menu_normal_color));
        label_pay.setTextColor(getResources().getColor(R.color.menu_normal_color));
        back_report.setBackgroundResource(R.drawable.roundedbutton_blue);
        back_alarm.setBackgroundResource(R.drawable.roundedbutton_blue);
        back_update_driver.setBackgroundResource(R.drawable.roundedbutton_blue);
        back_share.setBackgroundResource(R.drawable.roundedbutton_blue);
        back_geofence.setBackgroundResource(R.drawable.roundedbutton_blue);
        back_family.setBackgroundResource(R.drawable.roundedbutton_blue);
    }

    public void onChat() {
        setDefaultColor();
        ChatRoomListFragment fragment = ChatRoomListFragment.newInstance(new ArrayList<>(GlobalConstant.AllTrackerList));
        this.pushFragment(fragment);
    }
    @OnClick(R.id.btnBattery)
    public void onBattery(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isBatteryOptimizationDisabled()) {
                Toast.makeText(getApplicationContext(), getString(R.string.no_need_to_do_it_you_already_disabled_it), Toast.LENGTH_SHORT).show();
            } else {
                Intent myIntent = new Intent();
                myIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(myIntent);
            }
        } else {
            doBatteryOptDisable();
//                Toast.makeText(getApplicationContext(), "Your Android version is lower that 6, you do not need to do that.", Toast.LENGTH_SHORT).show();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isBatteryOptimizationDisabled() {
        String packageName = getApplicationContext().getPackageName();
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        assert pm != null;
        return pm.isIgnoringBatteryOptimizations(packageName);
    }
    public void doBatteryOptDisable() {
        Dialog alertDialog = new Dialog(getBaseContext());
        alertDialog.setContentView(R.layout.dialog_battery_optimization);
        TextView btnDisableBatteryOpt = alertDialog.findViewById(R.id.btn_disable_optimization);
        TextView btnShowMeHow = alertDialog.findViewById(R.id.btn_show_me_how);
        TextView btnSkip = alertDialog.findViewById(R.id.btn_skip);

        btnDisableBatteryOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent();
                myIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(myIntent);
//                startActivityForResult(myIntent, REQUEST_CODE);
            }
        });

        btnShowMeHow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog showD = new Dialog(getBaseContext());
                showD.setContentView(R.layout.dialog_battery_disable_alert);
                showD.show();
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        alertDialog.show();
    }

    @OnClick(R.id.btn_monitor)
    public void onMonitor() {
        setDefaultColor();
        icon_monitor.setColorFilter(getResources().getColor(R.color.menu_select_color));
        label_monitor.setTextColor(getResources().getColor(R.color.menu_select_color));
        if (fragmentStack.get(0) instanceof MonitorFragment) {
            pushFragment(fragmentStack.get(0));
        } else {
            fragment = MonitorFragment.newInstance();
            fragmentStack.clear();
            fragmentStack.add(fragment);
            setFragment();
        }
    }

    @OnClick(R.id.btn_replay)
    public void onReplay() {
        setDefaultColor();
        icon_replay.setColorFilter(getResources().getColor(R.color.menu_select_color));
        label_reply.setTextColor(getResources().getColor(R.color.menu_select_color));
        if (fragmentStack.get(0) instanceof ReplayFragment) {
            pushFragment(fragmentStack.get(0));
        } else {
            fragment = ReplayFragment.newInstance();
            fragmentStack.clear();
            fragmentStack.add(fragment);
            setFragment();
        }
    }

    @OnClick(R.id.btn_report)
    public void onReport() {
        setDefaultColor();
        back_report.setBackgroundResource(R.drawable.roundedbutton_green);
        menuMoreLayout.setVisibility(View.GONE);
        backView.setVisibility(View.GONE);
        if (fragmentStack.get(0) instanceof ReportsFragment) {
            pushFragment(fragmentStack.get(0));
        } else {
            fragment = ReportsFragment.newInstance();
            fragmentStack.clear();
            fragmentStack.add(fragment);
            setFragment();
        }
    }

    @OnClick(R.id.btn_alarms)
    public void onAlarms() {
        setDefaultColor();
        back_alarm.setBackgroundResource(R.drawable.roundedbutton_green);
        menuMoreLayout.setVisibility(View.GONE);
        backView.setVisibility(View.GONE);

        if (fragmentStack.get(0) instanceof AlarmFragment) {
            pushFragment(fragmentStack.get(0));
        } else {
            fragment = AlarmFragment.newInstance();
            fragmentStack.clear();
            fragmentStack.add(fragment);
            setFragment();
        }
    }

    @OnClick(R.id.btn_geofence)
    public void onGeofence() {
        setDefaultColor();

        menuMoreLayout.setVisibility(View.GONE);
        backView.setVisibility(View.GONE);

        if (fragmentStack.get(0) instanceof GeofenceFragment) {
            pushFragment(fragmentStack.get(0));
        } else {
            fragment = GeofenceFragment.newInstance();
            fragmentStack.clear();
            fragmentStack.add(fragment);
            setFragment();
        }
    }

//    @OnClick(R.id.btn_share_tracker)
    public void onShareTracker() {
        setDefaultColor();

        menuMoreLayout.setVisibility(View.GONE);
        backView.setVisibility(View.GONE);

        if (fragmentStack.get(0) instanceof ShareFragment) {
            pushFragment(fragmentStack.get(0));
        } else {
            fragment = ShareFragment.newInstance();
            fragmentStack.clear();
            fragmentStack.add(fragment);
            setFragment();
        }
    }

//    @OnClick(R.id.btn_set_shareDevice)
//    public void onSetShareDevice() {
//        setDefaultColor();
//        icon_set_share.setColorFilter(getResources().getColor(R.color.menu_select_color));
//
//        menuMoreLayout.setVisibility(View.GONE);
//        backView.setVisibility(View.GONE);
//
//        if (fragmentStack.get(0) instanceof SetShareDeviceFragment) {
//            pushFragment(fragmentStack.get(0));
//        } else {
//            fragment = SetShareDeviceFragment.newInstance();
//            fragmentStack.clear();
//            fragmentStack.add(fragment);
//            setFragment();
//        }
//    }

    @OnClick(R.id.btn_vehicle_info)
    public void onUpdateDriverInfo() {
        setDefaultColor();
        back_update_driver.setBackgroundResource(R.drawable.roundedbutton_green);
        menuMoreLayout.setVisibility(View.GONE);
        backView.setVisibility(View.GONE);

        if (fragmentStack.get(0) instanceof UpdateDriverInfoFragment) {
            pushFragment(fragmentStack.get(0));
        } else {
            fragment = UpdateDriverInfoFragment.newInstance();
            fragmentStack.clear();
            fragmentStack.add(fragment);
            setFragment();
        }
    }

    @OnClick(R.id.btn_order_service)
    public void onOrderService() {
        setDefaultColor();
        icon_order_service.setColorFilter(getResources().getColor(R.color.menu_select_color));
        label_pay.setTextColor(getResources().getColor(R.color.menu_select_color));
        if (fragmentStack.get(0) instanceof OrderServiceFragment) {
            pushFragment(fragmentStack.get(0));
        } else {
            fragment = OrderServiceFragment.newInstance();
            fragmentStack.clear();
            fragmentStack.add(fragment);
            setFragment();
        }
    }

//    @OnClick(R.id.btn_order_tracker)
//    public void onOrderTracker() {
//        setDefaultColor();
//        icon_order_tracker.setColorFilter(getResources().getColor(R.color.menu_select_color));
//
//        menuMoreLayout.setVisibility(View.GONE);
//        backView.setVisibility(View.GONE);
//
//        if (fragmentStack.get(0) instanceof OrderTrackerFragment) {
//            pushFragment(fragmentStack.get(0));
//        } else {
//            fragment = OrderTrackerFragment.newInstance();
//            fragmentStack.clear();
//            fragmentStack.add(fragment);
//            setFragment();
//        }
//    }

    @OnClick(R.id.btn_activate)
    public void onActivate() {
        setDefaultColor();
        icon_activate.setColorFilter(getResources().getColor(R.color.menu_select_color));
        label_activate.setTextColor(getResources().getColor(R.color.menu_select_color));
        if (fragmentStack.get(0) instanceof ActivateTrackerFragment) {
            pushFragment(fragmentStack.get(0));
        } else {
            fragment = ActivateTrackerFragment.newInstance();
            fragmentStack.clear();
            fragmentStack.add(fragment);
            setFragment();
        }
    }
    public void onCheckout() {
        menuMoreLayout.setVisibility(View.GONE);
        backView.setVisibility(View.GONE);

        if (fragmentStack.get(0) instanceof CheckoutFragment) {
            pushFragment(fragmentStack.get(0));
        } else {
            fragment = CheckoutFragment.newInstance("OrderServiceFragment");
            fragmentStack.clear();
            fragmentStack.add(fragment);
            setFragment();
        }
    }
    @OnClick(R.id.btn_contact)
    public void onContact() {
        setDefaultColor();
        String url = "https://spectrumtracking.com/contact.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @OnClick(R.id.btn_faq)
    public void onFAQ() {
        setDefaultColor();
        String url = "https://spectrumtracking.com/faq.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @OnClick(R.id.btn_more)
    public void onMore() {
        menuMoreLayout.setVisibility(View.VISIBLE);
        backView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_menu_close)
    public void onClose() {
        menuMoreLayout.setVisibility(View.GONE);
        backView.setVisibility(View.GONE);
    }

    @SuppressWarnings("deprecation")
    public void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
        GlobalConstant.app_user = null;
    }

    @OnClick(R.id.btn_logout)
    public void onLogout() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_logout_select);
        //dialog.setTitle("Forgot password");
        dialog.setCancelable(false);

        Button btnYes = dialog.findViewById(R.id.btn_yes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("spectrum", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("is_remember", true);
                editor.commit();
                dialog.dismiss();
                clearCookies(MainActivity.this.getBaseContext());
                MainActivity.this.startActivity(new Intent(MainActivity.this, LoginActivity.class));
                MainActivity.this.finish();
            }
        });

        Button btnNo = dialog.findViewById(R.id.btn_no);
        btnNo.setOnClickListener(v -> {
            SharedPreferences preferences = getSharedPreferences("spectrum", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("is_remember", false);
            editor.commit();
            dialog.dismiss();
            clearCookies(MainActivity.this.getBaseContext());
            MainActivity.this.startActivity(new Intent(MainActivity.this, LoginActivity.class));
            MainActivity.this.finish();
        });

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            menuMoreLayout.setVisibility(View.GONE);
            backView.setVisibility(View.GONE);
        });

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT; // this is where the magic happens
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lWindowParams);


        SharedPreferences preferences = getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("is_login", false);
        editor.apply();
        return;
    }

    public void startInitChatting() {
        chatClientManager.connectClient(new TaskCompletionListener<Void, String>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Chat", "ChatClientManager connectClient success");
                ChannelManager.getInstance().populateUserChannels(channels -> {
//                    for (Channel channel : channels) {
//                        channel.destroy(null);
//                    }
//                    initalizeAllChattingChannels();
                });
            }

            @Override
            public void onError(String s) {
                Log.d("Chat", s);
            }
        });
    }

    public void initalizeAllChattingChannels() {
        for (Resp_Tracker tracker : GlobalConstant.AllTrackerList) {
            SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
            String myId = preferences.getString("username", "");
            String partnerId = tracker.getSpectrumId();

            if (myId.equals(partnerId)) {
                continue;
            }

            String channelName = ChannelManager.getInstance().getChannelName(partnerId);
            ChannelManager.getInstance().joinOrCreatePrivateChannelWithName(channelName, partnerId, null);
        }
    }

    public void updatedUnreadCount() {
        Long unreadCount = 0L;
        for (Long count : ChannelManager.getInstance().unreadCountMap.values()) {
            unreadCount += count;
        }

        if (totalUnreadCount < unreadCount && !(overlayFragment instanceof ChatRoomFragment)) {
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        totalUnreadCount = unreadCount.intValue();

        if(fragment instanceof MonitorFragment) {
            MonitorFragment monitorFragment = (MonitorFragment)fragment;
            monitorFragment.updateUnreadCount();
        }

        if (overlayFragment instanceof ChatRoomListFragment) {
            ChatRoomListFragment chatRoomListFragment = (ChatRoomListFragment)overlayFragment;
            chatRoomListFragment.updateList();
        }
    }

    public void checkChatInvitation() {
        if(fragment instanceof MonitorFragment) {
            MonitorFragment monitorFragment = (MonitorFragment)fragment;
            monitorFragment.checkChatInvitation();
        }

        if (overlayFragment instanceof ChatRoomListFragment) {
            ChatRoomListFragment chatRoomListFragment = (ChatRoomListFragment)overlayFragment;
            chatRoomListFragment.updateList();
        }
    }

    public Point getLocationFromTracker() {
        if (gpsTracker != null) {
            if(gpsTracker.getLocation()==null) {
                return null;
            }
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            Point user_point = Point.fromLngLat(longitude, latitude);
            GlobalConstant.user_point = user_point;

            return user_point;
        }

        return null;
    }

    void checkAndStartLocationService() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("spectrum", Context.MODE_PRIVATE);

        if(preferences.getBoolean("sPhoneTracking",false)) {
            int upload_delay=preferences.getInt("upload_delay",20);
            final Handler handler = new Handler();
            if(isMyServiceRunning(GPSTracker2Plus.class)) {
                Intent intent= new Intent(getApplicationContext(), GPSTracker2Plus.class);
                this.stopService(intent);

                handler.postDelayed(() -> {
                    Intent intent1 = new Intent(getApplicationContext(), GPSTracker2Plus.class);
                    intent1.putExtra("flag", true);
                    intent1.putExtra("delay", upload_delay);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent1);
                    } else {
                        startService(intent1);
                    }
                }, upload_delay);
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void pickImage(String assetId, String trackerId) {
        this.uploadImageAssetId = assetId;
        this.uploadImageTrackerId = trackerId;
        ImagePicker.Companion.with(this).compress(1024).maxResultSize(256, 256).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (uploadImageAssetId == "" || uploadImageTrackerId == "") return;

            Uri fileUri = data.getData();
            File file = ImagePicker.Companion.getFile(data);

            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
                if (bitmap != null) bitmap = Utils.getResizedBitmap(bitmap, 256);
                if (fragment instanceof MonitorFragment) {
                    ((MonitorFragment)fragment).uploadDriverPhoto(bitmap, uploadImageAssetId, uploadImageTrackerId);
                    this.uploadImageAssetId = "";
                    this.uploadImageTrackerId = "";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (resultCode == ImagePicker.RESULT_ERROR) {

        } else {

        }
    }

    public void getPushToken(){
        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w("FCM Token", "Fetching FCM registration token failed", task.getException());
//                                pushToken.setText("Fetching FCM registration token failed : "+ task.getException());
                                return;
                            }

                            // Get new FCM registration token
                            String token = task.getResult();

                            SharedPreferences preferences = getSharedPreferences("spectrum", Context.MODE_PRIVATE);
                            String email = preferences.getString("username", "");

                            if (!email.isEmpty() && !token.isEmpty()) {
                                ApiInterface apiInterface = ApiClient.getClient(MainActivity.this.getBaseContext()).create(ApiInterface.class);

                                HashMap<String, Object> body = new HashMap<>();
                                body.put("email", email);
                                body.put("pushToken", token);

                                apiInterface.postFirebaseToken(body).enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        Log.d("Firebase Token", "Successfully submit firebase token");

                                        FirebaseMessaging.getInstance().subscribeToTopic("Teset").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("FCM Token", "Successfully subscribed topic");
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        Log.d("Firebase Token", "Failed to submit firebase token");
                                    }
                                });
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
