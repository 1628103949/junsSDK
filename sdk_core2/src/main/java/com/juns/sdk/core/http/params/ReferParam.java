package com.juns.sdk.core.http.params;

import android.text.TextUtils;

import com.juns.sdk.core.http.JunSUrl;
import com.juns.sdk.core.http.builder.CommonParamBuilder;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.core.sdk.common.MiIdHelper;
import com.juns.sdk.framework.common.Dev;
import com.juns.sdk.framework.xutils.http.annotation.HttpRequest;

import org.json.JSONException;

/**
 * Data：21/12/2018-11:03 AM
 * Author: ranger
 */
@HttpRequest(
        url = JunSUrl.M_REFER,
        builder = CommonParamBuilder.class
)
public class ReferParam extends ReferCommonParam {

    public ReferParam(String adId) {
        super();
        buildJunSInfo(adId);
    }

    private void buildJunSInfo(String adId) {
        try {
            junSJson.put("padid", adId);
            junSJson.put("androidid", SDKData.getSdkAndroidId());
            junSJson.put("imei", Dev.getPhoneIMEI(SDKCore.getMainAct()));
            //junSJson.put("oaid", SDKData.getSdkOaid());
            //junSJson.put("padid", adId);
            junSJson.put("sign", buildSign(adId));
            encryptGInfo(JunSUrl.M_REFER);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
