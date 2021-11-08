package com.juns.sdk.framework.mvp.presenter;

/**
 * User: Ranger
 * Date: 22/05/2017
 * Time: 8:35 PM
 * Desc: presenter interface，所有Presenter必须实现此接口
 */
public interface BasePresenter {
    public void subscribe();

    public void unSubscribe();
}
