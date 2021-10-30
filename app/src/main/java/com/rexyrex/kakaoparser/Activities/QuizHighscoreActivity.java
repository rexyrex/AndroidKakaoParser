package com.rexyrex.kakaoparser.Activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rexyrex.kakaoparser.Database.DAO.AnalysedChatDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.AnalysedChatModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.HighscoreData;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FirebaseUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class QuizHighscoreActivity extends AppCompatActivity implements FirebaseUtils.HighscoreCallback {

    List<HighscoreData> highscoreDataList;
    SharedPrefUtils spu;
    HighscoreAdapter ha;
    ListView highscoreLV;

    ConstraintLayout cl;
    View line1, line2;
    TextView myDescTV, myTitleTV, myScoreTV, mainListDescTV;

    ChatData cd;
    private MainDatabase database;
    AnalysedChatDAO analysedChatDAO;

    Button monthlyBtn, allTimeBtn;

    boolean isMy = false;

    boolean isMonthly = true;

    int month;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_highscore);

        isMy = getIntent().getBooleanExtra("my", false);
        spu = new SharedPrefUtils(this);

        if(isMy){
            spu.incInt(R.string.SP_QUIZ_SEE_MY_RANKING_COUNT);
        } else {
            spu.incInt(R.string.SP_QUIZ_SEE_ONLINE_RANKING_COUNT);
        }

        database = MainDatabase.getDatabase(this);
        cd = ChatData.getInstance(this);
        analysedChatDAO = database.getAnalysedChatDAO();


        highscoreDataList = new ArrayList<>();

        highscoreDataList.add(new HighscoreData(0, "불러오는중..."));

        myDescTV = findViewById(R.id.highlightedQuizHighscoreLayoutDescText);
        myTitleTV = findViewById(R.id.highlightedQuizHighscoreTitleTV);
        myScoreTV = findViewById(R.id.highlightedQuizHighscoreValueTV);
        mainListDescTV = findViewById(R.id.quizHighscoreLVDescText);
        line1 = findViewById(R.id.quizFirstLine);
        line2 = findViewById(R.id.quizSecondLine);
        cl = findViewById(R.id.highlightedQuizHighscoreLayout);
        monthlyBtn = findViewById(R.id.thisMonthRankingBtn);
        allTimeBtn = findViewById(R.id.allTimeRankingBtn);

        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        month = calendar.get(Calendar.MONTH) + 1;

        monthlyBtn.setText(month + "월 랭킹");

//        if(!isMy){
//            titleTV.setText("온라인 랭킹");
//        } else {
//            titleTV.setText("나의 점수");
//        }

        hideShowSomeUi(false);

        highscoreLV = findViewById(R.id.quizHighscoreLV);
        ha = new HighscoreAdapter(highscoreDataList);
        highscoreLV.setAdapter(ha);

        monthlyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMonthly) return;
                isMonthly = true;
                updateBtn();
                loadData();
            }
        });

        allTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isMonthly) return;
                isMonthly = false;
                updateBtn();
                loadData();
            }
        });

        updateBtn();
        FirebaseUtils.setHighscoreCallback(this);
        loadData();
    }

    public void updateBtn(){
        if(isMonthly){
            monthlyBtn.setBackground(getDrawable(R.drawable.ranking_tab_btn_selected));
            allTimeBtn.setBackground(getDrawable(R.drawable.ranking_tab_btn_released));
        } else {
            monthlyBtn.setBackground(getDrawable(R.drawable.ranking_tab_btn_released));
            allTimeBtn.setBackground(getDrawable(R.drawable.ranking_tab_btn_selected));
        }
    }

    public void loadData(){
        highscoreDataList.clear();
        highscoreDataList.add(new HighscoreData(0, "불러오는중..."));
        ha.notifyDataSetChanged();
        FirebaseUtils.getHighscores(isMonthly, this, spu);
    }

    public void hideShowSomeUi(boolean show){
        myDescTV.setVisibility(show ? View.VISIBLE : View.GONE);
        myTitleTV.setVisibility(show ? View.VISIBLE : View.GONE);
        myScoreTV.setVisibility(show ? View.VISIBLE : View.GONE);
        mainListDescTV.setVisibility(show ? View.VISIBLE : View.GONE);
        line1.setVisibility(show ? View.VISIBLE : View.GONE);
        line2.setVisibility(show ? View.VISIBLE : View.GONE);
        cl.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void getHighscores(List<HighscoreData> highscores, int myScore) {
        highscoreDataList.clear();
        if(!isMy){
            //온라인 랭킹
            for(int i=0; i<highscores.size(); i++){
                highscoreDataList.add(highscores.get(i));
            }
            ha.notifyDataSetChanged();

            int myRank = 0;
            //get my rank
            for(int i=0; i<highscoreDataList.size(); i++){
                if(highscoreDataList.get(i).getNickname().equals(spu.getString(R.string.SP_QUIZ_NICKNAME, "-1"))){
                    myRank = i+1;
                    break;
                }
            }
            hideShowSomeUi(true);

            myDescTV.setText("나의 랭킹");
            myTitleTV.setText("" + (myRank == 0 ? "?" : (""+myRank)) + ". " + spu.getString(R.string.SP_QUIZ_NICKNAME, "<등록안됨>"));
            if(isMonthly){
                if(myRank!=0){
                    myScoreTV.setText(""+ myScore);
                } else {
                    myScoreTV.setText("0");
                }

                mainListDescTV.setText(month + "월 랭킹 (Top 100)");
            } else {
                myScoreTV.setText(""+ spu.getInt(R.string.SP_QUIZ_ALL_TIME_HIGH_SCORE, 0));
                mainListDescTV.setText("전체 랭킹 (Top 100)");
            }

        } else {
            //개인 채팅 랭킹
            List<AnalysedChatModel> allAnalysedChatsList = analysedChatDAO.getItemsByScore();
            for(AnalysedChatModel acm : allAnalysedChatsList){
                highscoreDataList.add(new HighscoreData(acm.getHighscore(), acm.getTitle() + " [" + acm.getDt() + "]"));
            }
            ha.notifyDataSetChanged();
            int myRank = 0;
            //get my rank
            for(int i=0; i<highscoreDataList.size(); i++){
                if(highscoreDataList.get(i).getNickname().equals(cd.getChatAnalyseDbModel().getTitle() + " [" + cd.getChatAnalyseDbModel().getDt() + "]")){
                    myRank = i+1;
                    break;
                }
            }
            hideShowSomeUi(true);

            myDescTV.setText("선택 채팅");
            myTitleTV.setText("" + (myRank == 0 ? "?" : (""+myRank)) + ". " + cd.getChatAnalyseDbModel().getTitle() + " [" + cd.getChatAnalyseDbModel().getDt() + "]");
            myScoreTV.setText(""+ cd.getChatAnalyseDbModel().getHighscore());
            mainListDescTV.setText("모든 채팅");
        }

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

            if( !isMy && highscores.get(position).getNickname().equals(spu.getString(R.string.SP_QUIZ_NICKNAME, "-1"))){
                cl.setBackground(QuizHighscoreActivity.this.getDrawable(R.drawable.custom_show_more_btn_highlighted));
            } else if( isMy && highscoreDataList.get(position).getNickname().equals(cd.getChatAnalyseDbModel().getTitle() + " [" + cd.getChatAnalyseDbModel().getDt() + "]")){
                cl.setBackground(QuizHighscoreActivity.this.getDrawable(R.drawable.custom_show_more_btn_highlighted));
            }

            nickTV.setText("" + (position+1) + ". " + highscores.get(position).getNickname());
            scoreTV.setText("" + highscores.get(position).getHighscore());

            return convertView;
        }
    }
}