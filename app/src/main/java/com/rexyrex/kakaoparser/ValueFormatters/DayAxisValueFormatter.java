package com.rexyrex.kakaoparser.ValueFormatters;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DayAxisValueFormatter extends ValueFormatter{

    private final BarLineChartBase<?> chart;

    public DayAxisValueFormatter(BarLineChartBase<?> chart) {
        this.chart = chart;
    }

    @Override
    public String getFormattedValue(float value) {

        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-M");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-M-d");
        Date date = new Date((long) value*1000);

        if (chart.getVisibleXRange() > 15780000) {

            return monthFormat.format(date);
        } else {
            return format.format(date);
        }
    }


}