package com.jo.spectrumtracking.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_Error;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {
    @BindView(R.id.reset_email)
    EditText resetEmailTxt;

    @BindView(R.id.reset_verify_code)
    EditText resetVerifyCodeTxt;

    @BindView(R.id.reset_new_password)
    EditText resetNewPasswordTxt;

    @BindView(R.id.reset_confirm_password)
    EditText resetConfirmPasswordTxt;

    @BindView(R.id.btn_reset_password)
    Button btn_resetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        ButterKnife.bind(this);
        Intent intent=getIntent();
        String email=intent.getStringExtra("email");
        resetEmailTxt.setText(email);
    }
    @OnClick(R.id.txt_back)
    public void onBackClick() {
        this.finish();
    }

    @OnClick(R.id.btn_reset_password)
    public void onResetPasswordClick(){
        String email=resetEmailTxt.getText().toString();
        String newPassword=resetNewPasswordTxt.getText().toString();
        String confirmPassword=resetConfirmPasswordTxt.getText().toString();
        String verificationCode=resetVerifyCodeTxt.getText().toString();

        if(email.equals("")) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_username), true);
            return;
        }

        if(newPassword.equals("") || confirmPassword.equals("")){
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_password), true);
            return;
        }
        if(!newPassword.equals(confirmPassword)) {
            Utils.showShortToast(this, getResources().getString(R.string.password_confirm_does_not_match), true);
        }
        if(verificationCode.equals("")) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_verification_code), true);
            return;
        }

        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();
        Utils.showProgress(ResetPasswordActivity.this);
        body.put("email", email);
        body.put("code", verificationCode);
        body.put("password",newPassword);

        apiInterface.resetPassword(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                int code = response.code();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    Utils.showShortToast(ResetPasswordActivity.this, getResources().getString(R.string.reset_password_has_been_successfully), true);

                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {

                        Utils.showShortToast(ResetPasswordActivity.this, error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(ResetPasswordActivity.this, "Response parse error");
                    }
                }
                Utils.hideProgress();
                //   btnVerify.setEnabled(true);

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(ResetPasswordActivity.this, getResources().getString(R.string.weak_cell_signal), true);
                // btnVerify.setEnabled(true);
            }
        });

    }

}
