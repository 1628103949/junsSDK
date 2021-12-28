package com.juns.channel;

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.api.JunSSdkApi;
import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.params.ReferParam;
import com.juns.sdk.core.platform.OPlatformAPP;
import com.juns.sdk.core.platform.OPlatformBean;
import com.juns.sdk.core.platform.event.OPayEv;
import com.juns.sdk.core.sdk.SDKApplication;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.log.LogFactory;
import com.juns.sdk.framework.log.TNLog;
import com.juns.sdk.framework.view.common.ViewUtils;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.http.RequestParams;
import com.juns.sdk.framework.xutils.x;
import com.vivo.unionsdk.open.ChannelInfoCallback;
import com.vivo.unionsdk.open.MissOrderEventHandler;
import com.vivo.unionsdk.open.OrderResultInfo;
import com.vivo.unionsdk.open.VivoUnionSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VIVOAPP extends OPlatformAPP {
    private static final String TAG = "VIVOAPP";
    private static TNLog logger = LogFactory.getLog(TAG, true);
    //public static String vivoAppId = "103909852";

    public VIVOAPP(OPlatformBean bean) {
        super(bean);
    }

    @Override
    public void onCreate(Application application) {
        super.onCreate(application);

        VivoUnionSDK.initSdk(application, SDKApplication.getPlatformConfig().getExt1(), false);

    }
}
