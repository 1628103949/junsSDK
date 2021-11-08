package com.juns.sdk.core.http.params;

import com.juns.sdk.core.http.JunSUrl;
import com.juns.sdk.core.http.builder.CommonParamBuilder;
import com.juns.sdk.framework.xutils.http.annotation.HttpRequest;

import org.json.JSONException;

/**
 * Data：21/12/2018-11:03 AM
 * Author: ranger
 */
@HttpRequest(
        url = JunSUrl.MOBILE_CODE,
        builder = CommonParamBuilder.class
)
public class PhoneCodeParam extends CommonParam {

    public PhoneCodeParam(String phone) {
        super();
        buildJunSInfo(phone);
    }

    private void buildJunSInfo(String phone) {
        try {
            junSJson.put("uname", phone);
            junSJson.put("sign", buildSign(phone));
            encryptGInfo(JunSUrl.MOBILE_CODE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
