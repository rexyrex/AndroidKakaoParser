package com.rexyrex.kakaoparser.Utils;

public class StringParseUtils {
    public static String chatFileNameToDate(String chatFileName){
        String[] split = chatFileName.split("_");
        return split[2] + " " + split[3].replace(".", ":");
    }

    public static String numberCommaFormat(String bytes){
        if(bytes.length()<4){
            return bytes;
        }
        String formatted = "";
        for(int i=0; i<bytes.length(); i++){
            if((bytes.length() - i) % 3 == 0){
                formatted += ",";
            }
            formatted += bytes.charAt(i);
        }
        return formatted;
    }
}
