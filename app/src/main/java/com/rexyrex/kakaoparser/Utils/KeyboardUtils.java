package com.rexyrex.kakaoparser.Utils;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class KeyboardUtils {
    public static void hideKeyboard(Activity act){
        InputMethodManager imm = (InputMethodManager) act.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = act.getCurrentFocus();
        if (view == null) {
            view = new View(act);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
