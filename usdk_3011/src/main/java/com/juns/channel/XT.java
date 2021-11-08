package com.juns.channel;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLogoutEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.xiantu.open.AnnounceTimeCallBack;
import com.xiantu.open.OrderInfo;
import com.xiantu.open.PayCallback;
import com.xiantu.open.UpdateRoleInfo;
import com.xiantu.open.XTApiFactory;
import com.xiantu.open.XTExitObsv;
import com.xiantu.open.XTExitResult;
import com.xiantu.open.XTSDKInitObsv;
import com.xiantu.open.XTSDKInitResult;
import com.xiantu.open.XTUserObsv;
import com.xiantu.open.XTUserResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class XT extends OPlatformSDK {
    private static final String TAG = "XT";
    private static TNLog logger = LogFactory.getLog(TAG, true);

    public XT(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void onCreate(Activity mainAct) {
        super.onCreate(mainAct);
        // 第一个参数和第二个参数：渠道id和渠道名称，平台一般不会特殊要求，这里填写固定值“0”和“自然注册”即可
        //第三、第四个参数分别为： 游戏id、游戏名称
        // 第五、第六个参数：访问秘钥和联运SDK服务器地址平台提供
        XTApiFactory.getInstance().setParams("0", // 渠道id
                "自然注册",// 渠道名称
                "6713", // 游戏id(编号)
                "海底寻宝大作战",// 游戏名称
                "CB9ZCQYBWlgESAJIUApZGRdEXkk=",// 访问秘钥： (SDK访问服务器时的加密key)
                "https://api.3011.cn");// 联运SDK服务器地址
//        第三个参数为可选惨呼，true为开启debug模式输出SDK日志，false或不传默认不开启日志。

//        XTApiFactory.getInstance().init(this, initObsv);

        XTApiFactory.getInstance().init(mainAct, initObsv, true); //需要日志输出开启即可
        /**个人中心里面点击退出登录执行的退出回调接口,根据实际需要选择 **/
        XTApiFactory.getInstance().initExitFromPersonInfoParams(mCenterLogoutObsv);
        /**账号切换监听 **/
        XTApiFactory.getInstance().initSwitchSmallCallback(mSwitchSmallObsv);
    }

    @Override
    public void init(Activity activity) {
        Bus.getDefault().post(OInitEv.getSucc());

    }


    /**
     * 初始化回调接口
     */
    private XTSDKInitObsv initObsv = new XTSDKInitObsv() {
        @Override
        public void onInitFinish(XTSDKInitResult initResult) {
            if (initResult.mInitErrCode == XTSDKInitResult.GPInitErrorCodeNone) {

            } else {

            }
        }
    };

    /**
     * 个人中心退出登录/修改密码注销回调
     */
    private XTExitObsv mCenterLogoutObsv = new XTExitObsv() {
        @Override
        public void onExitFinish(XTExitResult xtExitResult) {
            switch (xtExitResult.mResultCode) {
                case XTExitResult.GPSDKResultCodeOfLogOffSucc:
                    Log.i(TAG, "注销回调：执行SDK注销登录成功");
                    // 下面是游戏退出逻辑，登出账号返回重新登录
                    Bus.getDefault().post(new OLogoutEv());
                    break;
                case XTExitResult.GPSDKResultCodeOfLogOffFail:
                    Log.i(TAG, "注销回调：执行SDK注销登录失败");
                    // 下面是游戏退出逻辑，登出账号返回重新登录
//                    XTApiFactory.getInstance().stopFloating(getApplicationContext());
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * 注销回调接口
     */
    private XTExitObsv logoutCallback = new XTExitObsv() {
        @Override
        public void onExitFinish(XTExitResult xtExitResult) {
            switch (xtExitResult.mResultCode) {
                case XTExitResult.GPSDKResultCodeOfLogOffSucc:
                    //注销成功
                    Log.i(TAG, "注销回调：执行SDK注销登录成功");
                    Bus.getDefault().post(new OLogoutEv());
                    // 下面是游戏退出逻辑，登出账号返回重新登录
                    break;
                case XTExitResult.GPSDKResultCodeOfLogOffFail:
                    Log.i(TAG, "注销回调：执行SDK注销登录失败");
                    // 下面是游戏退出逻辑，登出账号返回重新登录
                    Bus.getDefault().post(new OLogoutEv());
                    break;
            }
        }
    };

    /**
     * 账号切换回调
     */
    private XTExitObsv mSwitchSmallObsv = new XTExitObsv() {
        @Override
        public void onExitFinish(XTExitResult xtExitResult) {
            switch (xtExitResult.mResultCode) {
                case XTExitResult.GPSDKResultCodeOfSwitchSucc:
                    Log.i(TAG, "账号切换回调：执行SDK账号切换成功");
                    // 下面是游戏切换账号逻辑，如果无切换功能重启游戏即可
                    Bus.getDefault().post(new OLogoutEv());
                    break;
                case XTExitResult.GPSDKResultCodeOfSwitchFail:
                    Log.i(TAG, "账号切换回调：执行SDK账号切换失败");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void login(Activity activity) {
        XTApiFactory.getInstance().startLogin(activity,loginCallback );
    }
    /**
     * 登录回调接口
     */
    private XTUserObsv loginCallback = new XTUserObsv() {
        @Override
        public void onFinish(XTUserResult result) {
            switch (result.getmErrCode()) {
                case XTUserResult.USER_RESULT_LOGIN_FAIL:
                    Log.w(TAG, "登录失败");
                    break;
                case XTUserResult.USER_RESULT_LOGIN_SUCC:
                    Log.w(TAG, "登录成功");
                    String uid = result.getUid();//用户ID
                    String token = result.getToken();//用户token
                    String idCard = result.getIdCard();//用户身份证号（未实名返回空字符串）
                    String realName = result.getRealName();//用户真实姓名（未实名返回空字符串）
                    login2RSService(token,uid);
                    break;
            }
        }
    };

    private void login2RSService(String token,String uid) {
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("uid", uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OPlatformUtils.loginToServer(token, dataJson.toString());
    }

    @Override
    public void submitInfo(Activity mainAct, HashMap<String, String> submitInfo) {
        super.submitInfo(mainAct, submitInfo);
        if(submitInfo.get(JunSConstants.SUBMIT_TYPE).equals(JunSConstants.SUBMIT_TYPE_ENTER)){
            //登录成功后调用该接口回传游戏角色相关信息
            UpdateRoleInfo userPlayInfo = new UpdateRoleInfo();
            userPlayInfo.setServerid(submitInfo.get(JunSConstants.SUBMIT_SERVER_ID));//游戏服务器id
            userPlayInfo.setServername(submitInfo.get(JunSConstants.SUBMIT_SERVER_NAME));//服务器名称
            userPlayInfo.setRoleid(submitInfo.get(JunSConstants.SUBMIT_ROLE_ID));//角色id
            userPlayInfo.setRolename(submitInfo.get(JunSConstants.SUBMIT_ROLE_NAME));//角色名称
            userPlayInfo.setRolelevel(submitInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));//角色等级
            userPlayInfo.setRoleviplevel(submitInfo.get(JunSConstants.SUBMIT_VIP));//角色VIP等级
//        userPlayInfo.setRolegold("10万");//角色剩余金币
//        userPlayInfo.setRolediamond("6000");//角色剩余钻石
//        userPlayInfo.setRoleprofession("战士");//角色职业
//        userPlayInfo.setReincarnationlevel("1转");//转生等级
//        userPlayInfo.setCombat("1402512");//战力
            userPlayInfo.setExtend("");//附加信息json字符串格式
            XTApiFactory.getInstance().setUserPlayInfo(userPlayInfo, mainAct);
            //防沉迷接口
            XTApiFactory.getInstance().initAnnounceTimeCallBack(announceTimeCallBack);
        }

    }
    /**
     * 防沉迷时间回掉
     */
    private AnnounceTimeCallBack announceTimeCallBack = new AnnounceTimeCallBack() {
        @Override
        public void callback(String result) {
            Log.i(TAG, "result: " + result);
            if (TextUtils.isEmpty(result)) {
                return;
            }
            if ("0".equals(result)) {
                //此处用户满足防沉迷限制，无需处理
                Log.i(TAG, "用户满足防沉迷限制不做处理");
            }
            if ("1".equals(result)) {
                //此处限制用户游戏收益
                Log.i(TAG, "限制用户游戏收益");
            }
            if ("2".equals(result)) {
                //超过限定防沉迷时间，用户下线
                Log.i(TAG, "超过限定防沉迷时间，用户下线");
                //调用SDK注销接口退出游戏
                XTApiFactory.getInstance().loginout(SDKCore.getMainAct(), logoutCallback);
            }
            if ("3".equals(result)) {
                Log.e(TAG, "疲劳时间，用户下线！");
                //调用SDK注销接口退出游戏
                XTApiFactory.getInstance().loginout(SDKCore.getMainAct(), logoutCallback);
            }

        }
    };
    @Override
    public void logout(Activity mainAct) {
        XTApiFactory.getInstance().loginout(mainAct, logoutCallback);
    }

    @Override
    public void pay(Activity activity, HashMap<String, String> payParams) {
        double money = Double.parseDouble(payParams.get(JunSConstants.PAY_MONEY));
        OrderInfo order = new OrderInfo();
        order.setProductName(payParams.get(JunSConstants.PAY_ORDER_NAME));// 物品名称
        order.setProductDesc(payParams.get(JunSConstants.PAY_ORDER_NAME)); // 物品描述
        order.setProductPrice(money);//商品价格（单位元）
        order.setCpNumber(payParams.get(JunSConstants.PAY_M_ORDER_ID)); //游戏中的CP交易订单号，用于确认交易给玩家发送商品
        order.setGameServerId(payParams.get(JunSConstants.PAY_SERVER_ID));//游戏服务器ID
        order.setGameServerName(payParams.get(JunSConstants.PAY_SERVER_NAME));//游戏服务器名称
        order.setGameUserId(payParams.get(JunSConstants.PAY_ROLE_ID));//角色ID
        order.setGameUserName(payParams.get(JunSConstants.PAY_ROLE_NAME));//角色名称
        //Log.e("guoinfo",payParams.toString());
        XTApiFactory.getInstance().startPay(activity, order, payCallback);
    }

    /**
     * 支付结果回调
     */
    private PayCallback payCallback = new PayCallback() {
        @Override
        public void callback(String paycode) {
            if (!TextUtils.isEmpty(paycode)) {
                if (paycode.equals("1")) {
                    Bus.getDefault().post(OPayEv.getSucc(paycode));
                } else {
                    Bus.getDefault().post(OPayEv.getFail(2,paycode));
                }
                return;
            }
            Log.w(TAG, paycode);
        }
    };

    @Override
    public void exitGame(Activity activity) {
        XTApiFactory.getInstance().exitDialog(activity, mExitObsv);
    }

    /**
     * 返回键退出回调接口
     */
    private XTExitObsv mExitObsv = new XTExitObsv() {
        @Override
        public void onExitFinish(XTExitResult xtExitResult) {
            switch (xtExitResult.mResultCode) {
                case XTExitResult.GPSDKExitResultCodeError:
                    Log.e(TAG, "退出回调:调用退出弹框失败");
                    break;
                case XTExitResult.GPSDKExitResultCodeExitGame:
                    Log.e(TAG, "退出回调:退出方法回调");
                    // 关闭悬浮窗
//                    XTApiFactory.getInstance().stopFloating(MainActivity.this);
                    // 下面是退出逻辑,解决退出之后微信还在的问题
//                    // 你自己的退出逻辑，退出程序
                    Bus.getDefault().post(OExitEv.getSucc());
//                    finish();
//                    System.exit(0);
                    break;
            }
        }
    };

    @Override
    public void onStart(Activity mainAct) {
        super.onStart(mainAct);
        XTApiFactory.getInstance().onStart(mainAct);
    }

    @Override
    public void onRestart(Activity mainAct) {
        super.onRestart(mainAct);
        XTApiFactory.getInstance().onRestart(mainAct);
    }

    @Override
    public void onResume(Activity mainAct) {
        super.onResume(mainAct);
        XTApiFactory.getInstance().onResume(mainAct);
    }

    @Override
    public void onPause(Activity mainAct) {
        super.onPause(mainAct);
        XTApiFactory.getInstance().onPause(mainAct);
    }

    @Override
    public void onStop(Activity mainAct) {
        super.onStop(mainAct);
        XTApiFactory.getInstance().onStop(mainAct);
    }

    @Override
    public void onDestroy(Activity mainAct) {
        super.onDestroy(mainAct);
        XTApiFactory.getInstance().onDestroy(mainAct);
    }

}
