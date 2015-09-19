package com.android.hhuimie.hmusic.waveview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.hhuimie.hmusic.R;

public class WaveView extends LinearLayout {
	protected static final int LARGE = 1;
	protected static final int MIDDLE = 2;
	protected static final int LITTLE = 3;
	private Context mContext;
	private int mProgress;
	private int mDuration = 100;
	private Wave mWave;
	private Solid mSolid;

	public WaveView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		setOrientation(VERTICAL);

		final TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WaveView, R.attr.waveViewStyle, 0);
		int aboveWaveColor = attributes.getColor(R.styleable.WaveView_above_wave_color, Color.WHITE);
		int blowWaveColor = attributes.getColor(R.styleable.WaveView_blow_wave_color, Color.WHITE);
		mProgress = attributes.getInt(R.styleable.WaveView_progress, 0);
		int waveHeight = attributes.getInt(R.styleable.WaveView_wave_height, MIDDLE);
		int waveMultiple = attributes.getInt(R.styleable.WaveView_wave_length, LARGE);
		int waveHz = attributes.getInt(R.styleable.WaveView_wave_hz, MIDDLE);
		attributes.recycle();
		mWave = new Wave(context, null);
		mWave.initializeWaveSize(waveMultiple, waveHeight, waveHz);
		mWave.setAboveWaveColor(aboveWaveColor);
		mWave.setBlowWaveColor(blowWaveColor);
		mWave.initializePainters();
		mSolid = new Solid(context, null);
		mSolid.setAboveWavePaint(mWave.getAboveWavePaint());
		mSolid.setBlowWavePaint(mWave.getBlowWavePaint());
		addView(mWave);
		addView(mSolid);
		setProgress(mProgress);
	}

	public void setPaint(int duration, int aboveWaveColor, int blowWaveColor) {
		// 设置布局的最大高度为歌曲的长度，后面可根据播放的比例，设置布局的高度
		mDuration = duration;
		// 设置波浪颜色,设置2种paint是为了显示出波浪交替上升的效果
		mWave.setAboveWaveColor(aboveWaveColor);
		mWave.setBlowWaveColor(blowWaveColor);
		if (mSolid == null) mSolid = new Solid(mContext, null);
		// 根据设置的颜色，初始化画布上的画笔，后面会贴出方法代码
		mWave.initializePainters();
		// 统一波浪与下方填充的view
		// 设置为相同的画笔
		mSolid.setAboveWavePaint(mWave.getAboveWavePaint());
		mSolid.setBlowWavePaint(mWave.getBlowWavePaint());
		// 移除之前所有的view
		removeAllViews();
		// 重新添加2个view
		addView(mWave);
		addView(mSolid);
	}

	public void setProgress(int progress) {
		mProgress = progress > mDuration ? mDuration : progress;
		computeWaveToTop();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		if (hasWindowFocus) {
			computeWaveToTop();
		}
	}

	private void computeWaveToTop() {
		// 计算出布局距离屏幕顶部的距离
		// 其中getHeight()得到屏幕高度，mProgress为当前播放进度，通过进度条设置，mDuration为歌曲时长
		int waveToTop = (int) (getHeight() * (1f - mProgress / (float) mDuration));
		// 获得view的参数
		ViewGroup.LayoutParams params = mWave.getLayoutParams();
		if (params != null) {
			// 设置参数中，顶部的空白
			((LayoutParams) params).topMargin = waveToTop;
		}
		// 将修改的参数设置为view的参数
		mWave.setLayoutParams(params);
	}

	@Override
	public Parcelable onSaveInstanceState() {

		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.progress = mProgress;
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		setProgress(ss.progress);
	}

	private static class SavedState extends BaseSavedState {
		int progress;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			progress = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(progress);
		}

		public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
