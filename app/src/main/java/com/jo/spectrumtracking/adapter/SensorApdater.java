package com.jo.spectrumtracking.adapter;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import androidx.fragment.app.Fragment;

public class SensorApdater implements SensorEventListener{
    private Fragment fragment;
    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
