package com.android.hhuimie.hmusic.model;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MusicInfo implements Serializable {
	public static final SimpleDateFormat mSDF = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
	public static final SimpleDateFormat mSDF_HOUR = new SimpleDateFormat("HH", Locale.CHINA);
	private long mId;
	private String mTitle;
	private String mArtist;
	private long mDuration;
	private long mSize;
	private long mAlbumId;
	private String mAlbum;
	private String mAlbumArt;
	private String mYear;
	private String mType;
	private String mTrack;
	private String mUrl;
	private long mPlaybacks;
	private int mBgColor;
	private int mPrimaryColor;
	private int mDetailColor;
	private long mLastPlay;

	public MusicInfo(long id, String title, String artist, long duration, long size, long albumId, String album, String albumArt, String year, String type, String track, String url, int bgColor, int primaryColor, int detailColor) {
		mId = id;
		mTitle = title;
		mArtist = artist;
		mDuration = duration;
		mSize = size;
		mAlbumId = albumId;
		mAlbum = album;
		mAlbumArt = albumArt;
		mYear = year;
		mType = type;
		mTrack = track;
		mUrl = url;
		mBgColor = bgColor;
		mPrimaryColor = primaryColor;
		mDetailColor = detailColor;
	}

	public MusicInfo(long id, String title, String artist, long duration, long size, long albumId, String album, String url, String year, String type, String track) {
		mId = id;
		mTitle = title;
		mArtist = artist;
		mDuration = duration;
		mSize = size;
		mAlbumId = albumId;
		mAlbum = album;
		mUrl = url;
		mYear = year;
		mType = type;
		mTrack = track;
	}

	public long getPlaybacks() {
		return mPlaybacks;
	}

	public void setPlaybacks(long playbacks) {
		mPlaybacks = playbacks;
	}

	public void setColor(int bgColor, int primaryColor, int detailColor) {
		mBgColor = bgColor;
		mPrimaryColor = primaryColor;
		mDetailColor = detailColor;
	}

	public int getBgColor() {
		return mBgColor;
	}

	public int getPrimaryColor() {
		return mPrimaryColor;
	}

	public int getDetailColor() {
		return mDetailColor;
	}

	public String getAlbumArt() {
		return mAlbumArt;
	}

	public void setAlbumArt(String albumArt) {
		mAlbumArt = albumArt;
	}

	public long getLastPlay() {
		return mLastPlay;
	}

	public void setLastPlay(long lastPlay) {
		mLastPlay = lastPlay;
	}

	public String getDateFormat() {
		Date date = new Date(getDateModified());
		return mSDF.format(date);
	}

	public String getSearch(int choice) {
		switch (choice) {
			case 0:
				return mTitle;
			case 1:
				return mArtist;
			case 2:
				return mAlbum;
			default:
				return mTitle;
		}
	}

	public String get(int choice) {
		switch (choice) {
			case Constant.ON_TITLE_LIST:
				return mTitle.substring(0, 1).toUpperCase();
			case Constant.ON_ARTIST_LIST:
				return mArtist;
			case Constant.ON_ALBUM_LIST:
				return mAlbum;
			case Constant.ON_DATE_LIST:
				return getDateFormat();
			case Constant.ON_LAST_PLAY_LIST:
				return mSDF.format(mLastPlay);
		}
		return null;
	}

	public long getDateModified() {
		return new File(mUrl).lastModified();
	}

	public long getId() {
		return mId;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getTrack() {
		return mTrack;
	}

	public String getType() {
		return mType;
	}

	public String getYear() {
		return mYear;
	}

	public String getArtist() {
		return mArtist;
	}

	public long getDuration() {
		return mDuration;
	}

	public long getSize() {
		return mSize;
	}

	public long getAlbumId() {
		return mAlbumId;
	}

	public String getAlbum() {
		return mAlbum;
	}

	public String getUrl() {
		return mUrl;
	}

	@Override
	public String toString() {
		return "--" + mTitle + "--";
	}
}
