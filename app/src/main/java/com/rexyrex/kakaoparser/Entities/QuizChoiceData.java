package com.rexyrex.kakaoparser.Entities;

public class QuizChoiceData {
    boolean correct;
    String str;
    String answerStr;

    public QuizChoiceData(boolean correct, String str, String answerStr) {
        this.correct = correct;
        this.str = str;
        this.answerStr = answerStr;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public String getAnswerStr() {
        return answerStr;
    }

    public void setAnswerStr(String answerStr) {
        this.answerStr = answerStr;
    }
}
