package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.rexyrex.kakaoparser.Entities.StringStringPair;
import com.rexyrex.kakaoparser.R;

import java.util.ArrayList;
import java.util.List;

public class QuizInstructionsActivity extends AppCompatActivity {

    ListView instructionsLV;
    List<StringStringPair> instructionsDataList;
    InstructionsAdapter ia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_instructions);

        instructionsLV = findViewById(R.id.quizInstructionsLV);
        instructionsDataList = new ArrayList<>();
        instructionsDataList.add(new StringStringPair(
                "퀴즈가 뭐에요?",
                "분석한 채팅 기준으로 무작위로 객관식 질문을 생성 후 사용자가 문제를 푸는 모드 입니다."
        ));
        instructionsDataList.add(new StringStringPair(
                "총 몇 문제가 나오나요?",
                "문제는 무작위로 생성되므로 무제한적입니다. 사용자는 기회가 3회 주어지며 3문제를 틀릴 시 퀴즈는 종료됩니다."
        ));
        instructionsDataList.add(new StringStringPair(
                "퀴즈 시작이 안됩니다",
                "퀴즈 시작 조건 미달 (채팅 최소 대화 횟수 1000, 최소 참여 인원 2 입니다)"
        ));
        instructionsDataList.add(new StringStringPair(
                "점수 계산은 어떻게 되나요?",
                "문제를 맞출때마다 점수가 오르는 양은 분석된 채팅의 길이, 참여 인원, 문제 유형을 고려하여 책정됩니다. \n" +
                        "(대화 길이가 길고, 참여인원이 많고, 문제 유형이 어려울수록 점수는 높아집니다)"
        ));
        instructionsDataList.add(new StringStringPair(
                "닉네임을 수정하고싶어요",
                "현재로서는 닉네임 수정을 허용하지 않습니다. 추후 정책이 바뀌면 공지드리겠습니다."
        ));
        instructionsDataList.add(new StringStringPair(
                "이번달 기록에 등록이 안돼요",
                "현재로서는 자신의 기록을 갱신해야 이번달 랭킹 대상이 됩니다."
        ));

        ia = new InstructionsAdapter(instructionsDataList);
        instructionsLV.setAdapter(ia);
    }

    class InstructionsAdapter extends BaseAdapter {
        List<StringStringPair> instructions;

        InstructionsAdapter(List<StringStringPair> instructions){
            this.instructions = instructions;
        }

        @Override
        public int getCount() {
            return instructions.size();
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
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_quiz_instructions, null);
            TextView qTV = convertView.findViewById(R.id.lveQTV);
            TextView aTV = convertView.findViewById(R.id.lveATV);

            qTV.setText("Q - " + instructions.get(position).getTitle());
            aTV.setText("A - " + instructions.get(position).getValue());

            return convertView;
        }
    }
}