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
        url = JunSUrl.MOBILE_RESET_VERIFY,
        builder = CommonParamBuilder.class
)
public class PhoneResetVerifyParam extends CommonParam {

    public PhoneResetVerifyParam(String phone, String phoneCode) {
        super();
        buildJunSInfo(phone, phoneCode);
    }

    private void buildJunSInfo(String phone, String phoneCode) {
        try {
            junSJson.put("uname", phone);
            junSJson.put("scode", phoneCode);
            junSJson.put("token", SDKData.getSdkUserToken());
            junSJson.put("sign", buildSign(phone, phoneCode));
            encryptGInfo(JunSUrl.MOBILE_RESET_VERIFY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
