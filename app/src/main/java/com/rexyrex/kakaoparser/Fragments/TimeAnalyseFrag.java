package com.rexyrex.kakaoparser.Fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class TimeAnalyseFrag extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    private RadarChart chart;

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

        chart = view.findViewById(R.id.dayOfWeekRadarChart);
        chart.setBackgroundColor(getActivity().getColor(R.color.lightBrown));
        chart.getDescription().setEnabled(false);

        chart.setWebLineWidth(1f);
        chart.setWebColor(getActivity().getColor(R.color.colorPrimary));
        chart.setWebLineWidthInner(1f);
        chart.setWebColorInner(getActivity().getColor(R.color.colorPrimaryDark));
        chart.setWebAlpha(100);


//        MarkerView mv = new MarkerView(getContext(), R.layout.radar_markerview);
//        mv.setChartView(chart); // For bounds control
//        chart.setMarker(mv); // Set the marker to the chart

        setData();

        XAxis xAxis = chart.getXAxis();
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

        YAxis yAxis = chart.getYAxis();
        //yAxis.setTypeface(tfLight);
        yAxis.setLabelCount(5, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(maxVal * 1.2f);
        yAxis.setDrawLabels(false);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        //l.setTypeface(tfLight);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        l.setTextColor(Color.BLACK);

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

        chart.setData(data);
        chart.invalidate();
    }
}