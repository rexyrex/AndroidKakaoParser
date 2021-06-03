package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FirebaseUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

public class SendOpinionActivity extends AppCompatActivity {

    Button sendBtn;
    SharedPrefUtils spu;
    EditText opinionET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_opinion);
        spu = new SharedPrefUtils(this);
        sendBtn = findViewById(R.id.sendOpinionBtn);
        opinionET = findViewById(R.id.opinionET);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(opinionET.getText().length() < 2){
                    Toast.makeText(SendOpinionActivity.this, "내용이 너무 짧습니다", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUtils.sendOpinion(opinionET.getText().toString(), spu);
                    Toast.makeText(SendOpinionActivity.this, "접수 완료", Toast.LENGTH_SHORT).show();
                    SendOpinionActivity.this.finish();
                }

            }
        });
    }
}