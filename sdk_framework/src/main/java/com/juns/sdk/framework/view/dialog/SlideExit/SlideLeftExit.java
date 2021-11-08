package com.juns.sdk.framework.view.dialog.SlideExit;

import android.animation.ObjectAnimator;
import android.util.DisplayMetrics;
import android.view.View;

import com.juns.sdk.framework.utils.ScreenUtils;
import com.juns.sdk.framework.view.dialog.animation.BaseAnimatorSet;


public class SlideLeftExit extends BaseAnimatorSet {

    @Override
    public void setAnimation(View view) {
        DisplayMetrics dm = view.getContext().getResources().getDisplayMetrics();
        animatorSet.playTogether(
                ObjectAnimator.ofFloat(view, "translationX", 0, -ScreenUtils.getScreenWidth()).setDuration(600));
    }

}
