package com.juns.sdk.core.http.params;

import android.text.TextUtils;

import com.juns.sdk.core.http.JunSUrl;
import com.juns.sdk.core.http.builder.CommonParamBuilder;
import com.juns.sdk.framework.xutils.http.annotation.HttpRequest;

import org.json.JSONException;

/**
 * Data：21/12/2018-11:07 AM
 * Author: ranger
 */
@HttpRequest(
        url = JunSUrl.M_VERIFY,
        builder = CommonParamBuilder.class
)
public class MVerifyParam extends CommonParam {

    public MVerifyParam(String token, String uid, String name, String data) {
        super();
        buildJunSInfo(token, uid, name, data);
    }

    private void buildJunSInfo(String token, String uid, String name, String data) {
        try {
            if (!TextUtils.isEmpty(token)) {
                junSJson.put("ptoken", token);
            }
            if (!TextUtils.isEmpty(uid)) {
                junSJson.put("puid", uid);
            }
            if (!TextUtils.isEmpty(name)) {
                junSJson.put("puname", name);
            }
            if (!TextUtils.isEmpty(data)) {
                junSJson.put("pdata", data);
            }
            junSJson.put("sign", buildSign());
            encryptGInfo(JunSUrl.M_VERIFY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
