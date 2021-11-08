package com.juns.sdk.core.platform;

import com.juns.sdk.core.sdk.config.PlatformConfig;
import com.juns.sdk.core.sdk.config.SDKConfig;

/**
 * Data：19/12/2018-3:40 PM
 * Author: ranger
 */
public class OPlatformBean {

    private String showSplash;
    private String showExit;
    private boolean isLand;
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

    private OPlatformBean(SDKConfig sdkConfig, PlatformConfig platformConfig) {
        this.showSplash = platformConfig.getShowSplash();
        this.showExit = platformConfig.getShowExit();
        this.isLand = sdkConfig.isLand();
        this.pAppId = platformConfig.getpAppId();
        this.pAppKey = platformConfig.getpAppKey();
        this.pPayId = platformConfig.getpPayId();
        this.pPayKey = platformConfig.getpPayKey();
        this.pGameId = platformConfig.getpGameId();
        this.pGameName = platformConfig.getpGameName();
        this.ext1 = platformConfig.getExt1();
        this.ext2 = platformConfig.getExt2();
        this.ext3 = platformConfig.getExt3();
        this.ext4 = platformConfig.getExt4();
        this.ext5 = platformConfig.getExt5();
        this.ext6 = platformConfig.getExt6();
        this.ext7 = platformConfig.getExt7();
        this.ext8 = platformConfig.getExt8();
        this.ext9 = platformConfig.getExt9();
        this.ext10 = platformConfig.getExt10();
    }

    public static OPlatformBean init(SDKConfig sdkConfig, PlatformConfig platformConfig) {
        return new OPlatformBean(sdkConfig, platformConfig);
    }

    public String getShowSplash() {
        return showSplash;
    }

    public String getShowExit() {
        return showExit;
    }

    public boolean isLand() {
        return isLand;
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
}
