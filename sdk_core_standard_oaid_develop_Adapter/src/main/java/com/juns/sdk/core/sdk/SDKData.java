package com.juns.sdk.core.sdk;

import android.text.TextUtils;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.framework.safe.JunSEncrypt;
import com.juns.sdk.framework.utils.SPUtils;

/**
 * Data：20/12/2018-3:28 PM
 * Author: ranger
 */
public class SDKData {
    private static final String SDK_SP_FILE = "juns_sdk";
    private static final SPUtils SP_UTILS = SPUtils.getInstance(SDK_SP_FILE);
    //帐号相关
    private static final String USER_LAST_LOGIN = "user_last_login";
    private static final String USER_RECORD = "user_record";
    private static final String USER_STORE_WH = "user_store_where";
    //浮标相关
    private static final String FW_LAST_X = "fw_last_x";
    private static final String FW_LAST_Y = "fw_last_y";
    private static final String FW_SWITCH_STATUS = "fw_switch_status";
    private static final String FW_GAME_SWITCH_STATUS = "fw_game_switch_status";
    //更新相关
    private static final String UPDATE_APK_VERSION = "update_apk_version";
    private static final String UPDATE_APK_PATH = "update_apk_path";

    //自动注册相关
    private static final String RANDOM_USER_NAME = "random_user_name";
    private static final String RANDOM_USER_PWD = "random_user_pwd";

    //duid
    private static final String SDK_DUID = "sdk_duid";

    //首次激活
    private static final String SDK_FIRST_ACTIVE = "sdk_first_active";
    private static final String SDK_AGREE = "sdk_agree";
    private static final String SDK_ANDROID_ID = "sdk_android_id";
    private static final String SDK_OAID = "sdk_oaid";
    private static final String SDK_KA_REFER = "sdk_ka_refer";
    private static final String SDK_KA_NEED = "sdk_ka_need";
    private static final String SDK_KA_ADID = "sdk_ka_adid";

    //心跳频率
    private static int SDK_PERIOD = 9999;

    private static String IS_SHOP = "";

    private static String USER_REAL_NAME = "";

    private static int SDK_INIT_DELAY = 0;

    private static boolean SDK_IS_REFER = true;

    public static String getUserRealName() {
        return USER_REAL_NAME;
    }

    public static void setUserRealName(String userRealName) {
        USER_REAL_NAME = userRealName;
    }

    private static int SDK_KA_STATE = 0;

    public static int getSdkKaState() {
        return SDK_KA_STATE;
    }

    public static void setSdkKaState(int sdkKaState) {
        SDK_KA_STATE = sdkKaState;
    }

    public static boolean isSdkIsRefer() {
        return SDK_IS_REFER;
    }

    public static void setSdkIsRefer(boolean sdkIsRefer) {
        SDK_IS_REFER = sdkIsRefer;
    }

    public static int getSdkInitDelay() {
        return SDK_INIT_DELAY;
    }

    public static void setSdkInitDelay(int sdkInitDelay) {
        SDK_INIT_DELAY = sdkInitDelay;
    }

    public static int getSdkPeriod() {
        return SDK_PERIOD;
    }

    public static void setSdkPeriod(int sdkPeriod) {
        SDK_PERIOD = sdkPeriod;
    }

    public static String getIsShop() {
        return IS_SHOP;
    }

    public static void setIsShop(String isShop) {
        IS_SHOP = isShop;
    }

    //悬浮窗内容
    public static String floatWindowData = null;
    //SDK基础配置
    private static String sdkGID = null;
    private static String sdkAppKey = null;
    private static String sdkPID = null;
    private static String sdkRefer = null;
    private static String sdkVer = null;
    private static String loginParams = null;

    //帐号信息
    private static String sdkUserName = null;
    private static String sdkUserPhone = null;
    private static String sdkUserId = null;
    private static String sdkUserToken = null;
    private static boolean sdkUserIsVerify = false;
    //日志开关
    private static boolean logSwitch = false;
    //平台配置相关
    private static String userAgreement = null;
    private static String userHorsePrompt = null;
    private static String userFindPwd = null;
    //渠道相关
    private static String pUserId = null;
    private static String pUserName = null;
    private static String pNickName = null;

    //角色相关信息
    private static String gameRoleId = null;
    private static String gameRoleName = null;
    private static String gameRoleLevel = null;
    private static String gameServerId = null;
    private static String gameServerName = null;
    private static String gameBalance = null;
    private static String gameVip = null;
    private static String gamePartyName = null;
    private static String gameExt = null;
    private static String gameCreateTime = null;
    private static String gameUpTime = null;
    private static String gameLastRoleName = null;

    //当前支付M平台订单
    private static String currentMPayOrder = null;

    public static int getFWLastX() {
        return SP_UTILS.getInt(FW_LAST_X);
    }

    public static void setFwLastX(int x) {
        SP_UTILS.put(FW_LAST_X, x);
    }

    public static int getFWLastY() {
        return SP_UTILS.getInt(FW_LAST_Y);
    }

    public static void setFwLastY(int y) {
        SP_UTILS.put(FW_LAST_Y, y);
    }

    public static boolean getFwSwitchStatus() {
        return SP_UTILS.getBoolean(FW_SWITCH_STATUS);
    }

    public static void setFwSwitchStatus(boolean status) {
        SP_UTILS.put(FW_SWITCH_STATUS, status);
    }

    public static boolean getFwGameSwitchStatus() {
        return SP_UTILS.getBoolean(FW_GAME_SWITCH_STATUS, true);
    }

    public static String getLoginParams() {
        return loginParams;
    }

    public static void setLoginParams(String loginParams) {
        SDKData.loginParams = loginParams;
    }

    public static void setFwGameSwitchStatus(boolean status) {
        SP_UTILS.put(FW_GAME_SWITCH_STATUS, status);
    }

    public static int getUserStoreWh() {
        return SP_UTILS.getInt(USER_STORE_WH, 0);
    }

    public static void setUserStoreWh(int wh) {
        SP_UTILS.put(USER_STORE_WH, wh);
    }

    public static String getUserLastLogin() {
        return JunSEncrypt.decryptInfo(SP_UTILS.getString(USER_LAST_LOGIN));
    }

    public static void setUserLastLogin(String userString) {
        SP_UTILS.put(USER_LAST_LOGIN, JunSEncrypt.encryptInfo(userString));
    }

    public static String getUserRecord() {
        return SP_UTILS.getString(USER_RECORD);
    }

    public static void setUserRecord(String userRecord) {
        SP_UTILS.put(USER_RECORD, userRecord);
    }

    public static String getSDKDuid() {
        return JunSEncrypt.decryptInfo(SP_UTILS.getString(SDK_DUID));
    }

    public static void setSdkDuid(String duid) {
        SP_UTILS.put(SDK_DUID, JunSEncrypt.encryptInfo(duid));
    }

    public static String getUpdateApkVersion() {
        return JunSEncrypt.decryptInfo(SP_UTILS.getString(UPDATE_APK_VERSION));
    }

    public static void setUpdateApkVersion(String version) {
        SP_UTILS.put(UPDATE_APK_VERSION, JunSEncrypt.encryptInfo(version));
    }

    public static String getUpdateApkPath() {
        return JunSEncrypt.decryptInfo(SP_UTILS.getString(UPDATE_APK_PATH));
    }

    public static void setUpdateApkPath(String path) {
        SP_UTILS.put(UPDATE_APK_PATH, JunSEncrypt.encryptInfo(path));
    }

    public static String getRandomUserName() {
        return JunSEncrypt.decryptInfo(SP_UTILS.getString(RANDOM_USER_NAME));
    }

    public static void setRandomUserName(String userName) {
        SP_UTILS.put(RANDOM_USER_NAME, JunSEncrypt.encryptInfo(userName));
    }

    public static String getRandomUserPwd() {
        return JunSEncrypt.decryptInfo(SP_UTILS.getString(RANDOM_USER_PWD));
    }

    public static void setRandomUserPwd(String pwd) {
        SP_UTILS.put(RANDOM_USER_PWD, JunSEncrypt.encryptInfo(pwd));
    }

    public static boolean getSdkFirstActive() {
        return SP_UTILS.getBoolean(SDK_FIRST_ACTIVE, true);
    }

    public static void setSdkFirstActive(boolean isActive) {
        SP_UTILS.put(SDK_FIRST_ACTIVE, isActive);
    }

    public static boolean getSdkAgree() {
        return SP_UTILS.getBoolean(SDK_AGREE, false);
    }

    public static void setSdkAgree(boolean isActive) {
        SP_UTILS.put(SDK_AGREE, isActive);
    }

    public static String getSdkAndroidId() {
        return SP_UTILS.getString(SDK_ANDROID_ID, "");
    }

    public static void setSdkAndroidId(String androidId) {
        SP_UTILS.put(SDK_ANDROID_ID, androidId);
    }

    public static String getSdkOaid() {
        return SP_UTILS.getString(SDK_OAID, "");
    }

    public static void setSdkOaid(String oaid) {
        SP_UTILS.put(SDK_OAID, oaid);
    }

    public static String getSdkKaRefer() {
        return SP_UTILS.getString(SDK_KA_REFER, "");
    }

    public static void setSdkKaRefer(String refer) {
        setSdkRefer(refer);
        SDKData.setSdkIsRefer(false);
        SP_UTILS.put(SDK_KA_REFER, refer);
    }
    public static String getSdkKaAdid() {
        return SP_UTILS.getString(SDK_KA_ADID, "");
    }

    public static void setSdkKaAdid(String adid) {
        SP_UTILS.put(SDK_KA_ADID, adid);
    }

    public static boolean getSdkKaNeed() {
        return SP_UTILS.getBoolean(SDK_KA_NEED, true);
    }

    public static void setSdkKaNeed(Boolean isNeed) {
        SP_UTILS.put(SDK_KA_NEED, isNeed);
    }

    public static void cleanSDKData() {
        sdkGID = null;
        sdkAppKey = null;
        sdkPID = null;
        sdkRefer = null;
        sdkVer = null;
        sdkUserName = null;
        sdkUserPhone = null;
        sdkUserId = null;
        sdkUserToken = null;
        sdkUserIsVerify = false;
        gameRoleId = null;
        gameRoleName = null;
        gameRoleLevel = null;
        gameServerId = null;
        gameServerName = null;
        gameBalance = null;
        gameVip = null;
        gamePartyName = null;
        gameExt = null;
        gameCreateTime = null;
        gameUpTime = null;
        gameLastRoleName = null;
        userHorsePrompt = null;
        userFindPwd = null;
        floatWindowData = null;
    }

    public static void cleanUserData() {
        sdkUserId = null;
        sdkUserName = null;
        sdkUserPhone = null;
        sdkUserIsVerify = false;
    }

    public static String getSdkGID() {
        return sdkGID;
    }

    public static void setSdkGID(String sdkGID) {
        SDKData.sdkGID = sdkGID;
    }

    public static String getSdkAppKey() {
        return sdkAppKey;
    }

    public static void setSdkAppKey(String sdkAppKey) {
        SDKData.sdkAppKey = sdkAppKey;
    }

    public static String getSdkPID() {
        return sdkPID;
    }

    public static void setSdkPID(String sdkPID) {
        SDKData.sdkPID = sdkPID;
    }

    public static String getSdkRefer() {
        return sdkRefer;
    }

    public static void setSdkRefer(String sdkRefer) {
        SDKData.sdkRefer = sdkRefer;
    }

    public static String getSdkVer() {
        return sdkVer;
    }

    public static void setSdkVer(String sdkVer) {
        SDKData.sdkVer = sdkVer;
    }

    public static String getSdkUserName() {
        return sdkUserName;
    }

    public static void setSdkUserName(String sdkUserName) {
        SDKData.sdkUserName = sdkUserName;
    }

    public static String getSdkUserPhone() {
        if (sdkUserPhone == null) {
            return "";
        }
        return sdkUserPhone;
    }

    public static void setSdkUserPhone(String sdkUserPhone) {
        SDKData.sdkUserPhone = sdkUserPhone;
    }

    public static String getSdkUserId() {
        return sdkUserId;
    }

    public static void setSdkUserId(String sdkUserId) {
        SDKData.sdkUserId = sdkUserId;
    }

    public static String getSdkUserToken() {
        return sdkUserToken;
    }

    public static void setSdkUserToken(String sdkUserToken) {
        SDKData.sdkUserToken = sdkUserToken;
    }

    public static boolean isLogSwitch() {
        return logSwitch;
    }

    public static void setLogSwitch(boolean logSwitch) {
        SDKData.logSwitch = logSwitch;
    }

    public static String getGameRoleId() {
        return gameRoleId;
    }

    public static void setGameRoleId(String gameRoleId) {
        SDKData.gameRoleId = gameRoleId;
    }

    public static String getGameRoleName() {
        return gameRoleName;
    }

    public static void setGameRoleName(String gameRoleName) {
        SDKData.gameRoleName = gameRoleName;
    }

    public static String getGameRoleLevel() {
        return gameRoleLevel;
    }

    public static void setGameRoleLevel(String gameRoleLevel) {
        SDKData.gameRoleLevel = gameRoleLevel;
    }

    public static String getGameServerId() {
        return gameServerId;
    }

    public static void setGameServerId(String gameServerId) {
        SDKData.gameServerId = gameServerId;
    }

    public static String getGameServerName() {
        return gameServerName;
    }

    public static void setGameServerName(String gameServerName) {
        SDKData.gameServerName = gameServerName;
    }

    public static String getGameBalance() {
        return gameBalance;
    }

    public static void setGameBalance(String gameBalance) {
        SDKData.gameBalance = gameBalance;
    }

    public static String getGameVip() {
        return gameVip;
    }

    public static void setGameVip(String gameVip) {
        SDKData.gameVip = gameVip;
    }

    public static String getGamePartyName() {
        return gamePartyName;
    }

    public static void setGamePartyName(String gamePartyName) {
        SDKData.gamePartyName = gamePartyName;
    }

    public static String getGameExt() {
        return gameExt;
    }

    public static void setGameExt(String gameExt) {
        SDKData.gameExt = gameExt;
    }

    public static String getGameCreateTime() {
        return gameCreateTime;
    }

    public static void setGameCreateTime(String gameCreateTime) {
        SDKData.gameCreateTime = gameCreateTime;
    }

    public static String getGameUpTime() {
        return gameUpTime;
    }

    public static void setGameUpTime(String gameUpTime) {
        SDKData.gameUpTime = gameUpTime;
    }

    public static String getGameLastRoleName() {
        return gameLastRoleName;
    }

    public static void setGameLastRoleName(String gameLastRoleName) {
        SDKData.gameLastRoleName = gameLastRoleName;
    }

    public static String getUserAgreement() {
        if (TextUtils.isEmpty(userAgreement)) {
            return JunSConstants.JUNS_USER_AGREEMENT;
        }
        return userAgreement;
    }

    public static void setUserAgreement(String userAgreement) {
        SDKData.userAgreement = userAgreement;
    }

    public static String getpUserId() {
        return pUserId;
    }

    public static void setpUserId(String pUserId) {
        SDKData.pUserId = pUserId;
    }

    public static String getpUserName() {
        return pUserName;
    }

    public static void setpUserName(String pUserName) {
        SDKData.pUserName = pUserName;
    }

    public static String getpNickName() {
        return pNickName;
    }

    public static void setpNickName(String pNickName) {
        SDKData.pNickName = pNickName;
    }

    public static String getUserHorsePrompt() {
        return userHorsePrompt;
    }

    public static void setUserHorsePrompt(String userHorsePrompt) {
        SDKData.userHorsePrompt = userHorsePrompt;
    }

    public static String getFloatWindowData() {
        return floatWindowData;
    }

    public static void setFloatWindowData(String floatWindowData) {
        SDKData.floatWindowData = floatWindowData;
    }

    public static boolean isSdkUserIsVerify() {
        return sdkUserIsVerify;
    }

    public static void setSdkUserIsVerify(boolean sdkUserIsVerify) {
        SDKData.sdkUserIsVerify = sdkUserIsVerify;
    }

    public static String getCurrentMPayOrder() {
        return currentMPayOrder;
    }

    public static void setCurrentMPayOrder(String currentMPayOrder) {
        SDKData.currentMPayOrder = currentMPayOrder;
    }

    public static String getUserFindPwd() {
        if (TextUtils.isEmpty(userFindPwd)) {
            return JunSConstants.JUNS_USER_FIND_PWD;
        }
        return userFindPwd;
    }

    public static void setUserFindPwd(String userFindPwd) {
        SDKData.userFindPwd = userFindPwd;
    }
}
