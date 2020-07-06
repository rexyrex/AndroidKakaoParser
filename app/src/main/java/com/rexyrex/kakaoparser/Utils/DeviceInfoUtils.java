package com.rexyrex.kakaoparser.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class DeviceInfoUtils {

    //외부 앱 설치 여부
    public static boolean appInstalledOrNot(Activity activity, String uri) {
        PackageManager pm = activity.getPackageManager();
        try {
            pm.getPackageInfo(uri, 0);
            //check if enabled
            ApplicationInfo ai = pm.getApplicationInfo(uri,0);
            return ai.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    //주어진 권한중 아직 수락 안된 권한 return
    public static ArrayList<String> getDeniedPermissions(Context c, String[] permissions){
        ArrayList<String> perms = new ArrayList<String>();
        for(int i=0; i<permissions.length; i++){
            if (ContextCompat.checkSelfPermission((Activity)c, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                perms.add(permissions[i]);
            }
        }
        return perms;
    }

}
