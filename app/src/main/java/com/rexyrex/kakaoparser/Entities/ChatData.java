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
    int totalWordCount;
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
    List<StringIntPair> top10ChattersByWord;
    List<StringIntPair> top10ChattersByPic;
    List<StringIntPair> top10ChattersByVideo;
    List<StringIntPair> top10ChattersByLink;
    List<StringIntPair> top10ChattersByDeletedMsg;
    List<StringIntPair> wordFreqArrList;
    List<StringIntPair> freqByDayOfWeek;
    int maxFreqByDayOfWeek;

    double avgDaysActive;
    List<StringIntPair> daysActiveRankingList;
    List<StringIntPair> distinctWordRankingList;
    List<StringIntPair> chatLineRankingList;
    List<StringIntPair> totalWordRankingList;
    List<StringIntPair> picRankingList;
    List<StringIntPair> videoRankingList;
    List<StringIntPair> linkRankingList;
    List<StringIntPair> delRankingList;
    List<StringIntPair> sentWordRankingList;
    List<StringIntPair> wordLengthRankingList;


    List<ChatLineModel> allChatInit;

    List<String> authorsList;

    MainDatabase database;
    ChatLineDAO chatLineDao;
    WordDAO wordDao;
    AnalysedChatDAO analysedChatDAO;
    SharedPrefUtils spu;

    boolean reset;

    public ChatData(Context c) {
        spu = new SharedPrefUtils(c);

        database = MainDatabase.getDatabase(c);
        chatLineDao = database.getChatLineDAO();
        wordDao = database.getWordDAO();
        analysedChatDAO = database.getAnalysedChatDAO();

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

    public String getChatType(){
        return spu.getString(R.string.SP_CD_CHAT_TYPE, "");
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
        return (authorsList == null || authorsList.size() == 0) ? chatLineDao.getChatters() : authorsList;
    }

    public void setAuthorsList(List<String> authorsList) {
        this.authorsList = authorsList;
    }

    public List<ChatLineModel> getAllChatInit() {
        return (allChatInit == null || allChatInit.size()==0 ) ? chatLineDao.getAllChatsByDateDesc() : allChatInit;
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
        return (freqByDayOfWeek == null) ? chatLineDao.getFreqByDayOfWeek() : freqByDayOfWeek;
    }

    public void setFreqByDayOfWeek(List<StringIntPair> freqByDayOfWeek) {
        this.freqByDayOfWeek = freqByDayOfWeek;
    }

    public int getMaxFreqByDayOfWeek() {
        return (maxFreqByDayOfWeek == 0) ? chatLineDao.getMaxFreqDayOfWeek() : maxFreqByDayOfWeek;
    }

    public void setMaxFreqByDayOfWeek(int maxFreqByDayOfWeek) {
        this.maxFreqByDayOfWeek = maxFreqByDayOfWeek;
    }

    public List<StringIntPair> getWordFreqArrList() {
        return (wordFreqArrList == null) ? wordDao.getFreqWordList() : wordFreqArrList;
    }

    public void setWordFreqArrList(List<StringIntPair> wordFreqArrList) {
        this.wordFreqArrList = wordFreqArrList;
    }

    public List<StringIntPair> getTop10Chatters() {
        return (top10Chatters==null) ? chatLineDao.getTop10Chatters() : top10Chatters;
    }

    public void setTop10Chatters(List<StringIntPair> top10Chatters) {
        this.top10Chatters = top10Chatters;
    }

    public List<StringIntPair> getTop10ChattersByWord() {
        return (top10ChattersByWord==null) ? wordDao.getTop10ChattersByWords() : top10ChattersByWord;
    }

    public void setTop10ChattersByWord(List<StringIntPair> top10ChattersByWord) {
        this.top10ChattersByWord = top10ChattersByWord;
    }

    public List<StringIntPair> getTop10ChattersByPic() {
        return (top10ChattersByPic==null) ? wordDao.getTop10ChattersByPic() : top10ChattersByPic;
    }

    public void setTop10ChattersByPic(List<StringIntPair> top10ChattersByPic) {
        this.top10ChattersByPic = top10ChattersByPic;
    }

    public List<StringIntPair> getTop10ChattersByVideo() {
        return (top10ChattersByVideo==null) ? wordDao.getTop10ChattersByVideo() : top10ChattersByVideo;
    }

    public void setTop10ChattersByVideo(List<StringIntPair> top10ChattersByVideo) {
        this.top10ChattersByVideo = top10ChattersByVideo;
    }

    public List<StringIntPair> getTop10ChattersByLink() {
        return (top10ChattersByLink==null) ? wordDao.getTop10ChattersByLink() : top10ChattersByLink;
    }

    public void setTop10ChattersByLink(List<StringIntPair> top10ChattersByLink) {
        this.top10ChattersByLink = top10ChattersByLink;
    }

    public List<StringIntPair> getTop10ChattersByDeletedMsg() {
        return (top10ChattersByDeletedMsg==null) ? chatLineDao.getTop10ChattersByDeletedMsg() : top10ChattersByDeletedMsg;
    }

    public void setTop10ChattersByDeletedMsg(List<StringIntPair> top10ChattersByDeletedMsg) {
        this.top10ChattersByDeletedMsg = top10ChattersByDeletedMsg;
    }

    public List<StringIntPair> getChatterFreqArrList() {
        return (chatterFreqArrList==null) ? chatLineDao.getChatterFrequencyPairs() : chatterFreqArrList;
    }

    public void setChatterFreqArrList(List<StringIntPair> chatterFreqArrList) {
        this.chatterFreqArrList = chatterFreqArrList;
    }

    public int getDayCount() {
        return (dayCount==0) ? chatLineDao.getDayCount() : dayCount;
    }

    public void setDayCount(int dayCount) {

        this.dayCount = dayCount;
    }

    public int getChatterCount() {
        if(chatterCount == 0){
            return chatLineDao.getChatterCount();
        } else {
            return chatterCount;
        }
    }

    public void setChatterCount(int chatterCount) {
        this.chatterCount = chatterCount;
    }

    public int getChatLineCount() {
        if(chatLineCount == 0){
            return chatLineDao.getCount();
        } else {
            return chatLineCount;
        }
    }

    public void setChatLineCount(int chatLineCount) {
        this.chatLineCount = chatLineCount;
    }

    public int getWordCount() {
        return (wordCount==0) ? wordDao.getDistinctCount() : wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getTotalWordCount() {
        return (totalWordCount==0) ? wordDao.getCount() : totalWordCount;
    }

    public void setTotalWordCount(int totalWordCount) {
        this.totalWordCount = totalWordCount;
    }

    public double getAvgWordCount() {
        return (avgWordCount==0) ? chatLineDao.getAverageWordCount() : avgWordCount;
    }

    public void setAvgWordCount(double avgWordCount) {
        this.avgWordCount = avgWordCount;
    }

    public double getAvgLetterCount() {
        return (avgLetterCount==0) ? wordDao.getAverageLetterCount() : avgLetterCount;
    }

    public void setAvgLetterCount(double avgLetterCount) {
        this.avgLetterCount = avgLetterCount;
    }

    public int getLinkCount() {
        return (linkCount==0) ? wordDao.getLinkCount() : linkCount;
    }

    public void setLinkCount(int linkCount) {
        this.linkCount = linkCount;
    }

    public int getPicCount() {
        return (picCount==0) ? wordDao.getPicCount() : picCount;
    }

    public void setPicCount(int picCount) {
        this.picCount = picCount;
    }

    public int getVideoCount() {
        return (videoCount==0) ? wordDao.getVideoCount() : videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    public int getPptCount() {
        return (pptCount==0) ? wordDao.getPowerpointCount() : pptCount;
    }

    public void setPptCount(int pptCount) {
        this.pptCount = pptCount;
    }

    public int getDeletedMsgCount() {
        return deletedMsgCount==0 ? chatLineDao.getDeletedMsgCount() : deletedMsgCount;
    }

    public void setDeletedMsgCount(int deletedMsgCount) {
        this.deletedMsgCount = deletedMsgCount;
    }

    public List<StringIntPair> getDaysActiveRankingList() {
        return (daysActiveRankingList==null) ? chatLineDao.getDaysActiveRank() : daysActiveRankingList;
    }

    public void setDaysActiveRankingList(List<StringIntPair> daysActiveRankingList) {
        this.daysActiveRankingList = daysActiveRankingList;
    }

    public List<StringIntPair> getDistinctWordRankingList() {
        return (daysActiveRankingList==null) ? wordDao.getDistinctWordCountByRank() : daysActiveRankingList;
    }

    public void setDistinctWordRankingList(List<StringIntPair> distinctWordRankingList) {
        this.distinctWordRankingList = distinctWordRankingList;
    }

    public List<StringIntPair> getChatLineRankingList() {
        return (chatLineRankingList==null) ? chatLineDao.getChatterChatLineByRank() : chatLineRankingList;
    }

    public void setChatLineRankingList(List<StringIntPair> chatLineRankingList) {
        this.chatLineRankingList = chatLineRankingList;
    }

    public List<StringIntPair> getTotalWordRankingList() {
        return (totalWordRankingList==null) ? wordDao.getTotalWordCountByRank() : totalWordRankingList;
    }

    public void setTotalWordRankingList(List<StringIntPair> totalWordRankingList) {
        this.totalWordRankingList = totalWordRankingList;
    }

    public List<StringIntPair> getPicRankingList() {
        return (picRankingList==null) ? wordDao.getPicRanking() : picRankingList;
    }

    public void setPicRankingList(List<StringIntPair> picRankingList) {
        this.picRankingList = picRankingList;
    }

    public List<StringIntPair> getVideoRankingList() {
        return (videoRankingList==null) ? wordDao.getVideoRanking() : videoRankingList;
    }

    public void setVideoRankingList(List<StringIntPair> videoRankingList) {
        this.videoRankingList = videoRankingList;
    }

    public List<StringIntPair> getLinkRankingList() {
        return (linkRankingList==null) ? wordDao.getLinkRanking() : linkRankingList;
    }

    public void setLinkRankingList(List<StringIntPair> linkRankingList) {
        this.linkRankingList = linkRankingList;
    }

    public List<StringIntPair> getDelRankingList() {
        return (delRankingList==null) ? chatLineDao.getDeletedMsgRanking() : delRankingList;
    }

    public void setDelRankingList(List<StringIntPair> delRankingList) {
        this.delRankingList = delRankingList;
    }

    public List<StringIntPair> getSentWordRankingList() {
        return (sentWordRankingList==null) ? chatLineDao.getAverageWordCountRanking() : sentWordRankingList;
    }

    public void setSentWordRankingList(List<StringIntPair> sentWordRankingList) {
        this.sentWordRankingList = sentWordRankingList;
    }

    public List<StringIntPair> getWordLengthRankingList() {
        return (wordLengthRankingList==null) ? wordDao.getAverageLetterCountByRank() : wordLengthRankingList;
    }

    public void setWordLengthRankingList(List<StringIntPair> wordLengthRankingList) {
        this.wordLengthRankingList = wordLengthRankingList;
    }

    public double getAvgDaysActive() {
        if(avgDaysActive==0){
            int count = 0;
            double aggregate = 0;
            for(StringIntPair sip : getDaysActiveRankingList()){
                count++;
                aggregate += sip.getFrequency();
            }
            return aggregate / count;
        } else {
            return avgDaysActive;
        }
    }

    public void setAvgDaysActive(double avgDaysActive) {
        this.avgDaysActive = avgDaysActive;
    }
}
