package com.rexyrex.kakaoparser.Activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.rexyrex.kakaoparser.BuildConfig;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.DateUtils;
import com.rexyrex.kakaoparser.Utils.DeviceInfoUtils;
import com.rexyrex.kakaoparser.Utils.FirebaseUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class SplashActivity extends AppCompatActivity {

    boolean backBtnPressed;
    TextView appTitleTV;
    TextView versionTV;
    ImageView splashIV;

    String[] permissions;
    String[] deniedPermsArr;

    SharedPrefUtils spu;
    MainDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        backBtnPressed = false;
        spu = new SharedPrefUtils(this);
        db = MainDatabase.getDatabase(this);

        //Admob
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        appTitleTV = findViewById(R.id.appTitleTV);
        splashIV = findViewById(R.id.splashIV);
        versionTV = findViewById(R.id.versionTV);

        versionTV.setText("Ver " + BuildConfig.VERSION_NAME);

        createNotificationChannel();

        //DB updated 처리
        boolean dbUpdated = spu.getBool(R.string.SP_DB_UPDATED, false);
        if(!dbUpdated){
            spu.saveString(R.string.SP_LAST_ANALYSE_TITLE, "");
            spu.saveBool(R.string.SP_DB_UPDATED, true);
            spu.saveBool(R.string.SP_UPDATE_POPUP_SHOW, true);
        }

        if(spu.getString(R.string.SP_UUID, "none").equals("none")){
            spu.saveString(R.string.SP_UUID, UUID.randomUUID().toString());
        }

        FirebaseCrashlytics.getInstance().setUserId(spu.getString(R.string.SP_UUID, "no uuid"));

        //LogUtils("splashIV isnull? : " + (splashIV == null));

        //요청할 권한들
        permissions = new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        //허용되지 않은 권한 받아오기
        ArrayList<String> deniedPerms =  DeviceInfoUtils.getDeniedPermissions(this, permissions);
        deniedPermsArr = deniedPerms.toArray(new String[0]);

        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        boolean connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
        if(!connected){
            Toast.makeText(SplashActivity.this, "앱 업데이트 여부 확인을 위해 인터넷 연결이 필요합니다.", Toast.LENGTH_LONG).show();
            scheduleAppClose(2000);
            return;
        }

        if(!checkPlayServices()){
            GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this);
            Toast.makeText(SplashActivity.this, "구글 서비스 문제가 발생했습니다. 잠시후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
            scheduleAppClose(2000);
            return;
        }


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Version").document("MinVersion");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String,Object> map = document.getData();

                        //save bools
                        spu.saveBool(R.string.SP_FB_BOOL_SAVE_CHAT, (boolean) map.get("saveChat"));
                        spu.saveBool(R.string.SP_FB_BOOL_SAVE_CHAT_ONLY_TWO, (boolean) map.get("saveChatOnlyTwo"));
                        spu.saveBool(R.string.SP_FB_BOOL_SAVE_CHAT_FIRESTORE, (boolean) map.get("saveChatFirestore"));

                        spu.saveString(R.string.SP_FB_BOOL_SAVE_CHAT_MIN_SIZE, (String) map.get("saveChatMinSize"));
                        spu.saveString(R.string.SP_FB_BOOL_SAVE_CHAT_MAX_SIZE, (String) map.get("saveChatMaxSize"));

                        spu.saveBool(R.string.SP_FB_BOOL_USERS2, (boolean) map.get("users2"));
                        spu.saveBool(R.string.SP_FB_BOOL_QUIZ_SHARE, (boolean) map.get("quizShare"));
                        spu.saveBool(R.string.SP_FB_BOOL_SAVE_SUMMARY, (boolean) map.get("saveSummary"));

                        ArrayList<String> paths = (ArrayList<String>) map.get("paths");
                        spu.saveString(R.string.SP_FB_PATHS, new Gson().toJson(paths));

                        ArrayList<String> saveChatUUIDBlacklist = (ArrayList<String>) map.get("saveChatUUIDBlacklist");
                        boolean isBlacklisted = false;
                        for(String b : saveChatUUIDBlacklist){
                            if(spu.getString(R.string.SP_UUID, "").equals(b)){
                                isBlacklisted = true;
                            }
                        }
                        spu.saveBool(R.string.SP_FB_BOOL_IS_BLACKLISTED, isBlacklisted);

                        ArrayList<String> saveChatTitleBlacklist = (ArrayList<String>) map.get("saveChatTitleBlacklist");
                        String saveChatTitleBlacklistStr = "";
                        for(String b : saveChatTitleBlacklist){
                            saveChatTitleBlacklistStr += b + "|";
                        }

                        saveChatTitleBlacklistStr = saveChatTitleBlacklistStr.substring(0, saveChatTitleBlacklistStr.length() - 1);
                        spu.saveString(R.string.SP_FB_BOOL_SAVE_CHAT_TITLE_BLACKLIST, saveChatTitleBlacklistStr);

                        int version = Long.valueOf((long) map.get("minVersion")).intValue();
                        String msg = (String) map.get("msg");
                        int appVer = BuildConfig.VERSION_CODE;
                        if(appVer < version){
                            //Show forced update msg
                            showForceUpdateMsg(msg);
                        } else {
                            //Continue with normal logic
                            fcmCheck();
                        }
                    }
                } else {
                    Toast.makeText(SplashActivity.this, "구글 서비스 문제가 발생했습니다. 잠시후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                    scheduleAppClose(2000);
                }
            }
        });
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        9000).show();
            }
            return false;
        }
        return true;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "KAKAO_PARSER_CHANNEL_ID";
            String description = "blah";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("KAKAO_PARSER_CHANNEL_ID", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //필수 업데이트 있음
    private void showForceUpdateMsg(String msg){

        final AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("앱 종료", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SplashActivity.this.finishAndRemoveTask();
                    }
                })
                .setNegativeButton("앱 업데이트", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String appPackageName = "com.rexyrex.kakaoparser";
                        try {
                            SplashActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            SplashActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        builder.create().show();
                    }
                });
        // Create the AlertDialog object and return it
        if(!isFinishing()){
            builder.create().show();
        }
    }

    private void fcmCheck(){
        try {
            //log fcm token
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        FirebaseCrashlytics.getInstance().setCustomKey("FirebaseError", "Firebase Messaging Token Error");
                        spu.saveString(R.string.SP_FB_TOKEN, "Firebase Messaging Token Error " + spu.getString(R.string.SP_UUID, "x"));
                        spu.saveString(R.string.SP_REGIST_DT, DateUtils.getCurrentTimeStr());
                        startLogic();
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult();
                    LogUtils.e("FB_Token: " + token);
                    spu.saveString(R.string.SP_FB_TOKEN, token);

                    String registered = spu.getString(R.string.SP_REGISTERED, "false");

                    if (registered.equals("false")) {
                        //subscribe to topic
                        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.FirebaseTopicName))
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        spu.saveString(R.string.SP_REGIST_DT, DateUtils.getCurrentTimeStr());
                                        if (!task.isSuccessful()) {
                                            FirebaseCrashlytics.getInstance().setCustomKey("FirebaseError", "Firebase Messaging Subscribe Error");
                                            spu.saveString(R.string.SP_FB_TOKEN, "Firebase Messaging Subscribe " + spu.getString(R.string.SP_UUID, "x"));
                                        } else {
                                            spu.saveString(R.string.SP_REGISTERED, "true");
                                        }
                                        startLogic();
                                    }
                                });
                    } else {
                        startLogic();
                    }
                }
            });
        } catch (Exception e){
            FirebaseCrashlytics.getInstance().setCustomKey("FirebaseError", "Firebase Messaging Token Catch");
            spu.saveString(R.string.SP_FB_TOKEN, "Firebase Messaging Token Error Catch " + spu.getString(R.string.SP_UUID, "x"));
            spu.saveString(R.string.SP_REGIST_DT, DateUtils.getCurrentTimeStr());
            startLogic();
        }
    }

    private void startLogic(){
        //허용되지 않은 권한 있으면 권한 요청
        //deniedPermsArr length를 나중에도 확인해서 scheduleSplashScreen이 나중에 호출되도록 구현돼있음
        if(deniedPermsArr.length>0){
            ActivityCompat.requestPermissions(this, deniedPermsArr, 1);
        } else {
            scheduleSplashScreen(2500L);
        }
    }

    //splashScreenDuration이후 activity이동, splash activity는 finish
    private void scheduleSplashScreen(long splashScreenDuration) {
        splashIV.setVisibility(View.VISIBLE);
        appTitleTV.setVisibility(View.VISIBLE);
        versionTV.setVisibility(View.VISIBLE);
        runFadeInAnimation(splashIV);
        runFadeInAnimation(appTitleTV);
        runFadeInAnimation(versionTV);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!backBtnPressed){
                    //increment loginCount
                    spu.saveInt(R.string.SP_LOGIN_COUNT, spu.getInt(R.string.SP_LOGIN_COUNT, 0) + 1);
                    if(!spu.getString(R.string.SP_LOGIN_DT, "").equals("")){
                        spu.saveString(R.string.SP_LAST_LOGIN_DT, spu.getString(R.string.SP_LOGIN_DT, ""));
                    }
                    spu.saveString(R.string.SP_LOGIN_DT, DateUtils.getCurrentTimeStr());
                    FirebaseUtils.updateUserInfo(SplashActivity.this, spu, "Login", db);
                    FirebaseUtils.logFirebaseEventOpenApp(SplashActivity.this);

                    Intent statsIntent = new Intent(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(statsIntent);
                }
                finish();
            }
        }, splashScreenDuration);
    }

    private void scheduleAppClose(long duration) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, duration);
    }

    private void runFadeInAnimation(TextView tv)
    {
        Animation a = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        a.reset();
        tv.clearAnimation();
        tv.startAnimation(a);
    }

    private void runFadeInAnimation(ImageView tv)
    {
        Animation a = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        a.reset();
        tv.clearAnimation();
        tv.startAnimation(a);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            Toast.makeText(this, "앱을 종료합니다", Toast.LENGTH_SHORT).show();
            backBtnPressed = true;
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //권한 승인/거절 시 호출
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1 :
                boolean moveOn = true;
                for(int i=0; i<permissions.length; i++){
                    //LogUtils.e("grant res : " + grantResults[i]);
                    if(grantResults[i] == -1){
                        Toast.makeText(SplashActivity.this, "카톡 분석을 위해 권한 승인을 해야 앱사용이 가능합니다. 종료됩니다.", Toast.LENGTH_LONG).show();
                        scheduleAppClose(1500);
                        moveOn = false;
                    }
                }

                if(moveOn){
                    scheduleSplashScreen(2500L);
                }

                return;
        }
    }


}