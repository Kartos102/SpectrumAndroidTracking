package com.jo.spectrumtracking.fragment;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.activity.MainActivity;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_Error;
import com.jo.spectrumtracking.model.Resp_Geofence;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.mapbox.android.gestures.AndroidGesturesManager;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.android.gestures.StandardGestureDetector;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditGeofenceFragment extends Fragment {

    @BindView(R.id.mapView)
    MapView mapView;
    @BindView(R.id.topView)
    RelativeLayout topView;
    @BindView(R.id.edit_fence_name)
    EditText fenceNameEdit;
    @BindView(R.id.spinner_fence_type)
    Spinner fenceTypeSpinner;
    @BindView(R.id.spinner_vehicle_name)
    Spinner vehicleNameSpinner;
    @BindView(R.id.btn_add_new_fence)
    TextView btnAddNewFence;
    @BindView(R.id.text_polygon_description)
    TextView txtPolygonDescription;
    @BindView(R.id.ll_draw_type_buttons)
    LinearLayout llDrawTypeButtons;
    @BindView(R.id.ll_bottom_buttons)
    LinearLayout llBottomButtons;

    MapboxMap mapboxMap;

    Boolean onViewCreatedOnceCalled = false;

    boolean isFragmentAlive = false;

    private Polyline geofencePolylin;
    private Polyline prevGeofencePolylin;
    private Polygon geofencePolygon;
    private Polygon prevGeofencePolygon;
    private List<Marker> pointMarkerArray = new ArrayList<>();
    private String style = GlobalConstant.MAP_BOX_STYLE_URL;
    double distance;
    private int geofenceType = 0;       // 0: circle, 1: polygon
    private boolean isDrawing = false;

    private AndroidGesturesManager drawingGestureManager;
    private AndroidGesturesManager mapboxDefaultGestureManager;

    private PointF moveFirstPoint;
    private PointF moveEndPoint;
    private ArrayList<LatLng> polygonPoints = new ArrayList<>();

    public CameraPosition originalCameraPosition;

    public ArrayList<Resp_Geofence> geofenceList = new ArrayList<>();

    List<Resp_Tracker> trackerList = null;
    Resp_Tracker selectedTracker = null;

    public Resp_Geofence newGeofence;

    public LatLng circleCenter;
    public double radius;

    public EditGeofenceFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static EditGeofenceFragment newInstance() {
        EditGeofenceFragment fragment = new EditGeofenceFragment();

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
        View rootView = inflater.inflate(R.layout.fragment_edit_geofence, container, false);

        ButterKnife.bind(this, rootView);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.geofence_type, R.layout.spinner_row_geofence_type);
        adapter.setDropDownViewResource(R.layout.spinner_row_geofence_type);
        fenceTypeSpinner.setAdapter(adapter);
        fenceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                EditGeofenceFragment.this.geofenceType = position;
                if (position == 0) {
                    txtPolygonDescription.setVisibility(View.GONE);
                } else {
                    txtPolygonDescription.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        llBottomButtons.setVisibility(View.GONE);
        txtPolygonDescription.setVisibility(View.GONE);

        List<String> trackerNames = new ArrayList<>();
        trackerList = new ArrayList<>();
        if (GlobalConstant.AllTrackerList.size() > 0) {
            trackerList.addAll(GlobalConstant.AllTrackerList);
            for (Resp_Tracker tracker : trackerList) {
                if (tracker.getDriverName().equals("")) {
                    trackerNames.add(tracker.getPlateNumber());
                } else {
                    trackerNames.add(tracker.getDriverName());
                }

                if (selectedTracker == null && GlobalConstant.selectedTrackerIds.contains(tracker.get_id())) {
                    tracker.setSelected(true);
                    selectedTracker = tracker;
                } else {
                    tracker.setSelected(false);
                }
            }

            if (selectedTracker == null) selectedTracker = trackerList.get(0);
        }

        if (trackerList.size() > 0) {
            vehicleNameSpinner.setVisibility(View.VISIBLE);

            ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getContext(), R.layout.spinner_row_geofence_type, trackerNames);
            adapter.setDropDownViewResource(R.layout.spinner_row_geofence_type);
            vehicleNameSpinner.setAdapter(adapter1);
            vehicleNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedTracker = trackerList.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (selectedTracker != null) {
                for (int i=0; i<trackerList.size(); i++) {
                    if (trackerList.get(i).get_id().equals(selectedTracker.get_id())) {
                        vehicleNameSpinner.setSelection(i);
                        break;
                    }
                }
            }
        } else {
            vehicleNameSpinner.setVisibility(View.GONE);
        }

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

        initMap(savedInstanceState);

        getGeofenceList();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initMap(Bundle savedInstanceState) {
        final IconFactory iconFactory = IconFactory.getInstance(EditGeofenceFragment.this.getContext());
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            mapboxMap.getUiSettings().setAttributionEnabled(false);
            mapboxMap.getUiSettings().setLogoEnabled(false);
            if(originalCameraPosition!=null)mapboxMap.setCameraPosition(EditGeofenceFragment.this.originalCameraPosition);

            EditGeofenceFragment.this.mapboxMap = mapboxMap;
            EditGeofenceFragment.this.mapboxMap.setStyle(new Style.Builder().fromUrl(style));
            EditGeofenceFragment.this.mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
            EditGeofenceFragment.this.initMapGesture();
        });
    }

    private void drawPolygon(ArrayList<LatLng> points, boolean drawPrevious) {
        if (prevGeofencePolygon != null) {
            EditGeofenceFragment.this.mapboxMap.removePolygon(prevGeofencePolygon);
        }

        if (prevGeofencePolylin != null) {
            EditGeofenceFragment.this.mapboxMap.removePolyline(prevGeofencePolylin);
        }

        if (drawPrevious == true) {
            prevGeofencePolygon = geofencePolygon;
            prevGeofencePolylin = geofencePolylin;
        } else {
            if (geofencePolygon != null) {
                EditGeofenceFragment.this.mapboxMap.removePolygon(geofencePolygon);
            }

            if (geofencePolylin != null) {
                EditGeofenceFragment.this.mapboxMap.removePolyline(geofencePolylin);
            }
        }

        List<Marker> markers = mapboxMap.getMarkers();
        for (Marker marker : markers) {
            mapboxMap.removeMarker(marker);
        }

        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.fillColor(Color.parseColor("#50FF0000"));
        polygonOptions.strokeColor(Color.parseColor("#FF0000"));
        polygonOptions.addAll(points);

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.parseColor("#FF0000"));
        polylineOptions.width(4.0f);
        polylineOptions.addAll(points);

        geofencePolygon = mapboxMap.addPolygon(polygonOptions);
        geofencePolylin = mapboxMap.addPolyline(polylineOptions);

        for (LatLng point : points) {
            BitmapDrawable bitmapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_marker_polygon_point);
            Bitmap b = bitmapDraw.getBitmap();
//            Bitmap resizedBmp = Bitmap.createScaledBitmap(b, 20, 20, false);
            final IconFactory iconFactory = IconFactory.getInstance(EditGeofenceFragment.this.getContext());

            mapboxMap.addMarker(new MarkerOptions()
                    .position(point)
                    .setIcon(iconFactory.fromBitmap(b))
            );
        }
    }

    private void initMapGesture() {
        Set<Integer> mutuallyExclusive1 = new HashSet<>();
        Set<Integer> mutuallyExclusive2 = new HashSet<>();
        mutuallyExclusive1.add(AndroidGesturesManager.GESTURE_TYPE_MOVE);
        mutuallyExclusive2.add(AndroidGesturesManager.GESTURE_TYPE_SINGLE_TAP_UP);
        mutuallyExclusive2.add(AndroidGesturesManager.GESTURE_TYPE_DOUBLE_TAP);

        this.drawingGestureManager = new AndroidGesturesManager(getContext(), mutuallyExclusive1, mutuallyExclusive2);
        this.drawingGestureManager.setMoveGestureListener(new MoveGestureDetector.OnMoveGestureListener() {
            @Override
            public boolean onMoveBegin(@NonNull MoveGestureDetector detector) {
                if (EditGeofenceFragment.this.geofenceType == 0) {
                    EditGeofenceFragment.this.moveFirstPoint = detector.getFocalPoint();
                    return true;
                }
                return false;
            }

            @Override
            public boolean onMove(@NonNull MoveGestureDetector detector, float distanceX, float distanceY) {
                if (EditGeofenceFragment.this.geofenceType == 0) {
                    EditGeofenceFragment.this.moveEndPoint = detector.getFocalPoint();

                    LatLng firstLatLng = mapboxMap.getProjection().fromScreenLocation(EditGeofenceFragment.this.moveFirstPoint);
                    LatLng secondLatLng = mapboxMap.getProjection().fromScreenLocation(EditGeofenceFragment.this.moveEndPoint);

                    Location l1 = new Location("first");
                    l1.setLatitude(firstLatLng.getLatitude());
                    l1.setLongitude(firstLatLng.getLongitude());

                    Location l2 = new Location("first");
                    l2.setLatitude(secondLatLng.getLatitude());
                    l2.setLongitude(secondLatLng.getLongitude());

                    distance = l1.distanceTo(l2);

                    circleCenter = firstLatLng;
                    radius = distance;

                    EditGeofenceFragment.this.drawPolygon(getCirclePoints(firstLatLng, distance), true);

                    return true;
                }
                return false;
            }

            @Override
            public void onMoveEnd(@NonNull MoveGestureDetector detector, float velocityX, float velocityY) {
                if (EditGeofenceFragment.this.geofenceType == 0) {
                    if (prevGeofencePolygon != null) {
                        EditGeofenceFragment.this.mapboxMap.removePolygon(prevGeofencePolygon);
                    }

                    Resp_Geofence geofence = new Resp_Geofence();
                    geofence.setName(fenceNameEdit.getText().toString());
                    geofence.setType("Circle");
                    geofence.setLat(circleCenter.getLatitude());
                    geofence.setLng(circleCenter.getLongitude());
                    geofence.setRadius(radius);
                    geofence.setBounday(new ArrayList<>());

                    newGeofence = geofence;
                }
            }
        });

        this.drawingGestureManager.setStandardGestureListener(new StandardGestureDetector.StandardOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (EditGeofenceFragment.this.geofenceType == 1) {
                    PointF point = new PointF(e.getX(), e.getY());
                    LatLng latLng = mapboxMap.getProjection().fromScreenLocation(point);

                    EditGeofenceFragment.this.polygonPoints.add(latLng);
                    if (EditGeofenceFragment.this.polygonPoints.size() > 1) {
                        EditGeofenceFragment.this.drawPolygon(EditGeofenceFragment.this.polygonPoints, false);
                    }

                    return true;
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                if (EditGeofenceFragment.this.geofenceType == 1) {
                    if (EditGeofenceFragment.this.polygonPoints.size() < 3) {
                        return true;
                    }

                    PointF point = new PointF(e.getX(), e.getY());
                    LatLng latLng = mapboxMap.getProjection().fromScreenLocation(point);

                    EditGeofenceFragment.this.polygonPoints.add(latLng);
                    if (EditGeofenceFragment.this.polygonPoints.size() > 1) {
                        EditGeofenceFragment.this.polygonPoints.add(EditGeofenceFragment.this.polygonPoints.get(0));
                        EditGeofenceFragment.this.drawPolygon(EditGeofenceFragment.this.polygonPoints, false);
                    }

                    Resp_Geofence geofence = new Resp_Geofence();
                    geofence.setName(fenceNameEdit.getText().toString());
                    geofence.setType("Polygon");

                    if (selectedTracker != null) {
                        geofence.setLat(selectedTracker.getLat());
                        geofence.setLng(selectedTracker.getLng());
                    } else {
                        geofence.setLat(0.0);
                        geofence.setLng(0.0);
                    }

                    geofence.setRadius(300);

                    ArrayList<ArrayList<Double>> points = new ArrayList<>();
                    for (LatLng p : EditGeofenceFragment.this.polygonPoints) {
                        ArrayList<Double> coordinate = new ArrayList<>();
                        coordinate.add(p.getLongitude());
                        coordinate.add(p.getLatitude());
                        points.add(coordinate);
                    }
                    geofence.setBounday(points);

                    newGeofence = geofence;

                    EditGeofenceFragment.this.polygonPoints.clear();

                    return true;
                }
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return false;
            }
        });

        this.mapboxDefaultGestureManager = mapboxMap.getGesturesManager();

        if (isDrawing) {
            mapboxMap.setGesturesManager(this.drawingGestureManager, false, true);
            btnAddNewFence.setText(getString(R.string.stop_drawing));
        } else {
            mapboxMap.setGesturesManager(this.mapboxDefaultGestureManager, true, true);
            btnAddNewFence.setText(getString(R.string.add_new_fence));
        }
    }

    private static ArrayList<LatLng> getCirclePoints(LatLng position, double radius) {
        int degreesBetweenPoints = 1; // change here for shape
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

    @OnClick(R.id.btn_add_new_fence)
    public void onAddNewFence() {
        if (isDrawing) {
            isDrawing = false;
//            mapboxMap.setGesturesManager(this.drawingGestureManager, false, true);
            mapboxMap.setGesturesManager(this.mapboxDefaultGestureManager, true, true);
            btnAddNewFence.setText(getString(R.string.draw_geofence));
            onFinishAdding(true);
        } else {
            isDrawing = true;
            mapboxMap.setGesturesManager(this.drawingGestureManager, false, true);
            btnAddNewFence.setText(getString(R.string.save_geofence));
        }
    }

    @OnClick(R.id.btn_type_circle)
    public void onTypeCirlce() {
        geofenceType = 0;
        isDrawing = true;
        txtPolygonDescription.setVisibility(View.GONE);
        llDrawTypeButtons.setVisibility(View.GONE);
        llBottomButtons.setVisibility(View.VISIBLE);

        mapboxMap.setGesturesManager(this.drawingGestureManager, false, true);
        btnAddNewFence.setText(R.string.save);
    }

    @OnClick(R.id.btn_type_polygon)
    public void onTypePolygon() {
        geofenceType = 1;
        isDrawing = true;
        txtPolygonDescription.setVisibility(View.VISIBLE);
        llDrawTypeButtons.setVisibility(View.GONE);
        llBottomButtons.setVisibility(View.VISIBLE);

        mapboxMap.setGesturesManager(this.drawingGestureManager, false, true);
        btnAddNewFence.setText(R.string.save);
    }

    @OnClick(R.id.btn_clear)
    public void onClear() {
        onFinishAdding(false);
    }

//    @OnClick(R.id.back)
//    public void onBack() {
//        MainActivity.get().popFragment();
//    }

//    @OnClick(R.id.btn_finish_adding)
    public void onFinishAdding(boolean shouldSave) {
        if (newGeofence != null) {
            String fenceName = fenceNameEdit.getText().toString();
            newGeofence.setName(fenceName);
            this.geofenceList.add(newGeofence);
            newGeofence = null;
        }

        fenceNameEdit.setText("");
        this.polygonPoints.clear();

        if (geofencePolygon != null) {
            EditGeofenceFragment.this.mapboxMap.removePolygon(geofencePolygon);
        }

        if (prevGeofencePolygon != null) {
            EditGeofenceFragment.this.mapboxMap.removePolygon(prevGeofencePolygon);
        }

        if (geofencePolylin != null) {
            EditGeofenceFragment.this.mapboxMap.removePolyline(geofencePolylin);
        }

        if (prevGeofencePolylin != null) {
            EditGeofenceFragment.this.mapboxMap.removePolyline(prevGeofencePolylin);
        }

        isDrawing = false;
        mapboxMap.setGesturesManager(this.mapboxDefaultGestureManager, true, true);
        btnAddNewFence.setText("Add new fence");

        txtPolygonDescription.setVisibility(View.GONE);
        llBottomButtons.setVisibility(View.GONE);
        llDrawTypeButtons.setVisibility(View.VISIBLE);

        if (shouldSave) {
            this.updateGeofence();
        }
    }

    private void getGeofenceList() {
        if (selectedTracker == null) {
            return;
        }
        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);
        apiInterface.trackers_id(selectedTracker.get_id()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                Gson gson = new Gson();

                Utils.hideProgress();
                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();

                    Resp_Tracker tracker = null;

                    try {
                        String bodyString = responseBody.string();
                        tracker = gson.fromJson(bodyString, Resp_Tracker.class);

                        EditGeofenceFragment.this.geofenceList = tracker.getGeofence();

                        if (tracker.getLat() != 0) {
                            EditGeofenceFragment.this.selectedTracker.setLat(tracker.getLat());
                        }

                        if (tracker.getLng() != 0) {
                            EditGeofenceFragment.this.selectedTracker.setLng(tracker.getLng());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(EditGeofenceFragment.this.getContext(), error.getMessage(), true);
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
                Utils.showShortToast(EditGeofenceFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    private void updateGeofence() {
        if (selectedTracker == null) {
            return;
        }

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        Gson gson = new Gson();

        HashMap<String, Object> body = new HashMap<>();
        body.put("id", selectedTracker.get_id());
        body.put("geofence", gson.toJsonTree(geofenceList));

        apiInterface.modify(GlobalConstant.X_CSRF_TOKEN,body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d("Geofence", "Update success");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Geofence", "Update failed");
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
