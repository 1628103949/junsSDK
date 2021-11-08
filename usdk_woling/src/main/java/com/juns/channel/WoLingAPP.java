package com.juns.channel;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.huosdk.huounion.sdk.HuoUnionSDK;
import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

public class WoLingAPP extends OPlatformAPP {
    private static final String TAG = "WoLingAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public WoLingAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        HuoUnionSDK.getInstance().onAppCreate(application);
    }

    @Override
    public void attachBaseContext(Application application, Context base) {
        super.attachBaseContext(application, base);
        HuoUnionSDK.getInstance().onAppAttachBaseContext(application, base);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        HuoUnionSDK.getInstance().onAppTerminate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        HuoUnionSDK.getInstance().onConfigurationChanged(newConfig);
    }
    
}
