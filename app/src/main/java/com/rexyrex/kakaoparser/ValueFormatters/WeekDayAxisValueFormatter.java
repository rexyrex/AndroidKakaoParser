package com.rexyrex.kakaoparser.ValueFormatters;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class WeekDayAxisValueFormatter extends ValueFormatter{

    String[] daysOfWeek = {"월", "화", "수", "목", "금", "토", "일"};

    public WeekDayAxisValueFormatter() {
    }

    @Override
    public String getFormattedValue(float value) {
        return daysOfWeek[(int) value];
    }
}