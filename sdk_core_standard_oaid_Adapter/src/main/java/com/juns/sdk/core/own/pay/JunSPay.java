package com.juns.sdk.core.own.pay;

import android.app.Activity;
import android.util.Log;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.own.event.JSPayEv;
import com.juns.sdk.core.sdk.JunSUtils;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.event.EvPay;
import com.juns.sdk.framework.common.Dev;
import com.juns.sdk.framework.utils.AppUtils;
import com.juns.sdk.framework.utils.EncryptUtils;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.xbus.Bus;

import java.util.HashMap;

/**
 * Data：19/01/2019-10:38 AM
 * Author: ranger
 */
public class JunSPay {
    private Activity payActivity;
    private HashMap<String, String> payParams;
    private PayDialog payDialog;
    private PayDialog.PayCallback payCallback = new PayDialog.PayCallback() {
        @Override
        public void onFinish() {
            //支付完成，查询订单，显示公告
            if(SDKApplication.isTNPlatform()){
                Bus.getDefault().post(JSPayEv.getFail(JunSConstants.Status.PAY_UNKNOWN, "支付状态未知，请游戏服务端按需发货！"));
            }else{
                //切支付，需要回调全局
                Bus.getDefault().post(EvPay.getFail(JunSConstants.Status.PAY_UNKNOWN, "支付状态未知，请游戏服务端按需发货！"));
            }
        }
    };

    public void doPay(Activity act, HashMap<String, String> payParams) {
        this.payActivity = act;
        this.payParams = payParams;
        dealPay();
    }

    private void dealPay() {
        HashMap<String, String> urlParams = new HashMap<>();
        urlParams.put("pid", SDKData.getSdkPID());
        urlParams.put("gid", SDKData.getSdkGID());
        urlParams.put("refer", SDKData.getSdkRefer());
        urlParams.put("duid", SDKData.getSDKDuid());
        urlParams.put("version", AppUtils.getAppVersionName(SDKCore.getMainAct().getPackageName()));
        urlParams.put("sdkver", SDKData.getSdkVer());
        String time = System.currentTimeMillis() / 1000 + "";
        urlParams.put("time", time);
        urlParams.put("token", SDKData.getSdkUserToken());
        urlParams.put("uid", SDKData.getSdkUserId());
        urlParams.put("uname", SDKData.getSdkUserName());
        urlParams.put("doid", payParams.get(JunSConstants.PAY_ORDER_ID));
        urlParams.put("dsid", payParams.get(JunSConstants.PAY_SERVER_ID));
        urlParams.put("dsname", payParams.get(JunSConstants.PAY_SERVER_NAME));
        urlParams.put("dext", payParams.get(JunSConstants.PAY_EXT));
        urlParams.put("drid", payParams.get(JunSConstants.PAY_ROLE_ID));
        urlParams.put("drname", payParams.get(JunSConstants.PAY_ROLE_NAME));
        urlParams.put("drlevel", payParams.get(JunSConstants.PAY_ROLE_LEVEL));
        urlParams.put("dmoney", payParams.get(JunSConstants.PAY_MONEY));
        urlParams.put("dradio", payParams.get(JunSConstants.PAY_RATE));
        urlParams.put("money", payParams.get(JunSConstants.PAY_MONEY));
        urlParams.put("moid", payParams.get(JunSConstants.PAY_M_ORDER_ID));
        urlParams.put("mac", Dev.getMacAddress(SDKCore.getMainAct()));
        urlParams.put("imei", Dev.getPhoneIMEI(SDKCore.getMainAct()));

        String preSign = SDKData.getSdkPID() + SDKData.getSdkGID() + time + SDKData.getSdkAppKey() + SDKData.getSdkUserId() + payParams.get(JunSConstants.PAY_M_ORDER_ID);
        urlParams.put("sign", EncryptUtils.encryptMD5ToString(preSign).toLowerCase());

        String payUrl = JunSUtils.buildUrlParams(payParams.get(JunSConstants.PAY_M_URL), urlParams);

       // Log.e("guoinfo","payurl:"+payUrl);
        showPayDialog(payUrl);
    }

    private void showPayDialog(String payUrl) {
        if (payDialog != null) {
            if (payDialog.isShowing()) {
                payDialog.dismiss();
            }
        }
        payDialog = null;
        payDialog = new PayDialog(payActivity, payUrl, payCallback);
        payDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
    }

    public void destroy() {

    }

}
