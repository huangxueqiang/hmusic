package com.android.hhuimie.hmusic.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.*;
import android.widget.*;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.activities.MainActivity;
import com.android.hhuimie.hmusic.adapter.MusicListAdapter;
import com.android.hhuimie.hmusic.fab.FloatingActionButton;
import com.android.hhuimie.hmusic.model.Constant;
import com.android.hhuimie.hmusic.model.MusicInfo;
import com.android.hhuimie.hmusic.service.MusicService;
import com.android.hhuimie.hmusic.slideexpandableorpinned.ActionSlideExpandableListView;
import com.android.hhuimie.hmusic.slideexpandableorpinned.PinnedSectionListView;
import com.android.hhuimie.hmusic.utils.ComparatorUtil;
import com.android.hhuimie.hmusic.utils.DBHelper;
import com.android.hhuimie.hmusic.utils.LoveCompatatorUtil;
import com.android.hhuimie.hmusic.utils.MusicInfoUtil;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

public class MainFragment extends Fragment {
	private static final MainFragment mMainFragment = new MainFragment();
	private static final String[] sTitles = {"标题", "艺术家", "专辑", "添加时间", "最近播放"};
	private String mDurationFormat;
	private MusicService mMusicService;
	private SwipeRefreshLayout mRefreshLayout;
	private boolean mIsRefresh;
	private PinnedSectionListView mMusicList;
	private List<MusicInfo> mMusicInfos;
	private List<MusicInfo> mMusicInfosTemp;
	private MusicListAdapter mMusicListAdapter;
	private View view;
	private ImageButton mPreBtn;
	private ImageButton mPlayBtn;
	private ImageButton mNextBtn;
	private LinearLayout mMusicLayout;
	private TextView mMusicTitle;
	private ImageView mMusicAlbum;
	private TextView mMusicDuration;
	private Intent mControlIntent;
	private Intent mPlayIntent;
	private MainActivity mMainActivity;
	private int mChoice;
	private boolean mIsReverse;
	private ComparatorUtil<Object> comparatorUtil;
	private FloatingActionButton mFloatBtn;
	private boolean mIsPlayList;
	private boolean mIsLoveList;

	public void setIsLoveList(boolean isLoveList) {
		mIsLoveList = isLoveList;
	}
	public boolean getIsLoveList() {
		return mIsLoveList;
	}

	public MainFragment() {
		comparatorUtil = new ComparatorUtil<>(mChoice);
	}

	public static MainFragment getMainFragment() {
		return mMainFragment;
	}

	public MusicListAdapter getMusicListAdapter() {
		return mMusicListAdapter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_main, container, false);
		MyMainLocalReceiver localReceiver = new MyMainLocalReceiver();
		mMainActivity = (MainActivity) getActivity();
		mMainActivity.setReceiver(localReceiver);
		mControlIntent = new Intent(Constant.ACTION_CONTROL);
		mPlayIntent = new Intent(Constant.ACTION_PLAY);
		initViewById();
		setViewListener();
		initRefreshLaout();
		initMusicList();
		return view;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private void initViewById() {
		mMusicList = (PinnedSectionListView) view.findViewById(R.id.music_list);
		mRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
		mPreBtn = (ImageButton) view.findViewById(R.id.previous_music);
		mPlayBtn = (ImageButton) view.findViewById(R.id.play_music);
		mNextBtn = (ImageButton) view.findViewById(R.id.next_music);
		mMusicTitle = (TextView) view.findViewById(R.id.song_title);
		mMusicAlbum = (ImageView) view.findViewById(R.id.music_album);
		mMusicDuration = (TextView) view.findViewById(R.id.song_duration1);
		mMusicLayout = (LinearLayout) view.findViewById(R.id.song_layout);
	}

	public void resetToAllMusic() {
		mMusicInfos = new ArrayList<>(MusicInfoUtil.getMyMusicInfos(mMainActivity));
		mMusicInfosTemp = new ArrayList<>(mMusicInfos);
		sortMusicList();
		mMusicListAdapter = new MusicListAdapter(mMainActivity, mMusicInfos, mChoice);
		mMusicList.setAdapter(mMusicListAdapter);
		setServiceMusicList();
		mMusicList.startLayoutAnimation();
		mMusicList.requestFocus();
		setRefresh(true);
		mIsPlayList = false;
	}

	public void setServiceMusicList() {
		if (mMusicService == null) mMusicService = mMainActivity.getMusicSrv();
		mMusicService.setMusicList(mMusicInfos);
	}

	public void setMusicInfos(List<MusicInfo> musicInfos, int choice) {
		mMusicInfos = new ArrayList<>(musicInfos);
		mMusicInfosTemp = new ArrayList<>(musicInfos);
		if (choice == 0) {
			sortMusicList();
			mIsPlayList = true;
		} else {
			Collections.sort(mMusicInfos, new LoveCompatatorUtil<MusicInfo>());
			mIsPlayList = false;
		}
		mMusicListAdapter = new MusicListAdapter(mMainActivity, mMusicInfos, mChoice);
		if (choice == 2)
			mMusicListAdapter.setIsLoveList(true);
		mMusicList.setAdapter(mMusicListAdapter);
		setServiceMusicList();
		mMusicList.startLayoutAnimation();
		setRefresh(false);
		mMusicList.requestFocus();
	}

	public void setRefresh(boolean enable) {
		mRefreshLayout.setEnabled(enable);
	}

	private void setViewListener() {
		MyViewListener myViewListener = new MyViewListener();
		mPreBtn.setOnClickListener(myViewListener);
		mPlayBtn.setOnClickListener(myViewListener);
		mNextBtn.setOnClickListener(myViewListener);
		mMusicLayout.setOnClickListener(myViewListener);
	}


	private void initRefreshLaout() {
		mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (!mIsRefresh) {
					mIsRefresh = true;
					new MyRefreshTask().execute();
				}
			}
		});
		mRefreshLayout.setColorSchemeResources(android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
	}

	private void initMusicList() {
		int duration = 150;
		AnimationSet set = new AnimationSet(true);
		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(duration);
		set.addAnimation(animation);
		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				                                  Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				                                  -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(duration);
		set.addAnimation(animation);
		LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
		controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
		mMusicList.setLayoutAnimation(controller);
		mMusicInfos = new ArrayList<>(MusicInfoUtil.getMyMusicInfos(mMainActivity));
		mMusicInfosTemp = new ArrayList<>(mMusicInfos);
		sortMusicList();
		mMusicListAdapter = new MusicListAdapter(mMainActivity, mMusicInfos, mChoice);
		mMusicList.setAdapter(mMusicListAdapter);
		mMusicList.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});
		mFloatBtn = (FloatingActionButton) view.findViewById(R.id.fab);
		mFloatBtn.attachToListView(mMusicList);
		mFloatBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MusicListSortFragment fragment = MusicListSortFragment.newFragment(mChoice, mIsReverse);
				fragment.setTargetFragment(MainFragment.this, 0);
				fragment.show(mMainActivity.getSupportFragmentManager(), "sort");
			}
		});
		mMusicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mPlayIntent.putExtra(Constant.EXTRA_CURRENT_POSITION, position);
				mMainActivity.sendLocalbroadcast(mPlayIntent);
			}
		});
		mMusicList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(mMainActivity, position + "", Toast.LENGTH_SHORT).show();
				return true;
			}
		});
		mMusicList.setItemActionListener(new ActionSlideExpandableListView.OnActionClickListener() {
			@Override
			public void onClick(View itemView, View clickedView, int position) {
				List<MusicInfo> temp;
				if (MainActivity.IS_SEARCHING) {
					temp = mMainActivity.getSearchCount();
				} else temp = mMusicInfos;
				switch (clickedView.getId()) {
					case R.id.slide_detail:
						DetailDialogFragment fragment = DetailDialogFragment.newInstance(temp.get(position));
						fragment.setTargetFragment(MainFragment.this, 2);
						fragment.show(mMainActivity.getSupportFragmentManager(), "detail");
						break;
					case R.id.slide_like:
						LoveDialogFragment loveFragment = LoveDialogFragment.newInstance(temp.get(position));
						loveFragment.setTargetFragment(MainFragment.this, 2);
						loveFragment.show(mMainActivity.getSupportFragmentManager(), "love");
						break;
					case R.id.slide_plus:
						PlayListDialogFragment fragment2 = PlayListDialogFragment.newInstance(temp.get(position).getId());
						fragment2.show(mMainActivity.getSupportFragmentManager(), "add");
						break;
					case R.id.slide_delete:
						if (mIsPlayList) {
							DeleteInPlayListDialogFragment fragment1 = DeleteInPlayListDialogFragment.newInstance(mMainActivity.getPlayListName(), temp.get(position), position);
							fragment1.show(mMainActivity.getSupportFragmentManager(), "delete2");
						} else {
							DeleteDialogFragment fragment1 = DeleteDialogFragment.newInstance(position);
							fragment1.setTargetFragment(MainFragment.this, 1);
							fragment1.show(mMainActivity.getSupportFragmentManager(), "delete");
						}
						break;
				}
				mMusicList.collapse();
			}
		}, R.id.slide_detail, R.id.slide_like, R.id.slide_plus, R.id.slide_delete);
	}

	public FloatingActionButton getFloatBtn() {
		return mFloatBtn;
	}

	public ActionSlideExpandableListView getMusicList() {
		return mMusicList;
	}

	public List<MusicInfo> getMusicInfos() {
		return mMusicInfos;
	}

	public List<MusicInfo> getMusicInfosTemp() {
		return mMusicInfosTemp;
	}

	public int getChoice() {
		return mChoice;
	}

	public void changeTitle() {
		mMainActivity.getActionBar().setTitle(sTitles[mChoice]);
	}

	public void sortMusicList() {
		comparatorUtil.setChoice(mChoice);
		Collections.sort(mMusicInfos, comparatorUtil);
		if (mIsReverse) Collections.reverse(mMusicInfos);
		// 声明额外头部信息list
		List<String> extraStrings;
		// 获得类对象
		Class<? extends List> aClass = mMusicInfos.getClass();
		try {
			// 获得add方法
			Method method = aClass.getMethod("add", int.class, Object.class);
			// 获得在之前排序过程中生成的头部信息
			extraStrings = comparatorUtil.getLabel(mChoice);
			// 头部信息也要排序，不然插入不了
			Collections.sort(extraStrings, comparatorUtil);
			// 对每一首歌，获取出对应的头部信息，即标题排序中的“A，B，C”或艺术家排序中的艺术家
			for (int i = 0, j = 0; i < mMusicInfos.size(); i++) {
				// 如果判断相等
				if (mMusicInfos.get(i).get(mChoice).compareToIgnoreCase(extraStrings.get(j)) == 0) {
					// 则调用反射机制，把对应的头部信息插入到音乐列表中
					method.invoke(mMusicInfos, i, extraStrings.get(j));
					// 索引递增
					j++;
					i++;
					// 如果头部信息已经遍历完毕，则退出循环即可
					if (extraStrings.size() == j) break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == 0) {
			int choice = data.getIntExtra(Constant.EXTRA_SORT_CHOICE, 0);
			boolean isReverse = data.getBooleanExtra(Constant.EXTRA_SORT_IS_REVERSE, false);
			if (choice == mChoice && isReverse == mIsReverse) return;
			mChoice = choice;
			mIsReverse = isReverse;
			mMainActivity.setIsSearched(false);
			if (choice == ComparatorUtil.CHOICE_LASTPLAY) {
				new MyLastPlayTask().execute();
			} else {
				mMusicInfos = new ArrayList<>(mMusicInfosTemp);
				sortMusicList();
				mMusicListAdapter = new MusicListAdapter(mMainActivity, mMusicInfos, mChoice);
				mMusicList.setAdapter(mMusicListAdapter);
				setServiceMusicList();
				mMusicList.startLayoutAnimation();
				changeTitle();
			}
		} else if (requestCode == 2 && resultCode == 0) {
			MusicInfo info = (MusicInfo) data.getSerializableExtra(Constant.EXTRA_CURRENT_SONG);
			int level = data.getIntExtra(Constant.EXTRA_LOVE_LEVEL, 0);
			boolean toReduce = data.getBooleanExtra(Constant.EXTRA_LOVE_TO_REDUCE, false);
			if (info != null) {
				Intent intent = new Intent(Constant.ACTION_LOVE);
				if (toReduce) level = -level;
				intent.putExtra(Constant.EXTRA_LOVE_LEVEL, level);
				intent.putExtra(Constant.EXTRA_LOVE_ID, info.getId());
				mMainActivity.sendLocalbroadcast(intent);
			}
		} else if (requestCode == 1 && resultCode == 0) {
			int delete = data.getIntExtra(Constant.EXTRA_CURRENT_DELETE, -1);
			List<MusicInfo> temp;
			MusicListAdapter tempAdapter;
			if (MainActivity.IS_SEARCHING) {
				temp = mMainActivity.getSearchCount();
				tempAdapter = mMainActivity.getMusicListAdapter();
			} else {
				temp = mMusicInfos;
				tempAdapter = mMusicListAdapter;
			}
			String url = temp.get(delete).getUrl();
			File file = new File(url);
			boolean isDelete = file.delete();
			if (!isDelete) {
				Toast.makeText(mMainActivity, "删除失败,请重试", Toast.LENGTH_SHORT).show();
			} else {
				mMainActivity.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MediaStore.Audio.Media.DATA + " = '" + url + "'", null);
				temp.remove(delete);
				checkIfPinned(tempAdapter, temp, delete);
				tempAdapter.notifyDataSetChanged();
				refreshMusicInfos(mMusicInfosTemp, url);
				if (MainActivity.IS_SEARCHING) {
					mMusicInfos = new ArrayList<>(mMusicInfosTemp);
					sortMusicList();
				}
				Intent intent = new Intent(Constant.ACTION_DELETE);
				intent.putExtra(Constant.EXTRA_DELETE_POSITION, delete);
				mMainActivity.sendLocalbroadcast(intent);
				mMusicListAdapter.notifyDataSetInvalidated();
			}
		}
	}

	private void refreshMusicInfos(List<MusicInfo> musicInfos, String url) {
		for (int i = 0; i < musicInfos.size(); i++) {
			if (url.equals(musicInfos.get(i).getUrl())) {
				musicInfos.remove(i);
				break;
			}
		}
	}

	public void checkIfPinned(MusicListAdapter adapter, List<MusicInfo> musicInfos, int position) {
		if (adapter.getItemViewType(position - 1) == MusicListAdapter.TYPE_PINNED) {
			if (position == musicInfos.size() || adapter.getItemViewType(position) == MusicListAdapter.TYPE_PINNED) {
				musicInfos.remove(position - 1);
			}
		}
	}

	public void newMyLastPlayTask() {
		new MyLastPlayTask().execute();
	}

	private class MyRefreshTask extends AsyncTask<Void, Integer, Boolean> {
		@Override
		protected void onPostExecute(Boolean aBoolean) {
			mMusicList.setAdapter(mMusicListAdapter);
			mMusicListAdapter.notifyDataSetChanged();
			mMusicList.startLayoutAnimation();
			Toast.makeText(mMainActivity, "刷新成功", Toast.LENGTH_SHORT).show();
			mIsRefresh = false;
			mRefreshLayout.setRefreshing(false);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			new DBHelper(mMainActivity).myUpgradeDB();
			mMusicInfos = new ArrayList<>(MusicInfoUtil.getMyDBMusicInfos(mMainActivity));
			mMusicInfosTemp = new ArrayList<>(mMusicInfos);
			sortMusicList();
			mMusicListAdapter = new MusicListAdapter(mMainActivity, mMusicInfos, mChoice);
			mIsPlayList = false;
			return true;
		}
	}

	private class MyViewListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.previous_music:
					mControlIntent.putExtra(Constant.EXTRA_CONTROL, Constant.CONTROL_PREVIOUS);
					mMainActivity.sendLocalbroadcast(mControlIntent);
					break;
				case R.id.next_music:
					mControlIntent.putExtra(Constant.EXTRA_CONTROL, Constant.CONTROL_NEXT);
					mMainActivity.sendLocalbroadcast(mControlIntent);
					break;
				case R.id.play_music:
					mControlIntent.putExtra(Constant.EXTRA_CONTROL, Constant.CONTROL_PLAY);
					mMainActivity.sendLocalbroadcast(mControlIntent);
					break;
				case R.id.song_layout:
					mMainActivity.changeToPlayFragment();
					break;
			}
		}
	}

	private class MyMainLocalReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Constant.ACTION_CURRENT_MSG)) {
				MusicInfo info = (MusicInfo) intent.getSerializableExtra(Constant.EXTRA_CURRENT_SONG);
				long id = info.getId();
				long duration = info.getDuration();
				long album = info.getAlbumId();
				String title = info.getTitle();
				Bitmap bitmap = MusicInfoUtil.getArtwork(mMainActivity, id, album, true, true);
				if (bitmap != null) {
					mMusicAlbum.setImageBitmap(bitmap);
				} else {
					mMusicAlbum.setImageResource(R.drawable.music5);
				}
				mMusicTitle.setText(title);
				mDurationFormat = MusicInfoUtil.getFormatDuration(duration);
				mMusicDuration.setText("00:00 / " + mDurationFormat);
			} else if (action.equals(Constant.ACTION_PLAY_STATUS)) {
				int status = intent.getIntExtra(Constant.EXTRA_PLAY_STATUS, -1);
				if (status == Constant.IS_PAUSED) {
					mPlayBtn.setImageResource(R.drawable.pause_selector);
				} else if (status == Constant.IS_PLAYING) {
					mPlayBtn.setImageResource(R.drawable.play_selector);
				}
			} else if (action.equals(Constant.ACTION_SETTING)) {
			} else if (MainFragment.this.isHidden()) {
			} else if (action.equals(Constant.ACTION_CURRENT_TIME)) {
				int currentTime = intent.getIntExtra(Constant.EXTRA_CURRENT_TIME, -1);
				String s1 = MusicInfoUtil.getFormatDuration(currentTime);
				mMusicDuration.setText(s1 + " / " + mDurationFormat);
			}
		}
	}

	private class MyLastPlayTask extends AsyncTask<Void, Integer, Boolean> {
		@Override
		protected void onPostExecute(Boolean aBoolean) {
			if (aBoolean) {
				mMusicListAdapter = new MusicListAdapter(mMainActivity, mMusicInfos, mChoice);
				mMusicList.setAdapter(mMusicListAdapter);
				setServiceMusicList();
				mMusicList.startLayoutAnimation();
				mMainActivity.getActionBar().setTitle("最近播放");
				mFloatBtn.setVisibility(View.GONE);
				setRefresh(false);
				mIsLoveList = false;
			}
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			List<MusicInfo> lastPlayList = new ArrayList<>();
			Map<Long, Long> map = new DBHelper(mMainActivity).myGetLastPlayList();
			if (map.size() == 0) return false;
			Set<Long> idSet = map.keySet();
			List<MusicInfo> temp = MusicInfoUtil.getMyMusicInfos(mMainActivity);
			for (MusicInfo info : temp) {
				for (long id : idSet) {
					if (id == info.getId()) {
						info.setLastPlay(map.get(id));
						lastPlayList.add(info);
					}
				}
			}
			mMusicInfos = new ArrayList<>(lastPlayList);
			int tempChoice = mChoice;
			boolean tempIsReverse = mIsReverse;
			mChoice = ComparatorUtil.CHOICE_LASTPLAY;
			mIsReverse = false;
			sortMusicList();
			mChoice = tempChoice;
			mIsReverse = tempIsReverse;
			return true;
		}
	}
}