package com.juns.sdk.framework.xutils.http.body;


import com.juns.sdk.framework.xutils.http.ProgressHandler;

public interface ProgressBody extends RequestBody {
    void setProgressHandler(ProgressHandler progressHandler);
}
