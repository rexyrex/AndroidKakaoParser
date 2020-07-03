package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.ChatLine;
import com.rexyrex.kakaoparser.R;

import java.io.File;
import java.util.HashMap;

public class WordDetailAnalyseActivity extends AppCompatActivity {

    TextView titleTV;
    TextView freqTV;
    PieChart freqPieChart;

    ChatData cd;
    public static HashMap<String, Integer> wordFreqMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail_analyse);

        cd = ChatData.getInstance();

        titleTV = findViewById(R.id.wordDtlTitleTV);
        freqTV = findViewById(R.id.wordDtlFreqTV);
        freqPieChart = findViewById(R.id.wordDtlFreqPieChart);

        String word = this.getIntent().getStringExtra("word");

        //ChatData chatFile = (ChatData) this.getIntent().getParcelableExtra("chat");

        titleTV.setText(word+"");
        freqTV.setText(cd.getWordFreqMap().get(word) + "회");
    }
}