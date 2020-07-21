package com.rexyrex.kakaoparser.ValueFormatters;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DayAxisValueFormatter extends ValueFormatter{

    private final BarLineChartBase<?> chart;
    private final Date startDate;

    public DayAxisValueFormatter(BarLineChartBase<?> chart, Date startDate) {
        this.chart = chart;
        this.startDate = startDate;
    }

    @Override
    public String getFormattedValue(float value) {

        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-M");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-M-d");
        Date newDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DATE, (int) value);
        newDate = c.getTime();


        if (chart.getVisibleXRange() > 150) {

            return monthFormat.format(newDate);
        } else {
            return format.format(newDate);
        }
    }


}