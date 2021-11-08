package com.juns.sdk.core.own.fw.view.listener;

import android.view.MotionEvent;

public interface BallTouchListener {

    void onDown(MotionEvent event);

    void onMove(MotionEvent event);

    void onUp(MotionEvent event);

}
