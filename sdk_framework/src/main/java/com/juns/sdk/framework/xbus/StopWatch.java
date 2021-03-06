package com.juns.sdk.framework.xbus;

import android.util.Log;

import java.util.HashMap;

class StopWatch {
    private static final String TAG = StopWatch.class.getSimpleName();
    private String mName;
    private HashMap<String, Long> mTicks;

    public StopWatch(final String name) {
        mName = name;
        mTicks = new HashMap<String, Long>();
    }

    public void start(String tag) {
        mTicks.put(tag, System.nanoTime());
    }

    public void stop(String tag) {
        Long start = mTicks.remove(tag);
        if (start != null) {
            long elapsed = (System.nanoTime() - start) / 1000000L;
            String message = mName + " " + tag + " elapsed time: " + elapsed + "ms";
            if (elapsed > 100) {
                Log.w(TAG, message);
            } else if (elapsed > 20) {
                Log.i(TAG, message);
            } else if (elapsed > 2) {
                Log.d(TAG, message);
            }
        }
    }
}
