package com.rexyrex.kakaoparser.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtils {

    SharedPreferences sharedPref;
    SharedPreferences.Editor sharedEditor;
    Context c;

    //Shared Preferences Library
    public SharedPrefUtils(Context c){
        sharedPref = c.getSharedPreferences("kakao_analyse_pref", Context.MODE_PRIVATE);
        sharedEditor = sharedPref.edit();
        this.c = c;
    }

    //keyID = int pointing to strings.xml
    public void saveString(int keyID, String value){
        sharedEditor.putString(c.getResources().getString(keyID), value);
        sharedEditor.apply();
    }

    public void saveBool(int keyID, Boolean value){
        sharedEditor.putBoolean(c.getResources().getString(keyID), value);
        sharedEditor.apply();
    }

    public void incInt(int keyID){
        sharedEditor.putInt(c.getResources().getString(keyID), getInt(keyID, 0) + 1);
        sharedEditor.apply();
    }

    public void saveInt(int keyID, int value){
        sharedEditor.putInt(c.getResources().getString(keyID), value);
        sharedEditor.apply();
    }

    public void saveLong(int keyID, long value){
        sharedEditor.putLong(c.getResources().getString(keyID), value);
        sharedEditor.apply();
    }

    public void saveDouble(int keyID, double value){
        sharedEditor.putLong(c.getResources().getString(keyID), Double.doubleToRawLongBits(value));
        sharedEditor.apply();
    }

    public String getString(int keyID, String defaultVal){
        return sharedPref.getString(c.getResources().getString(keyID), defaultVal);
    }

    public Boolean getBool(int keyID, Boolean defaultVal){
        return sharedPref.getBoolean(c.getResources().getString(keyID), defaultVal);
    }

    public int getInt(int keyID, int defaultVal){
        return sharedPref.getInt(c.getResources().getString(keyID), defaultVal);
    }

    public long getLong(int keyID, long defaultVal){
        return sharedPref.getLong(c.getResources().getString(keyID), defaultVal);
    }

    public double getDouble(int keyID, double defaultVal){
        return Double.longBitsToDouble(sharedPref.getLong(c.getResources().getString(keyID), Double.doubleToRawLongBits(defaultVal)));
    }


}
