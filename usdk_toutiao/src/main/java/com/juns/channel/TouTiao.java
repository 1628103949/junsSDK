package com.juns.channel;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.bytedance.applog.AppLog;
import com.bytedance.applog.GameReportHelper;
import com.bytedance.ugame.rocketapi.IRocketCnApi;
import com.bytedance.ugame.rocketapi.RocketCn;
import com.bytedance.ugame.rocketapi.account.IAccountCallback;
import com.bytedance.ugame.rocketapi.account.UserInfo;
import com.bytedance.ugame.rocketapi.account.UserInfoResult;
import com.bytedance.ugame.rocketapi.callback.InitCallback;
import com.bytedance.ugame.rocketapi.callback.InitResult;
import com.bytedance.ugame.rocketapi.callback.LogoutCallback;
import com.bytedance.ugame.rocketapi.pay.IPayCallback;
import com.bytedance.ugame.rocketapi.pay.RocketPayResult;
import com.bytedance.ugame.rocketapi.pay.UnionPayInfo;
import com.bytedance.ugame.rocketapi.privacy.IPermissionReqListener;
import com.bytedance.ugame.rocketapi.privacy.IPrivacyListener;
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
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class TouTiao extends OPlatformSDK {
    private static final String TAG = "TouTiao";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public TouTiao(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void preInit(final Activity mainAct) {
        super.preInit(mainAct);
        SDKData.setInitEnable(false);
        RocketCn.getInstance().showPrivacyDialog(mainAct, new IPrivacyListener() {
            @Override
            public void onPrivacyResult(boolean isAgreed) {
                if (!isAgreed) {
                    Toast.makeText(mainAct, "需要接受隐私政策才能继续游戏", Toast.LENGTH_SHORT).show();
                    //finish();
                    Bus.getDefault().post(OExitEv.getSucc());
                    return;
                }
                RocketCn.getInstance().requestPermission(mainAct,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE}, new IPermissionReqListener() {
                            @Override
                            public void onPermissionRequest(boolean isAllGranted, @NonNull String[] permissions, int[] grantResults) {
                                if (isAllGranted) {
                                    SDKData.setInitEnable(true);
                                    RocketCn.getInstance().init(mainAct.getApplication(), new InitCallback() {
                                        @Override
                                        public void onSuccess(InitResult initResult) {
                                            // 在初始化成功之后，若接入了巨量引擎，需要手动调用深度转化sdk激活方法
                                            Bus.getDefault().post(OInitEv.getSucc());
                                            AppLog.manualActivate();
                                        }

                                        @Override
                                        public void onFailed(String errorMessage) {
                                            Bus.getDefault().post(OInitEv.getFail(2,errorMessage));
                                        }
                                    });
                                }  else {
                                    Toast.makeText(mainAct, "权限被拒绝，无法初始化", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });
            }
        });
    }


    @Override
    public void init(Activity activity) {
        RocketCn.getInstance().setLogoutCallback(new LogoutCallback() {
            @Override
            public void onLogout() {
                Bus.getDefault().post(new OLogoutEv());
            }
        });
    }

    @Override
    public void login(final Activity activity) {
        RocketCn.getInstance().login(activity, new IAccountCallback<UserInfoResult>() {
            @Override
            public void onSuccess(@Nullable UserInfoResult userInfoResult) {
                RocketCn.getInstance().showFloatingView(activity);
                UserInfo userInfo = userInfoResult.data;
                login2RSService(userInfo.getToken());
            }
            @Override
            public void onFailed(@Nullable UserInfoResult userInfoResult) {
                Bus.getDefault().post(OLoginEv.getFail(userInfoResult.code,userInfoResult.message));
            }
        });
    }
    private void login2RSService(String token) {
        JSONObject dataJson = new JSONObject();
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }

    @Override
    public void logout(Activity mainAct) {
        RocketCn.getInstance().logout(mainAct, new IAccountCallback<UserInfoResult>() {
            @Override
            public void onSuccess(@Nullable UserInfoResult result) {
                Bus.getDefault().post(new OLogoutEv());
            }

            @Override
            public void onFailed(@Nullable UserInfoResult exception) {

            }
        });
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        logger.print("1111"+payParams.toString()+SDKData.getpUserId());
        try {
            JSONObject datajson = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            UnionPayInfo unionPayInfo = new UnionPayInfo();
            unionPayInfo.amount = (int)Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY))*100;
            unionPayInfo.callbackUrl = datajson.getString("callbackurl");
            unionPayInfo.cpOrderId = payParams.get(JunSConstants.PAY_M_ORDER_ID);
            unionPayInfo.extraInfo = datajson.getString("extraInfo");
            unionPayInfo.productDesc = datajson.getString("productDesc");
            unionPayInfo.productId = datajson.getString("productId");
            unionPayInfo.productName = payParams.get(JunSConstants.PAY_ORDER_NAME);
            unionPayInfo.sdkOpenId = SDKData.getpUserId();
            RocketCn.getInstance().unionPay(activity, unionPayInfo, new IPayCallback<RocketPayResult>() {
                @Override
                public void onSuccess(@Nullable RocketPayResult result) {
                    if (result != null) {
//                    tvResult.append(result.toString());
//                    tvResult.append("\n");
                        Bus.getDefault().post(OPayEv.getSucc(result.getSdkMessage()));
                    }

                }

                @Override
                public void onFailed(@Nullable RocketPayResult exception) {
                    if (exception != null) {
//                    tvResult.append(exception.toString());
//                    tvResult.append("\n");
                        Bus.getDefault().post(OPayEv.getSucc(exception.getSdkMessage()));
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_UPGRADE)){
            GameReportHelper.onEventUpdateLevel(Integer.parseInt(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL))); // 升级
        }
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            GameReportHelper.onEventCreateGameRole(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));// 创建角色
        }

    }

    @Override
    public void exitGame(Activity activity) {
        TouTiaoExitDialog.showExit(activity, new TouTiaoExitDialog.ExitCallback() {
            @Override
            public void toContinue() {

            }

            @Override
            public void toExit() {
                Bus.getDefault().post(OExitEv.getSucc());
            }
        });
    }

    @Override
    public void sdkOnRequestPermissionsResult(Activity mainAct, int requestCode, String[] permissions, int[] grantResults) {
        super.sdkOnRequestPermissionsResult(mainAct, requestCode, permissions, grantResults);
        RocketCn.getInstance().getComponent(IRocketCnApi.class)
                .dispatchPermissionResult(mainAct, requestCode, permissions, grantResults);
    }
}
