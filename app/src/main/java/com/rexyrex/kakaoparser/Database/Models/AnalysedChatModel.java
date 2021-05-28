package com.rexyrex.kakaoparser.Database.Models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tb_analysed_chat", indices = {@Index("id")})
public class AnalysedChatModel {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name="id")
    @NonNull
    private int id;
    private String title;
    private String dt;

    @ColumnInfo(defaultValue = "0")
    private int highscore;

    @Ignore
    public AnalysedChatModel(){

    }

    public AnalysedChatModel(String title, String dt){
        this.title = title;
        this.dt = dt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public int getHighscore() {
        return highscore;
    }

    public void setHighscore(int highscore) {
        this.highscore = highscore;
    }
}
