package com.android.hhuimie.hmusic.waveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.hhuimie.hmusic.R;

class Wave extends View {

	public final int DEFAULT_ABOVE_WAVE_ALPHA = 50;
	public final int DEFAULT_BLOW_WAVE_ALPHA = 30;

	private final float X_SPACE = 20;

	private Path mAboveWavePath = new Path();
	private Path mBlowWavePath = new Path();

	private Paint mAboveWavePaint = new Paint();
	private Paint mBlowWavePaint = new Paint();

	private int mAboveWaveColor;
	private int mBlowWaveColor;

	private float mWaveMultiple;
	private float mWaveLength;
	private int mWaveHeight;
	private float mMaxRight;
	private float mWaveHz;

	private float mAboveOffset = 0.0f;
	private float mBlowOffset;

	private RefreshProgressRunnable mRefreshProgressRunnable;

	private int left, right, bottom;

	private double omega;

	public Wave(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.waveViewStyle);
	}

	public Wave(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawPath(mBlowWavePath, mBlowWavePaint);
		canvas.drawPath(mAboveWavePath, mAboveWavePaint);
	}

	public void setAboveWaveColor(int aboveWaveColor) {
		mAboveWaveColor = aboveWaveColor;
	}

	public void setBlowWaveColor(int blowWaveColor) {
		mBlowWaveColor = blowWaveColor;
	}

	public Paint getAboveWavePaint() {
		return mAboveWavePaint;
	}

	public Paint getBlowWavePaint() {
		return mBlowWavePaint;
	}

	public void initializeWaveSize(int waveMultiple, int waveHeight, int waveHz) {
		mWaveMultiple = getWaveMultiple(waveMultiple);
		mWaveHeight = getWaveHeight(waveHeight);
		mWaveHz = getWaveHz(waveHz);
		mBlowOffset = mWaveHeight * 0.4f;
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				                                                          mWaveHeight * 2);
		setLayoutParams(params);
	}

	public void initializePainters() {
		// 设置颜色
		mAboveWavePaint.setColor(mAboveWaveColor);
		// 设置透明度
		mAboveWavePaint.setAlpha(DEFAULT_ABOVE_WAVE_ALPHA);
		// 设置样式为实心
		mAboveWavePaint.setStyle(Paint.Style.FILL);
		// 设置抗锯齿，否则会出现锯齿很难看
		mAboveWavePaint.setAntiAlias(true);

		mBlowWavePaint.setColor(mBlowWaveColor);
		mBlowWavePaint.setAlpha(DEFAULT_BLOW_WAVE_ALPHA);
		mBlowWavePaint.setStyle(Paint.Style.FILL);
		mBlowWavePaint.setAntiAlias(true);
	}

	private float getWaveMultiple(int size) {
		switch (size) {
			case WaveView.LARGE:
				return 1.5f;
			case WaveView.MIDDLE:
				return 1f;
			case WaveView.LITTLE:
				return 0.5f;
		}
		return 0;
	}

	private int getWaveHeight(int size) {
		switch (size) {
			case WaveView.LARGE:
				return 16;
			case WaveView.MIDDLE:
				return 8;
			case WaveView.LITTLE:
				return 5;
		}
		return 0;
	}

	private float getWaveHz(int size) {
		switch (size) {
			case WaveView.LARGE:
				return 0.13f;
			case WaveView.MIDDLE:
				return 0.09f;
			case WaveView.LITTLE:
				return 0.05f;
		}
		return 0;
	}

	/**
	 * calculate wave track
	 */
	private void calculatePath() {
		mAboveWavePath.reset();
		mBlowWavePath.reset();

		getWaveOffset();

		float y;
		mAboveWavePath.moveTo(left, bottom);
		for (float x = 0; x <= mMaxRight; x += X_SPACE) {
			y = (float) (mWaveHeight * Math.sin(omega * x + mAboveOffset) + mWaveHeight);
			mAboveWavePath.lineTo(x, y);
		}
		mAboveWavePath.lineTo(right, bottom);

		mBlowWavePath.moveTo(left, bottom);
		for (float x = 0; x <= mMaxRight; x += X_SPACE) {
			y = (float) (mWaveHeight * Math.sin(omega * x + mBlowOffset) + mWaveHeight);
			mBlowWavePath.lineTo(x, y);
		}
		mBlowWavePath.lineTo(right, bottom);
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		if (View.GONE == visibility) {
			removeCallbacks(mRefreshProgressRunnable);
		} else {
			removeCallbacks(mRefreshProgressRunnable);
			mRefreshProgressRunnable = new RefreshProgressRunnable();
			post(mRefreshProgressRunnable);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (hasWindowFocus) {
			if (mWaveLength == 0) {
				startWave();
			}
		}
	}

	public void startWave() {
		if (getWidth() != 0) {
			int width = getWidth();
			mWaveLength = width * mWaveMultiple;
			left = getLeft();
			right = getRight();
			bottom = getBottom();
			mMaxRight = right + X_SPACE;
			double PI2 = 2 * Math.PI;
			omega = PI2 / mWaveLength;
		}
	}

	private void getWaveOffset() {
		if (mBlowOffset > Float.MAX_VALUE - 100) {
			mBlowOffset = 0;
		} else {
			mBlowOffset += mWaveHz;
		}

		if (mAboveOffset > Float.MAX_VALUE - 100) {
			mAboveOffset = 0;
		} else {
			mAboveOffset += mWaveHz;
		}
	}

	private class RefreshProgressRunnable implements Runnable {
		public void run() {
			synchronized (Wave.this) {
				long start = System.currentTimeMillis();

				calculatePath();

				invalidate();

				long gap = 16 - (System.currentTimeMillis() - start);
				postDelayed(this, gap < 0 ? 0 : gap);
			}
		}
	}

}
