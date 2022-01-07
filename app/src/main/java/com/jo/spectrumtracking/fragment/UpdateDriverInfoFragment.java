package com.jo.spectrumtracking.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.jo.spectrumtracking.adapter.UpdateDriverInfoRecyclerViewAdapter;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_Error;
import com.jo.spectrumtracking.model.Resp_Tracker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateDriverInfoFragment extends Fragment {

    ApiInterface apiInterface;
    Call<ResponseBody> apiCall;

    boolean isFragmentAlive = false;

    @BindView(R.id.rv_update_driver_info)
    RecyclerView rvUpdateDriverInfo;

    List<Resp_Tracker> trackerList = null;

    public UpdateDriverInfoFragment() {
        // Required empty public constructor
    }

    public static UpdateDriverInfoFragment newInstance() {
        UpdateDriverInfoFragment fragment = new UpdateDriverInfoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_update_driver_info, container, false);

        ButterKnife.bind(this, rootView);

        isFragmentAlive = true;
        apiInterface = ApiClient.getClient(getContext()).create(ApiInterface.class);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentAlive = false;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Update Driver Information");

        trackerList = new ArrayList<>();
        if (GlobalConstant.AllTrackerList.size() > 0) {
            trackerList.addAll(GlobalConstant.AllTrackerList);
            setDriverInfoTableData();
        } else {
            loadAllDrivers();
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

                        try {
                            for (final Resp_Tracker tracker : newTrackerList) {
//                                if (tracker.getLat() == 0.0 && tracker.getLng() == 0.0) {
//                                    continue;
//                                }
//                                if (Math.abs(tracker.getLat()) > 90.0 || Math.abs(tracker.getLng()) > 180) {
//                                    continue;
//                                }
//                                if (isPhoneTracking == false && tracker.getSpectrumId().equals(GlobalConstant.email)) {
//                                    continue;
//                                }
                                trackerList.add(tracker);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        setDriverInfoTableData();
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
                Utils.showShortToast(UpdateDriverInfoFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    private void setDriverInfoTableData() {
        UpdateDriverInfoRecyclerViewAdapter adapter =
                new UpdateDriverInfoRecyclerViewAdapter(
                        UpdateDriverInfoFragment.this,
                        trackerList,
                        R.layout.recyclerview_row_update_driver_info);

        rvUpdateDriverInfo.setAdapter(adapter);
        rvUpdateDriverInfo.setLayoutManager(new LinearLayoutManager(UpdateDriverInfoFragment.this.getContext().getApplicationContext()));
        rvUpdateDriverInfo.setItemAnimator(new DefaultItemAnimator());
    }

    public void onUpdateButtonClick(Resp_Tracker tracker, String driverName, String driverPhone, String vehicleName, String color, String autoRenew) {
        doUpdateWork(tracker, driverName, driverPhone, vehicleName, color, autoRenew);
    }

    private void doUpdateWork(Resp_Tracker tracker, String driverName, String driverPhone, String vehicleName, String color, String autoRenew) {
        if (!Utils.isNetworkConnected(this.getContext())) {
            return;
        }

        HashMap<String, Object> body = new HashMap<>();

        body.put("id", tracker.get_id());
        body.put("driverName", driverName);
        body.put("autoRenew", autoRenew);
        body.put("plateNumber",vehicleName);
        //body.put("driverPhoneNumber", driverPhone);
        body.put("color", color);

        if(apiCall!=null) {
            apiCall.cancel();
        }

        apiCall = apiInterface.modify(GlobalConstant.X_CSRF_TOKEN,body);

        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isFragmentAlive) {
                    return;
                }

                //  Utils.hideProgress();

                int code = response.code();

                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);

                Gson gson = gsonBuilder.create();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;

                    Utils.showShortToast(UpdateDriverInfoFragment.this.getContext(), getString(R.string.update_success), false);

                    loadAllDrivers();
                } else {

                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(UpdateDriverInfoFragment.this.getContext(), error.getMessage(), true);
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
}
