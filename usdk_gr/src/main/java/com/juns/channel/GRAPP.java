package com.juns.channel;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.gaore.mobile.GrAPI;
import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

public class GRAPP extends OPlatformAPP {
    private static final String TAG = "GRAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public GRAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        GrAPI.getInstance().grOnAppCreate(application);
    }

    @Override
    public void attachBaseContext(Application application, Context base) {
        super.attachBaseContext(application, base);
        GrAPI.getInstance().grOnAppAttachBaseContext(application, base);
    }
}
