package com.rexyrex.kakaoparser.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.DeviceInfoUtils;
import com.rexyrex.kakaoparser.Utils.LogUtils;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {

    boolean backBtnPressed;
    TextView appTitleTV;
    ImageView splashIV;

    String[] permissions;
    String[] deniedPermsArr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        backBtnPressed = false;

        appTitleTV = findViewById(R.id.appTitleTV);
        splashIV = findViewById(R.id.splashIV);

        //LogUtils("splashIV isnull? : " + (splashIV == null));

        //요청할 권한들
        permissions = new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE
        };

        //허용되지 않은 권한 받아오기
        ArrayList<String> deniedPerms =  DeviceInfoUtils.getDeniedPermissions(this, permissions);
        deniedPermsArr = deniedPerms.toArray(new String[0]);

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
        runFadeInAnimation(splashIV);
        runFadeInAnimation(appTitleTV);
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
                    LogUtils.e("grant res : " + grantResults[i]);
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