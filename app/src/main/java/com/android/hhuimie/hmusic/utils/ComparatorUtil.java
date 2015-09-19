package com.android.hhuimie.hmusic.utils;

import com.android.hhuimie.hmusic.model.MusicInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ComparatorUtil<T> implements Comparator<T> {
	public static final int CHOICE_TITLE = 0;
	public static final int CHOICE_ARTIST = 1;
	public static final int CHOICE_ALBUM = 2;
	public static final int CHOICE_DATE = 3;
	public static final int CHOICE_LASTPLAY = 4;
	public static final int CHOICE_SEARCH = 5;
	public static final String SEARCH_TITLE = "TITLE";
	public static final String SEARCH_ARTIST = "ARTIST";
	public static final String SEARCH_ALBUM = "ALBUM";
	private String mSearchMsg;
	private String mSearchType;
	private List<String> mArtists = new ArrayList<>();
	private List<String> mDates = new ArrayList<>();
	private List<String> mAlbums = new ArrayList<>();
	private List<String> mTitles = new ArrayList<>();
	private List<String> mLastPlays = new ArrayList<>();
	private int mChoice = 0;

	public ComparatorUtil(String searchMsg, int choice, String searchType) {
		mSearchMsg = searchMsg.toUpperCase();
		mSearchType = searchType;
		mChoice = choice;
	}

	public ComparatorUtil(int choice) {
		mChoice = choice;
	}

	public void setSearchType(String searchType) {
		mSearchType = searchType;
	}

	public void setChoice(int choice) {
		mChoice = choice;
	}

	@Override
	public int compare(T lhs, T rhs) {
		if (lhs instanceof String) {
			return compareUnknownAlbumOrArtist((String) lhs, (String) rhs);
		}
		if (lhs instanceof MusicInfo) {
			MusicInfo info1 = (MusicInfo) lhs;
			MusicInfo info2 = (MusicInfo) rhs;
			int aReturn;
			switch (mChoice) {
				case CHOICE_TITLE:
					// 先判断标题，如果不等于0直接返回，如果等于0则判断艺术家，再则判断专辑
					return (aReturn = compareTitle(info1, info2)) != 0 ? aReturn :
							       (aReturn = compareArtist(info1, info2)) != 0 ? aReturn :
									       compareAlbum(info1, info2);
				case CHOICE_ARTIST:
					return (aReturn = compareArtist(info1, info2)) != 0 ? aReturn :
							       (aReturn = compareTitle(info1, info2)) != 0 ? aReturn :
									       compareAlbum(info1, info2);
				case CHOICE_ALBUM:
					return (aReturn = compareAlbum(info1, info2)) != 0 ? aReturn :
							       (aReturn = compareArtist(info1, info2)) != 0 ? aReturn :
									       compareTitle(info1, info2);
				case CHOICE_DATE:
					return (aReturn = compareDate(info1, info2)) != 0 ? aReturn :
							       (aReturn = compareTitle(info1, info2)) != 0 ? aReturn :
									       (aReturn = compareArtist(info1, info2)) != 0 ? aReturn : compareAlbum(info1, info2);
				case CHOICE_LASTPLAY:
					return (aReturn = compareLastPlay(info1, info2)) != 0 ? aReturn :
							       (aReturn = compareTitle(info1, info2)) != 0 ? aReturn :
									       (aReturn = compareArtist(info1, info2)) != 0 ? aReturn : compareAlbum(info1, info2);
				case CHOICE_SEARCH:
					switch (mSearchType) {
						case SEARCH_TITLE:
							return (aReturn = compareTitle(info1, info2)) != 0 ? aReturn :
									       (aReturn = compareArtist(info1, info2)) != 0 ? aReturn :
											       compareAlbum(info1, info2);
						case SEARCH_ARTIST:
							return (aReturn = compareArtist(info1, info2)) != 0 ? aReturn :
									       (aReturn = compareTitle(info1, info2)) != 0 ? aReturn :
											       compareAlbum(info1, info2);
						case SEARCH_ALBUM:
							return (aReturn = compareAlbum(info1, info2)) != 0 ? aReturn :
									       (aReturn = compareTitle(info1, info2)) != 0 ? aReturn :
											       compareArtist(info1, info2);

					}
					break;

			}
		}
		return 0;
	}

	private int compareLastPlay(MusicInfo info1, MusicInfo info2) {
		long date1 = info1.getLastPlay();
		long date2 = info2.getLastPlay();
		String s1 = MusicInfo.mSDF.format(date1);
		String s2 = MusicInfo.mSDF.format(date2);
		if (!mLastPlays.contains(s1)) mLastPlays.add(s1);
		if (!mLastPlays.contains(s2)) mLastPlays.add(s2);
		return date1 > date2 ? -1 : date1 < date2 ? 1 : 0;
	}

	private int compareDate(MusicInfo info1, MusicInfo info2) {
		long date1 = info1.getDateModified();
		long date2 = info2.getDateModified();
		if (mChoice == CHOICE_DATE) {
			String s1 = info1.getDateFormat();
			String s2 = info2.getDateFormat();
			if (!mDates.contains(s1)) mDates.add(s1);
			if (!mDates.contains(s2)) mDates.add(s2);
		}
		return date1 > date2 ? -1 : date1 < date2 ? 1 : 0;
	}


	private int compareTitle(MusicInfo info1, MusicInfo info2) {
		// 接收2个音乐对象，分别获得标题，转为大写
		String title1 = info1.getTitle().toUpperCase();
		String title2 = info2.getTitle().toUpperCase();
		// 如果当前不是搜索状态及不是搜索标题，即普通情况下排序
		if (mChoice != CHOICE_SEARCH || !mSearchType.equals(SEARCH_TITLE)) {
			// 获得标题的首个字符
			String s1 = title1.substring(0, 1);
			String s2 = title2.substring(0, 1);
			// 如果当前排序方式是标题排序，则
			if (mChoice == CHOICE_TITLE) {
				// 将s1，s2保存为头部信息，不重复
				if (!mTitles.contains(s1)) mTitles.add(s1);
				if (!mTitles.contains(s2)) mTitles.add(s2);
			}
			// 返回2个String的比较值
			return title1.compareTo(title2);
		}
		// 如果当前是搜索状态的排序，则按照标题中，关键字出现的顺序排序
		// 即关键字出现的索引越小，该标题就越排在前面
		else {
			int i = title1.indexOf(mSearchMsg);
			int j = title2.indexOf(mSearchMsg);
			return i < j ? -1 : i > j ? 1 : 0;
		}
	}

	private int compareArtist(MusicInfo info1, MusicInfo info2) {
		String artist1 = info1.getArtist();
		String artist2 = info2.getArtist();
		if (mChoice != CHOICE_SEARCH || !mSearchType.equals(SEARCH_ARTIST)) {
			if (mChoice == CHOICE_ARTIST) {
				if (!mArtists.contains(artist1)) mArtists.add(artist1);
				if (!mArtists.contains(artist2)) mArtists.add(artist2);
			}
			return compareUnknownAlbumOrArtist(artist1, artist2);
		} else {
			artist1 = artist1.toUpperCase();
			artist2 = artist2.toUpperCase();
			int i = artist1.indexOf(mSearchMsg);
			int j = artist2.indexOf(mSearchMsg);
			return i < j ? -1 : i > j ? 1 : 0;
		}
	}


	public List<String> getLabel(int choice) {
		List<String> temp;
		switch (choice) {
			case CHOICE_TITLE:
				temp = new ArrayList<>(mTitles);
				mTitles.clear();
				return temp;
			case CHOICE_ARTIST:
				temp = new ArrayList<>(mArtists);
				mArtists.clear();
				return temp;
			case CHOICE_ALBUM:
				temp = new ArrayList<>(mAlbums);
				mAlbums.clear();
				return temp;
			case CHOICE_DATE:
				temp = new ArrayList<>(mDates);
				mDates.clear();
				return temp;
			case CHOICE_LASTPLAY:
				return mLastPlays;
			default:
				return null;
		}
	}

	private int compareAlbum(MusicInfo info1, MusicInfo info2) {
		String album1 = info1.getAlbum();
		String album2 = info2.getAlbum();
		if (mChoice != CHOICE_SEARCH || !mSearchType.equals(SEARCH_ALBUM)) {
			if (mChoice == CHOICE_ALBUM) {


				if (!mAlbums.contains(album1)) mAlbums.add(album1);
				if (!mAlbums.contains(album2)) mAlbums.add(album2);
			}
			return compareUnknownAlbumOrArtist(album1, album2);
		} else {
			album1 = album1.toUpperCase();
			album2 = album2.toUpperCase();
			int i = album1.indexOf(mSearchMsg);
			int j = album2.indexOf(mSearchMsg);
			return i < j ? -1 : i > j ? 1 : 0;
		}
	}

	private int compareUnknownAlbumOrArtist(String lhs, String rhs) {
		if (mChoice == CHOICE_ALBUM) {
			if (lhs.equals("未知专辑") && !rhs.equals("未知专辑")) {
				return 1;
			}
			if (rhs.equals("未知专辑") && !lhs.equals("未知专辑")) {
				return -1;
			}
		} else if (mChoice == CHOICE_ARTIST) {
			if (lhs.equals("未知艺术家") && !rhs.equals("未知艺术家")) {
				return 1;
			}
			if (rhs.equals("未知艺术家") && !lhs.equals("未知艺术家")) {
				return -1;
			}
		}
		return lhs.compareToIgnoreCase(rhs);
	}
}
