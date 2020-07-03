package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.ChatLine;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.Fragments.WordAnalyseFrag;
import com.rexyrex.kakaoparser.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class WordDetailAnalyseActivity extends AppCompatActivity {

    TextView titleTV;
    TextView freqTV;
    PieChart freqPieChart;

    ListView chatLinesLV;

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
        chatLinesLV = findViewById(R.id.wordChatSentencesLV);

        String word = this.getIntent().getStringExtra("word");

        //ChatData chatFile = (ChatData) this.getIntent().getParcelableExtra("chat");

        titleTV.setText(word+"");
        freqTV.setText(cd.getWordFreqMap().get(word) + "회");

        WordListAdapter ca = new WordListAdapter(cd.getWordChatLinesMap().get(word));
        chatLinesLV.setAdapter(ca);

        freqPieChart.setData(cd.getWordUserFreqPieData(word));
        freqPieChart.animateXY(2000, 2000);
    }

    class WordListAdapter extends BaseAdapter {
        ArrayList<ChatLine> wordFreqArrList;

        WordListAdapter(ArrayList<ChatLine> wordFreqArrList){
            this.wordFreqArrList = wordFreqArrList;
        }

        @Override
        public int getCount() {
            return wordFreqArrList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_chat_line, null);
            TextView sentenceTV = convertView.findViewById(R.id.chatLineExampleSentenceTV);

            sentenceTV.setText(wordFreqArrList.get(position).getContent());

            return convertView;
        }
    }
}