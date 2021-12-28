package com.juns.sdk.core.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

/**
 * Data：10/09/2021-10:38 AM
 * Author: enjoy
 */
public interface IOaidAdapter {

    void init(Context context);

    void start(Activity mainAct);

    String getOaid();

}
