package com.rexyrex.kakaoparser.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.rexyrex.kakaoparser.R;

public class PicUtils {
    public static Drawable getProfiePic(Context c, int index){
        switch(index){
            case 0 : return c.getResources().getDrawable(R.drawable.avatar0); 
            case 1 : return c.getResources().getDrawable(R.drawable.avatar1); 
            case 2 : return c.getResources().getDrawable(R.drawable.avatar2); 
            case 3 : return c.getResources().getDrawable(R.drawable.avatar3); 
            case 4 : return c.getResources().getDrawable(R.drawable.avatar4); 
            case 5 : return c.getResources().getDrawable(R.drawable.avatar5); 
            case 6 : return c.getResources().getDrawable(R.drawable.avatar6); 
            case 7 : return c.getResources().getDrawable(R.drawable.avatar7); 
            case 8 : return c.getResources().getDrawable(R.drawable.avatar8); 
            case 9 : return c.getResources().getDrawable(R.drawable.avatar9); 
            default : return c.getResources().getDrawable(R.drawable.avatar0); 
        }
    }
}
