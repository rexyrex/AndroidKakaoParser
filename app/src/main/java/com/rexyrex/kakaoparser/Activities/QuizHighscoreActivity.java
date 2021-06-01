package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.rexyrex.kakaoparser.Entities.HighscoreData;
import com.rexyrex.kakaoparser.Fragments.main.QuizFrag;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.List;

public class QuizHighscoreActivity extends AppCompatActivity {

    List<HighscoreData> highscoreDataList;
    SharedPrefUtils spu;
    HighscoreAdapter ha;
    ListView highscoreLV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_highscore);

        spu = new SharedPrefUtils(this);

        highscoreDataList = (List<HighscoreData>) getIntent().getSerializableExtra("highscoreDataList");

        highscoreLV = findViewById(R.id.quizHighscoreLV);
        ha = new HighscoreAdapter(highscoreDataList);
        highscoreLV.setAdapter(ha);

    }


    class HighscoreAdapter extends BaseAdapter {
        List<HighscoreData> highscores;

        HighscoreAdapter(List<HighscoreData> highscores){
            this.highscores = highscores;
        }

        @Override
        public int getCount() {
            return highscores.size();
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
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_quiz_highscore, null);
            ConstraintLayout cl = convertView.findViewById(R.id.quizHighscoreCV);

            TextView nickTV = convertView.findViewById(R.id.quizHighscoreLVElemNicknameTV);
            TextView scoreTV = convertView.findViewById(R.id.quizHighscoreLVElemScoreTV);

            if( highscores.get(position).getNickname().equals(spu.getString(R.string.SP_QUIZ_NICKNAME, "-1"))){
                cl.setBackground(QuizHighscoreActivity.this.getDrawable(R.drawable.custom_show_more_btn_highlighted));
            }

            nickTV.setText("" + (position+1) + ". " + highscores.get(position).getNickname());
            scoreTV.setText("" + highscores.get(position).getHighscore());

            return convertView;
        }
    }
}