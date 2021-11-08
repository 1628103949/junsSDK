package com.juns.channel;

import android.app.Application;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

public class DDTAPP extends OPlatformAPP {
    private static final String TAG = "DDTAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public DDTAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);

    }
}
