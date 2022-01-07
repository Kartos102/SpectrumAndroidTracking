package com.jo.spectrumtracking.tracking;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.jo.spectrumtracking.activity.BatteryOptimizationDisableActivity;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.mapbox.geojson.Point;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GPSTrackerPlus extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // Get Class Name
    private static String TAG = GPSTracker.class.getName();
    private Context mContext;
    // Flag for GPS Status
    boolean isGPSEnabled = false;
    // Flag for network status
    boolean isNetworkEnabled = false;
    // Flag for GPS Tracking is enabled
    boolean isGPSTrackingEnabled = false;
    Location location;
    public double latitude;
    public double longitude;
    private double altitude;
    // The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_BW_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static long TIME_BW_UPDATES = 1000 * 60; // 30 seconds
    private static long FASTEST_TIME_BW_UPDATES = 1000 * 5; // 5 seconds
    // Declaring a Location Manager
    protected LocationManager locationManager;
    private long lastUpdate = 0;
    private Location prev_location = null;
    private int accStatus = 1;
    private Point user_point;
    private String user_email = "";

    GoogleApiClient googleApiClient;

    FusedLocationProviderClient fusedLocationClient;
    private Activity activity=new Activity();

    /**
     * To run the Service outside the UI Thread, for long-lasting operations.
     */
    private AtomicBoolean working = new AtomicBoolean(true);
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (working.get()) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                String startTimeString = sdf.format(new Date());

                HashMap<String, Object> body = new HashMap<>();
                body.put("reportingId", user_email);
                body.put("dateTime", startTimeString);
                if (GlobalConstant.user_point == null || (GlobalConstant.user_point.latitude() == 0.0 && GlobalConstant.user_point.longitude() == 0.0)) {
                    body.put("lat", 0.0);
                    body.put("lng", 0.0);
                } else {
                    body.put("lat", GlobalConstant.user_point.latitude());
                    body.put("lng", GlobalConstant.user_point.longitude());
                }
                body.put("ACCStatus", 0);
                body.put("currentTripMileage", 0); // Meter to miles
                body.put("speedInMph", 0);
                body.put("trackerModel", "phone");
                body.put("lastAlert", "");

                ApiInterface apiInterface = ApiClient.getClient(mContext).create(ApiInterface.class);
                apiInterface.postUserLocation("33bedd43-209c-4025-b157-d7c6df1211e3", body)
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                int code = response.code();
                                if (code == 201) {
                                    Log.e(TAG, "postUserLocation, onResponse: success!");
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                t.printStackTrace();
                            }
                        });
            }
        }
    };

    /**
     * Service Constructors
     */
    public GPSTrackerPlus() {
    }

    public GPSTrackerPlus(Context context) {
        this.mContext = context;
        prepareDeviceLocation();
    }

    /**
     * onCreate will only ever be called once per instantiated object
     */
    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = getApplicationContext();
        startMyOwnForeground();
//        new Thread(runnable).start();
    }

    /**
     * onStartCommand is called every time a client starts the service using startService(Intent intent)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.mContext = getApplicationContext();
        if (intent != null) {
            boolean flag = intent.getBooleanExtra("flag", false);
            //TIME_BW_UPDATES = intent.getIntExtra("delay", 1) * 1000;
            //TIME_BW_UPDATES = intent.getIntExtra("delay", 1) * 1000;
            if (flag) user_email = "";
        }

        callChecking();

        return START_STICKY; // For automatic restarting of service.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        working.set(false);
        stopLocationUpdates();
    }

    /**
     * Make the service running on the foreground.
     */
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.jo.spectrum";
        String channelName = "Spectrum GPS Tracking Service";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Spectrum is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(2, notification);
    }

    /**
     * GPSTracker latitude, longitude, altitude and speed
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public double getSpeed() {
        double speed = 0.0;
        if (location != null) {
            speed = location.getSpeed();
        }

        return speed;
    }

    public double getAltitude() {
        if (location != null) {
            altitude = location.getAltitude();
        }
        return altitude;
    }

    /**
     * Service implementation methods
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * NEW WAY TO TRACK LOCATION USING GOOGLE API.
     */
    public void prepareDeviceLocation() {
        locationManager = (LocationManager) mContext.getSystemService(Service.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            showSettingsAlert();
        } else {
            GlobalConstant.cannotEnableGPS=false;
            getGoogleApiClient();
        }
    }
    // FIXME: java.lang.IllegalStateException on the CONTEXT
    /**
     * Show Location Settings Alert
     */
    private void showSettingsAlert() {
        try{
            if(getApplicationContext()==null) {
                GlobalConstant.cannotEnableGPS=true;
                return;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            GlobalConstant.cannotEnableGPS=true;
            return;
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());
        dialog.setTitle("Enable GPS");
        dialog.setMessage("Please enable GPS on your device!");
        dialog.setPositiveButton("OK", (dialogInterface, i) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });
        dialog.setNegativeButton("Cancel",
                (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.show();
    }

    /**
     * Prepare GoogleApiClient to get Location Data
     */
    private void getGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        googleApiClient.connect();
    }

    /**
     * Try to get my current location by GPS or Network Provider
     */
    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            List<String> providers = locationManager.getProviders(true);
            Location bestLocation = null;
            for (String provider : providers) {
                Location l = locationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }

            if (bestLocation != null) {
                isGPSEnabled = true;
                isGPSTrackingEnabled = true;
                location = bestLocation;
                updateGPSCoordinates();
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Impossible to connect to LocationManager", e);
        }
    }

    /**
     * Update GPSTracker latitude and longitude
     */
    public void updateGPSCoordinates() {
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    /**
     * Start Location Updates
     */
    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(TIME_BW_UPDATES); //Send location request every 60 Secs
        //locationRequest.setSmallestDisplacement(MIN_DISTANCE_BW_UPDATES); //Send location request every when distance more than 10 meters
        locationRequest.setFastestInterval(FASTEST_TIME_BW_UPDATES);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        fusedLocationClient=LocationServices.getFusedLocationProviderClient(this.mContext);


    }

    /**
     * Stop Location Updates
     */
    private void stopLocationUpdates() {
        if (googleApiClient != null) {
            if (googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                googleApiClient.disconnect();
            }
        }
    }

    /**
     * GoogleApiClient implementation methods
     * onConnected Get the Current Location
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        //if (location != null) {
         //   Toast.makeText(mContext, "Current Locations is: Lat/ " + location.getLatitude() + " , Long/ " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        //}
        LocationManager lm;
        try{
            lm = (LocationManager)this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        catch (Exception ex){
            ex.printStackTrace();
            GlobalConstant.cannotEnableGPS=true;
            return;
        }
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}


        if(gps_enabled)
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * LocationListener implementation methods
     */
    @Override
    public void onLocationChanged(Location location) {
        if (lastUpdate != 0 && System.currentTimeMillis() - lastUpdate < 2000) {
            return;
        }
        ApiInterface apiInterface = ApiClient.getClient(mContext).create(ApiInterface.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startTimeString = sdf.format(new Date());
        user_point = Point.fromLngLat(location.getLongitude(), location.getLatitude());
        double speed = location.getSpeed() * 2.2369;//meter to mile
        if (speed == 0) accStatus = 0;
        else accStatus = 1;
        Log.d("locationspeed", "" + speed);
        GlobalConstant.user_point = user_point;
        HashMap<String, Object> body = new HashMap<>();
        if (user_email.equals("") || user_email == null) {
            SharedPreferences preferences = mContext.getSharedPreferences("spectrum", Context.MODE_PRIVATE);
            user_email = preferences.getString("username", "");
        }
        double distance = 0;
        if (prev_location != null) {
            distance = prev_location.distanceTo(location) * 2.2369;
            prev_location=new Location("Previous Location");
            prev_location.setLatitude(user_point.latitude());
            prev_location.setLongitude(user_point.longitude());
        }

        if((distance/2.2369)<MIN_DISTANCE_BW_UPDATES && prev_location!=null) {
            prev_location=new Location("Previous Location");
            prev_location.setLatitude(user_point.latitude());
            prev_location.setLongitude(user_point.longitude());
            return;
        }


        lastUpdate = System.currentTimeMillis();
        body.put("reportingId", user_email);
        body.put("dateTime", startTimeString);
        body.put("lat", this.user_point.latitude());
        body.put("lng", this.user_point.longitude());
        body.put("ACCStatus", 1);
        body.put("currentTripMileage", distance);
        body.put("speedInMph", speed);
        body.put("trackerModel", "phone");
        body.put("lastAlert", "");
        apiInterface.postUserLocation("33bedd43-209c-4025-b157-d7c6df1211e3", body)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        int code = response.code();
                        if (code == 201) {
                            Log.e(TAG, "Post a new location is: Lat/ " + location.getLatitude() + " , Long/ " + location.getLongitude());
                            Log.e(TAG, "Interval: " + TIME_BW_UPDATES);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        t.printStackTrace();

                    }
                });
        prev_location = location;
    }

    /** Check if GPS is enabled and Loaction permission is granted */
    private void callChecking() {
        if (!isGPSEnabled()) {
            showGPSDisabledAlert();
        }

        if (!isLocationPermissionAccepted()) {
            if(!GlobalConstant.cannotEnableGPS){
                try{
                    Intent intent=new Intent(this, BatteryOptimizationDisableActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                catch (Exception ex){
                    ex.printStackTrace();
                    GlobalConstant.cannotEnableGPS=true;
                }
            }

        }
    }

    private boolean isLocationPermissionAccepted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showGPSDisabledAlert() {
        if(GlobalConstant.cannotEnableGPS) return;
        Utils.showSweetAlert(this, null, "GPS is disabled in your device. Would you like to enable it?", "Goto Settings Page To Enable GPS", null, SweetAlertDialog.WARNING_TYPE, new Utils.OnSweetAlertListener() {
            @Override
            public void onConfirm() {
                Intent callGPSSettingIntent = new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(callGPSSettingIntent);
            }

            @Override
            public void onCancel() {

            }
        });
    }
}
