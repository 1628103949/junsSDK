package com.juns.channel;

import android.app.Application;
import android.content.Context;

import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.agconnect.config.LazyInputStream;
import com.huawei.hms.api.HuaweiMobileServicesUtil;
import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

import java.io.IOException;
import java.io.InputStream;

public class HuaWeiAPP extends OPlatformAPP {

    public HuaWeiAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        AGConnectServicesConfig config = AGConnectServicesConfig.fromContext(application);
        config.overlayWith(new LazyInputStream(application) {
            public InputStream get(Context context) {
                try {
                    return context.getAssets().open("agconnect-services.json");
                } catch (IOException e) {
                    return null;
                }
            }
        });
        HuaweiMobileServicesUtil.setApplication(application);
    }

}
