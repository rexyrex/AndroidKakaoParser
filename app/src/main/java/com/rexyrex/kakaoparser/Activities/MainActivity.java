package com.rexyrex.kakaoparser.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.rexyrex.kakaoparser.Constants.DateFormats;
import com.rexyrex.kakaoparser.Database.DAO.AnalysedChatDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Entities.ChatData;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Services.DeleteService;
import com.rexyrex.kakaoparser.Utils.AdUtils;
import com.rexyrex.kakaoparser.Utils.DateUtils;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.FirebaseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.PicUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;
import com.rexyrex.kakaoparser.Utils.StringParseUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    ListView chatLV;
    File[] files;
    File[] reversedFilesArr;
    int fileIndex = 0;

    ChatData cd;

    LinearLayout kakaoBtn;
    LinearLayout instructionsBtn;

    ImageView settingsIV;

    NumberFormat numberFormat;

    SharedPrefUtils spu;

    MainDatabase db;

    Dialog updateDialog, dateRangeDialog;
    Dialog loadingDialog;
    //AlertDialog loadingAlertDialog;
    TextView updateTitleTV, updateContentsTV;
    CheckBox updateShowCheckBox;
    Button updatePopupCloseBtn;

    TextView dateRangeStartDtTV, dateRangeEndDtTV;

    Calendar startCalendar,endCalendar,minCalendar, maxCalendar;
    String calendarType;

    Button startAnalysisBtn, loadAnalysisBtn, dateRangeBackBtn;

    AsyncTask<Integer, Void, String> loadTask;

    private static long lastBackAttemptTime;

    private InterstitialAd mInterstitialAd;
    private AdRequest adRequest;
    private AdView mAdView;
    FrameLayout adContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ad
        adRequest = new AdRequest.Builder().build();
        //banner ad
        adContainer = findViewById(R.id.adView);
        mAdView = new AdView(this);
        mAdView.setAdUnitId(getString(R.string.AdMob_ad_unit_ID_Banner_Main));
        adContainer.addView(mAdView);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.setAdSize(AdUtils.getAdSize(this));
        mAdView.loadAd(adRequest);

        //loadAd();

        chatLV = findViewById(R.id.chatLV);
        settingsIV = findViewById(R.id.settingsIV);

        kakaoBtn = findViewById(R.id.openKakaoLayout);
        instructionsBtn = findViewById(R.id.instructionsLayout);

        db = MainDatabase.getDatabase(this);
        spu = new SharedPrefUtils(this);

        startCalendar = Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        minCalendar = Calendar.getInstance();
        maxCalendar = Calendar.getInstance();
        calendarType = "";

        registerReceiver(deleteReceiver, new IntentFilter("kakaoChatDelete"));

        //View view = (LayoutInflater.from(MainActivity.this)).inflate(R.layout.loading_popup, null);

        loadingDialog = new Dialog(this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.loading_popup);
        loadingDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAndFadeOut;
        ImageView loadingIV = loadingDialog.findViewById(R.id.loadingPopupIV);
        TextView loadingTV = loadingDialog.findViewById(R.id.loadingPopupTV);
        Glide.with(this).asGif().load(R.drawable.loading1).into(loadingIV);
        loadingTV.setText("날짜 불러오는중...");

        dateRangeDialog = new Dialog(this);
        dateRangeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dateRangeDialog.setContentView(R.layout.date_range_picker_popup);
        dateRangeDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAndFadeOut;

        dateRangeStartDtTV = dateRangeDialog.findViewById(R.id.datePickPopStartDateTV);
        dateRangeEndDtTV = dateRangeDialog.findViewById(R.id.datePickPopEndDateTV);
        dateRangeBackBtn = dateRangeDialog.findViewById(R.id.datePickBackBtn);
        startAnalysisBtn = dateRangeDialog.findViewById(R.id.datePickStartAnalyseBtn);
        loadAnalysisBtn = dateRangeDialog.findViewById(R.id.datePickLoadAnalyseBtn);

        startAnalysisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //throw new RuntimeException("Test Crash"); // Force a crash
                startAnalysis();
                /*loadAd();
                if (mInterstitialAd != null) {
                    mInterstitialAd.show(MainActivity.this);
                } else {
                    LogUtils.e("The interstitial ad wasn't ready yet.");
                    startAnalysis();
                }*/
            }
        });

        loadAnalysisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                spu.incInt(R.string.SP_LOAD_COUNT);
                Intent analyseIntent = new Intent(MainActivity.this, ChatStatsTabActivity.class);
                analyseIntent.putExtra("lastAnalyseDt", StringParseUtils.chatFileNameToDate(reversedFilesArr[fileIndex].getName()));
                analyseIntent.putExtra("startDt", spu.getString(R.string.SP_LAST_ANALYSE_START_DT, "null"));
                analyseIntent.putExtra("endDt", spu.getString(R.string.SP_LAST_ANALYSE_END_DT, "null"));
                analyseIntent.putExtra("analysed", true);
                dateRangeDialog.cancel();
                cd.setChatFile(reversedFilesArr[fileIndex]);
                MainActivity.this.startActivity(analyseIntent);
            }
        });

        dateRangeBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateRangeDialog.cancel();
            }
        });

        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                if(calendarType.equals("start")){
                    startCalendar.set(Calendar.YEAR, year);
                    startCalendar.set(Calendar.MONTH, monthOfYear);
                    startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    if(startCalendar.before(minCalendar)){
                        Toast.makeText(MainActivity.this, "대화 시작점으로 설정", Toast.LENGTH_SHORT).show();
                        startCalendar = (Calendar) minCalendar.clone();
                    }

                    if(startCalendar.after(endCalendar)){
                        Toast.makeText(MainActivity.this, "분석 시작일을 종료일 이후로 설정 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        startCalendar = (Calendar) endCalendar.clone();
                    }

                    startCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    startCalendar.set(Calendar.MINUTE, 0);
                    startCalendar.set(Calendar.SECOND, 0);

                    dateRangeStartDtTV.setText(DateFormats.simpleKoreanFormat.format(startCalendar.getTime()));
                } else if(calendarType.equals("end")){
                    endCalendar.set(Calendar.YEAR, year);
                    endCalendar.set(Calendar.MONTH, monthOfYear);
                    endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    if(endCalendar.after(maxCalendar)){
                        Toast.makeText(MainActivity.this, "대화 마지막 시점으로 설정", Toast.LENGTH_SHORT).show();
                        endCalendar = (Calendar) maxCalendar.clone();
                    }

                    if(endCalendar.before(startCalendar)){
                        Toast.makeText(MainActivity.this, "분석 종료일을 시작일 이전으로 설정 할 수 없습니다.", Toast.LENGTH_SHORT).show();
                        endCalendar = (Calendar) startCalendar.clone();
                    }

                    endCalendar.set(Calendar.HOUR_OF_DAY, 23);
                    endCalendar.set(Calendar.MINUTE, 59);
                    endCalendar.set(Calendar.SECOND, 59);

                    dateRangeEndDtTV.setText(DateFormats.simpleKoreanFormat.format(endCalendar.getTime()));
                }
            }
        };

        dateRangeStartDtTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarType = "start";
                String dtStr = dateRangeStartDtTV.getText().toString();
                int[] tmpDtArr = getDateFromStr(dtStr);
                new DatePickerDialog(MainActivity.this, R.style.DatePickerTheme, dateSetListener, tmpDtArr[0], tmpDtArr[1], tmpDtArr[2]).show();
            }
        });

        dateRangeEndDtTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendarType = "end";
                String dtStr = dateRangeEndDtTV.getText().toString();
                int[] tmpDtArr = getDateFromStr(dtStr);
                new DatePickerDialog(MainActivity.this, R.style.DatePickerTheme, dateSetListener, tmpDtArr[0], tmpDtArr[1], tmpDtArr[2]).show();
            }
        });

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

        //업데이트 내용
        loadUpdateContents();

        updatePopupCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDialog.cancel();
            }
        });

        if(spu.getBool(R.string.SP_UPDATE_POPUP_SHOW, true)){
            //updateDialog.show();
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

    private void loadAd(){
        InterstitialAd.load(MainActivity.this,getString(R.string.AdMob_ad_unit_Interstitial_Chat_Tab), adRequest,
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
                                startAnalysis();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when fullscreen content failed to show.
                                LogUtils.e("The ad failed to show.");
                                startAnalysis();
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

    protected void startAnalysis(){
        Intent analyseIntent = new Intent(MainActivity.this, ChatStatsTabActivity.class);
        analyseIntent.putExtra("lastAnalyseDt", StringParseUtils.chatFileNameToDate(reversedFilesArr[fileIndex].getName()));
        analyseIntent.putExtra("startDt", DateFormats.defaultFormat.format(new Date(startCalendar.getTimeInMillis())));
        analyseIntent.putExtra("endDt", DateFormats.defaultFormat.format(new Date(endCalendar.getTimeInMillis())));
        if(FileParseUtils.parseFileForTitle(reversedFilesArr[fileIndex]).equals(spu.getString(R.string.SP_LAST_ANALYSE_TITLE, "null"))
                && StringParseUtils.chatFileNameToDate(reversedFilesArr[fileIndex].getName()).equals(spu.getString(R.string.SP_LAST_ANALYSE_DT, "null"))
                && analyseIntent.getStringExtra("startDt").equals(spu.getString(R.string.SP_LAST_ANALYSE_START_DT, "null"))
                && analyseIntent.getStringExtra("endDt").equals(spu.getString(R.string.SP_LAST_ANALYSE_END_DT, "null"))
        ){
            analyseIntent.putExtra("analysed", true);
        } else {
            analyseIntent.putExtra("analysed", false);
        }

        dateRangeDialog.cancel();
        cd.setChatFile(reversedFilesArr[fileIndex]);
        MainActivity.this.startActivity(analyseIntent);
    }

    protected void loadUpdateContents(){
        InputStream is = getResources().openRawResource(R.raw.update_1_3);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String jsonString = writer.toString();
        String resStr = "";

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray contentsArray = jsonObject.getJSONArray("contents");

            for(int i=0; i<contentsArray.length(); i++){
                JSONObject contentObject = (JSONObject) contentsArray.get(i);
                resStr += contentObject.getString("subtitle") + "\n";

                JSONArray contentDescArr = contentObject.getJSONArray("subcontents");
                for(int j=0; j<contentDescArr.length(); j++){
                    resStr += "  •  " + (String) contentDescArr.get(j) + "\n";
                }
                resStr += "\n";
            }

            updateTitleTV.setText(jsonObject.getString("title"));
            updateContentsTV.setText(resStr);

        } catch (JSONException err) {
            err.printStackTrace();
        }
    }

    //"yyyy년 M월 d일" -> [year, month, year]
    protected int[] getDateFromStr(String strDt){
        String year = strDt.split(" ")[0];
        String month = strDt.split(" ")[1];
        String day = strDt.split(" ")[2];

        int[] res = new int[3];
        res[0] = Integer.parseInt(year.substring(0, year.length()-1));
        res[1] = Integer.parseInt(month.substring(0, month.length()-1))-1;
        res[2] = Integer.parseInt(day.substring(0,day.length()-1));
        return res;
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
                    //Toast.makeText(MainActivity.this, FileParseUtils.parseFileForType(reversedFilesArr[position]), Toast.LENGTH_LONG).show();
                    cd.setChatType(FileParseUtils.parseFileForType(reversedFilesArr[position]));
                    loadingDialog.show();
                    loadTask = new AsyncTask<Integer, Void, String>() {

                        @Override
                        protected String doInBackground(Integer... integers) {
                            int position = integers[0];
                            String res = FileParseUtils.parseFileForDateRange(reversedFilesArr[position]);
                            fileIndex = position;
                            return res;
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            super.onPostExecute(s);
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dateRangeStartDtTV.setText(s.split("~")[0]);
                                    dateRangeEndDtTV.setText(s.split("~")[1]);

                                    String tmpStartDtStr = dateRangeStartDtTV.getText().toString();
                                    int[] tmpDtArr = getDateFromStr(tmpStartDtStr);
                                    minCalendar.set(Calendar.YEAR, tmpDtArr[0]);
                                    minCalendar.set(Calendar.MONTH, tmpDtArr[1]);
                                    minCalendar.set(Calendar.DAY_OF_MONTH, tmpDtArr[2]);
                                    minCalendar.set(Calendar.HOUR_OF_DAY, 0);
                                    minCalendar.set(Calendar.MINUTE, 0);
                                    minCalendar.set(Calendar.SECOND, 0);

                                    startCalendar = (Calendar) minCalendar.clone();

                                    String tmpEndDtStr = dateRangeEndDtTV.getText().toString();
                                    tmpDtArr = getDateFromStr(tmpEndDtStr);
                                    maxCalendar.set(Calendar.YEAR, tmpDtArr[0]);
                                    maxCalendar.set(Calendar.MONTH, tmpDtArr[1]);
                                    maxCalendar.set(Calendar.DAY_OF_MONTH, tmpDtArr[2]);
                                    maxCalendar.set(Calendar.HOUR_OF_DAY, 23);
                                    maxCalendar.set(Calendar.MINUTE, 59);
                                    maxCalendar.set(Calendar.SECOND, 59);

                                    endCalendar = (Calendar) maxCalendar.clone();

                                    //Enable load btn only when last analysed is available
                                    loadingDialog.cancel();

                                    if(FileParseUtils.parseFileForTitle(reversedFilesArr[fileIndex]).equals(spu.getString(R.string.SP_LAST_ANALYSE_TITLE, "null"))
                                        && StringParseUtils.chatFileNameToDate(reversedFilesArr[fileIndex].getName()).equals(spu.getString(R.string.SP_LAST_ANALYSE_DT, "null"))
                                        ){
                                        loadAnalysisBtn.setEnabled(true);
                                        loadAnalysisBtn.setBackground(getResources().getDrawable(R.drawable.custom_show_more_btn_highlighted, null));
                                        loadAnalysisBtn.setText("불러오기 \n(" +
                                                spu.getString(R.string.SP_LAST_ANALYSE_START_DT, "null") + " ~ " +
                                                spu.getString(R.string.SP_LAST_ANALYSE_END_DT, "null") + ")");
                                    } else {
                                        loadAnalysisBtn.setBackground(getResources().getDrawable(R.drawable.custom_show_more_btn_disabled, null));
                                        loadAnalysisBtn.setText("불러오기");
                                        loadAnalysisBtn.setEnabled(false);
                                    }
                                    dateRangeDialog.show();
                                }
                            });
                        }
                    };
                    loadTask.execute(new Integer[] {position});
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
            FirebaseUtils.updateUserInfo(this, spu, "Logout", db);
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
