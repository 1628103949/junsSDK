package com.juns.sdk.core.sdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.text.TextUtils;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.own.JunSPlatformSDK;
import com.juns.sdk.core.own.event.JSExitEv;
import com.juns.sdk.core.own.pay.JunSPay;
import com.juns.sdk.core.own.submit.JunSSubmit;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.sdk.ads.JunsAds;
import com.juns.sdk.core.sdk.flow.MPayFlow;
import com.juns.sdk.core.sdk.track.Track;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.utils.ReflectUtils;
import com.juns.sdk.framework.xbus.Bus;

import java.util.HashMap;

/**
 * Data：27/09/2018-11:32 AM
 * Author: ranger
 */
public class PlatformHelper {

    private IPlatformSDK platform;
    private MPayFlow mPayFlow;
    private SDKCore mSDKCore;
    //切支付
    private JunSPay junSPay;
    private JunSSubmit junSSubmit;
    PlatformHelper(SDKCore core) {
        mSDKCore = core;
        if (SDKApplication.isTNPlatform()) {
            platform = new JunSPlatformSDK();
        } else {
            platform = getRealPlatform();
        }
        Bus.getDefault().register(platform);
    }
    void preInit(Activity mainAct) {
        junSSubmit = new JunSSubmit();
        platform.preInit(mainAct);
    }
    void init(Activity mainAct) {
        //preInit(mainAct);
        //junSSubmit = new JunSSubmit();
        platform.init(mainAct);
    }

    private void preLogin(Activity mainAct) {
        platform.preLogin(mainAct);
    }

    void login(Activity mainAct) {
        preLogin(mainAct);
        platform.login(mainAct);
    }

    void logout(Activity mainAct) {
        platform.logout(mainAct);
    }

    private String prePay(Activity mainAct) {
        return platform.prePay(mainAct);
    }

    void pay(final Activity mainAct, final HashMap<String, String> payParams) {
        String prePayData = prePay(mainAct);
        if (!TextUtils.isEmpty(prePayData)) {
            payParams.put(JunSConstants.PAY_M_DATA, prePayData);
        }
        if (mPayFlow == null) {
            mPayFlow = new MPayFlow();
        }
        mPayFlow.doMPay(mainAct, payParams, new MPayFlow.MPayFlowCallback() {
            @Override
            public void onFinish(final HashMap<String, String> payMParams) {
                //设置当前MPayOrder
                SDKData.setCurrentMPayOrder(payMParams.get(JunSConstants.PAY_M_ORDER_ID));
                //判断是否有切支付逻辑
                if (SDKApplication.isTNPlatform()) {
                    //自营，不用处理

                        platform.pay(mainAct, payMParams);
                //    }
                    //platform.pay(mainAct, payMParams);
                } else {
                    if (payMParams.containsKey(JunSConstants.PAY_M_URL) && !TextUtils.isEmpty(payMParams.get(JunSConstants.PAY_M_URL))) {
                        //有aid，切

                        junSPay = new JunSPay();
                        junSPay.doPay(mainAct, payParams);
                    } else {
                        //不切
                        platform.pay(mainAct, payParams);
                    }
                }
            }
        });
    }

    void exitGame(Activity mainAct) {
        //检查配置文件，是否使用平台退出框或原生退出框
        //未初始化，未初始化，直接使用原生弹窗
        if (!SDKApplication.getPlatformConfig().getShowExit().equals("1") && !SDKCore.isSdkInitialized()) {
            //使用原生退出框
            showAndroidExit(mainAct);
        } else {
            platform.exitGame(mainAct);

        }
    }

    void showFloat(Activity mainAct){
        platform.showFloat(mainAct);
    }

    void hideFloat(Activity mainAct){
        platform.hideFloat(mainAct);
    }

    void eventSubmit(String name,String data){
        HashMap<String, String> submitInfo = new HashMap<>();
        submitInfo.put(JunSConstants.SUBMIT_TYPE,name);
        submitInfo.put(JunSConstants.SUBMIT_EXT,data);
        junSSubmit.doSubmitLog(submitInfo);
    }
    void submitInfo(Activity mainAct, HashMap<String, String> roleInfo) {

        SDKData.setGameRoleId(roleInfo.get(JunSConstants.SUBMIT_ROLE_ID));
        SDKData.setGameRoleName(roleInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
        SDKData.setGameRoleLevel(roleInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));
        SDKData.setGameServerId(roleInfo.get(JunSConstants.SUBMIT_SERVER_ID));
        SDKData.setGameServerName(roleInfo.get(JunSConstants.SUBMIT_SERVER_NAME));
        SDKData.setGameBalance(roleInfo.get(JunSConstants.SUBMIT_BALANCE));
        SDKData.setGameVip(roleInfo.get(JunSConstants.SUBMIT_VIP));
        SDKData.setGamePartyName(roleInfo.get(JunSConstants.SUBMIT_PARTY_NAME));
        SDKData.setGameExt(roleInfo.get(JunSConstants.SUBMIT_EXT));
        SDKData.setGameCreateTime(roleInfo.get(JunSConstants.SUBMIT_CREATE_TIME));
        SDKData.setGameUpTime(roleInfo.get(JunSConstants.SUBMIT_UP_TIME));
        SDKData.setGameLastRoleName(roleInfo.get(JunSConstants.SUBMIT_LAST_ROLE_NAME));

        switch (roleInfo.get(JunSConstants.SUBMIT_TYPE)) {
            case JunSConstants.SUBMIT_TYPE_CREATE:
                Track.trackRole(roleInfo, Track.TN_ROLE_CREATE);
                JunsAds.getInstance().onRoleCreate(mainAct, roleInfo.get(JunSConstants.SUBMIT_ROLE_ID), roleInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
                //Bus.getDefault().post(EvSubmit.getSucc());
                break;
            case JunSConstants.SUBMIT_TYPE_ENTER:
                Track.trackRole(roleInfo, Track.TN_ROLE_ENTER);
                //Bus.getDefault().post(EvSubmit.getSucc());
                break;
            case JunSConstants.SUBMIT_TYPE_UPGRADE:
                Track.trackRole(roleInfo, Track.TN_ROLE_UPGRADE);
                //Bus.getDefault().post(EvSubmit.getSucc());
                break;
            case JunSConstants.SUBMIT_TYPE_UPDATE:
                Track.trackRole(roleInfo, Track.TN_ROLE_UPDATE);
                //Bus.getDefault().post(EvSubmit.getSucc());
                break;
            default:
                break;
        }
        junSSubmit.doSubmit(roleInfo);
        platform.submitInfo(mainAct, roleInfo);
    }

    void onCreate(Activity mainAct) {
        platform.onCreate(mainAct);
    }

    void onStart(Activity mainAct) {
        platform.onStart(mainAct);
    }

    void onRestart(Activity mainAct) {
        platform.onRestart(mainAct);
    }

    void onResume(Activity mainAct) {
        platform.onResume(mainAct);
    }

    void onPause(Activity mainAct) {
        platform.onPause(mainAct);
    }

    void onStop(Activity mainAct) {
        platform.onStop(mainAct);
        if (platform != null && mainAct != null && mainAct.isFinishing()) {
            Bus.getDefault().unregister(platform);
        }
    }

    void onDestroy(Activity mainAct) {
        platform.onDestroy(mainAct);
        if (platform != null) {
            Bus.getDefault().unregister(platform);
        }
    }

    void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        platform.onActivityResult(mainAct, requestCode, resultCode, data);
    }

    void onNewIntent(Activity mainAct, Intent data) {
        platform.onNewIntent(mainAct, data);

    }

    void onConfigurationChanged(Activity mainAct, Configuration newConfig) {
        platform.onConfigurationChanged(mainAct, newConfig);
    }

    void onRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        platform.sdkOnRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
    }

    private IPlatformSDK getRealPlatform() {
        return (IPlatformSDK) ReflectUtils.reflect(SDKApplication.getPlatformConfig().getPlatformClass())
                .newInstance(OPlatformBean.init(SDKApplication.getSdkConfig(), SDKApplication.getPlatformConfig()))
                .get();
    }

    //展示原生退出窗
    private void showAndroidExit(Context ctx) {
        new AlertDialog.Builder(ctx).setMessage(ResUtil.getStringID("juns_confirm_exit_game", ctx))
                .setCancelable(false)
                .setPositiveButton(ResUtil.getStringID("juns_yes", ctx), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (SDKApplication.isTNPlatform()) {
                            Bus.getDefault().post(JSExitEv.getSucc());
                        } else {
                            Bus.getDefault().post(OExitEv.getSucc());
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(ResUtil.getStringID("juns_cancel", ctx), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (SDKApplication.isTNPlatform()) {
                            Bus.getDefault().post(JSExitEv.getFail(JunSConstants.Status.USER_CANCEL, "user cancel."));
                        } else {
                            Bus.getDefault().post(OExitEv.getFail(JunSConstants.Status.USER_CANCEL, "user cancel."));
                        }
                        dialog.dismiss();
                    }
                }).show();
    }

}
