package com.juns.channel;

import android.app.Application;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.loongcheer.lrsmallsdk.LrSmallApi;
import com.loongcheer.lrsmallsdk.LrSmallApplication;
import com.loongcheer.lrsmallsdk.utils.ApplicationUtils;

public class LongYueAPP extends OPlatformAPP {
    private static final String TAG = "LongYueAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public LongYueAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        new LrSmallApplication().init(application);
    }
}
