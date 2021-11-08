package com.juns.sdk.core.own.submit;

import android.text.TextUtils;
import android.util.Log;

import com.juns.sdk.core.api.JunSConstants;
import com.juns.sdk.core.http.JunSResponse;
import com.juns.sdk.core.http.params.EventSubmitParam;
import com.juns.sdk.core.sdk.JunSUtils;
import com.juns.sdk.core.sdk.SDKCore;
import com.juns.sdk.core.sdk.common.NoticeDialog;
import com.juns.sdk.core.sdk.event.EvSubmit;
import com.juns.sdk.core.sdk.event.EvSubmitLog;
import com.juns.sdk.framework.xbus.Bus;
import com.juns.sdk.framework.xutils.common.Callback;
import com.juns.sdk.framework.xutils.x;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Data：09/01/2019-3:04 PM
 * Author: ranger
 */
public class JunSSubmit {

    private NoticeDialog loginNoticeDialog;
    public void doSubmit(HashMap<String, String> eventInfo) {
        EventSubmitParam eventSubmitParam = new EventSubmitParam(eventInfo);
        x.http().post(eventSubmitParam, new Callback.CommonCallback<JunSResponse>() {
            @Override
            public void onSuccess(final JunSResponse result) {
               // Log.e("TAG", "onSuccess: "+result.state);
                String data = result.data;
                try {
                    JSONObject dataJson = new JSONObject(data);
                    if (dataJson.has("nurl")) {
                        String noticeUrl = JunSUtils.buildCommonWebUrl(dataJson.getString("nurl"), true);
                        if (!TextUtils.isEmpty(noticeUrl)) {
                            if (loginNoticeDialog != null && loginNoticeDialog.isShowing()) {
                                loginNoticeDialog.dismiss();
                            }
                            loginNoticeDialog = null;
                            loginNoticeDialog = new NoticeDialog(SDKCore.getMainAct(), noticeUrl, new NoticeDialog.NoticeCallback() {
                                @Override
                                public void onFinish() {

                                }
                            });
                            loginNoticeDialog.show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (result.state == 0) {
                    //服务端返回数据为空
                    Bus.getDefault().post(EvSubmit.getFail(JunSConstants.Status.SERVER_ERR, "result data is null."));
                    return;
                }
                Bus.getDefault().post(EvSubmit.getSucc());
            }

            @Override
            public void onError(final Throwable ex, boolean isOnCallback) {
                //Log.e("TAG", "onError: "+ex.getLocalizedMessage()+isOnCallback);
                Bus.getDefault().post(EvSubmit.getFail(2,"submit fail"));
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    public void doSubmitLog(HashMap<String, String> eventInfo) {
        EventSubmitParam eventSubmitParam = new EventSubmitParam(eventInfo);
        x.http().post(eventSubmitParam, new Callback.CommonCallback<JunSResponse>() {
            @Override
            public void onSuccess(final JunSResponse result) {
                Log.e("TAG", "onSuccess: "+result.state);
                if (result.state == 0) {
                    //服务端返回数据为空
                    Bus.getDefault().post(EvSubmitLog.getFail(JunSConstants.Status.SERVER_ERR,"submit fail"));
                    //Bus.getDefault().post(EvSubmit.getFail(JunSConstants.Status.SERVER_ERR, "result data is null."));
                    return;
                }

                Bus.getDefault().post(EvSubmitLog.getSucc());
            }

            @Override
            public void onError(final Throwable ex, boolean isOnCallback) {
                Log.e("TAG", "onError: "+ex.getLocalizedMessage()+isOnCallback);
                Bus.getDefault().post(EvSubmitLog.getFail(2,"submit fail"));
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }
}
