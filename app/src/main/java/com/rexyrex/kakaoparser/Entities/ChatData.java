package com.rexyrex.kakaoparser.Entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Queue;

public class ChatData implements Parcelable {

    protected ChatData(Parcel in) {
        chatStr = in.readString();
        chatters = in.createStringArrayList();
        chatLines = in.createStringArray();
    }

    public static final Creator<ChatData> CREATOR = new Creator<ChatData>() {
        @Override
        public ChatData createFromParcel(Parcel in) {
            return new ChatData(in);
        }

        @Override
        public ChatData[] newArray(int size) {
            return new ChatData[size];
        }
    };

    public File getChatFile() {
        return chatFile;
    }

    public void setChatFile(File chatFile) {
        this.chatFile = chatFile;
    }

    public String getChatStr() {
        return chatStr;
    }

    public void setChatStr(String chatStr) {
        this.chatStr = chatStr;
    }

    public ArrayList<String> getChatters() {
        return chatters;
    }

    public void setChatters(ArrayList<String> chatters) {
        this.chatters = chatters;
    }

    public LinkedHashMap<String, ArrayList<ChatLine>> getChatMap() {
        return chatMap;
    }

    public void setChatMap(LinkedHashMap<String, ArrayList<ChatLine>> chatMap) {
        this.chatMap = chatMap;
    }

    public HashMap<String, Integer> getChatAmount() {
        return chatAmount;
    }

    public void setChatAmount(HashMap<String, Integer> chatAmount) {
        this.chatAmount = chatAmount;
    }

    public String[] getChatLines() {
        return chatLines;
    }

    public void setChatLines(String[] chatLines) {
        this.chatLines = chatLines;
    }

    File chatFile;
    String chatStr;
    ArrayList<String> chatters;
    LinkedHashMap<String, ArrayList<ChatLine>> chatMap = new LinkedHashMap<>();
    HashMap<String, Integer> chatAmount = new HashMap<>();
    String[] chatLines;
    Date chatStartDate;
    HashMap<String, Integer> wordFreqMap = new HashMap<>();
    //sorted array list of word frequency data
    ArrayList<Pair> wordFreqArrList;

    public ArrayList<Pair> getWordFreqArrList() {
        return wordFreqArrList;
    }

    public void setWordFreqArrList(ArrayList<Pair> wordFreqArrList) {
        this.wordFreqArrList = wordFreqArrList;
    }

    double loadElapsedSeconds;

    public double getLoadElapsedSeconds() {
        return loadElapsedSeconds;
    }

    public void setLoadElapsedSeconds(double loadElapsedSeconds) {
        this.loadElapsedSeconds = loadElapsedSeconds;
    }

    public HashMap<String, Integer> getWordFreqMap() {
        return wordFreqMap;
    }

    public void setWordFreqMap(HashMap<String, Integer> wordFreqMap) {
        this.wordFreqMap = wordFreqMap;
    }


    public Date getChatStartDate() {
        return chatStartDate;
    }

    public void setChatStartDate(Date chatStartDate) {
        this.chatStartDate = chatStartDate;
    }

    public Date getChatEndDate() {
        return chatEndDate;
    }

    public void setChatEndDate(Date chatEndDate) {
        this.chatEndDate = chatEndDate;
    }

    public String getChatDateRangeStr(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.M.d");
        return dateFormat.format(chatStartDate) + "~" + dateFormat.format(chatEndDate);
    }

    Date chatEndDate;

    public ChatData() {

    }

    public int getChatDays(){
        return chatMap.size();
    }

    public int getChatterCount(){
        return chatters.size();
    }

    public int getChatLinesCount(){
        return chatLines.length;
    }

    public long getChatFileSize(){
        return chatFile.length();
    }

    public String getChatFileName(){
        return chatFile.getName();
    }

    public String getSummaryText(){
        String chatStatsStr = "";

        chatStatsStr += "File Name : " + getChatFileName() + "\n";
        chatStatsStr += "File Size : " + getChatFileSize() + "\n";
        chatStatsStr += "Lines : " + getChatLinesCount() + "\n";
        chatStatsStr += "Chatters : " + getChatterCount() + "\n";
        chatStatsStr += "====== Chat Amount ======\n";

        for(String chatter : chatters){
            chatStatsStr += chatter + " : " + chatAmount.get(chatter) + "\n";
        }

        chatStatsStr += "=========================\n";
        chatStatsStr += "Chat Days : " + getChatDays() + "\n";
        chatStatsStr += "=========================\n";

        return chatStatsStr;
    }

    public PieData getChatAmountPieData(){
        ArrayList chatAmountArrayList = new ArrayList();
        ArrayList chatNicknameArrayList = new ArrayList();
        int tmpIndex = 0;

        for(String chatter : chatters){
            chatAmountArrayList.add(new PieEntry(chatAmount.get(chatter),chatter));
            chatNicknameArrayList.add(chatter);
            tmpIndex++;
        }

        PieDataSet dataSet = new PieDataSet(chatAmountArrayList, "채팅 비율");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12);
        PieData pieData = new PieData(dataSet);
        return pieData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(chatStr);
        dest.writeStringList(chatters);
        dest.writeStringArray(chatLines);
    }
}
