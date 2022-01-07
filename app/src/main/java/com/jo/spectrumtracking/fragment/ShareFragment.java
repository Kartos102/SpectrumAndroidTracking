package com.jo.spectrumtracking.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
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
import com.jo.spectrumtracking.adapter.ShareTrackerRecyclerViewAdapter;
import com.jo.spectrumtracking.adapter.SharedUserListRecyclerViewAdapter;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_Share;
import com.jo.spectrumtracking.model.Resp_SharedList;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.jo.spectrumtracking.model.Resp_User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
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


public class ShareFragment extends Fragment {

    ApiInterface apiInterface;
    Call<ResponseBody> apiCall;

    boolean isFragmentAlive = false;
    private String[] asset_ids;
    private int selected_index = 0;
    private ShareTrackerRecyclerViewAdapter main_adapter;
    private boolean isLodaingDrivers = false;

    List<Resp_Tracker> trackerList = null;
    List<Resp_Share> shareList = null;
    List<Resp_SharedList> sharedUserList = null;

    @BindView(R.id.rv_tracker_list)
    RecyclerView rvShareTrackerList;

    @BindView(R.id.rv_sharedUser_list)
    RecyclerView rvSharedUserList;

    @BindView(R.id.sharedListCardView)
    CardView sharedListCardView;

    @BindView(R.id.btn_ok)
    Button btn_ok;

    @BindView(R.id.btn_selectAll)
    Button btn_selectAll;

    @BindView(R.id.backview)
    View backView;

    @BindView(R.id.txt_no_user)
    TextView txt_no_user;

    public ShareFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_share, container, false);

        ButterKnife.bind(this, rootView);
        isFragmentAlive = true;

        apiInterface = ApiClient.getClient(getContext()).create(ApiInterface.class);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_ok.setOnClickListener(v -> {
            String str = "";
            for (Resp_SharedList resp_sharedList : sharedUserList) {
                if (resp_sharedList.getChecked()) {
                    str += resp_sharedList.getEmail() + ",";
                }
            }
            if (!str.equals("")) {
                str = str.substring(0, str.length() - 1);
            } else {
                str = getString(R.string.select_users);
            }
            shareList.get(selected_index).setSpinner_label(str);
            main_adapter.notifyItemChanged(selected_index);
            backView.setVisibility(View.GONE);
            sharedListCardView.setVisibility(View.GONE);
        });
        btn_selectAll.setOnClickListener(v -> {
            String str = "";
            for (Resp_SharedList resp_sharedList : sharedUserList) {
                str += resp_sharedList.getEmail() + ",";
            }
            if (!str.equals("")) {
                str = str.substring(0, str.length() - 1);
            } else {
                str = getString(R.string.select_users);
            }
            shareList.get(selected_index).setSpinner_label(str);
            main_adapter.notifyItemChanged(selected_index);
            backView.setVisibility(View.GONE);
            sharedListCardView.setVisibility(View.GONE);
        });
        backView.setOnClickListener(v -> {
            backView.setVisibility(View.GONE);
            sharedListCardView.setVisibility(View.GONE);
        });

        trackerList = new ArrayList<>();
        shareList = new ArrayList<>();

        if (GlobalConstant.AllTrackerList.size() > 0) {
            trackerList.addAll(GlobalConstant.AllTrackerList);

            for (Resp_Tracker tracker : trackerList) {
                Resp_Share _temp = new Resp_Share();
                _temp.setReport_id(tracker.getReportingId());
                _temp.setPlateNumber(tracker.getPlateNumber());
                _temp.setSpinner_label(getString(R.string.select_users));
                shareList.add(_temp);
            }

            setTrackerListTableData();
        } else {
            loadAllDrivers();
        }
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
        if (shareList == null) {
            shareList = new ArrayList<>();
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

                        trackerList.clear();
                        shareList.clear();

                        for (Resp_Tracker tracker : newTrackerList) {
                            Resp_Share _temp = new Resp_Share();
                            _temp.setReport_id(tracker.getReportingId());
                            _temp.setPlateNumber(tracker.getPlateNumber());
                            _temp.setSpinner_label(getString(R.string.select_users));
                            trackerList.add(tracker);
                            shareList.add(_temp);
                        }
                        setTrackerListTableData();
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
                Utils.showShortToast(ShareFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    public void onCheckSharedUserList(int position, Boolean checked) {
        sharedUserList.get(position).setChecked(checked);
    }

    public void getShareUsers(final Resp_Share item, int position) {
        selected_index = position;
        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();
        body.put("reportId", item.getReport_id());
        apiInterface.getShareUsers(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    GsonBuilder gsonBuilder = new GsonBuilder();
                    gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
                    Gson gson = gsonBuilder.create();
                    try {
                        object = new JSONObject(responseBody.string());
                        JSONArray items = (JSONArray) object.get("items");
                        Type type = new com.google.common.reflect.TypeToken<List<Resp_User>>() {
                        }.getType();
                        List<Resp_User> userList = gson.fromJson(items.toString(), type);
                        sharedUserList = new ArrayList<>();
                        for (Resp_User user : userList) {
                            Resp_SharedList _temp = new Resp_SharedList();
                            _temp.setEmail(user.getEmail());
                            _temp.setChecked(false);
                            if (item.getSpinner_label().contains(user.getEmail()))
                                _temp.setChecked(true);
                            sharedUserList.add(_temp);
                        }
                        if (sharedUserList.size() == 0) txt_no_user.setVisibility(View.VISIBLE);
                        else txt_no_user.setVisibility(View.GONE);
                        SharedUserListRecyclerViewAdapter adapter =
                                new SharedUserListRecyclerViewAdapter(
                                        ShareFragment.this,
                                        sharedUserList,
                                        R.layout.recyclerview_row_shared_user_list);

                        rvSharedUserList.setAdapter(adapter);
                        rvSharedUserList.setLayoutManager(new LinearLayoutManager(ShareFragment.this.getContext().getApplicationContext()));
                        rvSharedUserList.setItemAnimator(new DefaultItemAnimator());
                        backView.setVisibility(View.VISIBLE);
                        sharedListCardView.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(ShareFragment.this.getContext(), getString(R.string.weak_cell_signal));
                    }
                } else {
//                    Utils.showShortToast(ShareFragment.this.getContext(), getString(R.string.weak_cell_signal));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(ShareFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    public void shareTracker(String reportId, String Email) {
        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();
        body.put("reportId", reportId);
        body.put("email", Email);
        body.put("flag", "0");
        apiInterface.shareTrakcer(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());
                        if (object.getString("result").equals("true")) {
                            Utils.showShortToast(ShareFragment.this.getContext(), getString(R.string.success), false);
                        } else {
                            Utils.showShortToast(ShareFragment.this.getContext(), object.getString("message"), true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(ShareFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    public void unShareTracker(String reportId, String[] Email) {
        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();
        body.put("reportId", reportId);
        body.put("email", Email);
        apiInterface.unShareTrakcer(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                if (code == 200) {
                    Utils.showShortToast(ShareFragment.this.getContext(), getString(R.string.unshare_success), false);
                } else {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(ShareFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    private void setTrackerListTableData() {
        main_adapter =
                new ShareTrackerRecyclerViewAdapter(
                        ShareFragment.this,
                        shareList,
                        R.layout.recyclerview_row_share_tracker);

        rvShareTrackerList.setAdapter(main_adapter);
        rvShareTrackerList.setLayoutManager(new LinearLayoutManager(ShareFragment.this.getContext().getApplicationContext()));
        rvShareTrackerList.setItemAnimator(new DefaultItemAnimator());
    }

    // TODO: Rename and change types and number of parameters
    public static ShareFragment newInstance() {
        ShareFragment fragment = new ShareFragment();

        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ShareTrackerRecyclerViewAdapter.REQUEST_CODE_CONTACT) {
            String email = "";
            Uri uri = data.getData();
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                email = cursor.getString(index);
            }
            cursor.close();

            main_adapter.importedContactEmail = email;
            main_adapter.notifyDataSetChanged();
        }
    }

    @OnClick(R.id.back)
    public void onBack() {
        MainActivity.get().popFragment();
    }
}
