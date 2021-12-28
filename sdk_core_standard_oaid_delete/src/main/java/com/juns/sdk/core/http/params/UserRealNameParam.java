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
        url = JunSUrl.USER_REAL_NAME,
        builder = CommonParamBuilder.class
)
public class UserRealNameParam extends CommonParam {

    public UserRealNameParam(String name, String idCard) {
        super();
        buildJunSInfo(name, idCard);
    }

    private void buildJunSInfo(String name, String idCard) {
        try {
            junSJson.put("uname", SDKData.getSdkUserName());
            junSJson.put("uid", SDKData.getSdkUserId());
            junSJson.put("token", SDKData.getSdkUserToken());
            junSJson.put("name", name);
            junSJson.put("id_card_number", idCard);
            junSJson.put("sign", buildSign(SDKData.getSdkUserName(), SDKData.getSdkUserId(), idCard));
            encryptGInfo(JunSUrl.USER_REAL_NAME);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
