package com.juns.sdk.core.sdk.update;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * UserManager: Ranger
 * Date: 12/04/2018
 * Time: 3:24 PM
 */
public class TNUpdate {
    public static void startUpdate(Context ctx, boolean force, String downloadUrl, String tips, UpdateView.UpdateViewCallback callback) {
        UpdateView updateDialog = new UpdateView(ctx, force, downloadUrl, tips, callback)
                .showAnim(new BounceBottomEnter())
                .dismissAnim(new ZoomOutExit())
                .dimEnabled(true);
        updateDialog.show();
    }

    static String getFileNameFormUrl(String downloadUrl) {
        String fileName = null;
        String pre = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
        if (!TextUtils.isEmpty(pre)) {
            try {
                fileName = URLUtil.guessFileName(downloadUrl, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            fileName = format.format(new Date()) + "_juns_update.apk";
        }
        return fileName;
    }

}
