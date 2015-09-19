package com.android.hhuimie.hmusic.activities;

import android.app.ActionBar;
import android.content.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;
import com.android.hhuimie.hmusic.FloatWindow.MyWindowManager;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.ResideMenu.ResideMenu;
import com.android.hhuimie.hmusic.ResideMenu.ResideMenuItem;
import com.android.hhuimie.hmusic.adapter.MusicListAdapter;
import com.android.hhuimie.hmusic.fragments.*;
import com.android.hhuimie.hmusic.model.Constant;
import com.android.hhuimie.hmusic.model.MusicInfo;
import com.android.hhuimie.hmusic.service.MusicService;
import com.android.hhuimie.hmusic.service.MusicService.MyMusicBinder;
import com.android.hhuimie.hmusic.slideexpandableorpinned.ActionSlideExpandableListView;
import com.android.hhuimie.hmusic.utils.ComparatorUtil;
import com.android.hhuimie.hmusic.utils.DBHelper;
import com.android.hhuimie.hmusic.utils.MusicInfoUtil;
import com.android.hhuimie.hmusic.utils.SettingManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends FragmentActivity implements SensorEventListener {
	public static boolean IS_SEARCHING;
	private IntentFilter mIntentFilter;
	private String mOldTitle;
	private SearchView mSearchView;
	private MenuItem mSearchItem;
	private MyServiceConnection mMusicConnection = new MyServiceConnection();
	private SensorManager mSensorManager;
	private BroadcastReceiver mScreenReceiver;
	private LocalBroadcastManager mBroadcastManager;
	private MyLocalReceiver mLocalReceiver;
	private MusicService mMusicSrv;
	private Intent mPlayIntent;
	private Fragment mCurrentFragment;
	private boolean mIsSearched;
	private ActionSlideExpandableListView mMusicList;
	private String mCurrentSearchTips;
	private MyHandler mHandler;
	private ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(10);
	private MainFragment mMainFragment;
	private PlayFragment mPlayFragment;
	private ResideMenu mResideMenu;
	private MusicListAdapter mMusicListAdapter;
	private ArrayList<MusicInfo> mSearchCount;
	private List<BroadcastReceiver> mReceivers;
	private long start;
	private int mLoveChoice = -1;
	private String mPlayListName;
	private boolean mIsInitSensor;
	private boolean mFloatSetting;
	private boolean mSearchSetting;
	private boolean mSearchTitleSetting;
	private boolean mSearchArtistSetting;
	private boolean mSearchAlbumSetting;

	public LocalBroadcastManager getBroadcastManager() {
		return mBroadcastManager;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			mMainFragment = MainFragment.getMainFragment();
			mPlayFragment = PlayFragment.getPlayFragment();
			getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mMainFragment).commit();
			getSupportFragmentManager().beginTransaction().hide(mMainFragment).add(R.id.fragment_container, mPlayFragment).commit();
			mCurrentFragment = mPlayFragment;
		}
		initBrocast();
		initResideMenu();
		updateSensorSetting();
	}

	private void updateSearchSetting() {
		mSearchSetting = SettingManager.getSingleSetting(this, SettingManager.sSEARCH_SETTING);
		if (mSearchSetting) {
			initSearch();
			mSearchTitleSetting = SettingManager.getSingleSetting(this, SettingManager.sSEARCH_TITLE_SETTING);
			mSearchArtistSetting = SettingManager.getSingleSetting(this, SettingManager.sSEARCH_ARTIST_SETTING);
			mSearchAlbumSetting = SettingManager.getSingleSetting(this, SettingManager.sSEARCH_ALBUM_SETTING);
		} else {
			if (mIsInitSearch) {
				setSearchVisiblity(View.GONE, false);
			}
		}
	}

	private void updateSensorSetting() {
		boolean sensorSetting = SettingManager.getSingleSetting(this, SettingManager.sSENSOR_SETTING);
		if (sensorSetting) {
			initSensor();
		} else {
			cancelSensor();
		}
	}

	private void initBrocast() {
		mBroadcastManager = LocalBroadcastManager.getInstance(this);
		mLocalReceiver = new MyLocalReceiver();
		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(Constant.ACTION_INTENT);
		mIntentFilter.addAction(Constant.ACTION_CURRENT_MSG);
		mIntentFilter.addAction(Constant.ACTION_CURRENT_TIME);
		mIntentFilter.addAction(Constant.ACTION_PLAY);
		mIntentFilter.addAction(Constant.ACTION_PLAY_STATUS);
		mIntentFilter.addAction(Constant.ACTION_CONTROL);
		mIntentFilter.addAction(Constant.ACTION_TIME_CHANGED);
		mIntentFilter.addAction(Constant.ACTION_DELETE);
		mIntentFilter.addAction(Constant.ACTION_LOVE);
		mIntentFilter.addAction(Constant.ACTION_CONTROL_STATE);
		mIntentFilter.addAction(Constant.ACTION_EXIT_SAVE);
		mIntentFilter.addAction(Constant.ACTION_NOT_CONTROL);
		mIntentFilter.addAction(Constant.ACTION_SETTING);
		mReceivers = new ArrayList<>();
		mBroadcastManager.registerReceiver(mLocalReceiver, mIntentFilter);
	}

	private void initSensor() {
		if (!mIsInitSensor) {
			mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
			mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
			mScreenReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
						mSensorManager.unregisterListener(MainActivity.this);
						mSensorManager.registerListener(MainActivity.this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
					}
				}
			};
			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
			registerReceiver(mScreenReceiver, filter);
			mIsInitSensor = true;
		}
	}

	private void cancelSensor() {
		if (mIsInitSensor) {
			unregisterReceiver(mScreenReceiver);
			mSensorManager.unregisterListener(MainActivity.this);
			mIsInitSensor = false;
		}
	}

	private void initResideMenu() {
		mResideMenu = new ResideMenu(this);
		mResideMenu.setBackground(R.drawable.menu_background);
		mResideMenu.attachToActivity(this);
		ResideMenuItem leftPlayingItem = new ResideMenuItem(this, R.drawable.ic_action_playing, "正在播放");
		ResideMenuItem leftMediaItem = new ResideMenuItem(this, R.drawable.ic_action_playlists, "媒体库");
		ResideMenuItem rightDetailItem = new ResideMenuItem(this, R.drawable.ic_action_detail_light, "详情");
		ResideMenuItem rightLoveLevelItem = new ResideMenuItem(this, R.drawable.ic_action_love_light, "热爱度");
		ResideMenuItem rightPlusItem = new ResideMenuItem(this, R.drawable.ic_action_plus_light, "添加到...");
		ResideMenuItem rightPlayListItem = new ResideMenuItem(this, R.drawable.ic_action_playlists, "播放列表");
		ResideMenuItem rightLoveListItem = new ResideMenuItem(this, R.drawable.ic_action_love_light, "挚爱音乐");
		ResideMenuItem rightHistoryItem = new ResideMenuItem(this, R.drawable.ic_action_history, "最近播放");
		ResideMenuItem rightAllMusicItem = new ResideMenuItem(this, R.drawable.ic_action_all_music, "所有音乐");
		ResideMenuItem rightSettingItem = new ResideMenuItem(this, R.drawable.ic_action_setting, "设置");
		mResideMenu.addMenuItem(leftPlayingItem, ResideMenu.DIRECTION_LEFT);
		mResideMenu.addMenuItem(leftMediaItem, ResideMenu.DIRECTION_LEFT);
		mResideMenu.addPlayItem(rightDetailItem);
		mResideMenu.addPlayItem(rightLoveLevelItem);
		mResideMenu.addPlayItem(rightPlusItem);
		mResideMenu.addMainItem(rightAllMusicItem);
		mResideMenu.addMainItem(rightPlayListItem);
		mResideMenu.addMainItem(rightLoveListItem);
		mResideMenu.addMainItem(rightHistoryItem);
		mResideMenu.addMainItem(rightSettingItem);
		leftPlayingItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeToPlayFragment();
			}
		});
		leftMediaItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				changeToMainFragment();
			}
		});
		rightDetailItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MusicInfo currentSong = mPlayFragment.getCurrentSong();
				if (currentSong == null) {
					Toast.makeText(MainActivity.this, "暂无播放歌曲,请播放后重试", Toast.LENGTH_SHORT).show();
				} else {
					DetailDialogFragment dialog = DetailDialogFragment.newInstance(currentSong);
					dialog.show(getSupportFragmentManager(), "detail");
				}
				mResideMenu.closeMenu();
			}
		});
		rightLoveLevelItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MusicInfo currentSong = mPlayFragment.getCurrentSong();
				if (currentSong == null) {
					Toast.makeText(MainActivity.this, "暂无播放歌曲,请播放后重试", Toast.LENGTH_SHORT).show();
				} else {
					LoveDialogFragment dialog = LoveDialogFragment.newInstance(currentSong);
					dialog.setTargetFragment(mMainFragment, 2);
					dialog.show(getSupportFragmentManager(), "love");
				}
				mResideMenu.closeMenu();

			}
		});
		rightPlusItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MusicInfo currentSong = mPlayFragment.getCurrentSong();
				if (currentSong == null) {
					Toast.makeText(MainActivity.this, "暂无播放歌曲,请播放后重试", Toast.LENGTH_SHORT).show();
				} else {
					PlayListDialogFragment fragment2 = PlayListDialogFragment.newInstance(currentSong.getId());
					fragment2.show(MainActivity.this.getSupportFragmentManager(), "add");
					mResideMenu.closeMenu();

				}
			}
		});
		rightPlayListItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SelectPlayListDialogFragment fragment = new SelectPlayListDialogFragment();
				fragment.show(getSupportFragmentManager(), "selectPlayList");
				mResideMenu.closeMenu();

			}
		});
		rightLoveListItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LovePlaylistsDialogFragment fragment = LovePlaylistsDialogFragment.newInstance(mLoveChoice);
				fragment.show(getSupportFragmentManager(), "loveplaylist");
				mResideMenu.closeMenu();

			}
		});
		rightHistoryItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMainFragment.newMyLastPlayTask();
				mResideMenu.closeMenu();
				mMainFragment.getMusicList().requestFocus();
			}
		});
		rightAllMusicItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mMainFragment.resetToAllMusic();
				mMainFragment.getFloatBtn().setVisibility(View.VISIBLE);
				mMainFragment.changeTitle();
				mResideMenu.closeMenu();
			}
		});
		rightSettingItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivityForResult(intent, 0);
//				SettingDialogFragment fragment = new SettingDialogFragment();
//				fragment.show(getSupportFragmentManager(), "setting");
				mResideMenu.closeMenu();
			}
		});
	}

	public void setIsSearched(boolean isSearched) {
		mIsSearched = isSearched;
	}

	public void setReceiver(BroadcastReceiver receiver) {
		mBroadcastManager.registerReceiver(receiver, mIntentFilter);
		mReceivers.add(receiver);
	}

	public void sendLocalbroadcast(Intent intent) {
		mBroadcastManager.sendBroadcast(intent);
	}

	@Override
	public void onBackPressed() {
		ExitDialogFragment fragment = new ExitDialogFragment();
		fragment.show(getSupportFragmentManager(), "exit");
	}

	public MusicListAdapter getMusicListAdapter() {
		return mMusicListAdapter;
	}

	public ArrayList<MusicInfo> getSearchCount() {
		return mSearchCount;
	}

	public MusicService getMusicSrv() {
		return mMusicSrv;
	}

	public void changeToMainFragment() {
		changeFragment(mMainFragment);
		mResideMenu.closeMenu();
		ActionBar actionBar = getActionBar();
		if (mOldTitle == null)
			actionBar.setTitle("标题");
		else actionBar.setTitle(mOldTitle);

		if (mSearchSetting) {
			setSearchVisiblity(View.VISIBLE, true);
		}
		mResideMenu.resetmenu(0);
	}

	public void changeToPlayFragment() {
		changeFragment(mPlayFragment);
		mResideMenu.closeMenu();
		ActionBar actionBar = getActionBar();
		mOldTitle = (String) actionBar.getTitle();
		actionBar.setTitle("HMusic");
		if (mSearchSetting) {
			setSearchVisiblity(View.GONE, false);
		}
		mResideMenu.resetmenu(1);
	}

	public void addIgnoredView(View view) {
		mResideMenu.addIgnoredView(view);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// 获得传感器事件的类型
		int sensorType = event.sensor.getType();
		// 获得3个坐标值，当手机正面朝上是，三个坐标值分别为{0,0,10}
		// 当手机正面朝下时，坐标值分别为{0,0,-10}
		// 根据这数值来判断屏幕方向
		float[] values = event.values;
		if (sensorType == Sensor.TYPE_ACCELEROMETER) {
			// 粗略判断手机是否是正面朝下
			if ((Math.abs(values[0]) < 4) && (Math.abs(values[1]) < 4) && (values[2] < -6)) {
				// 若是，再判断如果还未记录操作的时间，则赋值到当前系统的毫秒值
				if (start == 0)
					start = System.currentTimeMillis();
			}
			// 如果当前手机不是正面朝下，判断当前系统时间减去操作的时间是否在1秒内
			else if (System.currentTimeMillis() - start < 1000) {
				// 如果是，粗略判断当前屏幕是否正面朝上
				if ((Math.abs(values[0]) < 4) && (Math.abs(values[1]) < 4) && (values[2] > 6)) {
					// 若是，则满足手势操作，即手机翻转至正面朝下，一秒内翻转回正面朝上
					// 构建意图
					Intent intent = new Intent(Constant.ACTION_CONTROL);
					// 设置额外数据为传感器
					intent.putExtra(Constant.EXTRA_CONTROL, Constant.CONTROL_SENSOR);
					// 发送本地广播
					mBroadcastManager.sendBroadcast(intent);
					// 操作时间重设为0；
					start = 0;
				}
			}
			// 如果整个过程时间大于1秒，则无效，重设操作时间为0
			else start = 0;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	protected void onStop() {
		super.onStop();
		mFloatSetting = SettingManager.getSingleSetting(this, SettingManager.sFLOAT_SETTING);
		if (mFloatSetting && mMusicSrv != null && !MyWindowManager.isApplicationShowing(getPackageName(), this)) {
			mMusicSrv.showFloatWindow();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mFloatSetting && mMusicSrv != null && MyWindowManager.isApplicationShowing(getPackageName(), this)) {
			mMusicSrv.removeFloatWindow();
		}
	}

	public void handleLovePlayList(int choice) {
		Log.d("idset", choice + "");
		if (choice == -1) return;
		mLoveChoice = choice;
		List<List<Long>> lovePlay = new DBHelper(this).getLovePlaylists(choice);
		if (lovePlay == null) return;
		List<MusicInfo> temp = MusicInfoUtil.getMyMusicInfos(this);
		List<Long> ids = lovePlay.get(0);
		List<Long> times = lovePlay.get(1);
		int size = ids.size();
		List<MusicInfo> lovePlayLists = new ArrayList<>(size);
		for (MusicInfo info : temp) {
			int i = Collections.binarySearch(ids, info.getId());
			if (i > -1) {
				info.setPlaybacks(times.get(i));
				Log.d("eoxz", info.getTitle() + "---" + info.getPlaybacks());
				lovePlayLists.add(info);
				if (--size == 0) break;
			}
		}
		getActionBar().setTitle("挚爱音乐");
		mMainFragment.setMusicInfos(lovePlayLists, 2);
		mMainFragment.setIsLoveList(true);
		mMainFragment.getFloatBtn().setVisibility(View.GONE);
	}

	public String getPlayListName() {
		return mPlayListName;
	}

	public void handlePlayList(String name) {
		if (name == null) return;
		mPlayListName = name;
		List<Long> ids = new DBHelper(this).getPlayListByName(name);
		if(ids == null){
			Toast.makeText(this,"该播放列表中暂无歌曲",Toast.LENGTH_SHORT).show();
			return;
		}
		int size = ids.size();
		List<MusicInfo> playListMusicInfos = new ArrayList<>(size);
		List<MusicInfo> myMusicInfos = MusicInfoUtil.getMyMusicInfos(this);
		for (MusicInfo info : myMusicInfos) {
			if (Collections.binarySearch(ids, info.getId()) > -1) {
				playListMusicInfos.add(info);
				Log.d("eieix", size + "");
				if (--size == 0) {
					Log.d("eieix", playListMusicInfos.toString());
					break;
				}
			}
		}
		getActionBar().setTitle(mPlayListName);
		mMainFragment.setMusicInfos(playListMusicInfos, 0);
		mMainFragment.setIsLoveList(false);
		mMainFragment.getFloatBtn().setVisibility(View.VISIBLE);
	}

	private boolean mIsInitSearch;

	private void initSearch() {
		if (mIsInitSearch) {
			setSearchVisiblity(View.VISIBLE, true);
			return;
		}
		mSearchView = (SearchView) mSearchItem.getActionView();
		SearchActionExpandListener expandListener = new SearchActionExpandListener();
		mSearchItem.setOnActionExpandListener(expandListener);
		mSearchView.setOnSearchClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mHandler == null) mHandler = new MyHandler();
			}
		});
		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				if (newText != null && !newText.isEmpty()) {
					mCurrentSearchTips = newText;
					scheduledExecutor.schedule(new MySearchTipsThread(newText), 300, TimeUnit.MILLISECONDS);
				}
				return true;
			}
		});
		setSearchVisiblity(View.VISIBLE, true);
		mIsInitSearch = true;
	}

	private void setSearchVisiblity(int visibility, boolean visible) {
		mSearchView.setVisibility(visibility);
		mSearchItem.setVisible(visible);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		mSearchItem = menu.findItem(R.id.action_search);
		updateSearchSetting();
		if (mSearchSetting) {
			initSearch();
			setSearchVisiblity(View.GONE, false);
		}
		mSearchItem.setVisible(false);
		return true;
	}

	private void restoreMusicList() {
		List<MusicInfo> musicInfos = new ArrayList<>(mMainFragment.getMusicInfos());
		if (mMusicList == null) mMusicList = mMainFragment.getMusicList();
		mMusicList.setAdapter(new MusicListAdapter(MainActivity.this, musicInfos, mMainFragment.getChoice()));
		mMusicSrv.setMusicList(musicInfos);
		mMusicList.startLayoutAnimation();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mPlayIntent == null) {
			mPlayIntent = new Intent(this, MusicService.class);
			bindService(mPlayIntent, mMusicConnection, Context.BIND_AUTO_CREATE);
			startService(mPlayIntent);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cancelSensor();
		if (mFloatSetting)
			mMusicSrv.removeFloatWindow();
		mBroadcastManager.unregisterReceiver(mLocalReceiver);
		for (BroadcastReceiver receiver : mReceivers) {
			mBroadcastManager.unregisterReceiver(receiver);
		}
		unbindService(mMusicConnection);
		stopService(mPlayIntent);
		mMusicSrv.removeMsg();
		mMusicSrv = null;
	}

	private boolean containsIgnoreCase(String src, String obj) {
		// 自定义方法，返回src是否包含obj字符串，无视大小写
		return obj.length() <= src.length() && src.toUpperCase().contains(obj.toUpperCase());
	}

	private void changeFragment(Fragment to) {
		getSupportFragmentManager().beginTransaction().hide(mCurrentFragment).show(to).commit();
		mCurrentFragment = to;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mBroadcastManager.sendBroadcast(new Intent(Constant.ACTION_SETTING));
	}

	public class SearchActionExpandListener implements MenuItem.OnActionExpandListener {
		@Override
		public boolean onMenuItemActionExpand(MenuItem item) {
			return true;
		}

		@Override
		public boolean onMenuItemActionCollapse(MenuItem item) {
			if (mIsSearched) {
				restoreMusicList();
				mIsSearched = false;
				IS_SEARCHING = false;
				mSearchItem.collapseActionView();
			}
			return true;
		}
	}

	private class MySearchTipsThread implements Runnable {
		String newText;

		public MySearchTipsThread(String newText) {
			this.newText = newText;
		}

		public void run() {
			if (newText != null && newText.equals(mCurrentSearchTips)) {
				mHandler.sendMessage(mHandler.obtainMessage(1, newText));
			}
		}
	}

	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					if (!mSearchSetting)
						return;
					mIsSearched = true;
					IS_SEARCHING = true;
					List<MusicInfo> musicInfosTemp = mMainFragment.getMusicInfosTemp();
					String searchMsg = (String) msg.obj;
					ArrayList<MusicInfo> searchTitle = new ArrayList<>();
					ArrayList<MusicInfo> searchArtist = new ArrayList<>();
					ArrayList<MusicInfo> searchAlbum = new ArrayList<>();
					ArrayList<ArrayList<MusicInfo>> searchTypeList = new ArrayList<>(3);
					searchTypeList.add(0, searchTitle);
					searchTypeList.add(1, searchArtist);
					searchTypeList.add(2, searchAlbum);
					mSearchCount = new ArrayList<>();
					// 为了得到搜索的类型，用int数组存储
					int[] searchType = new int[3];
					// 首先判断是否搜索标题，若是
					if (mSearchTitleSetting) {
						// 由于searchType[0]=0，所以这里省略不写
						// 再判断是否搜索艺术家，若是
						if (mSearchArtistSetting) {
							// 赋值，说明第二个搜索的是艺术家
							searchType[1] = 1;
							// 在判断是否搜索专辑，若是
							if (mSearchAlbumSetting) {
								// 赋值，说明第三个搜索的是专辑
								// 如果程序判断到这里，searchType的值为{0,1,2}，说明搜索全部类型
								searchType[2] = 2;
							}
							//若不搜索专辑，则searchType[3]=0，这时searchType的值为{0,1,0}，说明搜索标题和艺术家
						}
						// 若不搜索艺术家，则判断是否搜索专辑，若是
						else if (mSearchAlbumSetting) {
							// 赋值，这时searchType = {0,2,0}，说明搜索标题和专辑
							searchType[1] = 2;
						}
					}
					// 若不搜索标题，则判断是否搜索艺术家，若是
					else if (mSearchArtistSetting) {
						// 赋值第一个搜索参数就是艺术家
						searchType[0] = 1;
						// 再判断是否搜索专辑，若是
						if (mSearchAlbumSetting) {
							// 赋值第二个参数为专辑，此时searchType = {1,2,0}，说明搜索艺术家和专辑
							searchType[1] = 2;
						}
					}
					// 若是不搜索标题和艺术家，则判断是否搜索专辑，若是
					else if (mSearchAlbumSetting) {
						// 赋值，此时searchType = {2,0,0},说明只搜索专辑
						searchType[0] = 2;
					}
					// 如果第三个参数为2，则可以肯定是{0,1,2}，所以搜索标题，艺术家以及专辑
					if (searchType[2] == 2) {
						for (MusicInfo musicInfo : musicInfosTemp) {
							if (containsIgnoreCase(musicInfo.getTitle(), searchMsg)) {
								searchTitle.add(musicInfo);
								// 当标题满足搜索条件时，只把该歌曲添加到满足搜索标题的列表中
								// 不再搜索其艺术家以及专辑
								continue;
							}
							if (containsIgnoreCase(musicInfo.getArtist(), searchMsg)) {
								searchArtist.add(musicInfo);
								// 同理，如果该歌曲艺术家满足搜索条件时，不再搜索该歌曲的专辑
								continue;
							}
							if (containsIgnoreCase(musicInfo.getAlbum(), searchMsg)) {
								searchAlbum.add(musicInfo);
							}
						}
					}
					// 如果第二个参数为0，则证明用户只启用了一个搜索，如只搜索标题，或只搜索艺术家，或只搜索专辑
					else if (searchType[1] == 0) {
						// 获得要填充的列表
						ArrayList<MusicInfo> temp = searchTypeList.get(searchType[0]);
						// 对所有歌曲进行判断
						for (MusicInfo info : musicInfosTemp) {
							// 如果歌曲要搜索的条件包含了关键字，则添加
							if (containsIgnoreCase(info.getSearch(searchType[0]), searchMsg)) {
								temp.add(info);
							}
						}
					} else {
						ArrayList<MusicInfo> temp1 = searchTypeList.get(searchType[0]);
						ArrayList<MusicInfo> temp2 = searchTypeList.get(searchType[1]);
						for (MusicInfo info : musicInfosTemp) {
							if (containsIgnoreCase(info.getSearch(searchType[0]), searchMsg)) {
								temp1.add(info);
								continue;
							}
							if (containsIgnoreCase(info.getSearch(searchType[1]), searchMsg)) {
								temp2.add(info);
							}
						}
					}
					ComparatorUtil<Object> comparatorUtil = new ComparatorUtil<>(searchMsg, ComparatorUtil.CHOICE_SEARCH, ComparatorUtil.SEARCH_TITLE);
					if (searchTitle.size() != 0) {
						if (searchTitle.size() > 1) {
							Collections.sort(searchTitle, comparatorUtil);
						}
						try {
							searchTitle.getClass().getMethod("add", int.class, Object.class).invoke(searchTitle, 0, "标题");
						} catch (Exception e) {
							e.printStackTrace();
						}
						mSearchCount.addAll(searchTitle);
					}
					if (searchArtist.size() != 0) {
						if (searchArtist.size() > 1) {
							comparatorUtil.setSearchType(ComparatorUtil.SEARCH_ARTIST);
							Collections.sort(searchArtist, comparatorUtil);
						}
						try {
							searchArtist.getClass().getMethod("add", int.class, Object.class).invoke(searchArtist, 0, "艺术家");
						} catch (Exception e) {
							e.printStackTrace();
						}
						mSearchCount.addAll(searchArtist);
					}
					if (searchAlbum.size() != 0) {
						if (searchAlbum.size() > 1) {
							comparatorUtil.setSearchType(ComparatorUtil.SEARCH_ALBUM);
							Collections.sort(searchAlbum, comparatorUtil);
						}
						try {
							searchAlbum.getClass().getMethod("add", int.class, Object.class).invoke(searchAlbum, 0, "专辑");
						} catch (Exception e) {
							e.printStackTrace();
						}
						mSearchCount.addAll(searchAlbum);
					}
					mMusicList = mMainFragment.getMusicList();
					mMusicListAdapter = new MusicListAdapter(MainActivity.this, mSearchCount, searchMsg, searchTitle.size(), searchArtist.size());
					if (mMainFragment.getIsLoveList())
						mMusicListAdapter.setIsLoveList(true);
					mMusicList.setAdapter(mMusicListAdapter);
					mMusicSrv.setMusicList(mSearchCount);
					mMusicList.startLayoutAnimation();
					Log.d("rtrty", "title--" + searchTitle.toString());
					Log.d("rtrty", "artist--" + searchArtist.toString());
					Log.d("rtrty", "album--" + searchAlbum.toString());
					Log.d("rtrty", "count--" + mSearchCount.toString());
					break;
			}
			this.removeMessages(msg.what);
		}
	}

	private class MyServiceConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MyMusicBinder binder = (MyMusicBinder) service;
			mMusicSrv = binder.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
	}

	private class MyLocalReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constant.ACTION_SETTING)) {
				updateSensorSetting();
				updateSearchSetting();
			}
		}
	}
}
