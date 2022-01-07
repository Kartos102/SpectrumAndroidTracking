package com.jo.spectrumtracking.fragment.datepicker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.adapter.ViewpagerAdapter;

import java.util.Calendar;

public class DatePickerDialog extends DialogFragment implements DatePickerFragment1.FromDatePickedListener, DatePickerFragment2.ToDatePickedListener {

    private CustomViewPager viewPager;

    private Calendar fromDate;
    private Calendar cal;

    private DateRangePickedListener listener;

    private boolean datePicked;

    public DatePickerDialog() {
    }

    public static DatePickerDialog newInstance() {
        return new DatePickerDialog();
    }

    public static DatePickerDialog newInstance(Calendar from, Calendar to) {
        return new DatePickerDialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        cal = Calendar.getInstance();
        initCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), true);
        fromDate = cal;

        return inflater.inflate(R.layout.dialog_calendar_picker, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPager = view.findViewById(R.id.viewpager);
        setupViewPager(viewPager);
        datePicked = false;

        TabLayout tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!datePicked && listener != null) {
            listener.OnDatePickCancelled();
        }
    }

    @Override
    public void OnFromDatePicked(int year, int month, int day) {
        cal = Calendar.getInstance();
        initCalendar(year, month, day, true);
        fromDate = cal;
        viewPager.setCurrentItem(1);
        viewPager.reMeasureCurrentPage(1);
    }

    @Override
    public void OnToDatePicked(int year, int month, int day) {
        cal = Calendar.getInstance();
        cal.set(year, month, day);
        Calendar toDate = cal;
        datePicked = true;
        if (listener != null) {
            if (fromDate.after(toDate)) {
                fromDate.set(Calendar.HOUR_OF_DAY, 23);
                fromDate.set(Calendar.MINUTE, 59);
                fromDate.set(Calendar.SECOND, 59);
                toDate.set(year, month, day, 0, 0, 0);
                listener.OnDateRangePicked(toDate, fromDate);
            } else {
                toDate.set(year, month, day, 23, 59, 59);
                listener.OnDateRangePicked(fromDate, toDate);
            }
        }
        getDialog().dismiss();
    }

    public void setOnDateRangePickedListener(DateRangePickedListener listener) {
        this.listener = listener;
    }

    private void initCalendar(int year, int month, int date, boolean flag) {
        cal.set(year, month, date);
        if (flag) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.clear(Calendar.MINUTE);
            cal.clear(Calendar.SECOND);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewpagerAdapter adapter = new ViewpagerAdapter(getChildFragmentManager());
        adapter.addFragment(new DatePickerFragment1(), "From");
        adapter.addFragment(new DatePickerFragment2(), "To");
        viewPager.setAdapter(adapter);
    }
}
