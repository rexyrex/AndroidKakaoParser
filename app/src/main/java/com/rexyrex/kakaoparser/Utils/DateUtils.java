package com.rexyrex.kakaoparser.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static String getCurrentTimeStr(){
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 a hh:mm", Locale.KOREAN);
        return sdf.format(date);
    }

    public static Date strToDate(String str){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 M월 d일 a hh:mm", Locale.KOREAN);
        try {
            return sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
