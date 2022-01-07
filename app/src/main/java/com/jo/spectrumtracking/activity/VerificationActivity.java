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

public class VerificationActivity extends AppCompatActivity {
    @BindView(R.id.verify_email)
    EditText verifyEmailTxt;

    @BindView(R.id.verify_code)
    EditText verifyCodeTxt;

    @BindView(R.id.btn_verify)
    Button btnVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        verifyEmailTxt.setText(email);


    }

    @OnClick(R.id.txt_back)
    public void onBackClick() {
        this.finish();
    }

    @OnClick(R.id.btn_verify)
    public void onVerifyClick() {

        verify();
    }

    private void verify() {
        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();
        Utils.showProgress(VerificationActivity.this);
        body.put("email", verifyEmailTxt.getText().toString());
        body.put("code", verifyCodeTxt.getText().toString());


        apiInterface.authVerify(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                int code = response.code();

                if (code == 200) {
                    // success
                    ResponseBody responseBody = response.body();
                    Utils.showShortToast(VerificationActivity.this, "Verification success", false);
                    Intent intent = new Intent(VerificationActivity.this, LoginActivity.class);
                    startActivity(intent);

                } else {
                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;
                    try {

                        Utils.showShortToast(VerificationActivity.this, error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(VerificationActivity.this, "Response parse error");
                    }
                }
                Utils.hideProgress();
                //   btnVerify.setEnabled(true);

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(VerificationActivity.this, getResources().getString(R.string.weak_cell_signal), true);
                // btnVerify.setEnabled(true);
            }
        });

    }
}
