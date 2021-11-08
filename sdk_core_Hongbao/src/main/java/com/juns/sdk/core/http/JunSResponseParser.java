package com.juns.sdk.core.http;

import android.text.TextUtils;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.http.exception.JunSRealNameException;
import com.juns.sdk.core.http.exception.JunSServerException;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.framework.safe.JunSEncrypt;
import com.juns.sdk.framework.xutils.http.app.ResponseParser;
import com.juns.sdk.framework.xutils.http.request.UriRequest;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URL;

/**
 * Data：21/12/2018-10:45 AM
 * Author: ranger
 */
public class JunSResponseParser implements ResponseParser {

    String encryptKey;

    @Override
    public void checkResponse(UriRequest request) throws Throwable {
        // custom check
        // get headers
        URL mUrl = new URL(request.getRequestUri());
        encryptKey = mUrl.getHost();
    }

    @Override
    public Object parse(Type resultType, Class<?> resultClass, String result) throws Throwable {
        JunSResponse response = new JunSResponse();
        String jsonStr = JunSEncrypt.decryptDInfo(encryptKey, JunSConstants.ENCRYPT_IV, result.replace("\"", ""));
        SDKCore.logger.print("request --> " + jsonStr);
        JSONObject dataJson = new JSONObject(jsonStr);
        if (dataJson.has("state")) {
            response.state = dataJson.getInt("state");
        }
        if (dataJson.has("data")) {
            String dataString = dataJson.getString("data");
            if (!TextUtils.isEmpty(dataString) && !dataString.equals("[]") && !dataString.equals("\"\"")) {
                response.data = dataString;
            }
        }
        if (dataJson.has("msg")) {
            response.msg = dataJson.getString("msg");
        }

        if (response.state == 2) {
            throw new JunSRealNameException(response);
        }

        if (response.state == 0) {
            throw new JunSServerException(response);
        }
        return response;
    }
}
