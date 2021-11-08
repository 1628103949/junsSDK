package com.juns.channel;

import android.app.Application;

import com.huya.berry.client.HuyaBerry;
import com.huya.berry.client.HuyaBerryConfig;
import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

public class HuYaAPP extends OPlatformAPP {
    private static final String TAG = "HuYaAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public HuYaAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);

    }
}
