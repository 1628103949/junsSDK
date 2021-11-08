package com.juns.sdk.core.api;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;

import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.framework.TNFramework;
import com.juns.sdk.framework.utils.SDCardUtils;
import com.quicksdk.QuickSdkApplication;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Data：19/12/2018-4:44 PM
 * Author: rangerhg
 */
public class JunSSdkApplication extends QuickSdkApplication {
    private static JunSDebugConfig junSDebugConfig;
    private SDKApplication sdkApp;

    public JunSSdkApplication() {
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
        TNFramework.globalReady(JunSSdkApplication.this, getDebugConfig());
        sdkApp.proxyOnCreate(JunSSdkApplication.this);
        sdkApp.setLogSwitch(getDebugConfig());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        junSDebugConfig = JunSDebugConfig.init(base);
        sdkApp.proxyAttachBaseContext(this,base);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sdkApp.proxyOnTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        sdkApp.proxyOnConfigurationChanged(newConfig);
    }
}
