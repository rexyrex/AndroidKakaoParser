package com.rexyrex.kakaoparser.Entities;

public class HighscoreData {
    int highscore;
    String nickname;

    public HighscoreData(int highscore, String nickname) {
        this.highscore = highscore;
        this.nickname = nickname;
    }

    public int getHighscore() {
        return highscore;
    }

    public void setHighscore(int highscore) {
        this.highscore = highscore;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
