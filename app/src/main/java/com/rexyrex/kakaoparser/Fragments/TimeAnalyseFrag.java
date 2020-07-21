package com.rexyrex.kakaoparser.Fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.DateIntPair;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.ValueFormatters.DayAxisValueFormatter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeAnalyseFrag extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    private RadarChart radarChart;
    private BarChart barChart;

    private MainDatabase database;
    private ChatLineDAO cld;

    ChatData cd;

    String[] daysOfWeek = {"월", "화", "수", "목", "금", "토", "일"};

    public TimeAnalyseFrag() {
        // Required empty public constructor
    }


    public static TimeAnalyseFrag newInstance() {
        TimeAnalyseFrag fragment = new TimeAnalyseFrag();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = MainDatabase.getDatabase(getContext());
        cld = database.getChatLineDAO();
        cd = ChatData.getInstance();

        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_analyse, container, false);

        Spinner typeSpinner = view.findViewById(R.id.timeAnalyseTypeSpinner);

        barChart = view.findViewById(R.id.dayBarChart);
        barChart.getDescription().setEnabled(false);

        barChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        barChart.setPinchZoom(false);

        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);

        ArrayList<BarEntry> barEntryArrayList = new ArrayList<>();
        List<DateIntPair> freqByDayPairs = cld.getFreqByDay();
        Date startDate = freqByDayPairs.get(0).getDate();

        ValueFormatter xAxisFormatter = new DayAxisValueFormatter(barChart, startDate);

        XAxis barXAxis = barChart.getXAxis();
        barXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        barXAxis.setDrawGridLines(false);
        barXAxis.setGranularity(1f);
        barXAxis.setLabelCount(6);
        barXAxis.setValueFormatter(xAxisFormatter);

        barChart.getAxisLeft().setDrawGridLines(false);

        //set data

        for(int i=0; i<freqByDayPairs.size(); i++){
            DateIntPair tmpPair = freqByDayPairs.get(i);
            long dateDiff = tmpPair.getDate().getTime() - startDate.getTime();
            int daysDiff = (int)( (tmpPair.getDate().getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
            barEntryArrayList.add(new BarEntry((float) daysDiff, tmpPair.getFrequency()));
        }

        BarDataSet barDataSet = new BarDataSet(barEntryArrayList, "일별 채팅량");
        barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.9f);

        barChart.setData(data);

        // add a nice and smooth animation
        barChart.animateY(1500);
        barChart.getLegend().setEnabled(false);

        radarChart = view.findViewById(R.id.dayOfWeekRadarChart);
        radarChart.setBackgroundColor(getActivity().getColor(R.color.lightBrown));

        radarChart.getDescription().setEnabled(false);

        radarChart.setWebLineWidth(1f);
        radarChart.setWebColor(getActivity().getColor(R.color.colorPrimary));
        radarChart.setWebLineWidthInner(1f);
        radarChart.setWebColorInner(getActivity().getColor(R.color.colorPrimaryDark));
        radarChart.setWebAlpha(100);


//        MarkerView mv = new MarkerView(getContext(), R.layout.radar_markerview);
//        mv.setChartView(chart); // For bounds control
//        chart.setMarker(mv); // Set the marker to the chart

        setData();

        XAxis xAxis = radarChart.getXAxis();
        //xAxis.setTypeface(tfLight);
        xAxis.setTextSize(16f);
        xAxis.setYOffset(12f);
        xAxis.setXOffset(12f);
        xAxis.setValueFormatter(new ValueFormatter() {

            private final String[] mActivities = daysOfWeek;

            @Override
            public String getFormattedValue(float value) {
                return mActivities[(int) value % mActivities.length];
            }
        });
        xAxis.setTextColor(Color.BLACK);

        int maxVal = cd.getMaxFreqByDayOfWeek();

        YAxis yAxis = radarChart.getYAxis();
        //yAxis.setTypeface(tfLight);
        yAxis.setLabelCount(5, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(maxVal * 1.2f);
        yAxis.setDrawLabels(false);

        Legend l = radarChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        //l.setTypeface(tfLight);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        l.setTextColor(Color.BLACK);

        final ImageView testIV = view.findViewById(R.id.imageTestView);

        final View[] viewItems = new View[]{radarChart, testIV, barChart};
        final String[] items = new String[]{"Radar", "Pic", "BarChart"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        typeSpinner.setAdapter(adapter);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for(int i=0; i<viewItems.length; i++){
                    if(position == i){
                        viewItems[i].setVisibility(View.VISIBLE);
                    } else {
                        viewItems[i].setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    private void setData() {

        ArrayList<RadarEntry> entries1 = new ArrayList<>();
        ArrayList<RadarEntry> entries2 = new ArrayList<>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
//        for (int i = 0; i < cnt; i++) {
//            float val1 = (float) (Math.random() * mul) + min;
//            entries1.add(new RadarEntry(val1));
//
//            float val2 = (float) (Math.random() * mul) + min;
//            entries2.add(new RadarEntry(val2));
//        }

        List<StringIntPair> res = cd.getFreqByDayOfWeek();

        for(StringIntPair sip : res){
            LogUtils.e("Day: " + sip.getword() + ", Freq: " + sip.getFrequency() );
        }

        for(String day : daysOfWeek){
            for(StringIntPair sip : res){
                if(day.equals(sip.getword())){
                    entries1.add(new RadarEntry(sip.getFrequency()));
                }
            }
        }

        RadarDataSet set1 = new RadarDataSet(entries1, "Last Week");
        set1.setColor(getActivity().getColor(R.color.colorPrimary));
        set1.setFillColor(getActivity().getColor(R.color.colorPrimaryDark));
        set1.setDrawFilled(true);
        set1.setFillAlpha(180);
        set1.setLineWidth(2f);
        set1.setDrawHighlightCircleEnabled(true);
        set1.setDrawHighlightIndicators(false);

        RadarDataSet set2 = new RadarDataSet(entries2, "This Week");
        set2.setColor(Color.rgb(121, 162, 175));
        set2.setFillColor(Color.rgb(121, 162, 175));
        set2.setDrawFilled(true);
        set2.setFillAlpha(180);
        set2.setLineWidth(2f);
        set2.setDrawHighlightCircleEnabled(true);
        set2.setDrawHighlightIndicators(false);

        ArrayList<IRadarDataSet> sets = new ArrayList<>();
        sets.add(set1);
        sets.add(set2);

        RadarData data = new RadarData(sets);
        //data.setValueTypeface(tfLight);
        data.setValueTextSize(8f);
        data.setDrawValues(false);
        data.setValueTextColor(Color.WHITE);

        radarChart.setData(data);
        radarChart.invalidate();
    }
}