package com.juns.sdk.framework.xbus.scheduler;

import android.os.Handler;

import java.util.concurrent.Executor;

public class HandlerExecutor implements Executor {
    private final Handler handler;

    public HandlerExecutor(final Handler handler) {
        this.handler = handler;
    }

    @Override
    public void execute(Runnable runnable) {
        handler.post(runnable);
    }
}
