package com.rexyrex.kakaoparser.Entities;

import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;

import java.io.File;
import java.util.List;

public class ChatData {

    double loadElapsedSeconds;
    File chatFile;
    String chatFileTitle;

    int chatterCount;
    int dayCount;
    int chatLineCount;
    int wordCount;
    double avgWordCount;
    double avgLetterCount;
    int linkCount;
    int picCount;
    int videoCount;
    int pptCount;
    int deletedMsgCount;

    List<StringIntPair> chatterFreqArrList;
    List<StringIntPair> top10Chatters;
    List<StringIntPair> wordFreqArrList;
    List<StringIntPair> freqByDayOfWeek;
    int maxFreqByDayOfWeek;

    List<ChatLineModel> allChatInit;

    List<String> authorsList;

    public ChatData() {

    }

    private static ChatData the_instance;
    public static ChatData getInstance() {
        if (the_instance == null) {
            the_instance = new ChatData();
        }

        return the_instance;
    }


    public String getChatFileTitle() {
        return chatFileTitle;
    }

    public void setChatFileTitle(String chatFileTitle) {
        this.chatFileTitle = chatFileTitle;
    }

    public List<String> getAuthorsList() {
        return authorsList;
    }

    public void setAuthorsList(List<String> authorsList) {
        this.authorsList = authorsList;
    }

    public List<ChatLineModel> getAllChatInit() {
        return allChatInit;
    }

    public void setAllChatInit(List<ChatLineModel> allChatInit) {
        this.allChatInit = allChatInit;
    }

    public File getChatFile() {
        return chatFile;
    }

    public void setChatFile(File chatFile) {
        this.chatFile = chatFile;
    }

    public double getLoadElapsedSeconds() {
        return loadElapsedSeconds;
    }

    public void setLoadElapsedSeconds(double loadElapsedSeconds) {
        this.loadElapsedSeconds = loadElapsedSeconds;
    }

    public List<StringIntPair> getFreqByDayOfWeek() {
        return freqByDayOfWeek;
    }

    public void setFreqByDayOfWeek(List<StringIntPair> freqByDayOfWeek) {
        this.freqByDayOfWeek = freqByDayOfWeek;
    }

    public int getMaxFreqByDayOfWeek() {
        return maxFreqByDayOfWeek;
    }

    public void setMaxFreqByDayOfWeek(int maxFreqByDayOfWeek) {
        this.maxFreqByDayOfWeek = maxFreqByDayOfWeek;
    }

    public List<StringIntPair> getWordFreqArrList() {
        return wordFreqArrList;
    }

    public void setWordFreqArrList(List<StringIntPair> wordFreqArrList) {
        this.wordFreqArrList = wordFreqArrList;
    }

    public List<StringIntPair> getTop10Chatters() {
        return top10Chatters;
    }

    public void setTop10Chatters(List<StringIntPair> top10Chatters) {
        this.top10Chatters = top10Chatters;
    }

    public List<StringIntPair> getChatterFreqArrList() {
        return chatterFreqArrList;
    }

    public void setChatterFreqArrList(List<StringIntPair> chatterFreqArrList) {
        this.chatterFreqArrList = chatterFreqArrList;
    }

    public int getDayCount() {
        return dayCount;
    }

    public void setDayCount(int dayCount) {
        this.dayCount = dayCount;
    }

    public int getChatterCount() {
        return chatterCount;
    }

    public void setChatterCount(int chatterCount) {
        this.chatterCount = chatterCount;
    }

    public int getChatLineCount() {
        return chatLineCount;
    }

    public void setChatLineCount(int chatLineCount) {
        this.chatLineCount = chatLineCount;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public double getAvgWordCount() {
        return avgWordCount;
    }

    public void setAvgWordCount(double avgWordCount) {
        this.avgWordCount = avgWordCount;
    }

    public double getAvgLetterCount() {
        return avgLetterCount;
    }

    public void setAvgLetterCount(double avgLetterCount) {
        this.avgLetterCount = avgLetterCount;
    }

    public int getLinkCount() {
        return linkCount;
    }

    public void setLinkCount(int linkCount) {
        this.linkCount = linkCount;
    }

    public int getPicCount() {
        return picCount;
    }

    public void setPicCount(int picCount) {
        this.picCount = picCount;
    }

    public int getVideoCount() {
        return videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    public int getPptCount() {
        return pptCount;
    }

    public void setPptCount(int pptCount) {
        this.pptCount = pptCount;
    }

    public int getDeletedMsgCount() {
        return deletedMsgCount;
    }

    public void setDeletedMsgCount(int deletedMsgCount) {
        this.deletedMsgCount = deletedMsgCount;
    }
}
