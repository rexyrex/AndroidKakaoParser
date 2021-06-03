package com.rexyrex.kakaoparser.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rexyrex.kakaoparser.Database.DAO.AnalysedChatDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Services.DeleteService;
import com.rexyrex.kakaoparser.Utils.DateUtils;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.FirebaseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.PicUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;
import com.rexyrex.kakaoparser.Utils.StringParseUtils;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ListView chatLV;
    File[] files;
    File[] reversedFilesArr;
    ChatData cd;

    LinearLayout kakaoBtn;
    LinearLayout instructionsBtn;

    ImageView settingsIV;

    NumberFormat numberFormat;

    SharedPrefUtils spu;

    MainDatabase db;
    AnalysedChatDAO acd;

    Dialog updateDialog;
    TextView updateTitleTV, updateContentsTV;
    CheckBox updateShowCheckBox;
    Button updatePopupCloseBtn;

    private static long lastBackAttemptTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatLV = findViewById(R.id.chatLV);
        settingsIV = findViewById(R.id.settingsIV);

        kakaoBtn = findViewById(R.id.openKakaoLayout);
        instructionsBtn = findViewById(R.id.instructionsLayout);

        db = MainDatabase.getDatabase(this);
        acd = db.getAnalysedChatDAO();
        spu = new SharedPrefUtils(this);

        registerReceiver(deleteReceiver, new IntentFilter("kakaoChatDelete"));

        updateDialog = new Dialog(this);
        updateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        updateDialog.setContentView(R.layout.update_contents_popup);
        updateDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAndFadeOut;
        updateDialog.setCancelable(false);

        updateTitleTV = updateDialog.findViewById(R.id.updatePopupTitleTV);
        updateContentsTV = updateDialog.findViewById(R.id.updatePopupContentsTV);
        updateShowCheckBox = updateDialog.findViewById(R.id.updatePopupSeeAgainCheckBox);
        updatePopupCloseBtn = updateDialog.findViewById(R.id.updatePopupCloseBtn);

        updateShowCheckBox.setText("다시 보지 않기");

        updateShowCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                spu.saveBool(R.string.SP_UPDATE_POPUP_SHOW, !b);
            }
        });
        
        updateTitleTV.setText("1.2.0 업데이트 내용");
        
        updateContentsTV.setText("1. 기능 추가 : 퀴즈 (Beta)\n" +
                "- 분석한 채팅을 기반 다양한 문제 생성\n" +
                "- 문제 랜덤 생성 알고리즘 적용\n" +
                "- 문제 공유 기능\n" +
                "- 온라인 점수 기록\n" +
                "- 퀴즈 실행 방법 : 분석 후 \"퀴즈\" 탭 선택\n" +
                "\n" +
                "2. 버그 수정 및 기능 개선\n" +
                "- 대화량 탭 : 이름이 길 경우 짤리는 현상 개선\n" +
                "- 대화 탭 : 채팅 버블 짤리는 현상 개선\n" +
                "- 단어,대화 탭 : 위로 가기 버튼 추가\n" +
                "- 메인 화면 : 정렬이 잘 안되는 현상 개선\n" +
                "- 채팅 분석 알고리즘 개선\n" +
                "- 앱 응답 없음 현상 개선\n" +
                "- 예상치 못한 앱 닫힘 현상 개선\n");

        updatePopupCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDialog.cancel();
            }
        });

        if(spu.getBool(R.string.SP_UPDATE_POPUP_SHOW, true)){
            updateDialog.show();
        }

        kakaoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spu.saveInt(R.string.SP_OPEN_KAKAO_COUNT, spu.getInt(R.string.SP_OPEN_KAKAO_COUNT, 0) + 1);
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.kakao.talk");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                }
            }
        });

        instructionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spu.saveInt(R.string.SP_OPEN_HOW_TO_COUNT, spu.getInt(R.string.SP_OPEN_HOW_TO_COUNT, 0) + 1);
                Intent instIntent = new Intent(MainActivity.this, InstructionsActivity.class);
                MainActivity.this.startActivity(instIntent);
            }
        });

        settingsIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spu.saveInt(R.string.SP_OPEN_SETTINGS_COUNT, spu.getInt(R.string.SP_OPEN_SETTINGS_COUNT, 0) + 1);
                Intent instIntent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(instIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(deleteReceiver);
    }

    public void loadList(){
        cd = ChatData.getInstance();
        lastBackAttemptTime = 0;

        numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);


        String folderPath = Environment.getExternalStorageDirectory()
                + File.separator + "KakaoTalk/Chats/";
        File dir = new File(folderPath);
        if (dir.isDirectory()) {
            files = dir.listFiles();
            Arrays.sort(files);
            reversedFilesArr = new File[files.length];

            //update file count
            spu.saveInt(R.string.SP_EXPORTED_CHAT_COUNT, files.length);

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
            //Collections.shuffle(profPicsIndexes);
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
                    statsIntent.putExtra("lastAnalyseDt", StringParseUtils.chatFileNameToDate(reversedFilesArr[position].getName()));
                    if(FileParseUtils.parseFileForTitle(reversedFilesArr[position]).equals(spu.getString(R.string.SP_LAST_ANALYSE_TITLE, "null"))
                    && StringParseUtils.chatFileNameToDate(reversedFilesArr[position].getName()).equals(spu.getString(R.string.SP_LAST_ANALYSE_DT, "null"))
                    ){
                        statsIntent.putExtra("analysed", true);
                    } else {
                        statsIntent.putExtra("analysed", false);
                    }

                    cd.setChatFile(reversedFilesArr[position]);
                    MainActivity.this.startActivity(statsIntent);
                }
            });

            final View delView = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.popup_delete, null);
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this, R.style.PopupStyle);
            alertBuilder.setView(delView);
            alertBuilder.setCancelable(true);
            final AlertDialog dialog = alertBuilder.create();

            final Button delBtn = delView.findViewById(R.id.delBtnTrue);
            Button delCancel = delView.findViewById(R.id.delBtnFalse);
            final TextView delPopTV = delView.findViewById(R.id.delPopMsg);



            delCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.hide();
                }
            });

            chatLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View longView, int i, long l) {

                    final int z = i;
                    final View tmpView = longView;

                    delBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            cd.setChatFile(reversedFilesArr[z]);
                            Intent serviceIntent = new Intent(MainActivity.this, DeleteService.class);
                            MainActivity.this.startService(serviceIntent);

                            spu.incInt(R.string.SP_DELETE_CHAT_COUNT);

                            TextView tv = tmpView.findViewById(R.id.elemTV3);
                            tv.setText("삭제중... (알림창 확인)");

//                            File delFile = reversedFilesArr[z];
                            Toast.makeText(MainActivity.this, "삭제가 완료되면 대화목록이 새로고침 됩니다.", Toast.LENGTH_LONG).show();
//                            deleteRecursive(delFile);
//                            loadList();
//                            Toast.makeText(MainActivity.this, "삭제 완료", Toast.LENGTH_SHORT).show();
                            dialog.hide();
                        }
                    });
                    delPopTV.setText("폴더 전체 크기 : " + FileParseUtils.humanReadableByteCountBin(FileParseUtils.getSizeRecursive(reversedFilesArr[z])) + "\n채팅 파일 크기 : " +FileParseUtils.humanReadableByteCountBin(FileParseUtils.getChatFileSize(reversedFilesArr[z])));
                    dialog.show();

                    return true;
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

        public void updateTitle(int position){

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getLayoutInflater().inflate(R.layout.list_view_elem_chat, null);
            if(FileParseUtils.parseFileForTitle(chatFiles[position]).equals(spu.getString(R.string.SP_LAST_ANALYSE_TITLE, "null"))
            && StringParseUtils.chatFileNameToDate(chatFiles[position].getName()).equals(spu.getString(R.string.SP_LAST_ANALYSE_DT, "null"))
            ){
                convertView.setBackgroundColor(getResources().getColor(R.color.colorAccent, MainActivity.this.getTheme()));
            }

            ImageView iv = convertView.findViewById(R.id.elemIV);
            TextView tv = convertView.findViewById(R.id.elemTV3);
            TextView tv2 = convertView.findViewById(R.id.elemTV2);
            TextView tv3 = convertView.findViewById(R.id.elemTV1);

            String title = FileParseUtils.parseFileForTitle(chatFiles[position]);
            iv.setImageDrawable(titleProfilePicMap.get(title));
            tv3.setText(StringParseUtils.chatFileNameToDate(chatFiles[position].getName()));
            tv.setText(title);
            //tv2.setText(StringParseUtils.numberCommaFormat(Long.toString(chatFiles[position].length())) + " bytes");
            tv2.setText(FileParseUtils.humanReadableByteCountBin(FileParseUtils.getChatFileSize((chatFiles[position]))));
            //LogUtils.e("Size: " + FileParseUtils.humanReadableByteCountBin(FileParseUtils.getChatFileSize((chatFiles[position]))));

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
        //LogUtils.e("PromptExit");
        long timeNow = System.currentTimeMillis();
        long tPassed = timeNow - lastBackAttemptTime;
        if(tPassed >2000){
            lastBackAttemptTime = System.currentTimeMillis();
            Toast.makeText(this, "뒤로 버튼을 한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show();
        } else {
            //increment logoutCount
            spu.saveInt(R.string.SP_LOGOUT_COUNT, spu.getInt(R.string.SP_LOGOUT_COUNT, 0) + 1);
            spu.saveString(R.string.SP_LOGOUT_DT, DateUtils.getCurrentTimeStr());
            List<String> chatTitleList = acd.getAllChatTitles();
            FirebaseUtils.updateUserInfo(this, spu, "Logout", chatTitleList);
            finishAndRemoveTask();
        }
    }

    BroadcastReceiver deleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadList();
            Toast.makeText(MainActivity.this, "삭제 완료 - 대화 목록이 새로고침 됐습니다", Toast.LENGTH_LONG).show();
        }
    };
}
