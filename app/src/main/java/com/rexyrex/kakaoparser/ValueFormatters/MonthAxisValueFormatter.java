package com.rexyrex.kakaoparser.ValueFormatters;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MonthAxisValueFormatter extends ValueFormatter{

    private final BarLineChartBase<?> chart;
    private final Date startDate;

    public MonthAxisValueFormatter(BarLineChartBase<?> chart, Date startDate) {
        this.chart = chart;
        this.startDate = startDate;
    }

    @Override
    public String getFormattedValue(float value) {

        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-M");
        Date newDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.MONTH, (int) value);

        newDate = c.getTime();
        return monthFormat.format(newDate);

    }


}