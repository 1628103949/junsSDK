package com.juns.channel;

import android.app.Application;
import android.content.Context;

import com.gm88.gmcore.GM;
import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

public class GuaMAPP extends OPlatformAPP {
    private static final String TAG = "GuaMAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public GuaMAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        GM.initApplication(application);
    }

    @Override
    public void attachBaseContext(Application application, Context base) {
        super.attachBaseContext(application, base);
        GM.attachBaseContext(base);
    }
}
