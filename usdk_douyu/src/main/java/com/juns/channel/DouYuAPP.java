package com.juns.channel;

import android.app.Application;

import com.douyu.gamesdk.DouyuGameSdk;
import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.sdk.common.SplashDialog;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

public class DouYuAPP extends OPlatformAPP {
    private static final String TAG = "DouYuAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public DouYuAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);

    }
}
