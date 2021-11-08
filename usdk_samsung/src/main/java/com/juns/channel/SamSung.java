package com.juns.channel;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.OPlatformSDK;
import com.juns.sdk.core.platform.OPlatformUtils;
import com.juns.sdk.core.platform.event.OExitEv;
import com.juns.sdk.core.platform.event.OInitEv;
import com.juns.sdk.core.platform.event.OLoginEv;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.xbus.Bus;
import com.samsung.interfaces.callback.ILoginResultCallback;
import com.samsung.interfaces.callback.INameAuthResultCallback;
import com.samsung.interfaces.callback.IPayResultCallback;
import com.samsung.sdk.main.IAppPay;
import com.samsung.sdk.main.IAppPayOrderUtils;
import com.samsung.sdk.notice.SamsungNoticeSdk;
import com.samsung.sdk.notice.callback.SamsungNoticeQuitCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class SamSung extends OPlatformSDK {
    private static final String TAG = "SamSung";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    private String acID = "999";
    public SamSung(OPlatformBean pBean) {
        super(pBean);
    }

    @Override
    public void init(Activity activity) {
        if(SDKApplication.getPlatformConfig().getExt3().equals("0")){
            IAppPay.init(activity,IAppPay.LANDSCAPE, SDKApplication.getPlatformConfig().getExt1(),acID);
        }else{
            IAppPay.init(activity,IAppPay.PORTRAIT, SDKApplication.getPlatformConfig().getExt1(),acID);
        }
        //IAppPay.init(activity,IAppPay.LANDSCAPE, SDKApplication.getPlatformConfig().getExt1(),acID);
        Bus.getDefault().post(OInitEv.getSucc());
    }
    String loginParams = "";
    @Override
    public void login(final Activity activity) {
        loginParams = SDKData.getLoginParams();
        logger.print(SDKData.getLoginParams());
        IAppPay.startLogin(activity, loginParams, new ILoginResultCallback() {
            @Override
            public void onSuccess(String signValue, Map<String, String> map) {
                login2RSService(activity,signValue);
            }

            @Override
            public void onFaild(String s, String s1) {
                logger.print("三星登录失败"+s+s1);
                 Bus.getDefault().post(OLoginEv.getFail(2,s+s1));
            }

            @Override
            public void onCanceled() {
                logger.print("三星登录取消");
                Bus.getDefault().post(OLoginEv.getFail(2,"cancel"));
            }
        });
    }

    private void login2RSService(Context ctx, String signValue) {
        JSONObject dataJson = new JSONObject();
        OPlatformUtils.loginToServer(signValue, dataJson.toString());
    }

    @Override
    public void logout(Activity mainAct) {

    }

    @Override
    public void pay(final Activity activity, HashMap<String, String> payParams) {
        String orderParam = "";
        try {
            JSONObject jsonData = new JSONObject(payParams.get(JunSConstants.PAY_M_DATA));
            try {
                orderParam = "transid="+ URLEncoder.encode(jsonData.getString("transid"),"UTF-8")+"&"+"appid="+URLEncoder.encode(SDKApplication.getPlatformConfig().getExt1(),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
           logger.print(orderParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        IAppPay.startPay(activity, loginParams, orderParam, new IPayResultCallback() {
            @Override
            public void onPayResult(int resultCode, String signValue, String resultInfo) {
                switch (resultCode) {
                    case IAppPay.PAY_SUCCESS:
                        //调用 IAppPayOrderUtils 的验签方法进行支付结果验证
                        boolean payState = IAppPayOrderUtils.checkPayResult(signValue, SDKApplication.getPlatformConfig().getExt2());
                        if (payState) {
                            Bus.getDefault().post(OPayEv.getSucc("pay success."));
                            //Toast.makeText(activity, "支付成功", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case IAppPay.PAY_CANCEL:
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.USER_CANCEL, "user cancel."));
                        //Toast.makeText(activity, "支付取消", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Bus.getDefault().post(OPayEv.getFail(JunSConstants.Status.PAY_UNKNOWN, "unknown"));
                        //Toast.makeText(activity, resultInfo, Toast.LENGTH_LONG).show();
                        break;
                }
                Log.d(TAG, "resultCode:" + resultCode + ",signValue:" + signValue + ",resultInfo:" + resultInfo);
            }
        });
    }

    @Override
    public void exitGame(final Activity activity) {

        SamsungNoticeSdk.showQuitNotice(activity, new SamsungNoticeQuitCallback() {
            @Override
            public void noticeQuitCallBack(int i, String s) {
                if(i == SamsungNoticeSdk.NOTICE_QUIT){
                    Bus.getDefault().post(OExitEv.getSucc());
                }else if(i == SamsungNoticeSdk.NOTICE_CANCEL){

                }
            }
        });
    }

}
