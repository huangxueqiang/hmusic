package com.android.hhuimie.hmusic.ResideMenu;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.android.hhuimie.hmusic.R;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;


public class ResideMenu extends FrameLayout {

	public static final int DIRECTION_LEFT = 0;
	public static final int DIRECTION_RIGHT = 1;
	private static final int PRESSED_MOVE_HORIZONTAL = 2;
	private static final int PRESSED_DOWN = 3;
	private static final int PRESSED_DONE = 4;
	private static final int PRESSED_MOVE_VERTICAL = 5;
	List<ResideMenuItem> mMainItems;
	List<ResideMenuItem> mPlayItems;
	private ImageView mImageViewShadow;
	private ImageView mImageViewBackground;
	private LinearLayout mLayoutLeftMenu;
	private LinearLayout mLayoutRightMenu;
	private ScrollView mScrollViewLeftMenu;
	private ScrollView mScrollViewRightMenu;
	private ScrollView mScrollViewMenu;
	private Activity mActivity;
	private ViewGroup mViewGroup;
	private TouchDisableView mViewActivity;
	private boolean mIsOpened;
	private float mShadowAdjustScaleX;
	private float mShadowAdjustScaleY;
	private List<View> mIgnoredViews;


	private DisplayMetrics mDisplayMetrics = new DisplayMetrics();
	private OnMenuListener mMenuListener;
	private float mLastRawX;
	private boolean mIsInIgnoredView = false;
	private int mScaleDirection = DIRECTION_LEFT;
	private int mPressedState = PRESSED_DOWN;

	private float mScaleValue = 0.65f;
	private Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			if (isOpened()) {
				showScrollViewMenu(mScrollViewMenu);
				if (mMenuListener != null)
					mMenuListener.openMenu();
			}
		}

		@Override
		public void onAnimationEnd(Animator animation) {

			if (isOpened()) {
				mViewActivity.setTouchDisable(true);
				mViewActivity.setOnClickListener(viewActivityOnClickListener);
			} else {
				mViewActivity.setTouchDisable(false);
				mViewActivity.setOnClickListener(null);
				hideScrollViewMenu(mScrollViewLeftMenu);
				hideScrollViewMenu(mScrollViewRightMenu);
				if (mMenuListener != null) {
					mMenuListener.closeMenu();
				}
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	};
	private OnClickListener viewActivityOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if (isOpened()) closeMenu();
		}
	};
	private float lastActionDownX, lastActionDownY;


	public ResideMenu(Context context) {
		super(context);
		initViews(context);
	}

	private void initViews(Context context) {
		LayoutInflater inflater = (LayoutInflater)
				                          context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.residemenu, this);
		mScrollViewLeftMenu = (ScrollView) findViewById(R.id.sv_left_menu);
		mScrollViewRightMenu = (ScrollView) findViewById(R.id.sv_right_menu);
		mImageViewShadow = (ImageView) findViewById(R.id.iv_shadow);
		mLayoutLeftMenu = (LinearLayout) findViewById(R.id.layout_left_menu);
		mLayoutRightMenu = (LinearLayout) findViewById(R.id.layout_right_menu);
		mImageViewBackground = (ImageView) findViewById(R.id.iv_background);
	}

	@Override
	protected boolean fitSystemWindows(Rect insets) {


		this.setPadding(mViewActivity.getPaddingLeft() + insets.left, mViewActivity.getPaddingTop() + insets.top,
				               mViewActivity.getPaddingRight() + insets.right, mViewActivity.getPaddingBottom() + insets.bottom);
		insets.left = insets.top = insets.right = insets.bottom = 0;
		return true;
	}

	public void attachToActivity(Activity activity) {
		initValue(activity);
		setShadowAdjustScaleXByOrientation();
		mViewGroup.addView(this, 0);
	}

	private void initValue(Activity activity) {
		this.mActivity = activity;
		mPlayItems = new ArrayList<>();
		mMainItems = new ArrayList<>();
		mIgnoredViews = new ArrayList<>();
		mViewGroup = (ViewGroup) activity.getWindow().getDecorView();
		mViewActivity = new TouchDisableView(this.mActivity);

		View mContent = mViewGroup.getChildAt(0);
		mViewGroup.removeViewAt(0);
		mViewActivity.setContent(mContent);
		addView(mViewActivity);

		ViewGroup parent = (ViewGroup) mScrollViewLeftMenu.getParent();
		parent.removeView(mScrollViewLeftMenu);
		parent.removeView(mScrollViewRightMenu);
	}

	private void setShadowAdjustScaleXByOrientation() {
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mShadowAdjustScaleX = 0.034f;
			mShadowAdjustScaleY = 0.12f;
		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			mShadowAdjustScaleX = 0.06f;
			mShadowAdjustScaleY = 0.07f;
		}
	}

	public void setBackground(int imageResource) {
		mImageViewBackground.setImageResource(imageResource);
	}

	public void addMenuItem(ResideMenuItem menuItem, int direction) {
		if (direction == DIRECTION_LEFT) {
			mLayoutLeftMenu.addView(menuItem);
		} else {
			mLayoutRightMenu.addView(menuItem);
		}
	}

	public void resetmenu(int where) {
		mLayoutRightMenu.removeAllViews();
		if (where == 0) {
			for (ResideMenuItem item : mMainItems)
				mLayoutRightMenu.addView(item);
		} else if (where == 1) {
			for (ResideMenuItem item : mPlayItems) {
				mLayoutRightMenu.addView(item);
			}
		}
	}

	public void addPlayItem(ResideMenuItem menuItem) {
		mPlayItems.add(menuItem);
		mLayoutRightMenu.addView(menuItem);
	}

	public void addMainItem(ResideMenuItem menuItem) {
		mMainItems.add(menuItem);
	}

	public void setMenuListener(OnMenuListener menuListener) {
		this.mMenuListener = menuListener;
	}

	public void openMenu(int direction) {

		setScaleDirection(direction);

		mIsOpened = true;
		AnimatorSet scaleDown_activity = buildScaleDownAnimation(mViewActivity, mScaleValue, mScaleValue);
		AnimatorSet scaleDown_shadow = buildScaleDownAnimation(mImageViewShadow,
				                                                      mScaleValue + mShadowAdjustScaleX, mScaleValue + mShadowAdjustScaleY);
		AnimatorSet alpha_menu = buildMenuAnimation(mScrollViewMenu, 1.0f);
		scaleDown_shadow.addListener(animationListener);
		scaleDown_activity.playTogether(scaleDown_shadow);
		scaleDown_activity.playTogether(alpha_menu);
		scaleDown_activity.start();
	}

	public void closeMenu() {

		mIsOpened = false;
		AnimatorSet scaleUp_activity = buildScaleUpAnimation(mViewActivity, 1.0f, 1.0f);
		AnimatorSet scaleUp_shadow = buildScaleUpAnimation(mImageViewShadow, 1.0f, 1.0f);
		AnimatorSet alpha_menu = buildMenuAnimation(mScrollViewMenu, 0.0f);
		scaleUp_activity.addListener(animationListener);
		scaleUp_activity.playTogether(scaleUp_shadow);
		scaleUp_activity.playTogether(alpha_menu);
		scaleUp_activity.start();
	}

	private void setScaleDirection(int direction) {

		int screenWidth = getScreenWidth();
		float pivotX;
		float pivotY = getScreenHeight() * 0.5f;

		if (direction == DIRECTION_LEFT) {
			mScrollViewMenu = mScrollViewLeftMenu;
			pivotX = screenWidth * 1.5f;
		} else {
			mScrollViewMenu = mScrollViewRightMenu;
			pivotX = screenWidth * -0.5f;
		}

		ViewHelper.setPivotX(mViewActivity, pivotX);
		ViewHelper.setPivotY(mViewActivity, pivotY);
		ViewHelper.setPivotX(mImageViewShadow, pivotX);
		ViewHelper.setPivotY(mImageViewShadow, pivotY);
		mScaleDirection = direction;
	}

	public boolean isOpened() {
		return mIsOpened;
	}

	private AnimatorSet buildScaleDownAnimation(View target, float targetScaleX, float targetScaleY) {

		AnimatorSet scaleDown = new AnimatorSet();
		scaleDown.playTogether(
				                      ObjectAnimator.ofFloat(target, "scaleX", targetScaleX),
				                      ObjectAnimator.ofFloat(target, "scaleY", targetScaleY)
		);

		scaleDown.setInterpolator(AnimationUtils.loadInterpolator(mActivity,
				                                                         android.R.anim.decelerate_interpolator));
		scaleDown.setDuration(250);
		return scaleDown;
	}

	private AnimatorSet buildScaleUpAnimation(View target, float targetScaleX, float targetScaleY) {

		AnimatorSet scaleUp = new AnimatorSet();
		scaleUp.playTogether(
				                    ObjectAnimator.ofFloat(target, "scaleX", targetScaleX),
				                    ObjectAnimator.ofFloat(target, "scaleY", targetScaleY)
		);

		scaleUp.setDuration(250);
		return scaleUp;
	}

	private AnimatorSet buildMenuAnimation(View target, float alpha) {

		AnimatorSet alphaAnimation = new AnimatorSet();
		alphaAnimation.playTogether(
				                           ObjectAnimator.ofFloat(target, "alpha", alpha)
		);

		alphaAnimation.setDuration(250);
		return alphaAnimation;
	}

	public void addIgnoredView(View v) {
		mIgnoredViews.add(v);
	}

	private boolean isInIgnoredView(MotionEvent ev) {
		Rect rect = new Rect();
		for (View v : mIgnoredViews) {
			v.getGlobalVisibleRect(rect);
			if (rect.contains((int) ev.getX(), (int) ev.getY()))
				return true;
		}
		return false;
	}

	private void setScaleDirectionByRawX(float currentRawX) {
		if (currentRawX < mLastRawX)
			setScaleDirection(DIRECTION_RIGHT);
		else
			setScaleDirection(DIRECTION_LEFT);
	}

	private float getTargetScale(float currentRawX) {
		float scaleFloatX = ((currentRawX - mLastRawX) / getScreenWidth()) * 0.75f;
		scaleFloatX = mScaleDirection == DIRECTION_RIGHT ? -scaleFloatX : scaleFloatX;

		float targetScale = ViewHelper.getScaleX(mViewActivity) - scaleFloatX;
		targetScale = targetScale > 1.0f ? 1.0f : targetScale;
		targetScale = targetScale < 0.5f ? 0.5f : targetScale;
		return targetScale;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		float currentActivityScaleX = ViewHelper.getScaleX(mViewActivity);
		if (currentActivityScaleX == 1.0f)
			setScaleDirectionByRawX(ev.getRawX());

		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				lastActionDownX = ev.getX();
				lastActionDownY = ev.getY();
				mIsInIgnoredView = isInIgnoredView(ev) && !isOpened();
				mPressedState = PRESSED_DOWN;
				break;

			case MotionEvent.ACTION_MOVE:
				if (mIsInIgnoredView)
					break;

				if (mPressedState != PRESSED_DOWN &&
						    mPressedState != PRESSED_MOVE_HORIZONTAL)
					break;

				int xOffset = (int) (ev.getX() - lastActionDownX);
				int yOffset = (int) (ev.getY() - lastActionDownY);

				if (mPressedState == PRESSED_DOWN) {
					if (yOffset > 25 || yOffset < -25) {
						mPressedState = PRESSED_MOVE_VERTICAL;
						break;
					}
					if (xOffset < -50 || xOffset > 50) {
						mPressedState = PRESSED_MOVE_HORIZONTAL;
						ev.setAction(MotionEvent.ACTION_CANCEL);
					}
				} else if (mPressedState == PRESSED_MOVE_HORIZONTAL) {
					if (currentActivityScaleX < 0.95)
						showScrollViewMenu(mScrollViewMenu);

					float targetScale = getTargetScale(ev.getRawX());
					ViewHelper.setScaleX(mViewActivity, targetScale);
					ViewHelper.setScaleY(mViewActivity, targetScale);
					ViewHelper.setScaleX(mImageViewShadow, targetScale + mShadowAdjustScaleX);
					ViewHelper.setScaleY(mImageViewShadow, targetScale + mShadowAdjustScaleY);
					ViewHelper.setAlpha(mScrollViewMenu, (1 - targetScale) * 2.0f);

					mLastRawX = ev.getRawX();
					return true;
				}

				break;

			case MotionEvent.ACTION_UP:

				if (mIsInIgnoredView) break;
				if (mPressedState != PRESSED_MOVE_HORIZONTAL) break;

				mPressedState = PRESSED_DONE;
				if (isOpened()) {
					if (currentActivityScaleX > 0.56f)
						closeMenu();
					else
						openMenu(mScaleDirection);
				} else {
					if (currentActivityScaleX < 0.94f) {
						openMenu(mScaleDirection);
					} else {
						closeMenu();
					}
				}

				break;

		}
		mLastRawX = ev.getRawX();
		return super.dispatchTouchEvent(ev);
	}

	public int getScreenHeight() {
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		return mDisplayMetrics.heightPixels;
	}

	public int getScreenWidth() {
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		return mDisplayMetrics.widthPixels;
	}

	private void showScrollViewMenu(ScrollView scrollViewMenu) {
		if (scrollViewMenu != null && scrollViewMenu.getParent() == null) {
			addView(scrollViewMenu);
		}
	}

	private void hideScrollViewMenu(ScrollView scrollViewMenu) {
		if (scrollViewMenu != null && scrollViewMenu.getParent() != null) {
			removeView(scrollViewMenu);
		}
	}

	public interface OnMenuListener {


		void openMenu();


		void closeMenu();
	}
}
