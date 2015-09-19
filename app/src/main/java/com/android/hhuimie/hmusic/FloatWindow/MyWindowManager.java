package com.android.hhuimie.hmusic.FloatWindow;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.android.hhuimie.hmusic.utils.DBHelper;

import java.util.List;

public class MyWindowManager {

	private static FloatWindowSmallView smallWindow;

	private static FloatWindowBigView bigWindow;

	private static LayoutParams smallWindowParams;

	private static LayoutParams bigWindowParams;

	private static WindowManager mWindowManager;
	private static LocalBroadcastManager sManager;
	public static String sTitle;
	public static boolean sIsPlaying;

	public static void createSmallWindow(Context context) {
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (smallWindow == null) {
			smallWindow = new FloatWindowSmallView(context);
			if (smallWindowParams == null) {
				smallWindowParams = new LayoutParams();
				smallWindowParams.type = LayoutParams.TYPE_PHONE;
				smallWindowParams.format = PixelFormat.RGBA_8888;
				smallWindowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
				smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
				smallWindowParams.width = FloatWindowSmallView.mViewWidth;
				smallWindowParams.height = FloatWindowSmallView.mViewHeight;
				int[] params = new DBHelper(context).getCustomFloatParams();
				if (params != null) {
					smallWindowParams.x = params[0];
					smallWindowParams.y = params[1];
				} else {
					smallWindowParams.x = screenWidth;
					smallWindowParams.y = screenHeight / 2;
				}
			}
			smallWindow.setParams(smallWindowParams);
			windowManager.addView(smallWindow, smallWindowParams);
		}
	}

	public static void removeSmallWindow(Context context) {
		if (smallWindow != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(smallWindow);
			smallWindow = null;
		}
	}

	public static void createBigWindow(Context context) {
		WindowManager windowManager = getWindowManager(context);
		if (bigWindow == null) {
			bigWindow = new FloatWindowBigView(context);
			if (bigWindowParams == null) {
				bigWindowParams = new LayoutParams();
				bigWindowParams.x = smallWindow.getParams().x;
				bigWindowParams.y = smallWindow.getParams().y;
				bigWindowParams.type = LayoutParams.TYPE_PHONE;
				bigWindowParams.format = PixelFormat.RGBA_8888;
				bigWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
				bigWindowParams.width = FloatWindowBigView.sViewWidth;
				bigWindowParams.height = FloatWindowBigView.sViewHeight;
			}
			bigWindowParams.x = smallWindow.getParams().x;
			bigWindowParams.y = smallWindow.getParams().y;
			windowManager.addView(bigWindow, bigWindowParams);
			if (sManager != null)
				sManager.registerReceiver(FloatWindowBigView.sMyReceiver, FloatWindowBigView.sIntentFilter);
		}
	}

	public static void removeBigWindow(Context context) {
		if (bigWindow != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(bigWindow);
			bigWindow = null;
			if (sManager != null) {
				sManager.unregisterReceiver(FloatWindowBigView.sMyReceiver);
			}
		}
	}

	public static void removeAllWindow(Context context) {
		removeSmallWindow(context);
		removeBigWindow(context);
	}

	public static boolean isWindowShowing() {
		return smallWindow != null || bigWindow != null;
	}

	private static WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}

	public static boolean isApplicationShowing(String packageName, Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
		if (appProcesses != null) {
			for (RunningAppProcessInfo runningAppProcessInfo : appProcesses) {
				if (runningAppProcessInfo.processName.equals(packageName)) {
					int status = runningAppProcessInfo.importance;
					if (status == RunningAppProcessInfo.IMPORTANCE_VISIBLE || status == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void setLocalManager(LocalBroadcastManager broadcastManager) {
		sManager = broadcastManager;
	}
}
