package com.rexyrex.kakaoparser.Entities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.rexyrex.kakaoparser.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ChatData {

    double loadElapsedSeconds;
    File chatFile;

    public File getChatFile() {
        return chatFile;
    }

    public void setChatFile(File chatFile) {
        this.chatFile = chatFile;
    }

    public double getLoadElapsedSeconds() {
        return loadElapsedSeconds;
    }

    public void setLoadElapsedSeconds(double loadElapsedSeconds) {
        this.loadElapsedSeconds = loadElapsedSeconds;
    }

    public ChatData() {

    }

    private static ChatData the_instance;
    public static ChatData getInstance() {
        if (the_instance == null) {
            the_instance = new ChatData();
        }

        return the_instance;
    }
}
