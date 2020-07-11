package com.rexyrex.kakaoparser.Utils;

import java.time.LocalTime;

public class TimeUtils {
    public static String getTimeLeftKorean(long seconds){
        if(seconds > 86399){
            seconds = 86399;
        }
        LocalTime lt = LocalTime.ofSecondOfDay(seconds);
        String res = "";
        if(lt.getHour() > 0){
            res += lt.getHour() + "시간 ";
        }
        if(lt.getMinute() > 0){
            res += lt.getMinute() + "분 ";
        }
        res += lt.getSecond() + "초 ";

        return res;
    }
}
