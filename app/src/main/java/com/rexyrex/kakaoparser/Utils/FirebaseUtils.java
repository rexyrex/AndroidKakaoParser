package com.rexyrex.kakaoparser.Utils;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rexyrex.kakaoparser.BuildConfig;
import com.rexyrex.kakaoparser.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FirebaseUtils {

    public static void updateUserInfo(Context c, SharedPrefUtils spu, String type){
        String firebaseToken = spu.getString(R.string.SP_FB_TOKEN, "null");

        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        int version = Build.VERSION.SDK_INT;
        TelephonyManager manager = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 a h:m", Locale.KOREAN);

        Map<String, Object> user = new HashMap<>();
        user.put("[B - DEVICE] Manufacturer", manufacturer);
        user.put("[B - DEVICE] Model", model);
        user.put("[B - DEVICE] Android Version", version);
        user.put("[B - DEVICE] Carrier Name", carrierName);
        user.put("[B - DEVICE] App Version", BuildConfig.VERSION_NAME);
        user.put("[A - General] Login Count", spu.getInt(R.string.SP_LOGIN_COUNT, 0));
        user.put("[A - General] Logout Count", spu.getInt(R.string.SP_LOGOUT_COUNT, 0));
        user.put("[A - General] Analyze Count", spu.getInt(R.string.SP_ANALYSE_COUNT, 0));
        user.put("[A - General] LastChangeDt", sdf.format(date));
        user.put("[A - General] FirebaseToken", firebaseToken);
        user.put("[A - General] Save Action", type);
        user.put("[A - General] Exported Chat Count", spu.getInt(R.string.SP_EXPORTED_CHAT_COUNT, -1));

        user.put("[A - General] LastLoginDt", spu.getString(R.string.SP_LOGIN_DT, "null"));
        user.put("[A - General] LastLogoutDt", spu.getString(R.string.SP_LOGOUT_DT, "null"));
        user.put("[A - General] FrstRegistDt", spu.getString(R.string.SP_REGIST_DT, "null"));

        user.put("[STATS Btn] Setting", spu.getInt(R.string.SP_OPEN_SETTINGS_COUNT, 0));
        user.put("[STATS Btn] Kakao", spu.getInt(R.string.SP_OPEN_KAKAO_COUNT, 0));
        user.put("[STATS Btn] Instructions", spu.getInt(R.string.SP_OPEN_HOW_TO_COUNT, 0));
        user.put("[STATS Btn] Privacy Policy", spu.getInt(R.string.SP_OPEN_PRIV_POLICY_COUNT, 0));

        user.put("[SHARE STATS 1] General", spu.getInt(R.string.SP_SHARE_GENERAL_ANALZ_COUNT, 0));
        user.put("[SHARE STATS 2] Person", spu.getInt(R.string.SP_SHARE_PERSON_ANALZ_COUNT, 0));
        user.put("[SHARE STATS 3] Word", spu.getInt(R.string.SP_SHARE_WORD_ANALZ_COUNT, 0));

        user.put("[SHARE STATS t1] Time 시간", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_1_COUNT, 0));
        user.put("[SHARE STATS t2] Time 요일", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_2_COUNT, 0));
        user.put("[SHARE STATS t3] Time 일", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_3_COUNT, 0));
        user.put("[SHARE STATS t4] Time 월", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_4_COUNT, 0));
        user.put("[SHARE STATS t5] Time 연", spu.getInt(R.string.SP_SHARE_TIME_ANALZ_5_COUNT, 0));

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
