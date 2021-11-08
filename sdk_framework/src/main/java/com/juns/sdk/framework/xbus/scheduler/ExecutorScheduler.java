package com.juns.sdk.framework.xbus.scheduler;

import com.juns.sdk.framework.xbus.Bus;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class ExecutorScheduler implements Scheduler {
    private Bus mBus;
    private Executor mExecutor;

    public ExecutorScheduler(final Bus bus) {
        mBus = bus;
        mExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public void post(final Runnable runnable) {
        mExecutor.execute(runnable);
    }
}
