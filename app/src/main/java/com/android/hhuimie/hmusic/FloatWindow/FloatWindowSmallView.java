package com.android.hhuimie.hmusic.FloatWindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.utils.DBHelper;
import com.android.hhuimie.hmusic.utils.SettingManager;

import java.lang.reflect.Field;

public class FloatWindowSmallView extends LinearLayout {

	public static int mViewWidth;

	public static int mViewHeight;

	private static int sStatusBarHeight;

	private WindowManager mWindowManager;

	private WindowManager.LayoutParams mParams;

	public WindowManager.LayoutParams getParams() {
		return mParams;
	}

	private float mXInScreen;

	private float mYInScreen;

	private float mXDownInScreen;

	private float mYDownInScreen;

	private float mXInView;

	private float mYInView;
	private Context mContext;

	public FloatWindowSmallView(Context context) {
		super(context);
		mContext = context;
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		LayoutInflater.from(context).inflate(R.layout.float_window_small, this);
		View view = findViewById(R.id.float_window_small_layout);
		mViewWidth = view.getLayoutParams().width;
		mViewHeight = view.getLayoutParams().height;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mXInView = event.getX();
					mYInView = event.getY();
					mXDownInScreen = event.getRawX();
					mYDownInScreen = event.getRawY() - getStatusBarHeight();
					mXInScreen = event.getRawX();
					mYInScreen = event.getRawY() - getStatusBarHeight();
					break;
				case MotionEvent.ACTION_MOVE:
					mXInScreen = event.getRawX();
					mYInScreen = event.getRawY() - getStatusBarHeight();

					updateViewPosition();
					break;
				case MotionEvent.ACTION_UP:

					if (mXDownInScreen == mXInScreen && mYDownInScreen == mYInScreen) {
						openBigWindow();
					} else {
						if (SettingManager.getSingleSetting(mContext, SettingManager.sFLOAT_LOCAL_SETTING))
							new DBHelper(mContext).setCustomFloatParams(mParams.x, mParams.y);
					}
					break;
				default:
					break;
			}
		return true;
	}

	public void setParams(WindowManager.LayoutParams params) {
		mParams = params;
	}

	private void updateViewPosition() {
		mParams.x = (int) (mXInScreen - mXInView);
		mParams.y = (int) (mYInScreen - mYInView);
		mWindowManager.updateViewLayout(this, mParams);
	}

	private void openBigWindow() {
		MyWindowManager.createBigWindow(getContext());
		MyWindowManager.removeSmallWindow(getContext());
	}

	private int getStatusBarHeight() {
		if (sStatusBarHeight == 0) {
			try {
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field field = c.getField("status_bar_height");
				int x = (Integer) field.get(o);
				sStatusBarHeight = getResources().getDimensionPixelSize(x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return sStatusBarHeight;
	}
}
