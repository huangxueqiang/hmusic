package com.android.hhuimie.hmusic.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.activities.MainActivity;
import com.android.hhuimie.hmusic.model.Constant;
import com.android.hhuimie.hmusic.model.MusicInfo;
import com.android.hhuimie.hmusic.utils.DBHelper;
import com.android.hhuimie.hmusic.utils.MusicInfoUtil;
import com.android.hhuimie.hmusic.waveview.ColorArt;
import com.android.hhuimie.hmusic.waveview.WaveView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PlayFragment extends Fragment {
	private static final PlayFragment mPlayFragment = new PlayFragment();
	private static final int sBG_COLOR = -65794;
	private static final int sPRI_COLOR = -8557974;
	private static final int sDET_COLOR = -9826288;
	private MusicInfo mCurrentSong;
	private WaveView mWaveView;
	private SeekBar mSeekBar;
	private MainActivity mMainActivity;
	private ImageView mAlbumImage;
	private TextView mCurrentTime;
	private TextView mCurrentDuration;
	private TextView mCurrentArtistAlbum;
	private View mPlayView;
	private TextView mCurrentTitle;
	private Intent mTimeChangedIntent;
	private ImageButton mCtrlBtn;
	private ImageButton mPrevBtn;
	private ImageButton mPlayBtn;
	private ImageButton mNextBtn;
	private ImageButton mToListBtn;
	private Drawable mOldBackground;
	private Drawable mOldActionbar;

	public static PlayFragment getPlayFragment() {
		return mPlayFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mPlayView = inflater.inflate(R.layout.fragment_play, container, false);
		findView();
		setViewListener();
		mMainActivity = (MainActivity) getActivity();
		MyPlayFragmentReceiver receiver = new MyPlayFragmentReceiver();
		mTimeChangedIntent = new Intent(Constant.ACTION_TIME_CHANGED);
		Context context = mMainActivity.getApplicationContext();
		List<String> list = Arrays.asList(context.fileList());
		if (!list.contains("default.png")) {
		}
		mMainActivity.setReceiver(receiver);
		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mWaveView.setProgress(progress);
				if (fromUser) {
					seekBar.setProgress(progress);
					mTimeChangedIntent.putExtra(Constant.EXTRA_TIME_CHANGED, progress);
					mCurrentTime.setText(MusicInfoUtil.getFormatDuration(progress));
					mMainActivity.sendLocalbroadcast(mTimeChangedIntent);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		return mPlayView;
	}

	private void findView() {
		mSeekBar = (SeekBar) mPlayView.findViewById(R.id.seekbar);
		mAlbumImage = (ImageView) mPlayView.findViewById(R.id.album_image);
		mCurrentTime = (TextView) mPlayView.findViewById(R.id.play_current_time);
		mCurrentDuration = (TextView) mPlayView.findViewById(R.id.play_duration);
		mCurrentTitle = (TextView) mPlayView.findViewById(R.id.play_current_title);
		mCtrlBtn = (ImageButton) mPlayView.findViewById(R.id.play_control);
		mPrevBtn = (ImageButton) mPlayView.findViewById(R.id.play_previous);
		mPlayBtn = (ImageButton) mPlayView.findViewById(R.id.play_play_pause);
		mNextBtn = (ImageButton) mPlayView.findViewById(R.id.play_next);
		mToListBtn = (ImageButton) mPlayView.findViewById(R.id.play_to_list);
		mWaveView = (WaveView) mPlayView.findViewById(R.id.wave_view);
		mCurrentArtistAlbum = (TextView) mPlayView.findViewById(R.id.play_current_artist_album);
	}

	private void setViewListener() {
		MyOnClickListener myOnClickListener = new MyOnClickListener();
		mCtrlBtn.setOnClickListener(myOnClickListener);
		mPrevBtn.setOnClickListener(myOnClickListener);
		mPlayBtn.setOnClickListener(myOnClickListener);
		mNextBtn.setOnClickListener(myOnClickListener);
		mToListBtn.setOnClickListener(myOnClickListener);
		Animation animation = new AlphaAnimation(1.0f, 0.0f);
		animation.setDuration(200);
		animation.setFillBefore(true);
	}

	public MusicInfo getCurrentSong() {
		return mCurrentSong;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mMainActivity.addIgnoredView(mSeekBar);
	}

	private void setAlbumArtAndColorArt(Bitmap bmp, int duration, int bgColor, int primaryColor, int detailColor) {
		mAlbumImage.setImageBitmap(bmp);
		mWaveView.setPaint(duration, detailColor, primaryColor);
		// 由于用到颜色的渐变过渡，所以用到ColorDrawable类配合TransitionDrawable类
		// 根据背景颜色代码,分别生成背景以及actionbar的ColorDrawable对象
		// 这里一定要分别生成2个对象，如果只用一个对象设置的话，会出现bug
		Drawable bgDrawable = new ColorDrawable(bgColor);
		Drawable barDrawable = new ColorDrawable(bgColor);
		// 获得actionbar的标题id
		int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
		// 获得actionbar的标题textview
		TextView abTitle = (TextView) mMainActivity.findViewById(titleId);
		// 如果之前没有保存过actionbar颜色
		if (mOldActionbar == null) {
			// 直接设置actionbar的背景drawable
			mMainActivity.getActionBar().setBackgroundDrawable(barDrawable);
		} else {
			// 否则，创建过渡drawable，效果是从旧的actionbar颜色转换为新的颜色
			TransitionDrawable transBarDrawable = new TransitionDrawable(new Drawable[]{mOldActionbar, barDrawable});
			// 设置背景drawable为过渡drawable
			mMainActivity.getActionBar().setBackgroundDrawable(transBarDrawable);
			// 开始过渡动画，时长为500毫秒
			transBarDrawable.startTransition(500);
		}
		// 背景颜色同理
		if (mOldBackground == null) {
			mWaveView.setBackground(bgDrawable);
		} else {
			TransitionDrawable transBgDrawable = new TransitionDrawable(new Drawable[]{mOldBackground, bgDrawable});
			mWaveView.setBackground(transBgDrawable);
			transBgDrawable.startTransition(500);
		}
		// 保存当前drawable为旧drawable
		mOldActionbar = barDrawable;
		mOldBackground = bgDrawable;
		// 设置字体颜色
		abTitle.setTextColor(primaryColor);
		mCurrentTime.setTextColor(detailColor);
		mCurrentArtistAlbum.setTextColor(detailColor);
		mCurrentTitle.setTextColor(primaryColor);
		mCurrentDuration.setTextColor(detailColor);
	}

	private Bitmap bitmapTask(Bitmap bitmap) {
		// 获得原专辑图片宽度
		int bmpWidth = bitmap.getWidth();
		// 获得原专辑图片高度
		int bmpHeight = bitmap.getHeight();
		// 创建像素点数组
		int[] pixels = new int[bmpWidth * bmpHeight];
		// 获得图片像素点数组
		bitmap.getPixels(pixels, 0, bmpWidth, 0, 0, bmpWidth, bmpHeight);
		// 不能对原图进行setPixel操作，会报错，新建一个副本
		Bitmap tempBitmap = Bitmap.createBitmap(bmpWidth, bmpHeight, Bitmap.Config.ARGB_8888);
		// 透明度
		int alpha = 0x00000000;
		// 从下到上，遍历每一个像素点
		for (int y = bmpHeight - 1; y > 0; y--) {
			for (int x = bmpWidth - 1; x > 0; x--) {
				// 获得像素点颜色
				int color = bitmap.getPixel(x, y);
				// red 十进制颜色代码
				int r = Color.red(color);
				// green 十进制颜色代码
				int g = Color.green(color);
				// blue 十进制颜色代码
				int b = Color.blue(color);
				// 设置像素点颜色透明度，其余的rgb不变，左移操作是为了将颜色代码转换回16进制
				color = alpha|r<<16|g<<8|b;
				// 设置该点的颜色代码到副本中
				tempBitmap.setPixel(x, y, color);
			}
			// 透明度减少
			alpha = alpha + 0x01000000;
		}
		// 返回副本图片，即处理过的图片
		return tempBitmap;
	}

	private class MyOnClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.play_control:
					Intent intent = new Intent(Constant.ACTION_CONTROL);
					mMainActivity.sendLocalbroadcast(intent);
					break;
				case R.id.play_previous:
					Intent intent1 = new Intent(Constant.ACTION_CONTROL);
					intent1.putExtra(Constant.EXTRA_CONTROL, Constant.CONTROL_PREVIOUS);
					mMainActivity.sendLocalbroadcast(intent1);
					break;
				case R.id.play_play_pause:
					Intent intent2 = new Intent(Constant.ACTION_CONTROL);
					intent2.putExtra(Constant.EXTRA_CONTROL, Constant.CONTROL_PLAY);
					mMainActivity.sendLocalbroadcast(intent2);
					break;
				case R.id.play_next:
					Intent intent3 = new Intent(Constant.ACTION_CONTROL);
					intent3.putExtra(Constant.EXTRA_CONTROL, Constant.CONTROL_NEXT);
					mMainActivity.sendLocalbroadcast(intent3);
					break;
				case R.id.play_to_list:
					mMainActivity.changeToMainFragment();
					break;
			}
		}
	}

	private class MyPlayFragmentReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Constant.ACTION_CURRENT_MSG)) {
				mCurrentSong = (MusicInfo) intent.getSerializableExtra(Constant.EXTRA_CURRENT_SONG);
				long id = mCurrentSong.getId();
				long duration = mCurrentSong.getDuration();
				long albumId = mCurrentSong.getAlbumId();
				String title = mCurrentSong.getTitle();
				String artist = mCurrentSong.getArtist();
				String album = mCurrentSong.getAlbum();
				String albumArt = mCurrentSong.getAlbumArt();
				if (albumArt == null) {
					setAlbumArtAndColorArt(BitmapFactory.decodeResource(getResources(), R.drawable.defaultalbum), (int) duration, sBG_COLOR, sPRI_COLOR, sDET_COLOR);
				} else {
					Bitmap currentBmp = BitmapFactory.decodeFile(mMainActivity.getApplicationContext().getFilesDir() + "/" + id + ".png");
					if (currentBmp != null) {
						setAlbumArtAndColorArt(currentBmp, (int) duration, mCurrentSong.getBgColor(), mCurrentSong.getPrimaryColor(), mCurrentSong.getDetailColor());
					} else {
						new MyHandleBitmapTask(id, albumId, (int) duration).execute();
					}
				}
				mSeekBar.setMax((int) duration);
				mCurrentTitle.setText(title);
				mCurrentArtistAlbum.setText(artist + " - " + album);
				mCurrentDuration.setText(MusicInfoUtil.getFormatDuration(duration));
			} else if (action.equals(Constant.ACTION_CONTROL_STATE)) {
				int state = intent.getIntExtra(Constant.EXTRA_CONTROL_STATE, -1);
				Log.d("state on receive", state + "");
				if (state != -1) {
					switch (state) {
						case 0:
							mCtrlBtn.setImageResource(R.drawable.play_btn_loop_selector);
							break;
						case 1:
							mCtrlBtn.setImageResource(R.drawable.play_btn_shuffle_selector);
							break;
						case 2:
							mCtrlBtn.setImageResource(R.drawable.play_btn_one_selector);
							break;
					}
				}
			} else if (action.equals(Constant.ACTION_PLAY_STATUS)) {
				int status = intent.getIntExtra(Constant.EXTRA_PLAY_STATUS, -1);
				if (status == Constant.IS_PAUSED) {
					mPlayBtn.setImageResource(R.drawable.play_btn_pause_selector);
				} else if (status == Constant.IS_PLAYING) {
					mPlayBtn.setImageResource(R.drawable.play_btn_play_selector);
				}
			} else if (PlayFragment.this.isHidden()) {
			} else if (action.equals(Constant.ACTION_CURRENT_TIME)) {
				int currentTime = intent.getIntExtra(Constant.EXTRA_CURRENT_TIME, -1);
				mSeekBar.setProgress(currentTime);
				String s1 = MusicInfoUtil.getFormatDuration(currentTime);
				mCurrentTime.setText(s1);
			}
		}
	}

	private class MyHandleBitmapTask extends AsyncTask<Void, Integer, Bitmap> {
		ColorArt mColorArt;
		private long mSongId;
		private long mAlbumId;
		private int mDuration;

		public MyHandleBitmapTask(long songId, long albumId, int duration) {
			mSongId = songId;
			mAlbumId = albumId;
			mDuration = duration;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (bitmap != null) {
				int bgColor = mColorArt.getBackgroundColor();
				int priColor = mColorArt.getPrimaryColor();
				int detColor = mColorArt.getDetailColor();
				setAlbumArtAndColorArt(bitmap, mDuration, bgColor, priColor, detColor);
				mCurrentSong.setColor(bgColor, priColor, detColor);
				new DBHelper(mMainActivity).setColorArt(mSongId, bgColor, priColor, detColor);
				FileOutputStream outputStream = null;
				try {
					outputStream = mMainActivity.openFileOutput(mSongId + ".png", Context.MODE_PRIVATE);
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (outputStream != null) {
							outputStream.flush();
							outputStream.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			Bitmap bitmap = MusicInfoUtil.getArtwork(mMainActivity, mSongId, mAlbumId, false, false);
			mColorArt = new ColorArt(bitmap);
			return bitmapTask(bitmap);
		}
	}
}
