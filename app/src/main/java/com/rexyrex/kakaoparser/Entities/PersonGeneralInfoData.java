package com.rexyrex.kakaoparser.Entities;

public class PersonGeneralInfoData {
    private String categoryTitle;
    private String rawData;
    private double diffFromAvg;
    private int ranking;
    private int total;

    public PersonGeneralInfoData(String categoryTitle, String rawData, double diffFromAvg, int ranking, int total) {
        this.categoryTitle = categoryTitle;
        this.rawData = rawData;
        this.diffFromAvg = diffFromAvg;
        this.ranking = ranking;
        this.total = total;
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
}
