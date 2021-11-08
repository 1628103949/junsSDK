package com.juns.sdk.core.platform;

import android.text.TextUtils;

import com.hio.sdk.HIOSDK;
import com.hio.sdk.common.modle.EventsType;
import com.hio.sdk.common.modle.HIOSDKConstant;
import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.exception.JunSServerException;
import com.juns.sdk.core.http.params.MVerifyParam;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.sdk.JunSUtils;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.common.NoticeDialog;
import com.juns.sdk.core.sdk.flow.MPayFlow;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.x;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Description: something help for channel compatibly.
 * Data：27/09/2018-11:21 AM
 * Author: ranger
 */
public class OPlatformUtils {
    private static NoticeDialog loginNoticeDialog;
    public static void loginToServer(String token) {
        loginToServer(token, null);
    }
    public static void loginToServer(String token, String data) {
        loginToServer(token, null, null, data);
    }

    public static void loginToServer(String token, String uid, String name, String data) {
        MVerifyParam mVerifyParam = new MVerifyParam(token, uid, name, data);
        x.http().post(mVerifyParam, new Callback.CommonCallback<JunSResponse>() {
            @Override
            public void onSuccess(JunSResponse junSResponse) {
                dealReqSuccess(junSResponse);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (ex instanceof JunSServerException) {
                    ViewUtils.sdkShowTips(SDKCore.getMainAct(),((JunSServerException) ex).getServerMsg());
                } else {
                    ViewUtils.sdkShowTips(SDKCore.getMainAct(),"网络异常，发送失败，请重试！");
                }
                dealReqFail(ex);
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    private static void dealReqFail(Throwable ex) {
        if (ex instanceof JunSServerException) {
            //服务端错误
            Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.SERVER_ERR, ((JunSServerException) ex).getServerMsg()));
        } else {
            Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.HTTP_ERR, ex.getMessage()));
        }
    }

    private static void dealReqSuccess(JunSResponse response) {
        try {
            if (TextUtils.isEmpty(response.data) || response.data.equals("[]")) {
                Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.SERVER_ERR, "server data error."));
                return;
            }
            JSONObject dataJson = new JSONObject(response.data);
            String userId = dataJson.getString("uid");
            String userName = dataJson.getString("uname");
            String userToken = dataJson.getString("token");
            //某些渠道传回accountid
            if(!dataJson.optString("puid").equals("")){
                SDKData.setpUserId(dataJson.optString("puid"));
            }
            HashMap<String, Object> infos = new HashMap<>();
//                //必传项
            infos.put(HIOSDKConstant.HIO_USERID, userId);//用户userid
            HIOSDK.getInstance().onEvent(EventsType.USER_LOGIN, infos);
            //储存进本地
            SDKData.setSdkUserId(userId);
            SDKData.setSdkUserName(userName);
            SDKData.setSdkUserToken(userToken);
            dealNotice(dataJson, userId, userName, userToken);
        } catch (Exception e) {
            e.printStackTrace();
            Bus.getDefault().post(OLoginEv.getFail(JunSConstants.Status.SERVER_ERR, "server data error, parse data exception."));
        }
    }

    private static void dealNotice(JSONObject dataJson, final String userId, final String userName, final String userToken) {
        try {
            if (dataJson.has("nurl")) {
                String noticeUrl = JunSUtils.buildCommonWebUrl(dataJson.getString("nurl"), true);
                if (!TextUtils.isEmpty(noticeUrl)) {
                    if (loginNoticeDialog != null && loginNoticeDialog.isShowing()) {
                        loginNoticeDialog.dismiss();
                    }
                    loginNoticeDialog = null;
                    loginNoticeDialog = new NoticeDialog(SDKCore.getMainAct(), noticeUrl, new NoticeDialog.NoticeCallback() {
                        @Override
                        public void onFinish() {
                            dealCallback(userId, userName, userToken);
                        }
                    });
                    loginNoticeDialog.show();
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dealCallback(userId, userName, userToken);
    }

    private static void dealCallback(String userId, String userName, String userToken) {
        //回调

        String retString = toOpenUser(userId, userName, userToken);
        Bus.getDefault().post(OLoginEv.getSucc(retString));
    }

    private static String toOpenUser(String userId, String userName, String token) {
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(token)) {
            return "no user data, please contact technical.";
        }
        try {
            JSONObject userJson = new JSONObject();
            userJson.put("userId", userId);
            userJson.put("userName", userName);
            userJson.put("token", token);
            return userJson.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
