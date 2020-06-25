package com.rexyrex.kakaoparser.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFrequencyFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFrequencyFrag extends Fragment {
    private static final String ARG_PARAM1 = "param1";

    private ChatData cd;

    public ChatFrequencyFrag() {
        // Required empty public constructor
    }

    public static ChatFrequencyFrag newInstance(ChatData cd) {
        ChatFrequencyFrag fragment = new ChatFrequencyFrag();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, cd);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cd = getArguments().getParcelable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_frequency, container, false);

        PieChart chatAmountPieChart = view.findViewById(R.id.chatAmountPieChart);

        chatAmountPieChart.setData(cd.getChatAmountPieData());
        chatAmountPieChart.animateXY(3000, 3000);
        return view;
    }
}