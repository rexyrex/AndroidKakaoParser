package com.rexyrex.kakaoparser.Entities;

public class PersonGeneralInfoData {
    private String categoryTitle;
    private String rawData;
    private String rawTotalData;
    private String diffFromRawTotalData;
    private double diffFromAvg;
    private double avg;
    private int ranking;
    private int total;
    private int type;

    public PersonGeneralInfoData(String categoryTitle, String rawData) {
        this.categoryTitle = categoryTitle;
        this.rawData = rawData;
        this.type=0;
    }

    public PersonGeneralInfoData(String categoryTitle, String rawData, double diffFromAvg, double avg, int ranking, int total) {
        this.categoryTitle = categoryTitle;
        this.rawData = rawData;
        this.diffFromAvg = diffFromAvg;
        this.ranking = ranking;
        this.total = total;
        this.avg = avg;
        this.type=1;
    }

    public PersonGeneralInfoData(String categoryTitle, String rawData, String rawTotalData, String diffFromRawTotalData, double diffFromAvg, double avg, int ranking, int total) {
        this.categoryTitle = categoryTitle;
        this.rawData = rawData;
        this.rawTotalData = rawTotalData;
        this.diffFromRawTotalData = diffFromRawTotalData;
        this.diffFromAvg = diffFromAvg;
        this.avg = avg;
        this.ranking = ranking;
        this.total = total;
        this.type=2;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void setCategoryTitle(String categoryTitle) {
        this.categoryTitle = categoryTitle;
    }

    public String getRawData() {
        return rawData;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public double getDiffFromAvg() {
        return diffFromAvg;
    }

    public void setDiffFromAvg(double diffFromAvg) {
        this.diffFromAvg = diffFromAvg;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getRawTotalData() {
        return rawTotalData;
    }

    public void setRawTotalData(String rawTotalData) {
        this.rawTotalData = rawTotalData;
    }

    public String getDiffFromRawTotalData() {
        return diffFromRawTotalData;
    }

    public void setDiffFromRawTotalData(String diffFromRawTotalData) {
        this.diffFromRawTotalData = diffFromRawTotalData;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
