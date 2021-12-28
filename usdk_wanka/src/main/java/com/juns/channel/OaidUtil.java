package com.juns.channel;

import android.app.Activity;
import android.content.Context;

import com.juns.sdk.core.sdk.IOaidAdapter;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;

public class OaidUtil implements IOaidAdapter {
    private static final String TAG = "OaidUtil";
    private static final TNLog logger = LogFactory.getLog(TAG, true);
    @Override
    public void init(Context context) {
        logger.print("init: ");

    }

    @Override
    public void start(Activity mainAct) {
        logger.print("start: ");
        new MiIdHelper().getDeviceIds(mainAct);
    }

    @Override
    public String getOaid() {
        logger.print("getOaid: "+MiIdHelper.mOAID);
        return MiIdHelper.mOAID;
    }
}
