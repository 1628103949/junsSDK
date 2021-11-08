package com.juns.channel;

import android.app.Application;
import android.content.Context;

import com.enjoy.sdk.core.api.EnjoyApplication;
import com.enjoy.sdk.core.api.EnjoySdkApi;
import com.enjoy.sdk.core.api.EnjoySdkApp;
import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

public class QuWanAPP extends OPlatformAPP {
    private static final String TAG = "QuWanKAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public QuWanAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        //EnjoySdkApp.getInstance().AppOnCreate(application);
    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //EnjoySdkApp.getInstance().attachBaseContext(base);
    }
}
