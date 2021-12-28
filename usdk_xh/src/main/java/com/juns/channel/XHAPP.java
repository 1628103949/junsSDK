package com.juns.channel;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.xh.libcommon.api.XhImp;
import com.xh.libcommon.tools.LoadConfig;
import com.xh.libcommon.tools.PluginUtils;


public class XHAPP extends OPlatformAPP {
    private static final String TAG = "XHAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public XHAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        LoadConfig.loadConfig();
        PluginUtils.getInstance().preparePlugin(LoadConfig.getXh_sdk_plugin());
        XhImp.getInstance().onApplicationCreate(application);

    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }
}
