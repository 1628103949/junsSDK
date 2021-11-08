package com.juns.sdk.core.sdk.config;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.juns.sdk.framework.common.TUitls;
import com.juns.sdk.framework.safe.JunSEncrypt;

import java.io.File;
import java.util.Properties;

/**
 * Description: juns sdk platform config.
 * Data：26/09/2018-12:24 PM
 * Author: ranger
 */
public class PlatformConfig {
    private static final String CONFIG_FILE_NAME = "juns" + File.separator + "juns_platform.ini";

    private String platformClass;
    private String appClass;

    private String showSplash;
    private String showExit;

    //广告相关参数
    private String adsClass;
    private String adsAppId;
    private String adsAppName;

    //经过加密后
    private String pAppId;
    private String pAppKey;
    private String pPayId;
    private String pPayKey;


    private String pGameId;
    private String pGameName;

    private String ext1;
    private String ext2;
    private String ext3;
    private String ext4;
    private String ext5;
    private String ext6;
    private String ext7;
    private String ext8;
    private String ext9;
    private String ext10;

    private PlatformConfig(Context ctx) {
        Properties properties = TUitls.readAssetsConfig(ctx, CONFIG_FILE_NAME);
        if (properties != null) {
            //platform 必要参数
            platformClass = properties.getProperty("platformClass");
            appClass = properties.getProperty("appClass");
            showSplash = properties.getProperty("showSplash");
            showExit = properties.getProperty("showExit");

            //以下为选参
            //广告相关参数
            if (properties.containsKey("adsClass")) {
                adsClass = properties.getProperty("adsClass");
            }
            if (properties.containsKey("adsAppId")) {
                adsAppId = properties.getProperty("adsAppId");
            }
            if (properties.containsKey("adsAppName")) {
                adsAppName = properties.getProperty("adsAppName");
            }

            //加密后，需解密
            if (properties.containsKey("pAppId")) {
                pAppId = JunSEncrypt.decryptInfo(properties.getProperty("pAppId"));
            }
            if (properties.containsKey("pAppKey")) {
                pAppKey = JunSEncrypt.decryptInfo(properties.getProperty("pAppKey"));
            }
            if (properties.containsKey("pPayId")) {
                pPayId = JunSEncrypt.decryptInfo(properties.getProperty("pPayId"));
            }
            if (properties.containsKey("pPayKey")) {
                pPayKey = JunSEncrypt.decryptInfo(properties.getProperty("pPayKey"));
            }
            if (properties.containsKey("pGameId")) {
                pGameId = properties.getProperty("pGameId");
            }
            if (properties.containsKey("pGameName")) {
                pGameName = properties.getProperty("pGameName");
            }
            if (properties.containsKey("ext1")) {
                ext1 = properties.getProperty("ext1");
            }
            if (properties.containsKey("ext2")) {
                ext2 = properties.getProperty("ext2");
            }
            if (properties.containsKey("ext3")) {
                ext3 = properties.getProperty("ext3");
            }
            if (properties.containsKey("ext4")) {
                ext4 = properties.getProperty("ext4");
            }
            if (properties.containsKey("ext5")) {
                ext5 = properties.getProperty("ext5");
            }
            if (properties.containsKey("ext6")) {
                ext6 = properties.getProperty("ext6");
            }
            if (properties.containsKey("ext7")) {
                ext7 = properties.getProperty("ext7");
            }
            if (properties.containsKey("ext8")) {
                ext8 = properties.getProperty("ext8");
            }
            if (properties.containsKey("ext9")) {
                ext9 = properties.getProperty("ext9");
            }
            if (properties.containsKey("ext10")) {
                ext10 = properties.getProperty("ext10");
            }
        }
        //配置文件不存在，或相关项目没配置，生成默认值
        if (TextUtils.isEmpty(showSplash)) {
            //默认不显示闪屏
            showSplash = "0";
        }

        if (TextUtils.isEmpty(showExit)) {
            //默认使用渠道退出框
            showExit = "1";
        }

        if (TextUtils.isEmpty(platformClass)) {
            //默认platform
            platformClass = "com.juns.sdk.core.open.OPlatformSDK";
        }

        if (TextUtils.isEmpty(appClass)) {
            //默认app
            appClass = "com.juns.sdk.core.open.OPlatformAPP";
        }


    }

    public static PlatformConfig init(Context ctx) {
        return new PlatformConfig(ctx);
    }

    public String getPlatformClass() {
        return platformClass;
    }

    public String getAppClass() {
        return appClass;
    }

    public String getShowSplash() {
        return showSplash;
    }

    public String getShowExit() {
        return showExit;
    }

    public String getpAppId() {
        return pAppId;
    }

    public String getpAppKey() {
        return pAppKey;
    }

    public String getpPayId() {
        return pPayId;
    }

    public String getpPayKey() {
        return pPayKey;
    }

    public String getpGameId() {
        return pGameId;
    }

    public String getpGameName() {
        return pGameName;
    }

    public String getExt1() {
        return ext1;
    }

    public String getExt2() {
        return ext2;
    }

    public String getExt3() {
        return ext3;
    }

    public String getExt4() {
        return ext4;
    }

    public String getExt5() {
        return ext5;
    }

    public String getExt6() {
        return ext6;
    }

    public String getExt7() {
        return ext7;
    }

    public String getExt8() {
        return ext8;
    }

    public String getExt9() {
        return ext9;
    }

    public String getExt10() {
        return ext10;
    }

    public String getAdsClass() {
        return adsClass;
    }

    public String getAdsAppId() {
        return adsAppId;
    }

    public String getAdsAppName() {
        return adsAppName;
    }
}
