package com.juns.sdk.core.sdk;

import android.text.TextUtils;

import com.juns.sdk.framework.common.Dev;
import com.juns.sdk.framework.utils.AppUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Data：2020-05-20-15:35
 * Author: ranger
 */
public class JunSUtils {

    public static String buildUrlParams(String url, HashMap<String, String> params) {
        String requestStr = "";
        if (params != null) {
            StringBuilder sb = new StringBuilder();
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String key = entry.getKey();
                String val = entry.getValue();
                sb.append(key + "=" + val + "&");
            }
            requestStr = sb.toString();
            //屏蔽掉最后一个"&"
            requestStr = requestStr.substring(0, requestStr.lastIndexOf("&"));
        }
        if (!TextUtils.isEmpty(requestStr)) {
            return url + "?" + requestStr;
        }
        return url;
    }

    public static String buildCommonWebUrl(String url) {
        return buildCommonWebUrl(url, false);
    }

    public static String buildCommonWebUrl(String url, boolean isLogin) {
        HashMap<String, String> params = new HashMap<>();
        params.put("pid", SDKData.getSdkPID());
        params.put("gid", SDKData.getSdkGID());
        params.put("refer", SDKData.getSdkRefer());
        params.put("sdkver", SDKData.getSdkVer());
        params.put("version", AppUtils.getAppVersionName(SDKCore.getMainAct().getPackageName()));
        params.put("duid", SDKData.getSDKDuid());
        params.put("mac", Dev.getMacAddress(SDKCore.getMainAct()));
        params.put("imei", Dev.getPhoneIMEI(SDKCore.getMainAct()));
        if(!SDKData.getCODE().equals("")){
            params.put("code", SDKData.getCODE());
            SDKData.setCODE("");
        }
        if (isLogin) {
            params.put("uname", SDKData.getSdkUserName());
            params.put("uid", SDKData.getSdkUserId());
            params.put("token", SDKData.getSdkUserToken());
        }
        return buildUrlParams(url, params);
    }

    public static String buildUrlWithUserName(String url, String userName) {
        HashMap<String, String> params = new HashMap<>();
        params.put("pid", SDKData.getSdkPID());
        params.put("gid", SDKData.getSdkGID());
        params.put("refer", SDKData.getSdkRefer());
        params.put("sdkver", SDKData.getSdkVer());
        params.put("version", AppUtils.getAppVersionName(SDKCore.getMainAct().getPackageName()));
        params.put("duid", SDKData.getSDKDuid());
        params.put("mac", Dev.getMacAddress(SDKCore.getMainAct()));
        params.put("imei", Dev.getPhoneIMEI(SDKCore.getMainAct()));
        if (!TextUtils.isEmpty(userName)) {
            params.put("uname", userName);
        }
        return buildUrlParams(url, params);
    }

}
