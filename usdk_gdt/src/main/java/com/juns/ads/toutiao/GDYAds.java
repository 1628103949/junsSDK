package com.juns.ads.toutiao;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.core.sdk.ads.IAds;
import com.qq.gdt.action.ActionType;
import com.qq.gdt.action.ActionUtils;
import com.qq.gdt.action.GDTAction;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Data：2020-06-07-22:42
 * Author: ranger
 */
public class GDYAds implements IAds {

    @Override
    public void onApplication(Application application) {
        GDTAction.init(application, SDKApplication.getPlatformConfig().getExt1(), SDKApplication.getPlatformConfig().getExt2());
    }
    @Override
    public void initAds(Context ctx, String appId, String appName, String oaid, String refer) {
        //GDTAction.init(ctx, "1111031328", SDKApplication.getPlatformConfig().getExt1());
        // 第一个参数是Context上下文;第二个参数是您在DMP上获得的行为数据源ID;第三个参数是您在DMP上获得AppSecretKey;第四个参数是您的渠道ID, 选填
    }

    @Override
    public void onEvResume(Activity act) {
       // Log.e("gdt_action","onEvResume");
        GDTAction.logAction(ActionType.START_APP);
    }

    @Override
    public void onEvPause(Activity act) {

    }

    @Override
    public void onEvActive(Activity act) {

    }

    @Override
    public void onEvRegister(Activity act, String type) {
        ActionUtils.onRegister(type,true);
    }

    @Override
    public void onEvLogin(Activity act, String userId) {
       // ActionUtils.onLogin();
    }

    @Override
    public void onEvLoginSucc(Activity act, String userId) {
        //设置帐号体系ID
        GDTAction.setUserUniqueId(userId);
    }

    @Override
    public void onEvPay(Activity act, String goodsName, String payWay, String payMoney) {
        try {
            // 用户发生购物行为时，可以用GDTAction.logAction上报用户的这次行为，并将价格等行为参数一起带上
            int money = (int)Float.parseFloat(payMoney)*100;
            JSONObject actionParam = new JSONObject();
            actionParam.put("value", money);
            actionParam.put("name", goodsName);
            GDTAction.logAction(ActionType.PURCHASE, actionParam);
          //  ActionUtils.onPurchase("type", goodsName, "1", 1, "juns", "CNY", money, true);
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRoleCreate(Activity act, String roleId, String roleName) {
        ActionUtils.onCreateRole(roleName);
    }
}
