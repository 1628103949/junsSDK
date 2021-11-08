package com.juns.channel;

import android.app.Activity;

import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;

import java.util.HashMap;

public class YSDK extends OPlatformSDK {
    private static final String TAG = "YSDK";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public YSDK(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {

    }

    @Override
    public void logout(Activity mainAct) {

    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {

    }

    @Override
    public void exitGame(Activity activity) {

    }
}
