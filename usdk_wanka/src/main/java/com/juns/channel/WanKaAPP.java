package com.juns.channel;

import android.app.Application;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.wanyugame.wygamesdk.app.WyApplication;

public class WanKaAPP extends OPlatformAPP {
    private static final String TAG = "WanKaAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public WanKaAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        //调用 WyApplication 的初始化方法，已继承 WyApplication 无需再次调用此接口
        WyApplication.getInstance().appInit(application);

    }
}
