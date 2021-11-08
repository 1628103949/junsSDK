package com.juns.channel;

import android.app.Application;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.nearme.game.sdk.GameCenterSDK;

public class OPPOAPP extends OPlatformAPP {
    private static final String TAG = "OPPOAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public OPPOAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        GameCenterSDK.init(SDKApplication.getPlatformConfig().getExt2(), application);
    }
}
