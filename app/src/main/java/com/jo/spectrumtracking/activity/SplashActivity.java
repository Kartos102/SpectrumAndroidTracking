package com.jo.spectrumtracking.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.FirebaseApp;
import com.jo.gps.spectrumtracking.R;


public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        /*
        new AppRate(this)
                .setMinDaysUntilPrompt(7)
                .setMinLaunchesUntilPrompt(5)
                .init();
                */

        int SPLASH_DISPLAY_LENGTH = (int) (1.5 * 1000);
        new Handler().postDelayed(() -> {
            /* Create an Intent that will start the Menu-Activity. */
//                    Intent intent = new Intent(SplashActivity.this, BatteryOptimizationDisableActivity.class);
            Intent intent = new Intent(SplashActivity.this, IntroActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DISPLAY_LENGTH);

    }
}
