package com.rexyrex.kakaoparser.Activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Database.Models.WordModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.ChatLine;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.ui.main.SectionsPagerAdapter;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatStatsTabActivity extends AppCompatActivity {

    ChatData cd;
    ProgressBar popupPB;
    TextView loadingTextTV;
    ImageView loadingGifIV;

    SectionsPagerAdapter sectionsPagerAdapter;
    ViewPager viewPager;
    TabLayout tabs;
    TextView titleTV;

    MainDatabase database;
    ChatLineDAO chatLineDao;
    WordDAO wordDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        View view = (LayoutInflater.from(ChatStatsTabActivity.this)).inflate(R.layout.horizontal_progress_popup, null);
        popupPB = view.findViewById(R.id.popupPB);
        loadingTextTV = view.findViewById(R.id.loadingTextTV);
        loadingGifIV = view.findViewById(R.id.loadingGifIV);
        Glide.with(this).asGif().load(R.drawable.loading1).into(loadingGifIV);

        viewPager = findViewById(R.id.view_pager);
        tabs = findViewById(R.id.tabs);
        titleTV = findViewById(R.id.title);

        database = MainDatabase.getDatabase(this);
        chatLineDao = database.getChatLineDAO();
        wordDao = database.getWordDAO();

        AlertDialog.Builder rexAlertBuilder = new AlertDialog.Builder(ChatStatsTabActivity.this, R.style.PopupStyle);
        rexAlertBuilder.setView(view);
        rexAlertBuilder.setCancelable(false);
        final AlertDialog dialog = rexAlertBuilder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        cd = ChatData.getInstance();
        final File chatFile = cd.getChatFile();

        @SuppressLint("StaticFieldLeak") AsyncTask<String, Void, String> statsTask = new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {
                long loadStartTime = System.currentTimeMillis();

                //clear tables
                wordDao.truncateTable();
                chatLineDao.truncateTable();

                String chatStr = FileParseUtils.parseFile(chatFile);

                //First, load chat room name only (later load date as spannable string)
                final String chatTitle = FileParseUtils.parseFileForTitle(chatFile);
                ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        titleTV.setText( chatTitle);
                    }
                });

                final String[] chatLines = chatStr.split("\n");

                final ArrayList<ChatLineModel> chatLineModelArrayList = new ArrayList<>();
                final ArrayList<WordModel> wordModelArrayList = new ArrayList<>();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 a h:m", Locale.KOREAN);
                SimpleDateFormat format = new SimpleDateFormat("yyyy년 M월 d일 (E)");

                Date date = null;
                String person = null;
                String chat = null;

                Pattern p = Pattern.compile("(\\d{4}년 \\d{1,2}월 \\d{1,2}일 (?:오후|오전) \\d{1,2}:\\d{1,2}),? (.+?) : ?(.+)");

                for(int i=0; i<chatLines.length; i++){
                    final int progress = (int) (((double)i/chatLines.length) * 100);

                    if(progress % 2 == 0){
                        ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                popupPB.setProgress( progress);
                            }
                        });
                    }

                    Matcher m = p.matcher(chatLines[i]);
                    if(m.matches()){
                        try {
                            date = sdf.parse(m.group(1));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        person = m.group(2);
                        chat = m.group(3);

                        String dateKey = format.format(date);

                        chatLineModelArrayList.add(new ChatLineModel(date, dateKey, person, chat));

                        String[] splitWords = chat.split(" ");
                        for(int w=0; w<splitWords.length; w++){
                            if(splitWords[w].length()>0){
                                wordModelArrayList.add(new WordModel(date, person, splitWords[w]));
                            }
                        }
                    }
                }

                ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        popupPB.setVisibility(View.INVISIBLE);
                        loadingTextTV.setText("정밀 분석중...");
                        loadingGifIV.setVisibility(View.VISIBLE);
                    }
                });
                chatLineDao.insertAll(chatLineModelArrayList);
                wordDao.insertAll(wordModelArrayList);
                long loadTime = System.currentTimeMillis() - loadStartTime;
                double loadElapsedSeconds = loadTime/1000.0;
                cd.setLoadElapsedSeconds(loadElapsedSeconds);

                //Change Title to include date
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.M.d");
                String dateRangeStr = "(" + dateFormat.format(chatLineDao.getStartDate()) + " ~ " + dateFormat.format(chatLineDao.getEndDate()) + ")";
                final SpannableString newChatTitle = generateTitleSpannableText(chatTitle, dateRangeStr);
                ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        titleTV.setText( newChatTitle);
                    }
                });

                return "";
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                dialog.cancel();
                sectionsPagerAdapter = new SectionsPagerAdapter(ChatStatsTabActivity.this, getSupportFragmentManager());
                viewPager.setAdapter(sectionsPagerAdapter);
                tabs.setupWithViewPager(viewPager);
            }
        };
        statsTask.execute();
    }

    private SpannableString generateTitleSpannableText(String title, String dateRangeStr) {
        SpannableString s = new SpannableString(title + "\n" + dateRangeStr);
        s.setSpan(new StyleSpan(Typeface.BOLD), 0, title.length(), 0);
        s.setSpan(new ForegroundColorSpan(getColor(R.color.lightBrown)), title.length(), title.length() + dateRangeStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new StyleSpan(Typeface.ITALIC), title.length(), title.length() + dateRangeStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new AbsoluteSizeSpan(15, true), title.length(), title.length() + dateRangeStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //AlignmentSpan alignmentSpan = new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE);
        //s.setSpan(alignmentSpan, title.length(), title.length() + dateRangeStr.length()+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
    }
}