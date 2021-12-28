package com.juns.sdk.core.http.params;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.http.JunSUrl;
import com.juns.sdk.core.http.builder.CommonParamBuilder;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.utils.AppUtils;
import com.juns.sdk.framework.utils.EncryptUtils;
import com.juns.sdk.framework.xutils.http.annotation.HttpRequest;

import org.json.JSONException;

/**
 * Data：22/01/2019-10:37 AM
 * Author: ranger
 */
@HttpRequest(
        url = JunSUrl.MPAY_QUERY,
        builder = CommonParamBuilder.class
)
public class MPayQueryParam extends CommonParam {

    public MPayQueryParam() {
        super();
        buildJunSInfo();
    }

    private void buildJunSInfo() {
        try {
            junSJson.put("token", SDKData.getSdkUserToken());
            junSJson.put("uid", SDKData.getSdkUserId());
            junSJson.put("uname", SDKData.getSdkUserName());
            junSJson.put("uuid", SDKData.getCurrentMPayOrder());
            junSJson.put("sign", buildSign(SDKData.getSdkUserId(), SDKData.getCurrentMPayOrder()));
            encryptGInfo(JunSUrl.MPAY_QUERY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String querySign(String... args) {
        StringBuilder preSignSb = new StringBuilder();
        preSignSb.append(SDKData.getSdkPID());
        preSignSb.append(SDKData.getSdkGID());
        preSignSb.append(SDKData.getSdkRefer());
        preSignSb.append(AppUtils.getAppVersionName(SDKCore.getMainAct().getPackageName()));
        preSignSb.append(JunSConstants.SDK_VERSION);
        preSignSb.append(String.valueOf(System.currentTimeMillis() / 1000));
        preSignSb.append(SDKData.getSdkAppKey());
        for (String arg : args) {
            preSignSb.append(arg);
        }
        return EncryptUtils.encryptMD5ToString(preSignSb.toString()).toLowerCase();
    }
}
