package com.juns.sdk.core.http.params;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.common.Dev;
import com.juns.sdk.framework.safe.JunSEncrypt;
import com.juns.sdk.framework.utils.AppUtils;
import com.juns.sdk.framework.utils.EncryptUtils;
import com.juns.sdk.framework.xutils.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class ReferCommonParam extends RequestParams {

    JSONObject junSJson = null;
    String junshang = null;

    ReferCommonParam() {
        buildCommon();
    }

    private void buildCommon() {
        junSJson = new JSONObject();
        try {
            junSJson.put("pid", SDKData.getSdkPID());
            junSJson.put("gid", SDKData.getSdkGID());
            junSJson.put("refer", SDKData.getSdkRefer());
            junSJson.put("version", AppUtils.getAppVersionName(SDKCore.getMainAct().getPackageName()));
            junSJson.put("sdkver", JunSConstants.SDK_VERSION);
            junSJson.put("time", String.valueOf(System.currentTimeMillis() / 1000));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void encryptGInfo(String url) {
        //获取接口二级域名
        try {
            URL mUrl = new URL(url);
            SDKCore.logger.print("http juns --> " + getJunSInfo());
            junshang = JunSEncrypt.encryptDInfo(mUrl.getHost(), JunSConstants.ENCRYPT_IV, getJunSInfo());
            junSJson = null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    String getJunSInfo() {
        if (junSJson == null) {
            return "";
        }
        return junSJson.toString();
    }

    String buildActiveSign() {
        try {
            StringBuilder preSignSb = new StringBuilder();
            preSignSb.append(SDKData.getSdkPID());
            preSignSb.append(SDKData.getSdkGID());
            preSignSb.append(SDKData.getSdkRefer());
            preSignSb.append(AppUtils.getAppVersionName(SDKCore.getMainAct().getPackageName()));
            preSignSb.append(JunSConstants.SDK_VERSION);
            preSignSb.append(junSJson.getString("time"));
            preSignSb.append(SDKData.getSdkAppKey());
            //Log.e("initParams",preSignSb.toString());
            return EncryptUtils.encryptMD5ToString(preSignSb.toString()).toLowerCase();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    String buildSign(String... args) {
        try {
            StringBuilder preSignSb = new StringBuilder();
            preSignSb.append(SDKData.getSdkPID());
            preSignSb.append(SDKData.getSdkGID());
            preSignSb.append(SDKData.getSdkRefer());
            preSignSb.append(AppUtils.getAppVersionName(SDKCore.getMainAct().getPackageName()));
            preSignSb.append(JunSConstants.SDK_VERSION);
            preSignSb.append(junSJson.getString("time"));
            preSignSb.append(SDKData.getSdkAppKey());
            if (args != null) {
                for (String arg : args) {
                    preSignSb.append(arg);
                }
            }
            return EncryptUtils.encryptMD5ToString(preSignSb.toString()).toLowerCase();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

}
