package com.jo.spectrumtracking.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;

import androidx.appcompat.widget.SwitchCompat;

import java.lang.reflect.Field;

public class CustomSwitch extends SwitchCompat {
    public CustomSwitch(Context context) {
        super(context);
    }
    public CustomSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CustomSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        try {
            Field switchWidth = SwitchCompat.class.getDeclaredField("mSwitchWidth");
            switchWidth.setAccessible(true);

            // Using 120 below as example width to set
            // We could use attr to pass in the desire width
            switchWidth.setInt(this, getPixel(70));

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    private int getPixel(int dp) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        return height;
    }
}
