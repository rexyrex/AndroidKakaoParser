package com.rexyrex.kakaoparser.Activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.ChatLine;
import com.rexyrex.kakaoparser.Entities.Pair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.ui.main.SectionsPagerAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

public class ChatStatsTabActivity extends AppCompatActivity {

    ChatData cd;
    String chatStatsStr;
    ProgressBar popupPB;
    SectionsPagerAdapter sectionsPagerAdapter;
    ViewPager viewPager;
    TabLayout tabs;
    TextView titleTV;

    public static Comparator<Pair> idComparator = new Comparator<Pair>(){
        @Override
        public int compare(Pair o1, Pair o2) {
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

        AlertDialog.Builder rexAlertBuilder = new AlertDialog.Builder(ChatStatsTabActivity.this, R.style.PopupStyle);
        rexAlertBuilder.setView(view);
        rexAlertBuilder.setCancelable(false);
        final AlertDialog dialog = rexAlertBuilder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        cd = new ChatData();

        final File chatFile = (File) ChatStatsTabActivity.this.getIntent().getSerializableExtra("chat");

        cd.setChatFile(chatFile);



        @SuppressLint("StaticFieldLeak") AsyncTask<String, Void, String> statsTask = new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {
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

                ArrayList<String> chatters = new ArrayList<>();
                //Date yyyyMMdd, ChatLine
                final LinkedHashMap<String, ArrayList<ChatLine>> chatMap = new LinkedHashMap<>();
                HashMap<String, Integer> chatAmount = new HashMap<>();
                HashMap<String, Integer> wordFreqMap = new HashMap<>();

                for(int i=0; i<chatLines.length; i++){
                    String person = getPersonFromLine(chatLines[i]);
                    String chat = getChatFromLine(chatLines[i]);
                    Date date = getDateFromLine(chatLines[i]);

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

                    if(date!=null){
                        SimpleDateFormat format = new SimpleDateFormat("yyyy년 M월 d일 (E)");
                        String dateKey = format.format(date);

                        //update chat data date
                        if(!chatStartDateSet){
                            chatStartDateSet = true;
                            cd.setChatStartDate(date);
                        } else {
                            cd.setChatEndDate(date);
                        }

                        //Populate chatMap
                        if(chatMap.containsKey(dateKey)){
                            chatMap.get(dateKey).add(new ChatLine(date, person, chat));
                        } else {
                            ArrayList<ChatLine> tmpCLArrList = new ArrayList<>();
                            tmpCLArrList.add(new ChatLine(date, person, chat));
                            chatMap.put(dateKey, tmpCLArrList);
                        }

                        //populate chatters
                        if(!chatters.contains(person) && !person.equals("")){
                            chatters.add(person);
                        }

                        //populate chat amount map
                        if(chatAmount.containsKey(person)){
                            chatAmount.put(person, chatAmount.get(person) + 1);
                        } else {
                            chatAmount.put(person, 0);
                        }

                        //populate word freq map
                        //split chat line into words
                        String[] splitWords = chat.split(" ");
                        for(int w=0; w<splitWords.length; w++){
                            if(wordFreqMap.containsKey(splitWords[w])){
                                wordFreqMap.put(splitWords[w], wordFreqMap.get(splitWords[w]) + 1);
                            } else {
                                wordFreqMap.put(splitWords[w], 0);
                            }
                        }
                    }
                }

                Queue<Pair> wordFreqQueue = new PriorityQueue(wordFreqMap.size(), idComparator);
                for (Map.Entry<String, Integer> entry : wordFreqMap.entrySet()) {
                    String key = entry.getKey();
                    Integer value = entry.getValue();
                    wordFreqQueue.add(new Pair(key, value));
                }

                // Test the order
                Pair temp = wordFreqQueue.peek();




                chatStatsStr += "File Name : " + chatFile.getName() + "\n";
                chatStatsStr += "File Size : " + chatFile.length() + "\n";
                chatStatsStr += "Lines : " + chatLines.length + "\n";
                chatStatsStr += "Chatters : " + chatters.size() + "\n";
                chatStatsStr += "====== Chat Amount ======\n";

                ArrayList chatAmountArrayList = new ArrayList();
                ArrayList chatNicknameArrayList = new ArrayList();
                int tmpIndex = 0;

                for(String chatter : chatters){
                    chatStatsStr += chatter + " : " + chatAmount.get(chatter) + "\n";
                    chatAmountArrayList.add(new PieEntry(chatAmount.get(chatter),chatter));
                    chatNicknameArrayList.add(chatter);
                    tmpIndex++;
                }

                PieDataSet dataSet = new PieDataSet(chatAmountArrayList, "채팅 비율");
                dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
                dataSet.setValueTextSize(12);
                final PieData pieData = new PieData(dataSet);

                chatStatsStr += "=========================\n";
                chatStatsStr += "Chat Days : " + chatMap.size() + "\n";
                chatStatsStr += "=========================\n";

                final String[] spinnerItems = new String[chatMap.size()];
                int tmpSpinnerIndex = 0;
                for (Map.Entry<String, ArrayList<ChatLine>> entry : chatMap.entrySet()) {
                    String key = entry.getKey();
                    ArrayList<ChatLine> value = entry.getValue();
                    //chatStatsStr += key + " : " + value.size() +  "\n";
                    spinnerItems[tmpSpinnerIndex] = key + " [대화: " + value.size() + "]";
                    tmpSpinnerIndex++;
                }

                cd.setChatAmount(chatAmount);
                cd.setChatLines(chatLines);
                cd.setChatMap(chatMap);
                cd.setChatStr(chatStr);
                cd.setChatters(chatters);
                cd.setWordFreqMap(wordFreqMap);
                cd.setWordFreqQueue(wordFreqQueue);

                ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ChatStatsTabActivity.this, android.R.layout.simple_spinner_dropdown_item, spinnerItems);
                    }
                });
                return "";
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                dialog.hide();
                sectionsPagerAdapter = new SectionsPagerAdapter(ChatStatsTabActivity.this, getSupportFragmentManager(), cd);
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