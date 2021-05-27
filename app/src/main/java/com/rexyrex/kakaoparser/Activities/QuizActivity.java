package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringBoolPair;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class QuizActivity extends AppCompatActivity {

    ChatData cd;
    private MainDatabase database;
    ChatLineDAO chatLineDAO;
    WordDAO wordDAO;

    final int MAX_CONTENT_LENGTH = 72;

    TextView qTV, qMainTV, qTimerTV;
    ListView answersLV;

    AnswersListAdapter ala;

    ArrayList<StringBoolPair> answersList;

    CountDownTimer cTimer = null;

    boolean isQuestionTime = false;
    boolean isCorrect = false;
    String choiceStr = "";
    ArrayList<String> answerStrList;

    Button shareBtn, showAnsBtn, nextQuestionBtn;

    int triesLeft = 2;
    int score = 0;

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
        showAnsBtn = findViewById(R.id.quizShowAnsBtn);

        cd = ChatData.getInstance();
        database = MainDatabase.getDatabase(this);
        chatLineDAO = database.getChatLineDAO();
        wordDAO = database.getWordDAO();

        answerStrList = new ArrayList<>();

        answersList = new ArrayList<>();
        ala = new AnswersListAdapter(answersList);
        answersLV.setAdapter(ala);

        answersLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                choiceStr = answersList.get(i).getStr();
                isCorrect = answersList.get(i).isBool();
                if(answersList.get(i).isBool()){
                    score++;
                    Toast.makeText(QuizActivity.this, "정답! ㅎㅎ", Toast.LENGTH_SHORT).show();
                    showAnswerAndMoveOnToNextQuestion();
                } else {
                    triesLeft--;
                    Toast.makeText(QuizActivity.this, "틀렸어요! ㅠㅠ", Toast.LENGTH_SHORT).show();
                    if(triesLeft < 0){
                        resetForNextQuestion();
                        qTimerTV.setText("최종 점수 : " + score);
                    } else {
                        showAnswerAndMoveOnToNextQuestion();
                    }
                }
                nextQuestionBtn.setEnabled(true);
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

    protected void showAnswerAndMoveOnToNextQuestion(){
        isQuestionTime = false;
        ala.notifyDataSetChanged();
        nextQuestionBtn.setEnabled(true);
    }

    protected void moveToNextQuestion(){
        qTimerTV.setText("점수 : " + score + ", 남은 회수 : " + triesLeft);

        int questionType = ThreadLocalRandom.current().nextInt(0, 5);
        isQuestionTime = true;
        resetForNextQuestion();
//        getNextQuestion5();
                switch(questionType){
                    case 0: getNextQuestion(); break;
                    case 1: getNextQuestion2(); break;
                    case 2: getNextQuestion3(); break;
                    case 3: getNextQuestion4(); break;
                    case 4: getNextQuestion5(); break;
                    default : getNextQuestion(); break;
                }
        long seed = System.nanoTime();
        Collections.shuffle(answersList, new Random(seed));
        ala.notifyDataSetChanged();
    }

    protected void resetForNextQuestion(){
        qMainTV.setText("");
        qTV.setText("");
        answerStrList.clear();
        answersList.clear();
        ala.notifyDataSetChanged();
    }

    protected void getNextQuestion5(){
        //SELECT word from top 100 most used
        List<StringIntPair> wordFreqList = wordDAO.getFreqWordListForQuiz();
        int randWordIndex = ThreadLocalRandom.current().nextInt(0, wordFreqList.size());

        String word = wordFreqList.get(randWordIndex).getword();

        qMainTV.setText("아래 단어를 가장 많이 사용한 사람은?");
        qTV.setText(word);

        //SELECT TOP people who used this word
        List<StringIntPair> authorList = wordDAO.getFreqWordListSearchByAuthor(word);

        //There might be multiple people who have used this word the same number of times
        int maxUsed = authorList.get(0).getFrequency();

        for(int i=0; i< (authorList.size() > 5 ? 5 : authorList.size()); i++){
            StringBoolPair sbp = new StringBoolPair(authorList.get(i).getword(), authorList.get(i).getFrequency() == maxUsed);
            if(authorList.get(i).getFrequency() == maxUsed){
                answerStrList.add(authorList.get(i).getword());
            }
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
        int randAuthorIndex = ThreadLocalRandom.current().nextInt(0, authorList.size());
        String author = authorList.get(randAuthorIndex);

        qMainTV.setText("아래 사람이 단어 목록 중 가장 많이 사용한 단어는?");
        qTV.setText(author);

        //Get 5 words used by author
        List<StringIntPair> wordFreqList = wordDAO.getFreqWordListRandomSamplesByAuthor(author);

        //Get max freq of word and set answers accordingly
        int maxFreq = 0;
        for(StringIntPair sip : wordFreqList){
            if(sip.getFrequency() > maxFreq) maxFreq = sip.getFrequency();
        }

        for(StringIntPair sip: wordFreqList){
            StringBoolPair sbp = new StringBoolPair(sip.getword(), sip.getFrequency() == maxFreq);
            answersList.add(sbp);
            if(sip.getFrequency() == maxFreq){
                answerStrList.add(sip.getword());
            }
        }
    }

    //Show person -> pick chat
    protected void getNextQuestion3(){
        //select a person
        List<String> authorList = new ArrayList<>();
        for(String s : cd.getAuthorsList()){
            authorList.add(s);
        }
        int randAuthorIndex = ThreadLocalRandom.current().nextInt(0, authorList.size());
        String author = authorList.get(randAuthorIndex);
        authorList.remove(randAuthorIndex);

        qMainTV.setText("아래 표시된 사람의 대화를 고르시오");
        qTV.setText(author);


        //Get Correct Answer
        ChatLineModel sampleSent = chatLineDAO.getChatterRandomChatlineSample(author);

        StringBoolPair sbp = new StringBoolPair(sampleSent.getContent(), true);
        answerStrList.add(sbp.getStr());
        answersList.add(sbp);

        //Get Wrong Answers
        List<ChatLineModel> wrongSentList = chatLineDAO.getOtherRandomChatlineSamples(author, sampleSent.getContent());
        for(ChatLineModel wrongSent : wrongSentList){
            StringBoolPair sbp2 = new StringBoolPair(wrongSent.getContent(), false);
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
        int randAuthorIndex = ThreadLocalRandom.current().nextInt(0, authorList.size());
        String author = authorList.get(randAuthorIndex);

        //if author does not have at least 5 chats, reselect author
        while(chatLineDAO.getChatterChatLineCount(author) < 5){
            randAuthorIndex = ThreadLocalRandom.current().nextInt(0, authorList.size());
            author = authorList.get(randAuthorIndex);
        }
        authorList.remove(randAuthorIndex);

        //get 3 chatLines of that person
        List<ChatLineModel> sampleSentsList = chatLineDAO.getChatterRandomChatlineSamples(author);

        StringBoolPair sbp = new StringBoolPair(author, true);
        answerStrList.add(sbp.getStr());
        answersList.add(sbp);

        qMainTV.setText("아래 대화를 한 사람을 고르시오");
        String sampleText = "";
        for(int i=0; i<sampleSentsList.size(); i++){
            sampleText += (i+1) + ". " + sampleSentsList.get(i).getShortenedContent(MAX_CONTENT_LENGTH);
            if(i < sampleSentsList.size()-1){
                sampleText+= "\n\n";
            }
        }
        qTV.setText(sampleText);

        long seed = System.nanoTime();
        Collections.shuffle(authorList, new Random(seed));

        int authorLimitCount = 0;
        //get list of other people as wrong answer
        for(String wrongAuthor : authorList){
            StringBoolPair sbp2 = new StringBoolPair(wrongAuthor, false);
            answersList.add(sbp2);
            authorLimitCount++;
            if(authorLimitCount >= 4){
                break;
            }
        }
    }

    //Show 3 consecutive chats -> pick next chat
    protected void getNextQuestion(){
        int randChatIndex = ThreadLocalRandom.current().nextInt(0, cd.getChatLineCount()-6);
        ChatLineModel qCLM = chatLineDAO.getItemById((long) randChatIndex);
        ChatLineModel qCLM2 = chatLineDAO.getItemById((long) randChatIndex + 1);
        ChatLineModel qCLM3 = chatLineDAO.getItemById((long) randChatIndex + 2);
        ChatLineModel qCLM4 = chatLineDAO.getItemById((long) randChatIndex + 3);
        ChatLineModel qCLM5 = chatLineDAO.getItemById((long) randChatIndex + 4);

        ChatLineModel aCLM = chatLineDAO.getItemById((long) (randChatIndex + 5));

        qMainTV.setText("아래 대화에 이어지는 답변을 고르시오");
        qTV.setText(
                "1. [" + qCLM.getAuthor() + "] " + qCLM.getShortenedContent(MAX_CONTENT_LENGTH) +
                        "\n\n2. [" + qCLM2.getAuthor() + "] " + qCLM2.getShortenedContent(MAX_CONTENT_LENGTH) +
                        "\n\n3. [" + qCLM3.getAuthor() + "] " + qCLM3.getShortenedContent(MAX_CONTENT_LENGTH) +
                        "\n\n4. [" + qCLM4.getAuthor() + "] " + qCLM4.getShortenedContent(MAX_CONTENT_LENGTH) +
                        "\n\n5. [" + qCLM5.getAuthor() + "] " + qCLM5.getShortenedContent(MAX_CONTENT_LENGTH)
                );

        //add real answer
        StringBoolPair sbp = new StringBoolPair("[" + aCLM.getAuthor() + "] " + aCLM.getShortenedContent(MAX_CONTENT_LENGTH), true);
        answerStrList.add(sbp.getStr());
        answersList.add(sbp);

        //add fake answers
        for(int i=0; i<4; i++){
            int randIndex = ThreadLocalRandom.current().nextInt(0, cd.getChatLineCount()-4);
            while(randIndex >= randChatIndex && randIndex <= randChatIndex + 5){
                randIndex = ThreadLocalRandom.current().nextInt(0, cd.getChatLineCount()-4);
            }

            ChatLineModel fakeCLM = chatLineDAO.getItemById((long) randIndex);
            StringBoolPair sbpFake = new StringBoolPair("[" + fakeCLM.getAuthor() + "] " + fakeCLM.getShortenedContent(MAX_CONTENT_LENGTH), false);
            answersList.add(sbpFake);
        }
    }

    class AnswersListAdapter extends BaseAdapter {
        ArrayList<StringBoolPair> ansList;
        String[] letters = {"A","B","C","D","E"};

        AnswersListAdapter(ArrayList<StringBoolPair> ansList){
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

            for(String ans : answerStrList){
                if(!isQuestionTime && ansList.get(position).getStr().equals(ans)){
                    convertView.setBackgroundColor(getResources().getColor(R.color.lightGreen, QuizActivity.this.getTheme()));
                }
            }



            if(!isCorrect && !isQuestionTime && ansList.get(position).getStr().equals(choiceStr)){
                convertView.setBackgroundColor(getResources().getColor(R.color.design_default_color_error, QuizActivity.this.getTheme()));
            }

            StringBoolPair wordData = ansList.get(position);

            answerTitleTV.setText(letters[position]);
            answerContentTV.setText(wordData.getStr());
            return convertView;
        }
    }
}