package com.rexyrex.kakaoparser.Utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;

import com.google.android.gms.ads.AdSize;

public class AdUtils {
    public static AdSize getAdSize(Context c) {
        Activity a = (Activity) c;
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = a.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(c, adWidth);
    }
}
