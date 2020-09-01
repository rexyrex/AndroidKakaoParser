package com.rexyrex.kakaoparser.Fragments.main;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.rexyrex.kakaoparser.Activities.PersonListActivity;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonAnalyseFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonAnalyseFrag extends Fragment {
    private static final String ARG_PARAM1 = "param1";

    ChatData cd;

    NumberFormat numberFormat;

    public PersonAnalyseFrag() {
        // Required empty public constructor
    }

    public static PersonAnalyseFrag newInstance() {
        PersonAnalyseFrag fragment = new PersonAnalyseFrag();
        Bundle args = new Bundle();
        //args.putParcelable(ARG_PARAM1, cd);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            numberFormat = NumberFormat.getInstance();
            numberFormat.setGroupingUsed(true);
            cd = ChatData.getInstance();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_person_analyse, container, false);

        PieChart chatAmountPieChart = view.findViewById(R.id.chatAmountPieChart);

        chatAmountPieChart.setData(getChatAmountPieData());

        Typeface tf = ResourcesCompat.getFont(getActivity(), R.font.nanum_square_round_r);

        chatAmountPieChart.setCenterTextTypeface(tf);
        chatAmountPieChart.setCenterText(generateCenterSpannableText(tf));
        chatAmountPieChart.setCenterTextSize(20);

        chatAmountPieChart.setExtraOffsets(20.f, 20.f, 20.f, 20.f);

        chatAmountPieChart.setDrawHoleEnabled(true);
        chatAmountPieChart.setHoleColor(getActivity().getResources().getColor(R.color.lightBrown));

        chatAmountPieChart.setTransparentCircleColor(Color.WHITE);
        chatAmountPieChart.setTransparentCircleAlpha(110);

        chatAmountPieChart.setHoleRadius(58f);
        chatAmountPieChart.setTransparentCircleRadius(61f);

        chatAmountPieChart.setDrawCenterText(true);
        chatAmountPieChart.setMinAngleForSlices(10f);

        chatAmountPieChart.setEntryLabelColor(Color.BLACK);
        chatAmountPieChart.setEntryLabelTextSize(12);
        chatAmountPieChart.setEntryLabelTypeface(tf);

        chatAmountPieChart.getDescription().setEnabled(false);
        chatAmountPieChart.setDragDecelerationFrictionCoef(0.95f);

        chatAmountPieChart.setHighlightPerTapEnabled(false);

        chatAmountPieChart.highlightValues(null);

        chatAmountPieChart.invalidate();

        chatAmountPieChart.setDrawEntryLabels(true);

        //chatAmountPieChart.set

        Legend l = chatAmountPieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(false);

        //chatAmountPieChart.animateXY(1000, 1000);
        chatAmountPieChart.spin(1000, chatAmountPieChart.getRotationAngle(), chatAmountPieChart.getRotationAngle() + 360, Easing.EaseInOutCubic);

        ListView freqLV = view.findViewById(R.id.pafPersonFreqLV);

        int totalCount = cd.getChatLineCount();

        CustomAdapter customAdapter = new CustomAdapter(cd.getTop10Chatters(), totalCount);
        freqLV.setAdapter(customAdapter);

        return view;
    }

    private SpannableString generateCenterSpannableText(Typeface tf) {
        SpannableString s = new SpannableString("대화량");
        s.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
        return s;
    }

    public PieData getChatAmountPieData(){
        List<StringIntPair> chatters = cd.getTop10Chatters();

        ArrayList chatAmountArrayList = new ArrayList();
        ArrayList chatNicknameArrayList = new ArrayList();
        int totalCount = cd.getChatLineCount();

        for(StringIntPair chatter : chatters){
            chatAmountArrayList.add(new PieEntry(chatter.getFrequency(),chatter.getword() + "(" + String.format("%.1f", (double)chatter.getFrequency()/totalCount*100) + "%)"));
            chatNicknameArrayList.add(chatter);
        }
        PieDataSet dataSet = new PieDataSet(chatAmountArrayList, "채팅 비율");

        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setSliceSpace(4);
        dataSet.setValueLineColor(Color.BLACK);

//        dataSet.setValueLinePart1OffsetPercentage(80.f);
//        dataSet.setValueLinePart1Length(0.2f);
//        dataSet.setValueLinePart2Length(0.4f);

//        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
//        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData pieData = new PieData(dataSet);

        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.BLACK);
        //pieData.setValueTypeface(tf);

        return pieData;
    }

    class CustomAdapter extends BaseAdapter {

        List<StringIntPair> pairs;
        int totalCount;

        CustomAdapter(List<StringIntPair> pairs, int totalCount){
            this.pairs = pairs;
            this.totalCount = totalCount;
        }

        @Override
        public int getCount() {
            return pairs.size()+1;
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
            if(position == pairs.size()){
                convertView = getLayoutInflater().inflate(R.layout.list_view_elem_show_more_btn, null);
                Button seeMoreBtn = convertView.findViewById(R.id.showMoreBtn);
                seeMoreBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent moreIntent = new Intent(PersonAnalyseFrag.this.getActivity(), PersonListActivity.class);
                        PersonAnalyseFrag.this.getActivity().startActivity(moreIntent);
                    }
                });
                return convertView;
            }

            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_person_frequency, null);

            TextView titleTV = convertView.findViewById(R.id.personFreqElemTitleTV);
            TextView valueTV = convertView.findViewById(R.id.personFreqElemFreqTV);

            titleTV.setText(position+1 + ". "+ pairs.get(position).getword());
            valueTV.setText(numberFormat.format(pairs.get(position).getFrequency()) + " (" + String.format("%.1f", (double)pairs.get(position).getFrequency()/totalCount*100) + "%)");

            return convertView;
        }
    }
}