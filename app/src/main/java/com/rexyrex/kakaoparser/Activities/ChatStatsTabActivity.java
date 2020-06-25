package com.rexyrex.kakaoparser.Activities;

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
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.ui.main.SectionsPagerAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ChatStatsTabActivity extends AppCompatActivity {

    ChatData cd;
    String chatStatsStr;
    ProgressBar popupPB;
    SectionsPagerAdapter sectionsPagerAdapter;
    ViewPager viewPager;
    TabLayout tabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        View view = (LayoutInflater.from(ChatStatsTabActivity.this)).inflate(R.layout.horizontal_progress_popup, null);

        popupPB = view.findViewById(R.id.popupPB);

        viewPager = findViewById(R.id.view_pager);
        tabs = findViewById(R.id.tabs);

        AlertDialog.Builder rexAlertBuilder = new AlertDialog.Builder(ChatStatsTabActivity.this, R.style.PopupStyle);
        rexAlertBuilder.setView(view);
        rexAlertBuilder.setCancelable(false);
        final AlertDialog dialog = rexAlertBuilder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        cd = new ChatData();

        final File chatFile = (File) ChatStatsTabActivity.this.getIntent().getSerializableExtra("chat");

        cd.setChatFile(chatFile);

        AsyncTask<String, Void, String> statsTask = new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {
                boolean chatStartDateSet = false;
                chatStatsStr = "";
                String chatStr = parseFile(chatFile);
                String[] chatLines = chatStr.split("\n");

                ArrayList<String> chatters = new ArrayList<>();
                //Date yyyyMMdd, ChatLine
                final LinkedHashMap<String, ArrayList<ChatLine>> chatMap = new LinkedHashMap<>();
                HashMap<String, Integer> chatAmount = new HashMap<>();

                for(int i=0; i<chatLines.length; i++){
                    String person = getPersonFromLine(chatLines[i]);
                    String chat = getChatFromLine(chatLines[i]);
                    Date date = getDateFromLine(chatLines[i]);



                    final int tInt = i;
                    final int totalInt = chatLines.length;

                    ChatStatsTabActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int progress = (int) (((double)tInt/totalInt) * 100);
                            popupPB.setProgress( progress);
                        }
                    });

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

                        if(!chatters.contains(person) && !person.equals("")){
                            chatters.add(person);
                        }
                        if(chatAmount.containsKey(person)){
                            chatAmount.put(person, chatAmount.get(person) + 1);
                        } else {
                            chatAmount.put(person, 0);
                        }
                    }
                }

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

    public String parseFile(File file) {
        String fileName = file.getAbsolutePath() + "/KakaoTalkChats.txt";
        String chat = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            int index = 0;
            while (line != null) {
                if(index > 3 && line.length() > 0){
                    sb.append(line);
                    sb.append("\n");
                }
                index++;
                line = br.readLine();
            }
            chat = sb.toString();
            LogUtils.e( "chat size: " + chat.length());
            String[] lines = chat.split("\n");
            LogUtils.e( "lines: " + lines.length);
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return chat;
    }
}