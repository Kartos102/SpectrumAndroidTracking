package com.jo.spectrumtracking.fragment.datepicker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.jo.gps.spectrumtracking.R;

import java.util.Calendar;

public class DatePickerFragment1 extends Fragment implements DatePicker.OnDateChangedListener {

    public DatePickerFragment1() {
    }

    @Override
    public void onDateChanged(DatePicker datePicker, int i, int i1, int i2) {
        FromDatePickedListener listener = (FromDatePickedListener) getParentFragment();
        listener.OnFromDatePicked(i, i1, i2);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_date_picker, container, false);

        DatePicker datePicker = view.findViewById(R.id.datePicker);
        Calendar cal = Calendar.getInstance();
        datePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), this);

        return view;
    }

    interface FromDatePickedListener {
        void OnFromDatePicked(int year, int month, int day);
    }
}
