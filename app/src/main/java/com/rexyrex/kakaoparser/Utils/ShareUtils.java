package com.rexyrex.kakaoparser.Utils;

import android.app.Activity;
import android.content.Intent;

public class ShareUtils {
    //Default share text
    public static void shareGeneral(Activity activity, String msg){
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            //i.putExtra(Intent.EXTRA_SUBJECT, "CherryAndroid");
            String sAux = msg;
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            activity.startActivity(Intent.createChooser(i, "선택하세요"));
        } catch(Exception e) {
            //e.printStackTrace();
        }
    }

    public static void shareGeneralWithPromo(Activity activity, String msg){
        String appPackageName = "com.rexyrex.kakaoparser";
        String promoString = "\n" + "카카오톡 분석을 직접 해보세요!" + "\n" + "https://play.google.com/store/apps/details?id=" + appPackageName;
        shareGeneral(activity, msg + promoString);
    }

    public static void shareAnalysisInfoWithPromo(Activity activity, String chatFileTitle, String title, String content){
        String appPackageName = "com.rexyrex.kakaoparser";
        String shareString = "[카카오톡 정밀 분석기]" + "\n";
        shareString += "분석 채팅방 : " + chatFileTitle + "\n";
        shareString += "분석 종류 : " + title + "\n";
        shareString += "==================" + "\n";
        shareString += content;
        shareString += "==================" + "\n";
        shareGeneralWithPromo(activity, shareString);
    }
}
