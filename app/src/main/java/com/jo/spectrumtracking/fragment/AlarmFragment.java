package com.jo.spectrumtracking.fragment;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

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
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_Alarm;
import com.jo.spectrumtracking.model.Resp_Error;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.jo.spectrumtracking.widget.CustomSwitch;
import com.mapbox.mapboxsdk.Mapbox;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import it.carlom.stikkyheader.core.StikkyHeaderBuilder;
import it.carlom.stikkyheader.core.animator.AnimatorBuilder;
import it.carlom.stikkyheader.core.animator.HeaderStikkyAnimator;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AlarmFragment extends Fragment {

    ApiInterface apiInterface;
    Call<ResponseBody> apiCall;

    @BindView(R.id.rv_alarm_right_options)
    RecyclerView rvAssetSingleSelect;
    @BindView(R.id.topView)
    RelativeLayout topView;
    @BindView(R.id.userTracker_speedLimit)
    EditText txt_userTracker_speedLimit;
    @BindView(R.id.userTracker_fatigueTime)
    EditText txt_userTracker_fatigueTime;
    @BindView(R.id.userTracker_email)
    EditText txt_userTracker_email;
    @BindView(R.id.textAlert_phonenumber)
    EditText txt_textAlert_phonenumber;
    @BindView(R.id.speedingAlarmStatus)
    CustomSwitch s_speedingAlarmStatus;
    @BindView(R.id.bottomView)
    ScrollView bottomView;
    @BindView(R.id.airplaneModeAlarmStatus)
    CustomSwitch s_airplaneModeAlarmStatus;
    @BindView(R.id.textAlertAlarmStatus)
    CustomSwitch s_textAlertAlarmStatus;
    @BindView(R.id.fatigueAlarmStatus)
    CustomSwitch s_fatigueAlarmStatus;

    @BindView(R.id.harshTurnAlarmStatus)
    CustomSwitch s_harshTurnAlarmStatus;
    @BindView(R.id.harshAcceAlarmStatus)
    CustomSwitch s_harshAcceAlarmStatus;
    @BindView(R.id.harshDeceAlarmStatus)
    CustomSwitch s_harshDeceAlarmStatus;
    @BindView(R.id.tamperAlarmStatus)
    CustomSwitch s_tamperAlarmStatus;
    @BindView(R.id.geoFenceAlarmStatus)
    CustomSwitch s_geoFenceAlarmStatus;
    @BindView(R.id.engineAlarmStatus)
    CustomSwitch s_engineAlarmStatus;
    @BindView(R.id.emailAlarmStatus)
    CustomSwitch s_emailAlarmStatus;
    @BindView(R.id.switch_alertsound)
    CustomSwitch s_alertSound;
    @BindView(R.id.switch_vibration)
    CustomSwitch s_vibration;
    @BindView(R.id.engineOffAlarmStatus)
    CustomSwitch s_engineOffAlarmStatus;
    @BindView(R.id.coolantAlarmStatus)
    CustomSwitch s_coolantAlarmStatus;
    @BindView(R.id.engineHealthAlarmStatus)
    CustomSwitch s_engineHealthAlarmStatus;
    @BindView(R.id.engineIdleAlarmStatus)
    CustomSwitch s_engineIdleAlarmStatus;
    @BindView(R.id.btn_save_textAlert)
    Button btn_sTextAlert;
    @BindView(R.id.btn_save_email)
    Button btn_sEmail;
    @BindView(R.id.btn_save_speed)
    Button btn_sSpeed;

    Boolean onViewCreatedOnceCalled = false;

    List<Resp_Tracker> trackerList = null;
    Resp_Tracker selectedTracker = null;

    AssetListSingleSelectRecyclerViewAdapter adapter = null;

    boolean isFragmentAlive = false;
    boolean change_flag = false;

    @OnCheckedChanged(R.id.speedingAlarmStatus)
    public void onChangeSpeedingAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.airplaneModeAlarmStatus)
    public void onChangeAirplaneModeAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.textAlertAlarmStatus)
    public void onChangetextAlertAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.harshTurnAlarmStatus)
    public void onChangeHarshTurnAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.harshAcceAlarmStatus)
    public void onChangeHarshAcceAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.harshDeceAlarmStatus)
    public void onChangeHarshDeceAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.tamperAlarmStatus)
    public void onChangeTamperAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.geoFenceAlarmStatus)
    public void onChangeGeofenceAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.coolantAlarmStatus)
    public void onChangeCoolantAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.engineHealthAlarmStatus)
    public void onChangeEngineHealthAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.engineIdleAlarmStatus)
    public void onChangeEngineIdleAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.engineAlarmStatus)
    public void onChangeEngineOnAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.emailAlarmStatus)
    public void onChangeEmailAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.switch_alertsound)
    public void onChangeAlertSoundStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.switch_vibration)
    public void onChangeVibrationStatus() {
        onSetAlarmButton_click();
    }

    @OnCheckedChanged(R.id.engineOffAlarmStatus)
    public void onChangeEngineOffAlarmStatus() {
        onSetAlarmButton_click();
    }

    @OnClick(R.id.btn_save_textAlert)
    public void onSaveTextAlert() {
        onSetAlarmButton_click();
    }

    @OnClick(R.id.btn_save_email)
    public void onSaveEmail() {
        onSetAlarmButton_click();
    }

    @OnClick(R.id.btn_save_speed)
    public void onSaveSpeed() {
        onSetAlarmButton_click();
    }


    public AlarmFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static AlarmFragment newInstance() {
        AlarmFragment fragment = new AlarmFragment();

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
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_alarm, container, false);
        ButterKnife.bind(this, rootView);

        Mapbox.getInstance(this.getActivity(), GlobalConstant.MAP_BOX_ACCESS_TOKEN);

        apiInterface = ApiClient.getClient(getContext()).create(ApiInterface.class);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (onViewCreatedOnceCalled) {
            return;
        }
        onViewCreatedOnceCalled = true;

        getActivity().setTitle("Set Alarm");
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
            setSelectedTracker(selectedTracker);
        } else {
            loadAllDrivers();
        }

        rvAssetSingleSelect.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        rvAssetSingleSelect.setHasFixedSize(true);

        StikkyHeaderBuilder.stickTo(bottomView)
                .setHeader(topView.getId(), (ViewGroup) getView())
                .minHeightHeader(0)
                .animator(new ParallaxStikkyAnimator())
                .build();

        txt_textAlert_phonenumber.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!change_flag) return;
                btn_sTextAlert.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        txt_userTracker_email.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!change_flag) return;
                btn_sEmail.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });

        txt_userTracker_speedLimit.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!change_flag) return;
                btn_sSpeed.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        });
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
            }
        });
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

        if (selectedTracker == null) return;

        initialize_comps();

//        String trackerId = selectedTracker.getAssetId();
        String trackerId = selectedTracker.get_id();

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
                    // success
                    ResponseBody responseBody = response.body();

                    Resp_Alarm respAlarm = null;

                    try {
                        String bodyString = responseBody.string();
                        Log.d("bodyString", bodyString);
                        respAlarm = gson.fromJson(bodyString, Resp_Alarm.class);

                        loadAlarmValues(respAlarm);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(AlarmFragment.this.getContext(), error.getMessage(), true);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Utils.hideProgress();
                t.printStackTrace();
                if (!isFragmentAlive) {
                    return;
                }
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

    /***
     * Update UI with alarm data
     * @param respAlarm
     */
    private void loadAlarmValues(Resp_Alarm respAlarm) {

        txt_userTracker_speedLimit.setText(respAlarm.speedLimit);
        txt_userTracker_fatigueTime.setText(respAlarm.fatigueTime);
        txt_userTracker_email.setText(respAlarm.email);
        txt_textAlert_phonenumber.setText(respAlarm.phoneNumber);
        btn_sEmail.setVisibility(View.GONE);
        btn_sTextAlert.setVisibility(View.GONE);
        btn_sSpeed.setVisibility(View.GONE);
        s_speedingAlarmStatus.setChecked(respAlarm.speedingAlarmStatus);
        s_fatigueAlarmStatus.setChecked(respAlarm.fatigueAlarmStatus);
        s_harshTurnAlarmStatus.setChecked(respAlarm.harshTurnAlarmStatus);
        s_harshAcceAlarmStatus.setChecked(respAlarm.harshAcceAlarmStatus);
        s_harshDeceAlarmStatus.setChecked(respAlarm.harshDeceAlarmStatus);
        s_tamperAlarmStatus.setChecked(respAlarm.tamperAlarmStatus);
        s_geoFenceAlarmStatus.setChecked(respAlarm.geoFenceAlarmStatus);
        s_emailAlarmStatus.setChecked(respAlarm.emailAlarmStatus);
        s_engineAlarmStatus.setChecked(respAlarm.accAlarmStatus);
        s_textAlertAlarmStatus.setChecked(respAlarm.phoneAlarmStatus);
        s_alertSound.setChecked(respAlarm.soundAlarmStatus);
        if (respAlarm.airplaneMode != null) {
            s_airplaneModeAlarmStatus.setChecked(respAlarm.airplaneMode);
        }
        s_vibration.setChecked(respAlarm.vibrationAlarmStatus);
        if (respAlarm.stopAlarmStatus != null) {
            s_engineOffAlarmStatus.setChecked(respAlarm.stopAlarmStatus);
        }
        s_coolantAlarmStatus.setChecked(respAlarm.coolantTempAlarmStatus);
        s_engineHealthAlarmStatus.setChecked(respAlarm.engineAlarmStatus);
        s_engineIdleAlarmStatus.setChecked(respAlarm.engineIdleAlarmStatus);
        change_flag = true;
    }

    public void onSetAlarmButton_click() {
       //if (!change_flag) return;
        if (selectedTracker == null) {
            Toast.makeText(getContext(), getString(R.string.please_choose_vehicle), Toast.LENGTH_LONG).show();
            return;
        }
        InputMethodManager imm = (InputMethodManager) this.getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
      /*  imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        try {
            imm.hideSoftInputFromWindow(this.getActivity().getCurrentFocus().getWindowToken(), 0);
        } catch (Exception ex) {
            return;
        }*/
        btn_sEmail.setVisibility(View.GONE);
        btn_sTextAlert.setVisibility(View.GONE);
        btn_sSpeed.setVisibility(View.GONE);
       // String id = selectedAsset.getTrackerId();  //trackerId
        String id = selectedTracker.get_id();
        String speedLimit = txt_userTracker_speedLimit.getText().toString();
        String fatigueTime = txt_userTracker_fatigueTime.getText().toString();
        String harshTurn = "120";//txt_userTracker_harshTurn.getText().toString();
        String harshAcceleration = "1";//txt_userTracker_harshAcceleration.getText().toString();
        String harshDeceleration = "1";//txt_userTracker_harshDeceleration.getText().toString();
        String email = txt_userTracker_email.getText().toString();
        String phoneNumber = txt_textAlert_phonenumber.getText().toString();

        boolean speedingAlarmStatus = s_speedingAlarmStatus.isChecked();
        boolean fatigueAlarmStatus = s_fatigueAlarmStatus.isChecked();
        boolean phoneAlarmStatus = s_textAlertAlarmStatus.isChecked();
        boolean harshTurnAlarmStatus = s_harshTurnAlarmStatus.isChecked();
        boolean harshAcceAlarmStatus = s_harshAcceAlarmStatus.isChecked();
        boolean harshDeceAlarmStatus = s_harshDeceAlarmStatus.isChecked();
        boolean tamperAlarmStatus = s_tamperAlarmStatus.isChecked();
        boolean airplaneMode = s_airplaneModeAlarmStatus.isChecked();
        boolean geoFenceAlarmStatus = s_geoFenceAlarmStatus.isChecked();
        boolean emailAlarmStatus = s_emailAlarmStatus.isChecked();
        boolean accAlarmStatus = s_engineAlarmStatus.isChecked();
        boolean soundAlarmStatus = s_alertSound.isChecked();
        boolean vibrationAlarmStatus = s_vibration.isChecked();
        boolean stopAlarmStatus = s_engineOffAlarmStatus.isChecked();
        boolean coolantAlarmStatus = s_coolantAlarmStatus.isChecked();
        boolean healthAlarmStatus = s_engineHealthAlarmStatus.isChecked();
        boolean idleAlarmStatus = s_engineIdleAlarmStatus.isChecked();

        if (emailAlarmStatus && email.indexOf('@') <= -1) {
            Toast.makeText(getContext(), getString(R.string.use_valid_email_address), Toast.LENGTH_LONG).show();
            return;
        }

        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("id", id);
        param.put("speedLimit", speedLimit);
        param.put("fatigueTime", fatigueTime);
        param.put("harshTurn", harshTurn);
        param.put("harshAcceleration", harshAcceleration);
        param.put("harshDeceleration", harshDeceleration);
        param.put("email", email);
        param.put("phoneNumber", phoneNumber);
        param.put("phoneAlarmStatus", phoneAlarmStatus);
        param.put("speedingAlarmStatus", speedingAlarmStatus);
        param.put("fatigueAlarmStatus", fatigueAlarmStatus);
        param.put("harshTurnAlarmStatus", harshTurnAlarmStatus);
        param.put("harshAcceAlarmStatus", harshAcceAlarmStatus);
        param.put("harshDeceAlarmStatus", harshDeceAlarmStatus);
        param.put("airplaneMode", airplaneMode);
        param.put("geoFenceAlarmStatus", geoFenceAlarmStatus);
        param.put("tamperAlarmStatus", tamperAlarmStatus);
        param.put("emailAlarmStatus", emailAlarmStatus);
        param.put("accAlarmStatus", accAlarmStatus);
        param.put("soundAlarmStatus", soundAlarmStatus);
        param.put("vibrationAlarmStatus", vibrationAlarmStatus);
        param.put("stopAlarmStatus", stopAlarmStatus);
        param.put("coolantTempAlarmStatus", coolantAlarmStatus);
        param.put("engineIdleAlarmStatus", idleAlarmStatus);
        param.put("engineAlarmStatus", healthAlarmStatus);

        if(apiCall!=null) {
            apiCall.cancel();
        }

        apiCall= apiInterface.modify(GlobalConstant.X_CSRF_TOKEN,param);
        apiCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (!isFragmentAlive) {
                    return;
                }

                int code = response.code();
                Gson gson = new Gson();

                if (code == 200) {
                    // success
//                    Utils.showShortToast(AlarmFragment.this.getContext(), getString(R.string.success), false);
                } else {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                if (!isFragmentAlive) {
                    return;
                }

                Utils.hideProgress();
            }
        });

    }

    private void initialize_comps() {
        change_flag = false;
        txt_userTracker_speedLimit.setText("");
        txt_userTracker_fatigueTime.setText("");
        txt_userTracker_email.setText("");
        txt_textAlert_phonenumber.setText("");

        s_speedingAlarmStatus.setChecked(false);
        s_fatigueAlarmStatus.setChecked(false);
        s_harshTurnAlarmStatus.setChecked(false);
        s_airplaneModeAlarmStatus.setChecked(false);
        s_harshAcceAlarmStatus.setChecked(false);
        s_harshDeceAlarmStatus.setChecked(false);
        s_textAlertAlarmStatus.setChecked(false);
        s_tamperAlarmStatus.setChecked(false);
        s_geoFenceAlarmStatus.setChecked(false);
        s_emailAlarmStatus.setChecked(false);
        s_alertSound.setChecked(false);
        s_vibration.setChecked(false);
        s_engineOffAlarmStatus.setChecked(false);
    }

    private class ParallaxStikkyAnimator extends HeaderStikkyAnimator {
        @Override
        public AnimatorBuilder getAnimatorBuilder() {
            View mHeader_image = getHeader().findViewById(R.id.topScroll);
            return AnimatorBuilder.create().applyVerticalParallax(mHeader_image, 1);
        }
    }

}
