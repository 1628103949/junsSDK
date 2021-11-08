package com.juns.sdk.core.api;

/**
 * Data：18/12/2018-2:54 PM
 * Author: ranger
 */
public class JunSConstants {

    public static final String SDK_VERSION = "1.0.7";
    public static final String OS_NAME = "1";
    public static final String OS_VER = android.os.Build.VERSION.RELEASE;
    public static final String JUNS_WEB_OBJ = "JunS";

    public static final String ENCRYPT_IV = "junshang";

    //角色信息相关
    public static final String SUBMIT_ROLE_ID = "roleId";
    public static final String SUBMIT_ROLE_NAME = "roleName";
    public static final String SUBMIT_ROLE_LEVEL = "roleLevel";
    public static final String SUBMIT_SERVER_ID = "serverId";
    public static final String SUBMIT_SERVER_NAME = "serverName";
    public static final String SUBMIT_BALANCE = "balance";
    public static final String SUBMIT_VIP = "vip";
    public static final String SUBMIT_PARTY_NAME = "partyName";
    public static final String SUBMIT_EXT = "ext";
    public static final String SUBMIT_CREATE_TIME = "createTime";
    public static final String SUBMIT_UP_TIME = "upTime";
    public static final String SUBMIT_LAST_ROLE_NAME = "lastRoleName";
    public static final String SUBMIT_TYPE = "submitType";

    public static final String SUBMIT_TYPE_CREATE = "roleCreate";
    public static final String SUBMIT_TYPE_ENTER = "roleEnter";
    public static final String SUBMIT_TYPE_UPGRADE = "roleUpgrade";
    public static final String SUBMIT_TYPE_UPDATE = "roleUpdate";

    //支付相关
    public static final String PAY_MONEY = "payMoney";
    public static final String PAY_ORDER_ID = "payOrderId";
    public static final String PAY_ORDER_NAME = "payOrderName";
    public static final String PAY_RATE = "payRate";
    public static final String PAY_EXT = "payExt";
    public static final String PAY_ROLE_ID = "payRoleId";
    public static final String PAY_ROLE_NAME = "payRoleName";
    public static final String PAY_ROLE_LEVEL = "payRoleLevel";
    public static final String PAY_SERVER_ID = "payServerId";
    public static final String PAY_SERVER_NAME = "payServerName";
    public static final String PAY_ROLE_VIP = "payRoleVip";
    public static final String PAY_ROLE_BALANCE = "payRoleBalance";
    public static final String PAY_M_ORDER_ID = "payMOrderId";
    public static final String PAY_M_DATA = "payMData";
    public static final String PAY_M_URL = "payMUrl";
    public static final String PAY_M_RATE = "payMRate";
    public static final String PAY_M_GOODS_NAME = "payGoodsName";
    public static final String PAY_M_GAME_NAME = "payGameName";

    //自营平台特有默认配置
    public static final String JUNS_USER_AGREEMENT = "http://junshanggame.com/uagree/uerService.html";
    public static final String JUNS_USER_FIND_PWD = "http://junshanggame.com/find/findAccount.html";

    /**
     * 返回码相关
     */
    public class Status {
        public static final int PAY_SUCC = 2000;
        public static final int PAY_UNKNOWN = 20001;
        public static final int SERVER_ERR = 10001;
        public static final int HTTP_ERR = 10002;
        public static final int CHANNEL_ERR = 10003;
        public static final int PARSE_ERR = 10004;
        public static final int SDK_ERR = 10005;
        public static final int USER_CANCEL = 10006;
        public static final int ANTI_ADDICTION = 1007;
    }

}
