package com.rexyrex.kakaoparser.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rexyrex.kakaoparser.Activities.ChatStatsTabActivity;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GeneralStatsFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeneralStatsFrag extends Fragment {


    private static final String ARG_PARAM1 = "param1";

    private ChatData cd;

    public GeneralStatsFrag() {
        // Required empty public constructor
    }

    public static GeneralStatsFrag newInstance(ChatData param1) {
        GeneralStatsFrag fragment = new GeneralStatsFrag();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, param1);

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
        View view = inflater.inflate(R.layout.fragment_general_stats, container, false);

        TextView statsChatterCountTV = view.findViewById(R.id.statsChatterCountTV);
        statsChatterCountTV.setText("" + cd.getChatterCount());

        TextView statsDateRangeTV = view.findViewById(R.id.statsDateRangeTV);
        statsDateRangeTV.setText("" + cd.getChatDateRangeStr());

        TextView statsAnalysedDayCountTV = view.findViewById(R.id.statsAnalysedDayCountTV);
        statsAnalysedDayCountTV.setText("" + cd.getChatDays());

        TextView statsAnalysedChatLineCountTV = view.findViewById(R.id.statsAnalysedChatLineCountTV);
        statsAnalysedChatLineCountTV.setText("" + cd.getChatLinesCount());

        TextView statsAnalysedTimeElapsedTV = view.findViewById(R.id.statsAnalysedTimeElapsedTV);
        statsAnalysedTimeElapsedTV.setText("" + cd.getLoadElapsedSeconds() + "초");

        return view;
    }
}