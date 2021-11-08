package com.juns.sdk.core.http.params;

import android.util.Log;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.http.JunSUrl;
import com.juns.sdk.core.http.builder.CommonParamBuilder;
import com.juns.sdk.core.sdk.SDKData;
import com.juns.sdk.framework.xutils.http.annotation.HttpRequest;

import org.json.JSONException;

import java.util.HashMap;

/**
 * Data：21/12/2018-11:03 AM
 * Author: ranger
 */
@HttpRequest(
        url = JunSUrl.EVENT_SUBMIT,
        builder = CommonParamBuilder.class
)
public class EventSubmitParam extends CommonParam {

    public EventSubmitParam(HashMap<String, String> eventInfo) {
        super();
        buildJunSInfo(eventInfo);
    }

    private void buildJunSInfo(HashMap<String, String> eventInfo) {
        try {
            //Log.e("guotest","111"+eventInfo.get(JunSConstants.SUBMIT_TYPE));
            String event = "";
            switch (eventInfo.get(JunSConstants.SUBMIT_TYPE)){
                case JunSConstants.SUBMIT_TYPE_CREATE:
                    event = "create";
                    break;
                case JunSConstants.SUBMIT_TYPE_ENTER:
                    event = "enter";
                    break;
                case JunSConstants.SUBMIT_TYPE_UPGRADE:
                    event = "levelup";
                    break;
                case JunSConstants.SUBMIT_TYPE_UPDATE:
                    event = "levelup";
                    break;
            }
            if(!event.equals("")){
                junSJson.put("uname", SDKData.getSdkUserName());
                junSJson.put("uid", SDKData.getSdkUserId());
                junSJson.put("dsid", eventInfo.get(JunSConstants.SUBMIT_SERVER_ID));
                junSJson.put("event", event);
                junSJson.put("dsname", eventInfo.get(JunSConstants.SUBMIT_SERVER_NAME));
                junSJson.put("drid", eventInfo.get(JunSConstants.SUBMIT_ROLE_ID));
                junSJson.put("drname", eventInfo.get(JunSConstants.SUBMIT_ROLE_NAME));
                junSJson.put("drlevel", eventInfo.get(JunSConstants.SUBMIT_ROLE_LEVEL));
                junSJson.put("ext", eventInfo.get(JunSConstants.SUBMIT_EXT));
                // junSJson.put("polltime", polltime);
                junSJson.put("sign", buildSign());
            }else{

                if(SDKData.getSdkUserName() == null){
                    junSJson.put("uname", "");
                    junSJson.put("uid", "");
                }else{
                    junSJson.put("uname", SDKData.getSdkUserName());
                    junSJson.put("uid", SDKData.getSdkUserId());
                }


                junSJson.put("event", eventInfo.get(JunSConstants.SUBMIT_TYPE));
                junSJson.put("ext", eventInfo.get(JunSConstants.SUBMIT_EXT));
                // junSJson.put("polltime", polltime);
                junSJson.put("sign", buildSign());
            }

            encryptGInfo(JunSUrl.EVENT_SUBMIT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
