package com.jo.spectrumtracking.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
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
import com.jo.spectrumtracking.activity.CheckoutActivity;
import com.jo.spectrumtracking.activity.MainActivity;
import com.jo.spectrumtracking.adapter.OrderServiceRecyclerViewAdapter;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.LTEData;
import com.jo.spectrumtracking.model.OrderService;
import com.jo.spectrumtracking.model.Resp_Tracker;
import com.jo.spectrumtracking.model.ServicePlan;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class OrderServiceFragment extends Fragment {
    @BindView(R.id.rv_order_service)
    RecyclerView rvOrderService;
    @BindView(R.id.txt_service_plan_sum)
    TextView txtServicePlanSum;
    @BindView(R.id.txt_lte_data_sum)
    TextView txtLTEDataSum;
    @BindView(R.id.txt_total_sum)
    TextView txtTotalSum;
    @BindView(R.id.txt_description)
    TextView txtDescription;
    @BindView(R.id.layout_order_summary)
    LinearLayout layout_order_summary;

    List<Resp_Tracker> trackerList = new ArrayList<>();
    List<LTEData> allLtePlanList = new ArrayList<>();
    List<String> planList = new ArrayList<>();
    List<Map<String, String>> ltePlanList = new ArrayList<>();
    List<Map<String, String>> trackerPlanList = new ArrayList<>();
    List<Map<String, String>> trackerIntPlanList = new ArrayList<>();
    List<Map<String, String>> phonePlanList = new ArrayList<>();
    Map<String, List<String>> planDetailList = new HashMap<>();

    int device_index = 0;
    OrderServiceRecyclerViewAdapter adapter = null;
    boolean isFragmentAlive;

    public OrderServiceFragment() {
        // Required empty public constructor
    }

    public static OrderServiceFragment newInstance() {
        OrderServiceFragment fragment = new OrderServiceFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_order_service_new, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.order_service));
//        getTrackerSummary();
        getCheckOutPlans();

        txtDescription.setText(Html.fromHtml(getString(R.string.order_service_description)));
    }


    private String[] asset_ids;

    private void getCheckOutPlans() {
        Utils.showProgress(getContext());
        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();

        apiInterface.getCheckOutPlans().enqueue(new Callback<ResponseBody>() {
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
                        JSONArray items = (JSONArray) object.get("items");
                        JSONObject plans = (JSONObject) object.get("plans");

                        Type type = new TypeToken<List<Resp_Tracker>>() {}.getType();

                        List<Resp_Tracker> newTrackerList = gson.fromJson(items.toString(), type);
                        trackerList = newTrackerList;

                        JSONArray newAllLtePlans = (JSONArray) plans.get("allLTEPlans");
                        for (int i=0; i<newAllLtePlans.length(); i++) {
                            JSONObject obj = (JSONObject) newAllLtePlans.get(i);
                            String name = obj.getString("name");
                            Integer data = obj.getInt("data");
                            LTEData lteData = new LTEData(name, data);
                            allLtePlanList.add(lteData);
                        }

                        Type typeStringList = new TypeToken<List<String>>() {}.getType();
                        Type typeMapList = new TypeToken<List<HashMap<String, String>>>() {}.getType();
                        Type typeList = new TypeToken<HashMap<String, List<String>>>() {}.getType();

                        planList = gson.fromJson(plans.get("allPlans").toString(), typeStringList);

                        JSONArray ltePlans = (JSONArray) plans.get("LTEPlanArray");
                        if (ltePlans.length() > 0) {
                            ltePlanList = gson.fromJson(ltePlans.toString(), typeMapList);
                        }

                        JSONArray trackerPlans = (JSONArray) plans.get("trackerPlanArray");
                        if (trackerPlans.length() > 0) {
                            trackerPlanList = gson.fromJson(trackerPlans.toString(), typeMapList);
                        }

                        JSONArray trackerIntPlans = (JSONArray) plans.get("trackerIntPlanArray");
                        if (trackerIntPlans.length() > 0) {
                            trackerIntPlanList = gson.fromJson(trackerIntPlans.toString(), typeMapList);
                        }

                        JSONArray phonePlanPlans = (JSONArray) plans.get("phonePlanArray");
                        if (phonePlanPlans.length() > 0) {
                            phonePlanList = gson.fromJson(phonePlanPlans.toString(), typeMapList);
                        }

                        JSONArray planDetails = (JSONArray) plans.get("planDetails");
                        if (planDetails.length() > 0) {
                            planDetailList = gson.fromJson(planDetails.get(0).toString(), typeList);
                        }

                        onTrackersAllLoaded();
                    } catch (Exception e) {
                        Utils.hideProgress();
                        e.printStackTrace();
                    }
                } else {
                    Utils.hideProgress();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
            }
        });
    }

    private void onTrackersAllLoaded() {
        if (!isFragmentAlive) {
            return;
        }
        showTable();
    }

    private void showTable() {
        List<OrderService> items = new ArrayList<>();
        for (Resp_Tracker tracker : trackerList) {
            OrderService item = new OrderService();
            item.setName(tracker.getDriverName());
            item.setTrackerId(tracker.get_id());
            item.setServicePlanList(new ArrayList<>());
            item.setLteDataList(new ArrayList<>());
            //Utils.showShortToast(OrderServiceFragment.this.getContext(), tracker.country);

            if (tracker.getTrackerModel().equals("PHONE")) {
                //phone tracking plan
                for (int i=0; i<phonePlanList.size(); i++) {
                    Map<String, String> plan = phonePlanList.get(i);
                    if (plan.keySet().size() > 0) {
                        String key = plan.keySet().toArray()[0].toString();
                        String planValue = plan.get(key);
                        item.getServicePlanList().add(new ServicePlan(planValue, this.getPriceFromString(planValue), this.planDetailList.get(key)));
                    }
                }
            } else if (tracker.getCountry() != null && !tracker.getCountry().isEmpty() && !tracker.getCountry().equals("United States") && !tracker.getCountry().equals("US")) {
                // international tracker plan
                for (int i=0; i<trackerIntPlanList.size(); i++) {
                    Map<String, String> plan = trackerIntPlanList.get(i);
                    if (plan.keySet().size() > 0) {
                        String key = plan.keySet().toArray()[0].toString();
                        String planValue = plan.get(key);
                        item.getServicePlanList().add(new ServicePlan(planValue, this.getPriceFromString(planValue), this.planDetailList.get(key)));
                    }
                }
            } else {
                // domestic tracker plan
                for (int i=0; i<trackerPlanList.size(); i++) {
                    Map<String, String> plan = trackerPlanList.get(i);
                    if (plan.keySet().size() > 0) {
                        String key = plan.keySet().toArray()[0].toString();
                        String planValue = plan.get(key);
                        item.getServicePlanList().add(new ServicePlan(planValue, this.getPriceFromString(planValue), this.planDetailList.get(key)));
                    }
                }
            }

            if (tracker.getHotspot() == 1) {
                for (int i=0; i<ltePlanList.size(); i++) {
                    Map<String, String> plan = ltePlanList.get(i);
                    if (plan.keySet().size() > 0) {
                        String key = plan.keySet().toArray()[0].toString();
                        String planValue = plan.get(key);
                        item.getLteDataList().add(new LTEData(planValue, this.getPriceFromString(planValue)));
                    }
                }
            } else {
                item.getLteDataList().add(new LTEData("No Text: $0.00", 0.0));
            }

            item.setLteDataEnabled(true);
            Date expDate = null;//You will get date object relative to server/client timezone wherever it is parsed

            expDate = tracker.getExpirationDate();

            String tmp;

            if (!"".equals(tracker.getDataPlan()) && tracker.getDataPlan() != null) {
                tmp = tracker.getDataPlan();
                for (int i = 0; i < item.getServicePlanList().size(); i++) {
                    if (tmp.endsWith(item.getServicePlanList().get(i).getServicePlan())) {
                        item.setSelectedServicePlanId(i);
                    }
                }
            } else item.setSelectedServicePlanId(0);

            if (!"".equals(tracker.getLTEData()) && tracker.getLTEData() != null) {
                tmp = tracker.getLTEData();
                for (int i = 0; i < item.getLteDataList().size(); i++) {
                    if (tmp.endsWith(item.getLteDataList().get(i).getLteData())) {
                        item.setSelectedLTEDataId(i);
                    }
                }
            } else item.setSelectedLTEDataId(0);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                item.setExpirationDate(sdf.format(expDate));
            } catch (Exception ex) {
                item.setExpirationDate("");
//                item.setExpirationDate(sdf.format(new Date()));
            }

            item.setAutoReview(tracker.getAutoRenew().equals("true"));

            items.add(item);
        }


        adapter = new OrderServiceRecyclerViewAdapter(this, items, R.layout.recyclerview_row_service);

        rvOrderService.setAdapter(adapter);
        rvOrderService.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        rvOrderService.setItemAnimator(new DefaultItemAnimator());

        layout_order_summary.setVisibility(View.VISIBLE);
        Utils.hideProgress();
        updateBottomPrices();

    }

    public void updateBottomPrices() {
        List<OrderService> items = adapter.getItemList();

        double sum1, sum2;

        sum1 = 0;
        sum2 = 0;

        for (OrderService item : items) {
            sum1 += item.getServicePlanList().get(item.getSelectedServicePlanId()).getPrice();
            sum2 += item.getLteDataList().get(item.getSelectedLTEDataId()).getPrice();
        }

        txtServicePlanSum.setText("$" + String.format("%.2f", sum1));
        txtLTEDataSum.setText("$" + String.format("%.2f", sum2));
        txtTotalSum.setText("$" + String.format("%.2f", sum1 + sum2));

    }

    @OnClick(R.id.btn_proceed_to_checkout)
    public void onProceedToCheckoutClick() {
        if (adapter == null) return;
        GlobalConstant.orderServiceItemList = adapter.getItemList();
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.onCheckout();
//        Intent intent = new Intent(this.getContext(), CheckoutActivity.class);
//        intent.putExtra("from", "OrderServiceFragment");
//        if (adapter == null) return;
//        this.startActivity(intent);
    }

    double getPriceFromString(String text) {
        if (text == null) {
            return 0.0;
        }
        int index = text.indexOf("$");
        String price = text.substring(index + 1);

        try {
            return Double.parseDouble(price);
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}
