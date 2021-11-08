package com.juns.sdk.framework.xutils.http.app;

import com.juns.sdk.framework.xutils.http.RequestParams;
import com.juns.sdk.framework.xutils.http.request.UriRequest;

public interface RedirectHandler {

    /**
     * 根据请求信息返回自定义重定向的请求参数
     *
     * @param request
     * @return 返回不为null时进行重定向
     */
    RequestParams getRedirectParams(UriRequest request) throws Throwable;
}
