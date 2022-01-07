package com.jo.spectrumtracking.tracking;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.mapbox.geojson.Point;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A bound and started service that is promoted to a foreground service when location updates have
 * been requested and all clients unbind.
 *
 * For apps running in the background on "O" devices, location is computed only once every 10
 * minutes and delivered batched every 30 minutes. This restriction applies even to apps
 * targeting "N" or lower which are run on "O" devices.
 *
 * This sample show how to use a long-running service for location updates. When an activity is
 * bound to this service, frequent location updates are permitted. When the activity is removed
 * from the foreground, the service promotes itself to a foreground service, and location updates
 * continue. When the activity comes back to the foreground, the foreground service stops, and the
 * notification assocaited with that service is removed.
 */
public class GPSTracker2Plus extends Service implements SensorEventListener {

    private static final String PACKAGE_NAME =
            "com.jo.spectrum.tracking";

    private static final String TAG = GPSTracker2Plus.class.getSimpleName();

    /**
     * The name of the channel for notifications.
     */
    private static final String CHANNEL_ID = "channel_01";

    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";

    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
            ".started_from_notification";

    private final IBinder mBinder = new LocalBinder();
    private long lastUpdate = 0;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static  long UPDATE_INTERVAL_IN_MILLISECONDS = 1000*10;///10sec
    private static  final float DISPLACEMENT=20;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * The identifier for the notification displayed for the foreground service.
     */
    private static final int NOTIFICATION_ID = 12345678;

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private boolean mChangingConfiguration = false;

    private NotificationManager mNotificationManager;

    /**
     * Contains parameters used by {@link com.google.android.gms.location.FusedLocationProviderApi}.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    private Handler mServiceHandler;
    private Location prev_location = null;

    private Location mLocation;
    private int iAccelReadings, iAccelSignificantReadings;
    private long iAccelTimestamp;
    private SensorManager mSensorManager;

    private float x_accelaration=0F;
    private float y_accelaration=0F;
    private float z_accelaration=0F;



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

                ApiInterface apiInterface = ApiClient.getClient(GPSTracker2Plus.this.getApplicationContext()).create(ApiInterface.class);
                apiInterface.postUserLocation("33bedd43-209c-4025-b157-d7c6df1211e3", body)
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                int code = response.code();
                                startAccelerometer();
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
     * The current location.
     */


    public GPSTracker2Plus() {
    }
    public void startAccelerometer() {
        iAccelReadings = 0;
        iAccelSignificantReadings = 0;
        iAccelTimestamp = System.currentTimeMillis();
        // should probably store handles to these earlier, when service is created
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void stopAccelerometer() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                //LIAN
                //upload to server gps location
                // where locationResult is assigned values?
                onNewLocation(locationResult.getLastLocation());

            }
        };

        // LIAN what are these two for?
        // why after onNewLocation?

        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mServiceHandler = new Handler(handlerThread.getLooper());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);
        }
        startMyOwnForeground();
    }
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service started");
        if (intent != null) {
            boolean flag = intent.getBooleanExtra("flag", false);
            UPDATE_INTERVAL_IN_MILLISECONDS = intent.getIntExtra("delay", 1) * 1000;
            //TIME_BW_UPDATES = intent.getIntExtra("delay", 1) * 1000;
            if (flag) user_email = "";
        }


     /*   boolean startedFromNotification = intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,
                false);


        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates();
            stopSelf();
        }*/
        // Tells the system to not try to recreate the service after it has been killed.

        startAccelerometer();
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) comes to the foreground
        // and binds with this service. The service should cease to be a foreground service
        // when that happens.
        Log.i(TAG, "in onBind()");
        //stopForeground(true);
        mChangingConfiguration = false;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()");
        //stopForeground(true);
        mChangingConfiguration = false;
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Last client unbound from service");

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!mChangingConfiguration && Utils.requestingLocationUpdates(this)) {
            Log.i(TAG, "Starting foreground service");

            // startForeground(NOTIFICATION_ID, getNotification());
        }
        return true; // Ensures onRebind() is called when a client re-binds.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mServiceHandler.removeCallbacksAndMessages(null);
        working.set(false);
        stopSelf();
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates");
        Utils.setRequestingLocationUpdates(this, true);
        startService(new Intent(getApplicationContext(), GPSTracker2Plus.class));
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, false);
            Log.e(TAG, "Lost location permission. Could not request updates. " + unlikely);
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * {@link SecurityException}.
     */
    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        try {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Utils.setRequestingLocationUpdates(this, false);
            stopSelf();
        } catch (SecurityException unlikely) {
            Utils.setRequestingLocationUpdates(this, true);
            Log.e(TAG, "Lost location permission. Could not remove updates. " + unlikely);
        }
    }

    /**
     * Returns the {@link NotificationCompat} used as part of the foreground service.
     */
  /*  private Notification getNotification() {
        Intent intent = new Intent(this, GPSTracker2Plus.class);

        CharSequence text = Utils.getLocationText(mLocation);

        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true);

        // The PendingIntent that leads to a call to onStartCommand() in this service.
        PendingIntent servicePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // The PendingIntent to launch activity.
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .addAction(R.drawable.ic_launch, getString(R.string.launch_activity),
                        activityPendingIntent)
                .addAction(R.drawable.ic_cancel, getString(R.string.remove_location_updates),
                        servicePendingIntent)
                .setContentText(text)
                .setContentTitle(Utils.getLocationTitle(this))
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(text)
                .setWhen(System.currentTimeMillis());

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID); // Channel ID
        }

        return builder.build();
    }*/

    public void getLastLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                mLocation = task.getResult();


                            } else {
                                Log.w(TAG, "Failed to get location.");
                            }
                        }
                    });
        } catch (SecurityException unlikely) {
            Log.e(TAG, "Lost location permission." + unlikely);
        }
    }
    public Location getLocation(){
        return mLocation;
    }
    public  double getLatitude() {
        return mLocation.getLatitude();
    }

    public double getLongitude() {
        return mLocation.getLongitude();
    }

    private Point user_point;
    private String user_email = "";
    private int accStatus = 1;

    private void onNewLocation(Location location) {

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("spectrum", Context.MODE_PRIVATE);

        int upload_delay=preferences.getInt("upload_delay",20);

        mLocation=new Location("Current Location");
        mLocation.setLatitude(location.getLatitude());
        mLocation.setLongitude(location.getLongitude());
        mLocation.setAltitude(location.getAltitude());

        Intent intent = new Intent(ACTION_BROADCAST);
        intent.putExtra(EXTRA_LOCATION, location);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

        if(!preferences.getBoolean("sPhoneTracking",false)) {
            // stopSelf();
//            onDestroy();
            return;
        }

        if (lastUpdate != 0 && System.currentTimeMillis() - lastUpdate < upload_delay*1000) {
            return;
        }

        lastUpdate = System.currentTimeMillis();
        // Notify anyone listening for broadcasts about the new location.

        ApiInterface  apiInterface = ApiClient.getClient(getApplicationContext()).create(ApiInterface.class);
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

            user_email = preferences.getString("username", "");
        }


        if(prev_location==null) {
            prev_location=new Location("Previous Location");
            prev_location.setLatitude(mLocation.getLatitude());
            prev_location.setLongitude(mLocation.getLongitude());
            return;

        }

        double distance=0;
        distance=prev_location.distanceTo(mLocation)* 2.2369;

        //Do we need this one? already set displacemnt in setting
        //if(distance/2.2369<DISPLACEMENT) return;

        Log.e(TAG, "Uploaded location");

        prev_location=new Location("Previous Location");
        prev_location.setLatitude(mLocation.getLatitude());
        prev_location.setLongitude(mLocation.getLongitude());


        body.put("reportingId", user_email);
        body.put("dateTime", startTimeString);
        body.put("lat", this.user_point.latitude());
        body.put("lng", this.user_point.longitude());
        body.put("ACCStatus", 1);
        body.put("currentTripMileage", distance);
        body.put("speedInMph", speed);
        body.put("trackerModel", "phone");
        body.put("lastAlert", "");

        body.put("altitude",mLocation.getAltitude());
        body.put("x_accelaration",x_accelaration);
        body.put("y_accelaration",y_accelaration);
        body.put("z_accelaration",z_accelaration);

        System.out.println(x_accelaration+":"+y_accelaration+":"+z_accelaration);



        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                apiInterface.postUserLocation("33bedd43-209c-4025-b157-d7c6df1211e3", body)
                        .enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                int code = response.code();
                                if (code == 201) {
                                    Log.e(TAG, "Post a new location is: Lat/ " + location.getLatitude() + " , Long/ " + location.getLongitude());
                                    // Log.e(TAG, "Interval: " + TIME_BW_UPDATES);
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                t.printStackTrace();

                            }
                        });

              }
            },2000);

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            //mNotificationManager.notify(NOTIFICATION_ID, getNotification());
        }
    }

    /**
     * Sets the location request parameters.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double accel, x, y, z;
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) {
            return;
        }
        iAccelReadings++;
        x_accelaration = event.values[0];
        y_accelaration = event.values[1];
        z_accelaration = event.values[2];

        accel = Math.abs(
                Math.sqrt(
                        Math.pow(x_accelaration,2)
                                +
                                Math.pow(y_accelaration,2)
                                +
                                Math.pow(z_accelaration,2)
                )
        );
        System.out.println("The accelration is "+accel);

        stopAccelerometer();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public GPSTracker2Plus getService() {
            return GPSTracker2Plus.this;
        }
    }

    /**
     * Returns true if this is a foreground service.
     *
     * @param context The {@link Context}.
     */
    public boolean serviceIsRunningInForeground(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(
                Integer.MAX_VALUE)) {
            if (getClass().getName().equals(service.service.getClassName())) {
                if (service.foreground) {
                    return true;
                }
            }
        }
        return false;
    }
}