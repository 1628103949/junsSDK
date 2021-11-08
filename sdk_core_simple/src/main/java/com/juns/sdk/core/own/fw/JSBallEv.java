package com.juns.sdk.core.own.fw;

/**
 * Data：07/03/2019-2:43 PM
 * Author: ranger
 */
public class JSBallEv {

    public static final int TO_SHOW = 2;
    public static final int TO_HIDE = 3;

    private int action;

    private JSBallEv(int act) {
        this.action = act;
    }

    public static JSBallEv getShow() {
        return new JSBallEv(TO_SHOW);
    }

    public static JSBallEv getHide() {
        return new JSBallEv(TO_HIDE);
    }

    public int getAction() {
        return action;
    }
}
