package com.juns.sdk.core.sdk;

import android.app.Application;
import android.content.Context;

/**
 * Data：03/01/2019-4:42 PM
 * Author: ranger
 */
public interface IPlatformAPP {

    void onCreate(Application application);

    void attachBaseContext(Context base);

}
