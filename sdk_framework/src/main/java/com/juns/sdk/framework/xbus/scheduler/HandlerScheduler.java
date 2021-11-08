package com.juns.sdk.framework.xbus.scheduler;

import android.os.Handler;

import com.juns.sdk.framework.xbus.Bus;

class HandlerScheduler implements Scheduler {
    private Bus mBus;
    private Handler mHandler;

    public HandlerScheduler(final Bus bus, final Handler handler) {
        mBus = bus;
        mHandler = handler;
    }

    @Override
    public void post(final Runnable runnable) {
        mHandler.post(runnable);
    }
}
