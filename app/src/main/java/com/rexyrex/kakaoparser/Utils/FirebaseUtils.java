package com.rexyrex.kakaoparser.Utils;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rexyrex.kakaoparser.BuildConfig;
import com.rexyrex.kakaoparser.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FirebaseUtils {

//    public static void logFirebaseEvent(Context c){
//        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(c);
//
//        Bundle bundle = new Bundle();
//        bundle.putString(FirebaseAnalytics.Param.METHOD, "Open App");
//        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);
//
//    }

    public static void updateUserInfo(Context c, SharedPrefUtils spu, String type){
        String firebaseToken = spu.getString(R.string.SP_FB_TOKEN, "null");

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        TelephonyManager manager = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 a hh:mm", Locale.KOREAN);

        Map<String, Object> user = new HashMap<>();
        user.put("Manufacturer", manufacturer);
        user.put("Model", model);
        user.put("Android Version", version);
        user.put("Carrier Name", carrierName);
        user.put("App Version", BuildConfig.VERSION_NAME);
        user.put("Login Count", spu.getInt(R.string.SP_LOGIN_COUNT, 0));
        user.put("Logout Count", spu.getInt(R.string.SP_LOGOUT_COUNT, 0));
        user.put("Analyze Count", spu.getInt(R.string.SP_ANALYSE_COUNT, 0));
        user.put("LastChangeDt", sdf.format(date));
        user.put("FirebaseToken", firebaseToken);
        user.put("Save Action", type);
        user.put("Exported Chat Count", spu.getInt(R.string.SP_EXPORTED_CHAT_COUNT, -1));

        user.put("LastLoginDt", spu.getString(R.string.SP_LOGIN_DT, "null"));
        user.put("LastLogoutDt", spu.getString(R.string.SP_LOGOUT_DT, "null"));
        user.put("FrstRegistDt", spu.getString(R.string.SP_REGIST_DT, "null"));

        user.put("SettingBtnCount", spu.getInt(R.string.SP_OPEN_SETTINGS_COUNT, 0));
        user.put("KakaoBtnCount", spu.getInt(R.string.SP_OPEN_KAKAO_COUNT, 0));
        user.put("InstructionsBtnCount", spu.getInt(R.string.SP_OPEN_HOW_TO_COUNT, 0));
        user.put("PrivacyPolicyBtnCount", spu.getInt(R.string.SP_OPEN_PRIV_POLICY_COUNT, 0));

        user.put("ShareGeneralCount", spu.getInt(R.string.SP_SHARE_GENERAL_ANALZ_COUNT, 0));
        user.put("SharePersonCount", spu.getInt(R.string.SP_SHARE_PERSON_ANALZ_COUNT, 0));
        user.put("ShareWordCount", spu.getInt(R.string.SP_SHARE_WORD_ANALZ_COUNT, 0));

        user.put("ShareTimeTime", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_1_COUNT, 0));
        user.put("ShareTimeDayOfWeek", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_2_COUNT, 0));
        user.put("ShareTimeDay", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_3_COUNT, 0));
        user.put("ShareTimeMonth", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_4_COUNT, 0));
        user.put("ShareTimeYear", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_5_COUNT, 0));

        db.collection("users").document(firebaseToken)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //LogUtils.e("Error adding document" + e.getMessage());
                        e.printStackTrace();
                    }
                });
    }

}
