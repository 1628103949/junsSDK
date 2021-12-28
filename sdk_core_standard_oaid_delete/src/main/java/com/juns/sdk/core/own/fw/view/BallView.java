package com.juns.sdk.core.own.fw.view;

import android.app.Activity;
import android.view.MotionEvent;

import com.juns.sdk.framework.common.ResUtil;

public class BallView extends BallRootView {

    public BallView(Activity act) {
        super(act, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

}
