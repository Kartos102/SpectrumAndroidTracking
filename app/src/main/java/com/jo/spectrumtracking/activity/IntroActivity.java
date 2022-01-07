package com.jo.spectrumtracking.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.fragment.AppIntroCustomLayoutFragment;

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getApplicationContext().getSharedPreferences("spectrum", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Integer launchCount = preferences.getInt("launch_count", 0);

        if (launchCount >= 3) {
            Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        } else {
            editor.putInt("launch_count", launchCount + 1);
            editor.apply();
        }

        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_custom_layout1));
        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_custom_layout2));
//        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_custom_layout3));
//        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_custom_layout4));
//        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_custom_layout5));
//        addSlide(AppIntroCustomLayoutFragment.newInstance(R.layout.intro_custom_layout6));

        showStatusBar(false);
        setColorSkipButton(getResources().getColor(R.color.main));
        setNextArrowColor(getResources().getColor(R.color.main));
        setColorDoneText(getResources().getColor(R.color.main));
        setBarColor(getResources().getColor(R.color.black));
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
