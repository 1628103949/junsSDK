package com.juns.sdk.core.http.params;

import android.text.TextUtils;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.http.JunSUrl;
import com.juns.sdk.core.http.builder.CommonParamBuilder;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.common.TUitls;
import com.juns.sdk.framework.utils.DeviceUtils;
import com.juns.sdk.framework.utils.NetworkUtils;
import com.juns.sdk.framework.utils.ScreenUtils;
import com.juns.sdk.framework.xutils.http.annotation.HttpRequest;

import org.json.JSONException;

/**
 * Data：21/12/2018-11:03 AM
 * Author: ranger
 */
@HttpRequest(
        url = JunSUrl.ACTIVE,
        builder = CommonParamBuilder.class
)
public class ActiveParam extends CommonParam {

    public ActiveParam() {
        super();
        buildJunSInfo();
    }

    private void buildJunSInfo() {
        try {
            junSJson.put("resolution", ScreenUtils.getScreenWidth() + "x" + ScreenUtils.getScreenHeight());
            junSJson.put("model", DeviceUtils.getModel());
            junSJson.put("os", JunSConstants.OS_NAME);
            junSJson.put("sysver", JunSConstants.OS_VER);
            junSJson.put("language", TUitls.getLanguageCode());
            junSJson.put("brand", DeviceUtils.getManufacturer());
            junSJson.put("pkgid", SDKCore.getMainAct().getPackageName());
            //网络类型
            junSJson.put("ntype", TUitls.getNetWorkTypeName());
            //网络名称
            junSJson.put("nname", NetworkUtils.getNetworkOperatorName());

            if (SDKData.getSdkFirstActive()) {
                junSJson.put("install", "1");
            } else {
                junSJson.put("install", 0);
            }
            junSJson.put("sign", buildActiveSign());
            encryptGInfo(JunSUrl.ACTIVE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    String getJunSInfo() {
        if (junSJson == null) {
            return "";
        }
        if (junSJson.has("duid")) {
            junSJson.remove("duid");
        }
        return junSJson.toString();
    }

}
