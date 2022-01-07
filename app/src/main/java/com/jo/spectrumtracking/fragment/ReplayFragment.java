package com.jo.spectrumtracking.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.adapter.AssetListSingleSelectRecyclerViewAdapter;
import com.jo.spectrumtracking.adapter.TripLogAdapter;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.fragment.datepicker.DatePickerDialog;
import com.jo.spectrumtracking.fragment.datepicker.DateRangePickedListener;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Replay_TripLog;
import com.jo.spectrumtracking.model.Resp_Error;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.nitri.gauge.Gauge;
import it.carlom.stikkyheader.core.StikkyHeaderBuilder;
import it.carlom.stikkyheader.core.animator.AnimatorBuilder;
import it.carlom.stikkyheader.core.animator.HeaderStikkyAnimator;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textTransform;

//import android.app.DatePickerDialog;
//TODO:- Replace Deprecated Marker!!
public class ReplayFragment extends Fragment {

    ApiInterface apiInterface;
    Call<ResponseBody> apiCall;

    @BindView(R.id.mapView)
    MapView mapView;
    @BindView(R.id.rv_replay_right_options)
    RecyclerView rvAssetSingleSelect;
    @BindView(R.id.txt_speed)
    TextView txtSpeed;
    @BindView(R.id.txt_gage_date)
    TextView txtGageDate;
    @BindView(R.id.img_speed_indicator)
    ImageView imgSpeedIndicator;
    @BindView(R.id.btn_play_pause)
    ImageView btnPlayPause;
    @BindView(R.id.mapPanel)
    FrameLayout mapPanel;
    @BindView(R.id.ll_animation_controls)
    LinearLayout llAnimationControls;
    @BindView(R.id.seek_geo)
    SeekBar seekGeo;
    @BindView(R.id.topView)
    LinearLayout topView;
    @BindView(R.id.btn_toggle_basemap)
    ImageView btn_toggle_basemap;
    @BindView(R.id.layout_trip_log)
    LinearLayout layout_trip_log;
    @BindView(R.id.txt_trip_time)
    TextView txt_trip_time;
    @BindView(R.id.txt_total_stops)
    TextView txt_total_stops;
    @BindView(R.id.txt_top_speed)
    TextView txt_top_speed;
    @BindView(R.id.btn_next_day)
    ImageView btn_next_day;
    @BindView(R.id.btn_next)
    ImageView btn_next_route;
    @BindView(R.id.btn_previous)
    ImageView btn_prev_route;
    @BindView(R.id.btn_prev_day)
    ImageView btn_prev_day;
    @BindView(R.id.tripLog_list)
    RecyclerView tripLog_list;
    @BindView(R.id.bottomView)
    ScrollView bottomView;
    @BindView(R.id.speed_gauge)
    Gauge speed_gauge;

    MapboxMap mapboxMap;
    private String style = GlobalConstant.MAP_BOX_STYLE_URL;

    Calendar replayStartDate;
    Calendar replayEndDate;

    List<Resp_Tracker> trackerList = null;
    Resp_Tracker selectedTracker = null;
    List<Replay_TripLog> tripLogList = null;

    AssetListSingleSelectRecyclerViewAdapter adapter = null;
    TripLogAdapter triplog_adapter = null;
    Timer animatingTimer = null;

    Marker geoMarker = null;

    boolean isFragmentAlive = false;

    List<LatLng> points = null;
    List<Double> speed_Array = null;
    List<String> dateTime_Array = null;
    List<Double> distance_Array = null;
    List<String> layerList_Array = null;
    List<String> sourceList_Array = null;
    List<Integer> speeding_Array = null;
    List<Integer> accAlarm_Array = null;
    List<Integer> onOffEvent = null;
    List<Integer> harshAcce_Array = null;
    List<Integer> harshDece_Array = null;
    List<Integer> idling_Array = null;
    List<String>  addressArray = null;

    int geoMarkerIndex = 0;             /// index of point in the current trip or segment which is shown for now.
    int current_route_segment = -1;     /// -1: Show all trip, otherwise show specified segment

    HashMap<Marker, MarkerModel> markerModels = new HashMap<Marker, MarkerModel>();
    List<RouteSegment> route_segment = new ArrayList<RouteSegment>();
    List<LatLng> displayPoints=null;

    Marker marker = null;
    int index = 0;
    private String displayAddress = "";
    private DatePickerDialog datePickerDialog;
    private DateRangePickedListener dateRangePickedListener;
    boolean isCallingTripInfoApi = false;
    private float defaultZoom = 15.5f;
    private String distUnit = "miles";

    public ReplayFragment() {

    }

    public static ReplayFragment newInstance() {
        ReplayFragment fragment = new ReplayFragment();
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

    public static ReplayFragment newInstance(Resp_Tracker item) {
        ReplayFragment fragment = new ReplayFragment();
        fragment.selectedTracker = item;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Mapbox.getInstance(this.getActivity(), GlobalConstant.MAP_BOX_ACCESS_TOKEN);
        View rootView = inflater.inflate(R.layout.fragment_replay, container, false);

        ButterKnife.bind(this, rootView);

        seekGeo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChangedValue = 0;

            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                geoMarkerIndex = progressChangedValue;
                showReplayPointOfIndex();
            }
        });

        apiInterface = ApiClient.getClient(getContext()).create(ApiInterface.class);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Replay");

        initMap(savedInstanceState);
        initBottomPanel();

        setSpeed(0);

        if (selectedTracker != null) {
            bottomView.setVisibility(View.GONE);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) topView.getLayoutParams();
            params.bottomMargin = 0;
            loadReplay();
        } else {
            if (trackerList == null) {
                trackerList = new ArrayList<>();
            }
            llAnimationControls.setVisibility(View.GONE);
            seekGeo.setVisibility(View.INVISIBLE);
            layout_trip_log.setVisibility(View.GONE);
            StikkyHeaderBuilder.stickTo(bottomView)
                    .setHeader(topView.getId(), (ViewGroup) getView())
                    .minHeightHeader(0)
                    .animator(new ParallaxStikkyAnimator1())
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
                setSelectedTracker(selectedTracker, true);
            } else
                loadAllDrivers();
        }
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            bottomView.getBackground().setAlpha(0);
        } else {
            bottomView.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        }
    }

    private void initBottomPanel() {
        if (!isFragmentAlive) {
            return;
        }

        Calendar day6before = Calendar.getInstance();
        day6before.add(Calendar.DAY_OF_YEAR, 0);

        Calendar day1after = Calendar.getInstance();
        day1after.add(Calendar.DAY_OF_YEAR, 1);

        setReplayStartDate(day6before);
        setReplayEndDate(day1after);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        txt_trip_time.setText(sdf.format(replayStartDate.getTime()));
    }

    private void initMap(Bundle savedInstanceState) {

        mapView.onCreate(savedInstanceState);

        //mapView.setStyleUrl(GlobalConstant.MAP_BOX_STYLE_URL);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                ReplayFragment.this.mapboxMap = mapboxMap;
                ReplayFragment.this.mapboxMap.getUiSettings().setAttributionEnabled(false);
                ReplayFragment.this.mapboxMap.getUiSettings().setLogoEnabled(false);
                ReplayFragment.this.mapboxMap.setStyle(new Style.Builder().fromUrl(style));
                if (GlobalConstant.user_point != null) {
                    ReplayFragment.this.mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(GlobalConstant.user_point.latitude(), GlobalConstant.user_point.longitude())).zoom(10f).build());
                }// ReplayFragment.this.mapboxMap.getUiSettings().setRotateGesturesEnabled(false);
                Utils.setMapUtilityToolsInReplay(ReplayFragment.this.mapboxMap, mapView.getRootView());

                ReplayFragment.this.mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull final Marker marker) {
                        List<Marker> markers = new ArrayList<Marker>(markerModels.keySet());
                        for (Marker marker1 : markers) {
                            if (marker1.isInfoWindowShown()) {
                                marker1.hideInfoWindow();
                            }
                        }

                        final MarkerModel markerModel = markerModels.get(marker);
                        if (markerModel == null) return false;

                        else {
                            if (markerModel.markerproperty.equals(MarkerProperty.HASHDACE) || markerModel.markerproperty.equals(MarkerProperty.HASHACE) || markerModel.markerproperty.equals(MarkerProperty.SPEEDING)) {
                                return false;
                            }

                            String[] splitString = markerModel.eventTime.split(" ");

                            String[] splitString1 = splitString[0].split("/");
                            String[] splitString2 = splitString[1].split(":");

                            String eventTime = splitString1[0] + "/" + splitString1[1] + " " + splitString2[0] + ":" + splitString2[1];

                            marker.setTitle(markerModel.markerproperty + " at " + eventTime + " " + splitString[2]);
                            marker.showInfoWindow(ReplayFragment.this.mapboxMap, mapView);
                        }
                        return false;
                    }
                });
            }
        });
    }

    /***
     * Refresh tracker list panel
     */
    private void setAssetSingleSelectTableData() {
        if (!isFragmentAlive) {
            return;
        }
        // items
        adapter = new AssetListSingleSelectRecyclerViewAdapter(this, trackerList, R.layout.recyclerview_row_asset_single_select);
        rvAssetSingleSelect.setAdapter(adapter);
        rvAssetSingleSelect.setLayoutManager(new LinearLayoutManager(this.getContext()));
        rvAssetSingleSelect.setItemAnimator(new DefaultItemAnimator());
        bottomView.setScrollY(Utils.getPixel(50, getResources()));
    }

    private void setReplayStartDate(Calendar date) {
        if (!isFragmentAlive) {
            return;
        }
        replayStartDate = date;
    }

    private void setReplayEndDate(Calendar date) {
        if (!isFragmentAlive) {
            return;
        }
        replayEndDate = date;
    }

    /***
     * Load tracker list by calling `getAllTrackersWeb` API
     */
    public void loadAllDrivers() {
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

                                if (selectedTracker == null && GlobalConstant.selectedTrackerIds.contains(tracker.get_id())) {
                                    tracker.setSelected(true);
                                    selectedTracker = tracker;
                                } else {
                                    tracker.setSelected(false);
                                }
                                trackerList.add(tracker);
                            }

                            if (selectedTracker == null) selectedTracker = trackerList.get(0);
                            setSelectedTracker(selectedTracker, true);
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
                Utils.showShortToast(ReplayFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    /***
     * Get color code based on speed.
     * @param vertexCount - index of speed in speed_Array
     * @return
     */
    private String getRouteColor(int vertexCount) {
        double speedInValue = speed_Array.get(vertexCount);
        String color = null;
        if (speedInValue != 0) {

            if (speedInValue <= 45) {
                color = "#F96F00";
            } else if (speedInValue > 45 && speedInValue <= 60) {
                color = "#0000FF";
            } else if (speedInValue > 60 && speedInValue <= 80) {
                color = "#32CD32";
            } else if (speedInValue > 80) {
                color = "#ff0000";
            } else {
                color = "#FF0000";
            }
        }
        return color;
    }

    /**
     * Update speedometer
     * @param speed
     */
    public void setSpeed(double speed) {
        if (!isFragmentAlive) {
            return;
        }
        speed_gauge.moveToValue((float)speed);
        speed_gauge.setUpperText(String.format("%.1f", speed));
        txtSpeed.setText(String.format("%.1f", speed));
        imgSpeedIndicator.setRotation(((360 - 90) * (float) speed / 100));
    }

    @OnClick(R.id.btn_prev_day)
    public void OnPreviousDay() {
        if(animatingTimer!=null)
            animatingTimer.cancel();
        animatingTimer = null;
        geoMarkerIndex=0;
        seekGeo.setProgress(0);

        replayStartDate.add(Calendar.DAY_OF_YEAR, -1);
        setReplayStartDate(replayStartDate);

        replayEndDate.add(Calendar.DAY_OF_YEAR, -1);
        setReplayEndDate(replayEndDate);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        txt_trip_time.setText(sdf.format(replayStartDate.getTime()));
        layout_trip_log.setVisibility(View.VISIBLE);
        loadReplay();

    }

    @OnClick(R.id.btn_next_day)
    public void OnNextDay() {

        if(animatingTimer!=null)
            animatingTimer.cancel();
        animatingTimer = null;
        geoMarkerIndex=0;
        seekGeo.setProgress(0);

        replayStartDate.add(Calendar.DAY_OF_YEAR, 1);
        setReplayStartDate(replayStartDate);

        replayEndDate.add(Calendar.DAY_OF_YEAR, 1);
        setReplayEndDate(replayEndDate);

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        txt_trip_time.setText(sdf.format(replayStartDate.getTime()));
        layout_trip_log.setVisibility(View.VISIBLE);
        loadReplay();
    }

    @OnClick(R.id.btn_calendar)
    public void replayStartDateClick() {

        if (!isFragmentAlive) {
            return;
        }
        FragmentManager fragmentManager = this.getActivity().getSupportFragmentManager(); //Initialize fragment manager
        datePickerDialog = DatePickerDialog.newInstance(); // Create datePickerDialog Instance
        dateRangePickedListener = new DateRangePickedListener() {
            @Override
            public void OnDateRangePicked(Calendar from, Calendar to) {
                replayStartDate = from;
                replayEndDate = to;
                setReplayStartDate(from);
                setReplayEndDate(to);
                loadReplay();
            }

            @Override
            public void OnDatePickCancelled() {

            }
        };
        datePickerDialog.setOnDateRangePickedListener(dateRangePickedListener);
        datePickerDialog.show(fragmentManager, "Date Picker3");
    }

    @OnClick(R.id.btn_next)
    public void OnNextRouteSegment() {
        if (points == null || points.size() == 0 || route_segment.size() == 0 || current_route_segment == route_segment.size() - 1)
            return;
        current_route_segment++;
        if (current_route_segment == route_segment.size() - 1) {
            disableButton(btn_next_route);
        }
        if (current_route_segment > 0) enableButton(btn_prev_route);

        LinearLayoutManager llm = (LinearLayoutManager) tripLog_list.getLayoutManager();

        if (llm != null) llm.scrollToPositionWithOffset(current_route_segment, 5);

        layout_trip_log.setVisibility(View.GONE);
        displayRouteSegment(current_route_segment);

        //displayCarAtCenter(current_route_segment);
        setSeekbar();

        if(animatingTimer!=null)
            animatingTimer.cancel();
        animatingTimer = null;
        geoMarkerIndex=0;
        seekGeo.setProgress(0);
    }


    @OnClick(R.id.btn_previous)
    public void OnPreviousRouteSegment() {
        if (points == null || points.size() == 0 || route_segment.size() == 0 || current_route_segment <= 0)
            return;
        current_route_segment--;
        if (current_route_segment <= 0) {
            disableButton(btn_prev_route);
        }
        enableButton(btn_next_route);

        LinearLayoutManager llm = (LinearLayoutManager) tripLog_list.getLayoutManager();
        if (llm != null) llm.scrollToPositionWithOffset(current_route_segment, 5);
        layout_trip_log.setVisibility(View.GONE);

        displayRouteSegment(current_route_segment);

        //displayCarAtCenter(current_route_segment);
        setSeekbar();


        if(animatingTimer!=null)
            animatingTimer.cancel();
        animatingTimer = null;
        geoMarkerIndex=0;
        seekGeo.setProgress(0);
    }

    @OnClick(R.id.btn_play_pause)
    public void playPauseClick() {

        if (!isFragmentAlive) {
            return;
        }
        if (points == null || points.size() == 0) return;
        if (animatingTimer == null) {
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            startAnimation();
        } else {
            animatingTimer.cancel();
            animatingTimer = null;
            btnPlayPause.setImageResource(R.drawable.ic_play);
        }
    }

    /***
     * Update seekbar based on current route segment.
     */
    private void setSeekbar() {
        RouteSegment routeSegment = this.route_segment.get(current_route_segment);
        seekGeo.setMax(routeSegment.endIndex-routeSegment.firstIndex+1);
        geoMarkerIndex = routeSegment.firstIndex;
    }

    @OnClick(R.id.btn_toggle_basemap)
    public void onToogleBaseMapClick() {

        if (style.equals(GlobalConstant.MAP_BOX_STYLE_URL)) {
            style = GlobalConstant.MAP_BOX_SATELLITE_URL;
        } else {
            style = GlobalConstant.MAP_BOX_STYLE_URL;
        }
        mapboxMap.setStyle(new Style.Builder().fromUrl(style));
    }

    /***
     * Update tracker selection.
     * @param selectedTracker - new selected tracker
     * @param shouldLoadReplay - If yes, call loadReplay() function after set variables
     */
    public void setSelectedTracker(Resp_Tracker selectedTracker, boolean shouldLoadReplay) {

        if (!isFragmentAlive) {
            return;
        }

        llAnimationControls.setVisibility(View.VISIBLE);
        layout_trip_log.setVisibility(View.VISIBLE);
        seekGeo.setVisibility(View.VISIBLE);

        this.selectedTracker = selectedTracker;
        for (Resp_Tracker tracker : trackerList) {
            if (selectedTracker.get_id() == tracker.get_id()) tracker.setSelected(true);
        }

        if (shouldLoadReplay) {
            loadReplay();
        }

        setAssetSingleSelectTableData();
        adapter.notifyDataSetChanged();
    }

    /***
     * Load replay data by calling `tripInfo` API
     */
    public void loadReplay() {
        if (!isFragmentAlive) {
            return;
        }
        if (!Utils.isNetworkConnected(this.getContext())) {
            return;
        }
        if (selectedTracker == null || !selectedTracker.getSelected()) {
            Utils.showShortToast(this.getContext(), getString(R.string.please_choose_vehicle), true);
            return;
        }
        if (isCallingTripInfoApi) {
            return;
        }

        current_route_segment = -1;

        disableButton(btn_next_route);
        disableButton(btn_prev_route);
        disableButton(btnPlayPause);
        geoMarker = null;
        bottomView.setScrollY(0);
        // //String id = selectedAsset.get_id();
        // String id = selectedAsset.getAssetId();

        //TODO:- Replace Deprecated Date
        Date startTime = replayStartDate.getTime();
        Date endTime = replayEndDate.getTime();

        startTime.setHours(0);
        startTime.setMinutes(0);
        startTime.setSeconds(0);

        endTime.setHours(0);
        endTime.setMinutes(0);
        endTime.setSeconds(0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String startTimeString = sdf.format(startTime) + ".000Z";
        String endTimeString = sdf.format(endTime) + ".000Z";
        Log.d("timestring", startTimeString + "/" + endTimeString);

        markerModels = new HashMap<Marker, MarkerModel>();

        isCallingTripInfoApi = true;

        if(apiCall!=null) {
            apiCall.cancel();
        }

        apiCall= apiInterface.tripInfo(selectedTracker.getReportingId(), startTimeString, endTimeString, true, true, true, true);
        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                isCallingTripInfoApi = false;

                if (!isFragmentAlive) {
                    return;
                }

                Date currentStartTime = replayStartDate.getTime();
                currentStartTime.setHours(0);
                currentStartTime.setMinutes(0);
                currentStartTime.setSeconds(0);
                String currentStartTimeString = sdf.format(currentStartTime) + ".000Z";

                if (!startTimeString.equals(currentStartTimeString)) {
                    loadReplay();
                    return;
                }

                int code = response.code();

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);

                Gson gson = gsonBuilder.create();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;

                    speed_Array = new ArrayList<>();
                    speeding_Array = new ArrayList<>();
                    accAlarm_Array = new ArrayList<>();
                    dateTime_Array = new ArrayList<>();
                    distance_Array = new ArrayList<>();
                    layerList_Array = new ArrayList<>();
                    sourceList_Array = new ArrayList<>();
                    harshAcce_Array = new ArrayList<>();
                    harshDece_Array = new ArrayList<>();
                    idling_Array = new ArrayList<>();
                    points = new ArrayList<>();

                    try {
                        object = new JSONObject(responseBody.string());

                        Type typeInt = new TypeToken<List<Integer>>() {}.getType();
                        Type typeDouble = new TypeToken<List<Double>>() {}.getType();
                        Type typeString = new TypeToken<List<String>>() {}.getType();
                        Type typeLatLng = new TypeToken<List<List<Double>>>() {}.getType();
                        Type typeDate = new TypeToken<List<Date>>() {}.getType();

                        speed_Array = gson.fromJson(object.get("speed_Array").toString(), typeDouble);
                        speeding_Array = gson.fromJson(object.get("speeding_Array").toString(), typeInt);
//                        harshDriving_Array = gson.fromJson(object.get("speed_Array").toString(), typeDouble);
                        accAlarm_Array = gson.fromJson(object.get("accAlarm_Array").toString(), typeInt);
                        dateTime_Array = gson.fromJson(object.get("dateTime_Array").toString(), typeString);
                        distance_Array = gson.fromJson(object.get("distance_Array").toString(), typeDouble);
//                        layerList_Array = gson.fromJson(object.get("speed_Array").toString(), typeDouble);
//                        sourceList_Array = gson.fromJson(object.get("speed_Array").toString(), typeDouble);
                        harshDece_Array = gson.fromJson(object.get("harshDece_Array").toString(), typeInt);
                        harshAcce_Array = gson.fromJson(object.get("harshAcce_Array").toString(), typeInt);
                        idling_Array = gson.fromJson(object.get("idling_Array").toString(), typeInt);
                        onOffEvent = gson.fromJson(object.get("onOffEvent").toString(), typeInt);
                        addressArray = gson.fromJson(object.get("addressArray").toString(), typeString);

                        ArrayList<Double> latArray = gson.fromJson(object.get("lat_Array").toString(), typeDouble);
                        ArrayList<Double> lngArray = gson.fromJson(object.get("lng_Array").toString(), typeDouble);
                        for (int i=0; i<latArray.size(); i++) {
                            Double lat = latArray.get(i);
                            Double lng = lngArray.get(i);
                            LatLng latLng = new LatLng(lat, lng);
                            points.add(latLng);
                        }

                        ArrayList<Integer> RPM_Array = gson.fromJson(object.get("RPM_Array").toString(), typeInt);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    animation_response();
                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(ReplayFragment.this.getContext(), error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isCallingTripInfoApi = false;
                t.printStackTrace();
                if (!isFragmentAlive) {
                    return;
                }
                Utils.showShortToast(ReplayFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    /***
     * Called after load replay data(tripInfo api's response)
     */
    private void animation_response() {
        if (points == null) {
            return;
        }
        if (points.size() == 0) {
            Utils.showShortToast(ReplayFragment.this.getContext(), getString(R.string.no_trip_change_to_another_day), true);
            tripLog_list.setVisibility(View.GONE);
            disableButton(btn_next_route);
            disableButton(btn_prev_route);
            disableButton(btnPlayPause);
            txt_total_stops.setText("0");
            txt_top_speed.setText("0");
        }

        enableButton(btnPlayPause);

        if (layerList_Array != null) {
            for (int i = 0; i < layerList_Array.size(); i++) {
                mapboxMap.getStyle().removeLayer(layerList_Array.get(i));
            }
        }
        if (sourceList_Array != null) {
            for (int i = 0; i < sourceList_Array.size(); i++) {
                mapboxMap.getStyle().removeSource(sourceList_Array.get(i));
            }
        }

        mapboxMap.getStyle().removeSource("geo_source");
        mapboxMap.getStyle().removeLayer("geo_label");
        mapboxMap.getStyle().removeLayer("geomarker");

        drawPath(null);

        // trip summary table
        report_trip_table();

        if (selectedTracker != null) {
            divideRoute();
            display_trip(addressArray);
        }
    }

    /***
     * Divide trip info data into route segment.
     */
    private void divideRoute() {
        route_segment = new ArrayList<>();
        if (accAlarm_Array.size() == 0) return;

        int firstIndex = 0;

        for (int i = 0; i < onOffEvent.size(); i++) {

            if (i == 0) { //get first
                firstIndex =onOffEvent.get(i);

            } else if (accAlarm_Array.get(onOffEvent.get(i)) == 1 || accAlarm_Array.get(onOffEvent.get(i)) == -1) { //end
                RouteSegment segment = new RouteSegment();

                segment.firstIndex = firstIndex;
                segment.endIndex = onOffEvent.get(i);
                segment.firstValue = accAlarm_Array.get(firstIndex);
                segment.endValue = accAlarm_Array.get(onOffEvent.get(i));

                if (accAlarm_Array.get(firstIndex) != 0) route_segment.add(segment);

                firstIndex = onOffEvent.get(i);
            }
        }

        if (route_segment.size() == 0) {
            disableButton(btn_next_route);
        } else {
            enableButton(btn_next_route);
        }
    }

    /***
     * Draw segment on map
     * @param current_segment
     */
    public void displayRouteSegment(int current_segment) {
        if (route_segment.size() == 0) return;

        RouteSegment routeSegment = this.route_segment.get(current_segment);
        drawPath(routeSegment);
    }

    /***
     * Prepare trip log list
     * @param addressList
     */
    private void display_trip(List<String> addressList) {
//        distUnit = this.checkSelecteTrackerIsUS() ? " miles" : " km";
//        double metricScale = this.checkSelecteTrackerIsUS() ? 1 : 1.60934;

        distUnit = Utils.getDistanceUnit();
        double metricScale = Utils.getDistanceUnit().equals("miles") ? 1 : 1.60934;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        final SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");

        int eventNum = 1;
        int trip_index = 0;

        for (trip_index = 0; trip_index < route_segment.size(); trip_index++) {
            if(eventNum>=onOffEvent.size()) break;

            // define the start and end points of each trip segment. iStart/iEnd
            // each trip start from onOffEvent(eventNum-1) [2]  to onOffEvent(eventNum) [6] shown below
            // [0    0    1   0   0   0   -1]

            int iStart = onOffEvent.get(eventNum-1);
            int iEnd   = onOffEvent.get(eventNum);

            if (iStart == iEnd) break;

            // get maxSpeed of the trip segment.
            double maxSpeed = 0;
            for (int i = iStart; i <= iEnd; i++) {
                maxSpeed = Math.max(maxSpeed, speed_Array.get(i).floatValue());
            }

            // start and end time of the trip
            Date startDate = null;
            Date endDate = null;
            try {
                startDate = formatter.parse(dateTime_Array.get(iStart));
                endDate = formatter.parse(dateTime_Array.get(iEnd));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            // convert time difference to a readable format
            long timeDiff = Math.abs((int) Math.floor(endDate.getTime() / 60000) - (int) Math.floor(startDate.getTime() / 60000));
            int hours = (int) Math.floor(timeDiff / 60);
            double minutes = timeDiff;
            String durationText = "";
            if (minutes >= 60) {
                minutes = minutes - 60 * hours;
                durationText = hours + " " + "hours" + " " + (int) minutes + " minutes";
            } else {
                durationText = (int) minutes + " minutes";
            }

            displayAddress = "";
            if(trip_index<=addressList.size()-1) {
                displayAddress = addressList.get(trip_index);
            }

            DecimalFormat df = new DecimalFormat("#.####");

            if (accAlarm_Array.get(iStart) == 1) { // trip
                int tripEndIndex = iEnd - 1;
                double fDistance = 0.0;
                if (distance_Array.get(tripEndIndex) != null) {
                    fDistance = distance_Array.get(tripEndIndex);
                }

                String distance = String.format("%.2f", fDistance * metricScale);

                if (startDate.compareTo(endDate) >= 0) {
                    Date currentDate = new Date();
                    Date tmpEndDate = endDate;
                    tmpEndDate.setHours(23);
                    tmpEndDate.setMinutes(59);
                    tmpEndDate.setSeconds(59);

                    if (currentDate.compareTo(tmpEndDate) < 0) {
                        endDate = currentDate;
                    } else {
                        endDate = tmpEndDate;
                    }
                }

                if (!displayAddress.equals("")) {
                    tripLogList.add(new Replay_TripLog(dateFormat.format(startDate) + " - " + dateFormat.format(endDate), "Travel " + distance + " " + distUnit + " " + durationText, "Left from " + displayAddress, 0, maxSpeed));
                } else {
                    tripLogList.add(new Replay_TripLog(dateFormat.format(startDate) + " - " + dateFormat.format(endDate),  "Travel " + distance + " " + distUnit + " " + durationText, "", 0, maxSpeed));
                }
                eventNum++;
            } else if (accAlarm_Array.get(iStart) == -1) { //stop
                if (!displayAddress.equals("")) {
                    tripLogList.add(new Replay_TripLog(dateFormat.format(startDate) + " - " + dateFormat.format(endDate), "Stop " + durationText, displayAddress, 1, 0));
                } else {
                    tripLogList.add(new Replay_TripLog(dateFormat.format(startDate) + " - " + dateFormat.format(endDate), "Stop " + durationText, "", 1, 0));
                }
                eventNum++;
            }
        }

        if (route_segment.size() > 0) {
            tripLog_list.setVisibility(View.VISIBLE);

            RouteSegment routeSegment=route_segment.get(route_segment.size()-1);

            if(trip_index==route_segment.size() && routeSegment.endValue==-1) {
                Date startDate = null;

                try {
                    startDate = formatter.parse(dateTime_Array.get(onOffEvent.get(onOffEvent.size()-1)));
                    //endDate = formatter.parse(dateTime_Array.get(iEnd));

                    if (addressList.size() > 0) {
                        tripLogList.add(new Replay_TripLog(dateFormat.format(startDate) + " ", "", "Stop at " + addressList.get(addressList.size()-1), 1, 0));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            tripLog_list.setVisibility(View.GONE);
        }


        triplog_adapter = new TripLogAdapter(this, tripLogList, R.layout.recyclerview_triplog, metricScale);
        tripLog_list.setAdapter(triplog_adapter);
        tripLog_list.setLayoutManager(new LinearLayoutManager(this.getContext()));
        tripLog_list.setItemAnimator(new DefaultItemAnimator());
    }

    /***
     * Prepare trip summary table
     */
    private void report_trip_table() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        txt_trip_time.setText(sdf.format(replayStartDate.getTime()));
        txt_total_stops.setText("0");
        txt_top_speed.setText("0");
        if (points.size() < 2) return;
        float maxSpeed = 0;
        //int total_stops = 0;
        for (int i = 0; i < points.size(); i++) {
            maxSpeed = Math.max(maxSpeed, speed_Array.get(i).floatValue());
        }
        int totalSpeeding=0, totalStops=0, totalAece=0, totalDece=0;
        for (int i=0; i<speeding_Array.size(); i++) {
            if (speeding_Array.get(i)==1) totalSpeeding++;
            if (harshDece_Array.get(i)==1) totalDece++;
            if (harshAcce_Array.get(i)==1) totalAece++;
            if (accAlarm_Array.get(i)==-1) totalStops++;
        }

        txt_total_stops.setText(String.valueOf(totalStops));
//        double metricScale = this.checkSelecteTrackerIsUS() ? 1 : 1.60934;
        double metricScale = Utils.getDistanceUnit().equals("miles") ? 1 : 1.60934;

        txt_top_speed.setText(String.format("%.0f", maxSpeed * metricScale));
    }

    private void addGeoMarker(LatLng point) {
        Point pt1 = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Feature f1 = Feature.fromGeometry(pt1);
        String name = selectedTracker.getDriverName();
        if (name.length() > 5) name = name.substring(0, 5);
        f1.addStringProperty("property", Utils.trucatLabelString(name, 5));
        if (selectedTracker == null) {
            f1.addStringProperty("color", "RED");
        } else {
            f1.addStringProperty("color", selectedTracker.getColor());
        }
        GeoJsonSource geoJsonSource = new GeoJsonSource("geo_source", f1);
        if (mapboxMap == null) return;
        if (mapboxMap.getStyle() == null) return;
        if (mapboxMap.getStyle().getSource("geo_source") == null) {
            mapboxMap.getStyle().addSource(geoJsonSource);
        } else {
            GeoJsonSource temp_geojsonsource = (GeoJsonSource) mapboxMap.getStyle().getSource("geo_source");
            temp_geojsonsource.setGeoJson(f1);
        }
        CircleLayer markerLayer = new CircleLayer("geomarker", "geo_source");
        markerLayer.setSourceLayer("geo_source");

        markerLayer.withProperties(

                circleColor(match(get("color"), rgba(253, 208, 23, 1f),
                        stop("RED", rgba(255, 0, 0, 1f)),
                        stop("ORANGE", rgba(255, 165, 0, 1f)),
                        stop("WHITE", rgba(255, 255, 255, 1f)),
                        stop("GREY", rgba(192, 192, 192, 1f)),
                        stop("BLACK", rgba(20, 20, 20, 1f)),
                        stop("SILVER", rgba(192, 192, 192, 1f)),
                        stop("BLUE", rgba(43, 56, 86, 1f)),
                        stop("GREEN", rgba(37, 65, 23, 1f)),
                        stop("ME", rgba(255, 173, 112, 1f))
                )),
                circleStrokeColor(match(get("color"), rgb(0, 0, 0),
                        stop("RED", rgb(0, 0, 255)),//blue
                        stop("ORANGE", rgb(255, 0, 0)), //red
                        stop("WHITE", rgb(255, 0, 0)),
                        stop("GREY", rgb(0, 0, 255)),
                        stop("BLACK", rgb(255, 0, 0)),
                        stop("SILVER", rgb(255, 165, 0)),
                        stop("BLUE", rgb(255, 165, 0)), //orange
                        stop("GREEN", rgb(255, 0, 0)),
                        stop("ME", rgba(255, 255, 255, 1f))
                )),
                circleStrokeWidth(1f),
                circleRadius(match(get("color"), Expression.ExpressionLiteral.literal(18f),

                        stop("ME", Expression.ExpressionLiteral.literal(20f))
                )));

        mapboxMap.getStyle().addLayer(markerLayer);

        SymbolLayer labelLayer = new SymbolLayer("geo_label", "geo_source");
        labelLayer.setSourceLayer("geo_source");
        labelLayer.setProperties(textField("{property}"),
                textSize(14f),
                textColor(Color.BLACK),
                textHaloColor(Color.WHITE),
                textHaloWidth(1f),
                textAllowOverlap(true)
        );
        mapboxMap.getStyle().addLayer(labelLayer);
    }

    /***
     * Draw route segment on map
     * @param routeSegment
     */
    private void drawPath(RouteSegment routeSegment) {
        List<Integer> accAlarms=null;

        if(routeSegment!=null) {
            displayPoints = points.subList(routeSegment.firstIndex, routeSegment.endIndex + 1);
            accAlarms = accAlarm_Array.subList(routeSegment.firstIndex, routeSegment.endIndex + 1);
        }
        else {
            displayPoints=new ArrayList<>(points);
            accAlarms=new ArrayList<>(accAlarm_Array);
        }

        if (geoMarker != null) {
            mapboxMap.removeMarker(geoMarker);
            geoMarker = null;
        }
        try {
            mapboxMap.clear();
            mapboxMap.removeAnnotations();
            for (int i = 0; i < layerList_Array.size(); i++) {
                mapboxMap.getStyle().removeLayer(layerList_Array.get(i));
            }
            for (int i = 0; i < sourceList_Array.size(); i++) {
                mapboxMap.getStyle().removeSource(sourceList_Array.get(i));
            }
            mapboxMap.getStyle().removeSource("geo_source");
            mapboxMap.getStyle().removeLayer("geo_label");
            mapboxMap.getStyle().removeLayer("geomarker");
            final IconFactory iconFactory = IconFactory.getInstance(ReplayFragment.this.getContext());

            ReplayFragment.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addGeoMarker(displayPoints.get(0));
                }
            });
            index = 0;
            LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();
            LatLng lastCoordinate = displayPoints.get(0);

            if (current_route_segment != -1 && route_segment.get(current_route_segment).firstValue != 1) {
                try {
                    latLngBoundsBuilder.include(displayPoints.get(0));
                    marker = mapboxMap.addMarker(new MarkerOptions()
                            .position(displayPoints.get(0))
                            .setIcon(iconFactory.fromResource(R.drawable.stop))
                    );
                    Point pt1 = Point.fromLngLat(displayPoints.get(0).getLongitude(), displayPoints.get(0).getLatitude());
                    Feature f1 = Feature.fromGeometry(pt1);
                    f1.addStringProperty("property", Utils.trucatLabelString(String.valueOf(current_route_segment + 1), 5));
                    GeoJsonSource geoJsonSource = new GeoJsonSource("stopId" + index, f1);
                    mapboxMap.getStyle().removeSource(geoJsonSource.getId());
                    mapboxMap.getStyle().addSource(geoJsonSource);
                    CircleLayer layer = new CircleLayer("stopCircle" + index, "stopId" + index);
                    layer.setProperties(
                            PropertyFactory.visibility(Property.VISIBLE),
                            PropertyFactory.circleRadius(9f),
                            PropertyFactory.circleColor(Color.RED),
                            PropertyFactory.circleStrokeWidth(1f),
                            PropertyFactory.circleStrokeColor(Color.WHITE),
                            PropertyFactory.iconImage("stopImage" + index)
                    );
                    // mapboxMap.getStyle().addLayer(layer);
                    SymbolLayer labelLayer = new SymbolLayer("stopLabel" + index, "stopId" + index);
                    labelLayer.setProperties(textField("{property}"),
                            textSize(11f),
                            textColor(Color.WHITE),
                            textAllowOverlap(true)
                    );
                    // mapboxMap.getStyle().addLayer(labelLayer);
                    layerList_Array.add("stopCircle" + index);
                    layerList_Array.add("stopLabel" + index);
                    sourceList_Array.add("stopId" + index);
                    markerModels.put(marker, new MarkerModel(MarkerProperty.STOP, dateTime_Array.get(routeSegment.firstIndex), index));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                for (int i = 0; i < displayPoints.size(); i++) {
                    latLngBoundsBuilder.include(displayPoints.get(i));
                }

                PolylineOptions polylineOptions = null;

                String color = "#0000FF";
                String previousColor = "#FE2EF7";
                lastCoordinate = displayPoints.get(displayPoints.size() - 1);
                for (int i = 0; i < displayPoints.size() - 1; i++) {
                    LatLng start = displayPoints.get(i);
                    LatLng end = displayPoints.get(i + 1);
                    color = getRouteColor(i);
                    if (color == null) color = "#0000FF";
                    if (polylineOptions == null) {
                        polylineOptions = new PolylineOptions();
                        polylineOptions.add(start, end);
                    } else {
                        if (!color.equals(previousColor)) {
                            polylineOptions.color(Color.parseColor(previousColor)).width(4);
                            Polyline polyline = mapboxMap.addPolyline(polylineOptions);
                            polylineOptions = new PolylineOptions();
                            polylineOptions.add(start, end);
                        } else polylineOptions.add(end);
                    }

                    if (end.getLatitude() == lastCoordinate.getLatitude() && end.getLongitude() == lastCoordinate.getLongitude()) {
                        polylineOptions.color(Color.parseColor(color)).width(4);
                        Polyline polyline = mapboxMap.addPolyline(polylineOptions);
                    }
                    previousColor = color;

                }

                int routeLength = displayPoints.size();
                seekGeo.setMax(routeLength - 1);
                geoMarkerIndex = 0;

                tripLogList = new ArrayList<Replay_TripLog>();


                int firstStop = accAlarms.size()-1, lastStop = 0;
             /*   onOffEvent = new ArrayList<>();

                for (int i = 0; i < accAlarm_Array.size(); i++) {
                    if (accAlarm_Array.get(i) != 0) {

                        onOffEvent.add(i);
                        if (i < firstStop) firstStop = i;
                        if (i > lastStop) lastStop = i;
                    }
                }*/
                // index=0;
                getReportLog(firstStop, lastStop, routeLength);
            }
            try {

                LatLngBounds latLngBounds = latLngBoundsBuilder.build();
                ///////////////////////////////////////////////////////
                LatLngBounds.Builder reLatLngBoundsBuilder = new LatLngBounds.Builder();
                reLatLngBoundsBuilder.include(new LatLng(latLngBounds.getCenter().getLatitude()+latLngBounds.getLatitudeSpan(),latLngBounds.getCenter().getLongitude()+latLngBounds.getLongitudeSpan()));
                reLatLngBoundsBuilder.include(new LatLng(latLngBounds.getCenter().getLatitude()-latLngBounds.getLatitudeSpan(),latLngBounds.getCenter().getLongitude()-latLngBounds.getLongitudeSpan()));
                LatLngBounds reLatLngBounds = reLatLngBoundsBuilder.build();
                ///////////////////////////////////////////////////////

                mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(reLatLngBounds, 0));

                double zoomScale = mapboxMap.getCameraPosition().zoom;

                if (zoomScale > defaultZoom) {
                    CameraPosition old = mapboxMap.getCameraPosition();
                    CameraPosition pos = new CameraPosition.Builder()
                            .target(new LatLng(old.target.getLatitude(), old.target.getLongitude()))
                            .zoom(defaultZoom)
                            .build();
                    mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
                }

            } catch (Exception ex) {

                // LatLng lastCoordinate=points.get(points.size()-1);
                ex.printStackTrace();
                LatLng latLng=displayPoints.get(0);
                CameraPosition pos = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(defaultZoom)
                        .build();
                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));


                // CameraPosition cameraPosition = new CameraPosition.Builder().target(lastCoordinate).bearing(0).tilt(15).zoom(defaultZoom).build();
                // mapboxMap.setCameraPosition(cameraPosition);
                //  return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /***
     * Get markerModels for drawing on map
     * @param firstStop
     * @param lastStop
     * @param routeLength
     */
    private void getReportLog(final int firstStop, final int lastStop, final int routeLength) {
        if (index >= points.size()) return;
        LatLng currentPoint = points.get(index);
        final IconFactory iconFactory = IconFactory.getInstance(ReplayFragment.this.getContext());

        if (accAlarm_Array.size() > index) {
            if (accAlarm_Array.get(index) == -1) {
                if (current_route_segment != -1) {
                    marker = mapboxMap.addMarker(new MarkerOptions()
                            .position(currentPoint)
                            .setIcon(iconFactory.fromResource(R.drawable.stop))
                    );
                    Point pt1 = Point.fromLngLat(currentPoint.getLongitude(), currentPoint.getLatitude());
                    Feature f1 = Feature.fromGeometry(pt1);
                    f1.addStringProperty("property", Utils.trucatLabelString(String.valueOf(current_route_segment + 1), 5));
                    GeoJsonSource geoJsonSource = new GeoJsonSource("stopId" + index, f1);
                    mapboxMap.getStyle().addSource(geoJsonSource);
                    CircleLayer layer = new CircleLayer("stopCircle" + index, "stopId" + index);
                    layer.setProperties(
                            PropertyFactory.visibility(Property.VISIBLE),
                            PropertyFactory.circleRadius(9f),
                            PropertyFactory.circleColor(Color.RED),
                            PropertyFactory.circleStrokeWidth(1f),
                            PropertyFactory.circleStrokeColor(Color.WHITE),
                            PropertyFactory.iconImage("stopImage" + index)
                    );
                    // mapboxMap.getStyle().addLayer(layer);
                    SymbolLayer labelLayer = new SymbolLayer("stopLabel" + index, "stopId" + index);
                    labelLayer.setProperties(textField("{property}"),
                            textSize(11f),
                            textColor(Color.WHITE),
                            textAllowOverlap(true)
                    );
                    // mapboxMap.getStyle().addLayer(labelLayer);
                    layerList_Array.add("stopCircle" + index);
                    layerList_Array.add("stopLabel" + index);
                    sourceList_Array.add("stopId" + index);
                } else {
                    marker = mapboxMap.addMarker(new MarkerOptions()
                            .position(currentPoint)
                            .setIcon(iconFactory.fromResource(R.drawable.stop_all))
                    );
                }
                markerModels.put(marker, new MarkerModel(MarkerProperty.STOP, dateTime_Array.get(index), index));
            } else if (accAlarm_Array.get(index) == 1) {
                if (current_route_segment != -1) {
                    marker = mapboxMap.addMarker(new MarkerOptions()
                            .position(currentPoint)
                            .setIcon(iconFactory.fromResource(R.drawable.start))
                    );
                    Point pt1 = Point.fromLngLat(currentPoint.getLongitude(), currentPoint.getLatitude());
                    Feature f1 = Feature.fromGeometry(pt1);
                    f1.addStringProperty("property", Utils.trucatLabelString(String.valueOf(current_route_segment + 1), 5));
                    GeoJsonSource geoJsonSource = new GeoJsonSource("startId" + index, f1);
                    mapboxMap.getStyle().addSource(geoJsonSource);
                    CircleLayer layer = new CircleLayer("startCircle" + index, "startId" + index);
                    layer.setProperties(
                            PropertyFactory.visibility(Property.VISIBLE),
                            PropertyFactory.circleRadius(9f),
                            PropertyFactory.circleColor(Color.parseColor("#7bb24d")),
                            PropertyFactory.circleStrokeWidth(1f),
                            PropertyFactory.circleStrokeColor(Color.WHITE),
                            PropertyFactory.iconImage("startImage" + index)
                    );
                    // mapboxMap.getStyle().addLayer(layer);
                    SymbolLayer labelLayer = new SymbolLayer("startLabel" + index, "startId" + index);
                    labelLayer.setProperties(textField("{property}"),
                            textSize(11f),
                            textColor(Color.WHITE),
                            textAllowOverlap(true)
                    );
                    // mapboxMap.getStyle().addLayer(labelLayer);
                    layerList_Array.add("startCircle" + index);
                    layerList_Array.add("startLabel" + index);
                    sourceList_Array.add("startId" + index);
                } else {
                    marker = mapboxMap.addMarker(new MarkerOptions()
                            .position(currentPoint)
                            .setIcon(iconFactory.fromResource(R.drawable.start_all))
                    );
                }
                markerModels.put(marker, new MarkerModel(MarkerProperty.START, dateTime_Array.get(index), index));
            }
        }

        if (speeding_Array.size() > index) {
            if (speeding_Array.get(index) == 1) {
                marker = mapboxMap.addMarker(new MarkerOptions()
                        .position(currentPoint)
                        .setIcon(iconFactory.fromResource(R.drawable.totalspeeding))
                );
                markerModels.put(marker, new MarkerModel(MarkerProperty.SPEEDING, dateTime_Array.get(index), index));
            }
        }

        if (harshAcce_Array.size() > index) {
            if (harshAcce_Array.get(index) == 1) {
                marker = mapboxMap.addMarker(new MarkerOptions()
                        .position(currentPoint)
                        .setIcon(iconFactory.fromResource(R.drawable.harsh_acce_image))

                );
                markerModels.put(marker, new MarkerModel(MarkerProperty.HASHACE, dateTime_Array.get(index), index));
            }
        }

        if (harshDece_Array.size() > index) {
            if (harshDece_Array.get(index) == 1) {
                marker = mapboxMap.addMarker(new MarkerOptions()
                        .position(currentPoint)
                        .setIcon(iconFactory.fromResource(R.drawable.harsh_dece_image))


                );
                markerModels.put(marker, new MarkerModel(MarkerProperty.HASHDACE, dateTime_Array.get(index), index));
            }
        }
        if (idling_Array.size() > index) {
            if (idling_Array.get(index) == 1) {

                marker = mapboxMap.addMarker(new MarkerOptions()
                        .position(currentPoint)
                        .setIcon(iconFactory.fromResource(R.drawable.idling_image))

                );
                markerModels.put(marker, new MarkerModel(MarkerProperty.IDLING, dateTime_Array.get(index), index));

            }
        }
        index++;
        if (index <= routeLength) {
            getReportLog(firstStop, lastStop, routeLength);
        }
    }

    /***
     * Start play animation when user tap on play button.
     */
    private void startAnimation() {

        if (animatingTimer != null) {
            animatingTimer.cancel();
        } else {
            animatingTimer = new Timer();
        }

        animatingTimer.schedule(new TimerTask() {
            @SuppressLint("NewApi")
            @Override
            public void run() {

                if (!isFragmentAlive || points == null) {
                    return;
                }

                if (geoMarkerIndex >= displayPoints.size()) {
                    animatingTimer.cancel();
                    animatingTimer = null;

                    ReplayFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnPlayPause.setImageResource(R.drawable.ic_play);
                            seekGeo.setEnabled(true);
                        }
                    });
                    return;
                }

                showReplayPointOfIndex();
                geoMarkerIndex++;

            }
        }, 0, 1000);

    }

    /***
     * Update map marker and UI for index of point in current segment route
     */
    private void showReplayPointOfIndex() {
        if (points == null || displayPoints.size() == 0) return;

        if (geoMarkerIndex >=displayPoints.size()) {
            if (animatingTimer != null) {
                animatingTimer.cancel();
            }
            animatingTimer = null;

            ReplayFragment.this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnPlayPause.setImageResource(R.drawable.ic_play);
                    seekGeo.setEnabled(true);
                    if (geoMarker == null) {
                        return;
                    }
                }
            });

            return;
        }

        ReplayFragment.this.getActivity().runOnUiThread(() -> {

            if(geoMarkerIndex==displayPoints.size()) return;
            LatLng point = displayPoints.get(geoMarkerIndex);

            if (point == null) {
                System.out.println(geoMarkerIndex);
                System.out.println(" point null");
            }
            if (mapboxMap == null) return;
            GeoJsonSource temp_geojsonsource = (GeoJsonSource) mapboxMap.getStyle().getSource("geo_source");
            if (temp_geojsonsource == null) {
                addGeoMarker(point);
            }
            temp_geojsonsource = (GeoJsonSource) mapboxMap.getStyle().getSource("geo_source");
            Point pt1 = Point.fromLngLat(point.getLongitude(), point.getLatitude());
            Feature f1 = Feature.fromGeometry(pt1);

            String name = selectedTracker.getDriverName();
            if(name==null) {
                name="Unknown";
            }

            if (name.length() > 5) {
                name = name.substring(0, 5);
            }

            f1.addStringProperty("property", Utils.trucatLabelString(name, 5));

            if(selectedTracker==null) return;

            if(selectedTracker.getColor()==null)
                f1.addStringProperty("color", "#F96F00");
              else
               f1.addStringProperty("color", selectedTracker.getColor());
            if (temp_geojsonsource != null) temp_geojsonsource.setGeoJson(f1);
            LatLngBounds latLngBounds = mapboxMap.getProjection().getVisibleRegion().latLngBounds;
            if (!latLngBounds.contains(point))
                ReplayFragment.this.mapboxMap.setCameraPosition(new CameraPosition.Builder().target(point).build());

            ReplayFragment.this.mapboxMap.setCameraPosition(new CameraPosition.Builder().target(mapboxMap.getCameraPosition().target).bearing(0).build());

        });

        if(this.current_route_segment<0) {
            current_route_segment=0;
        }

        if (this.current_route_segment < this.route_segment.size()) {
            RouteSegment routeSegment = this.route_segment.get(this.current_route_segment);

            if(geoMarkerIndex+routeSegment.firstIndex >= speed_Array.size()) return;

            final double speed = Math.min(speed_Array.get(geoMarkerIndex+routeSegment.firstIndex), 100);
            final String dateString = dateTime_Array.get(geoMarkerIndex+routeSegment.firstIndex);
            //TODO:- Fix the Date Problem
            ReplayFragment.this.getActivity().runOnUiThread(() -> {
                setSpeed(speed);
//            String date = convertDateStringFormat(dateString, "MM/dd hh:mm aaa", "MM/dd hh:mm aaa");
//                String date = convertDateStringFormat(dateString, "MM/dd/yyyy hh:mm:ss aa", "MM/dd hh:mm aa");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

                try {
                    Date date = formatter.parse(dateString);

                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm aa");
                    txtGageDate.setText(sdf.format(date));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else if (this.geoMarkerIndex < this.points.size()) { // (this.geoMarkerIndex < this.assetLogList.size()) {
//            Resp_AssetLog log = this.assetLogList.get(this.geoMarkerIndex);
//
//            final double speed = Math.min(log.getSpeedInMph(), 100);
//            final Date date = log.getDateTime();

            final double speed = Math.min(speed_Array.get(this.geoMarkerIndex), 100);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

            try {
                final Date date = sdf.parse(dateTime_Array.get(this.geoMarkerIndex));
                ReplayFragment.this.getActivity().runOnUiThread(() -> {
                    setSpeed(speed);
                    DateFormat dateFormat = new SimpleDateFormat("MM/dd hh:mm aa");
                    String dateString = dateFormat.format(date);
                    txtGageDate.setText(dateString);
                });

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        seekGeo.post(() -> seekGeo.setProgress(geoMarkerIndex));
        Runtime.getRuntime().gc();
        System.gc();
    }

    private boolean checkSelecteTrackerIsUS() {
        String country = "";

        if (selectedTracker != null && selectedTracker.getCountry() != null) {
            country = selectedTracker.getCountry();
        }

        return country.equals("United States") || country.equals("US") || country.length() == 0;
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

    public void disableButton(ImageView button) {
        button.setColorFilter(getResources().getColor(R.color.btn_disabled));
    }

    public void enableButton(ImageView button) {
        button.setColorFilter(getResources().getColor(R.color.main));
    }

    public interface MarkerProperty {
        String STOP = "stop";
        String IDLING = "idling";
        String SPEEDING = "speeding";
        String HASHACE = "harshAcce";
        String HASHDACE = "harshDace";
        String START = "start";
    }

    private class ParallaxStikkyAnimator1 extends HeaderStikkyAnimator {
        @Override
        public AnimatorBuilder getAnimatorBuilder() {
            View mHeader_image = getHeader().findViewById(R.id.mapPanel);
            return AnimatorBuilder.create().applyVerticalParallax(mHeader_image, 1);
        }
    }

    private class MarkerModel {
        public int index;

        public String markerproperty;
        public String eventTime;

        public MarkerModel(String markerproperty, String eventTime, int index) {
            this.markerproperty = markerproperty;
            this.eventTime = eventTime;
            this.index = index;
        }
    }

    private class RouteSegment {
        public int firstIndex;
        public int endIndex;
        public int firstValue;
        public int endValue;

    }
}
