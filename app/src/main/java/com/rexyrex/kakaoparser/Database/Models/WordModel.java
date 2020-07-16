package com.rexyrex.kakaoparser.Database.Models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.rexyrex.kakaoparser.Database.Converters.DateConverter;

import java.util.Date;

@Entity(tableName = "tb_word", indices = {@Index("line_id")}, foreignKeys = @ForeignKey(entity = ChatLineModel.class, parentColumns = "id", childColumns = "line_id", onDelete = ForeignKey.CASCADE))
@TypeConverters(DateConverter.class)
public class WordModel {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    @ColumnInfo(name="line_id")
    private int lineId;

    private Date date;
    private String author;
    private String word;

    private int letterCount;
    private boolean isLink;
    private boolean isPic;
    private boolean isVideo;
    private boolean isPowerpoint;

    @Ignore
    public WordModel(){

    }

    public WordModel(int lineId, Date date, String author, String word, boolean isLink, boolean isPic, boolean isVideo, boolean isPowerpoint, int letterCount){
        this.lineId = lineId;
        this.date = date;
        this.author = author;
        this.word = word;
        this.isLink = isLink;
        this.isPic = isPic;
        this.isVideo = isVideo;
        this.isPowerpoint = isPowerpoint;
        this.letterCount = letterCount;
    }

    public int getLetterCount() {
        return letterCount;
    }

    public void setLetterCount(int letterCount) {
        this.letterCount = letterCount;
    }

    public boolean isLink() {
        return isLink;
    }

    public void setLink(boolean link) {
        isLink = link;
    }

    public boolean isPic() {
        return isPic;
    }

    public void setPic(boolean pic) {
        isPic = pic;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public boolean isPowerpoint() {
        return isPowerpoint;
    }

    public void setPowerpoint(boolean powerpoint) {
        isPowerpoint = powerpoint;
    }

    public int getLineId() {
        return lineId;
    }

    public void setLineId(int lineId) {
        this.lineId = lineId;
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
