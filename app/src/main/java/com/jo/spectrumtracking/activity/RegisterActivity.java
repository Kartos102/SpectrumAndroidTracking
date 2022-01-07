package com.jo.spectrumtracking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_Error;
import com.jo.spectrumtracking.model.Resp_User;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.edit_first_name)
    EditText editFirstName;

    @BindView(R.id.edit_last_name)
    EditText editLastName;

    @BindView(R.id.edit_email)
    EditText editEmail;

    @BindView(R.id.edit_password)
    EditText editPassword;

    @BindView(R.id.edit_password_confirm)
    EditText editPasswordConfirm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.txt_back)
    public void onBackClick() {
        this.finish();
    }


    @OnClick(R.id.btn_register)
    public void onRegisterClick() {
        String firstName = editFirstName.getText().toString();
        String lastName = editLastName.getText().toString();
        final String email = editEmail.getText().toString().replaceAll(" ", "").toLowerCase();
        String password = editPassword.getText().toString().replaceAll(" ", "");
        String passwordConfirm = editPasswordConfirm.getText().toString();

        if ("".equals(firstName)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_first_name), true);
            return;
        }

        if ("".equals(lastName)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_last_name), true);
            return;
        }

        if ("".equals(email)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_email), true);
            return;
        }

        if ("".equals(password)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_password), true);
            return;
        }

        if (!password.equals(passwordConfirm)) {
            Utils.showShortToast(this, getResources().getString(R.string.password_confirm_does_not_match), true);
            return;
        }

        Utils.showProgress(this);

        userRegistraion(email, password, firstName, lastName);


    }

    private void setPhoneTracker(String email) throws JSONException {

        ApiInterface apiInterface = ApiClient.getClient(RegisterActivity.this.getBaseContext()).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();
        body.put("reportingId", email);
        body.put("userId", GlobalConstant.app_user.getString("_id"));
        apiInterface.registerPhoneTracker(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                int code = response.code();
                if (code == 201) {
                    ResponseBody responseBody = response.body();
                    JSONObject object = null;
                    try {
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);


                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(RegisterActivity.this.getBaseContext(), "response parse error");
                    }
                } else {
//                    Utils.showShortToast(RegisterActivity.this.getBaseContext(), "response parse error");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Utils.showShortToast(RegisterActivity.this.getBaseContext(), getString(R.string.weak_cell_signal), true);
            }
        });

    }

    private void userRegistraion(String email, String password, String firstName, String lastName) {
        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();

        body.put("email", email);
        body.put("password", password);
        body.put("firstName", firstName);
        body.put("lastName", lastName);

        /*
        Intent intent=new Intent(RegisterActivity.this,VerificationActivity.class);
        intent.putExtra("email",email);
        intent.putExtra("verification_code","");
        startActivity(intent);
        */

        apiInterface.authRegister(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();

                int code = response.code();
                Gson gson = new Gson();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    Resp_User user = null;
                    try {
                        user = gson.fromJson(responseBody.string(), Resp_User.class);
                        //The old code
                        /*
                        Intent intent=new Intent(RegisterActivity.this,VerificationActivity.class);
                        intent.putExtra("email",email);
                        intent.putExtra("verification_code","");
                        startActivity(intent);*/

                        /*try {
                            setPhoneTracker(email);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        */
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        Utils.showShortToast(RegisterActivity.this, "register success", false);

                    } catch (IOException e) {
                        e.printStackTrace();
//                        Utils.showShortToast(RegisterActivity.this, "response parse error");
                    }
                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(RegisterActivity.this, error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(RegisterActivity.this, "response parse error");
                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(RegisterActivity.this, getResources().getString(R.string.weak_cell_signal), true);
            }
        });
    }


}
