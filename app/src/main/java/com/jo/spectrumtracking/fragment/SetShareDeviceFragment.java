package com.jo.spectrumtracking.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.activity.MainActivity;
import com.jo.spectrumtracking.adapter.SetSharedDeviceRecyclerViewAdapter;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_SharedDevice;
import com.jo.spectrumtracking.model.Resp_Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetShareDeviceFragment extends Fragment {
    boolean isFragmentAlive = false;
    List<Resp_SharedDevice> deviceList = new ArrayList<>();
    @BindView(R.id.rv_tracker_list)
    RecyclerView rvShareTrackerList;

    @BindView(R.id.label_no_device)
    TextView label_no_device;

    public SetShareDeviceFragment() {
        // Required empty public constructor
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
        View rootView = inflater.inflate(R.layout.fragment_set_share_device, container, false);

        ButterKnife.bind(this, rootView);
        isFragmentAlive = true;

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadSharedDevice();
    }

    public void loadSharedDevice() {
        JSONArray _sharedDeviceList = null;
        try {
            _sharedDeviceList = GlobalConstant.app_user.getJSONArray("sharedDeviceList");
            if (_sharedDeviceList == null || _sharedDeviceList.length() == 0) {
                label_no_device.setVisibility(View.VISIBLE);
                return;
            }
            for (int i = 0; i < _sharedDeviceList.length(); i++) {
                JSONObject _sharedTracker = (JSONObject) _sharedDeviceList.get(i);
                Resp_SharedDevice _sharedDevice = new Resp_SharedDevice();
                _sharedDevice.setFlag(_sharedTracker.getString("flag"));
                _sharedDevice.setReport_id(_sharedTracker.getString("reportId"));
                String reportId = _sharedTracker.getString("reportId");
                Resp_Tracker _tracker = null;
                for (int j = 0; j < GlobalConstant.sharedTrackerList.size(); j++) {
                    if (GlobalConstant.sharedTrackerList.get(j).getReportingId().equals(reportId)) {
                        _tracker = GlobalConstant.sharedTrackerList.get(j);
                        break;
                    }
                }
                if (_tracker != null) {
                    _sharedDevice.setPlateNumber(_tracker.getPlateNumber() + "(" + _tracker.getDriverName() + ")");
                    deviceList.add(_sharedDevice);
                }

            }
            if (deviceList.size() == 0) {
                label_no_device.setVisibility(View.VISIBLE);
                return;
            }
            SetSharedDeviceRecyclerViewAdapter adapter =
                    new SetSharedDeviceRecyclerViewAdapter(
                            SetShareDeviceFragment.this,
                            deviceList,
                            R.layout.recyclerview_row_set_share_device);

            rvShareTrackerList.setAdapter(adapter);
            rvShareTrackerList.setLayoutManager(new LinearLayoutManager(SetShareDeviceFragment.this.getContext().getApplicationContext()));
            rvShareTrackerList.setItemAnimator(new DefaultItemAnimator());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getUserInfo() {
        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);
        apiInterface.getUserInfo(GlobalConstant.X_CSRF_TOKEN).enqueue(new Callback<ResponseBody>() {
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
//                        Utils.showShortToast(SetShareDeviceFragment.this.getContext(), getString(R.string.weak_cell_signal));
                    }
                } else {
//                    Utils.showShortToast(SetShareDeviceFragment.this.getContext(), getString(R.string.weak_cell_signal));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(SetShareDeviceFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    public void setShareFlag(String reportId, String flag) {

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();
        body.put("reportId", reportId);
        body.put("flag", flag);
        apiInterface.setShareFlag(body).enqueue(new Callback<ResponseBody>() {
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
//                    Utils.showShortToast(SetShareDeviceFragment.this.getContext(), "response parse error");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(SetShareDeviceFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    // TODO: Rename and change types and number of parameters
    public static SetShareDeviceFragment newInstance() {
        SetShareDeviceFragment fragment = new SetShareDeviceFragment();

        return fragment;
    }

    @OnClick(R.id.back)
    public void onBack() {
        MainActivity.get().popFragment();
    }
}
