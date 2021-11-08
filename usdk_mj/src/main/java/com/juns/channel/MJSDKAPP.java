package com.juns.channel;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

import fusion.mj.means.proxy.FusionCommonSdk;

public class MJSDKAPP extends OPlatformAPP {
    private static final String TAG = "MJDKAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private Application application;
    public MJSDKAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        this.application = application;
        FusionCommonSdk.getInstance().onApplicationCreate(application);
    }

    @Override
    public void onTerminate(Application application) {
        super.onTerminate(application);
        FusionCommonSdk.getInstance().onApplicationTerminate(application);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        FusionCommonSdk.getInstance().onApplicationConfigurationChanged(application,newConfig);
    }

    @Override
    public void attachBaseContext(Application application,Context base) {
        super.attachBaseContext(application,base);
        FusionCommonSdk.getInstance().onApplicationAttachBaseContextInApplication(application,base);
    }
}
