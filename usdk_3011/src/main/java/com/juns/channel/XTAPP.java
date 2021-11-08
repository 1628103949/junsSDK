package com.juns.channel;

import android.app.Application;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.xiantu.open.XTApiFactory;
import com.xiantu.open.XTSDKInitObsv;
import com.xiantu.open.XTSDKInitResult;

public class XTAPP extends OPlatformAPP {
    private static final String TAG = "XTAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public XTAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);

    }

}
