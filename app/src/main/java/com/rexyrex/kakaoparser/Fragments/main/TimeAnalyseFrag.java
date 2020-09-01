package com.rexyrex.kakaoparser.Fragments.main;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.DateIntPair;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.ValueFormatters.DayAxisValueFormatter;
import com.rexyrex.kakaoparser.ValueFormatters.MonthAxisValueFormatter;
import com.rexyrex.kakaoparser.ValueFormatters.WeekDayAxisValueFormatter;
import com.rexyrex.kakaoparser.ValueFormatters.YearAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TimeAnalyseFrag extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    private RadarChart radarChart;
    private BarChart barChart;

    private MainDatabase database;
    private ChatLineDAO cld;

    ChatData cd;

    String[] daysOfWeek = {"월", "화", "수", "목", "금", "토", "일"};
    String[] items = {"시간 분석", "요일 분석", "일 분석", "월 분석", "연 분석"};
    String[] timeOfDayStrs = {"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23"};

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
        LogUtils.e("FRAGMENT ON CREATE VIEW");

        Spinner typeSpinner = view.findViewById(R.id.timeAnalyseTypeSpinner);

        //
        // Data
        // AxisValueFormatter
        // xAxis granularity, bar width should be reliant on this val
        barChart = view.findViewById(R.id.dayBarChart);
        radarChart = view.findViewById(R.id.dayOfWeekRadarChart);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        typeSpinner.setAdapter(adapter);

        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadGraph(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        typeSpinner.setSelection(0);

        LogUtils.e("Loading : " + typeSpinner.getSelectedItemPosition());
        loadGraph(typeSpinner.getSelectedItemPosition());

        return view;
    }

    private void loadGraph(int position){
        boolean isBar = true;
        View[] viewItems = {radarChart, barChart, barChart, barChart, barChart};


        switch(items[position]){
            case "일 분석":
                LogUtils.e("일별");
                makeBarChart(cld.getFreqByDay(), "day");
                barChart.invalidate();
                break;
            case "월 분석":
                makeBarChart(cld.getFreqByMonth(), "month");
                barChart.invalidate();
                break;
            case "연 분석":
                makeBarChart(cld.getFreqByYear(), "year");
                barChart.invalidate();
                break;
            case "요일 분석":
                makeBarChart(cld.getFreqByDayOfWeek(), "dayOfWeek");
                barChart.invalidate();
                break;
            case "시간 분석":
                isBar = false;
                makeRadarChart();
                break;
            default:
                isBar = false;
                break;
        }

        for(int i=0; i<viewItems.length; i++){
            if(position == i){
                viewItems[i].setVisibility(View.VISIBLE);
            } else {
                //LogUtils.e("isBar: " + isBar);
                if(i > 0 && isBar){
                    //LogUtils.e("Not Hide : " + i);
                } else {
                    //LogUtils.e("Hiding: " + i);
                    viewItems[i].setVisibility(View.GONE);
                }

            }
        }
    }

    private void makeBarChart(List listData, String type) {
        barChart.getDescription().setEnabled(false);

        barChart.setBackgroundColor(getActivity().getColor(R.color.lightBrown));

        barChart.setMaxVisibleValueCount(50);

        // scaling can now only be done on x- and y-axis separately
        barChart.setPinchZoom(false);

        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);

        //barChart.resetZoom();
        while(!barChart.isFullyZoomedOut()){
            barChart.zoomOut();
        }

        ArrayList<BarEntry> barEntryArrayList = new ArrayList<>();

        XAxis barXAxis = barChart.getXAxis();
        barXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        barXAxis.setDrawGridLines(false);

        barXAxis.setLabelCount(6);

        ValueFormatter xAxisFormatter = null;
        float xAxisGranularity = 1f;

        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy년 M월");
        SimpleDateFormat parsableFormat = new SimpleDateFormat("yyyy-MM-dd");

        switch(type){
            case "day" :
                List<DateIntPair> freqByDayPairs = listData;
                Date startDate = freqByDayPairs.get(0).getDate();
                Long startTime = startDate.getTime();
                startDate = new Date(startTime - startTime % (24 * 60 * 60 * 1000));
                xAxisFormatter = new DayAxisValueFormatter(barChart, startDate);
                xAxisGranularity = 1f;

                for(int i=0; i<freqByDayPairs.size(); i++){
                    DateIntPair tmpPair = freqByDayPairs.get(i);
                    int daysDiff = (int)( (tmpPair.getDate().getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24));
                    barEntryArrayList.add(new BarEntry((float) daysDiff, tmpPair.getFrequency()));
                }
                break;
            case "month" :
                List<StringIntPair> freqByMonthPairs = listData;
                String startDateStr = freqByMonthPairs.get(0).getword();

                Date startMonthDate = null;
                try{
                    startMonthDate = monthFormat.parse(startDateStr);
                    xAxisFormatter = new MonthAxisValueFormatter(barChart, startMonthDate);

                    for(int i=0; i<freqByMonthPairs.size(); i++){
                        StringIntPair tmpPair = freqByMonthPairs.get(i);

                        Date tmpDate = monthFormat.parse(tmpPair.getword());

                        long monthsBetween = ChronoUnit.MONTHS.between(
                                YearMonth.from(LocalDate.parse(parsableFormat.format(startMonthDate))),
                                YearMonth.from(LocalDate.parse(parsableFormat.format(tmpDate)))
                        );

                        barEntryArrayList.add(new BarEntry((float) monthsBetween, tmpPair.getFrequency()));
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
                break;
            case "year" :
                List<StringIntPair> freqByYearPairs = listData;
                String startDateYearStr = freqByYearPairs.get(0).getword();
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy년");

                Date startYearDate = null;
                try{
                    startYearDate = yearFormat.parse(startDateYearStr);

                    LogUtils.e("Start YEar Date: " + startYearDate.toString());
                    LogUtils.e(" freqByYearPairs.size() size: " + freqByYearPairs.size());


                    xAxisFormatter = new YearAxisValueFormatter(barChart, startYearDate);

                    for(int i=0; i<freqByYearPairs.size(); i++){
                        StringIntPair tmpPair = freqByYearPairs.get(i);

                        Date tmpDate = yearFormat.parse(tmpPair.getword());

                        LogUtils.e("parsable: " + parsableFormat.format(startYearDate));
                        LogUtils.e("parsable2: " + parsableFormat.format(tmpDate));

                        long yearsBetween = ChronoUnit.YEARS.between(
                                Year.from(LocalDate.parse(parsableFormat.format(startYearDate))),
                                Year.from(LocalDate.parse(parsableFormat.format(tmpDate)))
                        );

                        LogUtils.e(" yearsBetween: " + yearsBetween);

                        barEntryArrayList.add(new BarEntry((float) yearsBetween, tmpPair.getFrequency()));
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
                break;

            case "dayOfWeek" :
                List<StringIntPair> freqByDayOfWeekPairs = listData;

                int tmpInd = 0;

                xAxisFormatter = new WeekDayAxisValueFormatter();

                for(String day : daysOfWeek){
                    for(StringIntPair sip : freqByDayOfWeekPairs){
                        if(day.equals(sip.getword())){
                            barEntryArrayList.add(new BarEntry(tmpInd, sip.getFrequency()));
                            LogUtils.e("day:" + sip.getword());
                            LogUtils.e("freq:" + sip.getFrequency());
                            tmpInd++;
                        }
                    }
                }
                break;
            default : break;
        }

        barXAxis.setGranularity(xAxisGranularity);
        barXAxis.setValueFormatter(xAxisFormatter);

        barChart.getAxisLeft().setDrawGridLines(false);

        //set data


        LogUtils.e("data count:" + barEntryArrayList.size());
        BarDataSet barDataSet = new BarDataSet(barEntryArrayList, "일별 채팅량");
        //barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        barDataSet.setColor(getActivity().getColor(R.color.colorPrimaryDark));

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(barDataSet);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(xAxisGranularity*0.9f);

        barChart.setData(data);

        //barChart.animateY(1500);
        barChart.getLegend().setEnabled(false);
    }

    private void makeRadarChart() {

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
        radarChart.setClickable(false);

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

        List<StringIntPair> res = cld.getFreqByTimeOfDay();



        for(StringIntPair sip : res){
            LogUtils.e("Key: " + sip.getword() + ", Freq: " + sip.getFrequency() );
        }

        for(String tString : timeOfDayStrs){
            LogUtils.e("searching for " + tString);
            boolean atLeastOne = false;
            for(StringIntPair sip : res){
                if(tString.equals(sip.getword())){
                    entries1.add(new RadarEntry(sip.getFrequency()));
                    LogUtils.e("added! " + sip.getFrequency());
                    atLeastOne = true;
                }
            }
            LogUtils.e("DONE searching for " + tString);
            if(!atLeastOne){
                entries1.add(new RadarEntry(0));
            }
        }

        LogUtils.e("entries size : " + entries1.size());

        RadarDataSet set1 = new RadarDataSet(entries1, "Last Week");
        set1.setColor(getActivity().getColor(R.color.colorPrimary));
        set1.setFillColor(getActivity().getColor(R.color.colorPrimaryDark));
        set1.setDrawFilled(true);
        set1.setFillAlpha(180);
        set1.setLineWidth(2f);
        set1.setDrawHighlightCircleEnabled(false);
        set1.setDrawHighlightIndicators(false);
        set1.setDrawValues(true);

        RadarDataSet set2 = new RadarDataSet(entries2, "This Week");
        set2.setColor(Color.rgb(121, 162, 175));
        set2.setFillColor(Color.rgb(121, 162, 175));
        set2.setDrawFilled(true);
        set2.setFillAlpha(180);
        set2.setLineWidth(2f);
        set2.setDrawHighlightCircleEnabled(false);
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

        radarChart.getData().setHighlightEnabled(false);

        XAxis xAxis = radarChart.getXAxis();
        //xAxis.setTypeface(tfLight);
        xAxis.setTextSize(12f);
        xAxis.setYOffset(22f);
        xAxis.setXOffset(22f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return timeOfDayStrs[(int) value % timeOfDayStrs.length] + "시";
            }
        });
        xAxis.setTextColor(Color.BLACK);

        YAxis yAxis = radarChart.getYAxis();
        //yAxis.setTypeface(tfLight);
        yAxis.setLabelCount(10, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0f);
        //yAxis.setAxisMaximum(maxVal * 1.2f);
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
    }
}