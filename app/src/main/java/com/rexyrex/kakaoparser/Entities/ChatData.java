package com.rexyrex.kakaoparser.Entities;

import android.graphics.Color;

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
import java.util.Map;

public class ChatData {

    //Raw chat file
    File chatFile;
    //Raw chat lines
    String[] chatLines;
    //Raw chat string
    String chatStr;

    //chatter names
    ArrayList<String> chatters;

    //Date - ChatLine Arr
    LinkedHashMap<String, ArrayList<ChatLine>> chatMap;
    //Full list of chatlines
    ArrayList<ChatLine> chatLineArrayList;

    //name - freq
    HashMap<String, Integer> chatAmount;

    Date chatStartDate;
    //word - freq
    HashMap<String, Integer> wordFreqMap;

    //word - user - freq
    HashMap<String, HashMap<String, Integer>> wordUserFreqMap;

    //word - ArrayList<ChatLine>
    HashMap<String, ArrayList<ChatLine>> wordChatLinesMap;

    //sorted array list of word frequency data
    ArrayList<StringIntPair> wordFreqArrList;

    public HashMap<String, ArrayList<ChatLine>> getWordChatLinesMap() {
        return wordChatLinesMap;
    }

    public void setWordChatLinesMap(HashMap<String, ArrayList<ChatLine>> wordChatLinesMap) {
        this.wordChatLinesMap = wordChatLinesMap;
    }

    public ArrayList<ChatLine> getChatLineArrayList() {
        return chatLineArrayList;
    }

    public void setChatLineArrayList(ArrayList<ChatLine> chatLineArrayList) {
        this.chatLineArrayList = chatLineArrayList;
    }

    public HashMap<String, HashMap<String, Integer>> getWordUserFreqMap() {
        return wordUserFreqMap;
    }

    public void setWordUserFreqMap(HashMap<String, HashMap<String, Integer>> wordUserFreqMap) {
        this.wordUserFreqMap = wordUserFreqMap;
    }

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

    public ArrayList<StringIntPair> getWordFreqArrList() {
        return wordFreqArrList;
    }

    public void setWordFreqArrList(ArrayList<StringIntPair> wordFreqArrList) {
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

    private static ChatData the_instance;
    public static ChatData getInstance() {
        if (the_instance == null) {
            the_instance = new ChatData();
        }
        return the_instance;
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

    public PieData getWordUserFreqPieData(String word){
        ArrayList chatAmountArrayList = new ArrayList();

        HashMap<String, Integer> userFreqMap = wordUserFreqMap.get(word);

        for (Map.Entry<String, Integer> entry : userFreqMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            chatAmountArrayList.add(new PieEntry(value,key));
        }

        PieDataSet dataSet = new PieDataSet(chatAmountArrayList, "단어 사용 비율");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(12);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setSliceSpace(4);
        PieData pieData = new PieData(dataSet);
        return pieData;
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
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setSliceSpace(4);
        PieData pieData = new PieData(dataSet);
        return pieData;
    }
}
