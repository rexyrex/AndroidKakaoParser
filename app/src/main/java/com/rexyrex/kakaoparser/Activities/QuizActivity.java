package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.Entities.StringBoolPair;
import com.rexyrex.kakaoparser.Entities.StringIntPair;
import com.rexyrex.kakaoparser.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class QuizActivity extends AppCompatActivity {

    ChatData cd;
    private MainDatabase database;
    ChatLineDAO chatLineDAO;

    TextView qTV;
    TextView qMainTV;
    ListView answersLV;

    AnswersListAdapter ala;

    ArrayList<StringBoolPair> answersList;

    /**
     * 누구의 대화? 예시 3개
     *
     * "xx"문구를 제일 많이 사용한 사람?
     *
     * x <- 이사람이 가장 많이 쓴 문구는?
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        answersLV = findViewById(R.id.quizAnswersList);
        qMainTV = findViewById(R.id.quizQMainTV);
        qTV = findViewById(R.id.quizQTV);
        cd = ChatData.getInstance();
        database = MainDatabase.getDatabase(this);
        chatLineDAO = database.getChatLineDAO();

        answersList = new ArrayList<StringBoolPair>();
        ala = new AnswersListAdapter(answersList);
        answersLV.setAdapter(ala);

        answersLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(answersList.get(i).isBool()){
                    Toast.makeText(QuizActivity.this, "맞아요! ㅎㅎ", Toast.LENGTH_SHORT).show();
                    getNextQuestion();
                } else {
                    Toast.makeText(QuizActivity.this, "틀렸어요! ㅠㅠ", Toast.LENGTH_SHORT).show();
                    getNextQuestion();
                }
            }
        });

        getNextQuestion();
    }

    protected void resetForNextQuestion(){
        qMainTV.setText("");
        qTV.setText("");

    }

    protected void getNextQuestion(){
        answersList.clear();
        int randChatIndex = ThreadLocalRandom.current().nextInt(0, cd.getChatLineCount()-4);
        ChatLineModel qCLM = chatLineDAO.getItemById((long) randChatIndex);
        ChatLineModel qCLM2 = chatLineDAO.getItemById((long) randChatIndex + 1);
        ChatLineModel qCLM3 = chatLineDAO.getItemById((long) randChatIndex + 2);
        ChatLineModel aCLM = chatLineDAO.getItemById((long) (randChatIndex + 3));

        qMainTV.setText("아래 대화에 이어지는 답변을 고르시오");

        final int MAX_CONTENT_LENGTH = 100;

        qTV.setText(
                "1. [" + qCLM.getAuthor() + "] " + qCLM.getShortenedContent(MAX_CONTENT_LENGTH) +
                        "\n\n2. [" + qCLM2.getAuthor() + "] " + qCLM2.getShortenedContent(MAX_CONTENT_LENGTH) +
                        "\n\n3. [" + qCLM3.getAuthor() + "] " + qCLM3.getShortenedContent(MAX_CONTENT_LENGTH)
                );

        //add real answer
        StringBoolPair sbp = new StringBoolPair("[" + aCLM.getAuthor() + "] " + aCLM.getShortenedContent(MAX_CONTENT_LENGTH), true);
        answersList.add(sbp);

        //add fake answers
        for(int i=0; i<4; i++){
            int randIndex = ThreadLocalRandom.current().nextInt(0, cd.getChatLineCount()-4);
            while(randIndex >= randChatIndex && randIndex <= randChatIndex + 3){
                randIndex = ThreadLocalRandom.current().nextInt(0, cd.getChatLineCount()-4);
            }

            ChatLineModel fakeCLM = chatLineDAO.getItemById((long) randIndex);
            StringBoolPair sbpFake = new StringBoolPair("[" + fakeCLM.getAuthor() + "] " + fakeCLM.getShortenedContent(MAX_CONTENT_LENGTH), false);
            answersList.add(sbpFake);
        }

        long seed = System.nanoTime();
        Collections.shuffle(answersList, new Random(seed));

        ala.notifyDataSetChanged();
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

            StringBoolPair wordData = ansList.get(position);

            answerTitleTV.setText(letters[position]);
            answerContentTV.setText(wordData.getStr());
            return convertView;
        }
    }
}