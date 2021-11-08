package com.juns.channel;

import android.content.Context;
import android.text.TextUtils;

import com.hio.sdk.HIOSDK;

public class AppUtils {
    /**
     * 获取oaid 方法
     *
     * @return 返回是否设备支持oaid
     */
    public static boolean getOaid(final Context context) {
        HIOMiitHelper miitHelper = new HIOMiitHelper(new HIOMiitHelper.AppIdsUpdater() {
            @Override
            public void OnIdsAvalid(String ids) {
                /*
                 * 该方法必须调用
                 *
                 */
                if (!TextUtils.isEmpty(ids)) {
                    HIOSDK.getInstance().setOAID(context, ids);
                }
            }
        });
        return miitHelper.getDeviceIds(context);
    }
}
