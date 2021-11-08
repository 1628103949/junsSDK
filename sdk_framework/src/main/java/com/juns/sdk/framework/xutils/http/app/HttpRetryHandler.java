package com.juns.sdk.framework.xutils.http.app;


import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.common.util.LogUtil;
import com.juns.sdk.framework.xutils.ex.HttpException;
import com.juns.sdk.framework.xutils.http.HttpMethod;
import com.juns.sdk.framework.xutils.http.request.UriRequest;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashSet;

public class HttpRetryHandler {

    protected static HashSet<Class<?>> blackList = new HashSet<Class<?>>();

    static {
        blackList.add(HttpException.class);
        blackList.add(Callback.CancelledException.class);
        blackList.add(MalformedURLException.class);
        blackList.add(URISyntaxException.class);
        blackList.add(NoRouteToHostException.class);
        blackList.add(PortUnreachableException.class);
        blackList.add(ProtocolException.class);
        blackList.add(NullPointerException.class);
        blackList.add(FileNotFoundException.class);
        blackList.add(JSONException.class);
        blackList.add(UnknownHostException.class);
        blackList.add(IllegalArgumentException.class);
    }

    protected int maxRetryCount = 2;

    public HttpRetryHandler() {
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public boolean canRetry(UriRequest request, Throwable ex, int count) {

        LogUtil.w(ex.getMessage(), ex);

        if (count > maxRetryCount) {
            LogUtil.w(request.toString());
            LogUtil.w("The Max Retry times has been reached!");
            return false;
        }

        if (!HttpMethod.permitsRetry(request.getParams().getMethod())) {
            LogUtil.w(request.toString());
            LogUtil.w("The Request Method can not be retried.");
            return false;
        }

        if (blackList.contains(ex.getClass())) {
            LogUtil.w(request.toString());
            LogUtil.w("The Exception can not be retried.");
            return false;
        }

        return true;
    }
}
