package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.event.EvExit;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.qihoo.gamecenter.sdk.activity.ContainerActivity;
import com.qihoo.gamecenter.sdk.common.IDispatcherCallback;
import com.qihoo.gamecenter.sdk.matrix.Matrix;
import com.qihoo.gamecenter.sdk.protocols.CPCallBackMgr;
import com.qihoo.gamecenter.sdk.protocols.ProtocolConfigs;
import com.qihoo.gamecenter.sdk.protocols.ProtocolKeys;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class QiHoo extends OPlatformSDK {
    private static final String TAG = "QiHoo";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private Context mContext;
    private boolean isLandScape = true;

    public QiHoo(OPlatformBean pBean) {
        super(pBean);
    }
    protected CPCallBackMgr.MatrixCallBack mSDKCallback = new CPCallBackMgr.MatrixCallBack() {
        @Override
        public void execute(Context context, int functionCode, String functionParams) {
            if (functionCode == ProtocolConfigs.FUNC_CODE_SWITCH_ACCOUNT) {
                doSdkSwitchAccount();
            }else if (functionCode == ProtocolConfigs.FUNC_CODE_INITSUCCESS) {
                //这里返回成功之后才能调用SDK 登录接口
            }else if (functionCode == ProtocolConfigs.FUNC_CODE_LOGIN){
                // sdk 浮窗内登录的回调，具体参见demo示
            }else if (functionCode == ProtocolConfigs.FUNC_CODE_LOGINAFTER_REALNAME_CALLBACK){
                // 当收到此回调后才可调用SDK提供的实名状态查询接口、打开实名认证界面接口。具体的返回内容是functionParams，数据格式如下。
            }
        }
    };
    protected void doSdkSwitchAccount() {
        Intent intent = getSwitchAccountIntent();
        Matrix.invokeActivity(mContext, intent, mLogoutCallback);
    }
    private Intent getSwitchAccountIntent() {
        Intent intent = new Intent(mContext, ContainerActivity.class);
// 必需参数，使用360SDK的登录模块
        intent.putExtra(ProtocolKeys.FUNCTION_CODE,
                ProtocolConfigs.FUNC_CODE_LOGIN);
// 可选参数，360SDK界面是否以横屏显示，默认为true，横屏
        intent.putExtra(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE,
                isLandScape);
        intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_SWITCH_ACCOUNT);

        return intent;
    }
    @Override
    public void init(Activity activity) {
        mContext = activity;
        //这里对接渠道初始化
        if(SDKApplication.getPlatformConfig().getExt1().equals("1")){
            isLandScape = false;
        }else{
            isLandScape = true;
        }
        Matrix.setActivity(activity, mSDKCallback, false);
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {
       // mIsInOffline = false;
        Intent intent = getLoginIntent();
        IDispatcherCallback callback = mLoginCallback;
//        if (getCheckBoxBoolean(R.id.isSupportOffline)) {
//            callback = mLoginCallbackSupportOffline;
//        }
        Matrix.execute(activity, intent, callback);
    }

    private IDispatcherCallback mLoginCallback = new IDispatcherCallback() {

        @Override
        public void onFinished(String data) {
            // press back
            logger.print(data);
            try {

                JSONObject mdata = new JSONObject(data);
                if(mdata.getInt("errno")==0){
                    JSONObject pdata = new JSONObject(mdata.getString("data"));
                    logger.print(pdata.toString());
                    login2RSService(pdata.getString("access_token"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
           // login2RSService(data);

        }
    };
    private IDispatcherCallback mLogoutCallback = new IDispatcherCallback() {

        @Override
        public void onFinished(String data) {
            // press back
            logger.print("swich:"+data);

            try {

                JSONObject mdata = new JSONObject(data);
                if(mdata.getInt("errno")==0){
                    Bus.getDefault().post(new OLogoutEv());
                    JSONObject pdata = new JSONObject(mdata.getString("data"));
                    logger.print(pdata.toString());
                    login2RSService(pdata.getString("access_token"));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            // login2RSService(data);

        }
    };
    private Intent getLoginIntent() {
        Intent intent = new Intent(mContext, ContainerActivity.class);
// 必需参数，使用360SDK的登录模块
        intent.putExtra(ProtocolKeys.FUNCTION_CODE,
                ProtocolConfigs.FUNC_CODE_LOGIN);
// 可选参数，360SDK界面是否以横屏显示，默认为true，横屏
        intent.putExtra(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE,
                isLandScape);
//-- 以下参数仅仅针对自动登录过程的控制
        // 可选参数，自动登录过程中是否不展示任何UI，默认展示。
//        intent.putExtra(ProtocolKeys.IS_AUTOLOGIN_NOUI,
//                getCheckBoxBoolean(R.id.isAutoLoginHideUI));
        return intent;
    }
    @Override
    public void logout(Activity mainAct) {

    }
    /**
     * 支付接口
     * payParams：服务器回传数据
     */
    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        doSdkPay(activity, payParams);
    }
    protected void doSdkPay(Activity activity, HashMap<String, String> payParams) {

        // 支付基础参数
      //  QihooPayInfo payInfo = getQihooPayInfo(functionCode);
        logger.print(payParams.toString());
        Intent intent = getPayIntent(payParams);

        //  必需参数，使用360SDK的支付模块:CP可以根据需求选择使用带有收银台的支付模块或者直接调用微信支付模块或者直接调用支付宝支付模块。
        //functionCode 对应三种类型的支付模块：
        //ProtocolConfigs.FUNC_CODE_PAY;// 360聚合支付模块。（首次支付有收银台，基于各类因素推荐用户使用每笔订单支付最便捷的支付方式，常规手游可以此方式接入支付，以最小开发成本快速接入上线）
        //ProtocolConfigs.FUNC_CODE_WEIXIN_PAY;//微信支付模块。（无收银台，直接调用微信发起支付，用户设备中需安装微信客户端）
        //ProtocolConfigs.FUNC_CODE_ALI_PAY;//支付宝支付模块。（无收银台，直接调用支付宝发起支付，用户设备中可不安装支付宝客户端）
      //  intent.putExtra(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_PAY);
        Matrix.invokeActivity(activity, intent, mPayCallback);
    }

    protected Intent getPayIntent(HashMap<String, String> payParams) {
        logger.print(payParams.toString());
        Bundle bundle = new Bundle();
        // 界面相关参数，360SDK界面是否以横屏显示。
        String data = payParams.get(JunSConstants.PAY_M_DATA);
        JSONObject mdata = null;
        try {
            mdata = new JSONObject(data);

        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);
// *** 以下非界面相关参数 ***
        // 设置QihooPay中的参数。
        // 必需参数，360帐号id。
        bundle.putString(ProtocolKeys.QIHOO_USER_ID, SDKData.getpUserId());

        // 必需参数，所购买商品金额，以分为单位，最小金额1分
        bundle.putString(ProtocolKeys.AMOUNT, mdata.getInt("moneyAmount")+"");

        // 必需参数，所购买商品名称，应用指定，建议中文，最大10个中文字。
        bundle.putString(ProtocolKeys.PRODUCT_NAME, mdata.getString("productname"));

        // 必需参数，购买商品的商品id，应用指定，最大36中文字。
        bundle.putString(ProtocolKeys.PRODUCT_ID, mdata.getString("productId"));

// 必需参数，应用方提供的支付结果通知uri，最大255字符。360服务器将把支付接口回调给该uri，具体协议请查看文档中，支付结果通知接口–应用服务器提供接口。
        bundle.putString(ProtocolKeys.NOTIFY_URI, mdata.getString("notifyUri"));

// 必需参数，游戏或应用名称，最大16中文字。
        bundle.putString(ProtocolKeys.APP_NAME, getAppName(mContext));

        // 必需参数，应用内的用户名，如游戏角色名。若应用内绑定360帐号和应用帐号，则可用360用户名，最大16中文字。（充值不分区服，充到统一的用户账户，各区服角色均可使用）。
        bundle.putString(ProtocolKeys.APP_USER_NAME, mdata.getString("appUserName"));

        //必需参数，应用内的用户id。
        // 若应用内绑定360帐号和应用帐号，充值不分区服，充到统一的用户账户，各区服角色均可使用，则可用360用户ID最大32字符。
        bundle.putString(ProtocolKeys.APP_USER_ID, mdata.getString("appUserId"));

        //若接入3.3.1下单接口【服务端调用】，则以下两个参数必需传递，由此服务端接口返回值获得；若未接入此服务端接口，则以下两个参数无需传递。
//        intent.putExtra(ProtocolKeys.TOKEN_ID, tokenIdS);
//        intent.putExtra(ProtocolKeys.ORDER_TOKEN, orderIdS);

// 可选参数，应用扩展信息1，原样返回，最大255字符。
        bundle.putString(ProtocolKeys.APP_EXT_1, payParams.get(JunSConstants.PAY_EXT));

// 可选参数，应用扩展信息2，原样返回，最大255字符。
      //  bundle.putString(ProtocolKeys.APP_EXT_2, pay.getAppExt2());

        // 必需参数，应用订单号，应用内必须唯一，最大32字符。。应用方需要生成自己的订单号app_order_id，应用订单号不能重复提交，并且一个应用订单不管是否支付成功，都仅可支付一次，以避免重复支付。若游戏无服务端，此订单号可通过年月日时分秒+设备号等随机数生成即可。
        bundle.putString(ProtocolKeys.APP_ORDER_ID, payParams.get(JunSConstants.PAY_M_ORDER_ID));

        // 必需参数，使用360SDK的支付模块:CP可以根据需求选择使用带有收银台的支付模块或者直接调用微信支付模块或者直接调用支付宝支付模块。
        //functionCode 对应三种支付模块：
        //ProtocolConfigs.FUNC_CODE_PAY;//表示带有360收银台的支付模块。
        //ProtocolConfigs.FUNC_CODE_WEIXIN_PAY;//表示微信支付模块。
        //ProtocolConfigs.FUNC_CODE_ALI_PAY;//表示支付宝支付模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE,ProtocolConfigs.FUNC_CODE_PAY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(mContext, ContainerActivity.class);
        intent.putExtras(bundle);

        return intent;
    }
    public static synchronized String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    protected IDispatcherCallback mPayCallback = new IDispatcherCallback() {
        @Override
        public void onFinished(String data) {
           // Log.d(TAG, "mPayCallback, data is " + data);
            if(TextUtils.isEmpty(data)) {
                return;
            }

            boolean isCallbackParseOk = false;
            JSONObject jsonRes;
            try {
                jsonRes = new JSONObject(data);
// error_code 状态码： 0 支付成功， -1 支付取消， 1 支付失败， -2 支付进行中。
// 请务必对case 0、1、-1、-2加入处理语句，如果为空会导致游戏崩溃。若应用有支付服务端，则需以360服务端通知给应用服务端的结果进行道具发放；若应用无服务端，则0、-2需发放道具；-1、1无需发放道具。
                // error_msg 状态描述
                int errorCode = jsonRes.optInt("error_code");
                isCallbackParseOk = true;
                switch (errorCode) {
                    case 0:
                        Bus.getDefault().post(OPayEv.getSucc("pay success."));
                        break;
                    case 1:

                    case -1:
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, jsonRes.optString("error_msg")));

                    case -2: {
//                        isAccessTokenValid = true;
//                        String errorMsg = jsonRes.optString("error_msg");
//                        String text = getString(R.string.pay_callback_toast, errorCode, errorMsg);
//                        Toast.makeText(SdkUserBaseActivity.this, text, Toast.LENGTH_SHORT).show();

                    }
                    break;
                    case 4010201:
//                        isAccessTokenValid = false;
//                        Toast.makeText(SdkUserBaseActivity.this, R.string.access_token_invalid, Toast.LENGTH_SHORT).show();
                        break;
                    case 4009911:
                        //QT失效
//                        isQTValid = false;
//                        Toast.makeText(SdkUserBaseActivity.this, R.string.qt_invalid, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // 用于测试数据格式是否异常。
            if (!isCallbackParseOk) {
//                Toast.makeText(SdkUserBaseActivity.this, getString(R.string.data_format_error),
//                        Toast.LENGTH_LONG).show();
            }
        }
    };
    @Override
    public void exitGame(Activity activity) {
        doExitGame();
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
       // logger.print(submitInfo.toString());
      //  if (!submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)) {
            sendRoleInfo(submitInfo);
       // }
    }





    //渠道登录成功后到服务器验证
    private void login2RSService(String token) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("ssoid", token);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, null);
    }

    /**
     * 提交玩家信息
     */
    private void sendRoleInfo(HashMap<String, String> params) {
        //角色信息上报
        logger.print(params.toString());
        //帐号余额

        String type = "";
        switch (params.get(JunSConstants.SUBMIT_TYPE)){
            case JunSConstants.SUBMIT_TYPE_CREATE:
                type = "createRole";
                break;
            case JunSConstants.SUBMIT_TYPE_ENTER:
                type = "enterServer";
                break;
            case JunSConstants.SUBMIT_TYPE_UPGRADE:
                type = "levelUp";
                break;
        }
        HashMap<String, String> eventParams=new HashMap<String, String>();

        eventParams.put("type",type);  //（必填）角色状态（enterServer（登录），levelUp（升级），createRole（创建角色），exitServer（退出））
        eventParams.put("zoneid",params.get(JunSConstants.SUBMIT_SERVER_ID));  //（必填）游戏区服ID
        eventParams.put("zonename",params.get(JunSConstants.SUBMIT_SERVER_NAME));  //（必填）游戏区服名称
        eventParams.put("roleid",params.get(JunSConstants.SUBMIT_ROLE_ID));  //（必填）玩家角色ID
        eventParams.put("rolename",params.get(JunSConstants.SUBMIT_ROLE_NAME));  //（必填）玩家角色名
        eventParams.put("professionid","0");  //（必填）职业ID
        eventParams.put("profession","无");  //（必填）职业名称
        eventParams.put("gender","无");  //（必填）性别

        eventParams.put("rolelevel",params.get(JunSConstants.SUBMIT_ROLE_LEVEL));  //（必填）玩家角色等级
        eventParams.put("power","0");  //（必填）战力数值
        eventParams.put("vip",params.get(JunSConstants.SUBMIT_VIP));  //（必填）当前用户VIP等级
        eventParams.put("balance","0");  //（必填）帐号余额
        eventParams.put("partyid","0");  //（必填）所属帮派帮派ID
        eventParams.put("partyname","无");  //（必填）所属帮派名称
        eventParams.put("partyroleid","0");  //（必填）帮派称号ID
        eventParams.put("partyrolename","无");  //（必填）帮派称号名称
        eventParams.put("friendlist","无");  //（必填）好友关系
        //eventParams.put("ranking",ranklist.toString());  //（选填）排行榜列表
        //参数eventParams相关的 key、value键值对相关具体使用说明，请参考文档。
        //----------------------------模拟数据------------------------------
        Matrix.statEventInfo(mContext, eventParams);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        Matrix.destroy(mainAct);
    }

    /**
     * 退出游戏
     */
    private void doExitGame() {
        //游戏退出
        Bundle bundle = new Bundle();

// 界面相关参数，360SDK界面是否以横屏显示。
        bundle.putBoolean(ProtocolKeys.IS_SCREEN_ORIENTATION_LANDSCAPE, isLandScape);

        // 可选参数，登录界面的背景图片路径，必须是本地图片路径
        bundle.putString(ProtocolKeys.UI_BACKGROUND_PICTRUE, "");

// 必需参数，使用360SDK的退出模块。
        bundle.putInt(ProtocolKeys.FUNCTION_CODE, ProtocolConfigs.FUNC_CODE_QUIT);

        Intent intent = new Intent(mContext, ContainerActivity.class);
        intent.putExtras(bundle);

        Matrix.invokeActivity(mContext, intent, mQuitCallback);

    }
    private IDispatcherCallback mQuitCallback = new IDispatcherCallback() {
        @Override
        public void onFinished(String data) {
// TODO your job
            logger.print(data);
            try {
                JSONObject mdata = new JSONObject(data);
                if(mdata.getInt("which")!=0){
                    Bus.getDefault().post(OExitEv.getSucc());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


}
