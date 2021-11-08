package com.bswhl.hongbao.junshang.wxapi;

import android.app.Activity;

import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

public class WXBind {
    private static final String APP_ID = "wxed78829f303c1c08";
    // IWXAPI 是第三方app和微信通信的openApi接口
    public static IWXAPI api;
    public void bind(Activity activity){
        api = WXAPIFactory.createWXAPI(activity, APP_ID, true);
        // 将应用的appId注册到微信
        api.registerApp(APP_ID);
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test";
        api.sendReq(req);
    }
}
