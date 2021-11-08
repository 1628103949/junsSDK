package com.juns.sdk.framework.common;

import android.content.Context;
import android.widget.Toast;

/**
 * Data：28/09/2018-5:05 PM
 * Author: ranger
 */
public class ToastUtil {

    private ToastUtil() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 短时间显示Toast
     *
     * @param context 上下文
     * @param message 显示消息
     */
    public static void showShort(Context context, CharSequence message) {
        Toast toast;
        toast = Toast.makeText(context, null, Toast.LENGTH_SHORT);
        toast.setText(message);
        toast.show();
    }

    /**
     * 长时间显示Toast
     *
     * @param context 上下文
     * @param message 显示消息
     */
    public static void showLong(Context context, CharSequence message) {
        Toast toast;
        toast = Toast.makeText(context, null, Toast.LENGTH_LONG);
        toast.setText(message);
        toast.show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context  上下文
     * @param message  显示消息
     * @param duration 时长
     */
    public static void show(Context context, CharSequence message, int duration) {
        Toast toast;
        toast = Toast.makeText(context, null, duration);
        toast.setText(message);
        toast.show();
    }

}
