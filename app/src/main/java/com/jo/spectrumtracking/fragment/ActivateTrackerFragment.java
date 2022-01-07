package com.jo.spectrumtracking.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.activity.MainActivity;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import belka.us.androidtoggleswitch.widgets.BaseToggleSwitch;
import belka.us.androidtoggleswitch.widgets.ToggleSwitch;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import soup.neumorphism.NeumorphButton;

public class ActivateTrackerFragment extends Fragment {
    @BindView(R.id.edit_tracker_id)
    EditText editTrackerId;
    @BindView(R.id.billing_layout)
    LinearLayout billing_layout;
    @BindView(R.id.edit_plate_number)
    EditText editPlateNumber;
    @BindView(R.id.edit_card_cvcode)
    EditText editCardCVCode;
    @BindView(R.id.edit_card_name)
    EditText editCardName;
    @BindView(R.id.edit_driver_name)
    EditText editDriverName;
    @BindView(R.id.edit_card_number)
    EditText editCardNumber;
    @BindView(R.id.spinner_dataplan)
    Spinner spinnerDataPlan;
    @BindView(R.id.spinner_wifiplan)
    Spinner spinnerWifiPlan;
    @BindView(R.id.spinner_category)
    Spinner spinnerCategory;
    @BindView(R.id.edit_card_expiry)
    EditText editCardExpiry;
    @BindView(R.id.edit_last_digits)
    EditText editLastDigits;
    @BindView(R.id.comment)
    TextView txt_comment;
    @BindView(R.id.edit_street)
    TextView editStreet;
    @BindView(R.id.edit_city)
    TextView editCity;
    @BindView(R.id.edit_state)
    TextView editState;
    @BindView(R.id.lbl_billing_info)
    TextView label_billing_info;
    @BindView(R.id.lbl_last_digits)
    TextView label_last_digits;
    @BindView(R.id.layout_card_info)
    LinearLayout layout_card_info;
    @BindView(R.id.edit_zip_code)
    TextView editZipCode;
    @BindView(R.id.spinner_country)
    Spinner spinnerCountry;
    @BindView(R.id.spinner_autorenew)
    Spinner spinnerAutoRenew;
    @BindView(R.id.btn_info)
    Button btn_info;
    @BindView(R.id.billing_method)
    ToggleSwitch billing_method;
    @BindView(R.id.btn_activate)
    NeumorphButton btn_activate;

    boolean isFragmentAlive = false;

    Resp_Tracker selectedTracker = null;
    List<Map<String, List<String>>> planList = new ArrayList<>();
    JSONArray wifiPlanList = new JSONArray();
    String selectedPlan = "", selectedWifiPlan = "", selectedPlanKey = "";
    String _userId = "";
    String _email = "";
    Boolean isCardExist = false;
    String paymentType = "card";

    public ActivateTrackerFragment() {
        // Required empty public constructor
    }

    public static ActivateTrackerFragment newInstance() {
        ActivateTrackerFragment fragment = new ActivateTrackerFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_activate_tracker, container, false);

        ButterKnife.bind(this, rootView);

        isFragmentAlive = true;
        String text = getString(R.string.activation_description);
//        "<p>1. Put the <font color=\"#ff0000\">SPECTRUMID</font> into the box below.</p>" +
//                "<p>2. For OBD tracker, plug the tracker into the OBD II port. <a href=\"https://www.carmd.com/wp/locating-the-obd2-port-or-dlc-locator/\" color='#00ff00' style=\"text-decoration: none;\">CLICK HERE</a> to locate OBD port. For portable tracker, fully charge your device for 2-3 hours.</p>" +
//                "<p>3. Drive your car for 5-10 minutes to get GPS location. Portable one can take up to 10 minutes when it is used the first time.\n</p>"+
//                "<p>4. Demo video here <a href=\"https://spectrumtracking.com/video.html\" color='#00ff00' style=\"text-decoration: none;\">CLICK HERE</a> </p> ";

        txt_comment.setText(Html.fromHtml(text));
        txt_comment.setMovementMethod(LinkMovementMethod.getInstance());

        editTrackerId.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.equals("")) {
                    getPlan();
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });
        String[] str = {""};
        ArrayAdapter<String> adp2 = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, str);
        adp2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDataPlan.setAdapter(adp2);

        ArrayAdapter<String> adp6 = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, str);
        adp6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWifiPlan.setAdapter(adp6);

        String[] categories = {"SUV", "Crossover", "Sedan", "Truck", "Hatchback", "Convertible", "Bus", "Semi-truck", "Box-truck", "Cargo-Van"};
        ArrayAdapter<String> adp3 = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, categories);
        adp3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adp3);

        ArrayAdapter<String> adp4 = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, GlobalConstant.countries);
        adp4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCountry.setAdapter(adp4);

        String[] autoRenewStrings = {getString(R.string.auto_renew), getString(R.string.no_auto_renew)};
        ArrayAdapter<String> adp5 = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, autoRenewStrings);
        adp5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAutoRenew.setAdapter(adp5);

        btn_info.setVisibility(View.GONE);
        btn_info.setOnClickListener(v -> {
            if (planList.size() == 0) {
                return;
            }

            int index = spinnerDataPlan.getSelectedItemPosition();
            if (index >= planList.size()) {
                return;
            }

            Map<String, List<String>> plan = planList.get(index);
            List<String> planDetails = plan.get(selectedPlan);

            if (planDetails == null) {
                return;
            }

            String info_html = "";

            for (int i=1; i<planDetails.size(); i++) {
                String detail = String.format("%d. %s\n", i, planDetails.get(i));
                info_html = info_html + detail;
            }

            if (!info_html.isEmpty()) {
                Utils.showSweetAlert(ActivateTrackerFragment.this.getContext(), planDetails.get(0), info_html, null, null, SweetAlertDialog.WARNING_TYPE, null);
            }
        });
        doAuth();
        billing_method.setOnToggleSwitchChangeListener(new BaseToggleSwitch.OnToggleSwitchChangeListener() {
            @Override
            public void onToggleSwitchChangeListener(int position, boolean isChecked) {
                if(position==1 && isChecked){
                    billing_layout.setVisibility(View.VISIBLE);
                    label_last_digits.setVisibility(View.GONE);
                    editLastDigits.setVisibility(View.GONE);
                    paymentType = "card";
                }else{
                    if(!isCardExist){
                        Utils.showShortToast(ActivateTrackerFragment.this.getContext(), "Sorry, you don't have any card now.", true);
                        billing_method.setCheckedTogglePosition(1);
                        return;
                    }
                    billing_layout.setVisibility(View.GONE);
                    label_last_digits.setVisibility(View.VISIBLE);
                    editLastDigits.setVisibility(View.VISIBLE);
                    paymentType = "token";
                }
            }
        });
        return rootView;
    }
    private void doAuth() {
        ApiInterface apiInterface = ApiClient.getClient(ActivateTrackerFragment.this.getContext()).create(ApiInterface.class);
        apiInterface.doAuth().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                int code = response.code();
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setLongSerializationPolicy(LongSerializationPolicy.STRING);
                Gson gson = gsonBuilder.create();
                if (code == 200) {
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());
                        String message = "";
                        _userId = object.getString("userId");
                        _email = object.getString("email");
                        if (object.has("last4Digits") && object.has("exp_date")) {
                            String exp_date = object.getString("exp_date");
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            Date e_date = format.parse("20"+exp_date.substring(2)+"-"+exp_date.substring(0,2)+"-01");
//                            Log.d("last4digits:","20"+exp_date.substring(2)+"-"+exp_date.substring(0,2)+"-01");
//                            if(new Date().after(e_date)){
//                                Log.d("last4digits:",""+object.getString("last4Digits"));
//                                billing_layout.setVisibility(View.GONE);
//                                layout_card_info.setVisibility(View.GONE);
//                                label_billing_info.setVisibility(View.GONE);
//                                isCardExist = true;
//                            }else {
                                billing_layout.setVisibility(View.GONE);
                                label_last_digits.setVisibility(View.VISIBLE);
                                editLastDigits.setVisibility(View.VISIBLE);
                                editLastDigits.setText(object.getString("last4Digits"));
                                //editCardExpiry.setText(exp_date);
                                paymentType = "token";
//                            }
                            isCardExist = true;
                        }else {
                            isCardExist = false;
                            billing_method.setCheckedTogglePosition(1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.showShortToast(ActivateTrackerFragment.this.getContext(), "try again later 1", true);
                    }
                }
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentAlive = false;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle(getString(R.string.activate_tracker));

    }

    public void getPlan() {
        String trackerId = editTrackerId.getText().toString().trim();
        trackerId = trackerId.replaceAll("/(^\\s+|\\s+$)/", "").toUpperCase();

        String[] str = {""};
        ArrayAdapter<String> adp2 = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, str);
        adp2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDataPlan.setAdapter(adp2);

        ArrayAdapter<String> adp3 = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, str);
        adp3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWifiPlan.setAdapter(adp3);

        planList = new ArrayList<>();
        wifiPlanList = new JSONArray();

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        apiInterface.getTrackerModelBySpectrumId(trackerId).enqueue(new Callback<ResponseBody>() {
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

                    try {
                        String bodyString = responseBody.string();
                        JSONObject json = new JSONObject(bodyString);
                        if(json.has("userId")){
                            Utils.showShortToast(ActivateTrackerFragment.this.getContext(), "Device has already registered. Contact us for help", true);
                            return;
                        }
                        Type typeString = new TypeToken<List<HashMap<String, List<String>>>>() {}.getType();
                        planList = gson.fromJson(json.get("plansMobile").toString(), typeString);
                        wifiPlanList = json.getJSONArray("LTEPlan");
                        List<String> keys = new ArrayList<>();
                        final List<String> str = new ArrayList<>();
                        for(int i=0; i<planList.size(); i++) {
                            Map<String, List<String>> plan = planList.get(i);
                            if (plan.keySet().size() > 0) {
                                String key = plan.keySet().toArray()[0].toString();
                                List<String> values = plan.get(key);
                                keys.add(key);
                                str.add(values.get(0));
                            }
                        }

                        ArrayAdapter<String> adp2 = new ArrayAdapter<>(ActivateTrackerFragment.this.getContext(), android.R.layout.simple_spinner_item, str);
                        adp2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerDataPlan.setAdapter(adp2);
                        spinnerDataPlan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedPlan = str.get(position);
                                selectedPlanKey = keys.get(position);
                                Double wifi_amount = (selectedWifiPlan.split("\\$").length>1)?Double.valueOf(selectedWifiPlan.split("\\$")[1]):0.0;
                                Double amount = Double.valueOf(selectedPlan.split("\\$")[1]) + wifi_amount;
                                if(amount!=0){
                                    btn_activate.setText("ACTIVATE "+String.format("$%.2f", amount));
                                }else btn_activate.setText("ACTIVATE");
//                                Map<String, List<String>> plan = planList.get(position);
//                                if (plan.keySet().size() > 0) {
//                                    selectedPlan = plan.keySet().toArray()[0].toString();
//                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        if (str.size() > 0) {
                            btn_info.setVisibility(View.VISIBLE);
                        } else {
                            btn_info.setVisibility(View.GONE);
                        }

                        List<String> wifikeys = new ArrayList<>();
                        List<String> str1 = new ArrayList<>();
                        for(int i=0; i<wifiPlanList.length(); i++) {
                            JSONObject plan = wifiPlanList.getJSONObject(i);
                            String key = plan.keys().next();
                            wifikeys.add(plan.getString(key));
                            str1.add(plan.getString(key));
                        }

                        ArrayAdapter<String> adp3 = new ArrayAdapter<>(ActivateTrackerFragment.this.getContext(), android.R.layout.simple_spinner_item, str1);
                        adp3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerWifiPlan.setAdapter(adp3);
                        spinnerWifiPlan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedWifiPlan = wifikeys.get(position);
                                Double wifi_amount = (selectedWifiPlan.split("\\$").length>1)?Double.valueOf(selectedWifiPlan.split("\\$")[1]):0.0;
                                Double amount = Double.valueOf(selectedPlan.split("\\$")[1]) + wifi_amount;
                                if(amount!=0){
                                    btn_activate.setText("ACTIVATE "+String.format("$%.2f", amount));
                                }else btn_activate.setText("ACTIVATE");
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(ActivateTrackerFragment.this.getContext(), "response parse error");
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
                Utils.showShortToast(ActivateTrackerFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });

    }

    @OnClick(R.id.btn_activate)
    public void onActivateClick()  {
        if (!Utils.isNetworkConnected(this.getContext())) {
            return;
        }

        String trackerId = editTrackerId.getText().toString().trim();
        String plateNumber = editPlateNumber.getText().toString().trim();
        String cardName = editCardName.getText().toString().trim();
        String cardNumber = editCardNumber.getText().toString().trim();
        String driverName = editDriverName.getText().toString().trim();
        String cardExpiry = editCardExpiry.getText().toString().trim();
        String cardCVCode = editCardCVCode.getText().toString().trim();
        String street = editStreet.getText().toString().trim();
        String city = editCity.getText().toString().trim();
        String state = editState.getText().toString().trim();
        String zipCode = editZipCode.getText().toString().trim();
        String country = spinnerCountry.getSelectedItem().toString();

        if ("".equals(trackerId)) {
            Utils.showShortToast(this.getContext(), getString(R.string.please_enter_tracker_id), true);
            return;
        }

        if (selectedPlan.equals("")) {
            Utils.showShortToast(this.getContext(), getString(R.string.please_select_data_plan), true);
            return;
        }

        if (cardExpiry.length() != 4 && !isCardExist) {
            Utils.showShortToast(this.getContext(), getString(R.string.expiration_date_format_is_wrong), true);
            return;
        }

        trackerId = trackerId.replaceAll("/(^\\s+|\\s+$)/", "").toUpperCase();
        cardNumber = cardNumber.replaceAll("/\\s+/g", "");
        cardCVCode = cardCVCode.replaceAll("/\\s+/g", "");
        Double wifi_amount = (selectedWifiPlan.split("\\$").length>1)?Double.valueOf(selectedWifiPlan.split("\\$")[1]):0.0;
        Double amount = Double.valueOf(selectedPlan.split("\\$")[1]) + wifi_amount;
        String sendHtml = "Your tracking plan is " + selectedPlan + " Your wifi plan is " +  selectedWifiPlan + " Your total payment is $" + amount;
        String autoRenewString = spinnerAutoRenew.getSelectedItem().toString();
        Boolean isAutoRenew = true;

        if (autoRenewString.equals(getString(R.string.auto_renew))) {
            isAutoRenew = true;
        } else {
            isAutoRenew = false;
        }
        JSONArray paymentItems = new JSONArray();
        JSONObject obj;
        try {
            obj = new JSONObject();
            obj.put("renew", isAutoRenew);
            obj.put("textPlan", selectedPlan);
            obj.put("service", true);
            obj.put("trackerId", trackerId);
            paymentItems.put(obj);
            JSONObject obj1;
            obj1 = new JSONObject();
            obj1.put("renew", isAutoRenew);
            obj1.put("textPlan", selectedWifiPlan);
            obj1.put("service", true);
            obj1.put("trackerId", trackerId);
            paymentItems.put(obj1);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        final ActivateTrackerForm activateTrackerForm = new ActivateTrackerForm(trackerId, plateNumber, driverName, cardName, street, city, state, zipCode, country, cardNumber, cardExpiry, cardCVCode);
        HashMap<String, Object> body = new HashMap<>();
        if(paymentType == "token"){
            body.put("paymentType", "token");
            body.put("tractionType", "purchase");
            body.put("currency_code", "USD");
            body.put("productService", "service");
            body.put("email", _email);
            body.put("auth", _userId);
            body.put("items", paymentItems.toString());
            body.put("amount", amount);
            body.put("sendHtml", sendHtml);
            body.put("card_cvv", cardCVCode);
        }else {
            body.put("paymentType", "card");
            body.put("tractionType", "purchase");
            body.put("card_holder_name", cardName);
            body.put("card_holder_address", street);
            body.put("card_holder_city", city);
            body.put("card_holder_state", state);
            body.put("card_holder_zip", zipCode);
            body.put("card_holder_country", country);
            body.put("card_number", cardNumber);
            body.put("card_expiry", cardExpiry);
            body.put("productService", "service");
            body.put("email", _email);
            body.put("auth", _userId);
            body.put("items", paymentItems.toString());
            body.put("amount", amount);
            body.put("sendHtml", sendHtml);
            body.put("card_cvv", cardCVCode);
        }
        Utils.showProgress(this.getActivity());

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        final String finalTrackerId = trackerId;

        apiInterface.ordersPayment(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();

                int code = response.code();

                if (code == 200) {
                    // success
                    registerTracker(_userId, _email, activateTrackerForm);
                } else {
                    Utils.showShortToast(ActivateTrackerFragment.this.getContext(), "Declined Wrong expiration date or wrong cvv number", true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(ActivateTrackerFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    private void generateToken(final String userId, final String email, final ActivateTrackerForm activateTrackerForm) {
        if (!Utils.isNetworkConnected(this.getContext())) {
            return;
        }
        Utils.showProgress(this.getActivity());

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        HashMap<String, Object> body = new HashMap<>();

        body.put("card_holder_name", activateTrackerForm.cardName);
        body.put("card_holder_address", activateTrackerForm.street);
        body.put("card_holder_city", activateTrackerForm.city);
        body.put("card_holder_state", activateTrackerForm.state);
        body.put("card_holder_zip", activateTrackerForm.zipCode);
        body.put("card_holder_country", activateTrackerForm.country);
        body.put("auth", userId);
        body.put("card_number", activateTrackerForm.cardNumber);
        body.put("card_expiry", activateTrackerForm.cardExpiry);
        body.put("card_cvv", activateTrackerForm.cardCVCode);

        apiInterface.generateToken(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();

                int code = response.code();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        String jsonString = responseBody.string();
                        if (jsonString.isEmpty()) {
                            registerTracker(userId, email, activateTrackerForm);
                        } else {
                            object = new JSONObject(jsonString);
                            if (!object.has("error")) {
                                registerTracker(userId, email, activateTrackerForm);
                            } else {
                                Utils.showShortToast(ActivateTrackerFragment.this.getContext(), object.getString("error"), true);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.showShortToast(ActivateTrackerFragment.this.getContext(), "failed to register tracker", true);
                    }
                } else {
                    Utils.showShortToast(ActivateTrackerFragment.this.getContext(), "failed to register tracker", true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(ActivateTrackerFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    private void registerTracker(final String userId, String email, final ActivateTrackerForm activateTrackerForm) {

        if (!Utils.isNetworkConnected(this.getContext())) {
            return;
        }
        Utils.showProgress(this.getActivity());

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        HashMap<String, Object> body = new HashMap<>();
        String trackerPlan = selectedPlanKey;
        String category = spinnerCategory.getSelectedItem().toString();
        String autoRenewString = spinnerAutoRenew.getSelectedItem().toString();
        Boolean isAutoRenew = true;

        if (autoRenewString.equals(getString(R.string.auto_renew))) {
            isAutoRenew = true;
        } else {
            isAutoRenew = false;
        }

        body.put("spectrumId", activateTrackerForm.trackerId);
        body.put("userId", userId);
        body.put("email", email);
        body.put("plan", trackerPlan);
        body.put("category", category);
        body.put("autoRenew", isAutoRenew);

        apiInterface.trackerRegister(GlobalConstant.X_CSRF_TOKEN, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();

                int code = response.code();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    Gson gson = new Gson();
                    try {
                        String jsonString = responseBody.string();
                        object = new JSONObject(jsonString);
                        if (!object.has("error")) {
                            selectedTracker = gson.fromJson(jsonString, Resp_Tracker.class);

                            if (selectedTracker != null) {
                                modify(userId, selectedTracker.get_id(), category, activateTrackerForm);
                            }
                        } else {
                            Utils.showShortToast(ActivateTrackerFragment.this.getContext(), object.getString("error"), true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.showShortToast(ActivateTrackerFragment.this.getContext(), "asset creation failed", true);
                    }
                } else {
                    //Utils.showShortToast(ActivateTrackerFragment.this.getContext(), "failed to register tracker");
                    ResponseBody responseBody = response.errorBody();
                    if (responseBody == null) return;
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());
                        String message = object.getString("message");
                        Utils.showSweetAlert(getContext(),"Alert",message,"Ok", null, SweetAlertDialog.ERROR_TYPE, null);
//                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                        builder.setMessage(message)
//                                .setTitle("Alert");
//                        builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss());
//                        AlertDialog dialog = builder.create();
//                        dialog.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Utils.showShortToast(ActivateTrackerFragment.this.getContext(), "asset creation failed", true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Utils.showShortToast(ActivateTrackerFragment.this.getContext(), "asset creation failed", true);
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(ActivateTrackerFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    private void modify(final String userId, final String trackerId, String category, final ActivateTrackerForm activateTrackerForm) {
        if (!Utils.isNetworkConnected(this.getContext())) {
            return;
        }
        Utils.showProgress(this.getActivity());

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        HashMap<String, Object> body = new HashMap<>();

        body.put("id", trackerId);
        body.put("driverName", activateTrackerForm.driverName);
        body.put("category", category);
        body.put("operation", "createNew");

        apiInterface.modify(GlobalConstant.X_CSRF_TOKEN,body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();

                int code = response.code();

                if (code == 200) {
                    doFinialRegisterTrackerWork(userId, trackerId, activateTrackerForm);
                } else {
                    Utils.showShortToast(ActivateTrackerFragment.this.getContext(), "failed to register tracker", true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(ActivateTrackerFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    private void doFinialRegisterTrackerWork(String userId, String trackerId, ActivateTrackerForm activateTrackerForm) {

        Utils.showProgress(this.getActivity());

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        HashMap<String, Object> body = new HashMap<>();

        String plateNumber = activateTrackerForm.plateNumber;
        String driverName = activateTrackerForm.driverName;


        if (plateNumber.equals("")) {
            plateNumber = activateTrackerForm.trackerId;
        }
        if (driverName.equals("")) {
            driverName = activateTrackerForm.trackerId;
        }

        body.put("name", plateNumber);
        body.put("trackerId", trackerId);
        body.put("userId", userId);
        body.put("driverName", driverName);
        body.put("spectrumId", activateTrackerForm.trackerId);

        apiInterface.createAssets(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();

                int code = response.code();

                if (code == 201) {
                    // success
//                    if (selectedTracker != null && selectedTracker.getHotspot() == 1) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(ActivateTrackerFragment.this.getContext());
                        alert.setTitle(getString(R.string.alert));
                        alert.setMessage(Html.fromHtml(getString(R.string.dialog_activation_success_message_1)));
                        alert.setPositiveButton("OK", (dialog, which) -> {
                            dialog.dismiss();
                            MainActivity mainActivity = (MainActivity) ActivateTrackerFragment.this.getActivity();
                            mainActivity.showMonitorFragment();
                        });
                        AlertDialog dialog = alert.create();
                        dialog.show();
//                    } else {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                        builder.setMessage(R.string.dialog_activate_success_message).setTitle(getString(R.string.alert));
//                        AlertDialog dialog = builder.create();
//                        dialog.show();
//                    }

//                    MainActivity mainActivity = (MainActivity) ActivateTrackerFragment.this.getActivity();
//                    mainActivity.showMonitorFragment();
                } else {
                    ResponseBody responseBody = response.errorBody();
                    if (responseBody == null) return;
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());
                        String message = object.getString("message");

                        Utils.showSweetAlert(ActivateTrackerFragment.this.getContext(), "Alert", message, "Ok", null, SweetAlertDialog.ERROR_TYPE, null);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(ActivateTrackerFragment.this.getContext(), "asset creation failed");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(ActivateTrackerFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    class ActivateTrackerForm {
        String trackerId;
        String plateNumber;
        String cardName;
        String cardNumber;
        String cardCVCode;
        String driverName;
        String cardExpiry;
        String street;
        String city;
        String state;
        String zipCode;
        String country;

        public ActivateTrackerForm(String trackerId, String plateNumber, String driverName, String cardName, String cardNumber, String cardExpiry, String cardCVCode) {
            this.trackerId = trackerId;
            this.plateNumber = plateNumber;
            this.cardName = cardName;
            this.cardNumber = cardNumber;
            this.driverName = driverName;
            this.cardExpiry = cardExpiry;
            this.cardCVCode = cardCVCode;
            this.street = "";
            this.city = "";
            this.state = "";
            this.zipCode = "";
            this.country = "";
        }

        public ActivateTrackerForm(String trackerId, String plateNumber, String driverName, String cardName, String street, String city, String state, String zipCode, String country, String cardNumber, String cardExpiry, String cardCVCode) {
            this.trackerId = trackerId;
            this.plateNumber = plateNumber;
            this.cardName = cardName;
            this.cardNumber = cardNumber;
            this.driverName = driverName;
            this.cardExpiry = cardExpiry;
            this.cardCVCode = cardCVCode;
            this.street = street;
            this.city = city;
            this.state = state;
            this.zipCode = zipCode;
            this.country = country;
        }
    }
}
