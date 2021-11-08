package com.juns.sdk.framework.logger;

public interface LogStrategy {

    void log(int priority, String tag, String message);
}
