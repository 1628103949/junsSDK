package com.juns.sdk.core.api.callback;

/**
 * Data：19/12/2018-5:43 PM
 * Author: ranger
 */
public interface JunSCallback {

    void onSuccess(String info);

    void onFail(int code, String msg);

}
