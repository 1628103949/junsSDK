package com.juns.sdk.core.own.account;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.exception.JunSServerException;
import com.juns.sdk.core.own.account.login.AutoLoginDialog;
import com.juns.sdk.core.own.account.login.JunsNotiDialog;
import com.juns.sdk.core.own.account.login.LoginDialog;
import com.juns.sdk.core.own.account.user.User;
import com.juns.sdk.core.own.account.user.UserUtils;
import com.juns.sdk.core.own.event.JSLoginEv;
import com.juns.sdk.core.own.fw.JSBallEv;
import com.juns.sdk.core.own.fw.account.AccountDialog;
import com.juns.sdk.core.sdk.JunSUtils;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.ads.JunsAds;
import com.juns.sdk.core.sdk.common.HeartBeat;
import com.juns.sdk.core.sdk.common.NoticeDialog;
import com.juns.sdk.core.sdk.common.RealNameDialog;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.view.dialog.BounceEnter.BounceBottomEnter;
import com.juns.sdk.framework.view.dialog.FadeEnter.FadeEnter;
import com.juns.sdk.framework.view.dialog.FadeExit.FadeExit;
import com.juns.sdk.framework.view.dialog.ZoomExit.ZoomOutExit;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.image.ImageOptions;
import com.juns.sdk.framework.xutils.x;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Data：09/01/2019-3:04 PM
 * Author: ranger
 */
public class JunSAccount {
    public static final int AUTO_LOGIN = 1;
    public static final int ACCOUNT_LOGIN = 2;
    public static final int ACCOUNT_REG = 3;
    public static final int PHONE_REG = 4;

    private static boolean isLogout = false;

    private Activity loginActivity;
    private AutoLoginDialog autoLoginDialog;
    private LoginDialog loginDialog;
    private NoticeDialog loginNoticeDialog;

    private User currentUser;

    public static synchronized boolean isLogout() {
        return isLogout;
    }

    public static synchronized void setIsLogout(boolean logout) {
        isLogout = logout;
    }

    public void doLogin(Activity act) {
        this.loginActivity = act;
        if (isLogout()) {
            //处于 Logout 状态显示登录界面
            //显示登录界面
            showLoginDialog();
        } else {
            //正常登录逻辑
            User lastUser = UserUtils.getLastUser();
            if (lastUser != null) {
                //上次登录信息不为空，触发自动登录
                autoLogin(lastUser);
            } else {
                //显示登录界面
                showLoginDialog();
            }
        }
    }

    private void autoLogin(final User user) {
        if (TextUtils.isEmpty(user.getUserId()) || TextUtils.isEmpty(user.getUserName()) || TextUtils.isEmpty(user.getUserPwd())) {
            showLoginDialog();
            return;
        }
        //显示自动登录界面
        dismissAutoDialog();
        String showName = user.getUserName();
        autoLoginDialog = new AutoLoginDialog(loginActivity, JunSAccount.this, showName, user.getUserPwd(), new AutoLoginDialog.AutoCallback() {
            @Override
            public void onSwitchAccount() {
                showLoginDialog();
            }
        });
        autoLoginDialog.showAnim(new FadeEnter()).dismissAnim(new FadeExit()).dimEnabled(true).show();
    }

    private void showLoginDialog() {
        if (loginDialog != null) {
            if (loginDialog.isShowing()) {
                loginDialog.dismiss();
            }
        }
        loginDialog = null;
        loginDialog = new LoginDialog(loginActivity, JunSAccount.this);
        loginDialog.showAnim(new BounceBottomEnter()).dismissAnim(new ZoomOutExit()).dimEnabled(true).show();
    }

    public void doLogout(Activity act) {
        //处理注销逻辑
        setIsLogout(true);
        //隐藏悬浮窗
        Bus.getDefault().post(JSBallEv.getHide());
    }

    public void dealLoginSuccResult(int loginType, JunSResponse ret) {
        dealLoginSuccResult(loginType, ret, null);
    }

    public void dealLoginSuccResult(int loginType, JunSResponse ret, String uPwd) {
        try {
            String data = ret.data;
            if (ret.state == 0) {
                //服务端返回数据为空
                if (loginType == AUTO_LOGIN) {
                    //显示登录框
                    dismissAutoDialog();
                    showLoginDialog();
                } else {
                    //回调登录失败
                    onLoginFail(JunSConstants.Status.SERVER_ERR, "result data is null.");
                }
                return;
            }
            //通用解析
            JSONObject dataJson = new JSONObject(data);
            String userId = dataJson.getString("uid");
            String userName = dataJson.getString("uname");
            String userToken = dataJson.getString("token");
            String userPhone = null;
            if (dataJson.has("phone")) {
                userPhone = dataJson.getString("phone");
            }
            User user = new User();
            user.setUserId(userId);
            user.setUserName(userName);
            user.setUserToken(userToken);
            switch (loginType) {
                case AUTO_LOGIN:
                    //自动登录，保存密码
                    JunsAds.getInstance().onEvLogin(loginActivity, userId);
                    if (!TextUtils.isEmpty(uPwd)) {
                        user.setUserPwd(uPwd);
                    }
                    dismissAutoDialog();
                    break;
                case ACCOUNT_LOGIN:
                    //帐号登录，保存密码
                    JunsAds.getInstance().onEvLogin(loginActivity, userId);
                    if (!TextUtils.isEmpty(uPwd)) {
                        user.setUserPwd(uPwd);
                    }
                    dismissLoginDialog();
                    break;
                case ACCOUNT_REG:
                    //帐号注册，保存密码
                    JunsAds.getInstance().onEvRegister(loginActivity, "account");
                    if (!TextUtils.isEmpty(uPwd)) {
                        user.setUserPwd(uPwd);
                    }
                    dismissLoginDialog();
                    break;
                case PHONE_REG:
                    //手机注册，解析并保存密码
                    JunsAds.getInstance().onEvRegister(loginActivity, "phone");
                    if (dataJson.has("upwd")) {
                        String phonePwd = dataJson.getString("upwd");
                        if (!TextUtils.isEmpty(phonePwd)) {
                            user.setUserPwd(phonePwd);
                        }
                    }
                    dismissLoginDialog();
                    break;
            }

            //新增一条帐号记录
            UserUtils.addUserRecord(user);
            //记录最近登录帐号
            UserUtils.setLastUser(user);

            //保存用户信息
            SDKData.setSdkUserId(user.getUserId());
            SDKData.setSdkUserName(user.getUserName());
            SDKData.setSdkUserToken(user.getUserToken());
            if (TextUtils.isEmpty(user.getUserPhone())) {
                SDKData.setSdkUserPhone(user.getUserPhone());
            }
            currentUser = user;

            //解析悬浮窗数据，并存储
            if (dataJson.has("xfc")) {
                SDKData.setFloatWindowData(dataJson.getString("xfc"));
                if (!TextUtils.isEmpty(SDKData.getFloatWindowData())) {
                    try {
                        JSONArray jsonArray = new JSONArray(SDKData.getFloatWindowData());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject itemJson = jsonArray.getJSONObject(i);
                            parseMenuItem(itemJson);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                preLoadIcon();
            }

            //处理登录公告
            //处理实名认证
//            if (dataJson.has("isadult2")) {
//                SDKData.setSdkUserIsadult(dataJson.getInt("isadult2"));
//            }
            dealNotice(dataJson, loginType);
            //防沉迷限制
            if (dataJson.has("code")) {
                if(dataJson.getInt("code") == 2){
                    JunsNotiDialog.showNoti(loginActivity,ret.msg,true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //数据解析异常
            if (loginType == AUTO_LOGIN) {
                //显示登录框
                dismissAutoDialog();
                showLoginDialog();
            } else {
                //回调登录失败
                onLoginFail(JunSConstants.Status.PARSE_ERR, "server data parse exception, exception : " + e.toString());
            }
        }
    }
    private void parseMenuItem(final JSONObject itemObj) {
        try {
            String keyName = itemObj.getString("key");
            switch (keyName) {
                case "account":
                    AccountDialog.mUserName = itemObj.getString("uname");
                    AccountDialog.mUserPhone = itemObj.getString("phone");
                    String name = itemObj.getString("name");
                    AccountDialog.mUserRealName = name.substring(0, 1) + "**";
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dealNotice(final JSONObject dataJson, final int loginType) {
        try {
            if (dataJson.has("nurl")) {
                String noticeUrl = JunSUtils.buildCommonWebUrl(dataJson.getString("nurl"), true);
                if (!TextUtils.isEmpty(noticeUrl)) {
                    if (loginNoticeDialog != null && loginNoticeDialog.isShowing()) {
                        loginNoticeDialog.dismiss();
                    }
                    loginNoticeDialog = null;
                    loginNoticeDialog = new NoticeDialog(loginActivity, noticeUrl, new NoticeDialog.NoticeCallback() {
                        @Override
                        public void onFinish() {
                            dealRealName(dataJson, loginType);
                        }
                    });
                    loginNoticeDialog.show();
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dealRealName(dataJson, loginType);
    }

    private void dealRealName(JSONObject dataJson, int loginType) {
        try {
            //0:已经实名认证无需弹窗认证
            //1:未实名要弹非强制实名认证窗口
            //2:未实名要弹强制实名认证窗口，否则不能进行下一步
            if (dataJson.has("isadult")) {
                SDKData.setSdkUserIsadult(dataJson.getInt("isadult"));
            }
            if (dataJson.has("isverify")) {
                String isVerify = dataJson.getString("isverify");
                if (isVerify.equals("1")) {
                    SDKData.setSdkUserIsVerify(false);
                    RealNameDialog.show(loginActivity, true, new RealNameDialog.Callback() {
                        @Override
                        public void onSuccess() {
                            dealCallback();
                        }

                        @Override
                        public void onCancel() {
                            dealCallback();
                        }
                    });
                    return;
                } else if (isVerify.equals("2")) {
                    SDKData.setSdkUserIsVerify(false);
                    RealNameDialog.show(loginActivity, false, new RealNameDialog.Callback() {
                        @Override
                        public void onSuccess() {
                            dealCallback();
                        }

                        @Override
                        public void onCancel() {
                            dealCallback();
                        }
                    });
//                    if(SDKData.getSdkPeriod()!=9999){
//                        JunsNotiDialog.showNoti(SDKCore.getMainAct(),
//                                "如果您是未成年人，请在家长监督下填写自己真实的身份证信息，根据国家新闻出版署《关于防止未成年人沉迷网络游戏的通知》和《关于进一步严格管理切实防止未成年人沉迷网络游戏的通知》，对您有以下限制：\n" +
//                                "游戏登陆:\n" +
//                                "1.将不以任何形式为未成年人提供网络游戏服务。\n" +
//                                "游戏充值:\n" +
//                                "1.将不以任何形式为未成年人提供游戏充值服务。\n",true,240);
//                    }
                    return;
                }
//                else {
//                    if(SDKData.getSdkPeriod()!=9999){
//                        if(dataJson.has("isadult")){
//                            int isadult = dataJson.getInt("isadult");
//                            if (isadult == 0){
//                                JunsNotiDialog.showNoti(SDKCore.getMainAct(),"\n未成年登陆提示：\n" +
//                                        "\n您已被识别为未成年人，根据国家新闻出版署《关于防止未成年人沉迷网络游戏的通知》和《关于进一步严格管理切实防止未成年人沉迷网络游戏的通知》，该游戏将不以任何形式为未成年人提供游戏服务。",false,190);
//                            }
//                        }
//                    }
//                    SDKData.setSdkUserIsVerify(true);
//                    return;
//                }
            }
//            else {
//                dealCallback();
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dealCallback();
    }

    private void dealCallback() {
        //回调登录结果
        String retString = UserUtils.toOpenUser(currentUser);
        //登录成功重置注销状态
        setIsLogout(false);
        JunsAds.getInstance().onEvLoginSucc(loginActivity, currentUser.getUserId());
        Bus.getDefault().post(JSLoginEv.getSucc(retString));
    }

    public void dealLoginFailResult(int loginType, final Throwable ex) {
        SDKCore.getMainAct().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ex instanceof JunSServerException) {
                    ViewUtils.sdkShowTips(loginActivity, ((JunSServerException) ex).getServerMsg());
                } else {
                    ViewUtils.sdkShowTips(loginActivity, "网络发生异常，请重试！");
                }
            }
        });
        if (loginType == AUTO_LOGIN) {
            //显示登录框
            dismissAutoDialog();
            showLoginDialog();
        }

    }

    public void onLoginViewClose() {
        //处理登录取消
        Bus.getDefault().post(JSLoginEv.getFail(JunSConstants.Status.USER_CANCEL, "用户取消登录！"));
        dismissLoginDialog();
    }

    private void onLoginFail(int code, String msg) {
        //处理登录失败
        Bus.getDefault().post(JSLoginEv.getFail(code, msg));
        ViewUtils.sdkShowTips(loginActivity, "登录异常，请重试！");
        dismissLoginDialog();
    }

    private void dismissLoginDialog() {
        if (loginDialog != null && loginDialog.isShowing()) {
            loginDialog.dismiss();
        }
        loginDialog = null;
    }

    private void dismissAutoDialog() {
        if (autoLoginDialog != null && autoLoginDialog.isShowing()) {
            autoLoginDialog.dismiss();
        }
        autoLoginDialog = null;
    }

    public void destroy() {
        if (loginDialog != null && loginDialog.isShowing()) {
            loginDialog.dismiss();
        }
        loginDialog = null;
        loginActivity = null;

        if (autoLoginDialog != null && autoLoginDialog.isShowing()) {
            autoLoginDialog.dismiss();
        }
        autoLoginDialog = null;
    }

    /**
     * 提前加载悬浮窗图片
     */
    private void preLoadIcon() {
        if (!TextUtils.isEmpty(SDKData.getFloatWindowData())) {
            try {
                JSONArray jsonArray = new JSONArray(SDKData.getFloatWindowData());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject itemJson = jsonArray.getJSONObject(i);
                    String iconUrl = itemJson.getString("icon");
                    if (!TextUtils.isEmpty(iconUrl)) {
                        x.image().loadFile(iconUrl, ImageOptions.DEFAULT, new Callback.CacheCallback<File>() {
                            @Override
                            public boolean onCache(File result) {
                                return true;
                            }

                            @Override
                            public void onSuccess(File result) {

                            }

                            @Override
                            public void onError(Throwable ex, boolean isOnCallback) {

                            }

                            @Override
                            public void onCancelled(CancelledException cex) {

                            }

                            @Override
                            public void onFinished() {

                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
