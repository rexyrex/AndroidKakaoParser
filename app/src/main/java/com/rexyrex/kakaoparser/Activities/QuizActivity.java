package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
import com.rexyrex.kakaoparser.Utils.FirebaseUtils;
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

    final int MAX_CONTENT_LENGTH = 72;

    TextView qTV, qMainTV, qTimerTV;
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
    TextView finalDialogTitle1TV, finalDialogTitle2TV, finalDialogScore1TV, finalDialogScore2TV;
    Button finalDialogCloseBtn;

    int triesLeft = 3;
    int score = 0;
    int scoreAddition = 0;

    ChatLineModel aCLM;

    String questionStr, questionExtraStr;


    /**
     * "xx"문구를 제일 많이 사용한 사람? -> 단어 보여주고 사람 고르기
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        answersLV = findViewById(R.id.quizAnswersList);
        qMainTV = findViewById(R.id.quizQMainTV);
        qTV = findViewById(R.id.quizQTV);
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

        finalDialogScore1TV = finalDialog.findViewById(R.id.quizCompleteScoreTV);
        finalDialogScore2TV = finalDialog.findViewById(R.id.quizCompleteScore2TV);
        finalDialogTitle1TV = finalDialog.findViewById(R.id.quizCompleteTitleTV);
        finalDialogTitle2TV = finalDialog.findViewById(R.id.quizCompleteTitleTV2);
        finalDialogCloseBtn = finalDialog.findViewById(R.id.quizCompleteCloseBtn);

        finalDialogCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalDialog.dismiss();
                resDialog.dismiss();
                QuizActivity.this.finish();
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
                    showAnswer();
                } else {
                    triesLeft--;
                    showResDialog(false);
                    showAnswer();
                }
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

        for(int i=0; i<answersList.size(); i++){
            shareStr += letters[i] + ". " + answersList.get(i).getStr() + "\n\n";
        }
        shareStr += "---------\n";
        ShareUtils.shareGeneralWithPromo(this, shareStr);
    }

    protected void moveToNextQuestion(){
        int prevHighScore = cd.getChatAnalyseDbModel().getHighscore();

        if(triesLeft < 1){
            finalDialogTitle1TV.setText("[" + cd.getChatFileTitle() + "] 퀴즈");
            finalDialogScore1TV.setText("최고 기록 : " + prevHighScore + "점");
            finalDialogScore2TV.setText("이번 기록 : " + score + "점");
            finalDialog.show();

            //Local highscore
            if(prevHighScore < score){
                cd.getChatAnalyseDbModel().setHighscore(score);
                analysedChatDAO.update(cd.getChatAnalyseDbModel());
                finalDialogTitle2TV.setText("점수 갱신 성공!");

                //Check if global highscore
                if(spu.getInt(R.string.SP_QUIZ_ALL_TIME_HIGH_SCORE, 0) < score){
                    //save locally
                    spu.saveInt(R.string.SP_QUIZ_ALL_TIME_HIGH_SCORE, score);
                    //save to firebase
                    FirebaseUtils.saveHighscore(score, spu);
                }
            } else {
                finalDialogTitle2TV.setText("점수 갱신 실패!");
            }
            return;
        }

        toggleButton(nextQuestionBtn, false);


        int questionType = RandomUtils.getRandomInt(0, 5);
        isQuestionTime = true;
        resetForNextQuestion();
//        getNextQuestion5();
        switch(questionType){
            case 1: getNextQuestion2(); lastQuestionType = QuestionType.PERSON_FROM_CHAT; break;
            case 2: getNextQuestion3(); lastQuestionType = QuestionType.CHAT_FROM_PERSON; break;
            case 3: getNextQuestion4(); lastQuestionType = QuestionType.WORD_FROM_PERSON; break;
            case 4: getNextQuestion5(); lastQuestionType = QuestionType.PERSON_FROM_WORD; break;
            default : getNextQuestion(); lastQuestionType = QuestionType.NEXT_CHAT; break;
        }
        long seed = System.nanoTime();
        Random r = new Random(seed);
        Collections.shuffle(answersList, r);
        ala.notifyDataSetChanged();
    }

    protected void resetForNextQuestion(){
        qMainTV.setText("");
        qTV.setText("");
        answersList.clear();
        ala.notifyDataSetChanged();
    }

    //Person from Word
    protected void getNextQuestion5(){
        //SELECT word from top 100 most used
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

    //Show 3 chats -> Pick Person
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