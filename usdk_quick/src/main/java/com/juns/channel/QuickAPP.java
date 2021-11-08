package com.juns.channel;

import android.app.Application;
import android.content.Context;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.quicksdk.Sdk;
import com.quicksdk.apiadapter.IAdapterFactory;
import com.quicksdk.ex.ExCollector;
import com.quicksdk.utility.AppConfig;

public class QuickAPP extends OPlatformAPP {
    private static final String TAG = "QuickAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    public QuickAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);

    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
