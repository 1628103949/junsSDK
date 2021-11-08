package com.juns.channel;

import android.app.Application;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Toast;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.utils.AppUtils;

import cn.nubia.nbgame.sdk.GameSdk;
import cn.nubia.nbgame.sdk.entities.AppInfo;
import cn.nubia.nbgame.sdk.entities.ErrorCode;
import cn.nubia.nbgame.sdk.interfaces.CallbackListener;

public class NubiaAPP extends OPlatformAPP {
    private static final String TAG = "NubiaAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public NubiaAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);

    }

}
