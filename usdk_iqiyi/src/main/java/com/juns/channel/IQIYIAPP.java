package com.juns.channel;

import android.app.Application;

import com.iqiyi.sdk.platform.GamePlatform;
import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

public class IQIYIAPP extends OPlatformAPP {
    private static final String TAG = "IQIYIAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public IQIYIAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        GamePlatform.getInstance().initApplication(application);
    }
}
