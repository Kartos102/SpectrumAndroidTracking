package com.jo.spectrumtracking.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.model.Resp_Error;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends Activity {

    @BindView(R.id.txt_copyright_year)
    TextView txtCopyRight;

    @BindView(R.id.edit_username)
    EditText editUsername;

    @BindView(R.id.edit_password)
    EditText editPassword;

    @BindView(R.id.ch_remember)
    CheckBox ch_remember;

    /*
    @BindView(R.id.txt_contact_us)
    TextView txtContatUS;
    */

    @BindView(R.id.txt_copyright)
    TextView txtCopyRightAsHTML;

    /*
    @BindView(R.id.txt_activate_tracker)
    TextView txtActivateTracker;
    */

    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
        // set copyright html
        String html = getString(R.string.copyright);
        Spanned htmlAsSpanned = Html.fromHtml(html);
        txtCopyRightAsHTML.setText(htmlAsSpanned);
        txtCopyRightAsHTML.setMovementMethod(LinkMovementMethod.getInstance());
        SharedPreferences preferences = getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        boolean isRemember = preferences.getBoolean("is_remember", false);
        boolean isLogin = preferences.getBoolean("is_login", false);

        // set copyright year
        txtCopyRight.setText(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));

        if (isLogin || isRemember) {
            String username = preferences.getString("username", "");
            String password = preferences.getString("password", "");

            editUsername.setText(username);
            editPassword.setText(password);
            if (isLogin) onLoginClick();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GlobalConstant.alerts = new HashMap<>();
//        logout();
    }


    @OnClick(R.id.txt_register)
    public void onRegisterClick() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.txt_forgot_password)
    public void onForgotPasswordClick() {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_forgot_password);
        //dialog.setTitle("Forgot password");
        dialog.setCancelable(false);

        Button btnOk = dialog.findViewById(R.id.btn_dlg_forgot_password_ok);

        final EditText editForgotPasswordEmail = dialog.findViewById(R.id.edit_forgot_password_email);

        btnOk.setOnClickListener(v -> {
            String email = editForgotPasswordEmail.getText().toString().toLowerCase();
            if ("".equals(email)) {
                Utils.showShortToast(LoginActivity.this, "please enter email", true);
                return;
            }
            doForgetPasswordWork(email);
            dialog.dismiss();
        });

        Button btnCancel = dialog.findViewById(R.id.btn_dlg_forgot_password_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT; // this is where the magic happens
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lWindowParams);

        /*
        String email = editUsername.getText().toString();
        if ("".equals(email)) {
            Utils.showShortToast(LoginActivity.this, "please enter email");
            return;
        }
        doForgetPasswordWork(email);
        */

    }

    public void clearCookies(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
        GlobalConstant.app_user = null;
    }

    public void logout() {
        GlobalConstant.selectedTrackerIds = new ArrayList<>();
        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);

        apiInterface.authLogout().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();

                int code = response.code();
                if (code == 200) {

                } else {
                    Utils.showShortToast(getBaseContext(), "error", true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(getBaseContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    @OnClick(R.id.btn_login)
    public void onLoginClick() {
        final String username = editUsername.getText().toString().replaceAll(" ", "").toLowerCase();
        final String password = editPassword.getText().toString().replaceAll(" ", "");

        if ("".equals(username)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_username), true);
            return;
        }
        if ("".equals(password)) {
            Utils.showShortToast(this, getResources().getString(R.string.please_enter_password), true);
            return;
        }

        Utils.showProgress(this);

        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();

        body.put("email", username);
        body.put("password", password);

        apiInterface.authLogin(body).enqueue(new Callback<ResponseBody>() {
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
                        Boolean success = false;
                        String user = "";

                        if (object.has("success")) {
                            success = object.getBoolean("success");
                        }

                        if (success) {

                            SharedPreferences preferences = getSharedPreferences("spectrum", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();

                            editor.putBoolean("is_login", true);
                            editor.putString("username", username);
                            editor.putString("password", password);
                            editor.putBoolean("is_remember", ch_remember.isChecked());
                            editor.apply();

                            Headers responseHeaders = response.headers();

                            String csrfToken = responseHeaders.get("x-csrftoken");
                            if(csrfToken.equals("undefined"))
                                csrfToken="";

//                            String locale;
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                locale = getApplicationContext().getResources().getConfiguration().getLocales().get(0).getCountry();
//                            } else {
//                                locale = getApplicationContext().getResources().getConfiguration().locale.getCountry();
//                            }
//                            if (locale != "US") {
//                                GlobalConstant.metricScale = 1.56;
//                                GlobalConstant.volumeMetricScale = 3.78541;
//                            }
                            GlobalConstant.X_CSRF_TOKEN = csrfToken;
                            GlobalConstant.email = username;
//                            Utils.showShortToast(LoginActivity.this, "welcome!");
//                            ContextWrapper cw = new ContextWrapper(getApplicationContext());
//                            File directory = cw.getDir("driver_images", Context.MODE_PRIVATE);
//                            directory.delete();
                            gotoMain(username);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(LoginActivity.this, getResources().getString(R.string.weak_cell_signal));
                    }
                } else {

                    ResponseBody errorBody = response.errorBody();
                    Resp_Error error = null;

                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(LoginActivity.this, error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(LoginActivity.this, "response parse error");
                    }

                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(LoginActivity.this, getResources().getString(R.string.weak_cell_signal), true);
            }
        });

    }

    @OnClick(R.id.btn_google_login)
    public void OnGoogleLoginClick() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    // [END onActivityResult]

    // [START handleSignInResult]
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            // Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(@Nullable GoogleSignInAccount account) {
        if (account != null) {
            System.out.println("ok");
        } else {
            System.out.println("no");
        }
    }

    @OnClick(R.id.btn_facebook_login)
    public void OnFacebookClick() {

    }

    public void gotoMain(String username) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void doForgetPasswordWork(final String email) {
        Utils.showProgress(this);

        ApiInterface apiInterface = ApiClient.getClient(this).create(ApiInterface.class);
        HashMap<String, Object> body = new HashMap<>();

        body.put("email", email.toLowerCase());

     /*   Intent intent=new Intent(LoginActivity.this,ResetPasswordActivity.class);
        intent.putExtra("email",editUsername.getText().toString());
        startActivity(intent);*/

        apiInterface.authResendPasswordReset(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();

                int code = response.code();
                Gson gson = new Gson();

                if (code == 200) {
                    // success
                    Utils.showShortToast(LoginActivity.this, "email sent", false);
                    Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);


                } else {
                    ResponseBody errorBody = response.errorBody();

                    Resp_Error error = null;
                    try {
                        error = gson.fromJson(errorBody.string(), Resp_Error.class);
                        Utils.showShortToast(LoginActivity.this, error.getMessage(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
//                        Utils.showShortToast(LoginActivity.this, "response parse error");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(LoginActivity.this, getResources().getString(R.string.weak_cell_signal), true);
            }
        });

    }

}
