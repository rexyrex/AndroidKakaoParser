package com.rexyrex.kakaoparser.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rexyrex.kakaoparser.BuildConfig;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.DeviceInfoUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    boolean backBtnPressed;
    TextView appTitleTV;
    TextView versionTV;
    ImageView splashIV;

    String[] permissions;
    String[] deniedPermsArr;

    SharedPrefUtils spu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        backBtnPressed = false;
        spu = new SharedPrefUtils(this);

//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        Map<String, Object> user = new HashMap<>();
//        user.put("first", "Ada");
//
//        db.collection("users")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        //LogUtils.e( "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        //LogUtils.e("Error adding document" + e.getMessage());
//                        e.printStackTrace();
//                    }
//                });

        appTitleTV = findViewById(R.id.appTitleTV);
        splashIV = findViewById(R.id.splashIV);
        versionTV = findViewById(R.id.versionTV);

        versionTV.setText("Ver " + BuildConfig.VERSION_NAME);

        //LogUtils("splashIV isnull? : " + (splashIV == null));

        //요청할 권한들
        permissions = new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        //허용되지 않은 권한 받아오기
        ArrayList<String> deniedPerms =  DeviceInfoUtils.getDeniedPermissions(this, permissions);
        deniedPermsArr = deniedPerms.toArray(new String[0]);


        //log fcm token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                // Get new Instance ID token
                String token = task.getResult();
                LogUtils.e( "FB_Token: " + token);
                spu.saveString(R.string.SP_FB_TOKEN, token);

                String registered = spu.getString(R.string.SP_REGISTERED, "false");

                if(registered.equals("false")){
                    //subscribe to topic
                    FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.FirebaseTopicName))
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SplashActivity.this, "구글 서비스 문제가 발생했습니다. 잠시후 다시 시도해주세요.", Toast.LENGTH_LONG).show();
                                        scheduleAppClose(1500);
                                    } else {
                                        spu.saveString(R.string.SP_REGISTERED, "true");
                                        startLogic();
                                    }
                                }
                            });
                } else {
                    startLogic();
                }


            }
        });
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