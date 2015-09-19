package com.android.hhuimie.hmusic.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.android.hhuimie.hmusic.FloatWindow.MyWindowManager;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.activities.MainActivity;
import com.android.hhuimie.hmusic.fragments.MainFragment;
import com.android.hhuimie.hmusic.model.Constant;
import com.android.hhuimie.hmusic.model.MusicInfo;
import com.android.hhuimie.hmusic.utils.DBHelper;
import com.android.hhuimie.hmusic.utils.MusicInfoUtil;
import com.android.hhuimie.hmusic.utils.SettingManager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

public class MusicService extends Service implements OnErrorListener, OnCompletionListener, OnPreparedListener {
	private final IBinder mMusicBinder = new MyMusicBinder();
	private BroadcastReceiver mNotReceiver;
	private MediaPlayer mMediaPlayer;
	private List<MusicInfo> mMusicInfos;
	private Vibrator mVibrator;
	private Intent mCurrentMSGIntent;
	private Intent mMusicStateIntent;
	private Intent mCurrentTimeIntent;
	private MyHandler mMyHandler;
	private Method mGetMethod;
	private MainActivity mMainActivity;
	private Random mRandom;
	private String[] mStateMsg = {"循环播放", "随机播放", "单曲播放"};
	private long mCurrentSongId = -1;
	private int mSongPosn;
	private String mSongUrl;
	private int mMusicState;
	private boolean mIsFirstTime = true;
	private boolean mIsSetList;
	private long mDuration;
	private long mTimer;
	private MusicInfo mCurrentSong;
	private NotificationManager mNotificationManager;
	private boolean mNotSetting;
	private boolean mIsInitNot;
	private boolean mIsRegisterNot;
	private boolean mPlugSetting;
	private boolean mFloatSetting;
	private boolean mPauseByUser;

	@Override
	public void onCreate() {
		super.onCreate();
		mSongPosn = 1;
		TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
		mCurrentMSGIntent = new Intent(Constant.ACTION_CURRENT_MSG);
		mCurrentTimeIntent = new Intent(Constant.ACTION_CURRENT_TIME);
		mMusicStateIntent = new Intent(Constant.ACTION_PLAY_STATUS);
		MyMusicReceiver musicReceiver = new MyMusicReceiver();
		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		mRandom = new Random();
		mMyHandler = new MyHandler();
		mMediaPlayer = new MediaPlayer();
		MainFragment mainFragment = MainFragment.getMainFragment();
		mMainActivity = (MainActivity) mainFragment.getActivity();
		mMainActivity.setReceiver(musicReceiver);
		MyWindowManager.setLocalManager(mMainActivity.getBroadcastManager());
		mMusicInfos = mainFragment.getMusicInfos();
		try {
			mGetMethod = mMusicInfos.getClass().getMethod("get", int.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		initMusicPlayer();
		long[] lastPlayMsg = new DBHelper(mMainActivity).getLastPlayMsg();
		if (lastPlayMsg != null) {
			long id = lastPlayMsg[0];
			setMusicState((int) lastPlayMsg[1]);
			for (int i = 0; i < mMusicInfos.size(); i++) {
				if (getItemType(i) == 1) {
					if (mMusicInfos.get(i).getId() == id) {
						mSongPosn = i;
						break;
					}
				}
			}
			if (mSongPosn != -1) {
				mCurrentSongId = id;
				mCurrentMSGIntent.putExtra(Constant.EXTRA_CURRENT_SONG, mMusicInfos.get(mSongPosn));
				mMainActivity.sendLocalbroadcast(mCurrentMSGIntent);
				MyWindowManager.sTitle = mMusicInfos.get(mSongPosn).getTitle();
			}
		}
		updateSetting();
	}

	public void showFloatWindow() {
		if (!MyWindowManager.isWindowShowing())
			MyWindowManager.createSmallWindow(getApplicationContext());
	}

	public void removeFloatWindow() {
		if (MyWindowManager.isWindowShowing()) {
			MyWindowManager.removeAllWindow(getApplicationContext());
		}
	}

	private void initMusicPlayer() {
		mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnErrorListener(this);
	}

	public void setMusicList(List<MusicInfo> musicInfos) {
		mMusicInfos = musicInfos;
		mIsSetList = true;
	}

	public void removeMsg() {
		mMyHandler.removeMessages(1);
	}

	public void setMusicState(int state) {
		mMusicState = state;
		Intent intent1 = new Intent(Constant.ACTION_CONTROL_STATE);
		intent1.putExtra(Constant.EXTRA_CONTROL_STATE, mMusicState);
		mMainActivity.sendLocalbroadcast(intent1);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mMusicBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		mMediaPlayer.stop();
		mMediaPlayer.release();
		return false;
	}

	public boolean isPlaying() {
		return mMediaPlayer.isPlaying();
	}

	public void pausePlayer() {
		mTimer = System.currentTimeMillis() - mTimer;
		mMediaPlayer.pause();
		mMusicStateIntent.putExtra(Constant.EXTRA_PLAY_STATUS, Constant.IS_PAUSED);
		mMainActivity.sendLocalbroadcast(mMusicStateIntent);
		mMyHandler.removeMessages(1);
		MyWindowManager.sIsPlaying = false;
	}

	public void go() {
		mMediaPlayer.start();
		mTimer = System.currentTimeMillis() - mTimer;
		mMusicStateIntent.putExtra(Constant.EXTRA_PLAY_STATUS, Constant.IS_PLAYING);
		mMainActivity.sendLocalbroadcast(mMusicStateIntent);
		mMyHandler.sendEmptyMessage(1);
		MyWindowManager.sIsPlaying = true;
	}

	public void playSong() {
		mIsFirstTime = false;
		autoHandlePlayTime();
//		if (mIsSetList) {
//			resetPosn();
//			mIsSetList = false;
//		}
		play();
	}

	private void autoHandlePlayTime() {
		if (!isPlaying()) {
			mMusicStateIntent.putExtra(Constant.EXTRA_PLAY_STATUS, Constant.IS_PLAYING);
			mMainActivity.sendLocalbroadcast(mMusicStateIntent);
			if (mTimer != 0) {
				Log.d("timer", MusicInfoUtil.getFormatDuration(mTimer));
			}
		} else if (mTimer != 0) {
			mTimer = System.currentTimeMillis() - mTimer;
			Log.d("timer", MusicInfoUtil.getFormatDuration(mTimer));
		}
		handlePlayTime();
		mTimer = 0;
	}

	public void play() {
		mMediaPlayer.reset();
		if (mTimer == 0) {
			mTimer = System.currentTimeMillis();
		} else {
			mTimer = System.currentTimeMillis() - mTimer;
		}
		try {
			mCurrentSong = (MusicInfo) mGetMethod.invoke(mMusicInfos, mSongPosn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mCurrentSong == null) return;
		mSongUrl = mCurrentSong.getUrl();
		mDuration = mCurrentSong.getDuration();
		mCurrentSongId = mCurrentSong.getId();
		// 根据歌曲id获取到唯一资源识别码
		Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCurrentSongId);
		try {
			// 将资源识别码设置为当前媒体播放资源
			mMediaPlayer.setDataSource(getApplicationContext(), trackUri);
			// 准备播放
			mMediaPlayer.prepare();
			// 填充意图,把当前歌曲作为额外数据填充到intent中
			mCurrentMSGIntent.putExtra(Constant.EXTRA_CURRENT_SONG, mCurrentSong);
			// 利用activity发送本地广播
			mMainActivity.sendLocalbroadcast(mCurrentMSGIntent);
			// MyHandler类是循环发送当前曲目时间的类，一秒发送一次，为了避免重复发送，先移除
			mMyHandler.removeMessages(1);
			// 重新发送
			mMyHandler.sendEmptyMessage(1);
			// 这个类是管理悬浮窗口的类，为了悬浮窗口中的图标和标题随之变化，从而如此设置
			MyWindowManager.sIsPlaying = true;
			MyWindowManager.sTitle = mCurrentSong.getTitle();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void resetPosn() {
		if (mMusicInfos.size() == 1) {
			mSongPosn = 0;
			return;
		}
		for (int i = 0; i < mMusicInfos.size(); i++) {
			if (getItemType(i) == 1 && mMusicInfos.get(i).getUrl().equals(mSongUrl)) {
				mSongPosn = i;
				return;
			}
		}
		mSongPosn = 1;
	}

	public void playPrev() {
		autoHandlePlayTime();
		if (mIsSetList) {
			resetPosn();
			mIsSetList = false;
		}
		// 得到列表的长度
		int size = mMusicInfos.size();
		if (size != 1) {
			// 如果当前播放模式为循环播放
			if (mMusicState == 0) {
				// 如果当前位置索引大于1
				if (mSongPosn > 1) {
					// 位置-1
					--mSongPosn;
					// 该方法用于判断该对象是否为音乐对象,下面会贴出代码
					// 如果type = 0, 意味着该对象是String类型,所以位置索引还要-1
					if (getItemType(mSongPosn) == 0) --mSongPosn;
				} else {
					// 如果位置索引小于1，则直接设置位置索引为最后一首
					if (getItemType(mSongPosn - 1) == 1) --mSongPosn;
					else
						mSongPosn = size - 1;
				}
			}
			// 如果当前播放模式为随机播放
			else if (mMusicState == 1) {
				// 随机位置索引
				mSongPosn = mRandom.nextInt(size);
				// 循环处理type为string的时候,重新随机索引
				while (getItemType(mSongPosn) == 0) {
					mSongPosn = mRandom.nextInt(size);
				}
			}
		}
		// 最后调用播放功能
		play();
	}

	public void playNext(boolean isPlaying) {
		if (!isPlaying) {
			mMusicStateIntent.putExtra(Constant.EXTRA_PLAY_STATUS, Constant.IS_PLAYING);
			mMainActivity.sendLocalbroadcast(mMusicStateIntent);
			if (mTimer != 0) {
				Log.d("timer", MusicInfoUtil.getFormatDuration(mTimer));
			}
		} else if (mTimer != 0) {
			mTimer = System.currentTimeMillis() - mTimer;
			Log.d("timer", MusicInfoUtil.getFormatDuration(mTimer));
		}
		handlePlayTime();
		mTimer = 0;
		if (mIsSetList) {
			resetPosn();
			mIsSetList = false;
		}
		int size = mMusicInfos.size();
		if (size != 1) {
			if (mMusicState == 0) {
				if (mSongPosn < size - 1) {
					++mSongPosn;
					if (getItemType(mSongPosn) == 0) ++mSongPosn;
				} else {
					if (getItemType(0) == 1) mSongPosn = 0;
					else
						mSongPosn = 1;
				}
			} else if (mMusicState == 1) {
				mSongPosn = mRandom.nextInt(size);
				while (getItemType(mSongPosn) == 0) {
					mSongPosn = mRandom.nextInt(size);
				}
			}
		}
		play();
	}

	private void handlePlayTime() {
		if (((double) mTimer) / ((double) mDuration) > 0.59) {
			Log.d("timerr", "mDuration: " + MusicInfoUtil.getFormatDuration(mDuration) + "---play: " + MusicInfoUtil.getFormatDuration(mTimer - 1));
			new MyAutoUpdateDBTask(mCurrentSongId).execute();
		}
	}

	private int getItemType(int position) {
		// 利用发射机制, 反射在程序中运用不少
		try {
			// 该method已在onCreate()方法中初始化, 获得的方法为List<>的get(int index)方法
			// 通过反射机制,对音乐列表调用该方法,根据position获得列表中的对象
			Object o = mGetMethod.invoke(mMusicInfos, position);
			// 如果该对象为音乐对象,则返回1
			if (o instanceof MusicInfo) {
				return 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 其余情况返回0;
		return 0;
	}

	private void initNotify() {
		// 如果通知栏开启，以及当前播放歌曲不为空
		if (mNotSetting && mCurrentSong != null) {
			// 创建通知栏通知
			NotificationCompat.Builder builder = new NotificationCompat.Builder(mMainActivity);
			// 创建自定义通知view
			RemoteViews remoteViews = new RemoteViews(mMainActivity.getPackageName(), R.layout.notification_layout);
			Bitmap bitmap = MusicInfoUtil.getArtwork(mMainActivity, mCurrentSongId, mCurrentSong.getAlbumId(), false, false);
			if (bitmap != null) {
				remoteViews.setImageViewBitmap(R.id.notification_album_art, bitmap);
			} else {
				remoteViews.setImageViewResource(R.id.notification_album_art, R.drawable.music5);
			}
			remoteViews.setTextViewText(R.id.notification_title, mCurrentSong.getTitle());
			remoteViews.setTextViewText(R.id.notification_artist, mCurrentSong.getArtist());
			remoteViews.setTextViewText(R.id.notification_album, mCurrentSong.getAlbum());
			if (mMediaPlayer.isPlaying()) {
				remoteViews.setImageViewResource(R.id.notification_play_btn, R.drawable.not_play_btn);
			} else {
				remoteViews.setImageViewResource(R.id.notification_play_btn, R.drawable.not_pause_btn);
			}

			Intent intent = new Intent(Constant.ACTION_NOT_CONTROL);
			intent.putExtra(Constant.EXTRA_NOT_CONTROL, Constant.CONTROL_NOT_PREVIOUS);
			PendingIntent prePendIntent = PendingIntent.getBroadcast(mMainActivity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			intent.putExtra(Constant.EXTRA_NOT_CONTROL, Constant.CONTROL_NOT_PLAY);
			PendingIntent playPendIntent = PendingIntent.getBroadcast(mMainActivity, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			intent.putExtra(Constant.EXTRA_NOT_CONTROL, Constant.CONTROL_NOT_NEXT);
			PendingIntent nextPendIntent = PendingIntent.getBroadcast(mMainActivity, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			intent.putExtra(Constant.EXTRA_NOT_CONTROL, Constant.CONTROL_NOT_EXIT);
			PendingIntent exitPendIntent = PendingIntent.getBroadcast(mMainActivity, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.notification_pre_btn, prePendIntent);
			remoteViews.setOnClickPendingIntent(R.id.notification_play_btn, playPendIntent);
			remoteViews.setOnClickPendingIntent(R.id.notification_next_btn, nextPendIntent);
			remoteViews.setOnClickPendingIntent(R.id.notification_exit_btn, exitPendIntent);
			Intent notPlayIntent = new Intent(mMainActivity, MainActivity.class);
			PendingIntent pendInt = PendingIntent.getActivity(this, 0, notPlayIntent, 0);
			Notification notify = builder.setContentIntent(pendInt)
					                      .setTicker(mCurrentSong.getTitle())
					                      .setPriority(Notification.PRIORITY_MAX)
					                      .setWhen(System.currentTimeMillis())
					                      .setOngoing(true)
					                      .setSmallIcon(android.R.drawable.ic_media_play)
					                      .build();
			notify.flags = Notification.FLAG_ONGOING_EVENT;
//			notify.contentView = remoteViews;
			notify.bigContentView = remoteViews;
			mNotificationManager.notify(999, notify);
			mIsInitNot = true;
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
		initNotify();
	}

	private void registerNotReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constant.ACTION_NOT_CONTROL);
		intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
		intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
		registerReceiver(mNotReceiver, intentFilter);
		mIsRegisterNot = true;
	}

	private void collapseStatusBar() {
		try {
			Object statusBarManager = getSystemService("statusbar");
			Method collapse;
			collapse = statusBarManager.getClass().getMethod("collapsePanels");

			collapse.invoke(statusBarManager);
		} catch (Exception localException) {
			localException.printStackTrace();
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mMediaPlayer.getCurrentPosition() > 0) {
			playNext(true);
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mp.reset();
		return false;
	}

	@Override
	public void onDestroy() {
		if (mNotSetting || mFloatSetting)
			unregisterReceiver(mNotReceiver);
	}

	private void exit() {
		if (mCurrentSongId != -1)
			new DBHelper(mMainActivity).exitSave(mCurrentSongId, mMusicState);
		if (mNotSetting)
			mNotificationManager.cancelAll();
		MyWindowManager.sIsPlaying = false;
		mMainActivity.finish();
	}

	private void updateSetting() {
		mNotSetting = SettingManager.getSingleSetting(mMainActivity, SettingManager.sNOT_SETTING);
		mFloatSetting = SettingManager.getSingleSetting(mMainActivity, SettingManager.sFLOAT_SETTING);
		mPlugSetting = SettingManager.getSingleSetting(mMainActivity, SettingManager.sPLUG_SETTING);
		if (mNotSetting || mFloatSetting || mPlugSetting) {
			if (mNotSetting && !mIsInitNot) {
				mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				initNotify();
			}
			if (!mNotSetting) {
				if (mNotificationManager != null) {
					mNotificationManager.cancelAll();
					mIsInitNot = false;
				}
			}
			if (mNotReceiver == null) {
				mNotReceiver = new MyNotBroadcastReceiver();
			}
			if (!mIsRegisterNot)
				registerNotReceiver();
		} else {
			if (mNotificationManager != null) {
				mNotificationManager.cancelAll();
				mIsInitNot = false;
			}
			if (mNotReceiver != null && mIsRegisterNot) {
				unregisterReceiver(mNotReceiver);
				mIsRegisterNot = false;
			}
		}
	}


	public class MyNotBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			switch (action) {
				case Constant.ACTION_NOT_CONTROL:
					int btnId = intent.getIntExtra(Constant.EXTRA_NOT_CONTROL, -1);
					switch (btnId) {
						case Constant.CONTROL_NOT_PREVIOUS:
							playPrev();
							break;
						case Constant.CONTROL_NOT_PLAY:
							if (isPlaying()) {
								mPauseByUser = true;
								pausePlayer();
								initNotify();
							} else if (mIsFirstTime) {
								playSong();
							} else go();
							initNotify();
							break;
						case Constant.CONTROL_NOT_NEXT:
							playNext(mMediaPlayer.isPlaying());
							break;
						case Constant.CONTROL_NOT_EXIT:
							exit();
							collapseStatusBar();
							break;
					}
					break;
				case Intent.ACTION_NEW_OUTGOING_CALL:
					if (mMediaPlayer.isPlaying()) {
						mPauseByUser = false;
						pausePlayer();
						initNotify();
					}
					break;
				case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
					Log.d("plugs", mPlugSetting ? "true" : "false");
					if (mPlugSetting && mMediaPlayer.isPlaying()) {
						mPauseByUser = false;
						pausePlayer();
						initNotify();
					}
					break;
				case Intent.ACTION_HEADSET_PLUG:
					Log.d("plugs", mPlugSetting ? "true" : "false");
					// 获得系统广播额外数据，即耳机状态，如果为1，则为插入状态
					int state = intent.getIntExtra("state", -1);
					// 判断：耳机监控开启，耳机插入，已经播放过，是由系统自动暂停的
					if (mPlugSetting && state == 1 && mTimer != 0 && !mPauseByUser) {
						// 则 播放音乐
						go();
						// 初始化通知栏
						initNotify();
					}
					break;
			}
		}
	}

	public class MyMusicBinder extends Binder {
		public MusicService getService() {
			return MusicService.this;
		}
	}

	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				if (mMediaPlayer != null) {
					int currentTime = mMediaPlayer.getCurrentPosition();
					mCurrentTimeIntent.putExtra(Constant.EXTRA_CURRENT_TIME, currentTime);
					mMainActivity.sendLocalbroadcast(mCurrentTimeIntent);
					this.sendEmptyMessageDelayed(1, 1000);
					Log.d("sendmsg", currentTime + "");
				}
			}
		}
	}

	private class MyMusicReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			switch (action) {
				case Constant.ACTION_CONTROL:
					int i = intent.getIntExtra(Constant.EXTRA_CONTROL, -1);
					switch (i) {
						case Constant.CONTROL_PREVIOUS:
							playPrev();
							break;
						case Constant.CONTROL_NEXT:
							playNext(mMediaPlayer.isPlaying());
							break;
						case Constant.CONTROL_PLAY:
							if (mIsFirstTime) {
								playSong();
								mMusicStateIntent.putExtra(Constant.EXTRA_PLAY_STATUS, Constant.IS_PLAYING);
								mMainActivity.sendLocalbroadcast(mMusicStateIntent);
							} else if (isPlaying()) {
								mPauseByUser = true;
								pausePlayer();
							} else go();
							initNotify();
							break;
						case Constant.CONTROL_STATE:
							mMusicState = (mMusicState + 1) % 3;
							Toast.makeText(mMainActivity, mStateMsg[mMusicState], Toast.LENGTH_SHORT).show();
							Intent intent1 = new Intent(Constant.ACTION_CONTROL_STATE);
							intent1.putExtra(Constant.EXTRA_CONTROL_STATE, mMusicState);
							Log.d("state", mMusicState + "");
							mMainActivity.sendLocalbroadcast(intent1);
							break;
						case Constant.CONTROL_SENSOR:
							// 判断当前歌曲播放时间与总时长是否大于30秒
							if (mDuration - mMediaPlayer.getCurrentPosition() > 30000) {
								// 若是，则播放下一曲
								playNext(mMediaPlayer.isPlaying());
								// 构造手机震动参数
								long[] args = {0, 30, 120, 30};
								// 手机微震两次
								mVibrator.vibrate(args, -1);
							} else {
								// 否则，循环播放目前歌曲
								playSong();
								// 手机微震1次
								mVibrator.vibrate(75);
							}
							break;
					}
					break;
				case Constant.ACTION_PLAY:
					mSongPosn = intent.getIntExtra(Constant.EXTRA_CURRENT_POSITION, 1);
					playSong();
					break;
				case Constant.ACTION_TIME_CHANGED:
					int extra = intent.getIntExtra(Constant.EXTRA_TIME_CHANGED, 0);
					if (extra >= mDuration) playNext(true);
					else {
						mMediaPlayer.seekTo(extra);
					}
					break;
				case Constant.ACTION_DELETE:
					int posn = intent.getIntExtra(Constant.EXTRA_DELETE_POSITION, -1);
					if (posn == mSongPosn) {
						playNext(true);
					}
					new MyCustomDeleteDBTask(mMusicInfos.get(posn).getId()).execute();
					break;
				case Constant.ACTION_LOVE:
					int love = intent.getIntExtra(Constant.EXTRA_LOVE_LEVEL, 0);
					long id = intent.getLongExtra(Constant.EXTRA_LOVE_ID, -1);
					if (id != -1) {
						new MyCustomUpdateDBLoveTask(love, id).execute();
					}
					break;
				case Constant.ACTION_SETTING:
					updateSetting();
					break;
				case Constant.ACTION_EXIT_SAVE:
					Log.d("exit", "到这了");
					exit();
					break;
			}
		}
	}

	private class MyCustomDeleteDBTask extends AsyncTask<Void, Integer, Boolean> {
		private long mId;

		public MyCustomDeleteDBTask(long id) {
			mId = id;
		}

		@Override
		protected void onPostExecute(Boolean aBoolean) {
			if (aBoolean) Toast.makeText(mMainActivity, "删除成功", Toast.LENGTH_SHORT).show();
			else Toast.makeText(mMainActivity, "删除失败", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			DBHelper dbHelper = new DBHelper(mMainActivity);
			return dbHelper.myDeleteDB(mId);
		}
	}

	private class MyCustomUpdateDBLoveTask extends AsyncTask<Void, Integer, Integer> {
		private int mLoveLevel;
		private long mId;

		public MyCustomUpdateDBLoveTask(int loveLevel, long id) {
			mLoveLevel = loveLevel;
			mId = id;
		}

		@Override
		protected void onPostExecute(Integer i) {
			if (i != -9451) Toast.makeText(mMainActivity, "操作成功\n当前热爱度为 " + i, Toast.LENGTH_SHORT).show();
			else Toast.makeText(mMainActivity, "操作失败", Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			DBHelper dbHelper = new DBHelper(mMainActivity);
			return dbHelper.customUpdateLove(mId, mLoveLevel);
		}
	}

	private class MyAutoUpdateDBTask extends AsyncTask<Void, Integer, Boolean> {
		private long id;

		public MyAutoUpdateDBTask(long songId) {
			id = songId;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			DBHelper dbHelper = new DBHelper(mMainActivity);
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			Cursor cursor = database.rawQuery("select " + DBHelper.AM_PLAYBACKS + "+" + DBHelper.PM_PLAYBACKS + "+" + DBHelper.MOON_PLAYBACKS + " as result from " + DBHelper.DEFAULT_TABLE, null);
			if (cursor.moveToFirst()) {
				do {
					int i = cursor.getInt(cursor.getColumnIndex("result"));
					Log.d("eixe", i + "");
				} while (cursor.moveToNext());
			}
			cursor.close();
			database.close();
			return dbHelper.autoUpdateLoveAndLastPlay(id);
		}
	}

	private class MyPhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
					if (mTimer != 0 && !mPauseByUser) {
						go();
						initNotify();
					}
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					if (mMediaPlayer.isPlaying()) {
						mPauseByUser = false;
						pausePlayer();
						initNotify();
					}
					break;
			}
		}
	}
}