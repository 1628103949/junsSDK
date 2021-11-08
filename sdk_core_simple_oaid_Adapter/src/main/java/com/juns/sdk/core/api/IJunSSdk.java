package com.juns.sdk.core.api;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;

import com.juns.sdk.core.api.callback.JunSCallback;
import com.juns.sdk.core.api.callback.JunSLogoutCallback;
import com.juns.sdk.core.api.callback.JunSPayCallback;

/**
 * Data：20/09/2018-5:20 PM
 * Author: ranger
 */
public interface IJunSSdk {

    /**
     * 初始化接口
     *
     * @param mainAct  主Activity
     * @param appKey   appKey
     * @param callback 初始化回调
     */
    void sdkInit(Activity mainAct, String appKey, JunSCallback callback);
    /**
     * 初始化接口
     *
     * @param name  事件key
     * @param data   携带参数
     * @param callback 事件上报回调
     */
    void sdkEventPost(String name, String data, JunSCallback callback);

    /**
     * 设置注销回调
     *
     * @param callback 注销回调
     */
    void setLogoutCallback(JunSLogoutCallback callback);

    /**
     * 登录接口
     *
     * @param mainAct  主Activity
     * @param callback 登录回调
     */
    void sdkLogin(Activity mainAct, JunSCallback callback);

    /**
     * SDK 注销接口
     *
     * @param mainAct 主Activity
     */
    void sdkLogout(Activity mainAct);

    /**
     * SDK 自定义信息回调接口
     *
     * @param mainAct 主Activity
     * @param info 自定义信息
     */
    void sdkDIYEvent(Activity mainAct,String info);

    /**
     * 是否已经登录
     *
     * @return 是否已登录
     */
    boolean isLogined();

    /**
     * 支付
     *
     * @param mainAct  主Activity
     * @param payInfo  支付参数
     * @param callback 支付回调
     */
    void sdkPay(Activity mainAct, JunSPayInfo payInfo, JunSPayCallback callback);

    /**
     * 退出游戏
     *
     * @param mainAct  主Activity
     * @param callback 退出回调
     */
    void sdkGameExit(Activity mainAct, JunSCallback callback);

    /**
     * 提交角色信息
     *
     * @param mainAct    主Activity
     * @param submitInfo 角色信息参数
     * @param callback   提交回调
     */
    void sdkSubmitInfo(Activity mainAct, JunSSubmitInfo submitInfo, JunSCallback callback);

    /**
     * 显示悬浮窗
     *
     * @param mainAct    主Activity
     */
    void showFloat(Activity mainAct);

    /**
     * 隐藏悬浮窗
     *
     * @param mainAct    主Activity
     */
    void hideFloat(Activity mainAct);

    /**
     * 获取SDK配置信息
     *
     * @param mainAct 主Activity
     * @return SDK配置信息
     */
    String sdkGetConfig(Activity mainAct);

    void sdkOnCreate(Activity mainAct);

    void sdkOnStart(Activity mainAct);

    void sdkOnRestart(Activity mainAct);

    void sdkOnResume(Activity mainAct);

    void sdkOnPause(Activity mainAct);

    void sdkOnStop(Activity mainAct);

    void sdkOnDestroy(Activity mainAct);

    void sdkOnActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data);

    void sdkOnNewIntent(Activity mainAct, Intent data);

    void sdkOnConfigurationChanged(Activity mainAct, Configuration newConfig);

    void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults);
}
