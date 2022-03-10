package com.juns.sdk.h5;

import com.juns.sdk.framework.utils.SPUtils;

public class SDKData {
    private static final String SDK_SP_FILE = "juns_sdk";
    private static final SPUtils SP_UTILS = SPUtils.getInstance(SDK_SP_FILE);


    //首次激活
    private static final String SDK_OAID = "sdk_oaid";
    private static final String SDK_ANDROID_ID = "sdk_android_id";


    public static String getSdkOaid() {
        return SP_UTILS.getString(SDK_OAID, "");
    }

    public static void setSdkOaid(String oaid) {
        SP_UTILS.put(SDK_OAID, oaid);
    }

    public static String getSdkAndroidId() {
        return SP_UTILS.getString(SDK_ANDROID_ID, "");
    }

    public static void setSdkAndroidId(String androidId) {
        SP_UTILS.put(SDK_ANDROID_ID, androidId);
    }
}
