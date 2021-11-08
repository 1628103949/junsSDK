package com.juns.sdk.core.platform;

import android.app.Application;
import android.content.Context;

import com.juns.sdk.core.sdk.IPlatformAPP;

/**
 * Data：19/12/2018-4:59 PM
 * Author: ranger
 */
public class OPlatformAPP implements IPlatformAPP {

    public OPlatformBean pBean = null;

    public OPlatformAPP(OPlatformBean bean) {
        this.pBean = bean;
    }

    @Override
    public void onCreate(Application application) {

    }

    @Override
    public void attachBaseContext(Context base) {

    }
}
