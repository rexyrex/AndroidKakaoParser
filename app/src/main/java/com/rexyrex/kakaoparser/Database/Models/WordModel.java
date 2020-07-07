package com.rexyrex.kakaoparser.Database.Models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.rexyrex.kakaoparser.Database.Converters.DateConverter;

import java.util.Date;

@Entity(tableName = "tb_word")
@TypeConverters(DateConverter.class)
public class WordModel {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    private Date date;
    private String author;
    private String word;

    public WordModel(){

    }

    public WordModel(Date date, String author, String word){
        this.date = date;
        this.author = author;
        this.word = word;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @NonNull
    @Override
    public String toString() {
        return date.toString() + " : " + author + " : " + word;
    }
}
