package com.juns.channel;

import android.app.Application;
import android.text.TextUtils;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.jzyx.common.utils.Utils;
import com.jzyx.sdk.core.JZCore;
import com.jzyx.sdk.core.JZInfo;
import com.jzyx.sdk.helper.OAIDSDKHelper;
import com.jzyx.sdk.manager.AnalyticsManager;
import com.jzyx.sdk.open.JZYXApplication;
import com.tencent.smtt.sdk.QbSdk;

import java.util.HashMap;

public class JiuZiAPP extends OPlatformAPP {
    private static final String TAG = "JiuZiAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public JiuZiAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        String meta = "";
        int msa_code = 20210301;
        OAIDSDKHelper.preLoad();
        HashMap<String, Object> amap = new HashMap();
        if (!TextUtils.isEmpty(meta)) {
            String params = Utils.decodeToString(meta);
            JZCore.getInstance().setAppContext(application);
            JZInfo.getInstance().setAppParams(params);
            amap.put("analytics_data", params);
            AnalyticsManager.getInstance(application);
            AnalyticsManager.ApplicationOnCreate(application, amap);
        } else {
            amap.put("analytics_data", "null");
            AnalyticsManager.getInstance(application);
            AnalyticsManager.ApplicationOnCreate(application, amap);
        }

        HashMap<String, Object> map = new HashMap();
        map.put("use_speedy_classloader", true);
        map.put("use_dexloader_service", true);
        QbSdk.initTbsSettings(map);
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            public void onViewInitFinished(boolean arg0) {
            }

            public void onCoreInitFinished() {
            }
        };
        QbSdk.initX5Environment(application, cb);
    }
}
