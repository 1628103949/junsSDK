package com.juns.sdk.framework.xbus.scheduler;

import android.os.Handler;
import android.os.Looper;

import com.juns.sdk.framework.xbus.Bus;

public final class Schedulers {

    public static Scheduler sender(final Bus bus) {
        return new SenderScheduler(bus);
    }

    public static Scheduler main(final Bus bus) {
        return new HandlerScheduler(bus, new Handler(Looper.getMainLooper()));
    }

    public static Scheduler thread(final Bus bus) {
        return new ExecutorScheduler(bus);
    }
}
