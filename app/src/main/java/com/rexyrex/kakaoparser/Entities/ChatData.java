package com.rexyrex.kakaoparser.Entities;

import android.content.Context;

import com.rexyrex.kakaoparser.Constants.DateFormats;
import com.rexyrex.kakaoparser.Constants.TextPatterns;
import com.rexyrex.kakaoparser.Database.DAO.AnalysedChatDAO;
import com.rexyrex.kakaoparser.Database.DAO.ChatLineDAO;
import com.rexyrex.kakaoparser.Database.DAO.WordDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.AnalysedChatModel;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Pattern;

public class ChatData {

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

    Pattern chatLinePattern;
    Pattern datePattern;
    SimpleDateFormat dateFormat;

    List<StringIntPair> chatterFreqArrList;
    List<StringIntPair> top10Chatters;
    List<StringIntPair> wordFreqArrList;
    List<StringIntPair> freqByDayOfWeek;
    int maxFreqByDayOfWeek;

    List<ChatLineModel> allChatInit;

    List<String> authorsList;

    MainDatabase database;
    ChatLineDAO chatLineDao;
    WordDAO wordDao;
    AnalysedChatDAO analysedChatDAO;
    SharedPrefUtils spu;

    public ChatData(Context c) {
        spu = new SharedPrefUtils(c);

        database = MainDatabase.getDatabase(c);
        chatLineDao = database.getChatLineDAO();
        wordDao = database.getWordDAO();
        analysedChatDAO = database.getAnalysedChatDAO();

        setChatterCount(chatLineDao.getChatterCount());
        setDayCount(chatLineDao.getDayCount());
        setChatLineCount(chatLineDao.getCount());
        setWordCount(wordDao.getDistinctCount());
        setAvgWordCount(chatLineDao.getAverageWordCount());
        setAvgLetterCount(wordDao.getAverageLetterCount());
        setLinkCount(wordDao.getLinkCount());
        setPicCount(wordDao.getPicCount());
        setVideoCount(wordDao.getVideoCount());
        setPptCount(wordDao.getPowerpointCount());
        setDeletedMsgCount(chatLineDao.getDeletedMsgCount());

        setChatterFreqArrList(chatLineDao.getChatterFrequencyPairs());
        setTop10Chatters(chatLineDao.getTop10Chatters());
        setWordFreqArrList(wordDao.getFreqWordList());
        setFreqByDayOfWeek(chatLineDao.getFreqByDayOfWeek());
        setMaxFreqByDayOfWeek(chatLineDao.getMaxFreqDayOfWeek());
        setAllChatInit(chatLineDao.getAllChatsByDateDesc());
        setAuthorsList(chatLineDao.getChatters());

        setChatType(spu.getString(R.string.SP_CD_CHAT_TYPE, ""));
    }

    private static ChatData the_instance;
    public static ChatData getInstance(Context c) {
        if (the_instance == null) {
            the_instance = new ChatData(c);
        }

        return the_instance;
    }

    public void setChatType(String chatType) {
        spu.saveString(R.string.SP_CD_CHAT_TYPE, chatType);

        switch(chatType){
            case "korean":
                chatLinePattern = TextPatterns.korean;
                datePattern = TextPatterns.koreanDate;
                dateFormat = DateFormats.koreanDate;
                break;

            case "english1":
                chatLinePattern = TextPatterns.english;
                datePattern = TextPatterns.englishDate;
                dateFormat = DateFormats.englishDate;
                break;

            case "english2":
                chatLinePattern = TextPatterns.english2;
                datePattern = TextPatterns.englishDate2;
                dateFormat = DateFormats.englishDate2;
                break;

            default:
                chatLinePattern = TextPatterns.korean;
                datePattern = TextPatterns.koreanDate;
                dateFormat = DateFormats.koreanDate;
                break;
        }
    }

    public Pattern getChatLinePattern() {
        return chatLinePattern;
    }

    public Pattern getDatePattern() {
        return datePattern;
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public long getChatFileSize() {
        return spu.getLong(R.string.SP_CD_CHAT_FILE_SIZE, 0);
    }

    public AnalysedChatModel getChatAnalyseDbModel() {
        return analysedChatDAO.getItemByTitleDt(spu.getString(R.string.SP_CD_CHAT_FILE_TITLE, ""), spu.getString(R.string.SP_CD_LAST_ANALYSE_DT, ""));
    }

    public void setChatAnalyseDbModel(String title, String lastDt) {
        spu.saveString(R.string.SP_CD_CHAT_FILE_TITLE, title);
        spu.saveString(R.string.SP_CD_LAST_ANALYSE_DT, lastDt);
    }

    public String getChatFileTitle() {
        return spu.getString(R.string.SP_CD_CHAT_FILE_TITLE_AND_DATE, "");
    }

    public void setChatFileTitle(String chatFileTitle) {
        spu.saveString(R.string.SP_CD_CHAT_FILE_TITLE_AND_DATE, chatFileTitle);
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
        return new File(spu.getString(R.string.SP_CD_CHAT_FILE_PATH, ""));
    }

    public void setChatFile(File chatFile) {
        spu.saveString(R.string.SP_CD_CHAT_FILE_PATH, chatFile.getAbsolutePath());
        spu.saveLong(R.string.SP_CD_CHAT_FILE_SIZE, FileParseUtils.getChatFileSize(chatFile));
    }

    public double getLoadElapsedSeconds() {
        return spu.getDouble(R.string.SP_CD_LOAD_SECONDS, 0);
    }

    public void setLoadElapsedSeconds(double loadElapsedSeconds) {
        spu.saveDouble(R.string.SP_CD_LOAD_SECONDS, loadElapsedSeconds);
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
