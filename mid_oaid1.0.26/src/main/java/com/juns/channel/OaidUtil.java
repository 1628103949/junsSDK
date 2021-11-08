package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.juns.sdk.core.sdk.IOaidAdapter;

public class OaidUtil implements IOaidAdapter {
    private final String TAG = "OaidUtil";
    @Override
    public void init(Context context) {

    }

    @Override
    public void start(Activity mainAct) {
        Log.e(TAG, "start: ");
        MiIdHelper miIdHelper = new MiIdHelper();
        miIdHelper.getDeviceIds(mainAct);
    }

    @Override
    public String getOaid() {
        Log.e(TAG, "getOaid: "+MiIdHelper.mOAID);
        return MiIdHelper.mOAID;
    }
}
