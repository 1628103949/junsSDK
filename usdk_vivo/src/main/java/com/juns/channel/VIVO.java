package com.juns.channel;

import android.app.Activity;
import android.text.TextUtils;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.api.JunSSdkApi;
import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.params.MPayQueryParam;
import com.juns.sdk.core.http.params.ReferParam;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;

import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.ads.JunsAds;

import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.http.RequestParams;
import com.juns.sdk.framework.xutils.x;
import com.vivo.unionsdk.open.ChannelInfoCallback;
import com.vivo.unionsdk.open.MissOrderEventHandler;
import com.vivo.unionsdk.open.OrderResultInfo;
import com.vivo.unionsdk.open.VivoAccountCallback;
import com.vivo.unionsdk.open.VivoConstants;
import com.vivo.unionsdk.open.VivoExitCallback;
import com.vivo.unionsdk.open.VivoPayCallback;
import com.vivo.unionsdk.open.VivoPayInfo;
import com.vivo.unionsdk.open.VivoRoleInfo;
import com.vivo.unionsdk.open.VivoUnionSDK;
import com.vivounion.ic.channelreader.ChannelReaderUtil;
import com.vivounion.ic.channelunit.ChannelUnit;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VIVO extends OPlatformSDK {
    private static final String TAG = "VIVO";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private String vivoDeliverUrl;
    private String vivoOPenId;
    private Activity mActivity;
    private boolean isPayFlow = false;
    private VivoAccountCallback vivoAccountCallback = new VivoAccountCallback() {

        @Override
        public void onVivoAccountLogout(int requestCode) {
            // 注销登录操作
            Bus.getDefault().post(new OLogoutEv());
        }

        @Override
        public void onVivoAccountLoginCancel() {
            //登录被取消
            Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "user cancel login."));
        }

        @Override
        public void onVivoAccountLogin(String userName, String openId, String authToken) {
            // authToken：第三方游戏用此token到vivo帐户系统服务端校验帐户信息
            // openId：帐户唯一标识, 支付需要用到
            // userName:帐户名
            logger.print("vivo login success" +
                    "\nuserName --> " + userName +
                    "\nopenId --> " + openId +
                    "\nauthToken --> " + authToken);
            vivoOPenId = openId;
            //VivoUnionSDK.queryMissOrderResult(openId);
            login2RSService(authToken, userName, openId);
        }
    };

    private VivoExitCallback vivoExitCallback = new VivoExitCallback() {
        @Override
        public void onExitCancel() {
            Bus.getDefault().post(OExitEv.getFail(JunSConstants.Status.CHANNEL_ERR, "user cancel."));
        }

        @Override
        public void onExitConfirm() {
            Bus.getDefault().post(OExitEv.getSucc());
        }
    };

    private VivoPayCallback vivoPayCallback = new VivoPayCallback() {
        // 客户端返回的支付结果不可靠，请再查询服务器，以服务器端最终的支付结果为准；
        @Override
        public void onVivoPayResult(int i, OrderResultInfo orderResultInfo) {


            if (i == VivoConstants.PAYMENT_RESULT_CODE_SUCCESS) {
                List<String> list = new ArrayList<>();
                list.add(orderResultInfo.getTransNo());
                orderQuery2(orderResultInfo);
                //VivoUnionSDK.reportOrderComplete(list, false);
                Bus.getDefault().post(OPayEv.getSucc("pay success."));
            } else if (i == VivoConstants.PAYMENT_RESULT_CODE_CANCEL) {
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL, "user cancel."));
                //Toast.makeText(MainActivity.this, "取消支付", Toast.LENGTH_SHORT).show();
            } else if (i == VivoConstants.PAYMENT_RESULT_CODE_UNKNOWN) {
                VivoUnionSDK.queryMissOrderResult(vivoOPenId);
                //Toast.makeText(MainActivity.this, "未知状态，请查询订单", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(MainActivity.this, "支付失败", Toast.LENGTH_SHORT).show();
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "pay fail"));
            }
        }
    };

    public VIVO(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        mActivity = activity;
        logger.print("vivo init Success");
        /**
         * 掉单注册接口  需要接入掉单补单处理的一定要加
         * !!!! 一定要加，否则无法通过上架审核 !!!
         * 作用：商品补发回调
         * 场景：支付完成后，游戏未正常发放商品，或发放后未成功通知到vivo侧，在异常订单查询后自动触发
         */
        VivoUnionSDK.registerMissOrderEventHandler(activity, mMissOrderEventHandler);
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {
        vivoDoLogin();
    }

    @Override
    public String prePay(Activity mainAct) {
        try {
            JSONObject dataJson = new JSONObject();
            dataJson.put("extuid", vivoOPenId);
            return dataJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        vivoDoPay(payParams, payParams.get(JunSConstants.PAY_M_ORDER_ID), payParams.get(JunSConstants.PAY_M_DATA));
    }

    @Override
    public void exitGame(Activity activity) {
        // 退出游戏
        VivoUnionSDK.exit(activity, vivoExitCallback);
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        if (submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)) {
            VivoUnionSDK.queryMissOrderResult(vivoOPenId);
            sendRoleInfo(submitInfo);
        }
    }

    /**
     * 登录，
     */
    private void vivoDoLogin() {
        isPayFlow = false;
        // 注册登录回调
        VivoUnionSDK.registerAccountCallback(mActivity, vivoAccountCallback);
        // 登录
        VivoUnionSDK.login(mActivity);
    }

    private void login2RSService(String authToken, String userName, String openId) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("userName", userName);
            dataJson.put("openId", openId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(authToken, dataJson.toString());
    }

    /**
     * 提交玩家信息
     */
    private void sendRoleInfo(HashMap<String, String> params) {
        String roleId = params.get(JunSConstants.SUBMIT_ROLE_ID);
        String roleLevel = params.get(JunSConstants.SUBMIT_ROLE_LEVEL);
        String roleName = params.get(JunSConstants.SUBMIT_ROLE_NAME);
        String serviceAreaID = params.get(JunSConstants.SUBMIT_SERVER_ID);
        String serviceAreaName = params.get(JunSConstants.SUBMIT_SERVER_NAME);
        // vivo汇报角色信息
        VivoUnionSDK.reportRoleInfo(new VivoRoleInfo(roleId, roleLevel, roleName, serviceAreaID, serviceAreaName));
    }

    private void vivoDoPay(HashMap<String, String> payParams,
                           String moid, String mData) {
        VivoUnionSDK.queryMissOrderResult(vivoOPenId);
        isPayFlow = true;
        // 组织调用支付接口需要的参数，参考附录“启动vivo支付中心参数表”
        // 必填参数
        String orderAmount; // 单位为分
        String productName; // 商品名称
        String productDesc; // 商品描述
        String vivoSignature;
        String notifyUrl;
        String appId; // 由开发者平台申请得到
        String cpOrderNumber;
        String roleId;
        String roleName;
        String serverName;


        // 以下为可选参数，能收集到务必填写，如未填写，掉单、用户密码找回等问题可能无法解决。

        // 获取必要参数
        if (mData != null) {
            try {
                JSONObject payJson = new JSONObject(mData);
                orderAmount = payJson.getString("orderAmount");
                productName = payJson.getString("productName");
                productDesc = payJson.getString("productDesc");
                vivoSignature = payJson.getString("vivoSignature");
                notifyUrl = payJson.getString("notifyUrl");
                vivoDeliverUrl = payJson.getString("notifyUrl");
                appId = payJson.getString("appId");
                cpOrderNumber = payJson.getString("cpOrderNumber");
                roleId = payJson.getString("roleId");
                roleName = payJson.getString("roleName");
                serverName = payJson.getString("serverName");
                cpOrderNumber = payJson.getString("cpOrderNumber");
            } catch (JSONException e) {
                e.printStackTrace();
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "支付参数解析失败！"));
                return;
            }

            VivoPayInfo vivoPayInfo = new VivoPayInfo.Builder()
                    //基本支付信息
                    .setAppId(appId)
                    .setCpOrderNo(cpOrderNumber)
                    .setNotifyUrl(notifyUrl)
                    .setOrderAmount(orderAmount)
                    .setProductDesc(productDesc)
                    .setProductName(productName)
                    //角色信息
                    .setRoleId(roleId)
                    .setRoleName(roleName)
                    .setServerName(serverName)
                    //计算出来的参数验签
                    .setVivoSignature(vivoSignature)
                    //接入vivo帐号传uid，未接入传""
                    .setExtUid(vivoOPenId)
                    .build();
            VivoUnionSDK.payV2(mActivity, vivoPayInfo, vivoPayCallback);
        } else {
            // 渠道支付参数获取失败
            Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "渠道支付参数获取失败！"));
        }
    }

    /**
     * 用户主动触发或调用queryMissOrderResult查询回调会在此做处理
     */
    private MissOrderEventHandler mMissOrderEventHandler = new MissOrderEventHandler() {
        @Override
        public void process(List orderResultInfos) {
            logger.print("registerOrderResultEventHandler: orderResultInfos = " + orderResultInfos);
            //Log.i(TAG, "registerOrderResultEventHandler: orderResultInfos = " + orderResultInfos);
            /**
             * 注意这里是查到未核销的订单
             * 需要调用自己的逻辑完成道具核销后再调用我们的订单完成接口
             * 切记！！！一定要走自己逻辑发送完道具后再调用完成接口！！！切记！切记！
             * ！！！游戏根据订单号检查、补发商品！！！
             * 自行完成补发逻辑  一定要完成道具补发后才能调用完成接口 此处一定要注意！！！
             * 如果不处理直接调用完成则掉单无法解决
             * 注意！！！注意！！！
             * 游戏侧用你们自己的订单号cpOrderNumber来校验是否完成发货  发货完成上报我们的订单号transNo
             */
            checkOrder(orderResultInfos);
        }
    };

    /**
     * 校验订单是否已经完成发货（游戏自己逻辑）
     * 未完成的执行发货操作
     * @param list
     */
    public void checkOrder(final List<OrderResultInfo> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        //List<String> orderList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            /**
             * 校验是否已经完成发货 如果已经完成发货则加入完成列表
             * 未完成则调用发货流程
             * 这里修改成自己的校验流程 调用游戏自己的服务器 注意！！！注意！！！
             */

                /**
                 * 未完成发货的执行发货流程
                 */
                logger.print("进行补单");
                //severDeliverGoods(list.get(i));
                orderQuery(list.get(i));

        }

        /**
         * ！！！批量订单补发完成后调用完成接口 ！！！
         */
        //VivoUnionSDK.reportOrderComplete(orderList,true);
    }




    private void orderQuery2(final OrderResultInfo orderResultInfo){
        logger.print("查询的订单号"+orderResultInfo.getCpOrderNumber()
                +"token"+SDKData.getSdkUserToken()
                +"uid"+SDKData.getSdkUserId()
                +"uname"+SDKData.getSdkUserName()
                +"uuid"+SDKData.getCurrentMPayOrder()
        );
        SDKData.setCurrentMPayOrder(orderResultInfo.getCpOrderNumber());
        //所有支付状态都查询一遍
        MPayQueryParam mPayQueryParam = new MPayQueryParam();
        x.http().post(mPayQueryParam, new Callback.CommonCallback<JunSResponse>() {
            @Override
            public void onSuccess(JunSResponse response) {
                logger.print("orderQuery onSuccess");
                    if(response.state == 1){
                        logger.print("orderQuery success");
                        List<String> list = new ArrayList<>();
                        list.add(orderResultInfo.getTransNo());
                        VivoUnionSDK.reportOrderComplete(list,false);
                    }else{

                    }
                //dealPayCallback(evPay);
            }

            @Override
            public void onError(final Throwable ex, boolean isOnCallback) {
                logger.print("orderQuery onError");
                //不用管
                //dealPayCallback(evPay);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                logger.print("orderQuery onCancelled");
            }

            @Override
            public void onFinished() {
                logger.print("orderQuery onFinished");
            }
        });
    }
    private void orderQuery(final OrderResultInfo orderResultInfo){
        logger.print("查询的订单号"+orderResultInfo.getCpOrderNumber()
                +"token"+SDKData.getSdkUserToken()
                +"uid"+SDKData.getSdkUserId()
                +"uname"+SDKData.getSdkUserName()
                +"uuid"+SDKData.getCurrentMPayOrder()
        );
//        List<String> list = new ArrayList<>();
//        list.add(orderResultInfo.getTransNo());
//        VivoUnionSDK.reportOrderComplete(list,true);
        SDKData.setCurrentMPayOrder(orderResultInfo.getCpOrderNumber());
        //所有支付状态都查询一遍
        MPayQueryParam mPayQueryParam = new MPayQueryParam();
        x.http().post(mPayQueryParam, new Callback.CommonCallback<JunSResponse>() {
            @Override
            public void onSuccess(JunSResponse response) {
                logger.print("orderQuery onSuccess");
                if(response.state == 1){
                    logger.print("orderQuery success");
                    List<String> list = new ArrayList<>();
                    list.add(orderResultInfo.getTransNo());
                    VivoUnionSDK.reportOrderComplete(list,true);
                }else{

                }
                //dealPayCallback(evPay);
            }

            @Override
            public void onError(final Throwable ex, boolean isOnCallback) {
                logger.print("orderQuery onError");
                //不用管
                //dealPayCallback(evPay);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                logger.print("orderQuery onCancelled");
            }

            @Override
            public void onFinished() {
                logger.print("orderQuery onFinished");
            }
        });
    }

    @Override
    public void preInit(Activity mainAct) {
        super.preInit(mainAct);
        if(SDKData.isSdkIsRefer()&&SDKData.getSdkKaNeed()){
            logger.print("channelinfo:"+"需要请求接口");
            logger.print("channelinfo:"+"isrefer"+SDKData.isSdkIsRefer());
            logger.print("channelinfo:"+"isneed"+SDKData.getSdkKaNeed());
            SDKData.setSdkKaState(1);
            if(!TextUtils.isEmpty(SDKData.getSdkKaAdid())){
                ReferParam referParam = new ReferParam(SDKData.getSdkKaAdid());
                x.http().post(referParam, new Callback.CommonCallback<JunSResponse>() {
                    @Override
                    public void onSuccess(JunSResponse tnResponse) {
                        //resultMsg
                        if(tnResponse.state == 1){
                            try {
                                JSONObject jsonObject = new JSONObject(tnResponse.data);
                                if(!jsonObject.optString("refer").equals("")){
                                    SDKData.setSdkKaRefer(jsonObject.optString("refer"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        SDKData.setSdkKaState(2);
                        //dealReqSuccess(tnResponse);
                    }

                    @Override
                    public void onError(Throwable ex, boolean isOnCallback) {
                        //dealReqFail(ex);
                        SDKData.setSdkKaState(2);
                    }

                    @Override
                    public void onCancelled(CancelledException cex) {

                    }

                    @Override
                    public void onFinished() {

                    }
                });
            }else {
                VivoUnionSDK.getChannelInfo(new ChannelInfoCallback() {
                    @Override
                    public void onReadResult(String s) {
                        logger.print("channelinfo:" + s);
                        if (!s.equals("")) {
                            SDKData.setSdkKaAdid(s);
                            ReferParam referParam = new ReferParam(s);
                            x.http().post(referParam, new Callback.CommonCallback<JunSResponse>() {
                                @Override
                                public void onSuccess(JunSResponse tnResponse) {
                                    //resultMsg
                                    if (tnResponse.state == 1) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(tnResponse.data);
                                            if (!jsonObject.optString("refer").equals("")) {
                                                SDKData.setSdkKaRefer(jsonObject.optString("refer"));
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    SDKData.setSdkKaState(2);
                                    //dealReqSuccess(tnResponse);
                                }

                                @Override
                                public void onError(Throwable ex, boolean isOnCallback) {
                                    //dealReqFail(ex);
                                    SDKData.setSdkKaState(2);
                                }

                                @Override
                                public void onCancelled(CancelledException cex) {

                                }

                                @Override
                                public void onFinished() {

                                }
                            });
                        } else {
                            SDKData.setSdkKaState(2);
                            SDKData.setSdkKaNeed(false);
                        }

                    }
                });
            }
        }
    }
}
