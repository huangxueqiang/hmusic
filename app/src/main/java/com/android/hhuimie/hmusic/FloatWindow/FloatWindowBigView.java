package com.android.hhuimie.hmusic.FloatWindow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.model.Constant;
import com.android.hhuimie.hmusic.model.MusicInfo;

public class FloatWindowBigView extends LinearLayout implements View.OnClickListener {

	public static int sViewWidth;

	public static int sViewHeight;
	public static MyReceiver sMyReceiver;
	public static IntentFilter sIntentFilter;
	private Context mContext;
	private TextView mTitleTV;
	private ImageButton mPlayBtn;

	public FloatWindowBigView(Context context) {
		super(context);
		mContext = context;
		LayoutInflater.from(context).inflate(R.layout.float_window_big, this);
		View view = findViewById(R.id.float_window_big_layout);
		sViewWidth = view.getLayoutParams().width;
		sViewHeight = view.getLayoutParams().height;
		ImageButton preBtn = (ImageButton) findViewById(R.id.float_pre_btn);
		mPlayBtn = (ImageButton) findViewById(R.id.float_play_btn);
		if (!MyWindowManager.sIsPlaying) {
			mPlayBtn.setImageResource(R.drawable.not_pause_selector);
		}
		mTitleTV = (TextView) findViewById(R.id.float_title);
		ImageButton nextBtn = (ImageButton) findViewById(R.id.float_next_btn);
		ImageButton returnBtn = (ImageButton) findViewById(R.id.float_return_btn);
		ImageButton exitBtn = (ImageButton) findViewById(R.id.float_exit_btn);
		if (MyWindowManager.sTitle != null) mTitleTV.setText(MyWindowManager.sTitle);
		preBtn.setOnClickListener(this);
		mPlayBtn.setOnClickListener(this);
		nextBtn.setOnClickListener(this);
		returnBtn.setOnClickListener(this);
		exitBtn.setOnClickListener(this);
		mTitleTV.setOnClickListener(this);
		sMyReceiver = new MyReceiver();
		sIntentFilter = new IntentFilter(Constant.ACTION_CURRENT_MSG);
		sIntentFilter.addAction(Constant.ACTION_PLAY_STATUS);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(Constant.ACTION_NOT_CONTROL);
		switch (v.getId()) {
			case R.id.float_pre_btn:
				intent.putExtra(Constant.EXTRA_NOT_CONTROL, Constant.CONTROL_NOT_PREVIOUS);
				mContext.sendBroadcast(intent);
				break;
			case R.id.float_play_btn:
				intent.putExtra(Constant.EXTRA_NOT_CONTROL, Constant.CONTROL_NOT_PLAY);
				mContext.sendBroadcast(intent);
				break;
			case R.id.float_next_btn:
				intent.putExtra(Constant.EXTRA_NOT_CONTROL, Constant.CONTROL_NOT_NEXT);
				mContext.sendBroadcast(intent);
				break;
			case R.id.float_return_btn:
				MyWindowManager.removeBigWindow(mContext);
				MyWindowManager.createSmallWindow(mContext);
				break;
			case R.id.float_exit_btn:
				intent.putExtra(Constant.EXTRA_NOT_CONTROL, Constant.CONTROL_NOT_EXIT);
				mContext.sendBroadcast(intent);
				break;
		}
	}

	private class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("dhde", intent.getAction());
			switch (intent.getAction()) {
				case Constant.ACTION_CURRENT_MSG:
					MusicInfo currentSong = (MusicInfo) intent.getSerializableExtra(Constant.EXTRA_CURRENT_SONG);
					if (currentSong != null) {
						mTitleTV.setText(MyWindowManager.sTitle);
					}
					break;
				case Constant.ACTION_PLAY_STATUS:
					int status = intent.getIntExtra(Constant.EXTRA_PLAY_STATUS, -1);
					if (status == Constant.IS_PAUSED) {
						mPlayBtn.setImageResource(R.drawable.not_pause_selector);
					} else if (status == Constant.IS_PLAYING) {
						mPlayBtn.setImageResource(R.drawable.not_play_selector);
					}
					break;
			}
		}
	}
}
