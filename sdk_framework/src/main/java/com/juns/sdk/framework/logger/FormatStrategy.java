package com.juns.sdk.framework.logger;

public interface FormatStrategy {

    void log(int priority, String tag, String message);
}
