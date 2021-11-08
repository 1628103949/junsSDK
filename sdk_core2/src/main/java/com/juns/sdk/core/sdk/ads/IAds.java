package com.juns.sdk.core.sdk.ads;

import android.app.Activity;
import android.content.Context;

/**
 * Data：2020-06-07-21:04
 * Author: ranger
 */
public interface IAds {

    void initAds(Context ctx, String appId, String appName, String oaid, String refer);

    void onEvResume(Activity act);

    void onEvPause(Activity act);

    void onEvActive(Activity act);

    void onEvRegister(Activity act, String type);

    void onEvLogin(Activity act, String userId);

    void onEvLoginSucc(Activity act, String userId);

    void onEvPay(Activity act, String goodsName, String payWay, String payMoney);

    void onRoleCreate(Activity act, String roleId, String roleName);
}
