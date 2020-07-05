package com.rexyrex.kakaoparser.Fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PersonAnalyseFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PersonAnalyseFrag extends Fragment {
    private static final String ARG_PARAM1 = "param1";

    private ChatData cd;

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
            //cd = getArguments().getParcelable(ARG_PARAM1);
            cd = ChatData.getInstance();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_person_analyse, container, false);

        PieChart chatAmountPieChart = view.findViewById(R.id.chatAmountPieChart);

        chatAmountPieChart.setData(cd.getChatAmountPieData());

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
        chatAmountPieChart.setMinAngleForSlices(5f);

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
        return view;
    }

    private SpannableString generateCenterSpannableText(Typeface tf) {
        SpannableString s = new SpannableString("대화량 (탑10)");
        s.setSpan(new StyleSpan(Typeface.BOLD), 0, s.length(), 0);
//        SpannableString s = new SpannableString("MPAndroidChart\ndeveloped by Philipp Jahoda");
//        s.setSpan(new RelativeSizeSpan(1.5f), 0, 14, 0);
//        s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length() - 15, 0);
//        s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() - 15, 0);
//        s.setSpan(new RelativeSizeSpan(.65f), 14, s.length() - 15, 0);
//        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
//        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);
        return s;
    }
}