package com.jo.spectrumtracking.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.adapter.AssetListSingleSelectRecyclerViewAdapter;
import com.jo.spectrumtracking.adapter.ReportExpandListViewAdapter;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Report_Group;
import com.jo.spectrumtracking.model.Report_Value;
import com.jo.spectrumtracking.model.Report_Value1;
import com.jo.spectrumtracking.model.Resp_Error;
import com.jo.spectrumtracking.model.Resp_Event;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.jo.spectrumtracking.model.Resp_TripLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

public class ReportsFragment extends Fragment {

    ApiInterface apiInterface;
    Call<ResponseBody> apiCall;

    @BindView(R.id.topView)
    LinearLayout topView;
    @BindView(R.id.rv_replay_right_options)
    RecyclerView rvAssetSingleSelect;
    @BindView(R.id.txt_mileage_mile)
    TextView txt_mileage_mile;
    @BindView(R.id.txt_average_trip)
    TextView txt_average_trip;
    @BindView(R.id.txt_fuel)
    TextView txt_fuel;
    @BindView(R.id.txt_driver_speeding)
    TextView txt_driver_speeding;
    @BindView(R.id.txt_idling)
    TextView txt_idling;
    @BindView(R.id.txt_harsh_acceleration)
    TextView txt_harsh_acceleration;
    @BindView(R.id.txt_harsh_deceleration)
    TextView txt_harsh_deceleration;
    @BindView(R.id.txt_low_battery)
    TextView txt_low_battery;
    @BindView(R.id.txt_total_stops)
    TextView txt_total_stops;
    @BindView(R.id.txt_max_speed)
    TextView txt_max_speed;
    @BindView(R.id.date_from_to)
    TextView date_from_to;
    @BindView(R.id.bottomView)
    ScrollView bottomView;
    @BindView(R.id.expandableListView)
    ExpandableListView expandableListView;
    @BindView(R.id.btn_backward)
    ImageView btn_backward;
    @BindView(R.id.btn_forward)
    ImageView btn_forward;
    boolean isFragmentAlive = false;

    Calendar replayStartDate;
    Calendar replayEndDate;

    List<Resp_Tracker> trackerList = null;
    List<Resp_TripLog> assetLogList = null;
    List<Resp_Event> eventList = null;
    AssetListSingleSelectRecyclerViewAdapter adapter = null;
    Resp_Tracker selectedTracker = null;
    private List<Report_Group> listDataGroup;

    private HashMap<String, List<Report_Value>> listDataChild;
    private ReportExpandListViewAdapter expandableListViewAdapter;
    private int week_index = 0;

    public ReportsFragment() {
        // Required empty public constructor
    }

    public static ReportsFragment newInstance() {
        ReportsFragment fragment = new ReportsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFragmentAlive = true;
        apiInterface = ApiClient.getClient(getContext()).create(ApiInterface.class);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        isFragmentAlive = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reports, container, false);
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(getString(R.string.report));
        initBottomPanel();
        if (trackerList == null) {
            trackerList = new ArrayList<>();
        }

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
        } else {
            loadAllDrivers();
        }

        initListeners();
        listDataGroup = new ArrayList<>();
        listDataChild = new HashMap<>();
        eventList = new ArrayList<>();
        expandableListViewAdapter = new ReportExpandListViewAdapter(this, getContext(), listDataGroup, listDataChild, eventList);

        // setting list adapter
        expandableListView.setAdapter(expandableListViewAdapter);
        StikkyHeaderBuilder.stickTo(bottomView)
                .setHeader(topView.getId(), (ViewGroup) getView())
                .minHeightHeader(0)
                .animator(new ParallaxStikkyAnimator())
                .build();
    }

    private void initListeners() {
        // ExpandableListView on child click listener
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> false);
    }

    private void initBottomPanel() {
        if (!isFragmentAlive) {
            return;
        }
        btn_forward.setVisibility(View.GONE);
        Calendar day7before_start = Calendar.getInstance();
        day7before_start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        setAssetLogStartDate(day7before_start);
        Calendar day7before_end = Calendar.getInstance();
        day7before_end.add(Calendar.DAY_OF_YEAR, 1);
        setAssetLogEndDate(day7before_end);
    }

    /***
     * Load all trackers
     */
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
        apiInterface.assets().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();
                if (!isFragmentAlive) {
                    return;
                }

                int code = response.code();
                Log.d("code", "" + code);
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

                Utils.showShortToast(ReportsFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });

    }

    public void setSelectedTracker(Resp_Tracker selectedTracker, boolean shouldLoadReplay) {
        if (!isFragmentAlive) {
            return;
        }

        this.selectedTracker = selectedTracker;
        for (Resp_Tracker tracker : trackerList) {
            if (selectedTracker.get_id() == tracker.get_id()) tracker.setSelected(true);
        }

        if (shouldLoadReplay) {
            loadEvents();
        }

        setAssetSingleSelectTableData();
        adapter.notifyDataSetChanged();
    }

    private void setAssetSingleSelectTableData() {

        if (!isFragmentAlive) {
            return;
        }
        // items
        adapter = new AssetListSingleSelectRecyclerViewAdapter(this, trackerList, R.layout.recyclerview_row_asset_single_select);

        rvAssetSingleSelect.setAdapter(adapter);
        rvAssetSingleSelect.setLayoutManager(new LinearLayoutManager(this.getContext()));
        rvAssetSingleSelect.setItemAnimator(new DefaultItemAnimator());
    }

    @OnClick(R.id.btn_forward)
    public void onNextWeek() {
        if (!isFragmentAlive) {
            return;
        }
        week_index++;
        if (week_index >= 0) btn_forward.setVisibility(View.GONE);
        if (week_index < 0) {
            replayStartDate.add(Calendar.DAY_OF_YEAR, 7);
            replayEndDate.add(Calendar.DAY_OF_YEAR, 7);
            setAssetLogStartDate(replayStartDate);
            setAssetLogEndDate(replayEndDate);
        } else {
            initBottomPanel();
        }
        loadEvents();
    }

    @OnClick(R.id.btn_backward)
    public void onPrevWeek() {
        if (!isFragmentAlive) {
            return;
        }
        week_index--;
        if (week_index < 0) btn_forward.setVisibility(View.VISIBLE);
        Calendar day7before_start = Calendar.getInstance();
        day7before_start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        day7before_start.add(Calendar.DAY_OF_YEAR, week_index * 7);
        setAssetLogStartDate(day7before_start);
        Calendar day7before_end = Calendar.getInstance();
        day7before_end.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        day7before_end.add(Calendar.DAY_OF_YEAR, week_index * 7 + 7);
        setAssetLogEndDate(day7before_end);
        loadEvents();
    }

    /***
     * Change report start date
     * @param date
     */
    private void setAssetLogStartDate(Calendar date) {
        if (!isFragmentAlive) {
            return;
        }
        replayStartDate = date;
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
        System.out.println(replayStartDate.getTime().toString());
    }

    /***
     * Change report end date
     * @param date
     */
    private void setAssetLogEndDate(Calendar date) {
        if (!isFragmentAlive) {
            return;
        }
        replayEndDate = date;
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
        System.out.println(replayEndDate.getTime().toString());
        if (week_index == 0) date_from_to.setText(getString(R.string.this_week));
        else {
            Calendar show_date = (Calendar) replayEndDate.clone();
            show_date.add(Calendar.DAY_OF_YEAR,-1);
            date_from_to.setText(sdf.format(replayStartDate.getTime()) + " - " + sdf.format(show_date.getTime()));
        }
    }

    /***
     * Load event data by calling `logs` API
     */
    public void loadEvents() {
        if (!isFragmentAlive) {
            return;
        }

        if (!Utils.isNetworkConnected(this.getContext())) {
            return;
        }

        if (selectedTracker == null) {
            Utils.showShortToast(this.getContext(), getString(R.string.please_choose_vehicle), true);
            return;
        }

        String reportingId = selectedTracker.getReportingId();
        Date startTime = replayStartDate.getTime();
        Date endTime = replayEndDate.getTime();

        startTime.setHours(0);
        startTime.setMinutes(0);
        startTime.setSeconds(0);

        endTime.setHours(0);
        endTime.setMinutes(0);
        endTime.setSeconds(0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
        //sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        String startTimeString = sdf.format(startTime);
        String endTimeString = sdf.format(endTime);
        final String reporting_id = reportingId;

        if(apiCall!=null) {
            apiCall.cancel();
        }
        apiCall= apiInterface.event_logs(reportingId, startTimeString, endTimeString);
        apiCall.enqueue(new Callback<ResponseBody>() {
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
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;

                    eventList = null;

                    try {
                        object = new JSONObject("{'items':" + responseBody.string() + "}");
                        JSONArray items = (JSONArray) object.get("items");
                        Log.d("tripLogs", responseBody.string());
                        Type type = new TypeToken<List<Resp_Event>>() {
                        }.getType();
                        eventList = gson.fromJson(items.toString(), type);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (eventList != null) {
                        if (eventList.size() != 0) {
                            // setEventRecyclerView();
                        } else {
                           // Utils.showShortToast(ReportsFragment.this.getContext(), "No Data. Change to another day");
                        }
                    }
                    loadTripLogSummarys(reporting_id);

                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(ReportsFragment.this.getContext(), error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    loadTripLogSummarys(reporting_id);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                if (!isFragmentAlive) {
                    return;
                }
                Utils.showShortToast(ReportsFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }
    public void loadTripLogSummarys(String reportingId) {
        Date startTime = replayStartDate.getTime();
        Date endTime = replayEndDate.getTime();
        startTime.setHours(0);
        startTime.setMinutes(0);
        startTime.setSeconds(0);

        endTime.setHours(0);
        endTime.setMinutes(0);
        endTime.setSeconds(0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
//        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String startTimeString = sdf.format(startTime);
        String endTimeString = sdf.format(endTime);

        if(apiCall!=null) {
            apiCall.cancel();
        }
        apiCall= apiInterface.trip_log_summary(reportingId, startTimeString, endTimeString, GlobalConstant.metricScale, GlobalConstant.volumeMetricScale);
        apiCall.enqueue(new Callback<ResponseBody>() {
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
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;

                    try {
                        object = new JSONObject(responseBody.string());
                        showSummaryResult(object);
                        Log.d("tripLogs", responseBody.string());
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(ReportsFragment.this.getContext(), "response parse error");
                    }
//                    if (assetLogList != null) {
//                        if (assetLogList.size() != 0) {
//                            showResult();
//                        } else {
//                            Utils.showShortToast(ReportsFragment.this.getContext(), getString(R.string.no_trip_change_to_another_day), true);
//                            showResult();
//                            txt_mileage_mile.setText("---");
//                            txt_average_trip.setText("---");
//                            txt_total_stops.setText("---");
//                            txt_max_speed.setText("---");
//                            txt_harsh_acceleration.setText("---");
//                            txt_harsh_deceleration.setText("---");
//                            txt_low_battery.setText("---");
//                            txt_fuel.setText("---");
//                            txt_driver_speeding.setText("---");
//                            txt_idling.setText("---");
//                        }
//                    }
                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(ReportsFragment.this.getContext(), error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(ReportsFragment.this.getContext(), "response parse error");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                if (!isFragmentAlive) {
                    return;
                }
                Utils.showShortToast(ReportsFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }
    public void loadTripLogs(String reportingId) {
        Date startTime = replayStartDate.getTime();
        Date endTime = replayEndDate.getTime();
        startTime.setHours(0);
        startTime.setMinutes(0);
        startTime.setSeconds(0);

        endTime.setHours(0);
        endTime.setMinutes(0);
        endTime.setSeconds(0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
//        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String startTimeString = sdf.format(startTime);
        String endTimeString = sdf.format(endTime);

        if(apiCall!=null) {
            apiCall.cancel();
        }
        apiCall= apiInterface.trip_logs(reportingId, startTimeString, endTimeString);
        apiCall.enqueue(new Callback<ResponseBody>() {
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
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;

                    assetLogList = null;

                    try {
                        object = new JSONObject("{'items':" + responseBody.string() + "}");
                        JSONArray items = (JSONArray) object.get("items");
                        Log.d("tripLogs", responseBody.string());
                        Type type = new TypeToken<List<Resp_TripLog>>() {
                        }.getType();
                        assetLogList = gson.fromJson(items.toString(), type);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(ReportsFragment.this.getContext(), "response parse error");
                    }
                    if (assetLogList != null) {

                        if (assetLogList.size() != 0) {
                            showResult();
                        } else {
                            Utils.showShortToast(ReportsFragment.this.getContext(), getString(R.string.no_trip_change_to_another_day), true);
                            showResult();
                            txt_mileage_mile.setText("---");
                            txt_average_trip.setText("---");
                            txt_total_stops.setText("---");
                            txt_max_speed.setText("---");
                            txt_harsh_acceleration.setText("---");
                            txt_harsh_deceleration.setText("---");
                            txt_low_battery.setText("---");
                            txt_fuel.setText("---");
                            txt_driver_speeding.setText("---");
                            txt_idling.setText("---");
                        }
                    }
                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(ReportsFragment.this.getContext(), error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(ReportsFragment.this.getContext(), "response parse error");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                if (!isFragmentAlive) {
                    return;
                }
                Utils.showShortToast(ReportsFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    public void setAddressClick(String url) {
        Log.d("address", url);

        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_google_route);
        WebView google_map = dialog.findViewById(R.id.google_map);
        google_map.getSettings().setJavaScriptEnabled(true);
        google_map.setWebViewClient(new MyWebViewClient());
        google_map.loadUrl(url.replaceAll(" ", ""));
        google_map.requestFocus();
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT; // this is where the magic happens
        lWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.show();
        dialog.getWindow().setAttributes(lWindowParams);
    }

    public void showSummaryResult(JSONObject object) throws JSONException, ParseException {
        JSONArray tripLogTable = object.getJSONArray("tripLogTable");
        if (tripLogTable.length() == 0) {
            Utils.showShortToast(ReportsFragment.this.getContext(), getString(R.string.no_trip_change_to_another_day), true);
        }
        String distanceUnit =  Utils.getDistanceUnit();
        String speedUnit =  Utils.getDistanceUnit().equals("miles") ? "mph" : "kmh";

        List<Report_Value> mileage_List = new ArrayList<>();
        List<Report_Value> stop_List = new ArrayList<>();
        List<Report_Value> totalspeed_List = new ArrayList<>();
        List<Report_Value> fuel_List = new ArrayList<>();
        List<Report_Value> harsh_acceList = new ArrayList<>();
        List<Report_Value> harsh_deceList = new ArrayList<>();
        List<Report_Value> speeding_List = new ArrayList<>();
        List<Report_Value> idling_List = new ArrayList<>();
        Boolean[] week_value = {false, false, false, false, false, false, false};
        for (int i = 0; i < tripLogTable.length(); i++) {
            JSONArray _tripLog = tripLogTable.getJSONArray(i);
            SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
            Date date = format.parse(_tripLog.getString(0));
            totalspeed_List.add(new Report_Value(date, String.valueOf(_tripLog.getDouble(8))));
            harsh_acceList.add(new Report_Value(date, String.valueOf(_tripLog.getInt(5))));
            harsh_deceList.add(new Report_Value(date, String.valueOf(_tripLog.getInt(6))));
            speeding_List.add(new Report_Value(date, String.valueOf(_tripLog.getInt(4))));
            stop_List.add(new Report_Value(date, String.valueOf(_tripLog.getInt(3))));
            idling_List.add(new Report_Value(date, String.valueOf(_tripLog.getInt(7))));
            fuel_List.add(new Report_Value(date, String.valueOf(_tripLog.getDouble(2))));
            mileage_List.add(new Report_Value(date, String.valueOf(_tripLog.getDouble(1))));
        }
        listDataChild.clear();
        listDataGroup.clear();
        listDataGroup.add(new Report_Group("Distance", String.format("%.0f %s", object.getDouble("totalMileage"), distanceUnit)));
        listDataGroup.add(new Report_Group("Max Speed", String.format("%.0f %s", object.getDouble("maxSpeed"), speedUnit)));
        listDataGroup.add(new Report_Group("Total Fuel", String.format("%.2f", object.getDouble("totalFuel"))));
        listDataGroup.add(new Report_Group("Total Stops", String.valueOf(object.getInt("totalTrips"))));
        listDataGroup.add(new Report_Group("Rapid Accel", String.valueOf(object.getInt("hardAcceNum"))));
        listDataGroup.add(new Report_Group("Hard Braking", String.valueOf(object.getInt("hardDeceNum"))));
        listDataGroup.add(new Report_Group("Speeding", String.valueOf(object.getInt("speedingNum"))));
        listDataGroup.add(new Report_Group("High RPM", String.valueOf(object.getInt("highRPMNum"))));
        listDataGroup.add(new Report_Group("Low Battery", String.valueOf(object.getInt("lowBatteryNum"))));
        listDataGroup.add(new Report_Group("High Coolant", String.valueOf(object.getInt("coolantTempHighNum"))));
        listDataGroup.add(new Report_Group("Idling", String.valueOf(object.getInt("idleEngineNum"))));
        listDataGroup.add(new Report_Group("Engine On", getSize("engine on")));
        listDataGroup.add(new Report_Group("Engine Off", getSize("engine off")));
        listDataGroup.add(new Report_Group("Device Removal", getSize("device removal")));

        listDataChild.put(getString(R.string.distance), mileage_List);
        listDataChild.put(getString(R.string.max_speed), totalspeed_List);
        listDataChild.put(getString(R.string.total_fuel), fuel_List);
        listDataChild.put(getString(R.string.total_stops), stop_List);
        listDataChild.put(getString(R.string.rapid_accel), harsh_acceList);
        listDataChild.put(getString(R.string.hard_braking), harsh_deceList);
        listDataChild.put(getString(R.string.speeding), speeding_List);
        listDataChild.put("High RPM", new ArrayList<>());
        listDataChild.put("Low Battery", new ArrayList<>());
        listDataChild.put("High Coolant", new ArrayList<>());
        listDataChild.put(getString(R.string.idling), idling_List);
        listDataChild.put("Engine On", idling_List);
        listDataChild.put("Engine Off", idling_List);
        listDataChild.put("Device Removal", idling_List);
        expandableListViewAdapter = new ReportExpandListViewAdapter(this, getContext(), listDataGroup, listDataChild, eventList);

        expandableListView.setAdapter(expandableListViewAdapter);
    }
    private String getSize(String title){
        int count = 0;
        for(int i=0;i<eventList.size();i++){
            if(eventList.get(i).getAlarm().toLowerCase(Locale.ROOT).replaceAll(" ","").equals(title.toLowerCase(Locale.ROOT).replaceAll(" ",""))){
                count++;
            }
        }
        return String.valueOf(count);
    }
    public void showResult() {
        double maxSpeed = 0;
        int harshDeceNum = 0;
        int harshAcceNum = 0;
        int speedingNum = 0;
        int totalTrips = 0;
        int idleNum = 0;
        double totalFuel = 0;
        double totalMileages = 0;

        String distanceUnit =  Utils.getDistanceUnit();
        String speedUnit =  Utils.getDistanceUnit().equals("miles") ? "mph" : "kmh";

        List<Report_Value> mileage_List = new ArrayList<>();
        List<Report_Value> stop_List = new ArrayList<>();
        List<Report_Value> totalspeed_List = new ArrayList<>();
        List<Report_Value> fuel_List = new ArrayList<>();
        List<Report_Value> harsh_acceList = new ArrayList<>();
        List<Report_Value> harsh_deceList = new ArrayList<>();
        List<Report_Value> speeding_List = new ArrayList<>();
        List<Report_Value> idling_List = new ArrayList<>();
        Boolean[] week_value = {false, false, false, false, false, false, false};
        for (int i = 0; i < assetLogList.size(); i++) {
            Resp_TripLog tripLog = assetLogList.get(i);
            Calendar c = Calendar.getInstance();
            c.setTime(tripLog.getDateTime());
            int dayOfWeek = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
            if (!week_value[dayOfWeek]) {

                if(selectedTracker==null) return;
                if(selectedTracker.getCountry()==null) {
                    selectedTracker.setCountry("unknown");
                }
//                double metricScale = (selectedTracker.getCountry().equals("United States")) ? 1 : 1.60934;
//                double volumeMetricScale = (selectedTracker.getCountry().equals("United States")) ? 1 : 3.78541;
                double metricScale = Utils.getDistanceUnit().equals("miles") ? 1 : 1.60934;
                double volumeMetricScale = Utils.getDistanceUnit().equals("miles") ? 1 : 3.78541;

                if (tripLog.getMaxSpeed() != 0) {
                    totalspeed_List.add(new Report_Value(tripLog.getDateTime(), String.valueOf(tripLog.getMaxSpeed() * metricScale)));
                }
                maxSpeed = Math.max(maxSpeed, tripLog.getMaxSpeed() * metricScale);

                if (tripLog.getHarshAcce() != 0) {
                    harsh_acceList.add(new Report_Value(tripLog.getDateTime(), String.valueOf(tripLog.getHarshAcce())));
                }
                harshAcceNum += tripLog.getHarshAcce();

                if (tripLog.getHarshDece() != 0) {
                    harsh_deceList.add(new Report_Value(tripLog.getDateTime(), String.valueOf(tripLog.getHarshDece())));
                }
                harshDeceNum += tripLog.getHarshDece();

                if (tripLog.getSpeeding() != 0) {
                    speeding_List.add(new Report_Value(tripLog.getDateTime(), String.valueOf(tripLog.getSpeeding())));
                }
                speedingNum += tripLog.getSpeeding();

                totalTrips += tripLog.getStops();
                if (tripLog.getStops() != 0) {
                    stop_List.add(new Report_Value(tripLog.getDateTime(), String.valueOf(tripLog.getStops())));
                }

                if (tripLog.getIdle() != 0) {
                    idling_List.add(new Report_Value(tripLog.getDateTime(), String.valueOf(tripLog.getIdle())));
                }
                idleNum += tripLog.getIdle();

                if (tripLog.getFuel() != 0) {
                    fuel_List.add(new Report_Value(tripLog.getDateTime(), String.format("%.2f", tripLog.getFuel() * volumeMetricScale)));
                }
                totalFuel += tripLog.getFuel() * volumeMetricScale;

                if (tripLog.getMileage() != 0) {
                    mileage_List.add(new Report_Value(tripLog.getDateTime(), String.format("%.2f", tripLog.getMileage() * metricScale)));
                }
                totalMileages += tripLog.getMileage() * metricScale;

                week_value[dayOfWeek] = true;
            }
        }
        Log.d("result", week_value.toString());
        txt_mileage_mile.setText(formatDouble(totalMileages));
        if (totalTrips < 1e-8) totalTrips = 0;
        //int average_trip = (int)Math.ceil(totalMileages/totalTrips);
        //  txt_average_trip.setText(String.valueOf(average_trip));
        txt_total_stops.setText(String.valueOf(totalTrips));
        txt_max_speed.setText(String.format("%.2f", maxSpeed));
        txt_harsh_acceleration.setText(String.valueOf(harshAcceNum));
        txt_harsh_deceleration.setText(String.valueOf(harshDeceNum));
        txt_low_battery.setText("0");
        txt_fuel.setText(formatDouble(totalFuel));
        txt_driver_speeding.setText(String.valueOf(speedingNum));
        txt_idling.setText(String.valueOf(idleNum));
        listDataChild.clear();
        listDataGroup.clear();
        listDataGroup.add(new Report_Group("Distance", String.format("%.0f %s", totalMileages, distanceUnit)));
        listDataGroup.add(new Report_Group("Max Speed", String.format("%.0f %s", maxSpeed, speedUnit)));
        listDataGroup.add(new Report_Group("Total Fuel", String.format("%.2f", totalFuel)));
        listDataGroup.add(new Report_Group("Total Stops", String.valueOf(totalTrips)));
        listDataGroup.add(new Report_Group("Rapid Accel", String.valueOf(harshAcceNum)));
        listDataGroup.add(new Report_Group("Hard Braking", String.valueOf(harshDeceNum)));
        listDataGroup.add(new Report_Group("Speeding", String.valueOf(speedingNum)));
        listDataGroup.add(new Report_Group("Idling", String.valueOf(idleNum)));
        listDataGroup.add(new Report_Group("Engine on", String.valueOf(eventList.size())));
        listDataGroup.add(new Report_Group("Engine off", String.valueOf(eventList.size())));
        listDataGroup.add(new Report_Group("Device removal", String.valueOf(eventList.size())));

        listDataChild.put(getString(R.string.distance), mileage_List);
        listDataChild.put(getString(R.string.max_speed), totalspeed_List);
        listDataChild.put(getString(R.string.total_fuel), fuel_List);
        listDataChild.put(getString(R.string.total_stops), stop_List);
        listDataChild.put(getString(R.string.rapid_accel), harsh_acceList);
        listDataChild.put(getString(R.string.hard_braking), harsh_deceList);
        listDataChild.put(getString(R.string.speeding), speeding_List);
        listDataChild.put(getString(R.string.idling), idling_List);
        listDataChild.put("Engine on", idling_List);
        listDataChild.put("Engine off", idling_List);
        listDataChild.put("Device removal", idling_List);
        expandableListViewAdapter = new ReportExpandListViewAdapter(this, getContext(), listDataGroup, listDataChild, eventList);

        expandableListView.setAdapter(expandableListViewAdapter);
        //  expandableListViewAdapter.notifyDataSetChanged();
    }

    private class ParallaxStikkyAnimator extends HeaderStikkyAnimator {
        @Override
        public AnimatorBuilder getAnimatorBuilder() {
            View mHeader_image = getHeader().findViewById(R.id.topScroll);
            return AnimatorBuilder.create().applyVerticalParallax(mHeader_image, 1);
        }
    }

    public String formatDouble(double d) {
        return String.format("%.2f", d);
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
