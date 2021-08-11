package com.rexyrex.kakaoparser.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.rexyrex.kakaoparser.Database.DAO.AnalysedChatDAO;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.QuizChoiceData;
import com.rexyrex.kakaoparser.Entities.StringBoolPair;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.Fragments.main.QuizFrag;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.AdUtils;
import com.rexyrex.kakaoparser.Utils.FirebaseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.RandomUtils;
import com.rexyrex.kakaoparser.Utils.ShareUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuizActivity extends AppCompatActivity {

    public enum QuestionType {
        NEXT_CHAT, PERSON_FROM_CHAT, CHAT_FROM_PERSON, WORD_FROM_PERSON, PERSON_FROM_WORD, DEFAULT
    }

    //Scoring : 100 * questionTypeScoreMultiplier * chatAndChattersMultiplier
    final double[] questionTypeScoreMultiplier = {1, 0.7, 0.8, 1.1, 0.9};
    double defaultChatLengthBonus = 10;
    double defaultChatterCountBonus = 10;
    double chatLengthmultiplier;
    double chatterCountMultiplier;

    QuestionType lastQuestionType;

    final String[] letters = {"A","B","C","D","E"};

    ChatData cd;
    private MainDatabase database;
    SharedPrefUtils spu;
    ChatLineDAO chatLineDAO;
    WordDAO wordDAO;
    AnalysedChatDAO analysedChatDAO;

    final int MAX_CONTENT_LENGTH = 500;

    TextView qTV, qMainTV, qTimerTV, qTVLengthWarningTV;
    ListView answersLV;

    AnswersListAdapter ala;

    ArrayList<QuizChoiceData> answersList;

    CountDownTimer cTimer = null;

    boolean isQuestionTime = false;
    boolean isCorrect = false;
    String choiceStr = "";

    Button shareBtn, nextQuestionBtn;

    Dialog resDialog, finalDialog;
    ImageView resDialogIV;
    TextView resDialogTV;
    TextView finalDialogTitleTV;

    TextView finalDialogCurrentQuizScoreTV;

    ConstraintLayout finalDialogLocalHighScoreCL;
    TextView finalDialogLocalHighScoreTitleTV, finalDialogLocalHighScoreValueTV;
    TextView finalDialogLocalScoreDescTV;

    ConstraintLayout finalDialogOnlineScoreCL;
    TextView finalDialogOnlineScoreTitleTV, finalDialogOnlineScoreValueTV;
    TextView finalDialogOnlineScoreDescTV;

    Button finalDialogCloseBtn, finalDialogMyRankingBtn, finalDialogOnlineRankingBtn;

    int triesLeft = 3;
    int score = 0;
    int scoreAddition = 0;

    ChatLineModel aCLM;

    String questionStr, questionExtraStr;

    FrameLayout adContainer;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    private AdRequest adRequest;

    /**
     * "xx"문구를 제일 많이 사용한 사람? -> 단어 보여주고 사람 고르기
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        //ad
        adRequest = new AdRequest.Builder().build();
        loadAd();

        //banner ad
        adContainer = findViewById(R.id.adView);
        mAdView = new AdView(this);
        mAdView.setAdUnitId(getString(R.string.AdMob_ad_unit_ID_Banner_Quiz));
        adContainer.addView(mAdView);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.setAdSize(AdUtils.getAdSize(this));
        mAdView.loadAd(adRequest);


        answersLV = findViewById(R.id.quizAnswersList);
        qMainTV = findViewById(R.id.quizQMainTV);
        qTV = findViewById(R.id.quizQTV);
        qTVLengthWarningTV = findViewById(R.id.quizLongQTVNoticeTV);
        qTVLengthWarningTV.setVisibility(View.GONE);
        qTimerTV = findViewById(R.id.quizTimerTV);

        nextQuestionBtn = findViewById(R.id.quizNextQuestionBtn);
        shareBtn = findViewById(R.id.quizShareBtn);

        lastQuestionType = QuestionType.DEFAULT;

        aCLM = null;

        cd = ChatData.getInstance();
        database = MainDatabase.getDatabase(this);
        spu = new SharedPrefUtils(this);
        chatLineDAO = database.getChatLineDAO();
        wordDAO = database.getWordDAO();
        analysedChatDAO = database.getAnalysedChatDAO();

        answersList = new ArrayList<>();

        ala = new AnswersListAdapter(answersList);
        answersLV.setAdapter(ala);

        chatLengthmultiplier =Math.log10(Math.max(Math.min(cd.getChatLineCount()/2000, 10), 1));
        chatterCountMultiplier = Math.log10(cd.getChatterCount());

        qTV.setMovementMethod(ScrollingMovementMethod.getInstance());

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareQuestion();
            }
        });

        resDialog = new Dialog(this);
        resDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        resDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        resDialog.setContentView(R.layout.quiz_res_popup);
        resDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAndFadeOut;
        resDialog.setCancelable(false);

        finalDialog = new Dialog(this);
        finalDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        finalDialog.setContentView(R.layout.quiz_complete_popup);
        finalDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAndFadeOut;
        finalDialog.setCancelable(false);

        finalDialogCurrentQuizScoreTV = finalDialog.findViewById(R.id.quizCompleteCurrentScoreTV);

        finalDialogLocalHighScoreCL = finalDialog.findViewById(R.id.quizLocalHighScoreCL);
        finalDialogLocalHighScoreTitleTV = finalDialog.findViewById(R.id.quizLocalScoreTitleTV);
        finalDialogLocalHighScoreValueTV = finalDialog.findViewById(R.id.quizLocalScoreValueTV);
        finalDialogLocalScoreDescTV = finalDialog.findViewById(R.id.quizLocalHighScoreCLSubTextTV);

        finalDialogOnlineScoreCL = finalDialog.findViewById(R.id.quizOnlineScoreCL);
        finalDialogOnlineScoreTitleTV = finalDialog.findViewById(R.id.quizOnlineScoreTitleTV);
        finalDialogOnlineScoreValueTV = finalDialog.findViewById(R.id.quizOnlineScoreValueTV);
        finalDialogOnlineScoreDescTV = finalDialog.findViewById(R.id.quizOnlineScoreCLSubTextTV);

        finalDialogTitleTV = finalDialog.findViewById(R.id.quizCompleteTitleTV);
        finalDialogCloseBtn = finalDialog.findViewById(R.id.quizCompleteCloseBtn);
        finalDialogMyRankingBtn = finalDialog.findViewById(R.id.quizCompleteMyRankingBtn);
        finalDialogOnlineRankingBtn = finalDialog.findViewById(R.id.quizCompleteOnlineRankBtn);

        finalDialogCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QuizActivity.this.finish();
            }
        });

        finalDialogMyRankingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuizActivity.this, QuizHighscoreActivity.class);
                intent.putExtra("my", true);
                startActivity(intent);
            }
        });

        finalDialogOnlineRankingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QuizActivity.this, QuizHighscoreActivity.class);
                intent.putExtra("my", false);
                startActivity(intent);
            }
        });

        resDialogIV = resDialog.findViewById(R.id.quizPopupResImg);
        resDialogTV = resDialog.findViewById(R.id.quizPopupResTV);

        qTimerTV.setText("점수 : " + score + ", 남은 기회 : " + triesLeft);

        answersLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(!isQuestionTime){
                    return;
                }
                choiceStr = answersList.get(i).getStr();
                isCorrect = answersList.get(i).isCorrect();
                if(answersList.get(i).isCorrect()){
                    scoreAddition = (int) ((100 + (chatLengthmultiplier * defaultChatLengthBonus) + (chatterCountMultiplier * defaultChatterCountBonus)) * questionTypeScoreMultiplier[lastQuestionType.ordinal()]);
                    score+= scoreAddition;
                    showResDialog(true);
                    spu.incInt(R.string.SP_QUIZ_CORRECT_COUNT);
                    switch(lastQuestionType){
                        case PERSON_FROM_CHAT: spu.incInt(R.string.SP_QUIZ_Q2_CORRECT_COUNT); break;
                        case CHAT_FROM_PERSON: spu.incInt(R.string.SP_QUIZ_Q3_CORRECT_COUNT); break;
                        case WORD_FROM_PERSON: spu.incInt(R.string.SP_QUIZ_Q4_CORRECT_COUNT); break;
                        case PERSON_FROM_WORD: spu.incInt(R.string.SP_QUIZ_Q5_CORRECT_COUNT); break;
                        default : spu.incInt(R.string.SP_QUIZ_Q1_CORRECT_COUNT); break;
                    }
                } else {
                    triesLeft--;
                    showResDialog(false);
                    spu.incInt(R.string.SP_QUIZ_WRONG_COUNT);
                }
                showAnswer();
                toggleButton(nextQuestionBtn,true);
            }
        });

        nextQuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moveToNextQuestion();
            }
        });

        //startTimer();
        spu.incInt(R.string.SP_QUIZ_START_COUNT);
        moveToNextQuestion();
    }

    protected void showResDialog(boolean success){
        if(success){
            resDialogIV.setImageDrawable(getDrawable(R.drawable.correct));
            resDialogTV.setText("정답! (+" + scoreAddition + "점)");
        } else {
            resDialogIV.setImageDrawable(getDrawable(R.drawable.incorrect));
            resDialogTV.setText("오답 (남은 기회 : "+triesLeft + ")");
        }
        resDialog.show();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                resDialog.dismiss();
            }
        }, 1200);
    }

//    //start timer function
//    void startTimer() {
//        cTimer = new CountDownTimer(60000, 1000) {
//            public void onTick(long millisUntilFinished) {
//                qTimerTV.setText(Math.round(millisUntilFinished / 1000) + "초");
//            }
//            public void onFinish() {
//                qTimerTV.setText("끝");
//            }
//        };
//        cTimer.start();
//    }
//
//    //cancel timer
//    void cancelTimer() {
//        if(cTimer!=null)
//            cTimer.cancel();
//    }

    @Override
    protected void onDestroy() {
        //cancelTimer();
        finalDialog.dismiss();
        resDialog.dismiss();
        super.onDestroy();
    }

    protected void toggleButton(Button btn, boolean toggle){
        btn.setClickable(toggle);
        btn.setEnabled(toggle);
    }

    protected void showAnswer(){
        if(triesLeft < 1) {
            qTimerTV.setText("최종 점수 : " + score);
            nextQuestionBtn.setText("결과 보기");
            spu.incInt(R.string.SP_QUIZ_FINISH_COUNT);
        } else {
            qTimerTV.setText("점수 : " + score + ", 남은 기회 : " + triesLeft);
        }
        isQuestionTime = false;
        ala.notifyDataSetChanged();
    }

    protected void shareQuestion(){
        String shareStr = "";
        shareStr += "문제:\n";
        shareStr += questionStr + "\n";
        shareStr += "---------\n";
        shareStr += questionExtraStr + "\n";
        shareStr += "---------\n\n";
        shareStr += "보기:\n";
        shareStr += "---------\n";

        List<String> tmpAnswerStrArr = new ArrayList<>();

        for(int i=0; i<answersList.size(); i++){
            shareStr += letters[i] + ". " + answersList.get(i).getStr() + "\n\n";
            tmpAnswerStrArr.add(letters[i] + ". " + answersList.get(i).getStr());
        }
        shareStr += "---------\n";
        spu.incInt(R.string.SP_QUIZ_SHARE_QUESTION_COUNT);
        ShareUtils.shareGeneralWithPromo(this, shareStr);

        FirebaseUtils.saveShareQuizQuestion(questionStr, questionExtraStr, tmpAnswerStrArr, spu, cd);
    }

    protected void moveToNextQuestion(){
        spu.incInt(R.string.SP_QUIZ_LOAD_QUESTION_COUNT);

        if(triesLeft < 1){
            //End Condition -> display final dialog
            finalDialogCurrentQuizScoreTV = finalDialog.findViewById(R.id.quizCompleteCurrentScoreTV);
            finalDialogLocalHighScoreTitleTV = finalDialog.findViewById(R.id.quizLocalScoreTitleTV);
            finalDialogLocalHighScoreValueTV = finalDialog.findViewById(R.id.quizLocalScoreValueTV);
            finalDialogOnlineScoreTitleTV = finalDialog.findViewById(R.id.quizOnlineScoreTitleTV);
            finalDialogOnlineScoreValueTV = finalDialog.findViewById(R.id.quizOnlineScoreValueTV);


            finalDialogTitleTV.setText("[" + cd.getChatFileTitle() + "] 퀴즈");
            finalDialogCurrentQuizScoreTV.setText("최종 점수 : " + score + "점");

            int prevHighScore = cd.getChatAnalyseDbModel().getHighscore();
            //Compare current score with local highscore
            if(prevHighScore < score){
                cd.getChatAnalyseDbModel().setHighscore(score);
                analysedChatDAO.update(cd.getChatAnalyseDbModel());

                finalDialogLocalHighScoreCL.setBackground(getDrawable(R.drawable.quiz_choice_correct));
                finalDialogLocalScoreDescTV.setTextColor(getColor(R.color.lightGreen));
                finalDialogLocalScoreDescTV.setText("기록 갱신!");
                finalDialogLocalHighScoreValueTV.setText("" + prevHighScore + "->" + score);
            } else {
                finalDialogLocalHighScoreCL.setBackground(getDrawable(R.drawable.quiz_choice_white));
//                finalDialogLocalScoreDescTV.setTextColor(getColor(R.color.design_default_color_error));
//                finalDialogLocalScoreDescTV.setText("갱신 실패");
                finalDialogLocalScoreDescTV.setVisibility(View.GONE);

                finalDialogLocalHighScoreValueTV.setText("" + prevHighScore);
//                finalDialogLocalHighScoreTitleTV.setTextColor(getColor(R.color.white));
//                finalDialogLocalHighScoreValueTV.setTextColor(getColor(R.color.white));
            }

            int onlineHighScore = spu.getInt(R.string.SP_QUIZ_ALL_TIME_HIGH_SCORE, 0);
            if(onlineHighScore < score){
                spu.saveInt(R.string.SP_QUIZ_ALL_TIME_HIGH_SCORE, score); //save locally
                FirebaseUtils.saveHighscore(score, spu, cd); //save to firebase

                finalDialogOnlineScoreCL.setBackground(getDrawable(R.drawable.quiz_choice_correct));
                finalDialogOnlineScoreDescTV.setTextColor(getColor(R.color.lightGreen));
                finalDialogOnlineScoreDescTV.setText("기록 갱신!");
                finalDialogOnlineScoreValueTV.setText("" + onlineHighScore + "->" + score);
            } else {
                finalDialogOnlineScoreCL.setBackground(getDrawable(R.drawable.quiz_choice_white));
//                finalDialogOnlineScoreDescTV.setTextColor(getColor(R.color.design_default_color_error));
//                finalDialogOnlineScoreDescTV.setText("갱신 실패");
                finalDialogOnlineScoreDescTV.setVisibility(View.GONE);

                finalDialogOnlineScoreValueTV.setText("" + onlineHighScore);
//                finalDialogOnlineScoreTitleTV.setTextColor(getColor(R.color.white));
//                finalDialogOnlineScoreValueTV.setTextColor(getColor(R.color.white));
            }
            finalDialog.show();
            if(mInterstitialAd!=null){
                mInterstitialAd.show(QuizActivity.this);
            }
            return;
        }

        toggleButton(nextQuestionBtn, false);

        int questionType = RandomUtils.getRandomInt(0, 5);
        isQuestionTime = true;
        resetForNextQuestion();
//        lastQuestionType = QuestionType.NEXT_CHAT;
//        getNextQuestion();
        switch(questionType){
            case 1: getNextQuestion2(); lastQuestionType = QuestionType.PERSON_FROM_CHAT; spu.incInt(R.string.SP_QUIZ_Q2_TOTAL_COUNT); break;
            case 2: getNextQuestion3(); lastQuestionType = QuestionType.CHAT_FROM_PERSON; spu.incInt(R.string.SP_QUIZ_Q3_TOTAL_COUNT); break;
            case 3: getNextQuestion4(); lastQuestionType = QuestionType.WORD_FROM_PERSON; spu.incInt(R.string.SP_QUIZ_Q4_TOTAL_COUNT); break;
            case 4: getNextQuestion5(); lastQuestionType = QuestionType.PERSON_FROM_WORD; spu.incInt(R.string.SP_QUIZ_Q5_TOTAL_COUNT); break;
            default : getNextQuestion(); lastQuestionType = QuestionType.NEXT_CHAT; spu.incInt(R.string.SP_QUIZ_Q1_TOTAL_COUNT); break;
        }
        if(qTV.getLineCount() > qTV.getMaxLines()){
            qTVLengthWarningTV.setVisibility(View.VISIBLE);
        } else {
            qTVLengthWarningTV.setVisibility(View.GONE);
        }
        long seed = System.nanoTime();
        Random r = new Random(seed);
        Collections.shuffle(answersList, r);
        ala.notifyDataSetChanged();
    }

    protected void resetForNextQuestion(){
        qMainTV.setText("");
        qTV.setText("");
        qTV.scrollTo(0,0);
        answersList.clear();
        ala.notifyDataSetChanged();
    }

    //Person from Word
    protected void getNextQuestion5(){
        //SELECT word from top 100 most used
        //word must be used by at least 2 people
        List<StringIntPair> wordFreqList = wordDAO.getFreqWordListForQuiz();
        int randWordIndex = RandomUtils.getRandomInt(0, wordFreqList.size());

        String word = wordFreqList.get(randWordIndex).getword();

        questionStr = "아래 단어를 가장 많이 사용한 사람은?";
        questionExtraStr = word;
        qMainTV.setText(questionStr);
        qTV.setText(questionExtraStr);

        //SELECT TOP people who used this word
        List<StringIntPair> authorList = wordDAO.getFreqWordListSearchByAuthor(word);

        //There might be multiple people who have used this word the same number of times
        int maxUsed = authorList.get(0).getFrequency();

        for(int i=0; i< (authorList.size() > 5 ? 5 : authorList.size()); i++){
            QuizChoiceData sbp = new QuizChoiceData(
                    authorList.get(i).getFrequency() == maxUsed,
                    authorList.get(i).getword(),
                    "[" + authorList.get(i).getFrequency() + "회] " + authorList.get(i).getword()
                    );
            answersList.add(sbp);
        }
    }

    //Show person -> pick most used word
    protected void getNextQuestion4(){
        //select a person
        List<String> authorList = new ArrayList<>();
        for(String s : cd.getAuthorsList()){
            authorList.add(s);
        }
        int randAuthorIndex = RandomUtils.getRandomInt(0, authorList.size());
        String author = authorList.get(randAuthorIndex);

        questionStr = "아래 사람이 단어 목록 중 가장 많이 사용한 단어는?";
        questionExtraStr = author;
        qMainTV.setText(questionStr);
        qTV.setText(questionExtraStr);

        //Get 5 words used by author
        List<StringIntPair> wordFreqList = wordDAO.getFreqWordListRandomSamplesByAuthor(author);

        //Get max freq of word and set answers accordingly
        int maxFreq = 0;
        for(StringIntPair sip : wordFreqList){
            if(sip.getFrequency() > maxFreq) maxFreq = sip.getFrequency();
        }

        for(StringIntPair sip: wordFreqList){
            QuizChoiceData sbp = new QuizChoiceData(sip.getFrequency() == maxFreq, sip.getword(), "[" + sip.getFrequency() + "회] " + sip.getword());
            answersList.add(sbp);
        }
    }

    //Show person -> pick chat
    protected void getNextQuestion3(){
        //select a person
        List<String> authorList = new ArrayList<>();
        for(String s : cd.getAuthorsList()){
            authorList.add(s);
        }
        int randAuthorIndex = RandomUtils.getRandomInt(0, authorList.size());
        String author = authorList.get(randAuthorIndex);

        authorList.remove(randAuthorIndex);

        questionStr = "아래 표시된 사람의 대화를 고르시오";
        questionExtraStr = author;
        qMainTV.setText(questionStr);
        qTV.setText(questionExtraStr);

        //Get Correct Answer
        ChatLineModel sampleSent = chatLineDAO.getChatterRandomChatlineSample(author);

        QuizChoiceData sbp = new QuizChoiceData(
                true,
                sampleSent.getContent(),
                "[" + sampleSent.getAuthor() + "] " + sampleSent.getContent()
        );
        answersList.add(sbp);

        //Get Wrong Answers
        List<ChatLineModel> wrongSentList = chatLineDAO.getOtherRandomChatlineSamples(author, sampleSent.getContent());
        for(ChatLineModel wrongSent : wrongSentList){
            QuizChoiceData sbp2 = new QuizChoiceData(false, wrongSent.getContent(), "[" + wrongSent.getAuthor() + "] " + wrongSent.getContent());
            answersList.add(sbp2);
        }
    }

    //Show 5 chats -> Pick Person
    protected void getNextQuestion2(){
        //select a person
        List<String> authorList = new ArrayList<>();
        for(String s : cd.getAuthorsList()){
            authorList.add(s);
        }
        int randAuthorIndex = RandomUtils.getRandomInt(0, authorList.size());
        String author = authorList.get(randAuthorIndex);

        //if author does not have at least 5 chats, reselect author
        while(chatLineDAO.getChatterChatLineCount(author) < 5){
            randAuthorIndex = RandomUtils.getRandomInt(0, authorList.size());
            author = authorList.get(randAuthorIndex);
        }
        authorList.remove(randAuthorIndex);

        //get 3 chatLines of that person
        List<ChatLineModel> sampleSentsList = chatLineDAO.getChatterRandomChatlineSamples(author);

        QuizChoiceData sbp = new QuizChoiceData(true, author, author);
        answersList.add(sbp);

        String sampleText = "";
        for(int i=0; i<sampleSentsList.size(); i++){
            sampleText += (i+1) + ". " + sampleSentsList.get(i).getShortenedContent(MAX_CONTENT_LENGTH);
            if(i < sampleSentsList.size()-1){
                sampleText+= "\n\n";
            }
        }

        questionStr = "아래 대화를 한 사람을 고르시오";
        questionExtraStr = sampleText;
        qMainTV.setText(questionStr);
        qTV.setText(questionExtraStr);

        long seed = System.nanoTime();
        Collections.shuffle(authorList, new Random(seed));

        int authorLimitCount = 0;
        //get list of other people as wrong answer
        for(String wrongAuthor : authorList){
            QuizChoiceData sbp2 = new QuizChoiceData(false, wrongAuthor, wrongAuthor);
            answersList.add(sbp2);
            authorLimitCount++;
            if(authorLimitCount >= 4){
                break;
            }
        }
    }

    //Show 3 consecutive chats -> pick next chat
    protected void getNextQuestion(){
        int randChatIndex = RandomUtils.getRandomInt(0, cd.getChatLineCount()-6);
        ChatLineModel qCLM = chatLineDAO.getItemById((long) randChatIndex);
        ChatLineModel qCLM2 = chatLineDAO.getItemById((long) randChatIndex + 1);
        ChatLineModel qCLM3 = chatLineDAO.getItemById((long) randChatIndex + 2);
        ChatLineModel qCLM4 = chatLineDAO.getItemById((long) randChatIndex + 3);
        ChatLineModel qCLM5 = chatLineDAO.getItemById((long) randChatIndex + 4);

        aCLM = chatLineDAO.getItemById((long) (randChatIndex + 5));

        questionStr = "아래 대화에 이어지는 답변을 고르시오";
        questionExtraStr = "1. [" + qCLM.getAuthor() + "] " + qCLM.getShortenedContent(MAX_CONTENT_LENGTH) +
                "\n\n2. [" + qCLM2.getAuthor() + "] " + qCLM2.getShortenedContent(MAX_CONTENT_LENGTH) +
                "\n\n3. [" + qCLM3.getAuthor() + "] " + qCLM3.getShortenedContent(MAX_CONTENT_LENGTH) +
                "\n\n4. [" + qCLM4.getAuthor() + "] " + qCLM4.getShortenedContent(MAX_CONTENT_LENGTH) +
                "\n\n5. [" + qCLM5.getAuthor() + "] " + qCLM5.getShortenedContent(MAX_CONTENT_LENGTH);
        qMainTV.setText(questionStr);
        qTV.setText(questionExtraStr);

        //add real answer
        QuizChoiceData sbp = new QuizChoiceData(
                true,
                "[" + aCLM.getAuthor() + "] " + aCLM.getShortenedContent(MAX_CONTENT_LENGTH),
                "[" + aCLM.getAuthor() + "] " + aCLM.getShortenedContent(MAX_CONTENT_LENGTH));

        answersList.add(sbp);

        //add fake answers
        for(int i=0; i<4; i++){
            int randIndex = RandomUtils.getRandomInt(0, cd.getChatLineCount()-4);
            while(randIndex >= randChatIndex && randIndex <= randChatIndex + 5){
                randIndex = RandomUtils.getRandomInt(0, cd.getChatLineCount()-4);
            }

            ChatLineModel fakeCLM = chatLineDAO.getItemById((long) randIndex);
            QuizChoiceData sbpFake = new QuizChoiceData(
                    false,
                    "[" + fakeCLM.getAuthor() + "] " + fakeCLM.getShortenedContent(MAX_CONTENT_LENGTH),
                    "[" + fakeCLM.getAuthor() + "] " + fakeCLM.getShortenedContent(MAX_CONTENT_LENGTH)
                    );
            answersList.add(sbpFake);
        }
    }

    protected void makeShallowCopy(ArrayList<StringBoolPair> a, ArrayList<StringBoolPair> b){
        for(int i=0; i<a.size(); i++){
            b.add(a.get(i));
        }
    }

    private void loadAd(){
        InterstitialAd.load(QuizActivity.this,getString(R.string.AdMob_ad_unit_Interstitial_Quiz_Finish), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;

                        LogUtils.e("Ad Load success");

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when fullscreen content is dismissed.
                                LogUtils.e("The ad was dismissed.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                LogUtils.e("The ad failed to show.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when fullscreen content is shown.
                                // Make sure to set your reference to null so you don't
                                // show it a second time.
                                mInterstitialAd = null;
                                LogUtils.e("The ad was shown.");
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        mInterstitialAd = null;
                        LogUtils.e("AD LOAD FAIL : " + loadAdError.toString());
                    }
                });
    }

    class AnswersListAdapter extends BaseAdapter {
        ArrayList<QuizChoiceData> ansList;


        AnswersListAdapter(ArrayList<QuizChoiceData> ansList){
            this.ansList = ansList;
        }

        @Override
        public int getCount() {
            return ansList.size();
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
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_quiz_answer, null);
            TextView answerTitleTV = convertView.findViewById(R.id.quizAnswerTitle);
            TextView answerContentTV = convertView.findViewById(R.id.quizAnswerContent);

            if(!isQuestionTime && ansList.get(position).isCorrect()){
                convertView.setBackground(getResources().getDrawable(R.drawable.quiz_choice_correct, QuizActivity.this.getTheme()));
            }

            if(!isCorrect && !isQuestionTime && ansList.get(position).getStr().equals(choiceStr)){
                convertView.setBackground(getResources().getDrawable(R.drawable.quiz_choice_incorrect, QuizActivity.this.getTheme()));
            }

            answerTitleTV.setText(letters[position]);
            answerContentTV.setText(isQuestionTime ? ansList.get(position).getStr() : ansList.get(position).getAnswerStr());
            return convertView;
        }
    }
}