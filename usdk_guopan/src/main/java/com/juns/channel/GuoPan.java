package com.juns.channel;

import android.app.Activity;

import com.flamingo.sdk.access.Callback;
import com.flamingo.sdk.access.GPApiFactory;
import com.flamingo.sdk.access.GPExitResult;
import com.flamingo.sdk.access.GPPayResult;
import com.flamingo.sdk.access.GPSDKGamePayment;
import com.flamingo.sdk.access.GPSDKInitResult;
import com.flamingo.sdk.access.GPSDKPlayerInfo;
import com.flamingo.sdk.access.GPUploadPlayerInfoResult;
import com.flamingo.sdk.access.GPUserResult;
import com.flamingo.sdk.access.IGPApi;
import com.flamingo.sdk.access.IGPExitObsv;
import com.flamingo.sdk.access.IGPPayObsv;
import com.flamingo.sdk.access.IGPSDKInitObsv;
import com.flamingo.sdk.access.IGPSDKInnerEventObserver;
import com.flamingo.sdk.access.IGPUploadPlayerInfoObsv;
import com.flamingo.sdk.access.IGPUserObsv;
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
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.flamingo.sdk.access.GPSDKPlayerInfo.TYPE_CREATE_ROLE;
import static com.flamingo.sdk.access.GPSDKPlayerInfo.TYPE_ENTER_GAME;
import static com.flamingo.sdk.access.GPSDKPlayerInfo.TYPE_LEVEL_UP;

public class GuoPan extends OPlatformSDK {
    private static final String TAG = "GuoPan";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public GuoPan(OPlatformBean pBean) {
        super(pBean);
    }
    IGPApi igpApi;
    @Override
    public void init(final Activity activity) {
        GPApiFactory.getGPApiForMarshmellow(activity, new Callback() {
            @Override
            public void onCallBack(IGPApi api) {
                igpApi = api;
                igpApi.initSdk(activity, SDKApplication.getPlatformConfig().getExt1(), SDKApplication.getPlatformConfig().getExt2(), new IGPSDKInitObsv() {
                    @Override
                    public void onInitFinish(GPSDKInitResult gpsdkInitResult) {
                        if(gpsdkInitResult.mInitErrCode == 0){
                            Bus.getDefault().post(OInitEv.getSucc());
                            GPApiFactory.getGPApi().setSDKInnerEventObserver(new IGPSDKInnerEventObserver() {
                                @Override
                                public void onSdkLogout() {
                                    Bus.getDefault().post(new OLogoutEv());
                                }

                                @Override
                                public void onSdkSwitchAccount() {

                                }
                            });
                        }else {
                            Bus.getDefault().post(OInitEv.getFail(gpsdkInitResult.mInitErrCode,"fail"));
                        }

                    }
                });
            }
        });

    }

    @Override
    public void login(Activity activity) {
        igpApi.login(activity, new IGPUserObsv() {
            @Override
            public void onFinish(GPUserResult gpUserResult) {
                if(gpUserResult.mErrCode == 0){
                    login2RSService(igpApi.getLoginToken(),igpApi.getLoginUin());
                }else {
                    Bus.getDefault().post(OLoginEv.getFail(gpUserResult.mErrCode,"fail"));
                }
            }
        });
    }
    private void login2RSService(String token,String uid) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("puid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }
    @Override
    public void logout(Activity mainAct) {
        GPApiFactory.getGPApi().logout();
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        GPSDKGamePayment gpsdkGamePayment = new GPSDKGamePayment();
        JSONObject payJson = null;
        try {
            payJson = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            gpsdkGamePayment.mItemId = payJson.getString("productId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        gpsdkGamePayment.mItemName = payParams.get(JunSConstants.PAY_ORDER_NAME);
        gpsdkGamePayment.mPaymentDes = payParams.get(JunSConstants.PAY_ORDER_NAME);
        gpsdkGamePayment.mItemPrice = Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));
        gpsdkGamePayment.mItemOrigPrice = Float.parseFloat(payParams.get(JunSConstants.PAY_MONEY));

        gpsdkGamePayment.mSerialNumber = payParams.get(JunSConstants.PAY_M_ORDER_ID);
        gpsdkGamePayment.mCurrentActivity = activity;
        igpApi.buy(gpsdkGamePayment, new IGPPayObsv() {
            @Override
            public void onPayFinish(GPPayResult gpPayResult) {
                if(gpPayResult.mErrCode == 0){
                    Bus.getDefault().post(OPayEv.getSucc("success"));
                }else{
                    Bus.getDefault().post(OPayEv.getFail(gpPayResult.mErrCode,"fail"));
                }
            }
        });
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        GPSDKPlayerInfo gpsdkPlayerInfo = new GPSDKPlayerInfo();
        gpsdkPlayerInfo.mGameLevel = submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL);
        gpsdkPlayerInfo.mPlayerId = submitInfo.get(JunSConstants.SUBMIT_ROLE_ID);
        gpsdkPlayerInfo.mPlayerNickName = submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME);
        gpsdkPlayerInfo.mServerId = submitInfo.get(JunSConstants.SUBMIT_SERVER_ID);
        gpsdkPlayerInfo.mPartyName = submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME);
        gpsdkPlayerInfo.mGameVipLevel = submitInfo.get(JunSConstants.SUBMIT_VIP);
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_CREATE)){
            gpsdkPlayerInfo.mType = TYPE_CREATE_ROLE;
        }
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            gpsdkPlayerInfo.mType = TYPE_ENTER_GAME;
        }
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_UPGRADE)){
            gpsdkPlayerInfo.mType = TYPE_LEVEL_UP;
        }
        igpApi.uploadPlayerInfo(gpsdkPlayerInfo, new IGPUploadPlayerInfoObsv() {
            @Override
            public void onUploadFinish(GPUploadPlayerInfoResult gpUploadPlayerInfoResult) {

            }
        });
    }

    @Override
    public void exitGame(Activity activity) {
        igpApi.exit(new IGPExitObsv() {
            @Override
            public void onExitFinish(GPExitResult gpExitResult) {
                if(gpExitResult.mResultCode == 1){
                    Bus.getDefault().post(OExitEv.getSucc());
                }
            }
        });
    }
}
