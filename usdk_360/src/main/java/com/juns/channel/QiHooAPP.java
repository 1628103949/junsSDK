package com.juns.channel;

import android.app.Application;

import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.qihoo.gamecenter.sdk.matrix.Matrix;

public class QiHooAPP extends OPlatformAPP {
    private static final String TAG = "QiHooAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public QiHooAPP(OPlatformBean bean) {
        super(bean);
    }

    //需要到application执行的可在此操作
    @Override
    public void onCreate(Application application) {
        super.onCreate(application);
        Matrix.initInApplication(application);
    }
}
