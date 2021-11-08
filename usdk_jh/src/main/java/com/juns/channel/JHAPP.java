package com.juns.channel;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.yaoyue.release.YYReleaseSDK;

public class JHAPP extends OPlatformAPP {
    private static final String TAG = "JHKAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public JHAPP(OPlatformBean bean) {
        super(bean);
    }
    Application application = null;
    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        this.application = application;
        YYReleaseSDK.getInstance().onAppCreate(application);
    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        YYReleaseSDK.getInstance().onAppAttachBaseContext(base,application);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        YYReleaseSDK.getInstance().onAppConfigurationChanged(configuration,
                application);
    }
}
