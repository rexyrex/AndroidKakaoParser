package com.rexyrex.kakaoparser.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rexyrex.kakaoparser.R;

import org.w3c.dom.Text;

public class InstructionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        TextView noChatHelpTV = findViewById(R.id.stillNoChatTV);

        noChatHelpTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://rexyrex.com/kakaoParserErrorHelp1"));
                startActivity(browserIntent);
            }
        });
    }
}