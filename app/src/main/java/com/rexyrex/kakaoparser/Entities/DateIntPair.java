package com.rexyrex.kakaoparser.Entities;

import java.util.Date;

public class DateIntPair {
    private Date date;
    private int frequency;

    @Override
    public int hashCode() {
        return date.hashCode();
    }

    @Override
    public String toString() {
        return date + ":" + frequency;
    }

    public DateIntPair(Date date, int frequency) {
        super();
        this.date = date;
        this.frequency = frequency;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public int getFrequency() {
        return frequency;
    }
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
