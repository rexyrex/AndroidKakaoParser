package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.PicUtils;
import com.rexyrex.kakaoparser.Utils.StringParseUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    ListView chatLV;
    File[] files;
    File[] reversedFilesArr;
    ChatData cd;

    private static long lastBackAttemptTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatLV = findViewById(R.id.chatLV);

        cd = ChatData.getInstance();
        lastBackAttemptTime = 0;

        String folderPath = Environment.getExternalStorageDirectory()
                + File.separator + "KakaoTalk/Chats/";
        File dir = new File(folderPath);
        if (dir.isDirectory()) {
            files = dir.listFiles();
            reversedFilesArr = new File[files.length];

            for(int i=0; i<files.length; i++){
                reversedFilesArr[i] = files[files.length-i-1];
            }

            //프로필 이미지
            Drawable[] profPics = new Drawable[10];
            ArrayList<Integer> profPicsIndexes = new ArrayList<>();
            HashMap<String, Drawable> titleProfilePicMap = new HashMap<>();
            for(int i=0; i<10; i++){
                profPicsIndexes.add(i);
            }
            Collections.shuffle(profPicsIndexes);
            for(int i=0; i<10; i++){
                profPics[i] = PicUtils.getProfiePic(MainActivity.this, profPicsIndexes.get(i).intValue());
            }
            int profPicIndex = 0;
            for(int i=0; i<reversedFilesArr.length; i++){
                while(profPicIndex > profPics.length -1){
                    profPicIndex -= profPics.length-1;
                }
                titleProfilePicMap.put(FileParseUtils.parseFileForTitle(reversedFilesArr[i]), profPics[profPicIndex]);
                profPicIndex++;
            }

            CustomAdapter customAdapter = new CustomAdapter(reversedFilesArr, titleProfilePicMap);
            chatLV.setAdapter(customAdapter);

            chatLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent statsIntent = new Intent(MainActivity.this, ChatStatsTabActivity.class);
                    //statsIntent.putExtra("chat", reversedFilesArr[position]);
                    cd.setChatFile(reversedFilesArr[position]);
                    MainActivity.this.startActivity(statsIntent);
                }
            });
        }
    }



    class CustomAdapter extends BaseAdapter {

        File[] chatFiles;
        HashMap<String, Drawable> titleProfilePicMap;

        CustomAdapter(File[] chatFiles, HashMap<String, Drawable> titleProfilePicMap){
            this.chatFiles = chatFiles;
            this.titleProfilePicMap = titleProfilePicMap;
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
            TextView tv = convertView.findViewById(R.id.generalStatsElemTitleTV);
            TextView tv2 = convertView.findViewById(R.id.elemTV2);
            TextView tv3 = convertView.findViewById(R.id.generalStatsElemValueTV);

            String title = FileParseUtils.parseFileForTitle(chatFiles[position]);
            iv.setImageDrawable(titleProfilePicMap.get(title));
            tv3.setText(StringParseUtils.chatFileNameToDate(chatFiles[position].getName()));
            tv.setText(title);
            tv2.setText(StringParseUtils.numberCommaFormat(Long.toString(chatFiles[position].length())) + " bytes");

            return convertView;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            promptExit();
        }
        return true;
    }

    //2초 안에 뒤로가기 버튼 2번 누를 시 앱 종료
    private void promptExit(){
        LogUtils.e("PromptExit");
        long timeNow = System.currentTimeMillis();
        long tPassed = timeNow - lastBackAttemptTime;
        if(tPassed >2000){
            lastBackAttemptTime = System.currentTimeMillis();
            Toast.makeText(this, "뒤로 버튼을 한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show();
        } else {
            finishAndRemoveTask();
        }
    }
}
