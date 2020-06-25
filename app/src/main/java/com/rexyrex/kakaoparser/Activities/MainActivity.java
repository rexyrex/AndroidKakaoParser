package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.StringParseUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView chatLV;
    File[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Intent statsIntent = new Intent(MainActivity.this, ChatStatsTabActivity.class);
        //MainActivity.this.startActivity(statsIntent);

        chatLV = findViewById(R.id.chatLV);

        String folderPath = Environment.getExternalStorageDirectory()
                + File.separator + "KakaoTalk/Chats/";
        File dir = new File(folderPath);
        File pFile = new File("");
        if (dir.isDirectory()) {
            files = dir.listFiles();

            CustomAdapter customAdapter = new CustomAdapter(files);
            chatLV.setAdapter(customAdapter);

            chatLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent statsIntent = new Intent(MainActivity.this, ChatStatsTabActivity.class);
                    statsIntent.putExtra("chat", files[position]);
                    MainActivity.this.startActivity(statsIntent);
                }
            });

//            for (File file : files) {
//                if (!file.getPath().contains("Not_Found")) {
//                    LogUtils.e( "list " + file.getName());
//                }
//            }
        }
    }



    class CustomAdapter extends BaseAdapter {

        File[] chatFiles;

        CustomAdapter(File[] chatFiles){
            this.chatFiles = chatFiles;
        }

        @Override
        public int getCount() {
            return chatFiles.length;
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
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem, null);
            ImageView iv = convertView.findViewById(R.id.elemIV);
            TextView tv = convertView.findViewById(R.id.elemTV);
            TextView tv2 = convertView.findViewById(R.id.elemTV2);

            iv.setImageDrawable(getResources().getDrawable(R.drawable.chat_icon));
            tv.setText(StringParseUtils.chatFileNameToDate(chatFiles[position].getName()));
            tv2.setText(StringParseUtils.byteFormat(Long.toString(chatFiles[position].length())) + " bytes");

            return convertView;
        }
    }
}
