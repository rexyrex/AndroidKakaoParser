package com.rexyrex.kakaoparser.Entities;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rexyrex.kakaoparser.Constants.DateFormats;
import com.rexyrex.kakaoparser.Constants.TextPatterns;
import com.rexyrex.kakaoparser.Database.DAO.AnalysedChatDAO;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.AnalysedChatModel;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.FileParseUtils;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

import java.io.File;
import java.lang.reflect.Type;
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

    public List<DateIntPair> timePreloadDayList;
    public List<StringIntPair> timePreloadMonthList;
    public List<StringIntPair> timePreloadYearList;
    public List<StringIntPair> timePreloadTimeOfDayList;

    List<ChatLineModel> allChatInit;

    List<String> authorsList;

    Type listStringType = new TypeToken< List <String> >() {}.getType();
    Type listChatLineModelType = new TypeToken< List <ChatLineModel> >() {}.getType();
    Type listStringIntPairType = new TypeToken< List <StringIntPair> >() {}.getType();
    Type listDateIntPairType = new TypeToken< List <DateIntPair> >() {}.getType();

    MainDatabase database;
    AnalysedChatDAO analysedChatDAO;
    SharedPrefUtils spu;

    boolean reset;

    public ChatData(Context c) {
        spu = new SharedPrefUtils(c);

        database = MainDatabase.getDatabase(c);
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
        return (authorsList == null || authorsList.size() == 0) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_AuthorsList, ""), listStringType) : authorsList;
    }

    public void setAuthorsList(List<String> authorsList) {
        this.authorsList = authorsList;
    }

    public List<ChatLineModel> getAllChatInit() {
        return (allChatInit == null || allChatInit.size()==0 ) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_AllChatInit, ""), listChatLineModelType) : allChatInit;
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
        return (freqByDayOfWeek == null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_TIME_DAY_OF_WEEK_LIST, ""), listStringIntPairType) : freqByDayOfWeek;
    }

    public void setFreqByDayOfWeek(List<StringIntPair> freqByDayOfWeek) {
        this.freqByDayOfWeek = freqByDayOfWeek;
    }

    public int getMaxFreqByDayOfWeek() {
        return (maxFreqByDayOfWeek == 0) ? spu.getInt(R.string.SP_BACKUP_MaxFreqByDayOfWeek, 0) : maxFreqByDayOfWeek;
    }

    public void setMaxFreqByDayOfWeek(int maxFreqByDayOfWeek) {
        this.maxFreqByDayOfWeek = maxFreqByDayOfWeek;
    }

    public List<StringIntPair> getWordFreqArrList() {
        return (wordFreqArrList == null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_WordFreqArrList, ""), listStringIntPairType) : wordFreqArrList;
    }

    public void setWordFreqArrList(List<StringIntPair> wordFreqArrList) {
        this.wordFreqArrList = wordFreqArrList;
    }

    public List<StringIntPair> getTop10Chatters() {
        return (top10Chatters==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_Top10Chatters, ""), listStringIntPairType) : top10Chatters;
    }

    public void setTop10Chatters(List<StringIntPair> top10Chatters) {
        this.top10Chatters = top10Chatters;
    }

    public List<StringIntPair> getTop10ChattersByWord() {
        return (top10ChattersByWord==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_Top10ChattersByWords, ""), listStringIntPairType) : top10ChattersByWord;
    }

    public void setTop10ChattersByWord(List<StringIntPair> top10ChattersByWord) {
        this.top10ChattersByWord = top10ChattersByWord;
    }

    public List<StringIntPair> getTop10ChattersByPic() {
        return (top10ChattersByPic==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_Top10ChattersByPic, ""), listStringIntPairType) : top10ChattersByPic;
    }

    public void setTop10ChattersByPic(List<StringIntPair> top10ChattersByPic) {
        this.top10ChattersByPic = top10ChattersByPic;
    }

    public List<StringIntPair> getTop10ChattersByVideo() {
        return (top10ChattersByVideo==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_Top10ChattersByVideo, ""), listStringIntPairType) : top10ChattersByVideo;
    }

    public void setTop10ChattersByVideo(List<StringIntPair> top10ChattersByVideo) {
        this.top10ChattersByVideo = top10ChattersByVideo;
    }

    public List<StringIntPair> getTop10ChattersByLink() {
        return (top10ChattersByLink==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_Top10ChattersByLink, ""), listStringIntPairType) : top10ChattersByLink;
    }

    public void setTop10ChattersByLink(List<StringIntPair> top10ChattersByLink) {
        this.top10ChattersByLink = top10ChattersByLink;
    }

    public List<StringIntPair> getTop10ChattersByDeletedMsg() {
        return (top10ChattersByDeletedMsg==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_Top10ChattersByDeletedMsg, ""), listStringIntPairType) : top10ChattersByDeletedMsg;
    }

    public void setTop10ChattersByDeletedMsg(List<StringIntPair> top10ChattersByDeletedMsg) {
        this.top10ChattersByDeletedMsg = top10ChattersByDeletedMsg;
    }

    public List<StringIntPair> getChatterFreqArrList() {
        return (chatterFreqArrList==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_ChatterFrequencyPairs, ""), listStringIntPairType) : chatterFreqArrList;
    }

    public void setChatterFreqArrList(List<StringIntPair> chatterFreqArrList) {
        this.chatterFreqArrList = chatterFreqArrList;
    }

    public int getDayCount() {
        return (dayCount==0) ? spu.getInt(R.string.SP_BACKUP_DayCount, 0) : dayCount;
    }

    public void setDayCount(int dayCount) {
        this.dayCount = dayCount;
    }

    public int getChatterCount() {
        if(chatterCount == 0){
            return spu.getInt(R.string.SP_BACKUP_ChatterCount, 0);
        } else {
            return chatterCount;
        }
    }

    public void setChatterCount(int chatterCount) {
        this.chatterCount = chatterCount;
        spu.saveInt(R.string.SP_BACKUP_ChatterCount, chatterCount);
    }

    public int getChatLineCount() {
        if(chatLineCount == 0){
            return spu.getInt(R.string.SP_BACKUP_ChatLineCount, 0);
        } else {
            return chatLineCount;
        }
    }

    public void setChatLineCount(int chatLineCount) {
        this.chatLineCount = chatLineCount;
    }

    public int getWordCount() {
        return (wordCount==0) ? spu.getInt(R.string.SP_BACKUP_WordCount, 0) : wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getTotalWordCount() {
        return (totalWordCount==0) ? spu.getInt(R.string.SP_BACKUP_TotalWordCount, 0) : totalWordCount;
    }

    public void setTotalWordCount(int totalWordCount) {
        this.totalWordCount = totalWordCount;
    }

    public double getAvgWordCount() {
        return (avgWordCount==0) ? spu.getDouble(R.string.SP_BACKUP_AvgWordCount, 0) : avgWordCount;
    }

    public void setAvgWordCount(double avgWordCount) {
        this.avgWordCount = avgWordCount;
    }

    public double getAvgLetterCount() {
        return (avgLetterCount==0) ? spu.getDouble(R.string.SP_BACKUP_AvgLetterCount, 0) : avgLetterCount;
    }

    public void setAvgLetterCount(double avgLetterCount) {
        this.avgLetterCount = avgLetterCount;
    }

    public int getLinkCount() {
        return (linkCount==0) ? spu.getInt(R.string.SP_BACKUP_LinkCount, 0) : linkCount;
    }

    public void setLinkCount(int linkCount) {
        this.linkCount = linkCount;
    }

    public int getPicCount() {
        return (picCount==0) ? spu.getInt(R.string.SP_BACKUP_PicCount, 0) : picCount;
    }

    public void setPicCount(int picCount) {
        this.picCount = picCount;
    }

    public int getVideoCount() {
        return (videoCount==0) ? spu.getInt(R.string.SP_BACKUP_VideoCount, 0) : videoCount;
    }

    public void setVideoCount(int videoCount) {
        this.videoCount = videoCount;
    }

    public int getPptCount() {
        return (pptCount==0) ? spu.getInt(R.string.SP_BACKUP_PptCount, 0) : pptCount;
    }

    public void setPptCount(int pptCount) {
        this.pptCount = pptCount;
    }

    public int getDeletedMsgCount() {
        return deletedMsgCount==0 ? spu.getInt(R.string.SP_BACKUP_DeletedMsgCount, 0) : deletedMsgCount;
    }

    public void setDeletedMsgCount(int deletedMsgCount) {
        this.deletedMsgCount = deletedMsgCount;
    }

    public List<StringIntPair> getDaysActiveRankingList() {
        return (daysActiveRankingList==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_DaysActiveRankingList, ""), listStringIntPairType) : daysActiveRankingList;
    }

    public void setDaysActiveRankingList(List<StringIntPair> daysActiveRankingList) {
        this.daysActiveRankingList = daysActiveRankingList;
    }

    public List<StringIntPair> getDistinctWordRankingList() {
        return (daysActiveRankingList==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_DistinctWordRankingList, ""), listStringIntPairType) : daysActiveRankingList;
    }

    public void setDistinctWordRankingList(List<StringIntPair> distinctWordRankingList) {
        this.distinctWordRankingList = distinctWordRankingList;
    }

    public List<StringIntPair> getChatLineRankingList() {
        return (chatLineRankingList==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_ChatLineRankingList, ""), listStringIntPairType) : chatLineRankingList;
    }

    public void setChatLineRankingList(List<StringIntPair> chatLineRankingList) {
        this.chatLineRankingList = chatLineRankingList;
    }

    public List<StringIntPair> getTotalWordRankingList() {
        return (totalWordRankingList==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_TotalWordRankingList, ""), listStringIntPairType) : totalWordRankingList;
    }

    public void setTotalWordRankingList(List<StringIntPair> totalWordRankingList) {
        this.totalWordRankingList = totalWordRankingList;
    }

    public List<StringIntPair> getPicRankingList() {
        return (picRankingList==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_PicRankingList, ""), listStringIntPairType) : picRankingList;
    }

    public void setPicRankingList(List<StringIntPair> picRankingList) {
        this.picRankingList = picRankingList;
    }

    public List<StringIntPair> getVideoRankingList() {
        return (videoRankingList==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_VideoRankingList, ""), listStringIntPairType) : videoRankingList;
    }

    public void setVideoRankingList(List<StringIntPair> videoRankingList) {
        this.videoRankingList = videoRankingList;
    }

    public List<StringIntPair> getLinkRankingList() {
        return (linkRankingList==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_LinkRankingList, ""), listStringIntPairType) : linkRankingList;
    }

    public void setLinkRankingList(List<StringIntPair> linkRankingList) {
        this.linkRankingList = linkRankingList;
    }

    public List<StringIntPair> getDelRankingList() {
        return (delRankingList==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_DelRankingList, ""), listStringIntPairType) : delRankingList;
    }

    public void setDelRankingList(List<StringIntPair> delRankingList) {
        this.delRankingList = delRankingList;
    }

    public List<StringIntPair> getSentWordRankingList() {
        return (sentWordRankingList==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_SentWordRankingList, ""), listStringIntPairType) : sentWordRankingList;
    }

    public void setSentWordRankingList(List<StringIntPair> sentWordRankingList) {
        this.sentWordRankingList = sentWordRankingList;
    }

    public List<StringIntPair> getWordLengthRankingList() {
        return (wordLengthRankingList==null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_WordLengthRankingList, ""), listStringIntPairType) : wordLengthRankingList;
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

    public List<DateIntPair> getTimePreloadDayList() {
        return (timePreloadDayList == null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_TIME_DAY_LIST, ""), listDateIntPairType) : timePreloadDayList;
    }

    public void setTimePreloadDayList(List<DateIntPair> timePreloadDayList) {
        this.timePreloadDayList = timePreloadDayList;
    }

    public List<StringIntPair> getTimePreloadMonthList() {
        return (timePreloadMonthList == null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_TIME_MONTH_LIST, ""), listStringIntPairType) : timePreloadMonthList;
    }

    public void setTimePreloadMonthList(List<StringIntPair> timePreloadMonthList) {
        this.timePreloadMonthList = timePreloadMonthList;
    }

    public List<StringIntPair> getTimePreloadYearList() {
        return (timePreloadYearList == null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_TIME_YEAR_LIST, ""), listStringIntPairType) : timePreloadYearList;
    }

    public void setTimePreloadYearList(List<StringIntPair> timePreloadYearList) {
        this.timePreloadYearList = timePreloadYearList;
    }

    public List<StringIntPair> getTimePreloadTimeOfDayList() {
        return (timePreloadTimeOfDayList==null)?new Gson().fromJson(spu.getString(R.string.SP_BACKUP_TIME_OF_DAY_LIST, ""), listStringIntPairType) : timePreloadTimeOfDayList;
    }

    public void setTimePreloadTimeOfDayList(List<StringIntPair> timePreloadTimeOfDayList) {
        this.timePreloadTimeOfDayList = timePreloadTimeOfDayList;
    }
}
