package com.juns.channel;

import android.content.Context;
import android.text.TextUtils;

import com.bytedance.ugame.rocketapi.RocketApplication;
import com.juns.sdk.core.api.JunSDebugConfig;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.framework.TNFramework;
import com.juns.sdk.framework.utils.SDCardUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class QingLanApplication extends RocketApplication {
    private static JunSDebugConfig junSDebugConfig;
    private SDKApplication sdkApp;

    public QingLanApplication() {
        this.sdkApp = new SDKApplication();
    }

    public static boolean getDebugConfig() {
        //获取SDCard Debug配置
        String sdCardDebugSwitch = "0";
        if (!TextUtils.isEmpty(SDCardUtils.getSDCardPath())) {
            String loggerFileDirPath = SDCardUtils.getSDCardPath() + "JunS_Debug.ini";
            try {
                File loggerFile = new File(loggerFileDirPath);
                if (loggerFile.exists()) {
                    FileInputStream fileInputStream = new FileInputStream(loggerFile);
                    Properties properties = new Properties();
                    properties.load(fileInputStream);
                    sdCardDebugSwitch = properties.getProperty("debugSwitch");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (sdCardDebugSwitch.equals("1") || junSDebugConfig.getDebugSwitch()) {
            return true;
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TNFramework.globalReady(QingLanApplication.this, getDebugConfig());
        sdkApp.proxyOnCreate(QingLanApplication.this);
        sdkApp.setLogSwitch(getDebugConfig());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        junSDebugConfig =  JunSDebugConfig.init(base);
        sdkApp.proxyAttachBaseContext(base);
    }
}
