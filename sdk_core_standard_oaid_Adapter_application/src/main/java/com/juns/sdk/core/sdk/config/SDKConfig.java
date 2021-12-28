package com.juns.sdk.core.sdk.config;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.framework.common.TUitls;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Properties;

/**
 * Description: juns sdk config.
 * Data：26/09/2018-12:23 PM
 * Author: ranger
 */
public class SDKConfig {
    private static final String CONFIG_FILE_NAME = "juns" + File.separator + "juns_sdk.ini";

    private String gameId;
    private String pId;
    private String refer;
    //是否为横屏，0为竖屏，1为横屏
    private boolean isLand;
    private boolean isOaid;
    private boolean isAgreement;


    private SDKConfig(Context ctx) {
        Properties properties = TUitls.readAssetsConfig(ctx, CONFIG_FILE_NAME);
        if (properties == null) {
            throw new RuntimeException("juns sdk exception. juns/juns_sdk.ini config file read fail");
        }
        gameId = properties.getProperty("game_id");
        pId = properties.getProperty("pid");
        refer = properties.getProperty("refer");

        String isLandValue = null;
        if (properties.containsKey("isLand")) {
            isLandValue = properties.getProperty("is_land");
        }
        String isOaidValue = properties.getProperty("is_oaid");

        String isAgreementValue = properties.getProperty("is_agreement");
        if (TextUtils.isEmpty(isLandValue)) {
            isLand = true;
        } else {
            if (isLandValue.equals("1")) {
                isLand = true;
            } else {
                isLand = false;
            }
        }

        if (TextUtils.isEmpty(isAgreementValue)) {
            isAgreement = true;
        } else {
            if (isAgreementValue.equals("1")) {
                isAgreement = true;
            } else {
                isAgreement = false;
            }
        }

        if (TextUtils.isEmpty(isOaidValue)) {
            isOaid = true;
        } else {
            if (isOaidValue.equals("1")) {
                isOaid = true;
            } else {
                isOaid = false;
            }
        }

        if (TextUtils.isEmpty(gameId)) {
            throw new RuntimeException("juns sdk exception. juns/ch/juns_sdk.ini config error, game_id read exception, please check juns/ch/juns_sdk.ini.");
        }

        if (TextUtils.isEmpty(pId)) {
            throw new RuntimeException("juns sdk exception. juns/ch/juns_sdk.ini config error, pid read exception, please check juns/ch/juns_sdk.ini.");
        }

        if (TextUtils.isEmpty(refer)) {
            throw new RuntimeException("juns sdk exception. juns/ch/juns_sdk.ini config error, refer read exception, please check juns/ch/juns_sdk.ini.");
        }
    }

    public static SDKConfig init(Context ctx) {
        return new SDKConfig(ctx);
    }

    public String getGameId() {
        return gameId;
    }

    public String getPtId() {
        return pId;
    }

    public String getRefer() {
        return refer;
    }

    public boolean isOaid() {
        //Log.e("guoinfoiisOaid","is:"+isOaid);
        return isOaid;
    }

    public boolean isAgreement() {
        //Log.e("guoinfoiisAgreement","is:"+isAgreement);
        return isAgreement;
    }

    public boolean isLand() {
        return isLand;
    }

    @Override
    public String toString() {
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("game_id", gameId);
            dataJson.put("pid", pId);
            dataJson.put("refer", refer);
            dataJson.put("is_land", isLand);
            dataJson.put("sdk_version", JunSConstants.SDK_VERSION);
            return dataJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "null";
    }
}
