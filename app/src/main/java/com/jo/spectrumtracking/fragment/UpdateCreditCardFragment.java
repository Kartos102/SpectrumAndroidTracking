package com.jo.spectrumtracking.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.Utils;

import org.json.JSONObject;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateCreditCardFragment extends Fragment {


    boolean isFragmentAlive = false;


    @BindView(R.id.edit_name_on_card)
    EditText editNameOnCard;

    @BindView(R.id.edit_card_number)
    EditText editCardNumber;

    @BindView(R.id.edit_expiration_date)
    EditText editExpirationDate;

    @BindView(R.id.edit_cv_code)
    EditText editCVCode;


    public UpdateCreditCardFragment() {
        // Required empty public constructor
    }

    public static UpdateCreditCardFragment newInstance() {
        UpdateCreditCardFragment fragment = new UpdateCreditCardFragment();

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
        View rootView = inflater.inflate(R.layout.fragment_update_credit_card, container, false);

        ButterKnife.bind(this, rootView);

        isFragmentAlive = true;

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

        getActivity().setTitle("Update Credit Card");
    }


    @OnClick(R.id.btn_update_credit_card)
    public void onUpdateCreditCardClick() {
        if (!Utils.isNetworkConnected(this.getContext())) {
            return;
        }

        String name = editNameOnCard.getText().toString().trim();
        String cardNumber = editCardNumber.getText().toString().trim();
        String expDate = editExpirationDate.getText().toString().trim();
        String cvCode = editCVCode.getText().toString().trim();

        expDate = expDate.replace("/", "_");
        cardNumber = cardNumber.replace(" ", "");

        if ("".equals(name)) {
            Utils.showShortToast(this.getContext(), getString(R.string.please_enter_name), true);
            return;
        }
        if ("".equals(cardNumber)) {
            Utils.showShortToast(this.getContext(), getString(R.string.please_enter_card_number), true);
            return;
        }
        if ("".equals(expDate)) {
            Utils.showShortToast(this.getContext(), getString(R.string.please_enter_expiration_date), true);
            return;
        }
        if ("".equals(cvCode)) {
            Utils.showShortToast(this.getContext(), getString(R.string.please_enter_cv_code), true);
            return;
        }


        //tokenizeCreditCard

        Utils.showProgress(this.getContext());

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();

        body.put("tractionType", "authorize");
        body.put("card_holder_name", name);
        body.put("card_number", cardNumber);
        body.put("card_expiry", expDate);
        body.put("card_cvv", cvCode);

        apiInterface.tokenizeCreditCard(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (!isFragmentAlive) {
                    return;
                }

                Utils.hideProgress();

                int code = response.code();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());
                        String transaction_status = object.getString("transaction_status");
                        if ("approved".equals(transaction_status)) {

                            Utils.showShortToast(UpdateCreditCardFragment.this.getContext(), "card approved", false);

                            String tokenType = object.getJSONObject("token").getString("token_type");
                            String tokenNumber = object.getJSONObject("token").getJSONObject("token_data").getString("token_data");
                            String cardholderName = object.getJSONObject("card").getString("cardholder_name");
                            String cardType = object.getJSONObject("card").getString("type");
                            String expDate = object.getJSONObject("card").getString("exp_date");

                            updateCreditCardInfo(new UpdateCreditCardFragment.CreditTokenInfo(tokenType, tokenNumber, cardholderName, cardType, expDate));

                        } else {
                            Utils.showShortToast(UpdateCreditCardFragment.this.getContext(), getString(R.string.retry_your_credit_card_if_still_not_working__), true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(UpdateCreditCardFragment.this.getContext(), "response parse error");

                    }
                } else {
//                    Utils.showShortToast(UpdateCreditCardFragment.this.getContext(), getString(R.string.weak_cell_signal));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                if (!isFragmentAlive) {
                    return;
                }

                Utils.hideProgress();
                Utils.showShortToast(UpdateCreditCardFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });


    }

    class CreditTokenInfo {
        String token_type;
        String token_number;
        String cardholder_name;
        String card_type;
        String exp_date;

        public CreditTokenInfo(String token_type, String token_number, String cardholder_name, String card_type, String exp_date) {
            this.token_type = token_type;
            this.token_number = token_number;
            this.cardholder_name = cardholder_name;
            this.card_type = card_type;
            this.exp_date = exp_date;
        }
    }

    private void updateCreditCardInfo(final UpdateCreditCardFragment.CreditTokenInfo creditTokenInfo) {

        if (!isFragmentAlive) {
            return;
        }
        if (!Utils.isNetworkConnected(this.getContext())) {
            return;
        }

        Utils.showProgress(this.getContext());

        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        apiInterface.doAuth().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                if (!isFragmentAlive) {
                    return;
                }

                Utils.hideProgress();

                int code = response.code();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        object = new JSONObject(responseBody.string());
                        String userId = object.getString("userId");
                        updateCreditCardInfoSecondary(userId, creditTokenInfo);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(UpdateCreditCardFragment.this.getContext(), "response parse error");

                    }
                } else {

                    Utils.showShortToast(UpdateCreditCardFragment.this.getContext(), "failed to get user Id", true);

                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                if (!isFragmentAlive) {
                    return;
                }

                Utils.hideProgress();
                Utils.showShortToast(UpdateCreditCardFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });


    }

    private void updateCreditCardInfoSecondary(String userId, UpdateCreditCardFragment.CreditTokenInfo creditTokenInfo) {

        if (!isFragmentAlive) {
            return;
        }
        if (!Utils.isNetworkConnected(this.getContext())) {
            return;
        }
        Utils.showProgress(this.getContext());


        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);

        apiInterface.updateCreditCardInfoSecondary(
                userId,
                creditTokenInfo.token_type,
                creditTokenInfo.token_number,
                creditTokenInfo.cardholder_name,
                creditTokenInfo.card_type,
                creditTokenInfo.exp_date).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (!isFragmentAlive) {
                    return;
                }

                Utils.hideProgress();

                int code = response.code();

                if (code == 200) {
                    // success
                    Utils.showShortToast(UpdateCreditCardFragment.this.getContext(), getString(R.string.update_success), false);
//                    UpdateCreditCardActivity.this.finish();
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                if (!isFragmentAlive) {
                    return;
                }


                Utils.hideProgress();
                Utils.showShortToast(UpdateCreditCardFragment.this.getContext(), getString(R.string.weak_cell_signal), true);
            }
        });


    }

}
