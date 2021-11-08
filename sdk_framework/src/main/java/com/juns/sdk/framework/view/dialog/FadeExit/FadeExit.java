package com.juns.sdk.framework.view.dialog.FadeExit;

import android.animation.ObjectAnimator;
import android.view.View;

import com.juns.sdk.framework.view.dialog.animation.BaseAnimatorSet;

public class FadeExit extends BaseAnimatorSet {

	@Override
	public void setAnimation(View view) {
		animatorSet.playTogether(//
				ObjectAnimator.ofFloat(view, "alpha", 1, 0).setDuration(duration));
	}

}
