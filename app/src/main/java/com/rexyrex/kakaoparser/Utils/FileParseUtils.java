package com.rexyrex.kakaoparser.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class FileParseUtils {
    public static String parseFile(File file) {
        String fileName = file.getAbsolutePath() + "/KakaoTalkChats.txt";
        String chat = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            int index = 0;
            while (line != null) {
                if(index > 3 && line.length() > 0){
                    sb.append(line);
                    sb.append("\n");
                }
                index++;
                line = br.readLine();
            }
            chat = sb.toString();
            LogUtils.e( "chat size: " + chat.length());
            String[] lines = chat.split("\n");
            LogUtils.e( "lines: " + lines.length);
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return chat;
    }

    public static String parseFileForTitle(File file){
        String fileName = file.getAbsolutePath() + "/KakaoTalkChats.txt";
        String chat = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            int index = 0;
            while (index < 1) {
                sb.append(line);
                index++;
                line = br.readLine();
            }
            chat = sb.toString();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return chat;
    }
}
