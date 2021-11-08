package com.juns.channel;

import android.app.Application;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.yysy.yygamesdk.common.YyGame;

public class DDDAPP extends OPlatformAPP {
    private static final String TAG = "DDDAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public DDDAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        YyGame.initSdk(application);
    }
}
