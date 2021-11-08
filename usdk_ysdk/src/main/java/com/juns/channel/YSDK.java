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
import com.juns.sdk.framework.view.dialog.FadeEnter.FadeEnter;
import com.juns.sdk.framework.view.dialog.FadeExit.FadeExit;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.http.RequestParams;
import com.juns.sdk.framework.xutils.x;
import com.tencent.ysdk.api.YSDKApi;
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
    // 防沉迷指令执行状态
    public static boolean mAntiAddictExecuteState = false;
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private Context mContext;
    private JSONObject ysdkPayJson;
    private YSDKLoginDialog loginDialog;
    private YSDKCallback mYSDKCallback = new YSDKCallback();
    private YSDKLoginDialog.LoginViewCallback loginViewCallback = new YSDKLoginDialog.LoginViewCallback() {
        @Override
        public void onQQ() {
            YSDKApi.logout();
            YSDKApi.login(ePlatform.QQ);
        }

        @Override
        public void onWX() {
            YSDKApi.logout();
            YSDKApi.login(ePlatform.WX);
        }
    };

    public YSDK(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        mContext = activity;
        Bus.getDefault().post(OInitEv.getSucc());
    }

    @Override
    public void login(Activity activity) {
        ysdkLogin(activity);
    }

    @Override
    public void logout(Activity mainAct) {
        YSDKApi.logout();
        dismissLoginView();
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        logger.print("ysdkpayinfo"+payParams.toString());
        doPay(activity, payParams.get(JunSConstants.PAY_M_ORDER_ID), payParams.get(JunSConstants.PAY_M_DATA));
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
        if(userLoginRet.flag!=0){
            showLoginView();
        }else{
            ysdkAutoLoginDialog = new YSDKAutoLoginDialog(activity, new YSDKAutoLoginDialog.AutoCallback() {
                @Override
                public void onSwitchAccount() {
                    showLoginView();
                }
            });
            ysdkAutoLoginDialog.showAnim(new FadeEnter()).dismissAnim(new FadeExit()).dimEnabled(true).show();
            TimerTask task = new TimerTask(){
                public void run(){
                    //execute the task
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ysdkAutoLoginDialog.dismiss();
                            loginToSever(userLoginRet);
                        }
                    });

                }
            };
            Timer timer = new Timer();
            timer.schedule(task, 2000);

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
            //1 --> QQ登录，2--> 微信
            if (userLoginRet.platform == ePlatform.PLATFORM_ID_QQ) {
                jsonData.put("pflag", "QQ");
            } else if (userLoginRet.platform == ePlatform.PLATFORM_ID_WX) {
                jsonData.put("pflag", "weixin");
            }
            jsonData.put("puid", userLoginRet.open_id);

            //ysdk 支付相关数据
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

    private void doPay(Context context, String moid, String mData) {
        try {
            JSONObject dataJson = new JSONObject(mData);
            int gcradio = dataJson.getInt("gcradio");   //兑换比例
            String verifyToken = dataJson.getString("verifyToken");
            int balance = dataJson.getInt("balance");   //余额
            int save_amt = dataJson.getInt("save_amt"); //累计充值金额
            int costGameCoins = dataJson.getInt("costGameCoins");   //消耗的游戏币
            String midasPayUrl = dataJson.getString("midasPayUrl"); //扣款地址
            String zoneid = dataJson.getString("zoneid");   //大区ID
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
                //余额充足，直接发起扣款
                StringBuilder payDialogContent = new StringBuilder();
                payDialogContent.setLength(0);
                payDialogContent.append("充值金额：" + costGameCoins / gcradio + "\n");
                showTencentPayDialog(false, payDialogContent, null, null);
            } else {
                //余额不足
                if (balance > 0) {
                    //余额大于0
                    StringBuilder payDialogContent = new StringBuilder();
                    int money_second = (costGameCoins - balance) / gcradio;
                    payDialogContent.append("您的账户余额不足，还需要充值:" + money_second + "元" + "\n");
                    payDialogContent.append("您是否充值?");
                    showTencentPayDialog(true, payDialogContent, zoneid, money_second * gcradio + "");
                } else {
                    //直接发起支付
                    launchTCPay(zoneid, costGameCoins + "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 腾讯确认充值/扣款提示框
     *
     * @param isCallTencentPay 是否调起腾讯充值，true：发起充值。false
     * @param dialogMsg        对话框内容
     */
    private void showTencentPayDialog(final boolean isCallTencentPay, StringBuilder dialogMsg, final String sid, final String money) {

        AlertDialog.Builder payBuilder = null;
        payBuilder = new AlertDialog.Builder(mContext);
        payBuilder.setTitle("支付提示");
        payBuilder.setMessage(dialogMsg.toString());
        payBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (isCallTencentPay) {
                        launchTCPay(sid, money);
                    } else {
                        // 余额足
                        toCostMoney();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        payBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 点击取消则返回取消支付回调
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "user cancel."));
            }
        });

        payBuilder.setCancelable(false);
        Dialog payDialog = payBuilder.create();
        payDialog.show();

    }

    private void launchTCPay(String sid, String money) {
        // 商品资源
        int appresID = ResUtil.getDrawableID("fy_product", mContext);
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), appresID);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appResData = baos.toByteArray();
        // 是否可更改
        boolean isCanChange = false;
        // 拓展参数
        String ysdkExt = "ysdkExt";
        // 充值回调在YSDKCallback中
        YSDKApi.recharge(sid, money, isCanChange, appResData, ysdkExt, new YSDKCallback());
    }

    @Override
    public void onCreate(Activity mainAct) {
        //YSDKApi.onCreate(mainAct);
        YSDKApi.init(true);
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

    public void executeInstruction(AntiAddictRet ret) {
        final int modal = ret.modal;
        switch (ret.type) {
            case AntiAddictRet.TYPE_TIPS:
            case AntiAddictRet.TYPE_LOGOUT:
                if (!mAntiAddictExecuteState) {
                    mAntiAddictExecuteState = true;
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(ret.title);
                    builder.setMessage(ret.content);
                    builder.setPositiveButton("知道了",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    if (modal == 1) {
                                        // 强制用户下线
                                        YSDKApi.logout();
                                        //回到游戏登录界面
                                        Bus.getDefault().post(new OLogoutEv());
                                    }
                                    changeExecuteState(false);
                                }
                            });
                    builder.setCancelable(false);
                    builder.show();
                    // 已执行指令
                    YSDKApi.reportAntiAddictExecute(ret, System.currentTimeMillis());
                }

                break;

            case AntiAddictRet.TYPE_OPEN_URL:
                if (!mAntiAddictExecuteState) {
                    mAntiAddictExecuteState = true;
                    View popwindowView = View.inflate(mContext, ResUtil.getLayoutID("pop_window_web_layout", mContext), null);
                    WebView webView = popwindowView.findViewById(ResUtil.getID("pop_window_webview", mContext));
                    Button closeButton = popwindowView.findViewById(ResUtil.getID("pop_window_close", mContext));

                    WebSettings settings = webView.getSettings();
                    settings.setJavaScriptEnabled(true);
                    webView.setWebViewClient(new WebViewClient());
                    webView.loadUrl(ret.url);

                    final PopupWindow popupWindow = new PopupWindow(popwindowView, 1000, 1000);
                    popupWindow.setTouchable(true);
                    popupWindow.setOutsideTouchable(false);
                    popupWindow.setBackgroundDrawable(new BitmapDrawable());

                    closeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (modal == 1) {
                                YSDKApi.logout();
                                //回到游戏登录界面
                                Bus.getDefault().post(new OLogoutEv());
                            }
                            popupWindow.dismiss();
                            changeExecuteState(false);
                        }
                    });

                    popupWindow.showAtLocation(popwindowView, Gravity.CENTER, 0, 0);
                    // 已执行指令
                    YSDKApi.reportAntiAddictExecute(ret, System.currentTimeMillis());
                }
                break;

        }
    }

    private void changeExecuteState(boolean state) {
        mAntiAddictExecuteState = state;
    }

    /**
     * YSDK通过UserListener抽象类中的方法将授权或查询结果回调给游戏。
     * 游戏根据回调结果调整UI等。只有设置回调，游戏才能收到YSDK的响应。
     * 这里是Java层回调(设置了Java层回调会优先调用Java层回调, 如果要使用C++层回调则不能设置Java层回调)
     */
    private class YSDKCallback implements UserListener, BuglyListener, PayListener, AntiAddictListener, AntiRegisterWindowCloseListener {

        public void OnLoginNotify(UserLoginRet ret) {
           // Log.e("guoinfo",ret.toString());
            YSDK.logger.print("OnLoginNotify called");
            YSDK.logger.print(ret.getAccessToken());
            YSDK.logger.print(ret.getPayToken());
            YSDK.logger.print("ret.flag" + ret.flag);
            switch (ret.flag) {
                case eFlag.Succ:
                    Log.e("guoinfo","success");
                    if (ret.getLoginType() != UserLoginRet.LOGIN_TYPE_TIMER) {
                        // 定时登录，不需要设置防沉迷统计开始，自动登录情况，不需上报
                        YSDKApi.setAntiAddictGameStart();
                        //开始登录
                        UserLoginRet userLoginRet = new UserLoginRet();
                        YSDKApi.getLoginRecord(userLoginRet);
                        loginToSever(userLoginRet);
                    }
                    break;
                // 游戏逻辑，对登录失败情况分别进行处理
                case eFlag.QQ_UserCancel:
                    YSDK.logger.print("用户取消授权，请重试");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "用户取消授权，请重试"));
                    break;

                case eFlag.QQ_LoginFail:
                    YSDK.logger.print("QQ登录失败，请重试");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "QQ登录失败，请重试"));
                    break;

                case eFlag.QQ_NetworkErr:
                    YSDK.logger.print("QQ登录异常，请重试");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "QQ登录异常，请重试"));
                    break;

                case eFlag.QQ_NotInstall:
                    YSDK.logger.print("手机未安装手Q，请安装后重试");
                    ViewUtils.sdkShowTips(mContext, "手机未安装手Q，请安装后重试");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "手机未安装手Q，请安装后重试"));
                    break;

                case eFlag.QQ_NotSupportApi:
                    YSDK.logger.print("手机手Q版本太低，请升级后重试");
                    ViewUtils.sdkShowTips(mContext, "手机手Q版本太低，请升级后重试");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "手机手Q版本太低，请升级后重试"));
                    break;

                case eFlag.WX_NotInstall:
                    YSDK.logger.print("手机未安装微信，请安装后重试");
                    ViewUtils.sdkShowTips(mContext, "手机未安装微信，请安装后重试");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "手机未安装微信，请安装后重试"));
                    break;

                case eFlag.WX_NotSupportApi:
                    YSDK.logger.print("手机微信版本太低，请升级后重试");
                    ViewUtils.sdkShowTips(mContext, "手机微信版本太低，请升级后重试");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "手机微信版本太低，请升级后重试"));
                    break;

                case eFlag.WX_UserCancel:
                    YSDK.logger.print("用户取消授权，请重试");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "用户取消授权，请重试"));
                    break;

                case eFlag.WX_UserDeny:
                    YSDK.logger.print("用户拒绝了授权，请重试");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "用户拒绝了授权，请重试"));
                    break;

                case eFlag.WX_LoginFail:
                    YSDK.logger.print("微信登录失败，请重试");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "微信登录失败，请重试"));
                    break;

                case eFlag.Login_TokenInvalid:
                    YSDK.logger.print("您尚未登录或者之前的登录已过期，请重试");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "您尚未登录或者之前的登录已过期，请重试"));
                    break;

                case eFlag.Login_NotRegisterRealName:
                    YSDK.logger.print("您的账号没有进行实名认证，请实名认证后重试");
                    //Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "您的账号没有进行实名认证，请实名认证后重试"));
                    break;

                case eFlag.Login_NeedRegisterRealName:
                    YSDK.logger.print("您的账号没有进行实名认证，请完成实名认证后重试");
                    //Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "您的账号没有进行实名认证，请完成实名认证后重试"));
                    break;

                case eFlag.Login_Free_Login_Auth_Failed:
                    YSDK.logger.print("免登录校验失败，请重启重试");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "免登录校验失败，请重启重试"));
                    break;

                default:
                    YSDK.logger.print("登录失败，请重启重试");
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "登录失败，请重启重试"));
                    break;
            }
        }

        public void OnWakeupNotify(WakeupRet ret) {
            YSDK.logger.print("OnWakeupNotify called.");
            YSDK.logger.print("flag:" + ret.flag);
            YSDK.logger.print("msg:" + ret.msg);
            YSDK.logger.print("platform:" + ret.platform);
            // TODO GAME 游戏需要在这里增加处理异账号的逻辑
            if (eFlag.Wakeup_YSDKLogining == ret.flag) {
                // 用拉起的账号登录，登录结果在OnLoginNotify()中回调
            } else {
                //重新登录
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
            // 此处游戏补充crash时上报的额外信息
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
                //支付流程成功
                switch (ret.payState) {
                    //支付成功
                    case PayRet.PAYSTATE_PAYSUCC:
                        YSDK.logger.print("用户支付成功，支付金额" + ret.realSaveNum + ";" +
                                "使用渠道：" + ret.payChannel + ";" +
                                "发货状态：" + ret.provideState + ";" +
                                "业务类型：" + ret.extendInfo + ";建议查询余额：" + ret.toString());
                        //发起扣款
                        toCostMoney();
                        break;
                    //取消支付
                    case PayRet.PAYSTATE_PAYCANCEL:
                        YSDK.logger.print("用户取消支付：" + ret.toString());
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "取消支付。"));
                        break;
                    //支付结果未知
                    case PayRet.PAYSTATE_PAYUNKOWN:
                        YSDK.logger.print("用户支付结果未知，建议查询余额：" + ret.toString());
                        //发起扣款
                        toCostMoney();
                        break;
                    //支付失败
                    case PayRet.PAYSTATE_PAYERROR:
                        YSDK.logger.print("支付异常" + ret.toString());
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "支付异常" + ret.toString()));
                        break;
                }
            } else {
                switch (ret.flag) {
                    case eFlag.Login_TokenInvalid:
                        YSDK.logger.print("登录态过期，请重新登录：" + ret.toString());
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "登录态过期，请重新登录：" + ret.toString()));
                        break;

                    case eFlag.Pay_User_Cancle:
                        //用户取消支付
                        YSDK.logger.print("用户取消支付：" + ret.toString());
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "用户取消支付：" + ret.toString()));
                        break;

                    case eFlag.Pay_Param_Error:
                        YSDK.logger.print("支付失败，参数错误" + ret.toString());
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "支付失败，参数错误" + ret.toString()));
                        break;

                    case eFlag.Error:
                    default:
                        YSDK.logger.print("支付异常" + ret.toString());
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "支付异常" + ret.toString()));
                        break;
                }
            }
        }

        @Override
        public void onTimeLimitNotify(AntiAddictRet ret) {
            if (AntiAddictRet.RET_SUCC == ret.ret) {
                // 防沉迷指令
                switch (ret.ruleFamily) {
                    case AntiAddictRet.RULE_WORK_TIP:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,true);
                        break;
                    case AntiAddictRet.RULE_WORK_NO_PLAY:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,false);
                        break;
                    case AntiAddictRet.RULE_HOLIDAY_TIP:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,true);
                        break;
                    case AntiAddictRet.RULE_HOLIDAY_NO_PLAY:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,false);
                        break;
                    case AntiAddictRet.RULE_GUEST:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,false);
                        break;
                    case AntiAddictRet.RULE_NIGHT_NO_PLAY:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,false);
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
                // 防沉迷指令
                switch (ret.ruleFamily) {
                    case AntiAddictRet.RULE_WORK_TIP:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,true);
                        break;
                    case AntiAddictRet.RULE_WORK_NO_PLAY:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,false);
                        break;
                    case AntiAddictRet.RULE_HOLIDAY_TIP:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,true);
                        break;
                    case AntiAddictRet.RULE_HOLIDAY_NO_PLAY:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,false);
                        break;
                    case AntiAddictRet.RULE_GUEST:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,false);
                        break;
                    case AntiAddictRet.RULE_NIGHT_NO_PLAY:
                        YSDKNotiDialog.showNoti((Activity) mContext,ret.content,false);
                        break;
                    default:
                        YSDK.logger.print("onLoginLimitNotify " + ret.toString());
                        break;
                }

            }
        }

        @Override
        public void onWindowClose() {
            YSDK.logger.print("请重新登录游戏");
            ViewUtils.sdkShowTips(mContext, "请重新登录游戏");
            showLoginView();
        }
    }

}
