package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;


import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import cn.m4399.operate.OperateCenter;
import cn.m4399.operate.OperateCenter.OnCheckFinishedListener;
import cn.m4399.operate.OperateCenter.OnCheckPhoneBindStateListener;
import cn.m4399.operate.OperateCenter.OnDownloadListener;
import cn.m4399.operate.OperateCenter.OnLoginFinishedListener;
import cn.m4399.operate.OperateCenter.OnQuitGameListener;
import cn.m4399.operate.OperateCenter.OnRechargeFinishedListener;
import cn.m4399.operate.OperateCenterConfig;
import cn.m4399.operate.OperateCenterConfig.PopLogoStyle;
import cn.m4399.operate.OperateCenterConfig.PopWinPosition;
import cn.m4399.operate.UpgradeInfo;
import cn.m4399.operate.User;

import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.core.sdk.event.EvLogout;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;



public class m4399 extends OPlatformSDK {
    private static final String TAG = "m4399";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private Context mContext;
    OperateCenterConfig mOpeConfig;
    OperateCenter mOpeCenter;
    public m4399(OPlatformBean pBean) {
        super(pBean);
    }


    @Override
    public void init(Activity activity) {
        mContext = activity;
        //这里对接渠道初始化
        mOpeCenter = OperateCenter.getInstance();
        mOpeConfig = new OperateCenterConfig.Builder(activity)
                .setGameKey(SDKApplication.getPlatformConfig().getExt1())     //设置GameKey
                .setDebugEnabled(false)     //设置DEBUG模式,用于接入过程中开关日志输出，发布前必须设置为false或删除该行。默认为false。
                .setOrientation(Integer.parseInt(SDKApplication.getPlatformConfig().getExt2()))  //设置横竖屏方向，默认为横屏，现支持横竖屏，和180度旋转
                .setSupportExcess(true)     //设置服务端是否支持处理超出部分金额，默认为false
                .setPopLogoStyle(OperateCenterConfig.PopLogoStyle.POPLOGOSTYLE_ONE) //设置悬浮窗样式，现有四种可选
                .setPopWinPosition(OperateCenterConfig.PopWinPosition.POS_LEFT) //设置悬浮窗默认显示位置，现有四种可选
                .build();
        mOpeCenter.setConfig(mOpeConfig);
        mOpeCenter.init(activity, new OperateCenter.OnInitGloabListener() {
            // 初始化结束执行后回调
            @Override
            public void onInitFinished(boolean isLogin, User userInfo) {
                logger.print("init success");

                assert (isLogin == mOpeCenter.isLogin());
                //login2RSService(userInfo);

            }

            // 注销帐号的回调， 包括个人中心里的注销和logout()注销方式
            // fromUserCenter区分是否是从悬浮窗-个人中心("4399游戏助手页面")注销的，若是则为true，不是为false
            @Override
            public void onUserAccountLogout(boolean fromUserCenter) {
             //   logout((Activity) mContext);
                Bus.getDefault().post(new OLogoutEv());
            }

            // 切换帐号的回调
            //fromUserCenter区分是否是从"4399游戏助手页面"切换的，若是则为true，不是为false
            @Override
            public void onSwitchUserAccountFinished(boolean fromUserCenter,User userInfo) {
                Bus.getDefault().post(new OLogoutEv());
                login2RSService(userInfo);
            }
        });
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {
        mOpeCenter.login(activity, new OnLoginFinishedListener() {

            @Override
            public void onLoginFinished(boolean success, int resultCode, User userInfo)
            {
                //登录结束后的游戏逻辑

                //logger.print(userInfo.toString());
                login2RSService(userInfo);
            }
        });

    }

    @Override
    public void logout(Activity mainAct) {
        mOpeCenter.logout();
    }
    /**
     * 支付接口
     * payParams：服务器回传数据
     */
    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        doPay(activity, payParams, payParams.get(JunSConstants.PAY_M_ORDER_ID), payParams.get(JunSConstants.PAY_M_DATA));
    }

    @Override
    public void exitGame(Activity activity) {
        mOpeCenter.shouldQuitGame(activity, new OnQuitGameListener() {

            @Override
            public void onQuitGame(boolean shouldQuit) {
                if(shouldQuit){
                    Bus.getDefault().post(OExitEv.getSucc());
                }else{
                    Bus.getDefault().post(OExitEv.getFail(JunSConstants.Status.CHANNEL_ERR, "user cancel."));
                }
                // 点击“退出游戏”时，shouldQuit为true，游戏处理自己的退出业务逻辑
                // 点击“前往游戏圈”时，shouldQuit为false，SDK会进入游戏圈或者下载
                //  游戏盒子界面，游戏可以不做处理。
                // 点击“留在游戏”时，shouldQuit为false，SDK和游戏都不做任何处理
                // 点击右上角的关闭图标，shouldQuit为false，SDK和游戏都不做任何处理
            }
        });

    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        if (!submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)) {
            sendRoleInfo(submitInfo);
        }
    }




    //渠道登录成功后到服务器验证
    private void login2RSService(User userinfo) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid", userinfo.getUid());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(userinfo.getState(), dataJson.toString());
    }

    /**
     * 提交玩家信息
     */
    private void sendRoleInfo(HashMap<String, String> params) {
        //角色信息上报
        if (!params.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)) {
            mOpeCenter.setServer(params.get(JunSConstants.SUBMIT_SERVER_ID));
        }

    }



    private void doPay(Context context, HashMap<String, String> payParams,
                       String moid, String mData) {
        //支付对接
        JSONObject data=null;

        try {
            data = new JSONObject(mData);
            mOpeCenter.recharge(context,
                    (int)Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY)),             //充值金额（元）
                    moid,           //游戏方订单号
                    data.getString("productname"),    //商品名称
                    new OperateCenter.OnRechargeFinishedListener() {

                        @Override
                        public void onRechargeFinished(
                                boolean success, int resultCode,
                                String msg)
                        {
                            if(success){
                                //请求游戏服，获取充值结果
                                logger.print(msg);
                                Bus.getDefault().post(OPayEv.getSucc(msg));
                            }else{
                                //充值失败逻辑
                                logger.print(msg);
                                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.SDK_ERR, msg));

                            }
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
