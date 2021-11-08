package com.juns.sdk.core.sdk.event;

import android.app.Activity;
import android.content.Intent;

/**
 * Data：2019/3/20-4:41 PM
 * Author: ranger
 */
public class OnActivityResult {
    private int requestCode;
    private int resultCode;
    private Intent data;
    private Activity activity;

    public OnActivityResult(int requestCode, int resultCode, Intent data, Activity act) {
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.data = data;
        this.activity = act;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Intent getData() {
        return data;
    }

    public Activity getActivity() {
        return activity;
    }
}
