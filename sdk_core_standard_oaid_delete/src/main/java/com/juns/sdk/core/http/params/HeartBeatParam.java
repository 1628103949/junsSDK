package com.juns.sdk.core.http.params;

import com.juns.sdk.core.http.JunSUrl;
import com.juns.sdk.core.http.builder.CommonParamBuilder;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.xutils.http.annotation.HttpRequest;

import org.json.JSONException;

/**
 * Data：21/12/2018-11:03 AM
 * Author: ranger
 */
@HttpRequest(
        url = JunSUrl.HEART_BEAT,
        builder = CommonParamBuilder.class
)
public class HeartBeatParam extends CommonParam {

    public HeartBeatParam(int polltime) {
        super();
        buildJunSInfo(polltime);
    }

    private void buildJunSInfo(int polltime) {
        try {
            junSJson.put("uname", SDKData.getSdkUserName());
            junSJson.put("uid", SDKData.getSdkUserId());
            junSJson.put("token", SDKData.getSdkUserToken());
            junSJson.put("polltime", polltime);
            junSJson.put("sign", buildSign(SDKData.getSdkUserId()));
            encryptGInfo(JunSUrl.HEART_BEAT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
