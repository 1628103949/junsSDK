package com.juns.channel;

import android.app.Application;

import com.cgamex.usdk.api.CGamexSDK;
import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

public class CGameAPP extends OPlatformAPP {
    private static final String TAG = "CGameAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public CGameAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        CGamexSDK.onApplicationCreate(application);
    }
}
