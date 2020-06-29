package com.rexyrex.kakaoparser.Entities;

public class Pair {
    private String word;
    private int frequency;

    @Override
    public int hashCode() {
        return word.hashCode();
    }

    @Override
    public String toString() {
        return word + ":" + frequency;
    }

    public Pair(String word, int frequency) {
        super();
        this.word = word;
        this.frequency = frequency;
    }

    public String getword() {
        return word;
    }
    public void setword(String word) {
        this.word = word;
    }
    public int getFrequency() {
        return frequency;
    }
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
}
