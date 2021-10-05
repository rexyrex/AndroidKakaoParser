package com.rexyrex.kakaoparser.Entities;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rexyrex.kakaoparser.Database.MainDatabase;
import com.rexyrex.kakaoparser.Database.Models.ChatLineModel;
import com.rexyrex.kakaoparser.R;
import com.rexyrex.kakaoparser.Utils.SharedPrefUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PersonChatData {

    public List<DateIntPair> timePreloadDayList;
    public List<StringIntPair> timePreloadMonthList;
    public List<StringIntPair> timePreloadYearList;
    public List<StringIntPair> timePreloadTimeOfDayList;
    public List<StringIntPair> timePreloadDayOFWeekList;

    public List<StringIntPair> top10Words;
    public int distinctWordCount;

    public ArrayList<PersonGeneralInfoData> statsList;
    public HashMap<String, List<StringStringPair>> statsDtlMap;

    Type listStringIntPairType = new TypeToken< List <StringIntPair> >() {}.getType();
    Type listDateIntPairType = new TypeToken< List <DateIntPair> >() {}.getType();

    Type listPersonGeneralInfoDataType = new TypeToken< ArrayList<PersonGeneralInfoData> >() {}.getType();
    Type hashMapType = new TypeToken< HashMap<String, List<StringStringPair>> >() {}.getType();

    SharedPrefUtils spu;

    public PersonChatData(Context c) {
        spu = new SharedPrefUtils(c);
    }

    private static PersonChatData the_instance;
    public static PersonChatData getInstance(Context c) {
        if (the_instance == null) {
            the_instance = new PersonChatData(c);
        }

        return the_instance;
    }

    public List<DateIntPair> getTimePreloadDayList() {
        return (timePreloadDayList == null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_PERSON_TIME_DAY_LIST, ""), listDateIntPairType) : timePreloadDayList;
    }

    public void setTimePreloadDayList(List<DateIntPair> timePreloadDayList) {
        this.timePreloadDayList = timePreloadDayList;
        spu.saveString(R.string.SP_BACKUP_PERSON_TIME_DAY_LIST, new Gson().toJson(timePreloadDayList));
    }

    public List<StringIntPair> getTimePreloadMonthList() {
        return (timePreloadMonthList == null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_PERSON_TIME_MONTH_LIST, ""), listStringIntPairType) : timePreloadMonthList;
    }

    public void setTimePreloadMonthList(List<StringIntPair> timePreloadMonthList) {
        this.timePreloadMonthList = timePreloadMonthList;
        spu.saveString(R.string.SP_BACKUP_PERSON_TIME_MONTH_LIST, new Gson().toJson(timePreloadMonthList));
    }

    public List<StringIntPair> getTimePreloadYearList() {
        return (timePreloadYearList == null) ? new Gson().fromJson(spu.getString(R.string.SP_BACKUP_PERSON_TIME_YEAR_LIST, ""), listStringIntPairType) : timePreloadYearList;
    }

    public void setTimePreloadYearList(List<StringIntPair> timePreloadYearList) {
        this.timePreloadYearList = timePreloadYearList;
        spu.saveString(R.string.SP_BACKUP_PERSON_TIME_YEAR_LIST, new Gson().toJson(timePreloadYearList));
    }

    public List<StringIntPair> getTimePreloadTimeOfDayList() {
        return (timePreloadTimeOfDayList==null)?new Gson().fromJson(spu.getString(R.string.SP_BACKUP_PERSON_TIME_OF_DAY_LIST, ""), listStringIntPairType) : timePreloadTimeOfDayList;
    }

    public void setTimePreloadTimeOfDayList(List<StringIntPair> timePreloadTimeOfDayList) {
        this.timePreloadTimeOfDayList = timePreloadTimeOfDayList;
        spu.saveString(R.string.SP_BACKUP_PERSON_TIME_OF_DAY_LIST, new Gson().toJson(timePreloadTimeOfDayList));
    }

    public List<StringIntPair> getTimePreloadDayOFWeekList() {
        return (timePreloadDayOFWeekList==null)?new Gson().fromJson(spu.getString(R.string.SP_BACKUP_PERSON_TIME_DAY_OF_WEEK_LIST, ""), listStringIntPairType) : timePreloadDayOFWeekList;
    }

    public void setTimePreloadDayOFWeekList(List<StringIntPair> timePreloadDayOFWeekList) {
        this.timePreloadDayOFWeekList = timePreloadDayOFWeekList;
        spu.saveString(R.string.SP_BACKUP_PERSON_TIME_DAY_OF_WEEK_LIST, new Gson().toJson(timePreloadDayOFWeekList));
    }

    public List<StringIntPair> getTop10Words() {
        return (top10Words==null)?new Gson().fromJson(spu.getString(R.string.SP_BACKUP_PERSON_top10Words, ""), listStringIntPairType) : top10Words;
    }

    public void setTop10Words(List<StringIntPair> top10Words) {
        this.top10Words = top10Words;
        spu.saveString(R.string.SP_BACKUP_PERSON_top10Words, new Gson().toJson(top10Words));
    }

    public int getDistinctWordCount() {
        return (distinctWordCount==0)?spu.getInt(R.string.SP_BACKUP_PERSON_distinctWordCount, 0):distinctWordCount;
    }

    public void setDistinctWordCount(int distinctWordCount) {
        this.distinctWordCount = distinctWordCount;
        spu.saveInt(R.string.SP_BACKUP_PERSON_distinctWordCount, distinctWordCount);
    }

    public ArrayList<PersonGeneralInfoData> getStatsList() {
        return (statsList==null)?new Gson().fromJson(spu.getString(R.string.SP_BACKUP_PERSON_statsList, ""), listPersonGeneralInfoDataType) : statsList;
    }

    public void setStatsList(ArrayList<PersonGeneralInfoData> statsList) {
        this.statsList = statsList;
        spu.saveString(R.string.SP_BACKUP_PERSON_statsList, new Gson().toJson(statsList));
    }

    public HashMap<String, List<StringStringPair>> getStatsDtlMap() {
        return (statsDtlMap==null)?new Gson().fromJson(spu.getString(R.string.SP_BACKUP_PERSON_statsDtlMap, ""), hashMapType) : statsDtlMap;
    }

    public void setStatsDtlMap(HashMap<String, List<StringStringPair>> statsDtlMap) {
        this.statsDtlMap = statsDtlMap;
        spu.saveString(R.string.SP_BACKUP_PERSON_statsDtlMap, new Gson().toJson(statsDtlMap));
    }
}
