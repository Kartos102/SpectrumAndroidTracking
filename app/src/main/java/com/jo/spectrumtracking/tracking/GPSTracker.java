package com.jo.spectrumtracking.tracking;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.mapbox.geojson.Point;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GPSTracker extends Service implements LocationListener {

    // Get Class Name
    private static String TAG = GPSTracker.class.getName();
    private Context mContext;
    // flag for GPS Status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    // flag for GPS Tracking is enabled
    boolean isGPSTrackingEnabled = false;
    Location location;
    public double latitude;
    public double longitude;
    private double altitude;
    // How many Geocoder should return our GPSTracker
    int geocoderMaxResults = 1;
    // The minimum distance to change updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static long MIN_TIME_BW_UPDATES = 1000 * 30; // 1 second
    // Declaring a Location Manager
    protected LocationManager locationManager;
    // Store LocationManager.GPS_PROVIDER or LocationManager.NETWORK_PROVIDER information
    private String provider_info;
    private String user_email = "";
    private long lastUpdate = 0;
    private Boolean upload_flag = false;
    private Location prev_location = null;
    private SensorManager sensorManager;
    private Intent locationIntent;
    private int accStatus = 1;
    private int delay = 10;
    private Point user_point;
    private boolean stopFlag = false;
    private final Handler mHandler = new Handler();

    public GPSTracker() {
        // locationIntent=new Intent("LocationRequest");
        // this.mContext=getApplicationContext();
    }

    public GPSTracker(Context context) {
        this.mContext = context;
        locationIntent = new Intent("LocationRequest");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = getApplicationContext();
        locationIntent = new Intent("LocationRequest");
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
//        sensorManager.registerListener(this,
//                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//                SensorManager.SENSOR_DELAY_NORMAL);
        if (intent != null) {
            Boolean flag = intent.getBooleanExtra("flag", false);
            MIN_TIME_BW_UPDATES = intent.getIntExtra("delay", 30) * 1000;
            //Toast.makeText(getApplicationContext(), ""+delay, Toast.LENGTH_SHORT).show();
            if (flag) user_email = "";
        }
        this.mContext = getApplicationContext();
        stopUsingGPS();
        locationManager = null;
        getLocation();
//        mHandler.removeCallbacks(timerRunnable);
//        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ApiInterface apiInterface = ApiClient.getClient(mContext).create(ApiInterface.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String startTimeString = sdf.format(new Date());
        if (user_email.equals("") || user_email == null) {
            SharedPreferences preferences = mContext.getSharedPreferences("spectrum", Context.MODE_PRIVATE);
            user_email = preferences.getString("username", "");
        }
        HashMap<String, Object> body = new HashMap<>();
        body.put("reportingId", user_email);
        body.put("dateTime", startTimeString);

        if (GlobalConstant.user_point == null) {
            body.put("lat", 0);
            body.put("lng", 0);
        } else {
            body.put("lat", GlobalConstant.user_point.latitude());
            body.put("lng", GlobalConstant.user_point.longitude());
        }

        body.put("ACCStatus", 0);
        body.put("currentTripMileage", 0);////////////////meter to miles
        body.put("speedInMph", 0);
        body.put("trackerModel", "phone");
        body.put("lastAlert", "");
        apiInterface.postUserLocation("33bedd43-209c-4025-b157-d7c6df1211e3", body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 201) {
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

            }
        });
        stopUsingGPS();
//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction("restartservice");
//        broadcastIntent.setClass(this, Restarter.class);
//        this.sendBroadcast(broadcastIntent);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "com.jo.spectrum.tracking";
        String channelName = "GPSTracker Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Spectrum is getting location in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    /**
     * Try to get my current location by GPS or Network Provider
     */
    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            if (locationManager == null) {
                locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

                isGPSEnabled = locationManager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                isNetworkEnabled = locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                if (!isGPSEnabled && isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                } else if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                } else {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                }

            }


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
            Log.e(TAG, "Impossible to connect to LocationManager", e);
        }
    }

    //    private void postUserLocation() {
    //        ApiInterface apiInterface = ApiClient.getClient(getApplicationContext()).create(ApiInterface.class);
    //        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    //        String startTimeString = sdf.format(new Date());
    //
    //        //Toast.makeText(getApplicationContext(), startTimeString, Toast.LENGTH_SHORT).show();
    //        getLocation();
    //        double  latitude = getLatitude();
    //        double  longitude = getLongitude();
    //        double speed = getSpeed();
    //
    //        if(lastUpdate < System.currentTimeMillis() - 1000 * 60 && accStatus==1) {///////////////////////start stop
    //            upload_flag = false;
    //            accStatus = 0;
    //            stopFlag = true;
    //        }
    //
    //        double distanceInMeters = 0.0;
    //        if(user_point != null) {
    //            Location loc1 = new Location("");
    //            loc1.setLatitude(user_point.latitude());
    //            loc1.setLongitude(user_point.longitude());
    //
    //            Location loc2 = new Location("");
    //            loc2.setLatitude(latitude);
    //            loc2.setLongitude(longitude);
    //
    //            distanceInMeters = loc1.distanceTo(loc2);
    //            if(distanceInMeters < 5 && accStatus==1) return;///////////////////////////////////// distance < 5: drifting
    //            if(!upload_flag && distanceInMeters < 10 && accStatus==1){/////////////////////////////////////////// drifting & motion
    //                return;
    //            }
    //        }
    //        if((upload_flag && distanceInMeters > 5 ) || distanceInMeters > 10)accStatus = 1;
    //        if(!stopFlag && accStatus == 0){////////////////////////////////continue stop
    //            return;
    //        }
    //        user_point = Point.fromLngLat(longitude, latitude);
    //        lastUpdate = System.currentTimeMillis();
    //       // if(country != null && country.equals("United States")){
    //        speed = speed * 2.2369;//meter to mile
    //
    //        //Utils.showShortToast(MonitorFragment.this.getContext(), country +":"+ speed);
    //        GlobalConstant.user_point = user_point;
    //        HashMap<String, Object> body = new HashMap<>();
    //        String email = "";
    //
    //        if(user_email.equals("")){
    //            SharedPreferences preferences = getSharedPreferences("spectrum", Context.MODE_PRIVATE);
    //            user_email = preferences.getString("username", "");
    //        }
    //        body.put("reportingId", user_email);
    //        body.put("dateTime", startTimeString);
    //        body.put("lat", this.user_point.latitude());
    //        body.put("lng", this.user_point.longitude());
    //        body.put("ACCStatus", accStatus);
    //        body.put("currentTripMileage",distanceInMeters * 2.2369);////////////////meter to miles
    //        body.put("speedInMph", speed);
    //        body.put("trackerModel", "phone");
    //        body.put("lastAlert", "");
    //        final String result = user_email;
    //        apiInterface.postUserLocation("33bedd43-209c-4025-b157-d7c6df1211e3",body).enqueue(new Callback<ResponseBody>() {
    //            @Override
    //            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
    //                int code = response.code();
    //                if (code == 201) {
    //                   // Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
    //                }
    //            }
    //            @Override
    //            public void onFailure(Call<ResponseBody> call, Throwable t) {
    //                t.printStackTrace();
    //
    //            }
    //        });
    //        stopFlag = false;
    //
    //    }

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
     * GPSTracker latitude getter and setter
     *
     * @return latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        return latitude;
    }

    public double getSpeed() {
        double speed = 0.0;
        if (location != null) {
            speed = location.getSpeed();
        }

        return speed;
    }

    /**
     * GPSTracker longitude getter and setter
     *
     * @return
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        return longitude;
    }

    public double getAltitude() {
        if (location != null) {
            altitude = location.getAltitude();
        }

        return altitude;
    }

    /**
     * GPSTracker isGPSTrackingEnabled getter.
     * Check GPS/wifi is enabled
     */
    public boolean getIsGPSTrackingEnabled() {

        return this.isGPSTrackingEnabled;
    }

    /**
     * Stop using GPS listener
     * Calling this method will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to show settings alert dialog
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        //Setting Dialog Title
        alertDialog.setTitle("GPSAlertDialog");

        //Setting Dialog Message
        alertDialog.setMessage("GPS Setting");

        //On Pressing Setting button
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        //On pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    /**
     * Get list of address by latitude and longitude
     *
     * @return null or List<Address>
     */
    public List<Address> getGeocoderAddress(double lat, double lon) {
        if (location != null) {

            Geocoder geocoder = new Geocoder(mContext);

            try {
                /**
                 * Geocoder.getFromLocation - Returns an array of Addresses
                 * that are known to describe the area immediately surrounding the given latitude and longitude.
                 */
                List<Address> addresses = geocoder.getFromLocation(lat, lon, this.geocoderMaxResults);

                return addresses;
            } catch (IOException e) {
                //e.printStackTrace();
                Log.e(TAG, "Impossible to connect to Geocoder", e);
            }
        }

        return null;
    }

    public String getAddressLine() {
        List<Address> addresses = getGeocoderAddress(latitude, longitude);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);

            return addressLine;
        } else {
            return null;
        }
    }

    /**
     * Try to get Locality
     *
     * @return null or locality
     */
    public String getLocality(double lat, double lon) {
        List<Address> addresses = getGeocoderAddress(lat, lon);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String locality = address.getLocality();

            return locality;
        } else {
            return null;
        }
    }

    /**
     * Try to get Postal Code
     *
     * @return null or postalCode
     */
    public String getPostalCode(double lat, double lon) {
        List<Address> addresses = getGeocoderAddress(lat, lon);

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String postalCode = address.getPostalCode();

            return postalCode;
        } else {
            return null;
        }
    }

    /**
     * Try to get CountryName
     *
     * @return null or postalCode
     */
    public String getCountryName(double lat, double lon) {
        List<Address> addresses = getGeocoderAddress(lat, lon);
        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            String countryName = address.getCountryName();

            return countryName;
        } else {
            return null;
        }
    }

    // Defines location changed event
    @Override
    public void onLocationChanged(Location location) {
        if (lastUpdate != 0 && System.currentTimeMillis() - lastUpdate < 2000) {
            //Toast.makeText(mContext,"sdf",Toast.LENGTH_SHORT).show();
            return;
        }
        ApiInterface apiInterface = ApiClient.getClient(mContext).create(ApiInterface.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
//        if(prev_location != null){
//            //distance = prev_location.distanceTo(location) * 2.2369;
//            distance = Math.abs(prev_location.getLatitude()-location.getLatitude()) + Math.abs(prev_location.getLongitude()-location.getLongitude());
//        }
        lastUpdate = System.currentTimeMillis();
        body.put("reportingId", user_email);
        body.put("dateTime", startTimeString);
        body.put("lat", this.user_point.latitude());
        body.put("lng", this.user_point.longitude());
        body.put("ACCStatus", 1);
        body.put("currentTripMileage", distance);////////////////meter to miles
        body.put("speedInMph", speed);
        body.put("trackerModel", "phone");
        body.put("lastAlert", "");
        apiInterface.postUserLocation("33bedd43-209c-4025-b157-d7c6df1211e3", body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 201) {
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

            }
        });
        mHandler.removeCallbacks(timerRunnable);
        int DELAY = 90 * 1000;
        mHandler.postDelayed(timerRunnable, DELAY);
        prev_location = location;
    }

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            // Toast.makeText(mContext,""+System.currentTimeMillis(),Toast.LENGTH_SHORT).show();
            ApiInterface apiInterface = ApiClient.getClient(mContext).create(ApiInterface.class);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String startTimeString = sdf.format(new Date());

            HashMap<String, Object> body = new HashMap<>();
            body.put("reportingId", user_email);
            body.put("dateTime", startTimeString);
            body.put("lat", GlobalConstant.user_point.latitude());
            body.put("lng", GlobalConstant.user_point.longitude());
            body.put("ACCStatus", 0);
            body.put("currentTripMileage", 0);////////////////meter to miles
            body.put("speedInMph", 0);
            body.put("trackerModel", "phone");
            body.put("lastAlert", "");
            apiInterface.postUserLocation("33bedd43-209c-4025-b157-d7c6df1211e3", body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    int code = response.code();
                    if (code == 201) {
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();

                }
            });
            return;
        }
    };

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    //    private   Runnable timerRunnable = new Runnable() {
    //        @Override
    //        public void run() {
    //            startTimer();
    //        }
    //    };
    //    public void startTimer() {
    //        postUserLocation();
    //        int DELAY = delay * 1000;
    //        mHandler.postDelayed(timerRunnable, DELAY);
    //    }


    //    @Override
    //    public void onSensorChanged(SensorEvent event) {
    //        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
    //            getAccelerometer(event);
    //        }
    //    }
    //
    //    private void getAccelerometer(SensorEvent event) {
    //        float[] values = event.values;
    //        // Movement
    //        float x = values[0];
    //        float y = values[1];
    //        float z = values[2];
    //
    //        float accelationSquareRoot = (x * x + y * y + z * z)
    //                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
    //        if (accelationSquareRoot >= 1.5) //
    //        {
    //            lastUpdate = event.timestamp;
    //            upload_flag = true;
    //        }
    //       // Toast.makeText(getApplicationContext(), ""+accelationSquareRoot, Toast.LENGTH_SHORT).show();
    //    }
    //    @Override
    //    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    //
    //    }
}

