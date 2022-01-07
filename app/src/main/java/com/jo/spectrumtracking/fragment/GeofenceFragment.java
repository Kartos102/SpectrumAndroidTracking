package com.jo.spectrumtracking.fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PointF;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.activity.MainActivity;
import com.jo.spectrumtracking.adapter.AssetListMultiSelectRecyclerViewAdapter;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.LowPassFilter;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_Error;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.jo.spectrumtracking.tracking.GPSTrackerPlus;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.ProjectedMeters;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.carlom.stikkyheader.core.StikkyHeaderBuilder;
import it.carlom.stikkyheader.core.animator.AnimatorBuilder;
import it.carlom.stikkyheader.core.animator.HeaderStikkyAnimator;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeofenceFragment extends Fragment implements SensorEventListener {

    ApiInterface apiInterface;
    Call<ResponseBody> apiCall;

    @BindView(R.id.mapView)
    MapView mapView;
    @BindView(R.id.rv_geofence_right_options)
    RecyclerView rvAssetSingleSelect;
    @BindView(R.id.topView)
    RelativeLayout topView;
    @BindView(R.id.btn_toggle_basemap)
    ImageView btn_toggle_basemap;
    @BindView(R.id.bottomView)
    ScrollView bottomView;
    @BindView(R.id.btn_new_geofence)
    ImageView btnNewGeofence;

    boolean clicked = false;
    private Marker vehicleMarker;

    private Marker moveMarker;
    private float defaultPtZoom = 15f;

    MapboxMap mapboxMap;

    Boolean onViewCreatedOnceCalled = false;

    List<Resp_Tracker> trackerList = null;
    Resp_Tracker selectedTracker = null;

    AssetListMultiSelectRecyclerViewAdapter adapter = null;

    boolean isFragmentAlive = false;

    private Marker geofenceMarker;
    private Polyline geofecePolyline;
    private String style = GlobalConstant.MAP_BOX_STYLE_URL;
    double distance;

    private GPSTrackerPlus gpsTracker;
    private GeomagneticField geomagneticField;
    private float[] smoothed;
    private float[] geomagnetic = new float[3];
    private double bearing = 0;
    private float[] rotation = new float[9];
    // orientation (azimuth, pitch, roll)
    private float[] orientation = new float[3];
    private float[] gravity = new float[3];
    private SensorManager mSensorManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagnetometer;

    public GeofenceFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static GeofenceFragment newInstance() {
        GeofenceFragment fragment = new GeofenceFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFragmentAlive = true;
    }

    @Override
    public void onDestroyView() {
        if(mapView!=null) {
            mapView.onDestroy();
        }
        super.onDestroyView();

        isFragmentAlive = false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Mapbox.getInstance(this.getActivity(), GlobalConstant.MAP_BOX_ACCESS_TOKEN);
        View rootView = inflater.inflate(R.layout.fragment_geofence, container, false);

        ButterKnife.bind(this, rootView);
        mSensorManager = (SensorManager) this.getActivity().getSystemService(
                Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetometer = mSensorManager.getDefaultSensor(
                Sensor.TYPE_MAGNETIC_FIELD);

        apiInterface = ApiClient.getClient(getContext()).create(ApiInterface.class);

        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (onViewCreatedOnceCalled) {
            return;
        }
        onViewCreatedOnceCalled = true;

        getActivity().setTitle("Geofence");

        initMap(savedInstanceState);
        //seekGeo.getThumb().setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY);

        rvAssetSingleSelect.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvAssetSingleSelect.setHasFixedSize(true);

        StikkyHeaderBuilder.stickTo(bottomView)
                .setHeader(topView.getId(), (ViewGroup) getView())
                .minHeightHeader(0)
                .animator(new ParallaxStikkyAnimator())
                .build();

        trackerList = new ArrayList<>();
        if (GlobalConstant.AllTrackerList.size() > 0) {
            trackerList.addAll(GlobalConstant.AllTrackerList);
            for (Resp_Tracker tracker : trackerList) {
                if (selectedTracker == null && GlobalConstant.selectedTrackerIds.contains(tracker.get_id())) {
                    tracker.setSelected(true);
                    selectedTracker = tracker;
                } else {
                    tracker.setSelected(false);
                }
            }

            if (selectedTracker == null) selectedTracker = trackerList.get(0);

            setAssetSingleSelectTableData();
            setSelectedTracker(selectedTracker);
        } else {
            loadAllDrivers();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initMap(Bundle savedInstanceState) {
        final IconFactory iconFactory = IconFactory.getInstance(GeofenceFragment.this.getContext());
        mapView.onCreate(savedInstanceState);

        //mapView.setStyleUrl(GlobalConstant.MAP_BOX_STYLE_URL);

        final MyView myView = new MyView(this.getActivity());

        myView.setOnTouchListener(new View.OnTouchListener() {
            float firstX, firstY;
            float secondX, secondY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {


                int action = event.getActionMasked();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        if (clicked) {
                            firstX = event.getX();
                            firstY = event.getY();
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (clicked) {
                            secondX = event.getX();
                            secondY = event.getY();

                            LatLng firstLatLng = mapboxMap.getProjection().fromScreenLocation(new PointF(firstX, firstY));
                            LatLng secondLatLng = mapboxMap.getProjection().fromScreenLocation(new PointF(secondX, secondY));

                            Location l1 = new Location("first");
                            l1.setLatitude(firstLatLng.getLatitude());
                            l1.setLongitude(firstLatLng.getLongitude());

                            Location l2 = new Location("first");
                            l2.setLatitude(secondLatLng.getLatitude());
                            l2.setLongitude(secondLatLng.getLongitude());

                            distance = l1.distanceTo(l2);

                            if (geofecePolyline != null)
                                GeofenceFragment.this.mapboxMap.removePolyline(geofecePolyline);
                            PolylineOptions polylineOptions = new PolylineOptions();
                            polylineOptions.color(Color.parseColor("#0000FF"));
                            polylineOptions.width(0.5f); // change the line width here
                            polylineOptions.addAll(getCirclePoints(geofenceMarker.getPosition(), distance));
                            geofecePolyline = mapboxMap.addPolyline(polylineOptions);

                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (clicked) {

                            myView.setVisibility(View.GONE);
                            clicked = false;
                            saveGeofence(geofenceMarker.getPosition(), distance);
                        }

                        break;
                }

                return true;
            }
        });

        myView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        mapView.addView(myView);
        mapView.bringChildToFront(myView);
        myView.setVisibility(View.GONE);

        mapView.getMapAsync(mapboxMap -> {
            GeofenceFragment.this.mapboxMap = mapboxMap;
            GeofenceFragment.this.mapboxMap.setStyle(new Style.Builder().fromUrl(style));
            GeofenceFragment.this.mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
            Utils.setMapUtilityToolsInGeofence(GeofenceFragment.this.mapboxMap, mapView.getRootView());
        });
    }

    private void saveGeofence(final LatLng position, final double distance) {
        if (!isFragmentAlive) {
            return;
        }

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        for (Resp_Tracker tracker : trackerList) {
            if (tracker.getSelected()) {
                HashMap<String, Object> param = new HashMap<String, Object>();
                param.put("latGeo", String.valueOf(position.getLatitude()));
                param.put("lngGeo", String.valueOf(position.getLongitude()));
                param.put("radiusGeo", String.valueOf(distance));
                //param.put("trackerId", asset.getTrackerId());
                param.put("assetId", tracker.getAssetId());
                param.put("plateNumber", tracker.getName());
                param.put("driverName", tracker.getDriverName());

                apiInterface.setGeoFence(param).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                        Utils.showShortToast(GeofenceFragment.this.getContext(), "Geofence is set", false);

                        IconFactory iconFactory = IconFactory.getInstance(GeofenceFragment.this.getContext());
                        Icon icon = iconFactory.fromResource(R.drawable.speedingupicon);

                        if (moveMarker != null) mapboxMap.removeMarker(moveMarker);

                        ProjectedMeters projectedMeters = mapboxMap.getProjection().getProjectedMetersForLatLng(position);
                        ProjectedMeters converted = new ProjectedMeters(projectedMeters.getNorthing() - distance, projectedMeters.getEasting());
                        LatLng moveMarkerPos = mapboxMap.getProjection().getLatLngForProjectedMeters(converted);

                        com.mapbox.mapboxsdk.annotations.MarkerOptions markerViewOptions = new com.mapbox.mapboxsdk.annotations.MarkerOptions()
                                .position(new LatLng(moveMarkerPos.getLatitude(), moveMarkerPos.getLongitude()))
                                .icon(icon);

                        moveMarker = mapboxMap.addMarker(markerViewOptions);

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Utils.showShortToast(GeofenceFragment.this.getContext(), "Failed", true);
                    }
                });
            }
        }
    }

    private void loadAllDrivers() {
        if (!isFragmentAlive) {
            return;
        }

        if (trackerList == null) {
            trackerList = new ArrayList<>();
        }

        if (GlobalConstant.AllTrackerList.size() == 0) Utils.showProgress(this.getContext());

        if(apiCall!=null) {
            apiCall.cancel();
        }

        apiCall= apiInterface.getAllTrackersWeb(GlobalConstant.X_CSRF_TOKEN);
        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();
                if (!isFragmentAlive) {
                    return;
                }

                int code = response.code();
                Log.d("codecode", "" + code);
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);

                Gson gson = gsonBuilder.create();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;

                    try {
                        object = new JSONObject(responseBody.string());
                        JSONArray items = (JSONArray) object.get("items");

                        Type type_trackers = new TypeToken<List<Resp_Tracker>>() {}.getType();
                        List<Resp_Tracker> newTrackerList = gson.fromJson(items.toString(), type_trackers);

                        SharedPreferences preferences = getActivity().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
                        Boolean isPhoneTracking = preferences.getBoolean("sPhoneTracking", false);

                        trackerList.clear();
                        selectedTracker = null;

                        try {
                            for (final Resp_Tracker tracker : newTrackerList) {
                                if (tracker.getLat() == 0.0 && tracker.getLng() == 0.0) {
                                    continue;
                                }
                                if (Math.abs(tracker.getLat()) > 90.0 || Math.abs(tracker.getLng()) > 180) {
                                    continue;
                                }
                                if (isPhoneTracking == false && tracker.getSpectrumId().equals(GlobalConstant.email)) {
                                    continue;
                                }

                                if (selectedTracker == null || GlobalConstant.selectedTrackerIds.contains(tracker.get_id())) {
                                    tracker.setSelected(true);
                                    selectedTracker = tracker;
                                } else {
                                    tracker.setSelected(false);
                                }
                                trackerList.add(tracker);
                            }

                            if (selectedTracker == null) selectedTracker = trackerList.get(0);
                            setSelectedTracker(selectedTracker);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!isFragmentAlive) {
                        return;
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                if (!isFragmentAlive) {
                    return;
                }
                Utils.showShortToast(GeofenceFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    private static ArrayList<LatLng> getCirclePoints(LatLng position, double radius) {
        int degreesBetweenPoints = 10; // change here for shape
        int numberOfPoints = (int) Math.floor(360 / degreesBetweenPoints);
        double distRadians = radius / 6371000.0; // earth radius in meters
        double centerLatRadians = position.getLatitude() * Math.PI / 180;
        double centerLonRadians = position.getLongitude() * Math.PI / 180;
        ArrayList<LatLng> polygons = new ArrayList<>(); // array to hold all the points
        for (int index = 0; index < numberOfPoints; index++) {
            double degrees = index * degreesBetweenPoints;
            double degreeRadians = degrees * Math.PI / 180;
            double pointLatRadians = Math.asin(Math.sin(centerLatRadians) * Math.cos(distRadians)
                    + Math.cos(centerLatRadians) * Math.sin(distRadians) * Math.cos(degreeRadians));
            double pointLonRadians = centerLonRadians + Math.atan2(Math.sin(degreeRadians)
                            * Math.sin(distRadians) * Math.cos(centerLatRadians),
                    Math.cos(distRadians) - Math.sin(centerLatRadians) * Math.sin(pointLatRadians));
            double pointLat = pointLatRadians * 180 / Math.PI;
            double pointLon = pointLonRadians * 180 / Math.PI;
            LatLng point = new LatLng(pointLat, pointLon);
            polygons.add(point);
        }
        // add first point at end to close circle
        polygons.add(polygons.get(0));
        return polygons;
    }

    @OnClick(R.id.btn_toggle_basemap)
    public void onToogleBaseMapClick() {

        Resources resources = this.getActivity().getResources();
        //mapView.setStyleUrl(GlobalConstant.MAP_BOX_STYLE_URL);
        if (style.equals(GlobalConstant.MAP_BOX_STYLE_URL)) {
            style = GlobalConstant.MAP_BOX_SATELLITE_URL;
        } else {
            style = GlobalConstant.MAP_BOX_STYLE_URL;
        }
        mapboxMap.setStyle(new Style.Builder().fromUrl(style));

    }

    @OnClick(R.id.btn_new_geofence)
    public void onNewGeofenceClick() {
        MainActivity mainActivity = (MainActivity)getActivity();
        EditGeofenceFragment fragment = EditGeofenceFragment.newInstance();
        fragment.originalCameraPosition = mapboxMap.getCameraPosition();
//        mainActivity.fragment = fragment;
//        mainActivity.setFragment();
        mainActivity.pushFragment(fragment);
    }

    private void setAssetSingleSelectTableData() {
        if (!isFragmentAlive) {
            return;
        }

        // items
        adapter = new AssetListMultiSelectRecyclerViewAdapter(this, trackerList, R.layout.recyclerview_row_asset_multi_select);

        rvAssetSingleSelect.setAdapter(adapter);
        rvAssetSingleSelect.setLayoutManager(new LinearLayoutManager(this.getContext()));
        rvAssetSingleSelect.setItemAnimator(new DefaultItemAnimator());
    }

    public void setSelectedTracker(Resp_Tracker selectedTracker) {
        this.selectedTracker = selectedTracker;
        for (Resp_Tracker tracker : trackerList) {
            if (selectedTracker.get_id() == tracker.get_id()) tracker.setSelected(true);
        }

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        apiInterface.trackers_id(selectedTracker.get_id()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Utils.hideProgress();
                if (!isFragmentAlive) {
                    return;
                }

                int code = response.code();
                Gson gson = new Gson();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();

                    Resp_Tracker tracker = null;

                    try {
                        String bodyString = responseBody.string();
                        tracker = gson.fromJson(bodyString, Resp_Tracker.class);
                        if (tracker.getLat() == 0 || tracker.getLng() == 0) return;
                        LatLng point = new LatLng(tracker.getLat(), tracker.getLng());

                        IconFactory iconFactory = IconFactory.getInstance(GeofenceFragment.this.getContext());
                        Icon icon = iconFactory.fromResource(R.drawable.locationcirclesmall_pre);

                        if (vehicleMarker != null) mapboxMap.removeMarker(vehicleMarker);

                          /* MarkerViewOptions markerViewOptions = new MarkerViewOptions()
                                .position(point)
                                .icon(icon);*/

                        //vehicleMarker= mapboxMap.addMarker(markerViewOptions);

                        CameraPosition cameraPosition = new CameraPosition.Builder().target(point).bearing(0).zoom(defaultPtZoom).tilt(0).build();
                        mapboxMap.setCameraPosition(cameraPosition);


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(GeofenceFragment.this.getContext(), error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                if (!isFragmentAlive) {
                    return;
                }
                //pBarLoading.setVisibility(View.INVISIBLE);
                Utils.showShortToast(GeofenceFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    public void onBottomTrackerListCheckChanged(int position, boolean isChecked) {
        if (position >= trackerList.size()) {
            return;
        }
        Resp_Tracker tracker = trackerList.get(position);
        tracker.setSelected(isChecked);
        if (!tracker.getSelected()) return;

        setSelectedTracker(tracker);
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorMagnetometer != null) {
            mSensorManager.registerListener(this, mSensorMagnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Sensor accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mSensorManager.registerListener(this, accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
            }
        }
        Sensor magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (magneticField != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mSensorManager.registerListener(this, magneticField,
                        SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
            }
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (gpsTracker == null) {
            gpsTracker = new GPSTrackerPlus(this.getActivity().getApplicationContext());
            gpsTracker.getLocation();
        }

        geomagneticField = new GeomagneticField(
                (float) gpsTracker.getLatitude(),
                (float) gpsTracker.getLongitude(),
                (float) gpsTracker.getAltitude(),
                System.currentTimeMillis());

        boolean accelOrMagnetic = false;

        // get accelerometer data
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // we need to use a low pass filter to make data smoothed
            smoothed = LowPassFilter.filter(event.values, gravity);
            gravity[0] = smoothed[0];
            gravity[1] = smoothed[1];
            gravity[2] = smoothed[2];
            accelOrMagnetic = true;

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            smoothed = LowPassFilter.filter(event.values, geomagnetic);
            geomagnetic[0] = smoothed[0];
            geomagnetic[1] = smoothed[1];
            geomagnetic[2] = smoothed[2];
            accelOrMagnetic = true;

        }

        // get rotation matrix to get gravity and magnetic data
        SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic);
        // get bearing to target
        SensorManager.getOrientation(rotation, orientation);
        // east degrees of true North
        bearing = orientation[0];
        // convert from radians to degrees
        bearing = Math.toDegrees(bearing);

        // fix difference between true North and magnetical North
        if (geomagneticField != null) {
            bearing += geomagneticField.getDeclination();
        }

        // bearing must be in 0-360
        if (bearing < 0) {
            bearing += 360;
        }


    }

    private class ParallaxStikkyAnimator extends HeaderStikkyAnimator {
        @Override
        public AnimatorBuilder getAnimatorBuilder() {
            View mHeader_image = getHeader().findViewById(R.id.mapView);
            return AnimatorBuilder.create().applyVerticalParallax(mHeader_image, 1);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class MyView extends View {

        public MyView(Context context) {
            super(context);

        }


    }

}
