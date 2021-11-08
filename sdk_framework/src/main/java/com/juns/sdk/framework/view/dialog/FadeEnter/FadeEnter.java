package com.juns.sdk.framework.view.dialog.FadeEnter;

import android.animation.ObjectAnimator;
import android.view.View;

import com.juns.sdk.framework.view.dialog.animation.BaseAnimatorSet;

public class FadeEnter extends BaseAnimatorSet {

	@Override
	public void setAnimation(View view) {
		animatorSet.playTogether(//
				ObjectAnimator.ofFloat(view, "alpha", 0, 1).setDuration(duration));
	}

}
