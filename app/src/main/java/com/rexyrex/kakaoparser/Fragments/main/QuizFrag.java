package com.rexyrex.kakaoparser.Fragments.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rexyrex.kakaoparser.Activities.PersonListActivity;
import com.rexyrex.kakaoparser.Activities.QuizActivity;
import com.rexyrex.kakaoparser.Activities.WordDetailAnalyseActivity;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.KeyboardUtils;
import com.rexyrex.kakaoparser.Utils.ShareUtils;

import java.util.ArrayList;
import java.util.List;

public class QuizFrag extends Fragment {
    ChatData cd;
    private MainDatabase database;
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
                    QuizFrag.this.getActivity().startActivity(moreIntent);
                }

            }
        });

        return view;
    }

}