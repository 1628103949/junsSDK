package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.common.InitInfoCallBack;
import com.juns.sdk.core.sdk.common.InitInfoDialog;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.xbus.Bus;
import com.xiaomi.gamecenter.sdk.GameInfoField;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnExitListner;
import com.xiaomi.gamecenter.sdk.OnLoginProcessListener;
import com.xiaomi.gamecenter.sdk.OnPayProcessListener;
import com.xiaomi.gamecenter.sdk.RoleData;
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo;
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfo;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;

public class XiaoMi extends OPlatformSDK {
    private static final String TAG = "XiaoMi";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private Context mContext;


    public XiaoMi(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        mContext = activity;
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(final Activity activity) {
        xiaomiDoLogin();
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        //MiCommplatform.getInstance().onMainActivityDestory();

    }

    @Override
    public void logout(Activity mainAct) {

    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        doPay(payParams, payParams.get(JunSConstants.PAY_M_ORDER_ID), payParams.get(JunSConstants.PAY_M_DATA));
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

    private void xiaomiDoLogin() {
        MiCommplatform.getInstance().miLogin((Activity) mContext, new OnLoginProcessListener() {
            @Override
            public void finishLoginProcess(int code, MiAccountInfo miAccountInfo) {
                switch (code){
                    case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
                        String uid = miAccountInfo.getUid();
                        String name = miAccountInfo.getNikename();
                        String session = miAccountInfo.getSessionId();
                        getTokenAndSsoid(uid,name,session);
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_LOGIN_FAIL:
                        // 登陆失败
                        Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "fail"));
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                        // 取消登录
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                        //登录操作正在进⾏中
                        break;
                    default:
                        // 登录失败
                        break;
                }
            }
        });

    }

    /**
     * 获取token和ssoid
     */
    private void getTokenAndSsoid(String uid,String name,String session) {
        if (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(session)) {
            login2RSService(mContext, uid,name, session);
        }
    }

    private void login2RSService(Context ctx, String uid, String name, String session) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid", uid);
            dataJson.put("puname", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(session, dataJson.toString());

    }

    /**
     * 提交玩家信息
     */
    private void sendRoleInfo(HashMap<String, String> params) {
        RoleData data = new RoleData();
        data.setLevel(params.get(JunSConstants.SUBMIT_ROLE_LEVEL));
        data.setRoleId(params.get(JunSConstants.SUBMIT_ROLE_ID));
        data.setRoleName(params.get(JunSConstants.SUBMIT_ROLE_NAME));
        data.setServerId(params.get(JunSConstants.SUBMIT_SERVER_ID));
        data.setServerName(params.get(JunSConstants.SUBMIT_SERVER_NAME));
        MiCommplatform.getInstance().submitRoleData((Activity) mContext, data);

    }

    /**
     * 退出游戏
     */
    private void doExitGame() {
        MiCommplatform.getInstance().miAppExit((Activity) mContext, new OnExitListner() {
            @Override
            public void onExit(int code) {
                if ( code == MiErrorCode.MI_XIAOMI_EXIT )
                {
                    Bus.getDefault().post(OExitEv.getSucc());
                }

            }
        });

    }

    private void doPay( HashMap<String, String> payParams,
                       String moid, String mData) {
        if (!TextUtils.isEmpty(mData)) {
            try {
                String order_no = moid;
                float momey = Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));
                MiBuyInfo miBuyInfo = new MiBuyInfo();
                miBuyInfo.setCpOrderId(order_no); //订单号唯⼀（不为空）
                miBuyInfo.setCpUserInfo(payParams.get(JunSConstants.PAY_EXT)); //此参数在⽤⼾⽀付成功后会透传给CP的服务器
                miBuyInfo.setAmount((int) momey); //必须是⼤于1的整数，10代表10⽶币，即10元⼈⺠币（不为空）
                //⽤⼾信息，⽹游必须设置、单机游戏或应⽤可选
                Bundle mBundle = new Bundle();
                mBundle.putString(GameInfoField.GAME_USER_BALANCE, payParams.get(JunSConstants.PAY_ROLE_BALANCE)); //⽤⼾余额
                mBundle.putString(GameInfoField.GAME_USER_GAMER_VIP, payParams.get(JunSConstants.PAY_ROLE_VIP)); //vip等级
                mBundle.putString(GameInfoField.GAME_USER_LV, payParams.get(JunSConstants.PAY_ROLE_LEVEL)); //⻆⾊等级
                mBundle.putString(GameInfoField.GAME_USER_PARTY_NAME, payParams.get(JunSConstants.PAY_ROLE_LEVEL)); //⼯会，帮派
                mBundle.putString(GameInfoField.GAME_USER_ROLE_NAME, payParams.get(JunSConstants.PAY_ROLE_NAME)); //⻆⾊名称
                mBundle.putString(GameInfoField.GAME_USER_ROLEID, payParams.get(JunSConstants.PAY_ROLE_ID)); //⻆⾊id
                mBundle.putString(GameInfoField.GAME_USER_SERVER_NAME, payParams.get(JunSConstants.PAY_SERVER_NAME)); //所在服务器
                miBuyInfo.setExtraInfo(mBundle); //设置⽤⼾信息
                logger.print("mipayinfo"+miBuyInfo.toString());
                logger.print("mipayinfo2"+mBundle.toString());
                MiCommplatform.getInstance().miUniPay((Activity) mContext, miBuyInfo, new OnPayProcessListener() {
                    @Override
                    public void finishPayProcess(int code) {
                        switch (code) {
                            case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS: //购买成功
                                Bus.getDefault().post(OPayEv.getSucc("pay success."));
                                break;
                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_CANCEL: //取消购买
                                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL, "user cancel."));
                                break;
                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_FAILURE: //购买失败
                                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "fail"));
                                break;
                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED: //操作正在进⾏中
                                break;
                            default: //购买失败
                                break;
                        }
                    }
                });
//
            } catch (Exception e) {
                e.printStackTrace();
                logger.print("XiaoMi --> doPay --> onException");
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.SDK_ERR, "支付参数解析有误"));
            }
        } else {
            Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.SDK_ERR, "平台支付参数为空"));
        }
    }


}
