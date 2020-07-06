package com.rexyrex.kakaoparser.Activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
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
    String chatStatsStr;
    ProgressBar popupPB;
    SectionsPagerAdapter sectionsPagerAdapter;
    ViewPager viewPager;
    TabLayout tabs;
    TextView titleTV;

    MainDatabase database;
    ChatLineDAO chatLineDao;
    WordDAO wordDao;

    public static Comparator<StringIntPair> wordFreqComparator = new Comparator<StringIntPair>(){
        @Override
        public int compare(StringIntPair o1, StringIntPair o2) {
            if(o1.getFrequency() > o2.getFrequency()) {
                return -1;
            }
            else if(o1.getFrequency() < o2.getFrequency()){
                return 1;
            }
            else {
                return 0;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        View view = (LayoutInflater.from(ChatStatsTabActivity.this)).inflate(R.layout.horizontal_progress_popup, null);

        popupPB = view.findViewById(R.id.popupPB);

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

        final File chatFile = (File) ChatStatsTabActivity.this.getIntent().getSerializableExtra("chat");

        cd.setChatFile(chatFile);



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

                boolean chatStartDateSet = false;
                chatStatsStr = "";
                String chatStr = FileParseUtils.parseFile(chatFile);
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

                Date date = null;
                String person = null;
                String chat = null;

                for(int i=0; i<chatLines.length; i++){
                    //String person = getPersonFromLine(chatLines[i]);
                    //String chat = getChatFromLine(chatLines[i]);
                    //Date date = getDateFromLine(chatLines[i]);



                    Pattern p = Pattern.compile("(\\d{4}년 \\d{1,2}월 \\d{1,2}일 (?:오후|오전) \\d{1,2}:\\d{1,2}),? (.+?) : ?(.+)");
                    Matcher m = p.matcher(chatLines[i]);
                    boolean match = m.matches();

                    if(match){
                        try {
                            date = sdf.parse(m.group(1));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        person = m.group(2);
                        chat = m.group(3);

                        //LogUtils.e("Person: " + person);
                        //LogUtils.e("Chat: " + chat);
                    }


                    final int tInt = i;
                    final int totalInt = chatLines.length;

                    final int progress = (int) (((double)tInt/totalInt) * 100);

                    if(progress % 10 == 0){
                        ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                popupPB.setProgress( progress);
                            }
                        });
                    }

                    if(match){
                        SimpleDateFormat format = new SimpleDateFormat("yyyy년 M월 d일 (E)");
                        String dateKey = format.format(date);

                        int tmpIndex = chatLineModelArrayList.size();
                        chatLineModelArrayList.add(new ChatLineModel(tmpIndex, date, dateKey, person, chat));

                        //populate word freq map
                        //split chat line into words
//                        chat.replace('(', ' ');
//                        chat.replace(')', ' ');
//                        chat.replace('<', ' ');
//                        chat.replace('>', ' ');

                        String[] splitWords = chat.split(" ");
                        for(int w=0; w<splitWords.length; w++){
                            if(splitWords[w].length()>0){
                                //word db
                                wordModelArrayList.add(new WordModel(chatLineModelArrayList.size()-1, date, person, splitWords[w]));
                            }
                        }
                    }
                }

                chatStatsStr += "File Name : " + chatFile.getName() + "\n";
                chatStatsStr += "File Size : " + chatFile.length() + "\n";
                chatStatsStr += "Lines : " + chatLines.length + "\n";

                cd.setChatLines(chatLines);
                cd.setChatStr(chatStr);

                chatLineDao.insertAll(chatLineModelArrayList);
                wordDao.insertAll(wordModelArrayList);
                long loadTime = System.currentTimeMillis() - loadStartTime;
                double loadElapsedSeconds = loadTime/1000.0;
                cd.setLoadElapsedSeconds(loadElapsedSeconds);

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

    public Date getDateFromLine(String line){
        String[] split = line.split(", ");
        if(split.length<2){
            return null;
        } else {
            Date date = new Date();
            String dateStr = split[0];

            if(!dateStr.equals("")){
                //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:m", Locale.KOREAN);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 a h:m", Locale.KOREAN);
                try {
                    date = sdf.parse(dateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return date;
        }
    }

    public String getDateStrFromLine(String line){
        String[] split = line.split(", ");
        if(split.length<2){
            return "";
        } else {
            return split[0];
        }
    }

    public String getChatFromLine(String line){
        String chat = "";
        String[] split = line.split(", ");
        if(split.length<2){
            return line;
        } else {
            String[] split2 = split[1].split(" : ");
            for(int i=1; i<split2.length; i++){
                chat += split2[i];
            }
            return chat;
        }
    }

    public String getPersonFromLine(String line){
        String chat = "";
        String[] split = line.split(", ");
        if(split.length>1){
            if(split[1].contains(" :")) {
                String[] split2 = split[1].split(" :");
                return split2[0];
            } else {
                return "";
            }
        }

        return "";
    }


}