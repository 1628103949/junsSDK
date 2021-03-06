package com.juns.channel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.PopupWindow;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.own.common.JunSWebDialog;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.framework.common.ResUtil;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.safe.JunSEncrypt;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.FadeEnter.FadeEnter;
import com.juns.sdk.framework.view.dialog.FadeExit.FadeExit;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.http.RequestParams;
import com.juns.sdk.framework.xutils.x;
import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.BaseRet;
import com.tencent.ysdk.framework.common.eFlag;
import com.tencent.ysdk.framework.common.ePlatform;
import com.tencent.ysdk.module.antiaddiction.listener.AntiAddictListener;
import com.tencent.ysdk.module.antiaddiction.listener.AntiRegisterWindowCloseListener;
import com.tencent.ysdk.module.antiaddiction.model.AntiAddictRet;
import com.tencent.ysdk.module.bugly.BuglyListener;
import com.tencent.ysdk.module.pay.PayListener;
import com.tencent.ysdk.module.pay.PayRet;
import com.tencent.ysdk.module.user.PersonInfo;
import com.tencent.ysdk.module.user.UserListener;
import com.tencent.ysdk.module.user.UserLoginRet;
import com.tencent.ysdk.module.user.UserRelationRet;
import com.tencent.ysdk.module.user.WakeupRet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class YSDK extends OPlatformSDK {
    private static final String TAG = "YYB";
    private JunSWebDialog junSWebDialog;
    // ???????????????????????????
    public static boolean mAntiAddictExecuteState = false;
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private Context mContext;
    private JSONObject ysdkPayJson;
    private YSDKLoginDialog loginDialog;
    private YSDKCallback mYSDKCallback = new YSDKCallback();
    private YSDKLoginDialog.LoginViewCallback loginViewCallback = new YSDKLoginDialog.LoginViewCallback() {
        @Override
        public void onQQ() {
            //YSDKApi.logout();
            YSDKApi.login(ePlatform.QQ);
        }

        @Override
        public void onWX() {
            //YSDKApi.logout();
            YSDKApi.login(ePlatform.WX);
        }
    };

    public YSDK(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        mContext = activity;
        //YSDKApi.init();
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {
        ysdkLogin(activity);
    }

    @Override
    public void logout(Activity mainAct) {
        YSDKApi.logout();
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        logger.print("ysdkpayinfo"+payParams.toString());
        doPay(payParams.get(JunSConstants.PAY_M_ORDER_ID), payParams.get(JunSConstants.PAY_M_DATA));
    }

    @Override
    public void exitGame(Activity activity) {
        YSDKExitDialog.showExit(activity, new YSDKExitDialog.ExitCallback() {
            @Override
            public void toContinue() {
                Bus.getDefault().post(OExitEv.getFail(JunSConstants.Status.CHANNEL_ERR, "user cancel."));
            }

            @Override
            public void toExit() {
                YSDKApi.setAntiAddictGameEnd();
                Bus.getDefault().post(OExitEv.getSucc());
            }
        });
    }
    YSDKAutoLoginDialog ysdkAutoLoginDialog =null;
    private void ysdkLogin(final Activity activity) {
        final UserLoginRet userLoginRet = new UserLoginRet();
        YSDKApi.getLoginRecord(userLoginRet);
        //Log.e("YSDKTEST",userLoginRet.toString());
        //showLoginView();
        if(userLoginRet.flag!=0){
            showLoginView();
        }else{
            YSDKApi.autoLogin();

        }

    }

    private void showLoginView() {
        if (loginDialog != null) {
            if (loginDialog.isShowing()) {
                loginDialog.dismiss();
            }
            loginDialog = null;
        }
        loginDialog = new YSDKLoginDialog(mContext, loginViewCallback);
        loginDialog.dimEnabled(true);
        loginDialog.show();
    }

    private void dismissLoginView() {
        if (loginDialog != null) {
            if (loginDialog.isShowing()) {
                loginDialog.dismiss();
            }
            loginDialog = null;
        }
    }

    private void loginToSever(UserLoginRet userLoginRet) {
        //Log.e("guoinfo","loginToSever");
        JSONObject jsonData = new JSONObject();
        ysdkPayJson = new JSONObject();
        try {
            //1 --> QQ?????????2--> ??????
            if (userLoginRet.platform == ePlatform.PLATFORM_ID_QQ) {
                jsonData.put("pflag", "QQ");
            } else if (userLoginRet.platform == ePlatform.PLATFORM_ID_WX) {
                jsonData.put("pflag", "weixin");
            }
            jsonData.put("puid", userLoginRet.open_id);

            //ysdk ??????????????????
            ysdkPayJson.put("ret", userLoginRet.ret);
            ysdkPayJson.put("openid", userLoginRet.open_id);
            ysdkPayJson.put("pf", userLoginRet.pf);
            ysdkPayJson.put("pay_token", userLoginRet.getPayToken());
            ysdkPayJson.put("pfkey", userLoginRet.pf_key);
            ysdkPayJson.put("access_token", userLoginRet.getAccessToken());
            if (userLoginRet.platform == ePlatform.PLATFORM_ID_QQ) {
                ysdkPayJson.put("flag", "QQ");
            } else if (userLoginRet.platform == ePlatform.PLATFORM_ID_WX) {
                ysdkPayJson.put("flag", "weixin");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(userLoginRet.getAccessToken(), jsonData.toString());
    }

    @Override
    public String prePay(Activity mainAct) {
        return ysdkPayJson.toString();
    }

    private void doPay(String moid, String mData) {
        try {
            JSONObject dataJson = new JSONObject(mData);
            int gcradio = dataJson.getInt("gcradio");   //????????????
            String verifyToken = dataJson.getString("verifyToken");
            int balance = dataJson.getInt("balance");   //??????
            int save_amt = dataJson.getInt("save_amt"); //??????????????????
            int costGameCoins = dataJson.getInt("costGameCoins");   //??????????????????
            String midasPayUrl = dataJson.getString("midasPayUrl"); //????????????
            String zoneid = dataJson.getString("zoneid");   //??????ID
            String paytoken = dataJson.getString("paytoken");
            String pfKey = dataJson.getString("pfKey");
            String productName = dataJson.getString("productName");

            ysdkPayJson.put("gcradio", gcradio);
            ysdkPayJson.put("verifyToken", verifyToken);
            ysdkPayJson.put("balance", balance);
            ysdkPayJson.put("save_amt", save_amt);
            ysdkPayJson.put("costGameCoins", costGameCoins);
            ysdkPayJson.put("midasPayUrl", midasPayUrl);
            ysdkPayJson.put("zoneid", zoneid);
            ysdkPayJson.put("paytoken", paytoken);
            ysdkPayJson.put("pfKey", pfKey);
            ysdkPayJson.put("productName", productName);
            ysdkPayJson.put("moid", moid);

            if (balance >= costGameCoins) {
                //?????????????????????????????????
                StringBuilder payDialogContent = new StringBuilder();
                payDialogContent.setLength(0);
                payDialogContent.append("???????????????" + costGameCoins / gcradio + "\n");
                showTencentPayDialog(false, payDialogContent, null, null);
            } else {
                //????????????
                if (balance > 0) {
                    //????????????0
                    StringBuilder payDialogContent = new StringBuilder();
                    int money_second = (costGameCoins - balance) / gcradio;
                    payDialogContent.append("??????????????????????????????????????????:" + money_second + "???" + "\n");
                    payDialogContent.append("????????????????");
                    showTencentPayDialog(true, payDialogContent, zoneid, money_second * gcradio + "");
                } else {
                    //??????????????????
                    launchTCPay(zoneid, costGameCoins + "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????/???????????????
     *
     * @param isCallTencentPay ???????????????????????????true??????????????????false
     * @param dialogMsg        ???????????????
     */
    private void showTencentPayDialog(final boolean isCallTencentPay, StringBuilder dialogMsg, final String sid, final String money) {

        AlertDialog.Builder payBuilder = null;
        payBuilder = new AlertDialog.Builder(mContext);
        payBuilder.setTitle("????????????");
        payBuilder.setMessage(dialogMsg.toString());
        payBuilder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (isCallTencentPay) {
                        launchTCPay(sid, money);
                    } else {
                        // ?????????
                        toCostMoney();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        payBuilder.setNegativeButton("??????", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ???????????????????????????????????????
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "user cancel."));
            }
        });

        payBuilder.setCancelable(false);
        Dialog payDialog = payBuilder.create();
        payDialog.show();

    }

    private void launchTCPay(String sid, String money) {
        // ????????????
        int appresID = ResUtil.getDrawableID("fy_product", mContext);
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), appresID);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appResData = baos.toByteArray();
        // ???????????????
        boolean isCanChange = false;
        // ????????????
        String ysdkExt = "ysdkExt";
        // ???????????????YSDKCallback???
        YSDKApi.recharge(sid, money, isCanChange, appResData, ysdkExt, new YSDKCallback());
    }

    @Override
    public void onCreate(Activity mainAct) {
        //YSDKApi.onCreate(mainAct);
        YSDKApi.init(false);
        YSDKApi.setUserListener(mYSDKCallback);
        YSDKApi.setBuglyListener(mYSDKCallback);
        YSDKApi.setAntiAddictListener(mYSDKCallback);
        YSDKApi.setAntiRegisterWindowCloseListener(mYSDKCallback);
        //YSDKApi.handleIntent(mainAct.getIntent());
    }

    @Override
    public void onResume(Activity mainAct) {
        //YSDKApi.onResume(mainAct);
        //YSDKApi.showDebugIcon(mainAct);
    }

    @Override
    public void onPause(Activity mainAct) {
        //YSDKApi.onPause(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        //YSDKApi.onStop(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        //YSDKApi.onDestroy(mainAct);
    }

    @Override
    public void onRestart(Activity mainAct) {
        //YSDKApi.onRestart(mainAct);
    }

    @Override
    public void onNewIntent(Activity mainAct, Intent data) {
        //YSDKApi.handleIntent(data);
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        YSDKApi.onActivityResult(requestCode, resultCode, data);
    }

    private void toCostMoney() {
        try {
            String midasPayUrl = ysdkPayJson.getString("midasPayUrl");
            RequestParams costMoneyParams = new RequestParams(midasPayUrl);
            final JSONObject junsJson = new JSONObject();
            junsJson.put("change_id", ysdkPayJson.getString("moid"));
            //YSDK QQ APP ID
            //junsJson.put("app_id", "1110509197");
            junsJson.put("money", (ysdkPayJson.getInt("costGameCoins") / ysdkPayJson.getInt("gcradio")) + "");
            junsJson.put("costGameCoins", ysdkPayJson.getInt("costGameCoins") + "");
            junsJson.put("verifyToken", ysdkPayJson.getString("verifyToken"));

            JSONObject payJson = new JSONObject();
            payJson.put("openid", ysdkPayJson.getString("openid"));
            payJson.put("pf", ysdkPayJson.getString("pf"));
            payJson.put("pay_token", ysdkPayJson.getString("paytoken"));
            payJson.put("pfkey", ysdkPayJson.getString("pfkey"));
            payJson.put("access_token", ysdkPayJson.getString("access_token"));
            payJson.put("flag", ysdkPayJson.getString("flag"));
            payJson.put("ret", 0);
            junsJson.put("yyb_param", payJson.toString());

            String junshang = JunSEncrypt.encryptDInfo("mpay.junshanggame.com", JunSConstants.ENCRYPT_IV, junsJson.toString());
            costMoneyParams.addBodyParameter("junshang", junshang);

            x.http().post(costMoneyParams, new Callback.CommonCallback<String>() {

                @Override
                public void onSuccess(String result) {
                    String ret = JunSEncrypt.decryptDInfo("mpay.junshanggame.com", JunSConstants.ENCRYPT_IV, result);
                    logger.print("costMoney --> " + ret);
                    try {
                        JSONObject resultJson = new JSONObject(ret);
                        if (resultJson.getInt("state") == 1) {
                            Bus.getDefault().post(OPayEv.getSucc("pay success."));
                        } else {
                            Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "pay fail."));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "pay fail. exception --> " + e.toString()));
                    }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "pay fail. exception --> " + ex.toString()));
                }

                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onFinished() {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * YSDK??????UserListener???????????????????????????????????????????????????????????????
     * ??????????????????????????????UI?????????????????????????????????????????????YSDK????????????
     * ?????????Java?????????(?????????Java????????????????????????Java?????????, ???????????????C++????????????????????????Java?????????)
     */
    private class YSDKCallback implements UserListener, BuglyListener, PayListener, AntiAddictListener, AntiRegisterWindowCloseListener {

        public void OnLoginNotify(UserLoginRet ret) {
           // Log.e("guoinfo",ret.toString());
            YSDK.logger.print("OnLoginNotify called");
            YSDK.logger.print(ret.getAccessToken());
            YSDK.logger.print(ret.getPayToken());
            YSDK.logger.print("ret.flag" + ret.flag);
//            YSDKApi.reportGameRoleInfo("zoneId", "zoneName", "roleId",
//                    "roleName", 9, 9, 9, null);
            switch (ret.flag) {
                case eFlag.Succ:
                    //????????????
                    UserLoginRet userLoginRet = new UserLoginRet();
                    YSDKApi.getLoginRecord(userLoginRet);
                    if (userLoginRet.ret != BaseRet.RET_SUCC) {
                        YSDK.logger.print("UserLogin error!!!");
                        YSDKApi.logout();
                        return;
                    }
                    loginToSever(userLoginRet);
                    if (ret.getLoginType() != UserLoginRet.LOGIN_TYPE_TIMER) {
                        // ???????????????????????????????????????????????????????????????????????????????????????
                        YSDKApi.setAntiAddictGameStart();
                    }
                    break;
                // ??????????????????????????????????????????????????????
                case eFlag.QQ_UserCancel:
                    YSDK.logger.print("??????????????????????????????");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "??????????????????????????????"));
                    break;

                case eFlag.QQ_LoginFail:
                    YSDK.logger.print("QQ????????????????????????");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "QQ????????????????????????"));
                    break;

                case eFlag.QQ_NetworkErr:
                    YSDK.logger.print("QQ????????????????????????");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "QQ????????????????????????"));
                    break;

                case eFlag.QQ_NotInstall:
                    YSDK.logger.print("??????????????????Q?????????????????????");
                    ViewUtils.sdkShowTips(mContext, "??????????????????Q?????????????????????");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "??????????????????Q?????????????????????"));
                    break;

                case eFlag.QQ_NotSupportApi:
                    YSDK.logger.print("?????????Q?????????????????????????????????");
                    ViewUtils.sdkShowTips(mContext, "?????????Q?????????????????????????????????");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "?????????Q?????????????????????????????????"));
                    break;

                case eFlag.WX_NotInstall:
                    YSDK.logger.print("??????????????????????????????????????????");
                    ViewUtils.sdkShowTips(mContext, "??????????????????????????????????????????");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "??????????????????????????????????????????"));
                    break;

                case eFlag.WX_NotSupportApi:
                    YSDK.logger.print("?????????????????????????????????????????????");
                    ViewUtils.sdkShowTips(mContext, "?????????????????????????????????????????????");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "?????????????????????????????????????????????"));
                    break;

                case eFlag.WX_UserCancel:
                    YSDK.logger.print("??????????????????????????????");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "??????????????????????????????"));
                    break;

                case eFlag.WX_UserDeny:
                    YSDK.logger.print("?????????????????????????????????");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "?????????????????????????????????"));
                    break;

                case eFlag.WX_LoginFail:
                    YSDK.logger.print("??????????????????????????????");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "??????????????????????????????"));
                    break;

                case eFlag.Login_TokenInvalid:
                    YSDK.logger.print("?????????????????????????????????????????????????????????");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "?????????????????????????????????????????????????????????"));
                    break;

                case eFlag.Login_NotRegisterRealName:
                    YSDK.logger.print("???????????????????????????????????????????????????????????????");
                    //Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "???????????????????????????????????????????????????????????????"));
                    //showLoginView();
                    break;

                case eFlag.Login_NeedRegisterRealName:
                    YSDK.logger.print("?????????????????????????????????????????????????????????????????????");
                    //Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "?????????????????????????????????????????????????????????????????????"));
                    //showLoginView();
                    break;

                case eFlag.Login_Free_Login_Auth_Failed:
                    YSDK.logger.print("???????????????????????????????????????");
                    YSDKApi.logout();
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "???????????????????????????????????????"));
                    break;

                case eFlag.Login_User_Logout:
                    // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
//                    YSDKDemoApi.sShowView.showToastTips("????????????????????????????????????");
                    YSDKApi.logout();
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "???????????????????????????????????????"));
                    break;

                default:
                    YSDK.logger.print("??????????????????????????????");
                    YSDKApi.logout();
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "??????????????????????????????"));
                    break;
            }
        }

        public void OnWakeupNotify(WakeupRet ret) {
            YSDK.logger.print("OnWakeupNotify called.");
            YSDK.logger.print("flag:" + ret.flag);
            YSDK.logger.print("msg:" + ret.msg);
            YSDK.logger.print("platform:" + ret.platform);
            // TODO GAME ???????????????????????????????????????????????????
            if (eFlag.Wakeup_YSDKLogining == ret.flag) {
                // ??????????????????????????????????????????OnLoginNotify()?????????
            } else {
                //????????????
                Bus.getDefault().post(new OLogoutEv());
            }
        }

        @Override
        public void OnRelationNotify(UserRelationRet relationRet) {
            final String lineBreak = "\n";
            StringBuilder builder = new StringBuilder();
            builder.append("flag:").append(relationRet.flag).append(lineBreak)
                    .append("msg:").append(relationRet.msg).append(lineBreak)
                    .append("platform:").append(relationRet.platform).append(lineBreak);

            if (relationRet.persons != null && relationRet.persons.size() > 0) {
                PersonInfo personInfo = (PersonInfo) relationRet.persons.firstElement();
                builder.append("UserInfoResponse json:").append(lineBreak)
                        .append("nick_name: ").append(personInfo.nickName).append(lineBreak)
                        .append("open_id: ").append(personInfo.openId).append(lineBreak)
                        .append("userId: ").append(personInfo.userId).append(lineBreak)
                        .append("gender: ").append(personInfo.gender).append(lineBreak)
                        .append("picture_small: ").append(personInfo.pictureSmall).append(lineBreak)
                        .append("picture_middle: ").append(personInfo.pictureMiddle).append(lineBreak)
                        .append("picture_large: ").append(personInfo.pictureLarge).append(lineBreak)
                        .append("provice: ").append(personInfo.province).append(lineBreak)
                        .append("city: ").append(personInfo.city).append(lineBreak)
                        .append("country: ").append(personInfo.country).append(lineBreak);
            } else {
                builder.append("relationRet.persons is bad");
            }

            YSDK.logger.print("OnRelationNotify" + builder.toString());
        }

        @Override
        public String OnCrashExtMessageNotify() {
            // ??????????????????crash????????????????????????
            YSDK.logger.print("OnCrashExtMessageNotify called");
            Date nowTime = new Date();
            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            return "new Upload extra crashing message for bugly on " + time.format(nowTime);
        }

        @Override
        public byte[] OnCrashExtDataNotify() {
            return null;
        }

        @Override
        public void OnPayNotify(PayRet ret) {
            YSDK.logger.print("OnPayNotify " + ret.toString());
            if (PayRet.RET_SUCC == ret.ret) {
                //??????????????????
                switch (ret.payState) {
                    //????????????
                    case PayRet.PAYSTATE_PAYSUCC:
                        YSDK.logger.print("?????????????????????????????????" + ret.realSaveNum + ";" +
                                "???????????????" + ret.payChannel + ";" +
                                "???????????????" + ret.provideState + ";" +
                                "???????????????" + ret.extendInfo + ";?????????????????????" + ret.toString());
                        //????????????
                        toCostMoney();
                        break;
                    //????????????
                    case PayRet.PAYSTATE_PAYCANCEL:
                        YSDK.logger.print("?????????????????????" + ret.toString());
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "???????????????"));
                        break;
                    //??????????????????
                    case PayRet.PAYSTATE_PAYUNKOWN:
                        YSDK.logger.print("????????????????????????????????????????????????" + ret.toString());
                        //????????????
                        toCostMoney();
                        break;
                    //????????????
                    case PayRet.PAYSTATE_PAYERROR:
                        YSDK.logger.print("????????????" + ret.toString());
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "????????????" + ret.toString()));
                        break;
                }
            } else {
                switch (ret.flag) {
                    case eFlag.Login_TokenInvalid:
                        YSDK.logger.print("????????????????????????????????????" + ret.toString());
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "????????????????????????????????????" + ret.toString()));
                        break;

                    case eFlag.Pay_User_Cancle:
                        //??????????????????
                        YSDK.logger.print("?????????????????????" + ret.toString());
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "?????????????????????" + "user cancel"));
                        break;

                    case eFlag.Pay_Param_Error:
                        YSDK.logger.print("???????????????????????????" + ret.toString());
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "???????????????????????????" + ret.toString()));
                        break;

                    case eFlag.Error:
                    default:
                        YSDK.logger.print("????????????" + ret.toString());
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "????????????" + ret.toString()));
                        break;
                }
            }
        }

        @Override
        public void onTimeLimitNotify(AntiAddictRet ret) {
            if (AntiAddictRet.RET_SUCC == ret.ret) {
                // ???????????????
                switch (ret.type) {
                    case AntiAddictRet.TYPE_TIPS:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,true);
                        YSDKApi.reportAntiAddictExecute(ret, System.currentTimeMillis());
                        break;
                    case AntiAddictRet.TYPE_LOGOUT:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,false);
                        YSDKApi.reportAntiAddictExecute(ret, System.currentTimeMillis());
                        break;
                    case AntiAddictRet.TYPE_OPEN_URL:
                        showJunsWebDialog("??????",ret.url);
                        // ???????????????
                        YSDKApi.reportAntiAddictExecute(ret, System.currentTimeMillis());
                        break;
                    default:
                        YSDK.logger.print("onTimeLimitNotify " + ret.toString());
                        break;
                }
            }
        }

        @Override
        public void onLoginLimitNotify(AntiAddictRet ret) {
            if (AntiAddictRet.RET_SUCC == ret.ret) {
                // ???????????????
                switch (ret.type) {
                    case AntiAddictRet.TYPE_TIPS:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,true);
                        YSDKApi.reportAntiAddictExecute(ret, System.currentTimeMillis());
                        break;
                    case AntiAddictRet.TYPE_LOGOUT:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,false);
                        YSDKApi.reportAntiAddictExecute(ret, System.currentTimeMillis());
                        break;
                    case AntiAddictRet.TYPE_OPEN_URL:
                        showJunsWebDialog("??????",ret.url);
                        // ???????????????
                        YSDKApi.reportAntiAddictExecute(ret, System.currentTimeMillis());
                        break;
                    default:
                        YSDK.logger.print("onTimeLimitNotify " + ret.toString());
                        break;
                }
            }
        }

        @Override
        public void onWindowClose() {
            YSDK.logger.print("?????????????????????");
            ViewUtils.sdkShowTips(mContext, "?????????????????????");
            showLoginView();
        }
    }

    private void showJunsWebDialog(String title, String url) {
        if (junSWebDialog != null) {
            if (junSWebDialog.isShowing()) {
                junSWebDialog.dismiss();
            }
            junSWebDialog = null;
        }
        junSWebDialog = new JunSWebDialog((Activity) mContext, title, url);
        junSWebDialog.showAnim(new BounceBottomEnter())
                .dismissAnim(new ZoomOutExit()).
                dimEnabled(true)
                .show();
    }
}
