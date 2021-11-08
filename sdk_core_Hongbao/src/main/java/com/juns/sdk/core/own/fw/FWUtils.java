package com.juns.sdk.core.own.fw;

import android.app.Activity;
import android.graphics.Rect;

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

}
