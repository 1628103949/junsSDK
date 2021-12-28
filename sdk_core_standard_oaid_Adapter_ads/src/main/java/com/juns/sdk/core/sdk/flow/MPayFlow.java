package com.juns.sdk.core.sdk.flow;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.Toast;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.exception.JunSServerException;
import com.juns.sdk.core.http.params.MPayParam;
import com.juns.sdk.core.own.account.login.JunsNotiDialog;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.common.RealNameDialog;
import com.juns.sdk.core.sdk.event.EvPay;
import com.juns.sdk.framework.common.ToastUtil;
import com.juns.sdk.framework.view.common.TipsDialog;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.x;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Data：22/01/2019-2:47 PM
 * Author: ranger
 */
public class MPayFlow {
    private HashMap<String, String> payParams;
    private MPayFlowCallback mPayFlowCallback;
    private Activity payActivity;


    public void doMPay(Activity act, HashMap<String, String> payParams, MPayFlowCallback mPayFlowCallback) {
        this.payActivity = act;
        this.payParams = payParams;
        this.mPayFlowCallback = mPayFlowCallback;
        mPayReq(payParams);
    }

    private void mPayReq(final HashMap<String, String> payParams) {
        MPayParam mPayParams = new MPayParam(payParams);
        x.http().post(mPayParams, new Callback.CommonCallback<JunSResponse>() {
            @Override
            public void onSuccess(JunSResponse response) {

                dealReqSuccess(response);
            }

            @Override
            public void onError(final Throwable ex, boolean isOnCallback) {
                if (ex instanceof JunSServerException) {
                    //服务器错误
                    //提示用户
                    //String data = ((JunSServerException) ex).getServerMsg();
                    JSONObject dataJson = null;
                    try {
                        dataJson = new JSONObject(((JunSServerException) ex).getServerData());
                        if(dataJson.has("isverify")){
                            String isVerify = dataJson.getString("isverify");
                            if (isVerify.equals("1")) {
                                RealNameDialog.show(payActivity, true, new RealNameDialog.Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onCancel() {

                                    }
                                });
                                JunsNotiDialog.showNoti(SDKCore.getMainAct(),((JunSServerException) ex).getServerMsg(),true);
                                return;
                            }
                            if (isVerify.equals("2")) {
                                RealNameDialog.show(payActivity, false, new RealNameDialog.Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onCancel() {

                                    }
                                });
                                JunsNotiDialog.showNoti(SDKCore.getMainAct(),((JunSServerException) ex).getServerMsg(),true);
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    ViewUtils.showTipsConfirm(payActivity, ((JunSServerException) ex).getServerMsg(), new TipsDialog.TipsConfirmCallback() {
                        @Override
                        public void onConfirm() {
                            Bus.getDefault().post(EvPay.getFail(JunSConstants.Status.SERVER_ERR, ((JunSServerException) ex).getServerMsg()));
                        }
                    });

                } else {
                    //网络错误
                    Bus.getDefault().post(EvPay.getFail(JunSConstants.Status.HTTP_ERR, ex.getMessage()));
                }
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private void dealReqSuccess(JunSResponse ret) {
        //SDKCore.logger.print("11112222"+ret.toString());
        //解析支付数据，进行实名
        if(ret.state == 0){
           // ToastUtil.show(SDKCore.getMainAct(),ret.msg,1);
            Toast.makeText(SDKCore.getMainAct(),ret.msg,Toast.LENGTH_SHORT).show();
        }
        try {
            final JSONObject dataJson = new JSONObject(ret.data);

            //0:已经实名认证无需弹窗认证
            //1:未实名要弹非强制实名认证窗口
            //2:未实名要弹强制实名认证窗口，否则不能进行下一步
            if (dataJson.has("isverify")) {
                String isVerify = dataJson.getString("isverify");
                if (isVerify.equals("1")) {
                    JunsNotiDialog.showNoti(SDKCore.getMainAct(),ret.msg,true);
                    RealNameDialog.show(payActivity, true, new RealNameDialog.Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    return;
                }
                if (isVerify.equals("2")) {
                    JunsNotiDialog.showNoti(SDKCore.getMainAct(),ret.msg,true);
                    RealNameDialog.show(payActivity, false, new RealNameDialog.Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    return;
                }
            }
            dealPaySuccess(dataJson);
            return;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //回调支付失败
        Bus.getDefault().post(EvPay.getFail(JunSConstants.Status.PARSE_ERR, "支付数据解析异常！"));
    }

    private void dealPaySuccess(JSONObject dataJson) {
        try {
            //多平台订单ID
            String moid = dataJson.getString("moid");
            //充值页面地址
            String payUrl = null;
            if (dataJson.has("aid")) {
                payUrl = dataJson.getString("aid");
            }
            //充值比例
            String payRate = null;
            if (dataJson.has("dradio")) {
                payRate = dataJson.getString("dradio");
            }
            //CP货币名称
            String goodsName = null;
            if (dataJson.has("dcn")) {
                goodsName = dataJson.getString("dcn");
            }
            //游戏名称
            String gameName = null;
            if (dataJson.has("gname")) {
                gameName = dataJson.getString("gname");
            }
            //多平台相关信息
            String pData = null;
            if (dataJson.has("mdata")) {
                pData = dataJson.getString("mdata");
            }
            if (!TextUtils.isEmpty(moid)) {
                payParams.put(JunSConstants.PAY_M_ORDER_ID, moid);
                if (!TextUtils.isEmpty(payUrl)) {
                    payParams.put(JunSConstants.PAY_M_URL, payUrl);
                }
                if (!TextUtils.isEmpty(payRate)) {
                    payParams.put(JunSConstants.PAY_M_RATE, payRate);
                }
                if (!TextUtils.isEmpty(goodsName)) {
                    payParams.put(JunSConstants.PAY_M_GOODS_NAME, goodsName);
                }
                if (!TextUtils.isEmpty(gameName)) {
                    payParams.put(JunSConstants.PAY_M_GAME_NAME, gameName);
                }
                if (!TextUtils.isEmpty(pData)) {
                    payParams.put(JunSConstants.PAY_M_DATA, pData);
                }
                if (mPayFlowCallback != null) {
                    mPayFlowCallback.onFinish(payParams);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //回调支付失败
        Bus.getDefault().post(EvPay.getFail(JunSConstants.Status.PARSE_ERR, "支付数据解析异常！"));
    }

    public interface MPayFlowCallback {
        void onFinish(HashMap<String, String> payParams);
    }
}
