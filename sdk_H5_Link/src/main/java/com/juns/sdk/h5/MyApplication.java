package com.juns.sdk.h5;

import android.app.Application;

import com.juns.sdk.framework.utils.Utils;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        new MiIdHelper().getDeviceIds(this);
    }
}
