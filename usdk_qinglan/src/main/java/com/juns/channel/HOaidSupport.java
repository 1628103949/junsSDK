package com.juns.channel;

import android.content.Context;

import com.hio.sdk.common.modle.OaidSupport;

public class HOaidSupport implements OaidSupport {
    private Context mContext;

    public HOaidSupport(Context context) {
        mContext = context;
    }

    @Override
    public boolean isSupport() {
        return AppUtils.getOaid(mContext);
    }
}
