package com.rexyrex.kakaoparser.Fragments.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.rexyrex.kakaoparser.Activities.QuizActivity;
import com.rexyrex.kakaoparser.Database.DAO.AnalysedChatDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.AnalysedChatModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.StringParseUtils;

public class QuizFrag extends Fragment {
    ChatData cd;
    private MainDatabase database;
    AnalysedChatModel acm;
    TextView quizScoreTV;

    public QuizFrag() {
        // Required empty public constructor
    }
    public static QuizFrag newInstance() {
        QuizFrag fragment = new QuizFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            database = MainDatabase.getDatabase(getContext());
            cd = ChatData.getInstance();
            acm = cd.getChatAnalyseDbModel();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        Button quizStartBtn = view.findViewById(R.id.quizStartBtn);
        quizStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cd.getChatLineCount() < 1000){
                    Toast.makeText(QuizFrag.this.getActivity(), "대화 내용이 너무 짧습니다. 대화를 더 하고 재분석 후 진행해주세요!", Toast.LENGTH_LONG).show();
                } else {
                    Intent moreIntent = new Intent(QuizFrag.this.getActivity(), QuizActivity.class);
                    startActivityForResult(moreIntent, 77);
                }
            }
        });

        Button quizInstructionsBtn = view.findViewById(R.id.quizInstructionsBtn);
        Button quizRankingBtn = view.findViewById(R.id.quizRankingBtn);

        quizScoreTV = view.findViewById(R.id.quizFragScoreTV);

        quizScoreTV.setText("최고 기록 (개인) : " + acm.getHighscore() + "점");
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 77){
            quizScoreTV.setText("최고 기록 (개인) : " + acm.getHighscore() + "점");
        }
    }
}