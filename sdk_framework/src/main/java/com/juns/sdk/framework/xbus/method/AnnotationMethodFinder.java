package com.juns.sdk.framework.xbus.method;

import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xbus.MethodInfo;

import java.util.Set;

public class AnnotationMethodFinder implements MethodFinder {


    @Override
    public Set<MethodInfo> find(final Bus bus, final Class<?> targetClass) {
        return MethodHelper.findSubscriberMethodsByAnnotation(targetClass);
    }
}
