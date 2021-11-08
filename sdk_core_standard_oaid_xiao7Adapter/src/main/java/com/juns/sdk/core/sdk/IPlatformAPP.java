package com.juns.sdk.core.sdk;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

/**
 * Data：03/01/2019-4:42 PM
 * Author: ranger
 */
public interface IPlatformAPP {

    void onCreate(Application application);

    void attachBaseContext(Application application,Context base);

    void attachBaseContext(Context base);

    void onTerminate();

    void onConfigurationChanged(Configuration newConfig);

}
