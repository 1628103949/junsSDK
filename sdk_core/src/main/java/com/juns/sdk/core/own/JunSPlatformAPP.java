package com.juns.sdk.core.own;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.juns.sdk.core.sdk.IPlatformAPP;
import com.juns.sdk.core.sdk.ads.JunsAds;

/**
 * Data：19/12/2018-4:59 PM
 * Author: ranger
 */
public class JunSPlatformAPP implements IPlatformAPP {

    public JunSPlatformAPP() {
    }

    @Override
    public void onCreate(Application application) {
        JunsAds.getInstance().onApplication(application);
    }

    @Override
    public void attachBaseContext(Context base) {

    }
}
