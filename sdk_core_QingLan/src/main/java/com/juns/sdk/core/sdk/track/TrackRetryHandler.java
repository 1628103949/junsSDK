package com.juns.sdk.core.sdk.track;


import com.juns.sdk.core.http.exception.JunSServerException;

import org.json.JSONException;

import java.util.HashSet;

class TrackRetryHandler {

    private static HashSet<Class<?>> blackList = new HashSet<Class<?>>();
    private static int maxRetryCount = 3;

    static {
        blackList.add(JunSServerException.class);
        blackList.add(JSONException.class);
    }

    static void setMaxRetryCount(int retryCount) {
        maxRetryCount = retryCount;
    }

    static boolean canRetry(Throwable ex, int count) {

        if (count > maxRetryCount) {
            return false;
        }

        if (blackList.contains(ex.getClass())) {
            return false;
        }

        return true;
    }
}
