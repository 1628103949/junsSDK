package com.juns.sdk.core.http.params;

import android.text.TextUtils;

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
        url = JunSUrl.USER_LOGON,
        builder = CommonParamBuilder.class
)
public class UserLoginParam extends CommonParam {

    public UserLoginParam(String uName, String uPwd) {
        super();
        buildJunSInfo(uName, uPwd);
    }

    private void buildJunSInfo(String uName, String uPwd) {
        try {
            junSJson.put("uname", uName);
            junSJson.put("upwd", uPwd);
            junSJson.put("sign", buildSign(uName, uPwd));
            encryptGInfo(JunSUrl.USER_LOGON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
