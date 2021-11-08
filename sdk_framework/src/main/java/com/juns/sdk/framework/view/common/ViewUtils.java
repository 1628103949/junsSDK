package com.juns.sdk.framework.view.common;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.juns.sdk.framework.common.ToastUtil;
import com.juns.sdk.framework.view.dialog.Attention.Tada;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;

//import com.juns.sdk.framework.permission.PermissionUtils;

/**
 * Data：28/09/2018-5:03 PM
 * Author: ranger
 */
public class ViewUtils {

    public static void showConfirmDialog(Context ctx, String title, String content, boolean cancelable, ConfirmDialog.ConfirmCallback callback) {
        if (TextUtils.isEmpty(content) || ctx == null || callback == null) {
            return;
        }
        ConfirmDialog commonConfirmDialog = new ConfirmDialog(ctx, title, content, cancelable, callback)
                .showAnim(new BounceBottomEnter())
                .dismissAnim(new ZoomOutExit())
                .dimEnabled(true);
        commonConfirmDialog.show();
    }

    public static void showConfirmDialog(Context ctx, String content, boolean cancelable, ConfirmDialog.ConfirmCallback callback) {
        if (TextUtils.isEmpty(content) || ctx == null || callback == null) {
            return;
        }
        ConfirmDialog commonConfirmDialog = new ConfirmDialog(ctx, content, cancelable, callback)
                .showAnim(new BounceBottomEnter())
                .dismissAnim(new ZoomOutExit())
                .dimEnabled(true);
        commonConfirmDialog.show();
    }

    public static void showConfirmDialog(Context ctx, SpannableStringBuilder spannableStringBuilder, boolean cancelable, ConfirmDialog.ConfirmCallback callback) {
        if (spannableStringBuilder == null || ctx == null || callback == null) {
            return;
        }
        ConfirmDialog commonConfirmDialog = new ConfirmDialog(ctx, spannableStringBuilder, cancelable, callback)
                .showAnim(new BounceBottomEnter())
                .dismissAnim(new ZoomOutExit())
                .dimEnabled(true);
        commonConfirmDialog.show();
    }

    public static void showTipsConfirm(Context ctx, String content, TipsDialog.TipsConfirmCallback callback) {
        if (ctx == null || TextUtils.isEmpty(content) || callback == null) {
            return;
        }
        TipsDialog commonTipsDialog = new TipsDialog(ctx, content, callback)
                .showAnim(new BounceBottomEnter())
                .dismissAnim(new ZoomOutExit())
                .dimEnabled(true);
        commonTipsDialog.show();
    }

    public static void showTipsDialog(Context ctx, boolean cancelable, String content, TipsDialog.TipsCallback callback) {
        if (ctx == null || TextUtils.isEmpty(content) || callback == null) {
            return;
        }
        TipsDialog commonTipsDialog = new TipsDialog(ctx, cancelable, content, callback)
                .showAnim(new BounceBottomEnter())
                .dismissAnim(new ZoomOutExit())
                .dimEnabled(true);
        commonTipsDialog.show();
    }

    public static void sdkShowTips(Context ctx, String msg) {
        ToastUtil.showShort(ctx, msg);
    }

}
