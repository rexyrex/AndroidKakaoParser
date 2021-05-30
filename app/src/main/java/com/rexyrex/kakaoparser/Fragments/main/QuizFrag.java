package com.rexyrex.kakaoparser.Fragments.main;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.util.SharedPreferencesUtils;
import com.rexyrex.kakaoparser.Activities.MainActivity;
import com.rexyrex.kakaoparser.Activities.QuizActivity;
import com.rexyrex.kakaoparser.Database.DAO.AnalysedChatDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.AnalysedChatModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.HighscoreData;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.FirebaseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;
import com.rexyrex.kakaoparser.Utils.StringParseUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuizFrag extends Fragment implements FirebaseUtils.NicknameCallback, FirebaseUtils.HighscoreCallback {
    ChatData cd;
    AnalysedChatModel acm;
    TextView quizScoreTV;

    SharedPrefUtils spu;

    Dialog nicknameDialog, highscoreDialog;
    Button ndCancelBtn, ndEnterBtn;
    TextView ndErrorMsgTv;
    EditText ndNickET;

    ListView highscoreLV;
    List<HighscoreData> highscoreDataList;
    CustomAdapter ca;

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
            cd = ChatData.getInstance();
            acm = cd.getChatAnalyseDbModel();
            spu = new SharedPrefUtils(getContext());

            highscoreDialog = new Dialog(getContext());
            highscoreDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            highscoreDialog.setContentView(R.layout.quiz_highscore_popup);
            highscoreDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAndFadeOut;

            highscoreLV = highscoreDialog.findViewById(R.id.quizHighscoreLV);
            highscoreDataList = new ArrayList<>();
            ca = new CustomAdapter(highscoreDataList);
            highscoreLV.setAdapter(ca);

            nicknameDialog = new Dialog(getContext());
            nicknameDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            nicknameDialog.setContentView(R.layout.quiz_nickname_creation);
            nicknameDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAndFadeOut;
            nicknameDialog.setCancelable(false);

            ndCancelBtn = nicknameDialog.findViewById(R.id.quizNicknameCancelBtn);
            ndEnterBtn = nicknameDialog.findViewById(R.id.quizNicknameEnterBtn);
            ndErrorMsgTv = nicknameDialog.findViewById(R.id.quizNicknameErrorTV);
            ndNickET = nicknameDialog.findViewById(R.id.quizNicknameET);

            ndErrorMsgTv.setText("");

            InputFilter filter = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end,
                                           Spanned dest, int dstart, int dend) {
                    for (int i = start; i < end; i++) {
                        if (!Character.isLetterOrDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
                }
            };

            ndNickET.setFilters(new InputFilter[] { filter });

            ndCancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ndNickET.setText("");
                    ndErrorMsgTv.setText("");
                    nicknameDialog.cancel();
                }
            });

            ndEnterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String attemptedNickname = ndNickET.getText().toString();
                    if(attemptedNickname.length() > 30 || attemptedNickname.length() < 2){
                        ndErrorMsgTv.setText("길이가 2글자 이상 30글자 이하여야 합니다.");
                    } else {
                        FirebaseUtils.setNicknameCallback(QuizFrag.this);
                        FirebaseUtils.nicknameExists(attemptedNickname);
                    }
                }
            });

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
                    Toast.makeText(QuizFrag.this.getActivity(), "대화 내용이 너무 짧아요", Toast.LENGTH_LONG).show();
                } else {
                    //Check if nickname exists on device
                    if(spu.getString(R.string.SP_QUIZ_NICKNAME, "-1").equals("-1")){
                        //nickname does not exist
                        nicknameDialog.show();
                    } else {
                        //Nickname exists -> Continue on with quiz
                        Intent moreIntent = new Intent(QuizFrag.this.getActivity(), QuizActivity.class);
                        startActivityForResult(moreIntent, 77);
                    }
                }
            }
        });

        Button quizInstructionsBtn = view.findViewById(R.id.quizInstructionsBtn);
        Button quizRankingBtn = view.findViewById(R.id.quizRankingBtn);

        quizRankingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                highscoreDialog.show();
                FirebaseUtils.setHighscoreCallback(QuizFrag.this);
                FirebaseUtils.getHighscores();
            }
        });

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

    @Override
    public void getNickname(String nickname) {
        if(nickname.equals("-1")){
            ndErrorMsgTv.setText("등록 완료");
            //save nickname to firebase
            FirebaseUtils.saveNickname(ndNickET.getText().toString(), spu);

            //save nickname locally
            spu.saveString(R.string.SP_QUIZ_NICKNAME, ndNickET.getText().toString());

            nicknameDialog.cancel();

            Intent moreIntent = new Intent(QuizFrag.this.getActivity(), QuizActivity.class);
            startActivityForResult(moreIntent, 77);
        } else {
            ndErrorMsgTv.setText("이미 등록되어있는 닉네임 입니다.");
        }
    }

    @Override
    public void getHighscores(List<HighscoreData> highscores) {
        highscoreDataList.clear();
        for(int i=0; i<highscores.size(); i++){
            highscoreDataList.add(highscores.get(i));
        }
        ca.notifyDataSetChanged();
    }

    class CustomAdapter extends BaseAdapter {
        List<HighscoreData> highscores;

        CustomAdapter(List<HighscoreData> highscores){
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
            TextView nickTV = convertView.findViewById(R.id.quizHighscoreLVElemNicknameTV);
            TextView scoreTV = convertView.findViewById(R.id.quizHighscoreLVElemScoreTV);

            nickTV.setText("" + (position+1) + ". " + highscores.get(position).getNickname());
            scoreTV.setText("" + highscores.get(position).getHighscore());

            return convertView;
        }
    }
}