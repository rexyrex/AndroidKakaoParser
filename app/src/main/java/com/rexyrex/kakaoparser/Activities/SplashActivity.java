package com.rexyrex.kakaoparser.Activities;

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

        splashIV.setVisibility(View.VISIBLE);
        appTitleTV.setVisibility(View.VISIBLE);

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
        }

        runFadeInAnimation(splashIV);
        runFadeInAnimation(appTitleTV);
        scheduleSplashScreen(3000);
    }

    //splashScreenDuration이후 activity이동, splash activity는 finish
    private void scheduleSplashScreen(long splashScreenDuration) {
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
}