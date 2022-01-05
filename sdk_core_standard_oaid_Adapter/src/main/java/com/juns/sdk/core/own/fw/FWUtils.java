package com.juns.sdk.core.own.fw;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;

/**
 * Data：2019/3/27-9:43 AM
 * Author: ranger
 */
public class FWUtils {

    public static int getWindowHeight(Activity act) {
        Rect rect = new Rect();
        act.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.height();
    }

    public static int getWindowWidth(Activity act) {
        Rect rect = new Rect();
        act.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.width();
    }

    public static int getScreenLeft(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            View decorView = act.getWindow().getDecorView();
            WindowInsets rootWindowInsets = decorView.getRootWindowInsets();
            if (rootWindowInsets == null) {
                Log.e("JunS-sdk", "rootWindowInsets为空了");
                return 0;
            }
            DisplayCutout displayCutout = rootWindowInsets.getDisplayCutout();
            if(displayCutout == null){
                Log.e("JunS-sdk" , "displayCutout为空了");
                return 0;
            }

            return displayCutout.getSafeInsetLeft();
        }else {
            return 0;
        }
    }

    public static int getScreenRight(Activity act) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            View decorView = act.getWindow().getDecorView();
            WindowInsets rootWindowInsets = decorView.getRootWindowInsets();
            if (rootWindowInsets == null) {
                Log.e("JunS-sdk", "rootWindowInsets为空了");
                return 0;
            }
            DisplayCutout displayCutout = rootWindowInsets.getDisplayCutout();
            if(displayCutout == null){
                Log.e("JunS-sdk" , "displayCutout为空了");
                return 0;
            }

            return displayCutout.getSafeInsetRight();
        }else {
            return 0;
        }
    }


}
