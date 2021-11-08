package com.juns.sdk.core.http.params;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.http.JunSUrl;
import com.juns.sdk.core.http.builder.CommonParamBuilder;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.xutils.http.annotation.HttpRequest;

import org.json.JSONException;

import java.util.HashMap;

/**
 * Data：22/01/2019-10:37 AM
 * Author: ranger
 */
@HttpRequest(
        url = JunSUrl.MPAY_ORDER,
        builder = CommonParamBuilder.class
)
public class MPayParam extends CommonParam {

    public MPayParam(HashMap<String, String> payInfo) {
        super();
        buildJunSInfo(payInfo);
    }

    private void buildJunSInfo(HashMap<String, String> payInfo) {
        try {
            junSJson.put("doid", payInfo.get(JunSConstants.PAY_ORDER_ID));
            junSJson.put("dsid", payInfo.get(JunSConstants.PAY_SERVER_ID));
            junSJson.put("dsname", payInfo.get(JunSConstants.PAY_SERVER_NAME));
            junSJson.put("dext", payInfo.get(JunSConstants.PAY_EXT));
            junSJson.put("drid", payInfo.get(JunSConstants.PAY_ROLE_ID));
            junSJson.put("drname", payInfo.get(JunSConstants.PAY_ROLE_NAME));
            junSJson.put("drlevel", payInfo.get(JunSConstants.PAY_ROLE_LEVEL));
            junSJson.put("dmoney", payInfo.get(JunSConstants.PAY_MONEY));
            junSJson.put("dradio", payInfo.get(JunSConstants.PAY_RATE));
            junSJson.put("uid", SDKData.getSdkUserId());
            junSJson.put("uname", SDKData.getSdkUserName());
            junSJson.put("pdata", payInfo.get(JunSConstants.PAY_M_DATA));
            junSJson.put("sign", buildSign(payInfo.get(JunSConstants.PAY_ORDER_ID), payInfo.get(JunSConstants.PAY_SERVER_ID), SDKData.getSdkUserId(), SDKData.getSdkUserName()));
            encryptGInfo(JunSUrl.MPAY_ORDER);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
