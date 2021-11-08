package com.juns.sdk.framework.xutils.http.app;

import java.io.InputStream;
import java.lang.reflect.Type;

public abstract class InputStreamResponseParser implements ResponseParser {

    public abstract Object parse(Type resultType, Class<?> resultClass, InputStream result) throws Throwable;

    /**
     * Deprecated, see {@link InputStreamResponseParser#parse(Type, Class, InputStream)}
     *
     * @throws Throwable
     */
    @Override
    @Deprecated
    public final Object parse(Type resultType, Class<?> resultClass, String result) throws Throwable {
        return null;
    }
}
