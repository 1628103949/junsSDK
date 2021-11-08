package com.juns.channel;

import android.app.Application;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.meizu.gamesdk.online.core.MzGameCenterPlatform;

class MeiZuAPP extends OPlatformAPP {
    private static final String TAG = "MeiZuAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public MeiZuAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);

        MzGameCenterPlatform.init(application, SDKApplication.getPlatformConfig().getExt1(), SDKApplication.getPlatformConfig().getExt2());
    }
}
