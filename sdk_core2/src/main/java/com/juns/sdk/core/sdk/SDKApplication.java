package com.juns.sdk.core.sdk;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.bun.miitmdid.core.JLibrary;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.own.JunSPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.sdk.config.PlatformConfig;
import com.juns.sdk.core.sdk.config.SDKConfig;
import com.juns.sdk.framework.utils.ReflectUtils;

/**
 * Data：19/12/2018-5:01 PM
 * Author: ranger
 */
public class SDKApplication {
    private static final String[] TN_PT_ID = {"1"};

    private static SDKConfig sdkConfig;
    private static PlatformConfig platformConfig;
    private IPlatformAPP platformApp;

    public static SDKConfig getSdkConfig() {
        return sdkConfig;
    }

    public static PlatformConfig getPlatformConfig() {
        return platformConfig;
    }

    public static boolean isTNPlatform() {
        String ptId = sdkConfig.getPtId();
        for (String id : TN_PT_ID) {
            if (ptId.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public void proxyOnCreate(Application application) {
        SDKData.cleanSDKData();
        SDKData.setSdkGID(sdkConfig.getGameId());
        SDKData.setSdkPID(sdkConfig.getPtId());
        if(TextUtils.isEmpty(SDKData.getSdkKaRefer())){
            SDKData.setSdkRefer(sdkConfig.getRefer());
        }else{
            SDKData.setSdkRefer(SDKData.getSdkKaRefer());
            SDKData.setSdkIsRefer(false);
        }
        //SDKData.setSdkRefer(sdkConfig.getRefer());
        SDKData.setSdkVer(JunSConstants.SDK_VERSION);
        platformApp.onCreate(application);
    }

    public void proxyAttachBaseContext(Application application,Context base) {
        sdkConfig = SDKConfig.init(base);
        platformConfig = PlatformConfig.init(base);
        if (isTNPlatform()) {
            this.platformApp = (IPlatformAPP) ReflectUtils.reflect(JunSPlatformAPP.class).newInstance().get();
        } else {
            this.platformApp = (IPlatformAPP) ReflectUtils.reflect(platformConfig.getAppClass()).newInstance(OPlatformBean.init(SDKApplication.getSdkConfig(), SDKApplication.getPlatformConfig())).get();
        }
        platformApp.attachBaseContext(application,base);
        try {
            JLibrary.InitEntry(base);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLogSwitch(boolean logSwitch) {
        SDKData.setLogSwitch(logSwitch);
    }

}
