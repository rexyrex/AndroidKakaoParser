package com.rexyrex.kakaoparser.Activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.rexyrex.kakaoparser.Entities.ChatLine;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.LogUtils;

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

public class ChatStatsActivity extends AppCompatActivity {

    TextView chatStatsTV;
    Button loadDayDtlBtn, findBtn;
    EditText findET;
    File chatFile;
    Spinner daySpinner;
    String chatStatsStr;
    ProgressBar popupPB;
    PieChart chatAmountPieChart;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_stats);
        loadDayDtlBtn = findViewById(R.id.loadDayDtlBtn);
        findBtn = findViewById(R.id.findInChatBtn);
        findET = findViewById(R.id.findInChatET);
        daySpinner = findViewById(R.id.daysSpinner);
        chatStatsTV = findViewById(R.id.chatStatsTV);
        chatAmountPieChart = findViewById(R.id.chatAmountPieChart);

        View view = (LayoutInflater.from(ChatStatsActivity.this)).inflate(R.layout.horizontal_progress_popup, null);

        popupPB = view.findViewById(R.id.popupPB);

        AlertDialog.Builder rexAlertBuilder = new AlertDialog.Builder(ChatStatsActivity.this, R.style.PopupStyle);
        rexAlertBuilder.setView(view);
        rexAlertBuilder.setCancelable(false);
        final AlertDialog dialog = rexAlertBuilder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();



        AsyncTask<String, Void, String> statsTask = new AsyncTask<String, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {

                chatStatsStr = "";
                chatFile = (File) ChatStatsActivity.this.getIntent().getSerializableExtra("chat");
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

                    ChatStatsActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int progress = (int) (((double)tInt/totalInt) * 100);
                            popupPB.setProgress( progress);
                        }
                    });

                    if(date!=null){
                        SimpleDateFormat format = new SimpleDateFormat("yyyy년 M월 d일 (E)");
                        String dateKey = format.format(date);
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

                ChatStatsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(ChatStatsActivity.this, android.R.layout.simple_spinner_dropdown_item, spinnerItems);
                        daySpinner.setAdapter(adapter);
                        chatStatsTV.setText(chatStatsStr);
                        chatAmountPieChart.setData(pieData);

                        chatAmountPieChart.animateXY(1000, 1000);

                        loadDayDtlBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                View view = (LayoutInflater.from(ChatStatsActivity.this)).inflate(R.layout.day_chat, null);

                                TextView dayChatTV = view.findViewById(R.id.dayChatTV);
                                TextView dayDateTV = view.findViewById(R.id.dayDateTV);

                                String dayChatStr = "";
                                String spinnerVal = daySpinner.getSelectedItem().toString().split(" \\[")[0];
                                LogUtils.e("Spinner Val : " + spinnerVal);
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                for(ChatLine cl : chatMap.get(spinnerVal)){
                                    dayChatStr += "[" + timeFormat.format(cl.getDate()) + "] " + cl.getAuthor() + " : " + cl.getContent() + "\n";
                                }
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 M월 d일 (E)");
                                dayDateTV.setText(dateFormat.format(chatMap.get(spinnerVal).get(0).getDate()));

                                dayChatTV.setText(dayChatStr);

                                AlertDialog.Builder rexAlertBuilder = new AlertDialog.Builder(ChatStatsActivity.this, R.style.PauseDialog2);
                                rexAlertBuilder.setView(view);
                                rexAlertBuilder.setCancelable(true);
                                final AlertDialog dialog = rexAlertBuilder.create();
                                dialog.show();
                            }
                        });

                        findBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                View view = (LayoutInflater.from(ChatStatsActivity.this)).inflate(R.layout.day_chat, null);

                                TextView dayChatTV = view.findViewById(R.id.dayChatTV);
                                TextView dayDateTV = view.findViewById(R.id.dayDateTV);

                                String searchText = findET.getText().toString();
                                if(searchText == null || searchText.length()<2){
                                    Toast.makeText(ChatStatsActivity.this, "검색 문장 길이가 너무 짧습니다", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                //ArrayList<ChatLine> foundChatLines = new ArrayList<>();
                                String dayChatStr = "검색 결과 없음";

                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 M월 d일 (E)");

                                for (Map.Entry<String, ArrayList<ChatLine>> entry : chatMap.entrySet()) {
                                    String key = entry.getKey();
                                    ArrayList<ChatLine> value = entry.getValue();

                                    for (ChatLine cl : chatMap.get(key)) {
                                        if (cl.getContent().contains(findET.getText().toString())) {
                                            //foundChatLines.add(cl);
                                            dayChatStr += "[" + dateFormat.format(cl.getDate()) + "] " + cl.getAuthor() + " : " + cl.getContent() + "\n";
                                        }
                                    }
                                }
                                dayDateTV.setText("\"" + findET.getText().toString()+ "\"" + "의 검색 결과");
                                dayChatTV.setText(dayChatStr);

                                AlertDialog.Builder rexAlertBuilder = new AlertDialog.Builder(ChatStatsActivity.this, R.style.PauseDialog2);
                                rexAlertBuilder.setView(view);
                                rexAlertBuilder.setCancelable(true);
                                final AlertDialog dialog = rexAlertBuilder.create();
                                dialog.show();
                            }
                        });
                    }
                });
                return "";
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                dialog.hide();
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