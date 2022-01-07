package com.jo.spectrumtracking.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.adapter.OrderSummaryRecyclerViewAdapter;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.OrderService;
import com.jo.spectrumtracking.model.OrderSummary;
import com.jo.spectrumtracking.model.OrderTracker;
import com.jo.spectrumtracking.model.Resp_Error;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    Toolbar toolbar;

    @BindView(R.id.sp_checkout_country)
    Spinner spCheckoutCountries;

    @BindView(R.id.edit_state)
    EditText editState;

    @BindView(R.id.edit_name_on_card)
    EditText editNameOnCard;

    @BindView(R.id.edit_street_address)
    EditText editStreetAddress;

    @BindView(R.id.edit_city)
    EditText editCity;

    @BindView(R.id.edit_zip)
    EditText editZip;

    @BindView(R.id.edit_card_number)
    EditText editCardNumber;

    @BindView(R.id.edit_expiration_date)
    EditText editExpirationDate;

    @BindView(R.id.edit_cv_code)
    EditText editCVCode;

    public String userId; // get after auth api called

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_checkout);

        ButterKnife.bind(this);

        setToolbar();

        setStateSpinner();

        orderSummaryClicked();

    }

    private void setStateSpinner() {

        String[] states = {
                "Alabama",
                "Alaska",
                "Arizona",
                "Arkansas",
                "California",
                "Colorado",
                "Connecticut",
                "Delaware",
                "District Of Columbia",
                "Florida",
                "Georgia",
                "Hawaii",
                "Idaho",
                "Illinois",
                "Indiana",
                "Iowa",
                "Kansas",
                "Kentucky",
                "Louisiana",
                "Maine",
                "Maryland",
                "Massachusetts",
                "Michigan",
                "Minnesota",
                "Mississippi",
                "Missouri",
                "Montana",
                "Nebraska",
                "Nevada",
                "New Hampshire",
                "New Jersey",
                "New Mexico",
                "New York",
                "North Carolina",
                "North Dakota",
                "Ohio",
                "Oklahoma",
                "Oregon",
                "Pennsylvania",
                "Rhode Island",
                "South Carolina",
                "South Dakota",
                "Tennessee",
                "Texas",
                "Utah",
                "Vermont",
                "Virginia",
                "Washington",
                "West Virginia",
                "Wisconsin",
                "Wyoming"
        };

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Arrays.asList(GlobalConstant.countries));
        spCheckoutCountries.setAdapter(arrayAdapter);
        spCheckoutCountries.setSelection(0);
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    /*  @OnClick(R.id.txt_privacy_policy)
      public void onPrivacyPolicyClicked() {
          final AlertDialog.Builder alertDlgBuilder = new AlertDialog.Builder(this);
          alertDlgBuilder.setTitle("Privacy Policy")
                  .setMessage("We will never sell your personal information and we use secure transmission to protect your personal information.")
                  .setCancelable(false)
                  .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                          dialog.dismiss();
                      }
                  });
          alertDlgBuilder.show();
      }
  */
    // @OnClick(R.id.txt_order_summary)
    public void orderSummaryClicked() {
        Intent intent = getIntent();

        String from = intent.getExtras().getString("from", "");
        List<OrderSummary> orderSummaryList = new ArrayList<OrderSummary>();


        if ("OrderServiceFragment".equals(from)) {
            // this is from order service fragment
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

            List<OrderService> itemList = GlobalConstant.orderServiceItemList;
            double sum1 = 0, sum2 = 0;
            int index = 0;
            for (OrderService item : itemList) {
                index++;
                String tracker = item.getName();

                String dataPlan = "$" + item.getServicePlanList().get(item.getSelectedServicePlanId()).getPrice();
                String LTEData = "$" + item.getLteDataList().get(item.getSelectedLTEDataId()).getPrice();
                Date dateTd = null;
                try {
                    dateTd = formatter.parse(item.getExpirationDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                //TODO Fix Deprecated dateTD!
                if (dateTd.before(new Date())) dateTd = new Date();
                if (item.getServicePlanList().get(item.getSelectedServicePlanId()).getServicePlan().contains("No Service") || item.getServicePlanList().get(item.getSelectedServicePlanId()).getServicePlan().equals("")) {
                    dateTd.setMonth(dateTd.getMonth());
                } else if (item.getServicePlanList().get(item.getSelectedServicePlanId()).getServicePlan().contains("Annual")) {
                    dateTd.setYear(dateTd.getYear() + 1);
                    //dateTd.setMonth(dateTd.getMonth()+1);
                } else {
                    dateTd.setMonth(dateTd.getMonth() + 1);
                }

                boolean autoRenew = item.getAutoReview();
                OrderSummary orderSummary = new OrderSummary();
                orderSummary.setVehicle("Vehicle #" + index);
                orderSummary.setTracker(tracker);
                orderSummary.setDateTd(formatter.format(dateTd));
                orderSummary.setDataPlan(dataPlan);
                orderSummary.setLTEData(LTEData);
                orderSummary.setAutoRenew(String.valueOf(autoRenew));
                orderSummaryList.add(orderSummary);

                sum1 += item.getServicePlanList().get(item.getSelectedServicePlanId()).getPrice();
                sum2 += item.getLteDataList().get(item.getSelectedLTEDataId()).getPrice();

            }

            double amount = sum1 + sum2;

            final Dialog dialog = new Dialog(this, R.style.Dialog);
            dialog.setContentView(R.layout.dialog_order_summary);
            dialog.setTitle(getResources().getString(R.string.order_summary));
            dialog.setCancelable(true);

            final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            final Date current = new Date();

            RecyclerView orderSummaryRecyclerView = dialog.findViewById(R.id.orderSummaryRecyclerView);
            OrderSummaryRecyclerViewAdapter adapter = new OrderSummaryRecyclerViewAdapter(this, orderSummaryList, R.layout.recyclerview_order_summary_row_layout);
            orderSummaryRecyclerView.setAdapter(adapter);
            orderSummaryRecyclerView.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
            orderSummaryRecyclerView.setItemAnimator(new DefaultItemAnimator());

            ((TextView) dialog.findViewById(R.id.txt_dlg_order_summary_service_plan)).setText(String.format("$%.2f", sum1));
            ((TextView) dialog.findViewById(R.id.txt_dlg_order_summary_lte_plan)).setText(String.format("$%.2f", sum2));
            ((TextView) dialog.findViewById(R.id.txt_dlg_order_summary_tax)).setText("$0.00");
            ((TextView) dialog.findViewById(R.id.txt_dlg_order_summary_total)).setText(String.format("$%.2f", amount));
            ((TextView) dialog.findViewById(R.id.txt_dlg_order_summary_time)).setText(sdf.format(current));

            Button btnOk = dialog.findViewById(R.id.txt_dlg_order_summary_ok);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        } else if ("ShippingAddressActivity".equals(from)) {
            // this is order tracker


        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_place_your_order)
    public void onPlaceYourOrderClicked() {

        if (!checkEntries()) {
            return;
        }


        Intent intent = getIntent();
        String from = intent.getExtras().getString("from", "");
        if ("OrderServiceFragment".equals(from)) {
            // this is from order service fragment
            doAuth(new Runnable() {
                @Override
                public void run() {
                    doWorkForOrderService();
                }
            });
        } else if ("ShippingAddressActivity".equals(from)) {
            // this is order tracker
            doAuth(new Runnable() {
                @Override
                public void run() {
                    doWorkForOrderTracker();
                }
            });

        }
    }

    private boolean checkEntries() {
        String card = editNameOnCard.getText().toString().trim();
         String streetAddress = editStreetAddress.getText().toString().trim();
         String city = editCity.getText().toString().trim();
        String zipCode = editZip.getText().toString().trim();
        String state = editState.getText().toString().trim();
        String country = spCheckoutCountries.getSelectedItem().toString().trim();
        String cardNumber = editCardNumber.getText().toString().trim();
        String expirationDate = editCardNumber.getText().toString().trim().replace("/", "");
        String cvCode = editCVCode.getText().toString().trim();


        if ("".equals(card)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_card_name), true);
            return false;
        }
        if ("".equals(cardNumber)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_card_number), true);
            return false;
        }
      /*  if ("".equals(streetAddress)) {
            Utils.showShortToast(this, "Please enter street address");
            return false;
        }*/
       /* if ("".equals(city)) {
            Utils.showShortToast(this, "Please enter city");
            return false;
        }*/

        if ("".equals(state)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_state), true);
            return false;
        }
        if ("".equals(zipCode)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_zip_code), true);
            return false;
        }

        if ("".equals(expirationDate)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_expiration_date), true);
            return false;
        }
        if ("".equals(cvCode)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_cv_code), true);
            return false;
        }
        return true;
    }

    private void doWorkForOrderTracker() {

        List<OrderTracker> itemList = GlobalConstant.orderTrackerList;

        JSONArray paymentItems = new JSONArray();

        try {
            for (OrderTracker item : itemList) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        double amount = 0;
        double subTotal = 0;
        for (OrderTracker tracker : itemList) {
            subTotal += tracker.getPrice() * tracker.getCount();
        }

        double upTax = 1;
        double downTax = 0;
        double shipping;
        double defaultShipping = 0;
        shipping = (subTotal > 0 && subTotal < (100 / upTax)) ? defaultShipping : 0;

        amount = subTotal * upTax + shipping;


        // Utils.showProgress(this);

        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();

        body.put("tractionType", "purchase");
        body.put("card_holder_name", editNameOnCard.getText().toString());
        body.put("card_holder_address", editStreetAddress.getText().toString().trim());
        body.put("card_holder_city", editCity.getText().toString().trim());
        body.put("card_holder_state", editState.getText().toString().trim());
        body.put("card_holder_zip", editZip.getText().toString());
        body.put("card_holder_country", spCheckoutCountries.getSelectedItem().toString().trim());

        String cardNumber = editCardNumber.getText().toString().trim();
        cardNumber = cardNumber.replaceAll("/\\s+/g", "");

        body.put("card_number", cardNumber);
        body.put("card_expiry", editExpirationDate.getText().toString().replace("/", ""));
        body.put("card_cvv", editCVCode.getText().toString());
        body.put("currency_code", "USD");
        body.put("amount", amount);
        body.put("items", paymentItems.toString());
        body.put("auth", userId);
        body.put("productService", "card");
        body.put("sendHtml", make_tracker_service_html(itemList));


        apiInterface.ordersPayment(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();

                int code = response.code();
                Gson gson = new Gson();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());
                        /*String err = object.getString("error");
                        if (!"".equals(err)) {
                            Utils.showShortToast(CheckoutActivity.this, err);
                        } else {
                            Utils.showShortToast(CheckoutActivity.this, "Your have successfully made payment. The confirmation has been sent to your email");
                        }*/
                        String err = object.getString("success");
                        if (!Boolean.parseBoolean(err)) {
                            Utils.showShortToast(CheckoutActivity.this, err, true);
                            //CheckoutActivity.this.finish();
                            return;
                        } else {
                            Utils.showShortToast(CheckoutActivity.this, getResources().getString(R.string.you_have_successfully_made_paymet), false);
                            gotoMonitor();
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(CheckoutActivity.this, "response parse error");
                        //CheckoutActivity.this.finish();
                        return;
                    }
                    //CheckoutActivity.this.finish();
                    return;
                } else {

                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    Utils.hideProgress();
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(CheckoutActivity.this, error.getMessage(), true);
                        return;
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(CheckoutActivity.this, "response parse error");
                        //CheckoutActivity.this.finish();
                        return;
                    }

                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(CheckoutActivity.this, getResources().getString(R.string.weak_cell_signal), true);
            }
        });


    }

    private Object make_tracker_service_html(List<OrderTracker> itemList) {
        String sendHtml = "";
        String customer_name = GlobalConstant.shippingAddress.getName();
        String card_number = editCardNumber.getText().toString();
        String card_type = "false";
        if (card_number.equals("3")) card_type = "American Express";
        if (card_number.equals("4")) card_type = "Visa";
        if (card_number.equals("5")) card_type = "Mastercard";
        if (card_number.equals('6')) card_type = "Discover";
        sendHtml = "<p style='text-align:center; font-size:20px; color:blue;'>Order Summary</p>";
        sendHtml += " <br>  ";
        sendHtml += "<h3>Dear " + customer_name + ", thank-you for ordering from us!  Here is a summary of your purchase order. </h3>";
        sendHtml += "<table class='order-summay' cellspacing='10' width='390'>";
        sendHtml += "<tbody>";
        sendHtml += "<tr>";

        sendHtml += "<td>Email: </td><td class='order-summay-right-text'>" + GlobalConstant.shippingAddress.getEmail() + "</td>";
        sendHtml += "</tr>";
        sendHtml += "<tr>";
        sendHtml += "<td>Name: </td><td class='order-summay-right-text'>" + GlobalConstant.shippingAddress.getName() + "</td>";
        sendHtml += "</tr>";
        sendHtml += "<tr>";
        sendHtml += "<td>Card type: </td><td class='order-summay-right-text'>" + card_type + "</td>";
        sendHtml += "</tr>";
        sendHtml += "<tr>";

        sendHtml += "<td>Card last 4 digits: </td><td class='order-summay-right-text'>" + card_number.substring(card_number.length() - 4) + "</td>";

        sendHtml += "</tr>";
        sendHtml += "<tr>";

        sendHtml += "<td>Shipping Address: </td><td class='order-summay-right-text'>" + GlobalConstant.shippingAddress.getStreetAddress() + "</td>";
        sendHtml += "</tr>";
        sendHtml += "<tr>";
        sendHtml += "<td> </td><td class='order-summay-right-text'>" + GlobalConstant.shippingAddress.getStreetAddress() + "</td>";
        sendHtml += "</tr>";
        sendHtml += "<tr>";
        sendHtml += "<td> </td><td class='order-summay-right-text'>" + GlobalConstant.shippingAddress.getStreetAddress() + "  " + GlobalConstant.shippingAddress.getState() + "  " + GlobalConstant.shippingAddress.getZipCode() + "</td>";
        sendHtml += "</tr>";


        double subTotal = 0;

        for (OrderTracker item : itemList) {

            String productName = item.getName();
            int unit = item.getCount();
            sendHtml += "<tr>";
            sendHtml += "<td>Product:</td><td class='order-summay-right-text'> " + productName + "</td>";
            sendHtml += "</tr>";
            sendHtml += "<tr>";
            sendHtml += "<td>Unit : </td><td class='order-summay-right-text'> " + unit + "</td>";
            sendHtml += "</tr>";
            subTotal += item.getCount() * item.getPrice();
        }

//        Spinner stateSpinner = findViewById(R.id.sp_checkout_states);
//
//        String state = stateSpinner.getSelectedItem().toString();

        String state = editState.getText().toString().trim();
        sendHtml += "<tr>";

        double upTax = 1.00;
        double downTax = 0.00;

        if (state == "Kentucky") {
            upTax = 1.06;
            downTax = 0.06;
        } else {
            upTax = 1.00;
            downTax = 0.00;
        }

        //subTotal=parseFloat($("#subtotalCtr").find(".cart-totals-value").html().replace(/[^0-9\.-]+/g,""));
        float taxPrice = (float) (subTotal * downTax);
        float totalPrice = (float) (subTotal * upTax);

        sendHtml += "</tr>";
        sendHtml += "<tr>";
        sendHtml += "<td>Products:</td><td class='order-summay-right-text'>" + "$" + subTotal + "</td>";
        sendHtml += "</tr>";
        sendHtml += "<tr>";
        sendHtml += "<td>Shipping: </td><td class='order-summay-right-text'>" + "$" + totalPrice + "</td>";
        sendHtml += "</tr>";
        sendHtml += "<tr>";
        sendHtml += "<td>Tax: </td><td class='order-summay-right-text'>" + "$" + taxPrice + "</td>";
        sendHtml += "</tr>";
        sendHtml += "<tr>";

        sendHtml += "<tr>";
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        //String date=now.getYear()+"-"+now.getMonth()+"-"+now.getDate()+" " +now.toLocaleTimeString();
        sendHtml += "<td>Time: </td><td class='order-summay-right-text'>" + dateFormat.format(now) + "</td>";
        sendHtml += "</tr>";
        sendHtml += "</tbody>";
        sendHtml += "</table>";

        return sendHtml;
    }

    private void doAuth(final Runnable r) {
        Utils.showProgress(this);

        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);

        apiInterface.doAuth().enqueue(new Callback<ResponseBody>() {


            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                Utils.hideProgress();

                int code = response.code();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());

                        CheckoutActivity.this.userId = object.getString("userId");

                        r.run();

                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(CheckoutActivity.this, "response parse error");
                        Utils.hideProgress();
                        //CheckoutActivity.this.finish();
                        return;

                    }
                } else {

                    Utils.showShortToast(CheckoutActivity.this, "failed to get user Id", true);

                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                Utils.hideProgress();
                Utils.showShortToast(CheckoutActivity.this, getResources().getString(R.string.weak_cell_signal), true);
            }
        });

    }


    private void doWorkForOrderService() {

        List<OrderService> itemList = GlobalConstant.orderServiceItemList;

        JSONArray paymentItems = new JSONArray();

        try {
            for (OrderService item : itemList) {

                if (item.getServicePlanList().size() != 1) {
                    String servicePlan = item.getServicePlanList().get(item.getSelectedServicePlanId()).getServicePlan();


                    JSONObject obj;
                    obj = new JSONObject();
                    obj.put("renew", item.getAutoReview() ? "Yes" : "No");

                    String textPlan = item.getLteDataList().get(item.getSelectedLTEDataId()).getLteData();

                    obj.put("textPlan", textPlan);
                    obj.put("service", true);
                    obj.put("trackerId", item.getTrackerId());

                    paymentItems.put(obj);

                    obj = new JSONObject();
                    obj.put("renew", item.getAutoReview() ? "Yes" : "No");
                    obj.put("service", true);
                    obj.put("trackPlan", servicePlan);
                    obj.put("trackerId", item.getTrackerId());

                    paymentItems.put(obj);

                } else {

                    JSONObject obj;
                    obj = new JSONObject();
                    obj.put("service", true);
                    obj.put("renew", item.getAutoReview() ? "Yes" : "No");
                    String textPlan = item.getLteDataList().get(item.getSelectedLTEDataId()).getLteData();

                    obj.put("textPlan", textPlan);
                    obj.put("trackerId", item.getTrackerId());
                    paymentItems.put(obj);

                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        double amount = 0;

        double servicePlanListSum, lteDataListSum;

        servicePlanListSum = 0;
        lteDataListSum = 0;

        for (OrderService item : itemList) {
            servicePlanListSum += item.getServicePlanList().get(item.getSelectedServicePlanId()).getPrice();
            lteDataListSum += item.getLteDataList().get(item.getSelectedLTEDataId()).getPrice();
        }

        amount = servicePlanListSum + lteDataListSum;


        Utils.showProgress(this);

        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();

        body.put("tractionType", "purchase");
        body.put("card_holder_name", editNameOnCard.getText().toString());
        body.put("card_holder_address", editStreetAddress.getText().toString());
        body.put("card_holder_city", editCity.getText().toString());
        body.put("card_holder_zip", editZip.getText().toString());
        body.put("card_holder_state", editState.getText().toString());
        body.put("card_holder_country", spCheckoutCountries.getSelectedItem().toString());

        String cardNumber = editCardNumber.getText().toString().trim();
        cardNumber = cardNumber.replaceAll("/\\s+/g", "");
        body.put("card_number", cardNumber);

        String card_expiry = editExpirationDate.getText().toString().replace("/", "");

        if (card_expiry.length() != 4) {
            Utils.showSweetAlert(this, "Warning", "expiration date format is wrong. Use mmyy. For example, if expiration date is December 2030, use 1230", null, null, SweetAlertDialog.WARNING_TYPE, null);
            return;
        }

        body.put("card_expiry", card_expiry);
        body.put("card_cvv", editCVCode.getText().toString().replace(" ", ""));
        body.put("currency_code", "USD");
        body.put("amount", amount);
        body.put("items", paymentItems.toString());
        body.put("auth", userId);
        body.put("productService", "service");
        // body.put("sendHtml", "Yor order was successfully");
        body.put("sendHtml", make_order_sumary_html(itemList, servicePlanListSum, lteDataListSum, amount));

        apiInterface.ordersPayment(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();

                int code = response.code();
                Gson gson = new Gson();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());

                        if (object.has("success")) {
                            String err = object.getString("success");
                            if (!Boolean.parseBoolean(err)) {
                                Utils.showShortToast(CheckoutActivity.this, err, true);
                            } else {
                                Utils.showShortToast(CheckoutActivity.this, getResources().getString(R.string.you_have_successfully_made_paymet), false);
                                gotoMonitor();
                            }
                        } else {
                            String errMsg = object.getString("error");
                            Utils.showShortToast(CheckoutActivity.this, errMsg, true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(CheckoutActivity.this, "response parse error");
                    }
                    //CheckoutActivity.this.finish();
                    return;
                } else {

                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;

                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(CheckoutActivity.this, error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(CheckoutActivity.this, "response parse error");
                    }

                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(CheckoutActivity.this, getResources().getString(R.string.weak_cell_signal), true);
            }
        });


    }

    private String make_order_sumary_html(List<OrderService> itemList, double servicePlanListSum, double lteDataListSum, double amount) {

        String sendHtml = "";

        //String customer_name = $("#shipping_name").val();
        String card_number = editCardNumber.getText().toString();
        String card_type = "false";
        if (card_number.equals("3")) card_type = "American Express";
        if (card_number.equals("4")) card_type = "Visa";
        if (card_number.equals("5")) card_type = "Mastercard";
        if (card_number.equals('6')) card_type = "Discover";


        sendHtml += "<H3>Here is a summary of your purchase order. Thank you for your business.</H3>";
        sendHtml += "<br>";
        sendHtml += "<replace_data>";

        sendHtml += "<table class='order-summay' cellspacing='10' width='390'>";
        sendHtml += "<tbody>";

        //var products = $(".shopping-cart--list-item");

        sendHtml += "<tr>";

        float upTax = 1f;
        float downTax = 0f;


        //var tableElem = document.getElementById('payment_table');
        //console.log(tableElem);


      /*  var rowLen = tableElem.rows.length;
        //console.log(rowLen);
        var lastRow = tableElem.rows[rowLen - 1];

        //console.log(lastRow);
        var servicePrice = parseFloat(lastRow.cells[2].innerHTML).to_$();
        var LTEDataPrice = parseFloat(lastRow.cells[3].innerHTML).to_$();
        var subTotal=parseFloat(lastRow.cells[4].innerHTML);
        var taxPrice = (subTotal*downTax).to_$();
        var totalSum = (subTotal*upTax).to_$();*/


        float taxPrice = (float) (amount * downTax);
        float totalSum = (float) (amount * upTax);

        sendHtml += "</tr>";
        sendHtml += "<tr>";
        sendHtml += "<td>Service Plan:</td><td class='order-summay-right-text'>" + servicePlanListSum + "</td>";
        sendHtml += "</tr>";
        sendHtml += "<tr>";
        //sendHtml+='<td>LTE Plan: </td><td class="order-summay-right-text">'+ $("#shippingCtr").find(".cart-totals-value").html()+'</td>';
        sendHtml += "<td>LTE Plan: </td><td class='order-summay-right-text'>" + lteDataListSum + "</td>";
        sendHtml += "</tr>";
        sendHtml += "<tr>";
        //sendHtml+='<td>Tax: </td><td class="order-summay-right-text">'+taxPrice+'</td>';
        sendHtml += "<td>Tax: </td><td class='order-summay-right-text'>" + "$" + taxPrice + "</td>";
        sendHtml += "</tr>";
        sendHtml += "<tr>";
        sendHtml += "<td>Total: </td><td class='order-summay-right-text'>" + "$" + totalSum + "</td>";
        sendHtml += "</tr>";
        sendHtml += "<tr>";
        Date now = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //String date=now.getYear()+"-"+now.getMonth()+"-"+now.getDate()+" " +now.toLocaleTimeString();
        sendHtml += "<td>Time: </td><td class='order-summay-right-text'>" + dateFormat.format(now) + "</td>";

        sendHtml += "</tr>";
        sendHtml += "</tbody>";
        sendHtml += "</table>";
        return sendHtml;

    }

    private void gotoMonitor() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
