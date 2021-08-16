package com.rexyrex.kakaoparser.Utils;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.rexyrex.kakaoparser.Constants.DateFormats;
import com.rexyrex.kakaoparser.Constants.TextPatterns;
import com.rexyrex.kakaoparser.Entities.ChatData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.CharacterIterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileParseUtils {
    public static String parseFile(File file, Date startDt, Date endDt) {
        String fileName = file.getAbsolutePath() + "/KakaoTalkChats.txt";
        String chat = "";

        ChatData cd = ChatData.getInstance();
        Pattern tPattern = cd.getChatLinePattern();
        SimpleDateFormat dateFormat = cd.getDateFormat();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            int index = 0;
            while (line != null) {
                if(index > 3 && line.length() > 0){
                    Matcher m = tPattern.matcher(line);
                    if(m.matches()){
                        //Extract Date
                        try {
                            Date date = dateFormat.parse(m.group(1));
                            if(date.before(startDt)){
                                index++;
                                line = br.readLine();
                                continue;
                            } else if(date.after(endDt)){
                                break;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    sb.append(line);
                    sb.append("\n");
                }
                index++;
                line = br.readLine();
            }
            chat = sb.toString();
            //LogUtils.e( "chat size: " + chat.length());
            String[] lines = chat.split("\n");
            //LogUtils.e( "lines: " + lines.length);
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return chat;
    }

    public static File getFileFromFolder(File folder){
        String fileName = folder.getAbsolutePath() + "/KakaoTalkChats.txt";
        File chatFile = new File(fileName);
        return chatFile;
    }

    //get chat file size given chat directory
    public static long getChatFileSize(File file){
        String fileName = file.getAbsolutePath() + "/KakaoTalkChats.txt";
        File chatFile = new File(fileName);
        return chatFile.length();
    }

    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %cB", value / 1024.0, ci.current());
    }

    public static long getSizeRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()){
            long size = 0;
            for (File child : fileOrDirectory.listFiles()){
                size += getSizeRecursive(child);
            }
            return size;
        } else {
            return fileOrDirectory.length();
        }


    }

    public static boolean checkIfChatFileExists(File file){
        String fileName = file.getAbsolutePath() + "/KakaoTalkChats.txt";
        File testFile = new File(fileName);
        return testFile.exists();
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

    public static String parseFileForType(File file){
        String fileName = file.getAbsolutePath() + "/KakaoTalkChats.txt";
        String checkLine = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            int index = 0;
            while (line != null) {
                if(index == 4){
                    checkLine = line;
                }

                if(index>4){
                    break;
                }

                index++;
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Matcher mk = TextPatterns.koreanDate.matcher(checkLine);
        Matcher me1 = TextPatterns.englishDate.matcher(checkLine);
        Matcher me2 = TextPatterns.englishDate2.matcher(checkLine);
        if(mk.matches()){
            return "korean";
        } else if(me1.matches()){
            return "english1";
        } else if(me2.matches()){
            return "english2";
        } else {
            return "unknown";
        }

    }

    public static String parseFileForDateRange(File file){

        ChatData cd = ChatData.getInstance();
        Pattern tPattern = cd.getChatLinePattern();
        SimpleDateFormat dateFormat = cd.getDateFormat();

        String fileName = file.getAbsolutePath() + "/KakaoTalkChats.txt";
        String firstLine = "";
        String lastLine ="";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = br.readLine();
            firstLine = line;
            lastLine = line;

            int index = 0;
            while (line != null) {
                if(index == 4){
                    firstLine = line;
                }
                //if(line.contains(",") && line.contains(":"))
                if(tPattern.matcher(line).find()){
                    lastLine = line;
                }

                index++;
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LogUtils.e("FirstLine : " + firstLine);
        LogUtils.e("LastLine : " + lastLine);

        Date startDt = null;
        Date endDt = null;

        SimpleDateFormat outputFormat = DateFormats.simpleKoreanFormat;

        boolean parseError = false;

        try {
            startDt = dateFormat.parse(firstLine);
            Matcher m = tPattern.matcher(lastLine);
            if(m.matches()){
                endDt = dateFormat.parse(m.group(1));
            }

        } catch (ParseException e) {
            parseError = true;
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }

        if(parseError){
            return "parse error";
        }

        String res = outputFormat.format(startDt) + "~" + outputFormat.format(endDt);

        return res;
    }
}
