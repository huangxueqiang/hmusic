package com.android.hhuimie.hmusic.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore;
import android.util.Log;
import com.android.hhuimie.hmusic.model.MusicInfo;

import java.util.*;

/**
 * Description: DBHelper
 * Author: hhuimie
 * Update: hhuimie(2015-05-07 08:58)
 */
public class DBHelper extends SQLiteOpenHelper {
	public static final String DEFAULT_TABLE = "default_table";
	public static final String PLAY_MSG_TABLE = "play_msg_table";
	public static final String PLAY_LIST_TABLE = "play_list_table";
	public static final String FLOAT_WINDOW_PARAMS_TABLE = "float_window_params_table";
	public static final String NOTIFICATION_ON = "notification_on";
	public static final String PARAMS_X = "params_x";
	public static final String PARAMS_Y = "params_Y";
	public static final String PLAY_LIST_NAME = "play_list_name";
	public static final String LASTPLAY = "last_play";
	public static final String AM_PLAYBACKS = "am_playbacks";
	public static final String PM_PLAYBACKS = "pm_playbacks";
	public static final String MOON_PLAYBACKS = "moon_playbacks";
	public static final String PLAY_STATE = "play_state";
	private static final String ID = "id";
	private static final String TITLE = "title";
	private static final String ARTIST = "artist";
	private static final String DURATION = "duration";
	private static final String SIZE = "size";
	private static final String ALBUM_ID = "album_id";
	private static final String ALBUM = "album";
	private static final String ALBUM_ART = "album_art";
	private static final String YEAR = "year";
	private static final String TYPE = "type";
	private static final String TRACK = "track";
	private static final String URL = "url";
	private static final String BG_COLOR = "bg_color";
	private static final String PRIMARY_COLOR = "primary_color";
	private static final String DETAIL_COLOR = "detail_color";
	private static final String DB_NAME = "hmusic";
	public static final String CREATE_PLAY_MSG_TABLE = "create table " + PLAY_MSG_TABLE + " ("
			                                                   + ID + " long primary key default -1, "
			                                                   + PLAY_STATE + " integer DEFAULT 0, "
			                                                   + "foreign key (" + ID + ") references " + DEFAULT_TABLE + " ("
			                                                   + ID + ") on delete cascade)";

	private static final String CREATE_DEFAULT_TABLE = "create table " + DEFAULT_TABLE + " ("
			                                                   + ID + " long primary key, "
			                                                   + LASTPLAY + " long DEFAULT 0, "
			                                                   + AM_PLAYBACKS + " long DEFAULT 0, "
			                                                   + PM_PLAYBACKS + " long DEFAULT 0, "
			                                                   + MOON_PLAYBACKS + " long DEFAULT 0, "
			                                                   + TITLE + " text, "
			                                                   + ARTIST + " text, "
			                                                   + DURATION + " long, "
			                                                   + SIZE + " long, "
			                                                   + ALBUM_ID + " long, "
			                                                   + ALBUM + " text, "
			                                                   + ALBUM_ART + " text, "
			                                                   + YEAR + " text, "
			                                                   + TYPE + " text, "
			                                                   + TRACK + " text, "
			                                                   + URL + " text, "
			                                                   + BG_COLOR + " integer DEFAULT 0, "
			                                                   + PRIMARY_COLOR + " integer DEFAULT 0, "
			                                                   + DETAIL_COLOR + " integer DEFAULT 0)";
	private static final String CREATE_PLAY_LIST_TABLE = "create table " + PLAY_LIST_TABLE + " ("
			                                                     + PLAY_LIST_NAME + " text)";
	private static final String CREATE_FLOAT_WINDOW_PARAMS_TABLE = "create table " + FLOAT_WINDOW_PARAMS_TABLE + " ("
			                                                               + PARAMS_X + " integer not null, "
			                                                               + PARAMS_Y + " integer not null)";
	private Context mContext;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, 1);
		mContext = context;
	}

	public void setCustomFloatParams(int x, int y) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(PARAMS_X, x);
		values.put(PARAMS_Y, y);
		database.delete(FLOAT_WINDOW_PARAMS_TABLE, null, null);
		database.insert(FLOAT_WINDOW_PARAMS_TABLE, null, values);
		database.close();
	}

	public int[] getCustomFloatParams() {
		SQLiteDatabase database = this.getReadableDatabase();
		Cursor cursor = database.query(FLOAT_WINDOW_PARAMS_TABLE, null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			int[] result = new int[2];
			result[0] = cursor.getInt(0);
			result[1] = cursor.getInt(1);
			cursor.close();
			database.close();
			return result;
		}
		return null;
	}

	public boolean deleteSongInPlayList(String name, long id) {
		SQLiteDatabase database = this.getWritableDatabase();
		boolean b = database.delete(name, ID + "=?", new String[]{id + ""}) != 0;
		database.close();
		return b;
	}

	public List<List<Long>> getLovePlaylists(int choice) {
		SQLiteDatabase database = this.getReadableDatabase();
		List<Long> ids = new ArrayList<>();
		List<Long> times = new ArrayList<>();
		String[] args = {"all", AM_PLAYBACKS, PM_PLAYBACKS, MOON_PLAYBACKS};
		Cursor cursor;
		if (choice == 0) {
			cursor = database.rawQuery("select " + ID + ",(" + AM_PLAYBACKS + "+" + PM_PLAYBACKS + "+" + MOON_PLAYBACKS + ") as sum from " + DEFAULT_TABLE + " where sum>0 order by id asc", null);
		} else {
			cursor = database.query(DEFAULT_TABLE, new String[]{ID, args[choice]}, args[choice] + ">?", new String[]{"0"}, null, null, ID + " asc");
		}
		if (cursor.moveToFirst()) {
			do {
				ids.add(cursor.getLong(0));
				times.add(cursor.getLong(1));
			} while (cursor.moveToNext());
			cursor.close();
			database.close();

			List<List<Long>> result = new ArrayList<>(2);
			result.add(ids);
			result.add(times);
			return result;
		}
		return null;
	}

	public List<Long> getPlayListByName(String name) {
		SQLiteDatabase database = this.getReadableDatabase();
		// 查询的表是用户点击的播放列表
		Cursor cursor = database.query(name, null, null, null, null, null, ID + " asc");
		if (cursor.moveToFirst()) {
			List<Long> result = new ArrayList<>();
			do {
				// 表中存储的是歌曲id，所以如果查询到，则把id添加到list中
				result.add(cursor.getLong(0));
			} while (cursor.moveToNext());
			Log.d("result11", result.toString());
			cursor.close();
			database.close();
			return result;
		}
		return null;
	}

	public boolean addToPlayList(String name, long id) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ID, id);
		boolean b = database.insert(name, null, values) != -1;
		database.close();
		return b;
	}

	public void deletePlayList(String name) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(PLAY_LIST_NAME, name);
		database.delete(PLAY_LIST_TABLE, PLAY_LIST_NAME + "=?", new String[]{name});
		database.execSQL("drop table if exists " + name);
		database.close();
	}

	public boolean newPlayList(String name, long id) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(PLAY_LIST_NAME, name);
		if (database.insert(PLAY_LIST_TABLE, null, values) != -1) {
			values.clear();
			database.execSQL("create table " + name + " (" + ID + " long primary key, foreign key (" + ID + ") " +
					                 "references " + DEFAULT_TABLE + " (" + ID + ") on delete cascade)");
			values.put(ID, id);
			boolean b = database.insert(name, null, values) != -1;
			database.close();
			return b;
		}
		return false;
	}

	public List<String> getPlayList() {
		// 数据库帮助类继承自sqliteOpenHelper，在这个类中，提供各种公开方法
		// 直接可以getWritableDatabase();
		SQLiteDatabase database = this.getWritableDatabase();
		// 查询，查询的表是保存所有播放列表名字的表
		Cursor cursor = database.query(PLAY_LIST_TABLE, null, null, null, null, null, null);
		List<String> result = new ArrayList<>();
		if (cursor.moveToFirst()) {
			do {
				// 如果找到列表，则添加到list中
				result.add(cursor.getString(0));
			} while (cursor.moveToNext());
			cursor.close();
			database.close();
			// 返回列表
			return result;
		}
		// 没有播放列表，返回空
		return null;
	}

	public long[] getLastPlayMsg() {
		SQLiteDatabase database = this.getWritableDatabase();
		long[] result = new long[2];
		Cursor cursor = database.query(PLAY_MSG_TABLE, null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			result[0] = cursor.getLong(0);
			result[1] = cursor.getInt(1);
			cursor.close();
			database.close();
			return result;
		}
		return null;
	}

	public void exitSave(long id, int state) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ID, id);
		values.put(PLAY_STATE, state);
		database.delete(PLAY_MSG_TABLE, null, null);
		database.insert(PLAY_MSG_TABLE, null, values);
		database.close();
	}

	public Map<Long, Long> myGetLastPlayList() {
		// 我们要获得的是歌曲id与播放时间键值对
		Map<Long, Long> map = new HashMap<>();
		SQLiteDatabase database = this.getWritableDatabase();
		// 查询默认表中的id以及最后播放时间，播放时间要求大于0，按照播放时间倒序，即最后播放的排前面
		Cursor cursor = database.query(DEFAULT_TABLE, new String[]{"id", "last_play"}, "last_play>?", new String[]{"0"}, null, null, "last_play desc");
		if (cursor.moveToFirst()) {
			do {
				// 先判断是否满100首歌曲，如果是，退出循环
				if (map.size() == 100) break;
				// 否则，把查询到的id和最后播放时间添加到map中
				map.put(cursor.getLong(0), cursor.getLong(1));
			} while (cursor.moveToNext());
		}
		cursor.close();
		database.close();
		// 返回map
		return map;
	}

	public boolean myDeleteDB(long id) {
		SQLiteDatabase database = this.getWritableDatabase();
		boolean b = database.delete(DEFAULT_TABLE, "id=?", new String[]{id + ""}) != -1;
		database.close();
		return b;
	}

	public void setColorArt(long id, int bgColor, int priColor, int detColor) {
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(BG_COLOR, bgColor);
		values.put(PRIMARY_COLOR, priColor);
		values.put(DETAIL_COLOR, detColor);
		database.update(DEFAULT_TABLE, values, ID + "=?", new String[]{id + ""});
		database.close();
	}

	public List<MusicInfo> myDBMusicInfos() {
		SQLiteDatabase database = this.getReadableDatabase();
		Cursor query = database.query(DEFAULT_TABLE, new String[]{
				                                                         ID,
				                                                         TITLE,
				                                                         ARTIST,
				                                                         DURATION,
				                                                         SIZE,
				                                                         ALBUM_ID,
				                                                         ALBUM,
				                                                         ALBUM_ART,
				                                                         YEAR,
				                                                         TYPE,
				                                                         TRACK,
				                                                         URL,
				                                                         BG_COLOR,
				                                                         PRIMARY_COLOR,
				                                                         DETAIL_COLOR},
				                             null, null, null, null, null);
		List<MusicInfo> musicInfos = new ArrayList<>(query.getCount());
		if (query.moveToFirst()) {
			do {
				musicInfos.add(new MusicInfo(
						                            query.getLong(0),
						                            query.getString(1),
						                            query.getString(2),
						                            query.getLong(3),
						                            query.getLong(4),
						                            query.getLong(5),
						                            query.getString(6),
						                            query.getString(7),
						                            query.getString(8),
						                            query.getString(9),
						                            query.getString(10),
						                            query.getString(11),
						                            query.getInt(12),
						                            query.getInt(13),
						                            query.getInt(14)));
			} while (query.moveToNext());
		}
		query.close();
		database.close();
		return musicInfos;
	}

	public void myUpgradeDB() {
		long l = System.currentTimeMillis();
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		database.execSQL("PRAGMA foreign_keys=ON");
		List<MusicInfo> musicInfos = MusicInfoUtil.getUpdatedMusicInfos(mContext);
		List<Long> srcIds = new ArrayList<>(musicInfos.size());
		List<Long> myIds = new ArrayList<>();
		Cursor query = null;
		for (MusicInfo info : musicInfos) {
			srcIds.add(info.getId());
			query = mContext.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
					                                           new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
					                                           MediaStore.Audio.Albums._ID + "=?",
					                                           new String[]{info.getAlbumId() + ""},
					                                           null);
			if (query.moveToFirst()) {
				info.setAlbumArt(query.getString(query.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)));
			}
		}

		if (query != null) {
			query.close();
		}

		Cursor cursor = database.query(DEFAULT_TABLE, new String[]{"id"}, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				myIds.add(cursor.getLong(0));
			} while (cursor.moveToNext());
		}
		for (long myId : myIds) {
			if (Collections.binarySearch(srcIds, myId) < 0) {
				database.delete(DEFAULT_TABLE, ID + "=?", new String[]{myId + ""});
			}
		}
		for (int i = 0; i < srcIds.size(); i++) {
			long srcId = srcIds.get(i);
			if (Collections.binarySearch(myIds, srcId) < 0) {
				MusicInfo info = musicInfos.get(i);
				long id = info.getId();
				values.put(ID, id);
				values.put(TITLE, info.getTitle());
				values.put(ARTIST, info.getArtist());
				values.put(DURATION, info.getDuration());
				values.put(SIZE, info.getSize());
				values.put(ALBUM_ID, info.getAlbumId());
				values.put(ALBUM, info.getAlbum());
				values.put(ALBUM_ART, info.getAlbumArt());
				values.put(YEAR, info.getYear());
				values.put(TYPE, info.getType());
				values.put(TRACK, info.getTrack());
				values.put(URL, info.getUrl());
				database.insert(DEFAULT_TABLE, null, values);
				values.clear();
			}
		}
		cursor.close();
		database.close();
		l = System.currentTimeMillis() - l;
		Log.d("didix", l + "");
	}

	public int customUpdateLove(long id, int lovelevel) {
		int index = getTimeIndex(new Date());
		String[] when = {AM_PLAYBACKS, PM_PLAYBACKS, MOON_PLAYBACKS};
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		Cursor cursor = database.query(DEFAULT_TABLE, null, "id=?", new String[]{id + ""}, null, null, null);
		int result = -9451;
		if (cursor.moveToFirst()) {
			int playbacks = cursor.getInt(index + 2);
			result = playbacks + lovelevel;
			values.put(when[index], playbacks + lovelevel);
			database.update(DEFAULT_TABLE, values, "id=?", new String[]{id + ""});
		}
		cursor.close();
		database.close();
		return result;
	}

	private int getTimeIndex(Date date) {
		int hour = Integer.parseInt(MusicInfo.mSDF_HOUR.format(date));
		if (hour > 6 && hour < 13) return 0;
		if (hour > 12 && hour < 19) return 1;
		return 2;
	}

	public boolean autoUpdateLoveAndLastPlay(long id) {
		Date date = new Date();
		int index = getTimeIndex(date);
		String[] when = {AM_PLAYBACKS, PM_PLAYBACKS, MOON_PLAYBACKS};
		boolean result = false;
		SQLiteDatabase database = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		Cursor cursor = database.query(DEFAULT_TABLE, null, "id=?", new String[]{id + ""}, null, null, null);
		if (cursor.moveToFirst()) {
			int playbacks = cursor.getInt(index + 2);
			values.put(when[index], playbacks + 1);
			values.put(LASTPLAY, date.getTime());
			database.update(DEFAULT_TABLE, values, "id=?", new String[]{id + ""});
			result = true;
		}
		cursor.close();
		database.close();
		return result;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_DEFAULT_TABLE);
		db.execSQL(CREATE_PLAY_MSG_TABLE);
		db.execSQL(CREATE_PLAY_LIST_TABLE);
		db.execSQL(CREATE_FLOAT_WINDOW_PARAMS_TABLE);
		ContentValues values = new ContentValues();
		values.put(NOTIFICATION_ON, 1);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
