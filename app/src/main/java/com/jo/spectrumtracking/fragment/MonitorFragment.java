package com.jo.spectrumtracking.fragment;

import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
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
import com.jo.spectrumtracking.adapter.VelocityRecyclerViewAdapter;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Landmark;
import com.jo.spectrumtracking.model.Resp_Alarm;
import com.jo.spectrumtracking.model.Resp_Error;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.jo.spectrumtracking.twilio.chat.channels.ChannelManager;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.ntt.customgaugeview.library.GaugeView;
import com.twilio.chat.Channel;
import com.twilio.chat.Member;
import com.twilio.chat.StatusListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.nitri.gauge.Gauge;
import it.carlom.stikkyheader.core.StikkyHeaderBuilder;
import it.carlom.stikkyheader.core.animator.AnimatorBuilder;
import it.carlom.stikkyheader.core.animator.HeaderStikkyAnimator;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleStrokeWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.hillshadeShadowColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineDasharray;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineTranslate;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloWidth;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

public class MonitorFragment extends Fragment {
    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";

    ApiInterface apiInterface;
    Call<ResponseBody> apiCall;

    @BindView(R.id.mapView)
    MapView mapView;
    @BindView(R.id.rv_monitor_right_options)
    RecyclerView rvMonitorRightOptions;
    @BindView(R.id.rv_monitor_velocity)
    RecyclerView rvMonitorVelocity;
    @BindView(R.id.layout_velocityTable)
    LinearLayout layout_velocityTable;
    @BindView(R.id.btn_toggle_basemap)
    ImageView btn_toggle_basemap;
    @BindView(R.id.btn_ShowUser)
    ImageView btn_show_user;
    @BindView(R.id.header_layout)
    View topView;
    @BindView(R.id.bottomView)
    ScrollView bottomView;
    @BindView(R.id.txtUnreadCount)
    TextView txtUnreadCount;
    @BindView(R.id.btn_toggle_velocity)
    ImageView btn_toggle_velocity;
    @BindView(R.id.txt_toggle_info_title)
    TextView txtToggleInfoTitle;
    @BindView(R.id.speed_view)
    Gauge speedGauge;
    @BindView(R.id.rpm_view)
    Gauge  rpmGauge;
    @BindView(R.id.battery_view)
    Gauge batteryGauge;
    public MapboxMap mapboxMap;
    private List<Marker> landMarkers;
    Marker geoMarker = null;

    private final Handler mHendler = new Handler();
    List<Resp_Tracker> trackerList = null;
    List<Resp_Tracker> selectedTrackers = null;
    public static List<Resp_Tracker> oldTrackers = new ArrayList<>();
    Resp_Alarm respAlarm;

    boolean isFragmentAlive;
    private String style = GlobalConstant.MAP_BOX_STYLE_URL;
    private boolean firstAppearFlag = true;
    private boolean dialog_flag = false;
    private boolean userShowFlag = false;
    private float defaultZoom = 15.5f;
    private boolean noSelectedFlag = false;
    private JSONObject landmark_icons = new JSONObject();
    private FeatureCollection dashedLineDirectionsFeatureCollection;

    private ArrayList<Polyline> trackLines;
    private AssetListMultiSelectRecyclerViewAdapter bottom_table_adapter;
    FeatureCollection featureCollection;

    Drawable drawable;
    Bitmap marker;

    public MonitorFragment() {
        // Required empty public constructor
    }

    public static MonitorFragment newInstance() {
        MonitorFragment fragment = new MonitorFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFragmentAlive = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        SharedPreferences preferences = getActivity().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        this.userShowFlag = preferences.getBoolean("sUserShowFlag", false);

        Mapbox.getInstance(this.getActivity(), GlobalConstant.MAP_BOX_ACCESS_TOKEN);
        View rootView = inflater.inflate(R.layout.fragment_monitor, container, false);
        ButterKnife.bind(this, rootView);

        //Convert Vector to Bitmap to use it as a Marker.
        drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_marker_blue, null);
        marker = BitmapUtils.getBitmapFromDrawable(drawable);

        Resources resources = this.getActivity().getResources();
        Drawable drawable = resources.getDrawable(R.drawable.btn_hide);
        btn_toggle_velocity.setImageDrawable(drawable);
        layout_velocityTable.setVisibility(View.GONE);

        apiInterface = ApiClient.getClient(getContext()).create(ApiInterface.class);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(getString(R.string.monitor));
        landMarkers = new ArrayList<>();
        initMap(savedInstanceState);
        
        if (GlobalConstant.app_user == null) {
            getUserInfo();
        }

        GlobalConstant.AllTrackerList.clear();
        loadAllDrivers();

        rvMonitorRightOptions.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvMonitorRightOptions.setHasFixedSize(true);
        rvMonitorRightOptions.setNestedScrollingEnabled(false);
        rvMonitorVelocity.setLayoutManager(new WrapContentLinearLayoutManager(this.getContext()));

        StikkyHeaderBuilder.stickTo(bottomView)
                .setHeader(topView.getId(), (ViewGroup) getView())
                .minHeightHeader(0)
                .animator(new ParallaxStikkyAnimator())
                .build();

        updateUnreadCount();
        checkChatInvitation();

        speedGauge.setLowerText(Utils.getDistanceUnit().equals("miles") ? " mph " : " kmh ");

    }

    @Override
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onResume() {
        super.onResume();

        if (!isFragmentAlive) {
            // isPause = false;
            isFragmentAlive = true;
            loadAllDrivers();
            Toast.makeText(getContext(), "onResume", Toast.LENGTH_SHORT).show();
        }

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
    public void onPause() {
        super.onPause();

        Utils.hideProgress();
    }

    @Override
    public void onStop() {
        super.onStop();

    }

    /***
     * Load all trackers by calling 'getAllTrackersWeb' api
     */
    public void loadAllDrivers() {

        if (!isFragmentAlive) {
            return;
        }

        if (trackerList == null) {
            trackerList = new ArrayList<>();
        }
        if (selectedTrackers == null) {
            selectedTrackers = new ArrayList<>();
        }
        if (GlobalConstant.alerts == null) {
            GlobalConstant.alerts = new HashMap<String, String>();
        }

        selectedTrackers.clear();

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

                        /// Update tracker list.
                        SharedPreferences preferences = getActivity().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
                        Boolean isPhoneTracking = preferences.getBoolean("sPhoneTracking", false);

                        List<Resp_Tracker> oldList = new ArrayList<>();
                        for (Resp_Tracker tracker : trackerList) {
                            oldList.add(tracker);
                        }
                        trackerList.clear();

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

                                if (tracker.getUserId() != null && !tracker.getUserId().equals(GlobalConstant.app_user.getString("_id"))) {
                                    acceptSharedTracker(tracker, oldList);
                                } else {
                                    addDevice(tracker, oldList, false);
                                }
                            }

                            boolean selected_flag = false;

                            GlobalConstant.selectedTrackerIds.clear();
                            for (Resp_Tracker tracker : trackerList) {
                                if (tracker.getSelected()) {
                                    selected_flag = true;
                                    selectedTrackers.add(tracker);
                                    GlobalConstant.selectedTrackerIds.add(tracker.get_id());
                                }
                            }

                            if (!selected_flag && trackerList.size() > 0 && firstAppearFlag) {
                                /// No selection at first load
                                trackerList.get(0).setSelected(true);

                                selectedTrackers.add(trackerList.get(0));
                                GlobalConstant.selectedTrackerIds.add(trackerList.get(0).get_id());
                                addTrackPoints(trackerList.get(0));
                                showCarsOnMap(true, false);
                            } else {
                                showCarsOnMap(false, false);
                            }
                            firstAppearFlag = false;

                            GlobalConstant.AllTrackerList = trackerList;
                            Utils.hideProgress();
                            onAllTrackersLoaded();
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
                Utils.showShortToast(MonitorFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    /***
     * Show ME marker on map
     * @return
     */
    private void addGPSLocation() {
        if (geoMarker != null) {
            mapboxMap.removeMarker(geoMarker);
            geoMarker = null;
        }
        if (GlobalConstant.user_point == null) {
            return;
        }

        BitmapDrawable bitmapDraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_marker_blue);
        Bitmap b = bitmapDraw.getBitmap();
        double ratio = (double)b.getWidth() / (double)b.getHeight();
        Bitmap resizedBmp = Bitmap.createScaledBitmap(b, 80, (int)(80 / ratio), false);

        final IconFactory iconFactory = IconFactory.getInstance(MonitorFragment.this.getContext());
        LatLng latLng = new LatLng(GlobalConstant.user_point.latitude(), GlobalConstant.user_point.longitude());
        geoMarker = mapboxMap.addMarker(new MarkerOptions()
                .position(latLng)
                .setIcon(iconFactory.fromBitmap(resizedBmp))
        );
    }

    private void hideGPSLocation() {
        if (geoMarker != null) {
            mapboxMap.removeMarker(geoMarker);
            geoMarker = null;
        }
    }

    private void showAddSharedTrackerDialog(final Resp_Tracker tracker, List<Resp_Tracker> oldTrackers) {
        dialog_flag = true;

        if(apiCall!=null) {
            apiCall.cancel();
        }

        apiCall= apiInterface.users_id(tracker.getUserId());
        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isFragmentAlive) {
                    return;
                }
                int code = response.code();
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
                if (code == 200) {
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());
                        final String firstName = object.getString("firstName");
                        final String lastName = object.getString("lastName");

                        Utils.showSweetAlert(MonitorFragment.this.getContext(), tracker.getPlateNumber(), firstName + " " + lastName + getString(R.string.__want_to_share_this_vehicle_information_with_you), "Yes", "No", SweetAlertDialog.WARNING_TYPE, new Utils.OnSweetAlertListener() {
                            @Override
                            public void onConfirm() {
                                setShareFlag(tracker.getReportingId(), "1");
                                addDevice(tracker, oldTrackers, true);
                                dialog_flag = false;
                            }

                            @Override
                            public void onCancel() {
                                setShareFlag(tracker.getReportingId(), "-1");
                                dialog_flag = false;
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(MonitorFragment.this.getContext(), "response parse error5");
                    }

                } else {
//                    Utils.showShortToast(MonitorFragment.this.getContext(), "response parse error6");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(MonitorFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    /***
     * This function is called after selecting "Yes" at sharing dialog
     * @param reportId
     * @param flag
     */
    private void setShareFlag(String reportId, String flag) {

        //ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        HashMap<String, Object> body = new HashMap<>();
        body.put("reportId", reportId);
        body.put("flag", flag);
        if(apiCall!=null) {
            apiCall.cancel();
        }

        apiCall=  apiInterface.setShareFlag(body);

        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isFragmentAlive) {
                    return;
                }
                int code = response.code();
                if (code == 200) {
                    ResponseBody responseBody = response.body();
                    getUserInfo();
                } else {
//                    Utils.showShortToast(MonitorFragment.this.getContext(), "response parse error8");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(MonitorFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    /***
     * Get UserInfo by calling API
     */
    private void getUserInfo() {
        if(apiCall!=null) {
            apiCall.cancel();
        }

        apiCall = apiInterface.getUserInfo(GlobalConstant.X_CSRF_TOKEN);

        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());
                        GlobalConstant.app_user = object;
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.showShortToast(MonitorFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
                    }
                } else {
                    Utils.showShortToast(MonitorFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(MonitorFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    /***
     * Add tracker to trackerList with saving selections
     * @param tracker
     * @param oldTrackers
     * @param shouldUpdateRightPanel
     */
    private void addDevice(Resp_Tracker tracker, List<Resp_Tracker> oldTrackers, boolean shouldUpdateRightPanel) {
        int trackerIndex = indexOfTrackerList(oldTrackers, tracker.get_id());
        Boolean isExist = trackerIndex > -1;
        Log.d("status------", ""+trackerIndex+": "+tracker.get_id());
        if (isExist) {
            Boolean changeflag = true;
            if(tracker.getLat()==oldTrackers.get(trackerIndex).getLat() && tracker.getLng()==oldTrackers.get(trackerIndex).getLng())changeflag = false;
            tracker.setChangeFlag(changeflag);
            tracker.setPhotoUpload(oldTrackers.get(trackerIndex).getPhotoUpload());
        }

        if (GlobalConstant.selectedTrackerIds.contains(tracker.get_id())) {
            tracker.setSelected(true);
            addTrackPoints(tracker);
        } else {
            tracker.setSelected(false);
        }

        this.trackerList.add(tracker);

        if (shouldUpdateRightPanel) {
            this.setBottomListData();
        }
    }

    /***
     * Returns index of tracker which has `id` in trackerList
     * @param c
     * @param id
     * @return
     */
    public int indexOfTrackerList(List<Resp_Tracker> c, String id) {
        for (int i = 0; i < c.size(); i++) {
            Resp_Tracker o = c.get(i);
            Log.d("%%%%", c.size() + ": " + o.get_id()+": "+id);
            if (o != null && o.get_id().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /***
     * Returns tracker object which has `id` from trackerList
     * @param c
     * @param id
     * @return
     */
    public Resp_Tracker getTracker(List<Resp_Tracker> c, String id) {
        for (int i = 0; i < c.size(); i++) {
            Resp_Tracker o = c.get(i);
            if (o != null && o.get_id().equals(id)) {
                return o;
            }
        }
        return null;
    }

    /***
     * Accept shared tracker and add to trackerList
     * @param tracker
     * @param oldTrackers
     */
    private void acceptSharedTracker(Resp_Tracker tracker, List<Resp_Tracker> oldTrackers) {
        Boolean exist_flag = false;
        try {
            for (int i = 0; i < GlobalConstant.sharedTrackerList.size(); i++) {
                if (GlobalConstant.sharedTrackerList.get(i).get_id().equals(tracker.get_id())) {
                    exist_flag = true;
                    break;
                }
            }
            if (!exist_flag) GlobalConstant.sharedTrackerList.add(tracker);
            JSONArray _sharedDeviceList = GlobalConstant.app_user.getJSONArray("sharedDeviceList");
            String flag = "";
            for (int i = 0; i < _sharedDeviceList.length(); i++) {
                JSONObject _sharedTracker = (JSONObject) _sharedDeviceList.get(i);
                if (_sharedTracker.getString("reportId").equals(tracker.getReportingId())) {
                    flag = _sharedTracker.getString("flag");
                    break;
                }
            }
            if (flag.equals("0")) {
                if (!dialog_flag) showAddSharedTrackerDialog(tracker, oldTrackers);
            } else if (flag.equals("1")) {
                addDevice(tracker, oldTrackers, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * get alarm setting of the tracker and play sound/vibration based on the setting.
     * @param trackerId
     */
    public void getAlarmStatus(String trackerId) {
        if (!isFragmentAlive) {
            return;
        }

        if(apiCall!=null) {
            apiCall.cancel();
        }

        apiCall = apiInterface.alarm(trackerId);
        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isFragmentAlive) {
                    return;
                }
                int code = response.code();
                Gson gson = new Gson();
                if (code == 200) {
                    ResponseBody responseBody = response.body();
                    try {
                        String bodyString = responseBody.string();
                        Log.d("bodyString", bodyString);
                        respAlarm = gson.fromJson(bodyString, Resp_Alarm.class);
                        if (respAlarm == null) {
                            return;
                        }
                        //sound
                        if (respAlarm.soundAlarmStatus) {
                            try {
                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                r.play();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        //vibrate
                        if (respAlarm.vibrationAlarmStatus) {
                            if (Build.VERSION.SDK_INT >= 26) {
                                ((Vibrator) getContext().getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                ((Vibrator) getContext().getSystemService(VIBRATOR_SERVICE)).vibrate(250);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(MonitorFragment.this.getContext(), "response parse error10");
                    }
                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(MonitorFragment.this.getContext(), error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(MonitorFragment.this.getContext(), "response parse error11");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                if (!isFragmentAlive) {
                    return;
                }
                Utils.showShortToast(MonitorFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    /***
     * Show alarm alert for the tracker
     * @param tracker
     */
    private void addTrackPoints(Resp_Tracker tracker) {

        if (!tracker.getSelected()) return;

        String driverName = tracker.getDriverName();
        if (!GlobalConstant.alerts.containsKey(driverName) || !GlobalConstant.alerts.get(driverName).equals(tracker.getLastAlert())) {
            if (tracker != null && tracker.getLastAlert() != null && !tracker.getLastAlert().equals("no alert") &&
                !tracker.getLastAlert().equals("") && !tracker.getLastAlert().equals("undefined") &&
                tracker.getLastAlert().length() > 11) {
                Date current = new Date();
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                String lastAlert_time = year + "/" + tracker.getLastAlert().substring(tracker.getLastAlert().length() - 11);
                SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                try {
                    Date date = format.parse(lastAlert_time);
                    long calDate = (current.getTime() - date.getTime()) / (24 * 60 * 60 * 1000);
                    Log.d("different", "/" + calDate);
                    if (calDate < 1) {
                        GlobalConstant.alerts.put(driverName, tracker.getLastAlert());

                        Utils.showSweetAlert(getContext(), tracker.getName(), tracker.getLastAlert(), null, null, SweetAlertDialog.WARNING_TYPE, null);
                        getAlarmStatus(tracker.get_id());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /***
     * This function is for load all trackers again after 20 seconds
     */
    private void onAllTrackersLoaded() {
        if (!isFragmentAlive) {
            return;
        }
        //Utils.showShortToast(MonitorFragment.this.getContext(),"allLoaded");
        setBottomListData();
        setVelocityData();
        int DELAY = 20 * 1000;
        mHendler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (Utils.isNetworkConnected(MonitorFragment.this.getContext())) {
                    loadAllDrivers();
                } else {
                    
                }
            }
        }, DELAY);
    }

    /***
     * Display velocity data
     */
    private void setVelocityData() {
        if (!isFragmentAlive) {
            return;
        }

        List<Resp_Tracker> displayTrackers = new ArrayList<>();
        for (Resp_Tracker tracker1 : trackerList) {
            int count = 0;
            for (Resp_Tracker tracker2 : trackerList) {
                if (tracker1.get_id().equals(tracker2.get_id())) {
                    count++;
                }
            }

            if (count < 2 && tracker1.getSelected()) {
                displayTrackers.add(tracker1);
            }
        }

        VelocityRecyclerViewAdapter adapter = new VelocityRecyclerViewAdapter(this, displayTrackers, R.layout.recyclerview_row_velocity);
        rvMonitorVelocity.setAdapter(adapter);
        rvMonitorVelocity.setItemAnimator(new DefaultItemAnimator());
        if(displayTrackers.size() >= 1){
            double metricScale = Utils.getDistanceUnit().equals("miles") ? 1 : 1.60934;
            float speed = (float)(displayTrackers.get(0).getSpeedInMph() * metricScale);
            speedGauge.moveToValue(speed);
            speedGauge.setUpperText(String.format("%.1f", speed));
            float rpm = (float)(displayTrackers.get(0).getRPM() * 0.001);
            rpmGauge.moveToValue(rpm);
            rpmGauge.setUpperText(String.format("%.1f", rpm));
            double voltage = displayTrackers.get(0).getVoltage();
//            if(metricScale!=1){
//                coolantTemp = (coolantTemp-32.0)*5.0/9.0;
//            }
            batteryGauge.moveToValue((float)(voltage));
            batteryGauge.setUpperText(String.format("%.1f", (float)voltage));
        }

    }

    private void setBottomListData() {
        if (!isFragmentAlive) {
            return;
        }

        bottom_table_adapter = new AssetListMultiSelectRecyclerViewAdapter(this, trackerList, R.layout.recyclerview_row_asset_multi_select);
        rvMonitorRightOptions.setAdapter(bottom_table_adapter);
        rvMonitorRightOptions.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvMonitorRightOptions.setHasFixedSize(true);
        rvMonitorRightOptions.setItemAnimator(new DefaultItemAnimator());
    }

    private void initDottedLineSourceAndLayer(@NonNull Style loadedMapStyle, int color) {
        dashedLineDirectionsFeatureCollection = FeatureCollection.fromFeatures(new Feature[]{});
        loadedMapStyle.addSource(new GeoJsonSource("SOURCE_ID", dashedLineDirectionsFeatureCollection));
        loadedMapStyle.addLayerBelow(
                new LineLayer(
                        "DIRECTIONS_LAYER_ID", "SOURCE_ID").withProperties(
                        lineWidth(4.5f),
                        lineColor(color),
                        lineTranslate(new Float[]{0f, 4f}),
                        lineDasharray(new Float[]{1.2f, 1.2f})
                ), "road-label-small");
    }

    public void onChatRoom(int position) {
        if (position >= trackerList.size()) {
            return;
        }

        Resp_Tracker tracker = trackerList.get(position);
        String email = tracker.getSpectrumId();
        String channelName = ChannelManager.getInstance().getChannelName(email);
        Channel channel = ChannelManager.getInstance().getPrivateChannelWithName(channelName);

        if (channel == null || channel.getMembers().getMembersList().size() == 0 || (channel.getMembers().getMembersList().size() == 1 && channel.getStatus() == Channel.ChannelStatus.JOINED)) {
            final Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.dialog_invite_to_chat);
            //dialog.setCancelable(false);
            final TextView shareEmail = dialog.findViewById(R.id.edt_invite_chat_share_email);
            if (tracker.getSpectrumId().isEmpty()) {
            } else {
                shareEmail.setText(tracker.getSpectrumId());
            }

            ImageView contactImageView = dialog.findViewById(R.id.iv_invite_chat_contact);
            Button btnInvite = dialog.findViewById(R.id.btn_dlg_invite_chat_ok);
            Button btnCancel = dialog.findViewById(R.id.btn_dlg_invite_chat_cancel);

            btnInvite.setOnClickListener(v -> {
                inviteToChat(shareEmail.getText().toString());
                dialog.dismiss();
            });

            btnCancel.setOnClickListener(v -> {
                dialog.dismiss();
            });


            WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
            lWindowParams.copyFrom(dialog.getWindow().getAttributes());
            lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.show();
            dialog.getWindow().setAttributes(lWindowParams);
        } else {
//            MainActivity mainActivity = (MainActivity)getActivity();
//            ChatRoomFragment fragment = ChatRoomFragment.newInstance(assetList.get(position), false);
//            mainActivity.showOverlayFragment(fragment);
        }
    }

    public void inviteToChat(String email) {
        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();
        body.put("email", email);
        apiInterface.inviteJoinChat(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                String channelName = ChannelManager.getInstance().getChannelName(email);
                ChannelManager.getInstance().joinOrCreatePrivateChannelWithName(channelName, email, null);
                Utils.showShortToast(MonitorFragment.this.getContext(), getString(R.string.successfully_invited_to_join_chat), false);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(MonitorFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    public void onGetRoute(int position) {
        if (position >= trackerList.size()) {
            return;
        }

        Resp_Tracker select_tracker = trackerList.get(position);

        if (GlobalConstant.user_point != null && GlobalConstant.user_point.longitude() != 0 && GlobalConstant.user_point.latitude() != 0) {
            final Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.dialog_google_route);
            //dialog.setCancelable(false);
            WebView google_map = dialog.findViewById(R.id.google_map);
            google_map.getSettings().setJavaScriptEnabled(true);
            google_map.setWebViewClient(new MyWebViewClient());
            google_map.loadUrl("https://www.google.com/maps/dir/" + GlobalConstant.user_point.latitude() + "," + GlobalConstant.user_point.longitude() + "/" + select_tracker.getLat() + "," + select_tracker.getLng() + "/data=!3m1!4b1!4m2!4m1!3e0");
            google_map.requestFocus();
            WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
            lWindowParams.copyFrom(dialog.getWindow().getAttributes());
            lWindowParams.width = WindowManager.LayoutParams.FILL_PARENT; // this is where the magic happens
            lWindowParams.height = WindowManager.LayoutParams.FILL_PARENT;
            dialog.show();
            dialog.getWindow().setAttributes(lWindowParams);
        } else {
            Utils.showShortToast(getContext(), getString(R.string.your_location_is_invalid), true);
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    /***
     * Called when select "Edit Vehicle Info" menu item
     * @param position
     */
    public void onUpdateTracker(int position) {

        if (position >= trackerList.size()) {
            return;
        }

        Resp_Tracker select_tracker = trackerList.get(position);

        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_update_vehicle);
        //dialog.setCancelable(false);
        final TextView editDriverName = dialog.findViewById(R.id.edit_driver_name);
        editDriverName.setText(select_tracker.getDriverName());

        final TextView editVehicleName = dialog.findViewById(R.id.edit_vehicle_name);
        //editVehicleName.setText(select_asset.getName());
        editVehicleName.setText(select_tracker.getPlateNumber());

        final Spinner edit_color = dialog.findViewById(R.id.edit_color);
        if (select_tracker != null && select_tracker.getColor() != null) {

            String selectedColor = select_tracker.getColor();
            if (selectedColor.equals("ORANGE")) {
                selectedColor = "YELLOW";
            }
            ArrayAdapter<String> colors = (ArrayAdapter<String>) edit_color.getAdapter();
            int selectedIndex = -1;
            for (int i = 0; i < colors.getCount(); i++) {
                if (colors.getItem(i).equals(selectedColor)) {
                    selectedIndex = i;
                    break;
                }
            }

            if (selectedIndex >= 0) edit_color.setSelection(selectedIndex);
        }

        Button btnUpdateDriverInfo = dialog.findViewById(R.id.btn_update_driver_info);
        btnUpdateDriverInfo.setOnClickListener(v -> {
            String color = edit_color.getSelectedItem().toString();
            if (color.equals("YELLOW")) color = "ORANGE";

            doUpdateWork(select_tracker, editDriverName.getText().toString(), editVehicleName.getText().toString(), color);

            dialog.dismiss();
        });

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lWindowParams);
    }

    private void setPhotoUploadStatus(String tracker_id) {
        if (!Utils.isNetworkConnected(this.getContext())) {
            return;
        }

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        HashMap<String, Object> body = new HashMap<>();
        body.put("id", tracker_id);
        body.put("photoStatus", true);

        apiInterface.modify_tracker(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isFragmentAlive) {
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
                    Utils.showShortToast(getContext(), getString(R.string.update_success), false);
                    loadAllDrivers();
                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(getContext(), error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(getContext(), "response parse error12");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                if (!isFragmentAlive) {
                    return;
                }
                Utils.showShortToast(getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    /***
     * Update tracker
     * @param selected_tracker
     * @param driverName
     * @param vehicleName
     * @param color
     */
    private void doUpdateWork(Resp_Tracker selected_tracker, String driverName, String vehicleName, String color) {
        if (!Utils.isNetworkConnected(this.getContext())) {
            return;
        }

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        HashMap<String, Object> body = new HashMap<>();
        body.put("id", selected_tracker.get_id());
        body.put("driverName", driverName);
        //body.put("name", vehicleName);
        body.put("plateNumber", vehicleName);
        body.put("color", color);

        apiInterface.modify(GlobalConstant.X_CSRF_TOKEN,body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isFragmentAlive) {
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
                    Utils.showShortToast(getContext(), getString(R.string.update_success), false);

                    selected_tracker.setPlateNumber(vehicleName);
                    selected_tracker.setDriverName(driverName);

                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(getContext(), error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(getContext(), "response parse error13");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                if (!isFragmentAlive) {
                    return;
                }
                Utils.showShortToast(getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }
    private void addLandmark(String name, String type, String lat, String lng) throws JSONException {
        if (!Utils.isNetworkConnected(this.getContext())) {
            return;
        }

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);
        Landmark newLandmark = new Landmark(name, type, lat, lng);
        GlobalConstant.landmarks.add(newLandmark);
        HashMap<String, Object> body = new HashMap<>();
//        body.put("id", selectedTrackers.get(0).get_id());
        body.put("", GlobalConstant.landmarks.toString());

        apiInterface.addLandmark(GlobalConstant.X_CSRF_TOKEN,GlobalConstant.landmarks).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isFragmentAlive) {
                    return;
                }
                int code = response.code();
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
                Gson gson = gsonBuilder.create();
                if (code == 200) {
                    // success
                    Utils.showShortToast(getContext(), "you have successfully add landmark", false);
                    showLandmarks();
                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(getContext(), error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(getContext(), "response parse error13");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                if (!isFragmentAlive) {
                    return;
                }
                Utils.showShortToast(getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    public void onBottomTrackerListCheckChanged(int position, boolean isChecked) {
        if (position >= trackerList.size()) {
            return;
        }

        trackerList.get(position).setSelected(isChecked);

        if (isChecked) {
            selectedTrackers.add(trackerList.get(position));
            GlobalConstant.selectedTrackerIds.add(trackerList.get(position).get_id());
        } else {
            selectedTrackers.remove(trackerList.get(position));
            GlobalConstant.selectedTrackerIds.remove(trackerList.get(position).get_id());
        }

        if (position < trackerList.size()) {
            showCarsOnMap(true, false);
        } else {
            showCarsOnMap(false, false);
        }

        setBottomListData();
    }

    /***
     * Show trackers on map.
     * @param shouldRecenter
     * If true, move and zoom map for show all trackers, otherwise keep current status if at least one tracker is visible.
     */
    private void showCarsOnMap(Boolean shouldRecenter, Boolean shouldCenterUserLocation) {
        if (!isFragmentAlive) {
            return;
        }

        if (trackLines == null) {
            trackLines = new ArrayList<>();
        }
        for (Polyline polyline : trackLines) {
            mapboxMap.removePolyline(polyline);
        }
        trackLines.clear();
        if (mapboxMap == null) return;

        String markerLayerId = "vehicle";
        String labelLayerId = "label";
        String vehicleSourceId = "vehicle-srouce";

        CircleLayer markerLayer = null;

        try {
            markerLayer = (CircleLayer) mapboxMap.getStyle().getLayer(markerLayerId);
        } catch (Exception ex) {

        }

        if (markerLayer != null) {
            mapboxMap.getStyle().removeLayer(markerLayerId);
        }

        SymbolLayer labelLayer = null;
        try {
            labelLayer = (SymbolLayer) mapboxMap.getStyle().getLayer(labelLayerId);
        } catch (Exception ex) {

        }

        if (labelLayer != null) mapboxMap.getStyle().removeLayer(labelLayerId);

        Source geoJsonSource = null;
        try {
            geoJsonSource = mapboxMap.getStyle().getSource(vehicleSourceId);
        } catch (Exception ex) {

        }

        if (geoJsonSource != null) {
            mapboxMap.getStyle().removeSource(geoJsonSource);

        }

        /////////////////////////////////////////////////////////////////
        //FIXME: Fix java.lang.IllegalArgumentException --> latitude & longitude are null!
        List<Feature> markerCoordinates = new ArrayList<>();
        LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();
        int includedCount = 0;
        if (userShowFlag) {
            includedCount++;
            addGPSLocation();
        } else {
            hideGPSLocation();
        }

        for (Resp_Tracker tracker : trackerList) {
            if (!tracker.getSelected()) continue;
            if (tracker == null) continue;

            double latitude;
            double longitude;
            latitude = tracker.getLat();
            longitude = tracker.getLng();
            if (latitude == 0 && longitude == 0) continue;

            if (latitude > 90 || latitude < -90 || longitude > 180 || longitude < -180) continue;

            System.out.println("Monitor Fragment:::" + latitude);

            LatLng point=null;
            try{
                point= new LatLng(latitude, longitude);
            }
            catch (java.lang.IllegalArgumentException ex) {
                continue;
            }

            latLngBoundsBuilder.include(point);
            includedCount++;

            double angle = 0;

            Point pt1 = Point.fromLngLat(longitude, latitude);
            Feature f1 = Feature.fromGeometry(pt1);
            String name = tracker.getDriverName();
            if (name.length() > 5) name = name.substring(0, 5);

            double metricScale = 1.60934;
//            if (tracker.getCountry() != null) {
//                metricScale = (tracker.getCountry().equals("United States") || tracker.getCountry().equals("US")) ? 1 : 1.60934;
//            }
            metricScale = Utils.getDistanceUnit().equals("miles") ? 1 : 1.60934;

            if (tracker.getACCStatus() != 0 && tracker.getSpeedInMph() != 0) {
                name += "\n" + String.format("%.1f", tracker.getSpeedInMph() * metricScale);
                f1.addStringProperty("status", "Moving");
            } else if (tracker.getACCStatus() != 0 && tracker.getSpeedInMph() == 0) {
                f1.addStringProperty("status", "Idle");
            } else f1.addStringProperty("status", "Stop");


            f1.addStringProperty("direction", String.valueOf(angle));
            f1.addStringProperty("property", name);
            f1.addStringProperty("color", tracker.getColor());
            markerCoordinates.add(f1);
        }

        featureCollection = FeatureCollection.fromFeatures(markerCoordinates);

        geoJsonSource = new GeoJsonSource(vehicleSourceId, featureCollection);

        if (mapboxMap == null) return;
        if (mapboxMap.getStyle() == null) return;
        mapboxMap.getStyle().addSource(geoJsonSource);

        markerLayer = new CircleLayer(markerLayerId, vehicleSourceId);
        markerLayer.setSourceLayer(vehicleSourceId);

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
                circleStrokeColor(match(get("status"), rgb(244, 31, 27),
                        stop("Moving", rgb(0, 200, 83)),//blue
                        stop("Idle", rgb(186, 69, 240)), //red
                        stop("Stop", rgb(244, 31, 27))
                )),
                circleStrokeWidth(3f),
                circleRadius(match(get("color"), Expression.ExpressionLiteral.literal(23f),
                        stop("ME", Expression.ExpressionLiteral.literal(20f))
                )));

        mapboxMap.getStyle().addLayer(markerLayer);

        labelLayer = new SymbolLayer(labelLayerId, vehicleSourceId);
        labelLayer.setProperties(textField("{property}"),
                textSize(16f),
                textColor(Color.BLACK),
                textHaloColor(Color.WHITE),
                textHaloWidth(2f),
                textAllowOverlap(true)
        );
        mapboxMap.getStyle().addLayer(labelLayer);

        List<LatLng> coordinateArray = new ArrayList<>();
        for (Feature marker : markerCoordinates) {
            Point pt = (Point)marker.geometry();
            LatLng latLng = new LatLng(pt.latitude(), pt.longitude());
            coordinateArray.add(latLng);
        }
        if(coordinateArray.size()==0)noSelectedFlag=true;
        else noSelectedFlag = false;
        if (shouldCenterUserLocation) {
            if (userShowFlag && GlobalConstant.user_point != null) {
                LatLng latLng = new LatLng(GlobalConstant.user_point.latitude(), GlobalConstant.user_point.longitude());
                coordinateArray.add(latLng);
            }
        }

        updateMapViewCamera(coordinateArray, shouldRecenter);
    }

    private double getAngle(double lng1, double lat1, double lng2, double lat2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLonRad = Math.toRadians(lng2 - lng1);
        double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) *
                Math.cos(lat2Rad) * Math.cos(deltaLonRad);
        double bearing = (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
        return bearing;
    }

    private void updateMapViewCamera(@NonNull  List<LatLng> coordinates, Boolean shouldRecenter) {
        if (coordinates.size() == 0) {
            return;
        }
        if (coordinates.size() == 1) {
            LatLng latLng = coordinates.get(0);

            if (this.isCoordinateVisibleOnMap(latLng.getLatitude(), latLng.getLongitude()) == true && shouldRecenter == false) return;

            int tilt_value = 0;
            if (style.equals(GlobalConstant.MAP_BOX_STYLE_URL)) tilt_value = 45;
            //cameraPosition = new CameraPosition.Builder().target(new LatLng(pt.latitude(),pt.longitude())).bearing(rotate).tilt(tilt_value).build();
            CameraPosition.Builder cameraPositionBuilder = new CameraPosition.Builder().target(latLng).tilt(tilt_value);

            if (shouldRecenter == true) {
                double old_zoomScale = mapboxMap.getCameraPosition().zoom;
                double zoom = Math.max(old_zoomScale, defaultZoom);
                cameraPositionBuilder = cameraPositionBuilder.zoom(zoom);
            }

            CameraPosition cameraPosition = cameraPositionBuilder.build();
            mapboxMap.setCameraPosition(cameraPosition);
        } else {
            Boolean shouldMoveCamera = true;
            LatLngBounds.Builder latLngBoundsBuilder = new LatLngBounds.Builder();

            for (LatLng latLng : coordinates) {
                latLngBoundsBuilder.include(latLng);
                if (this.isCoordinateVisibleOnMap(latLng.getLatitude(), latLng.getLongitude()) == true && shouldRecenter == false) {
                    shouldMoveCamera = false;
                }
            }

            if (shouldMoveCamera == false) return;

            LatLngBounds latLngBounds = latLngBoundsBuilder.build();
            double old_zoomScale = mapboxMap.getCameraPosition().zoom;
            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 150, 50, 150, 50));
            double new_zoomScale = mapboxMap.getCameraPosition().zoom;
            CameraPosition position = new CameraPosition.Builder().target(latLngBounds.getCenter()).zoom(new_zoomScale).build();
            mapboxMap.setCameraPosition(position);

            if (old_zoomScale < new_zoomScale && shouldRecenter == false) {
                CameraPosition old = mapboxMap.getCameraPosition();
                CameraPosition pos = new CameraPosition.Builder()
                        .target(new LatLng(old.target.getLatitude(), old.target.getLongitude()))
                        .bearing(0).zoom(old_zoomScale)
                        .build();
                mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
            }

            double zoomScale = mapboxMap.getCameraPosition().zoom;
            if(zoomScale>defaultZoom) {
                int tilt_value = 0;
                if(style.equals(GlobalConstant.MAP_BOX_STYLE_URL)) tilt_value = 45;
                CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latLngBounds.getCenter().getLatitude(),latLngBounds.getCenter().getLongitude())).bearing(0).tilt(tilt_value).zoom(defaultZoom).build();
                mapboxMap.setCameraPosition(cameraPosition);
            }
        }
    }

    private Boolean isCoordinateVisibleOnMap(Double lat, Double lng) {
        LatLng latLng = new LatLng(lat, lng);
        LatLngBounds visibleBounds = mapboxMap.getProjection().getVisibleRegion().latLngBounds;
        return visibleBounds.contains(latLng);
    }

    private void initMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        // mapView.setStyleUrl(style);
        mapView.getMapAsync(mapboxMap -> {
            MonitorFragment.this.mapboxMap = mapboxMap;
            MonitorFragment.this.mapboxMap.setStyle(new Style.Builder().fromUrl(style), style -> initDottedLineSourceAndLayer(style, Color.BLACK));
            MonitorFragment.this.mapboxMap.setMaxZoomPreference(20);
            if (GlobalConstant.user_point != null) {
                MonitorFragment.this.mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(GlobalConstant.user_point.latitude(), GlobalConstant.user_point.longitude())).zoom(defaultZoom).tilt(45).build());
            }
            MonitorFragment.this.mapboxMap.getUiSettings().setRotateGesturesEnabled(false);

            MonitorFragment.this.mapboxMap.addOnMapLongClickListener(point -> {
                showLandMarkDialog(point);
                com.google.maps.model.LatLng latLng = new com.google.maps.model.LatLng(point.getLatitude(), point.getLongitude());
                iAddMarker(new LatLng(latLng.lat, latLng.lng));
                return false;
            });

            MonitorFragment.this.mapboxMap.setOnMarkerClickListener(marker -> {
                LatLng position = marker.getPosition();
                com.google.maps.model.LatLng latLng = new com.google.maps.model.LatLng(position.getLatitude(), position.getLongitude());
                if (marker == null) {
                    iAddMarker(new LatLng(latLng.lat, latLng.lng));
                }
                return false;
            });

            showLandmarks();
        });
        try {
            landmark_icons.put("Home", R.drawable.landmark_home);
            landmark_icons.put("Office", R.drawable.landmark_office);
            landmark_icons.put("Warehouse", R.drawable.landmark_warehouse);
            landmark_icons.put("Mall", R.drawable.landmark_mall);
            landmark_icons.put("School", R.drawable.landmark_school);
        } catch (JSONException  e){
            e.printStackTrace();
        };
    }
    public void showLandmarks() {
        Log.d("landmark", ""+GlobalConstant.landmarks.size());
        for(int i=0;i<landMarkers.size();i++){
            mapboxMap.removeMarker(landMarkers.get(i));
        }
        landMarkers.clear();
        for(int i=0;i<GlobalConstant.landmarks.size();i++) {
            try {
                BitmapDrawable bitmapDraw = (BitmapDrawable) getResources().getDrawable(landmark_icons.getInt(GlobalConstant.landmarks.get(i).type));
                Bitmap b = bitmapDraw.getBitmap();
                double ratio = (double) b.getWidth() / (double) b.getHeight();
                Bitmap resizedBmp = Bitmap.createScaledBitmap(b, 40, (int) (40 / ratio), false);

                final IconFactory iconFactory = IconFactory.getInstance(MonitorFragment.this.getContext());
                LatLng latLng = new LatLng(Double.valueOf(GlobalConstant.landmarks.get(i).lat), Double.valueOf(GlobalConstant.landmarks.get(i).lng));
                landMarkers.add(mapboxMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .setIcon(iconFactory.fromBitmap(resizedBmp))
                ));
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }
    private void showLandMarkDialog(LatLng point) {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_add_landmark);
        //dialog.setCancelable(false);
        final EditText name = dialog.findViewById(R.id.edt_landmark_name);
        final EditText lon = dialog.findViewById(R.id.edit_longitude);
        final EditText lat = dialog.findViewById(R.id.edit_latitude);
        lon.setText(point.getLongitude()+"");
        lat.setText(point.getLatitude()+"");
        Spinner landmark_type = dialog.findViewById(R.id.spinner_landmark_type);
        String[] categories = {"Home", "Office", "Warehouse", "School", "Mall"};
        ArrayAdapter<String> adp3 = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, categories);
        adp3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        landmark_type.setAdapter(adp3);
        Button btnOk = dialog.findViewById(R.id.btn_landmark_ok);
        Button btnCancel = dialog.findViewById(R.id.btn_landmark_cancel);

//        contactImageView.setVisibility(View.VISIBLE);

        btnOk.setOnClickListener(v -> {
            try {
                addLandmark(name.getText().toString(),categories[landmark_type.getSelectedItemPosition()],lat.getText().toString(),lon.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lWindowParams);
    }
    @OnClick(R.id.btn_ShowUser)
    public void onUserShowHide() {
        this.userShowFlag = !this.userShowFlag;
        if(noSelectedFlag)this.userShowFlag = true;
        SharedPreferences preferences = getActivity().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        boolean sPTrackingFlag = preferences.getBoolean("sPhoneTracking", false);
        if(!sPTrackingFlag) {
            Utils.showShortToast(getContext(), "Please turn on phone tracking function", true);
            return;
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("sUserShowFlag", this.userShowFlag);
        editor.commit();

        // showCarsOnMap();
        int tilt_value = 0;
        if (style.equals(GlobalConstant.MAP_BOX_STYLE_URL)) tilt_value = 45;
        if (this.userShowFlag) {
            LocationManager locationManager = (LocationManager) this.getActivity().getSystemService(Service.LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(!isGPSEnabled) {
                Utils.showSweetAlert(getActivity(), getString(R.string.note), getString(R.string.enable_your_gps_to_show_your_location), null, null, SweetAlertDialog.WARNING_TYPE, null);
                return;
            }

            showCarsOnMap(true, true);
        } else {
            showCarsOnMap(true, false);
        }
    }

    public void changeMapStyle(boolean isSatellite) {
        Resources resources = this.getActivity().getResources();
        if (isSatellite) {
            style = GlobalConstant.MAP_BOX_SATELLITE_URL;
            mapboxMap.setStyle(new Style.Builder().fromUrl(style), style -> initDottedLineSourceAndLayer(style, Color.YELLOW));
        } else {
            style = GlobalConstant.MAP_BOX_STYLE_URL;
            mapboxMap.setStyle(new Style.Builder().fromUrl(style), style -> initDottedLineSourceAndLayer(style, Color.BLACK));
        }
    }

    public boolean isSatelliteStyle() {
        if (style.equals(GlobalConstant.MAP_BOX_STYLE_URL)) {
            return false;
        } else {
            return true;
        }
    }

    @OnClick(R.id.btn_toggle_basemap)
    public void onToogleBaseMapClick() {
        Resources resources = this.getActivity().getResources();
        if (style.equals(GlobalConstant.MAP_BOX_STYLE_URL)) {
            style = GlobalConstant.MAP_BOX_SATELLITE_URL;
            mapboxMap.setStyle(new Style.Builder().fromUrl(style), style -> initDottedLineSourceAndLayer(style, Color.YELLOW));
        } else {
            style = GlobalConstant.MAP_BOX_STYLE_URL;
            mapboxMap.setStyle(new Style.Builder().fromUrl(style), style -> initDottedLineSourceAndLayer(style, Color.BLACK));
        }
    }

    @OnClick(R.id.btn_toggle_velocity)
    public void onToogleVelocityButtonClick() {
        Resources resources = this.getActivity().getResources();
        if (this.layout_velocityTable.isShown()) {
            Drawable drawable = resources.getDrawable(R.drawable.btn_hide);
            btn_toggle_velocity.setImageDrawable(drawable);
            layout_velocityTable.setVisibility(View.GONE);
        } else {
            Drawable drawable = resources.getDrawable(R.drawable.btn_show);
            btn_toggle_velocity.setImageDrawable(drawable);
            layout_velocityTable.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btn_toggle_info)
    public void onToggleVelocityInfoClick() {
        Resources resources = this.getActivity().getResources();
        if (this.layout_velocityTable.isShown()) {
            Drawable drawable = resources.getDrawable(R.drawable.btn_hide);
            btn_toggle_velocity.setImageDrawable(drawable);
            txtToggleInfoTitle.setText(R.string.show_info);
            layout_velocityTable.setVisibility(View.GONE);
        } else {
            Drawable drawable = resources.getDrawable(R.drawable.btn_show);
            btn_toggle_velocity.setImageDrawable(drawable);
            txtToggleInfoTitle.setText(R.string.hide_info);
            layout_velocityTable.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btn_new_geofence)
    public void onNewGeofenceClick() {
        MainActivity mainActivity = (MainActivity)getActivity();
        EditGeofenceFragment fragment = EditGeofenceFragment.newInstance();
        fragment.originalCameraPosition = mapboxMap.getCameraPosition();
        mainActivity.pushFragment(fragment);
    }

    @OnClick(R.id.btn_tools)
    public void onToolsClick() {
        MainActivity mainActivity = (MainActivity)getActivity();
        ToolsFragment fragment = ToolsFragment.newInstance();
        mainActivity.pushFragment(fragment);
//        mainActivity.showOverlayFragment(fragment);
    }

    private class ParallaxStikkyAnimator extends HeaderStikkyAnimator {
        @Override
        public AnimatorBuilder getAnimatorBuilder() {
            View mHeader_image = getHeader().findViewById(R.id.mapView);
            return AnimatorBuilder.create().applyVerticalParallax(mHeader_image, 1);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public class WrapContentLinearLayoutManager extends LinearLayoutManager {

        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }
    }

    /**
     * Add & Remove Markers to the Map
     */
    public void iAddMarker(LatLng point) {
        if (point == null) return;

        Feature feature = Feature.fromGeometry(Point.fromLngLat(point.getLongitude(), point.getLatitude()));
        mapboxMap
                .setStyle(new Style.Builder()
                        .fromUrl(GlobalConstant.MAP_BOX_STYLE_URL)
                        .withImage(ICON_ID, marker)
                        .withSource(new GeoJsonSource(SOURCE_ID, feature))
                        .withLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                        .withProperties(PropertyFactory.iconImage(ICON_ID),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true),
                                iconOffset(new Float[]{0f, -9f}))));

    }

    public void iRemoveMarker() {
        mapboxMap
                .setStyle(new Style.Builder()
                        .fromUrl(GlobalConstant.MAP_BOX_STYLE_URL));
    }

    @OnClick(R.id.btn_chatting)
    public void onBtnChatting() {
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.onChat();
    }
    @OnClick(R.id.btn_landmark)
    public void onBtnLandmark() {
//        showLandMarkDialog(new LatLng());
        Utils.showShortToast(getContext(), "long press the screen to add a landmark", false);
//        Toast.makeText(getContext(),"long press the screen to add a landmark", Toast.LENGTH_LONG).show();
    }
    public void updateUnreadCount() {
        int unreadCount = MainActivity.get().totalUnreadCount;
        if (unreadCount == 0) {
            txtUnreadCount.setVisibility(View.GONE);
        } else {
            txtUnreadCount.setVisibility(View.VISIBLE);
            txtUnreadCount.setText(String.format("%d", unreadCount));
        }
    }

    public void checkChatInvitation() {
        ArrayList<Channel> channels = ChannelManager.getInstance().getPrivateChannels();
        for (Channel channel : channels) {
            if (channel.getStatus() == Channel.ChannelStatus.INVITED && channel.getMembers().getMembersList().size() > 0) {
                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.dialog_single_chat_invitation);

                Member member = channel.getMembers().getMembersList().get(0);
                final TextView descriptionText = dialog.findViewById(R.id.txt_dlg_chat_invitation_description);
                String description = String.format("%s invited you to the chat.", member.getIdentity());
                descriptionText.setText(description);

                Button btnAccept = dialog.findViewById(R.id.btn_dlg_chat_invitation_accept);
                Button btnReject = dialog.findViewById(R.id.btn_dlg_chat_invitation_reject);

                btnAccept.setOnClickListener(v -> {
                    channel.join(new StatusListener() {
                        @Override
                        public void onSuccess() {
                            MainActivity mainActivity = (MainActivity)getActivity();
                            String channelName = channel.getUniqueName();
                            ChatRoomFragment fragment = ChatRoomFragment.newInstance(channelName, true);
                            mainActivity.pushFragment(fragment);
                        }
                    });
                    dialog.dismiss();
                });

                btnReject.setOnClickListener(v -> {
                    ChannelManager.getInstance().declineInvitation(channel);
                    dialog.dismiss();
                });


                WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
                lWindowParams.copyFrom(dialog.getWindow().getAttributes());
                lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                dialog.show();
                dialog.getWindow().setAttributes(lWindowParams);
                break;
            }
        }
    }

    /***
     * Upload driver photo
     * @param bitmap
     * @param asset_id
     * @param tracker_id
     */
    public void uploadDriverPhoto(Bitmap bitmap, String asset_id, final String tracker_id) {
        File filesDir = getApplicationContext().getFilesDir();
        File file = new File(filesDir, "image" + ".png");

        OutputStream os;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapdata = bos.toByteArray();

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
        }

        if (!Utils.isNetworkConnected(getContext())) {
            return;
        }

        Utils.showProgress(getActivity());

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        RequestBody requestBody = new MultipartBody.Builder().addFormDataPart("filename", asset_id)
                .addFormDataPart("state", "1")
                .addFormDataPart("file", file.getName(), reqFile).build();
        if(apiCall!=null) {
            apiCall.cancel();
        }

        apiCall=apiInterface.imageUpload(requestBody);

        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();
                Log.d("uploadimage", response.toString());
                ResponseBody responseBody = response.body();
                try {
                    String bodyString = responseBody.string();
                    Log.d("uploadimage", bodyString);
                    setPhotoUploadStatus(tracker_id);
                    //loadAllDrivers();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                loadAllDrivers();
                Utils.showShortToast(getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
        GlobalConstant.upload_state = false;
    }

    public void onUpdateCurrentLocation() {
        if (!userShowFlag) return;
        showCarsOnMap(false, false);
    }
}
