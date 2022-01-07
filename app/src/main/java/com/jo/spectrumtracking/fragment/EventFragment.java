package com.jo.spectrumtracking.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.jo.spectrumtracking.adapter.EventAdapter;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_Error;
import com.jo.spectrumtracking.model.Resp_Event;
import com.jo.spectrumtracking.model.Resp_Tracker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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

public class EventFragment extends Fragment {

    ApiInterface apiInterface;
    Call<ResponseBody> apiCall;

    @BindView(R.id.topView)
    LinearLayout topView;
    @BindView(R.id.rv_replay_right_options)
    RecyclerView rvAssetSingleSelect;
    @BindView(R.id.eventRecyclerView)
    RecyclerView eventRecyclerView;
    @BindView(R.id.date_from_to)
    TextView txt_start_date;
    @BindView(R.id.btn_forward)
    ImageView btn_forward;
    @BindView(R.id.bottomView)
    ScrollView bottomView;

    boolean isFragmentAlive = false;

    Calendar replayStartDate;
    Calendar replayEndDate;

    List<Resp_Tracker> trackerList = null;
    Resp_Tracker selectedTracker = null;
    List<Resp_Event> assetLogList = null;
    AssetListSingleSelectRecyclerViewAdapter adapter = null;
    EventAdapter event_adapter = null;
    int day_index = 0;

    public EventFragment() {
        // Required empty public constructor
    }

    public static EventFragment newInstance() {
        EventFragment fragment = new EventFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFragmentAlive = true;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        isFragmentAlive = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_events, container, false);

        ButterKnife.bind(this, rootView);

        apiInterface = ApiClient.getClient(getContext()).create(ApiInterface.class);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(getString(R.string.event));
        initBottomPanel();
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
            setSelectedTracker(selectedTracker);
        } else {
            loadAllDrivers();
        }
    }

    private void initBottomPanel() {
        if (!isFragmentAlive) {
            return;
        }
        Calendar day7before_start = Calendar.getInstance();
        day7before_start.add(Calendar.DAY_OF_YEAR, 0);
        setAssetLogStartDate(day7before_start);
        Calendar day7before_end = Calendar.getInstance();
        day7before_end.add(Calendar.DAY_OF_YEAR, 1);
        setAssetLogEndDate(day7before_end);
    }

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
                Utils.showShortToast(EventFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
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

    private void setEventRecyclerView() {
        if (!isFragmentAlive) {
            return;
        }

        event_adapter = new EventAdapter(this, assetLogList, R.layout.recyclerview_events_row);

        eventRecyclerView.setAdapter(event_adapter);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        eventRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    public void setSelectedTracker(Resp_Tracker selectedTracker) {
        if (!isFragmentAlive) {
            return;
        }

        this.selectedTracker = selectedTracker;
        for (Resp_Tracker tracker : trackerList) {
            if (selectedTracker.get_id() == tracker.get_id()) tracker.setSelected(true);
        }

        setAssetSingleSelectTableData();
        adapter.notifyDataSetChanged();

        loadReplay();
    }

    @OnClick(R.id.btn_forward)
    public void onNextDay() {
        if (!isFragmentAlive) {
            return;
        }
        day_index++;
        if (day_index >= 0) btn_forward.setVisibility(View.INVISIBLE);
        replayStartDate.add(Calendar.DAY_OF_YEAR, 1);
        replayEndDate.add(Calendar.DAY_OF_YEAR, 1);
        setAssetLogStartDate(replayStartDate);
        setAssetLogEndDate(replayEndDate);
        loadReplay();
    }

    @OnClick(R.id.btn_backward)
    public void onPrevDay() {
        if (!isFragmentAlive) {
            return;
        }
        day_index--;
        btn_forward.setVisibility(View.VISIBLE);
        replayStartDate.add(Calendar.DAY_OF_YEAR, -1);
        replayEndDate.add(Calendar.DAY_OF_YEAR, -1);
        setAssetLogStartDate(replayStartDate);
        setAssetLogEndDate(replayEndDate);
        loadReplay();
    }

    public void loadReplay() {
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

        String id = selectedTracker.get_id();

        if(apiCall!=null) {
            apiCall.cancel();
        }

        apiCall = apiInterface.trackers_id(id);
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

                    Resp_Tracker tracker = null;
                    try {
                        String bodyString = responseBody.string();
                        tracker = gson.fromJson(bodyString, Resp_Tracker.class);
                        loadTripLogs(tracker.getReportingId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(EventFragment.this.getContext(), error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                if (!isFragmentAlive) {
                    return;
                }
            }
        });
    }

    private void setAssetLogStartDate(Calendar date) {
        if (!isFragmentAlive) {
            return;
        }
        replayStartDate = date;
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd");
        System.out.println(replayStartDate.getTime().toString());
        txt_start_date.setText(sdf.format(replayStartDate.getTime()));
    }

    private void setAssetLogEndDate(Calendar date) {
        if (!isFragmentAlive) {
            return;
        }
        replayEndDate = date;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        System.out.println(replayEndDate.getTime().toString());
        //txt_end_date.setText(sdf.format(replayEndDate.getTime()));
    }

    public void loadTripLogs(String reportingId) {
        //TODO:- Replace Deprecated Date
        Date startTime = replayStartDate.getTime();
        Date endTime = replayEndDate.getTime();
        startTime.setHours(0);
        startTime.setMinutes(0);
        startTime.setSeconds(0);

        endTime.setHours(0);
        endTime.setMinutes(0);
        endTime.setSeconds(0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String startTimeString = sdf.format(startTime);
        String endTimeString = sdf.format(endTime);

        if(apiCall!=null) {
            apiCall.cancel();
        }

        apiCall = apiInterface.event_logs(reportingId, startTimeString, endTimeString);
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
                        Type type = new TypeToken<List<Resp_Event>>() {}.getType();
                        assetLogList = gson.fromJson(items.toString(), type);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (assetLogList != null) {
                        if (assetLogList.size() != 0) {
                            setEventRecyclerView();
                        } else {
                            Utils.showShortToast(EventFragment.this.getContext(), getString(R.string.no_data_change_to_another_day), true);
                        }
                    }
                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(EventFragment.this.getContext(), error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                if (!isFragmentAlive) {
                    return;
                }
            }
        });
    }

    private class ParallaxStikkyAnimator extends HeaderStikkyAnimator {
        @Override
        public AnimatorBuilder getAnimatorBuilder() {
            View mHeader_image = getHeader().findViewById(R.id.topLayout);
            return AnimatorBuilder.create().applyVerticalParallax(mHeader_image, 1);
        }
    }

}
