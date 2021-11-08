package com.juns.sdk.framework.view.dialog.Attention;

import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.CycleInterpolator;

import com.juns.sdk.framework.view.dialog.animation.BaseAnimatorSet;

public class ShakeVertical extends BaseAnimatorSet {

	public ShakeVertical() {
		duration = 300;
	}
	@Override
	public void setAnimation(View view) {
		ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -10, 10);
		animator.setInterpolator(new CycleInterpolator(2));
		animatorSet.playTogether(animator);
	}

}
