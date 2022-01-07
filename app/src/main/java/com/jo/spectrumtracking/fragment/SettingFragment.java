package com.jo.spectrumtracking.fragment;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.activity.LoginActivity;
import com.jo.spectrumtracking.activity.MainActivity;
import com.jo.spectrumtracking.api.ApiClient;
import com.jo.spectrumtracking.api.ApiInterface;
import com.jo.spectrumtracking.global.GlobalConstant;
import com.jo.spectrumtracking.global.Utils;
import com.jo.spectrumtracking.tracking.GPSTracker2Plus;
import com.jo.spectrumtracking.widget.CustomSwitch;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.POWER_SERVICE;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;


public class SettingFragment extends Fragment {

    boolean isFragmentAlive = false;

    @BindView(R.id.sAutoLockStatus)
    CustomSwitch sAutoLockStatus;

    @BindView(R.id.sPhoneTrackingStatus)
    CustomSwitch sPhoneTrackingStatus;

    @BindView(R.id.ic_logout)
    ImageView ic_logout;

    @BindView(R.id.ic_activate)
    ImageView ic_activate;

    @BindView(R.id.ic_geofence)
    ImageView ic_geofence;

    @BindView(R.id.edit_delay)
    EditText edit_delay;

    @BindView(R.id.ic_alarm)
    ImageView ic_alarm;

    @BindView(R.id.ic_service)
    ImageView ic_service;

    @BindView(R.id.ic_autolock)
    ImageView ic_autolock;

    @BindView(R.id.ic_phonetracking)
    ImageView ic_phone;

    @BindView(R.id.ic_faq)
    ImageView ic_faq;

    @BindView(R.id.ic_contact)
    ImageView ic_contact;

    @BindView(R.id.btnBattery)
    CardView btnBattery;

    @BindView(R.id.rb_miles)
    RadioButton rbMiles;
    @BindView(R.id.rb_kilometer)
    RadioButton rbKilometers;

    Intent intent;

    public SettingFragment() {
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isBatteryOptimizationDisabled() {
        String packageName = getApplicationContext().getPackageName();
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        assert pm != null;
        return pm.isIgnoringBatteryOptimizations(packageName);
    }

    @OnClick(R.id.btn_service)
    public void onService() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.onOrderService();
    }

    @OnClick(R.id.btn_geofence)
    public void onGeofence() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.onGeofence();
    }

    @OnClick(R.id.btn_activation)
    public void onActivate() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.onActivate();
    }

    @OnClick(R.id.btn_fag)
    public void onFAQ() {
        String url = "https://spectrumtracking.com/faq.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @OnClick(R.id.btn_contact)
    public void onContact() {
        String url = "https://spectrumtracking.com/contact.html";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    public void logout() {
        GlobalConstant.selectedTrackerIds = new ArrayList<>();
        ApiInterface apiInterface = ApiClient.getClient(this.getContext()).create(ApiInterface.class);
        apiInterface.authLogout().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Utils.hideProgress();
                int code = response.code();
                if (code == 200) {
                 /*   if (isMyServiceRunning()) {
                        Intent intent = new Intent(getApplicationContext(), GPSTracker2Plus.class);
                        getApplicationContext().stopService(intent);
                    }*/
                } else {
                    Utils.showShortToast(getContext(), "Log out error", true);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Utils.hideProgress();
                t.printStackTrace();
                Utils.showShortToast(getContext(), getString(R.string.weak_cell_signal), true);
            }
        });
    }

    @OnClick(R.id.btn_logout)
    public void onLogout() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_logout_select);
        dialog.setCancelable(false);

        Button btnYes = dialog.findViewById(R.id.btn_yes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getActivity().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("is_remember", true);
                editor.apply();
                logout();
                dialog.dismiss();
                getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        });

        Button btnNo = dialog.findViewById(R.id.btn_no);
        btnNo.setOnClickListener(v -> {
            SharedPreferences preferences = getActivity().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("is_remember", false);
            editor.apply();
            logout();
            dialog.dismiss();
            getActivity().startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(dialog.getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT; // this is where the magic happens
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.show();
        dialog.getWindow().setAttributes(lWindowParams);


        SharedPreferences preferences = getApplicationContext().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("is_login", false);
        editor.apply();


        return;
    }

    @OnCheckedChanged(R.id.sAutoLockStatus)
    public void onChange() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("sAutoLock", sAutoLockStatus.isChecked());
        editor.apply();
        if (sAutoLockStatus.isChecked()) {
            this.getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            this.getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    //FIXME: Fix NullPointerException (Context)
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (GPSTracker2Plus.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        ButterKnife.bind(this, rootView);

        SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        if (Utils.getDistanceUnit().equals("miles")) {
            rbMiles.setChecked(true);
            rbKilometers.setChecked(false);
        } else {
            rbMiles.setChecked(false);
            rbKilometers.setChecked(true);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences preferences = this.getActivity().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        boolean sAutoLockFlag = preferences.getBoolean("sAutoLock", false);
        sAutoLockStatus.setChecked(sAutoLockFlag);
        boolean sPTrackingFlag = preferences.getBoolean("sPhoneTracking", false);
        sPhoneTrackingStatus.setChecked(sPTrackingFlag);
        ic_logout.setColorFilter(getResources().getColor(R.color.menu_normal_color));
        ic_alarm.setColorFilter(getResources().getColor(R.color.menu_normal_color));
        ic_geofence.setColorFilter(getResources().getColor(R.color.menu_normal_color));
        ic_service.setColorFilter(getResources().getColor(R.color.menu_normal_color));
        ic_activate.setColorFilter(getResources().getColor(R.color.menu_normal_color));
        ic_faq.setColorFilter(getResources().getColor(R.color.menu_normal_color));
        ic_phone.setColorFilter(getResources().getColor(R.color.menu_normal_color));
        ic_contact.setColorFilter(getResources().getColor(R.color.menu_normal_color));
        ic_autolock.setColorFilter(getResources().getColor(R.color.menu_normal_color));
        SharedPreferences _preferences = getActivity().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        edit_delay.setText(String.valueOf(_preferences.getInt("upload_delay", 30)));
        edit_delay.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (!s.toString().equals("") && !s.toString().equals("0")) {
                    SharedPreferences preferences = getActivity().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();

                    editor.putInt("upload_delay", Integer.parseInt(s.toString()));
                    editor.apply();

                    if (isMyServiceRunning()) {
                      intent= new Intent(getApplicationContext(), GPSTracker2Plus.class);
                        getContext().stopService(intent);


                                intent = new Intent(getApplicationContext(), GPSTracker2Plus.class);
                                intent.putExtra("flag", true);
                                intent.putExtra("delay", Integer.parseInt(s.toString()));

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                    getContext().startForegroundService(intent);
                                } else {

                                    getContext().startService(intent);
                                }
                            }


                    } else {
                                int delay=5;
                                try {
                                    delay=Integer.parseInt(s.toString());
                                }
                                catch (Exception ex){
                                    ex.printStackTrace();
                                }

                                intent = new Intent(getApplicationContext(), GPSTracker2Plus.class);
                                intent.putExtra("flag", true);
                                intent.putExtra("delay", delay);
                                getContext().startService(intent);



                    }
                }

        });

        btnBattery.setOnClickListener(view1 -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (isBatteryOptimizationDisabled()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_need_to_do_it_you_already_disabled_it), Toast.LENGTH_SHORT).show();
                } else {
                    Intent myIntent = new Intent();
                    myIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivity(myIntent);
                }
            } else {
                doBatteryOptDisable();
//                Toast.makeText(getApplicationContext(), "Your Android version is lower that 6, you do not need to do that.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();

        return fragment;
    }

    public void doBatteryOptDisable() {
        Dialog alertDialog = new Dialog(getContext());
        alertDialog.setContentView(R.layout.dialog_battery_optimization);
        TextView btnDisableBatteryOpt = alertDialog.findViewById(R.id.btn_disable_optimization);
        TextView btnShowMeHow = alertDialog.findViewById(R.id.btn_show_me_how);
        TextView btnSkip = alertDialog.findViewById(R.id.btn_skip);

        btnDisableBatteryOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent();
                myIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivity(myIntent);
//                startActivityForResult(myIntent, REQUEST_CODE);
            }
        });

        btnShowMeHow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog showD = new Dialog(getContext());
                showD.setContentView(R.layout.dialog_battery_disable_alert);
                showD.show();
            }
        });

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        alertDialog.show();
    }

    @OnClick(R.id.rb_miles)
    public void onMilesRadioButton() {
        SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("distance_unit", "miles");
        editor.commit();
    }

    @OnClick(R.id.rb_kilometer)
    public void onKilometerRadioButton() {
        SharedPreferences preferences = MainActivity.get().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("distance_unit", "km");
        editor.commit();
    }
}
