package com.juns.sdk.framework.xbus.exception;

public class BusException extends RuntimeException {

    public BusException(final String detailMessage) {
        super(detailMessage);
    }

    public BusException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }

    public BusException(final Throwable throwable) {
        super(throwable);
    }

    public BusException() {
        super();
    }
}
