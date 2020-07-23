package com.rexyrex.kakaoparser.Database.Models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.rexyrex.kakaoparser.Database.Converters.DateConverter;

import java.util.Date;

@Entity(tableName = "tb_chat_line", indices = {@Index("id")})
@TypeConverters(DateConverter.class)
public class ChatLineModel {
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name="id")
    @NonNull
    private int id;
    private String author;
    private Date date;
    private String dateDayString;
    private String dateMonthString;
    private String dateYearString;
    private String dateDayOfWeekString;
    private String dateHourOfDayString;
    private String content;
    private int wordCount;
    private int length;

    @Ignore
    public ChatLineModel(){

    }

    public ChatLineModel(int id, Date date, String dateDayString, String dateMonthString,
                         String dateYearString, String dateDayOfWeekString, String dateHourOfDayString,
                         String author, String content, int wordCount, int length){
        this.id = id;
        this.date = date;
        this.dateDayString = dateDayString;
        this.dateMonthString = dateMonthString;
        this.dateYearString = dateYearString;
        this.dateDayOfWeekString = dateDayOfWeekString;
        this.dateHourOfDayString = dateHourOfDayString;
        this.author = author;
        this.content = content;
        this.wordCount = wordCount;
        this.length = length;

    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getDateMonthString() {
        return dateMonthString;
    }

    public void setDateMonthString(String dateMonthString) {
        this.dateMonthString = dateMonthString;
    }

    public String getDateYearString() {
        return dateYearString;
    }

    public void setDateYearString(String dateYearString) {
        this.dateYearString = dateYearString;
    }

    public String getDateDayOfWeekString() {
        return dateDayOfWeekString;
    }

    public void setDateDayOfWeekString(String dateDayOfWeekString) {
        this.dateDayOfWeekString = dateDayOfWeekString;
    }

    public String getDateHourOfDayString() {
        return dateHourOfDayString;
    }

    public void setDateHourOfDayString(String dateHourOfDayString) {
        this.dateHourOfDayString = dateHourOfDayString;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDateDayString() {
        return dateDayString;
    }

    public void setDateDayString(String dateDayString) {
        this.dateDayString = dateDayString;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
