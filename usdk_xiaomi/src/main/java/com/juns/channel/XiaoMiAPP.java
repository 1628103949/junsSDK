package com.juns.channel;

import android.app.Application;
import android.util.Log;


import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.OnInitProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;

import java.util.List;


public class XiaoMiAPP extends OPlatformAPP {
    private static final String TAG = "XiaoMiAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public XiaoMiAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);

        MiAppInfo appInfo = new MiAppInfo();
        appInfo.setAppId(SDKApplication.getPlatformConfig().getExt1());
        appInfo.setAppKey(SDKApplication.getPlatformConfig().getExt2());
        MiCommplatform.Init(application, appInfo, new OnInitProcessListener() {
            @Override
            public void finishInitProcess(List<String> loginMethod, int gameConfig) {
//                Log.i(Demo, Init success);
            }
            @Override
            public void onMiSplashEnd() {
            //⼩⽶闪屏⻚结束回调，⼩⽶闪屏可配，⽆闪屏也会返回此回调，游戏的闪屏应当在收到此回调之后开始。
            }
        });
    }
}
