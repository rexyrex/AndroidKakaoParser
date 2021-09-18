package com.rexyrex.kakaoparser.Activities.DetailActivities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.rexyrex.kakaoparser.Activities.ChatPeekActivity;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Entities.ChatSnippetData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class WordDetailAnalyseActivity extends AppCompatActivity {
    TextView titleTV;
    TextView freqTV;
    PieChart freqPieChart;
    ListView chatLinesLV;

    private MainDatabase database;
    private ChatLineDAO chatLineDao;
    private WordDAO wordDao;

    public static HashMap<String, Integer> wordFreqMap;
    List<ChatLineModel> popupChatLineList;

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

        final String word = this.getIntent().getStringExtra("word");

        titleTV.setText("단어 : \"" + word+"\"");
        freqTV.setText("빈도 : " + wordDao.getFreqWordListSearch(word).getFrequency() + "회");

        popupChatLineList = wordDao.getChatLinesContainingWord(word);
        WordListAdapter ca = new WordListAdapter(popupChatLineList);
        chatLinesLV.setAdapter(ca);

        chatLinesLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewz, int position, long id) {
//                DialogUtils du = new DialogUtils(WordDetailAnalyseActivity.this, chatLineDao.getSurroundingChatLines(popupChatLineList.get(position).getId()));
//                du.setHighlightText(popupChatLineList.get(position));
//                du.openDialog();
                ChatSnippetData csd = new ChatSnippetData();
                csd.setClm(chatLineDao.getSurroundingChatLines(popupChatLineList.get(position).getId()));
                csd.setHighlightChatLine(popupChatLineList.get(position));
                Intent statsIntent = new Intent(WordDetailAnalyseActivity.this, ChatPeekActivity.class);
                statsIntent.putExtra("chatSnippetData", csd);
                WordDetailAnalyseActivity.this.startActivity(statsIntent);
            }
        });
        
        freqPieChart.setData(getWordUserFreqPieData(word));

        Typeface tf = ResourcesCompat.getFont(this, R.font.nanum_square_round_r);

        freqPieChart.setCenterTextTypeface(tf);
        freqPieChart.setCenterText(generateCenterSpannableText(tf));
        freqPieChart.setCenterTextSize(14);

        freqPieChart.setExtraOffsets(20.f, 20.f, 20.f, 20.f);

        freqPieChart.setDrawHoleEnabled(true);
        freqPieChart.setHoleColor(this.getResources().getColor(R.color.lightBrown));

        freqPieChart.setTransparentCircleColor(Color.WHITE);
        freqPieChart.setTransparentCircleAlpha(110);

        freqPieChart.setHoleRadius(58f);
        freqPieChart.setTransparentCircleRadius(61f);

        freqPieChart.setDrawCenterText(true);
        freqPieChart.setMinAngleForSlices(10f);

        freqPieChart.setEntryLabelColor(Color.BLACK);
        freqPieChart.setEntryLabelTextSize(12);
        freqPieChart.setEntryLabelTypeface(tf);

        freqPieChart.getDescription().setEnabled(false);
        freqPieChart.setDragDecelerationFrictionCoef(0.95f);

        freqPieChart.setHighlightPerTapEnabled(false);

        freqPieChart.highlightValues(null);

        freqPieChart.invalidate();

        freqPieChart.setDrawEntryLabels(true);

        //freqPieChart.set

        Legend l = freqPieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(false);
        
        
        //freqPieChart.animateXY(2000, 2000);
        freqPieChart.spin(500, freqPieChart.getRotationAngle(), freqPieChart.getRotationAngle() + 180, Easing.EaseInOutCubic);
    }

    private SpannableString generateCenterSpannableText(Typeface tf) {
        SpannableString s = new SpannableString("사용 빈도");
        s.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
        return s;
    }


    class WordListAdapter extends BaseAdapter {
        List<ChatLineModel> wordFreqArrList;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 a h:m", Locale.KOREAN);

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
            TextView sentenceTV = convertView.findViewById(R.id.chatLineElemContentsTV);
            TextView dtlTV = convertView.findViewById(R.id.chatLineElemDescTV);

            String dtlStr = wordFreqArrList.get(position).getAuthor() + ", " + sdf.format(wordFreqArrList.get(position).getDate());

            sentenceTV.setText(wordFreqArrList.get(position).getContent());
            dtlTV.setText(dtlStr);

            return convertView;
        }
    }

    public PieData getWordUserFreqPieData(String word){
        ArrayList chatAmountArrayList = new ArrayList();

        List<StringIntPair> authorFrequencyList = wordDao.getFreqWordListSearchByAuthor(word);

        //calculate total
        int totalCount = 0;
        for (StringIntPair sip : authorFrequencyList) {
            totalCount += sip.getFrequency();
        }

        for (StringIntPair sip : authorFrequencyList) {
            chatAmountArrayList.add(new PieEntry(sip.getFrequency(),sip.getword() + "(" + String.format("%.1f", (double)sip.getFrequency()/totalCount*100) + "%)"));
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