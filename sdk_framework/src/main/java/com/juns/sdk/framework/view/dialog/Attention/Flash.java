package com.juns.sdk.framework.view.dialog.Attention;

import android.animation.ObjectAnimator;
import android.view.View;

import com.juns.sdk.framework.view.dialog.animation.BaseAnimatorSet;


public class Flash extends BaseAnimatorSet {

    public Flash() {
        duration = 1000;
    }

    @Override
    public void setAnimation(View view) {
        animatorSet.playTogether(//
                ObjectAnimator.ofFloat(view, "alpha", 1, 0, 1, 0, 1));
    }

}
