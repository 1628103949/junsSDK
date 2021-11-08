package com.juns.sdk.framework.mvp.view;

/**
 * User: Ranger
 * Date: 22/05/2017
 * Time: 8:33 PM
 * Desc: view interface，所有View必须实现此接口
 */
public interface BaseView<T> {
    void setPresenter(T presenter);
}
