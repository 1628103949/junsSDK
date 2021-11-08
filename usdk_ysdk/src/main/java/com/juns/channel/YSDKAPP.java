package com.juns.channel;

import android.app.Application;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

public class YSDKAPP extends OPlatformAPP {
    private static final String TAG = "YSDKAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public YSDKAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);

    }
}
