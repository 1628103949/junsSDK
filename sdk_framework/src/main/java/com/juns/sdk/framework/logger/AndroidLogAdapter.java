package com.juns.sdk.framework.logger;

public class AndroidLogAdapter implements com.juns.sdk.framework.logger.LogAdapter {

    private final com.juns.sdk.framework.logger.FormatStrategy formatStrategy;

    public AndroidLogAdapter() {
        this.formatStrategy = com.juns.sdk.framework.logger.PrettyFormatStrategy.newBuilder().build();
    }

    public AndroidLogAdapter(com.juns.sdk.framework.logger.FormatStrategy formatStrategy) {
        this.formatStrategy = formatStrategy;
    }

    @Override
    public boolean isLoggable(int priority, String tag) {
        return true;
    }

    @Override
    public void log(int priority, String tag, String message) {
        formatStrategy.log(priority, tag, message);
    }

}