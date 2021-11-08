package com.juns.channel;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.sspyx.psdk.SSPSDK;

public class YXAPP extends OPlatformAPP {
    private static final String TAG = "YXAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public YXAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        SSPSDK.getInstance().onAppCreate(application);
    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        SSPSDK.getInstance().onAppAttachBaseContext(base);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SSPSDK.getInstance().onAppConfigurationChanged(newConfig);
    }
}
