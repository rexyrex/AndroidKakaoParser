package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.ChatLine;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordDetailAnalyseActivity extends AppCompatActivity {
    TextView titleTV;
    TextView freqTV;
    PieChart freqPieChart;
    ListView chatLinesLV;

    private MainDatabase database;
    private ChatLineDAO chatLineDao;
    private WordDAO wordDao;

    public static HashMap<String, Integer> wordFreqMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail_analyse);

        database = MainDatabase.getDatabase(this);
        chatLineDao = database.getChatLineDAO();
        wordDao = database.getWordDAO();

        titleTV = findViewById(R.id.wordDtlTitleTV);
        freqTV = findViewById(R.id.wordDtlFreqTV);
        freqPieChart = findViewById(R.id.wordDtlFreqPieChart);
        chatLinesLV = findViewById(R.id.wordChatSentencesLV);

        String word = this.getIntent().getStringExtra("word");

        titleTV.setText(word+"");
        freqTV.setText(wordDao.getFreqWordListSearch(word).getFrequency() + "회");

        WordListAdapter ca = new WordListAdapter(chatLineDao.getChatLinesContainingWord(word));
        chatLinesLV.setAdapter(ca);

        freqPieChart.setData(getWordUserFreqPieData(word));
        freqPieChart.animateXY(2000, 2000);
    }

    class WordListAdapter extends BaseAdapter {
        List<ChatLineModel> wordFreqArrList;

        WordListAdapter(List<ChatLineModel> wordFreqArrList){
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
            TextView sentenceTV = convertView.findViewById(R.id.generalStatsElemTitleTV);

            sentenceTV.setText(wordFreqArrList.get(position).getContent());

            return convertView;
        }
    }

    public PieData getWordUserFreqPieData(String word){
        ArrayList chatAmountArrayList = new ArrayList();

        List<StringIntPair> authorFrequencyList = wordDao.getFreqWordListSearchByAuthor(word);
        for (StringIntPair sip : authorFrequencyList) {
            chatAmountArrayList.add(new PieEntry(sip.getFrequency(),sip.getword()));
        }

        PieDataSet dataSet = new PieDataSet(chatAmountArrayList, "단어 사용 비율");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setSliceSpace(4);
        PieData pieData = new PieData(dataSet);
        return pieData;
    }
}