package com.rexyrex.kakaoparser.Entities;

public class StringBoolPair {
    private String str;
    private boolean bool;

    public StringBoolPair(String str, boolean bool) {
        this.str = str;
        this.bool = bool;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public boolean isBool() {
        return bool;
    }

    public void setBool(boolean bool) {
        this.bool = bool;
    }
}
