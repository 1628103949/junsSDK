package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.text.TextUtils;
import android.util.Base64;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.iap.Iap;
import com.huawei.hms.iap.IapApiException;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq;
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseResult;
import com.huawei.hms.iap.entity.InAppPurchaseData;
import com.huawei.hms.iap.entity.IsEnvReadyResult;
import com.huawei.hms.iap.entity.OrderStatusCode;
import com.huawei.hms.iap.entity.OwnedPurchasesReq;
import com.huawei.hms.iap.entity.OwnedPurchasesResult;
import com.huawei.hms.iap.entity.PurchaseIntentReq;
import com.huawei.hms.iap.entity.PurchaseIntentResult;
import com.huawei.hms.iap.entity.PurchaseResultInfo;
import com.huawei.hms.jos.AntiAddictionCallback;
import com.huawei.hms.jos.AppParams;
import com.huawei.hms.jos.AppUpdateClient;
import com.huawei.hms.jos.JosApps;
import com.huawei.hms.jos.JosAppsClient;
import com.huawei.hms.jos.JosStatusCodes;
import com.huawei.hms.jos.games.Games;
import com.huawei.hms.jos.games.PlayersClient;
import com.huawei.hms.jos.games.buoy.BuoyClient;
import com.huawei.hms.jos.games.player.Player;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.api.client.Status;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.huawei.updatesdk.service.appmgr.bean.ApkUpgradeInfo;
import com.huawei.updatesdk.service.otaupdate.CheckUpdateCallBack;
import com.huawei.updatesdk.service.otaupdate.UpdateKey;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.api.JunSSdkApi;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.framework.common.ToastUtil;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.http.RequestParams;
import com.juns.sdk.framework.xutils.x;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

public class HuaWei extends OPlatformSDK {
    private static final String TAG = "HuaWei";
    private static final int LOGON_REQ_CODE = 1998;
    private static final int PAY_REQ_CODE = 1999;
    private static TNLog logger = LogFactory.getLog(TAG, true);
    BuoyClient buoyClient = null;
    PlayersClient playersClient = null;
    JosAppsClient josAppsClient = null;
    AppUpdateClient appUpdateClient;
    String hwPlayerId = "";
    String hwTransactionId = "";
    HuaweiIdAuthService huaweiIdAuthService;
    private Context mContext;
    private String hwDeliverUrl;

    private boolean isPayFlow = false;

    public HuaWei(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        mContext = activity;
        AccountAuthParams params = AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME;
        josAppsClient = JosApps.getJosAppsClient(activity);
        Task<Void> initTask;
        initTask = josAppsClient.init(new AppParams(params, new AntiAddictionCallback() {
            @Override
            public void onExit() {
                //在此处实现游戏防沉迷功能，如保存游戏、调用帐号退出接口
                Bus.getDefault().post(new OLogoutEv());
            }
        }));
        initTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                //showLog("init success");
                Bus.getDefault().post(OInitEv.getSucc());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    int statusCode = apiException.getStatusCode();
                    //错误码为7401时表示用户未同意华为联运隐私协议
                    if (statusCode == JosStatusCodes.JOS_PRIVACY_PROTOCOL_REJECTED) {
                        //showLog("has reject the protocol");
                        //在此处实现退出游戏或者重新调用初始化接口
                        ToastUtil.showLong(mContext,"需要同意隐私协议才能继续游戏！");
                        //Bus.getDefault().post(OInitEv.getFail(statusCode,"拒绝协议"));
                    }
                    init((Activity) mContext);
                    //Bus.getDefault().post(OInitEv.getFail(statusCode,e.getMessage()));
                    //在此处实现其他错误码的处理
                }
                //showLog("init failed, " + e.getMessage());
            }
        });
        //josAppsClient.init();
        buoyClient = Games.getBuoyClient(activity);
        appUpdateClient = JosApps.getAppUpdateClient(activity);
        checkUpdate();
    }

    private void checkUpdate() {
        appUpdateClient.checkAppUpdate(mContext, new CheckUpdateCallBack() {
            @Override
            public void onUpdateInfo(Intent intent) {
                if (intent != null) {
                    // 获取更新状态码， Default_value为取不到status时默认的返回码，由应用自行决定
                    int status = intent.getIntExtra(UpdateKey.STATUS, 40404);
                    // 错误码，建议打印
                    int rtnCode = intent.getIntExtra(UpdateKey.FAIL_CODE, 40405);
                    // 失败信息，建议打印
                    String rtnMessage = intent.getStringExtra(UpdateKey.FAIL_REASON);
                    Serializable info = intent.getSerializableExtra(UpdateKey.INFO);
                    //可通过获取到的info是否属于ApkUpgradeInfo类型来判断应用是否有更新
                    if (info instanceof ApkUpgradeInfo) {
                        // 这里调用showUpdateDialog接口拉起更新弹窗，由于demo有单独拉起弹窗按钮，所以不在此调用，详见checkUpdatePop()方
                        appUpdateClient.showUpdateDialog(mContext, (ApkUpgradeInfo) info, false);
                        logger.print("checkUpdatePop success");
                    }
                    logger.print("onUpdateInfo status: " + status + ", rtnCode: " + rtnCode + ", rtnMessage: " + rtnMessage);
                    //Bus.getDefault().post(OInitEv.getSucc());
                }
            }

            @Override
            public void onMarketInstallInfo(Intent intent) {

            }

            @Override
            public void onMarketStoreError(int i) {

            }

            @Override
            public void onUpdateStoreError(int i) {

            }
        });
    }

    @Override
    public void login(Activity activity) {
        isPayFlow = false;
        HuaweiIdAuthParams authParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME).createParams();
        huaweiIdAuthService = HuaweiIdAuthManager.getService(activity, authParams);
        activity.startActivityForResult(huaweiIdAuthService.getSignInIntent(), LOGON_REQ_CODE);
    }

    private void handleSignInResult(Intent data) {
        if (null == data) {
            logger.print("signIn intent is null");
            Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "huawei login fail, data is null."));
            return;
        }
        Task<AuthHuaweiId> task = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
        task.addOnSuccessListener(new OnSuccessListener<AuthHuaweiId>() {
            @Override
            public void onSuccess(AuthHuaweiId authHuaweiId) {
                playersClient = Games.getPlayersClient((Activity) mContext);
                currentPlayerInfo(authHuaweiId, authHuaweiId.getAccessToken());
                logger.print("sign in success." + authHuaweiId.getAccessToken() + "|" + authHuaweiId.getIdToken());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                logger.print("parseAuthResultFromIntent failed");
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "huawei login fail, " + e.toString()));
            }
        });
    }

    private void currentPlayerInfo(final AuthHuaweiId authHuaweiId, final String token) {
        Task<Player> playerTask = playersClient.getCurrentPlayer();
        playerTask.addOnSuccessListener(new OnSuccessListener<Player>() {
            @Override
            public void onSuccess(Player player) {
                //请求成功，获取玩家信息
                hwPlayerId = player.getPlayerId();
                buoyClient.showFloatWindow();
                logger.print("getPlayerInfo Success, player info: " + player.toString());
                login2RSService(token, player.getSignTs(), player.getPlayerId(), player.getLevel() + "", player.getPlayerSign());
                //登录成功，补发货
                patchDeliver();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // 获取玩家信息失败
                if (e instanceof ApiException) {
                    logger.print("getPlayerInfo failed, status: " + ((ApiException) e).getStatusCode());
                    Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.CHANNEL_ERR, "huawei login fail, " + e.toString()));
                }
            }
        });
    }

    private void login2RSService(String token, String ts, String playerId, String playLevel, String playerSSign) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("ts", ts);
            dataJson.put("playerId", playerId);
            dataJson.put("playerLevel", playLevel);
            dataJson.put("playerSSignEncode", Base64.encodeToString(playerSSign.getBytes(), Base64.DEFAULT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer("play", dataJson.toString());
    }

    @Override
    public void logout(Activity mainAct) {
        try {
            if (huaweiIdAuthService != null) {
                huaweiIdAuthService.signOut();
                //隐藏悬浮球
                if (buoyClient != null) {
                    buoyClient.hideFloatWindow();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pay(Activity activity, final HashMap<String, String> payParams) {
        isPayFlow = true;
        Task<IsEnvReadyResult> task = Iap.getIapClient(activity).isEnvReady();
        task.addOnSuccessListener(new OnSuccessListener<IsEnvReadyResult>() {
            @Override
            public void onSuccess(IsEnvReadyResult result) {
                // Obtain the execution result.
                logger.print("支持信息：支持");
                toHWPay(payParams);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                logger.print("支持信息：不支持");
                if (e instanceof IapApiException) {
                    logger.print("huawei pay not support --> " + e.toString());
//                    IapApiException apiException = (IapApiException) e;
//                    Status status = apiException.getStatus();
//                    if (status.getStatusCode() == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
//                        // Not logged in.
//                        if (status.hasResolution()) {
//                            try {
//                                // 6666 is an int constant defined by the developer.
//                                status.startResolutionForResult((Activity) context, 6666);
//                            } catch (IntentSender.SendIntentException exp) {
//                            }
//                        }
//                    } else if (status.getStatusCode() == OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED) {
//                        // The current region does not support HUAWEI IAP.
//                    }
                }
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "not support huawei pay."));
            }
        });
    }

    private void toHWPay(HashMap<String, String> payInfo) {
        // Constructs a PurchaseIntentReq object.
        JSONObject mDataJson = null;
        PurchaseIntentReq purchaseIntentReq = new PurchaseIntentReq();
        try {
            mDataJson = new JSONObject(payInfo.get(JunSConstants.PAY_M_DATA));
            //华为后台商品ID
            purchaseIntentReq.setProductId(mDataJson.getString("productId"));
            //商品类型。0: 消耗型商品1: 非消耗型商品2: 自动续费订阅商品
            purchaseIntentReq.setPriceType(mDataJson.getInt("priceType"));
            // 商户侧保留信息。若该字段有值，在支付成功后的回调结果中会原样返回给应用。
            purchaseIntentReq.setDeveloperPayload(mDataJson.getString("extReserved"));
            hwDeliverUrl = mDataJson.getString("notifyurl");
        } catch (JSONException e) {
            e.printStackTrace();
            logger.print("pay exception --> " + e.toString());
            Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "pay exception."));
        }

        // The product ID is the same as that set by a developer when configuring product information in AppGallery Connect.
        // to call the createPurchaseIntent API.
        // To get the Activity instance that calls this API.
        //  final Activity activity = (Activity) mContext;
        Task<PurchaseIntentResult> task = Iap.getIapClient(mContext).createPurchaseIntent(purchaseIntentReq);
        task.addOnSuccessListener(new OnSuccessListener<PurchaseIntentResult>() {
            @Override
            public void onSuccess(PurchaseIntentResult result) {
                // Obtain the payment result.
                Status status = result.getStatus();
                logger.print("pay status --> " + status.hasResolution() + "");
                if (status.hasResolution()) {
                    try {
                        logger.print("pay status --> 拉起收银台");
                        status.startResolutionForResult((Activity) mContext, PAY_REQ_CODE);
                    } catch (IntentSender.SendIntentException exp) {
                        logger.print("pay status fail --> " + exp.toString());
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "pay exception. " + exp.toString()));
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                    logger.print("pay status fail --> " + returnCode);
                } else {
                    // Other external errors
                    logger.print("pay status fail --> other");
                }
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "pay fail."));
            }
        });
    }

    private void handlePayResult(Intent data) {
        if (data == null) {
            logger.print("handlePayResult data is null.");
            Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "result data is null."));
            return;
        }
        PurchaseResultInfo purchaseResultInfo = Iap.getIapClient(mContext).parsePurchaseResultInfoFromIntent(data);
        switch (purchaseResultInfo.getReturnCode()) {
            case OrderStatusCode.ORDER_STATE_CANCEL:
                // User cancel payment.
                logger.print("user cancel pay.");
                Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL, "user cancel."));
                break;

            case OrderStatusCode.ORDER_STATE_FAILED:
                logger.print("product fail");
                //补发货流程
                patchDeliver();
                break;

            case OrderStatusCode.ORDER_PRODUCT_OWNED:
                // to check if there exists undelivered products.
                logger.print("product owned");
                //补发货流程
                patchDeliver();
                break;

            case OrderStatusCode.ORDER_STATE_SUCCESS:
                // pay success.
                logger.print("pay success.");
                String inAppPurchaseData = purchaseResultInfo.getInAppPurchaseData();
                String inAppPurchaseDataSignature = purchaseResultInfo.getInAppDataSignature();
                //向服务端请求发货
                severDeliverGoods(inAppPurchaseData, inAppPurchaseDataSignature);
                //  Bus.getDefault().post(OPayEv.getSucc("pay success."));
                // use the public key of your app to verify the signature.
                // If ok, you can deliver your products.
                // If the user purchased a consumable product, call the consumeOwnedPurchase API to consume it after successfully delivering the product.
                break;

            default:
                break;
        }
    }

    @Override
    public void exitGame(Activity activity) {
////        Task<String> task = playersClient.submitPlayerEvent(hwPlayerId, hwTransactionId, "GAMEEND");
////        task.addOnSuccessListener(new OnSuccessListener<String>() {
////            @Override
////            public void onSuccess(String s) {
////                logger.print("submitPlayerEvent traceId: " + s);
////            }
////        }).addOnFailureListener(new OnFailureListener() {
////            @Override
////            public void onFailure(Exception e) {
////                if (e instanceof ApiException) {
////                    String result = "rtnCode:" + ((ApiException) e).getStatusCode();
////                    logger.print(result);
////                }
////            }
////        });

        HWExitDialog.showExit(activity, new HWExitDialog.ExitCallback() {
            @Override
            public void toContinue() {
                Bus.getDefault().post(OExitEv.getFail(JunSConstants.Status.CHANNEL_ERR, "user cancel."));
            }

            @Override
            public void toExit() {
                Bus.getDefault().post(OExitEv.getSucc());
            }
        });

    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
//        if (submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)) {
//            //角色进入游戏
//            String uid = UUID.randomUUID().toString();
//            Task<String> task = playersClient.submitPlayerEvent(hwPlayerId, uid, "GAMEBEGIN");
//            task.addOnSuccessListener(new OnSuccessListener<String>() {
//                @Override
//                public void onSuccess(String jsonRequest) {
//                    try {
//                        JSONObject data = new JSONObject(jsonRequest);
//                        hwTransactionId = data.getString("transactionId");
//                    } catch (JSONException e) {
//                        logger.print("parse jsonArray meet json exception");
//                        return;
//                    }
//                    logger.print("submitPlayerEvent traceId: " + jsonRequest);
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(Exception e) {
//                    if (e instanceof ApiException) {
//                        String result = "rtnCode:" + ((ApiException) e).getStatusCode();
//                        logger.print(result);
//                    }
//                }
//            });
//        }
    }

    @Override
    public void onActivityResult(Activity mainAct, int requestCode, int resultCode, Intent data) {
        if (requestCode == PAY_REQ_CODE) {
            handlePayResult(data);
        } else if (requestCode == LOGON_REQ_CODE) {
            handleSignInResult(data);
        }
    }

    @Override
    public void onResume(Activity mainAct) {
        if (buoyClient != null) {
            buoyClient.showFloatWindow();
        }
    }

    @Override
    public void onPause(Activity mainAct) {
        if (buoyClient != null) {
            buoyClient.hideFloatWindow();
        }
    }

    private void severDeliverGoods(final String hwPurchaseData, String hwPurchaseSignature) {
        if (TextUtils.isEmpty(hwDeliverUrl)) {
            try {
                String sdkConfig = JunSSdkApi.getInstance().sdkGetConfig((Activity) mContext);
                JSONObject configJson = new JSONObject(sdkConfig);
                String pid = configJson.getString("pid");
                String gameId = configJson.getString("game_id");
                hwDeliverUrl = "http://mpay.junshanggame.com/pay/ptpay/" + pid + "/" + gameId;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        RequestParams deliverParams = new RequestParams(hwDeliverUrl);
        deliverParams.addBodyParameter("hwPurchaseData", hwPurchaseData);
        deliverParams.addBodyParameter("hwPurchaseDataSignature", hwPurchaseSignature);
        x.http().post(deliverParams, new Callback.CommonCallback<String>() {

            @Override
            public void onSuccess(String result) {
                logger.print("deliver result --> " + result);
                try {
                    JSONObject resultJson = new JSONObject(result);
                    if (resultJson.getInt("state") == 1) {
                        //支付成功，消耗商品
                        JSONObject dataJson = resultJson.getJSONObject("data");
                        String hwPurchaseData = dataJson.getString("hwPurchaseData");
                        hwConsumeProduct(hwPurchaseData);
                        if (isPayFlow) {
                            Bus.getDefault().post(OPayEv.getSucc("huawei pay success."));
                            isPayFlow = false;
                        }
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //支付异常，同样也消耗商品
                //有可能同个订单请求多次发货
                hwConsumeProduct(hwPurchaseData);
                if (isPayFlow) {
                    Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "pay exception."));
                    isPayFlow = false;
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                //因网络原因失败，不进行消耗，并做提示，准备触发补发货流程
                ViewUtils.sdkShowTips(mContext, "尊敬的玩家，网络发生抖动，如未到账，请尝试重启游戏！");
                if (isPayFlow) {
                    Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.CHANNEL_ERR, "支付异常"));
                    isPayFlow = false;
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

    private void hwConsumeProduct(String hwPurchaseData) {
        // Constructs a ConsumeOwnedPurchaseReq object.
        String purchaseToken = "";
        try {
            InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(hwPurchaseData);
            purchaseToken = inAppPurchaseDataBean.getPurchaseToken();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ConsumeOwnedPurchaseReq consumeOwnedPurchaseReq = new ConsumeOwnedPurchaseReq();
        consumeOwnedPurchaseReq.setPurchaseToken(purchaseToken);
        // to call the consumeOwnedPurchase API.
        // To get the Activity instance that calls this API.
        Task<ConsumeOwnedPurchaseResult> task = Iap.getIapClient(mContext).consumeOwnedPurchase(consumeOwnedPurchaseReq);
        task.addOnSuccessListener(new OnSuccessListener<ConsumeOwnedPurchaseResult>() {
            @Override
            public void onSuccess(ConsumeOwnedPurchaseResult result) {
                // Obtain the result
                //补发货
                patchDeliver();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                //此错误无需处理
//                if (e instanceof IapApiException) {
//                    IapApiException apiException = (IapApiException) e;
//                    Status status = apiException.getStatus();
//                    int returnCode = apiException.getStatusCode();
//                } else {
//                    // Other external errors
//                }
            }
        });
    }

    private void patchDeliver() {
        //先查询已经拥有的商品
        // Constructs a OwnedPurchasesReq object.
        OwnedPurchasesReq ownedPurchasesReq = new OwnedPurchasesReq();
        // In-app product type contains:
        // priceType: 0: consumable; 1: non-consumable; 2: auto-renewable subscription
        ownedPurchasesReq.setPriceType(0);
        // to call the obtainOwnedPurchases API
        // To get the Activity instance that calls this API.
        Task<OwnedPurchasesResult> task = Iap.getIapClient(mContext).obtainOwnedPurchases(ownedPurchasesReq);
        task.addOnSuccessListener(new OnSuccessListener<OwnedPurchasesResult>() {
            @Override
            public void onSuccess(OwnedPurchasesResult result) {
                // Obtain the execution result.
                try {
                    if (result != null && result.getInAppPurchaseDataList() != null) {
                        for (int i = 0; i < result.getInAppPurchaseDataList().size(); i++) {
                            String inAppPurchaseData = result.getInAppPurchaseDataList().get(i);
                            String InAppSignature = result.getInAppSignature().get(i);
                            // use the payment public key to verify the signature of the inAppPurchaseData.
                            // if success.
                            try {
                                InAppPurchaseData inAppPurchaseDataBean = new InAppPurchaseData(inAppPurchaseData);
                                int purchaseState = inAppPurchaseDataBean.getPurchaseState();
                                severDeliverGoods(inAppPurchaseData, InAppSignature);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof IapApiException) {
                    IapApiException apiException = (IapApiException) e;
                    Status status = apiException.getStatus();
                    int returnCode = apiException.getStatusCode();
                }
                //失败无需处理
            }
        });
    }

}
