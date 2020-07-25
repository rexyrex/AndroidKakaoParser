package com.rexyrex.kakaoparser.ValueFormatters;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class YearAxisValueFormatter extends ValueFormatter{

    private final BarLineChartBase<?> chart;
    private final Date startDate;

    public YearAxisValueFormatter(BarLineChartBase<?> chart, Date startDate) {
        this.chart = chart;
        this.startDate = startDate;
    }

    @Override
    public String getFormattedValue(float value) {

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy년");
        Date newDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.YEAR, (int) value);

        newDate = c.getTime();
        return yearFormat.format(newDate);

    }


}