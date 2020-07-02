package com.rexyrex.kakaoparser.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.R;

public class TimeAnalyseFrag extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    private ChatData cd;

    public TimeAnalyseFrag() {
        // Required empty public constructor
    }


    public static TimeAnalyseFrag newInstance(ChatData param1) {
        TimeAnalyseFrag fragment = new TimeAnalyseFrag();
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
        View view = inflater.inflate(R.layout.fragment_time_analyse, container, false);

        return view;
    }
}