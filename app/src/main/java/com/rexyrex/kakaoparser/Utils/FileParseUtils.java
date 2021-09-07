package com.rexyrex.kakaoparser.Utils;

import android.content.Context;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.rexyrex.kakaoparser.Constants.DateFormats;
import com.rexyrex.kakaoparser.Constants.TextPatterns;
import com.rexyrex.kakaoparser.Entities.ChatData;

import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.text.CharacterIterator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileParseUtils {
    public static String parseFile(File file, Date startDt, Date endDt, Context c) {
        String fileName = file.getAbsolutePath() + "/KakaoTalkChats.txt";
        String chat = "";

        ChatData cd = ChatData.getInstance(c);
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
            //String[] lines = chat.split("\n");
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

    public static String parseFileForDateRange(File file, Context c){

        ChatData cd = ChatData.getInstance(c);
        Pattern tPattern = cd.getChatLinePattern();
        SimpleDateFormat dateFormat = cd.getDateFormat();

        String fileName = file.getAbsolutePath() + "/KakaoTalkChats.txt";
        File txtFile = new File(fileName);
        String firstLine = "";
        String lastLine ="";

        //Get first line
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = br.readLine();
            firstLine = line;

            while (line != null) {
                if(tPattern.matcher(line).find()){
                    firstLine = line;
                    break;
                }
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Get Last Line
        try {
            ReversedLinesFileReader reader
                    = new ReversedLinesFileReader(txtFile, Charset.forName("UTF-8"));
            String line = reader.readLine();
            while(line!=null){
                if(tPattern.matcher(line).find()){
                    lastLine = line;
                    break;
                }
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e){
            e.printStackTrace();
        }

        LogUtils.e("FirstLine : " + firstLine);
        LogUtils.e("LastLine : " + lastLine);

        Date startDt = null;
        Date endDt = null;

        boolean parseError = false;
        try {
            Matcher firstMatch = tPattern.matcher(firstLine);
            Matcher lastMatch = tPattern.matcher(lastLine);
            if(firstMatch.matches() && lastMatch.matches()){
                startDt = dateFormat.parse(firstMatch.group(1));
                endDt = dateFormat.parse(lastMatch.group(1));
            } else {
                return "parse error";
            }
        } catch (ParseException e) {
            parseError = true;
            FirebaseCrashlytics.getInstance().log("[REXYREX] parse or matcher error");
            FirebaseCrashlytics.getInstance().log("[REXYREX] first line : " + firstLine);
            FirebaseCrashlytics.getInstance().log("[REXYREX] last line : " + lastLine);
            FirebaseCrashlytics.getInstance().log("[REXYREX] chat type : " + cd.getChatType());
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
        }

        if(parseError){
            return "parse error";
        }

        SimpleDateFormat outputFormat = DateFormats.simpleKoreanFormat;
        return outputFormat.format(startDt) + "~" + outputFormat.format(endDt);
    }
}
