package com.android.hhuimie.hmusic.slideexpandableorpinned;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class ExpandCollapseAnimation extends Animation {
	private View mAnimatedView;
	private int mEndHeight;
	private int mType;
	public final static int COLLAPSE = 1;
	public final static int EXPAND = 0;
	private LinearLayout.LayoutParams mLayoutParams;

	public ExpandCollapseAnimation(View view, int type) {
		mAnimatedView = view;
		mEndHeight = mAnimatedView.getMeasuredHeight();
		mLayoutParams = ((LinearLayout.LayoutParams) view.getLayoutParams());
		mType = type;
		if (mType == EXPAND) {
			mLayoutParams.bottomMargin = -mEndHeight;
		} else {
			mLayoutParams.bottomMargin = 0;
		}
		view.setVisibility(View.VISIBLE);
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		super.applyTransformation(interpolatedTime, t);
		if (interpolatedTime < 1.0f) {
			if (mType == EXPAND) {
				mLayoutParams.bottomMargin = -mEndHeight + (int) (mEndHeight * interpolatedTime);
			} else {
				mLayoutParams.bottomMargin = -(int) (mEndHeight * interpolatedTime);
			}
			Log.d("ExpandCollapseAnimation", "anim height " + mLayoutParams.bottomMargin);
			mAnimatedView.requestLayout();
		} else {
			if (mType == EXPAND) {
				mLayoutParams.bottomMargin = 0;
				mAnimatedView.requestLayout();
			} else {
				mLayoutParams.bottomMargin = -mEndHeight;
				mAnimatedView.setVisibility(View.GONE);
				mAnimatedView.requestLayout();
			}
		}
	}
}
