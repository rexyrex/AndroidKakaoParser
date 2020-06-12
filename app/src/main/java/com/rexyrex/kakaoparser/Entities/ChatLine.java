package com.rexyrex.kakaoparser.Entities;

import java.util.Date;

public class ChatLine {
    Date date;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    String author;
    String content;

    public ChatLine(Date date, String author, String content){
        this.date = date;
        this.author = author;
        this.content = content;
    }
}
