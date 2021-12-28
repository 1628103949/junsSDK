package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.params.ReferParam;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.x;
import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.nearme.game.sdk.callback.GameExitCallback;
import com.nearme.game.sdk.common.model.biz.PayInfo;
import com.nearme.game.sdk.common.model.biz.ReportUserGameInfoParam;
import com.nearme.platform.opensdk.pay.PayResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class  OPPO extends OPlatformSDK {
    private static final String TAG = "OPPO";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private Context mContext;


    public OPPO(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        mContext = activity;
        GameCenterSDK.init(SDKApplication.getPlatformConfig().getExt2(), activity);
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {
        oppoDoLogin();
    }

    @Override
    public void logout(Activity mainAct) {

    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        doPay(activity, payParams, payParams.get(JunSConstants.PAY_M_ORDER_ID), payParams.get(JunSConstants.PAY_M_DATA));
    }

    @Override
    public void exitGame(Activity activity) {
        doExitGame();
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        if (!submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)) {
            sendRoleInfo(submitInfo);
        }
    }

    private void oppoDoLogin() {
        GameCenterSDK.getInstance().doLogin(mContext, new ApiCallback() {
            @Override
            public void onSuccess(String resultMsg) {
                logger.print("OPPO doLogin success --> " + resultMsg);
                getTokenAndSsoid();
            }

            @Override
            public void onFailure(String resultMsg, int resultCode) {
                logger.print("OPPO onFailure --> " +
                        "\nresultMsg --> " + resultMsg +
                        "\nresultCode --> " + resultCode);
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, resultMsg));
            }
        });
    }

    /**
     * 获取token和ssoid
     */
    private void getTokenAndSsoid() {

        GameCenterSDK.getInstance().doGetTokenAndSsoid(new ApiCallback() {

            @Override
            public void onSuccess(final String resultMsg) {
                logger.print("OPPO --> getTokenAndSsoid --> " + resultMsg);
                try {
                    JSONObject json = new JSONObject(resultMsg);
                    final String token = json.getString("token");
                    final String ssoid = json.getString("ssoid");
                    final String adId = json.getString("adId");
                    final int channel = json.getInt("channel");
                    //if(SDKData)
                    if(SDKData.isSdkIsRefer()&&SDKData.getSdkKaNeed()) {
                        //logger.print("OPPO --> getTokenAndSsoid22222 --> " + resultMsg);
                        if (channel == 1) {
                            SDKData.setSdkKaNeed(false);
                            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(ssoid)) {
                                login2RSService(mContext, token, ssoid);
                            }
                        } else{
                            logger.print(adId);
                            ReferParam referParam = new ReferParam(adId);
                        x.http().post(referParam, new Callback.CommonCallback<JunSResponse>() {
                            @Override
                            public void onSuccess(JunSResponse tnResponse) {
                                //resultMsg
                                if (tnResponse.state == 1) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(tnResponse.data);
                                        if (!jsonObject.optString("refer").equals("")) {
                                            SDKData.setSdkKaRefer(jsonObject.optString("refer"));
                                            if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(ssoid)) {
                                                login2RSService(mContext, token, ssoid);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(ssoid)) {
                                    login2RSService(mContext, token, ssoid);
                                }
                                //dealReqSuccess(tnResponse);
                            }

                            @Override
                            public void onError(Throwable ex, boolean isOnCallback) {
                                //dealReqFail(ex);
                                if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(ssoid)) {
                                    login2RSService(mContext, token, ssoid);
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
                    }else{
                        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(ssoid)) {
                            login2RSService(mContext, token, ssoid);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, resultMsg));
                }
            }

            @Override
            public void onFailure(String content, int resultCode) {
                logger.print("OPPO -->" +
                        " getTokenAndSsoid onFailure --> " +
                        "\ncontent --> " + content +
                        "\nresultCode --> " + resultCode);
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, content));
            }
        });
    }

    private void login2RSService(Context ctx, String token, String ssoid) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("ssoid", ssoid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }

    /**
     * 提交玩家信息
     */
    private void sendRoleInfo(HashMap<String, String> params) {

        String roleId = params.get(JunSConstants.SUBMIT_ROLE_ID);
        String roleName = params.get(JunSConstants.SUBMIT_ROLE_NAME);
        int roleLevel = 1;
        try {
            roleLevel = Integer.parseInt(params.get(JunSConstants.SUBMIT_ROLE_LEVEL));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        String serverId = params.get(JunSConstants.SUBMIT_SERVER_ID);
        String serverName = params.get(JunSConstants.SUBMIT_SERVER_NAME);
        String chapter = "无";

        GameCenterSDK.getInstance().doReportUserGameInfoData(new ReportUserGameInfoParam(roleId,roleName, roleLevel, serverId, serverName, chapter, new HashMap<String, Number>()),
                new ApiCallback() {

                    @Override
                    public void onSuccess(String resultMsg) {
                        logger.print("OPPO --> doReportUserGameInfoData --> success --> resultMsg --> " + resultMsg);
                    }

                    @Override
                    public void onFailure(String resultMsg, int resultCode) {
                        logger.print("OPPO --> onFailure --> " +
                                "\nresultMsg --> " + resultMsg +
                                "\nresultCode --> " + resultCode);
                    }
                });
    }

    /**
     * 退出游戏
     */
    private void doExitGame() {
        GameCenterSDK.getInstance().onExit((Activity) mContext, new GameExitCallback() {
            @Override
            public void exitGame() {
                Bus.getDefault().post(OExitEv.getSucc());
            }
        });
    }

    private void doPay(Context context, HashMap<String, String> payParams,
                       String moid, String mData) {
        String notifyUrl;
        if (!TextUtils.isEmpty(mData)) {
            JSONObject data;
            try {
                data = new JSONObject(mData);
                notifyUrl = data.getString("notifyurl");
                // PayInfo（1.订单号 （必要） 2.自定义回调字段（否） 3.金额（必要））

                String order_no = moid;
                String custom = moid;
                int rate = data.getInt("rate");
                int goodsCount = data.getInt("goodscount");
                int amount = (int) ((goodsCount * 100 / rate));
                PayInfo payInfo = new PayInfo(order_no, custom, amount);
                payInfo.setProductDesc(data.getString("productdescription"));// 商品描述（否）
                payInfo.setProductName(data.getString("productname"));// 商品名（必要）
                payInfo.setCallbackUrl(notifyUrl);// 回调地址（必要）
                //logger.print("OPPO --> notifyurl --> " + notifyUrl);

                GameCenterSDK.getInstance().doPay(mContext, payInfo, new ApiCallback() {

                    @Override
                    public void onSuccess(String resultMsg) {
                        logger.print("OPPO --> doPay --> Success --> resultMsg --> " + resultMsg);
                        Bus.getDefault().post(OPayEv.getSucc("pay success."));
                    }

                    @Override
                    public void onFailure(String resultMsg, int resultCode) {
                        logger.print("OPPO --> doPay --> onFailure --> " +
                                "\nresultMsg --> " + resultMsg +
                                "\nresultCode --> " + resultCode);
                        if (PayResponse.CODE_CANCEL == resultCode) {
                            // 取消支付处理
                            Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL, "user cancel."));
                        } else {
                            Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, resultMsg));
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                logger.print("OPPO --> doPay --> onException");
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.SDK_ERR, "支付参数解析有误"));
            }
        } else {
            Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.SDK_ERR, "平台支付参数为空"));
        }
    }

}
