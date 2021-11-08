package com.juns.sdk.framework.xbus.method;

import com.juns.sdk.framework.xbus.MethodInfo;

import java.lang.reflect.Method;

interface MethodConverter {

    MethodInfo convert(final Method method);
}
