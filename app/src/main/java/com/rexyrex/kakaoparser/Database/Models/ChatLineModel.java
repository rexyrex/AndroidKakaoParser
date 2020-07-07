package com.rexyrex.kakaoparser.Database.Models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.rexyrex.kakaoparser.Database.Converters.DateConverter;

import java.util.Date;

@Entity(tableName = "tb_chat_line")
@TypeConverters(DateConverter.class)
public class ChatLineModel {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    private String author;
    private Date date;
    private String dateDayString;
    private String content;

    public ChatLineModel(){

    }

    public ChatLineModel(Date date, String dateDayString, String author, String content){
        this.date = date;
        this.dateDayString = dateDayString;
        this.author = author;
        this.content = content;
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
