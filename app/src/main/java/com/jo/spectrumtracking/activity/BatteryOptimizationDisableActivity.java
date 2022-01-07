package com.jo.spectrumtracking.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.global.Utils;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BatteryOptimizationDisableActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 6000;

    private CheckBox cbGPS;
    private CheckBox cbLocation;
    private CheckBox cbBattery;
    private Button btnDone;
    private Button tvReCall;

    private boolean allGood;
    View parentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_optimization_disable);

        parentLayout = findViewById(android.R.id.content);

        cbGPS = findViewById(R.id.cbGPS);
        cbLocation = findViewById(R.id.cbLocation);
        cbBattery = findViewById(R.id.cbBattery);
        btnDone = findViewById(R.id.btnDone);
        tvReCall = findViewById(R.id.tvReCall);

        if (isGPSEnabled() && isLocationPermissionAccepted()) {
            startActivity(new Intent(BatteryOptimizationDisableActivity.this, LoginActivity.class));
            finish();
        }

    }

    private static boolean checkedGPS = false;

    @Override
    protected void onStart() {
        super.onStart();
        checkedGPS = true;
        callChecking();
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkedGPS = false;
        //callChecking();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkedGPS)
            callChecking();

        //btnDone.setEnabled(allGood);
        btnDone.setOnClickListener(view -> {
            startActivity(new Intent(BatteryOptimizationDisableActivity.this, LoginActivity.class));
            finish();
        });

        tvReCall.setOnClickListener(view -> callChecking());
    }

    private void callChecking() {

        if (isGPSEnabled()) {
            cbGPS.setChecked(true);
        } else {
            showGPSDisabledAlert();
        }

        if (isLocationPermissionAccepted()) {
            cbLocation.setChecked(true);
        } else {
            checkForPermissions(parentLayout);
        }

        if (isGPSEnabled() && isLocationPermissionAccepted()) {
            allGood = true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isBatteryOptimizationDisabled()) {
                cbBattery.setChecked(true);
            } else {
                showBatteryDisabledAlert();
            }
        }

    }

    private void checkForPermissions(View view) {
        MultiplePermissionsListener snackbarMultiplePermissionsListener =
                SnackbarOnAnyDeniedMultiplePermissionsListener.Builder
                        .with(view, getResources().getString(R.string.we_need_permissions_access_to_track_you))
                        .withButton(getResources().getString(R.string.accept), this::checkForPermissions)
                        .withCallback(new Snackbar.Callback() {
                            @Override
                            public void onShown(Snackbar snackbar) {
                                // Event handler for when the given Snackbar is visible
                            }

                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                // Event handler for when the given Snackbar has been dismissed
                            }
                        })
                        .build();

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(snackbarMultiplePermissionsListener).check();
    }

    private boolean isLocationPermissionAccepted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showGPSDisabledAlert() {
        Utils.showSweetAlert(this, null, getResources().getString(R.string.gps_disabled_in_your_device_would_you_like_enable), getResources().getString(R.string.goto_setting_page_to_enable_gps), "Cacnel", SweetAlertDialog.WARNING_TYPE, new Utils.OnSweetAlertListener() {
            @Override
            public void onConfirm() {
                Intent callGPSSettingIntent = new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(callGPSSettingIntent);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showBatteryDisabledAlert() {
        Dialog alertDialog = new Dialog(BatteryOptimizationDisableActivity.this);
        alertDialog.setContentView(R.layout.dialog_battery_optimization);
        TextView btnDisableBatteryOpt = alertDialog.findViewById(R.id.btn_disable_optimization);
        TextView btnShowMeHow = alertDialog.findViewById(R.id.btn_show_me_how);
        TextView btnSkip = alertDialog.findViewById(R.id.btn_skip);

        btnDisableBatteryOpt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent();
                myIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                startActivityForResult(myIntent, REQUEST_CODE);
            }
        });

        btnShowMeHow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog showD = new Dialog(BatteryOptimizationDisableActivity.this);
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean isBatteryOptimizationDisabled() {
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        assert pm != null;
        return pm.isIgnoringBatteryOptimizations(packageName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String packageName = getPackageName();
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                if (pm.isIgnoringBatteryOptimizations(packageName)) {
                    cbBattery.setChecked(true);
                }
            }
        }
    }


}
