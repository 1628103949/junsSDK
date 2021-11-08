package com.juns.channel;

import android.app.Application;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.wett.cooperation.container.TTSDKV2;

public class TTAPP extends OPlatformAPP {
    private static final String TAG = "TTAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public TTAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        TTSDKV2.getInstance().prepare(application);
    }
}
