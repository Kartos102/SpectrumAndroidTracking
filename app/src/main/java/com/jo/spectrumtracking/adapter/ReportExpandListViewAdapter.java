package com.jo.spectrumtracking.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.jo.gps.spectrumtracking.R;
import com.jo.spectrumtracking.fragment.ReportsFragment;
import com.jo.spectrumtracking.model.Report_Group;
import com.jo.spectrumtracking.model.Report_Value;
import com.jo.spectrumtracking.model.Resp_Event;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReportExpandListViewAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ReportsFragment fragment;
    // group titles
    private List<Report_Group> listDataGroup;

    // child data in format of header title, child title
    private HashMap<String, List<Report_Value>> listDataChild;

    private List<Resp_Event> listEvent;

    public ReportExpandListViewAdapter(ReportsFragment fragment, Context context, List<Report_Group> listDataGroup,
                                       HashMap<String, List<Report_Value>> listChildData, List<Resp_Event> listEvent) {
        this.context = context;
        this.listDataGroup = listDataGroup;
        this.listDataChild = listChildData;
        this.listEvent = listEvent;
        this.fragment = fragment;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this.listDataChild.get(this.listDataGroup.get(groupPosition).getTitle())
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    public class MyXAxisValueFormatter implements IAxisValueFormatter {

        private String[] mValues;

        public MyXAxisValueFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            return mValues[(int) value];
        }

    }
    private List<Resp_Event> getEventList(String title){
        List<Resp_Event> temp_list = new ArrayList<>();
        for(int i=0;i<listEvent.size();i++){
            if(listEvent.get(i).getAlarm().toLowerCase(Locale.ROOT).replaceAll(" ","").equals(title.toLowerCase(Locale.ROOT).replaceAll(" ",""))){
                temp_list.add(listEvent.get(i));
            }
        }
        return temp_list;
    }
    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.report_child_layout, null);
        }

        BarChart barchart = convertView.findViewById(R.id.bar_chart);
        CardView chartView = convertView.findViewById(R.id.chartView);
        RecyclerView eventRecyclerView = convertView.findViewById(R.id.eventRecyclerView);
        Log.d("list_event::::", "" + listEvent.size());
        String group_title = this.listDataGroup.get(groupPosition).getTitle();
        if ((group_title.equals("Engine On") || group_title.equals("Engine Off") || group_title.equals("Device Removal")) && listEvent.size() > 0) {
            chartView.setVisibility(View.GONE);
            eventRecyclerView.setVisibility(View.VISIBLE);
            List<Resp_Event> temp_list = getEventList(group_title);
            this.listDataGroup.set(groupPosition, new Report_Group(group_title,String.valueOf(temp_list.size())));
            EventAdapter event_adapter = new EventAdapter(this.fragment, temp_list, R.layout.recyclerview_events_row);
            eventRecyclerView.setAdapter(event_adapter);
            eventRecyclerView.setLayoutManager(new LinearLayoutManager(this.context));
            eventRecyclerView.setItemAnimator(new DefaultItemAnimator());
        } else {
            eventRecyclerView.setVisibility(View.GONE);
            chartView.setVisibility(View.VISIBLE);
            List<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                boolean value_flag = false;
                for (int j = 0; j < this.listDataChild.get(this.listDataGroup.get(groupPosition).getTitle()).size(); j++) {
                    Date date = this.listDataChild.get(this.listDataGroup.get(groupPosition).getTitle()).get(j).getDate();
                    String value = this.listDataChild.get(this.listDataGroup.get(groupPosition).getTitle()).get(j).getValue();
                    Calendar c = Calendar.getInstance();
                    c.setTimeZone(TimeZone.getTimeZone("GMT"));
                    c.setTime(date);
                    Log.d("date", date.toString());
                    int dayOfWeek = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
                    if (i == dayOfWeek) {
                        entries.add(new BarEntry((float) dayOfWeek, Double.valueOf(value).floatValue()));
                        value_flag = true;
                        break;
                    }
                }
                if (!value_flag) entries.add(new BarEntry((float) i, 0));
            }

            BarDataSet set = new BarDataSet(entries, this.listDataGroup.get(groupPosition).getTitle());
            set.setValueTextSize(14f);
            BarData data = new BarData(set);
            if (groupPosition > 2) {
                data.setValueFormatter(new MyValueFormatter(0));
            } else {
                data.setValueFormatter(new MyValueFormatter(2));
            }
            String[] x_values = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            XAxis xAxis = barchart.getXAxis();
            xAxis.setValueFormatter(new MyXAxisValueFormatter(x_values));
            xAxis.setTextSize(16f);
            xAxis.setTextColor(Color.rgb(101, 38, 152));
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            barchart.setData(data);
            barchart.getDescription().setEnabled(false);
            barchart.getAxisRight().setDrawGridLines(false);
            barchart.getAxisLeft().setDrawGridLines(false);
            barchart.getXAxis().setDrawGridLines(false);
            barchart.setClickable(false);
            barchart.setTouchEnabled(false);
            barchart.getLegend().setTextSize(13f);
            barchart.getAxisRight().setEnabled(false);
            barchart.setScaleEnabled(false);
            barchart.getAxisLeft().setEnabled(false);
            barchart.setFitBars(true); // make the x-axis fit exactly all bars
            barchart.invalidate();
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (this.listDataGroup.get(groupPosition).getTitle().equals("Main Events") && listEvent.size() > 0)
            return 1;
        else {
            return (this.listDataChild.get(this.listDataGroup.get(groupPosition).getTitle())
                    .size() > 0) ? 1 : 0;
        }
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        Report_Group header = (Report_Group) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.report_group_layout, null);
        }

        TextView textViewGroup = convertView
                .findViewById(R.id.textViewGroup);
        textViewGroup.setTypeface(null, Typeface.BOLD);
        textViewGroup.setText(header.getTitle());

        TextView groupvalue = convertView
                .findViewById(R.id.mark);
        groupvalue.setText(header.getValue());

        return convertView;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataGroup.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataGroup.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    public class MyValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter(int num) {
            String p_str = "###,###,##0";
            if (num > 0) p_str += ".";
            for (int i = 0; i < num; i++) {
                p_str += "0";
            }
            mFormat = new DecimalFormat(p_str);
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return mFormat.format(value);
        }
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}