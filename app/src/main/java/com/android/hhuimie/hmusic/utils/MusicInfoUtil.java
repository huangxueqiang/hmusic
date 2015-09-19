package com.android.hhuimie.hmusic.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.model.MusicInfo;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MusicInfoUtil {

	private static List<MusicInfo> mMusicInfos;
	public static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");

	public static List<MusicInfo> getUpdatedMusicInfos(Context context) {
		List<MusicInfo> musicInfos = new ArrayList<>();
		Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				                                                  new String[]{MediaStore.Audio.Media.IS_MUSIC,
						                                                              MediaStore.Audio.Media._ID,
						                                                              MediaStore.Audio.Media.TITLE,
						                                                              MediaStore.Audio.Media.DATA,
						                                                              MediaStore.Audio.Media.ALBUM,
						                                                              MediaStore.Audio.Media.ALBUM_ID,
						                                                              MediaStore.Audio.Media.ARTIST,
						                                                              MediaStore.Audio.Media.DURATION,
						                                                              MediaStore.Audio.Media.SIZE,
						                                                              MediaStore.Audio.Media.YEAR,
						                                                              MediaStore.Audio.Media.MIME_TYPE,
						                                                              MediaStore.Audio.Media.TRACK,}
				                                                  , null, null, MediaStore.Audio.Media._ID);
		if (cursor.moveToFirst()) {
			do {
				int isMusic = cursor.getInt(0);
				if (isMusic != 0) {
					long id = cursor.getLong(1);
					String title = cursor.getString(2);
					String url = cursor.getString(3);
					int i1 = url.lastIndexOf("/");
					int i2 = url.lastIndexOf("/", i1 - 1) + 1;
					String album = cursor.getString(4);
					if (url.substring(i2, i1).equals(album)) album = "未知专辑";
					long albumId = cursor.getLong(5);
					String artist = cursor.getString(6);
					if (artist.equals("<unknown>")) artist = "未知艺术家";
					long duration = cursor.getLong(7);
					long size = cursor.getLong(8);
					String year = cursor.getString(9);
					if (year == null) year = "暂无";
					String type = cursor.getString(10);
					switch (type) {
						case "audio/mpeg":
							type = "MP3";
							break;
						case "audio/flac":
							type = "FLAC";
							break;
						case "audio/x-ape":
							type = "APE";
							break;
						case "audio/x-ms-wma":
							type = "WMA";
							break;
					}
					String track = cursor.getString(11);
					musicInfos.add(new MusicInfo(id, title, artist, duration, size, albumId, album, url, year, type, track));
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		return musicInfos;
	}

	public static List<MusicInfo> getMyMusicInfos(Context context) {
		if (mMusicInfos != null) return mMusicInfos;
		return getMyDBMusicInfos(context);
	}

	public static List<MusicInfo> getMyDBMusicInfos(Context context) {
		mMusicInfos = new DBHelper(context).myDBMusicInfos();
		return mMusicInfos;
	}


	public static Bitmap getDefaultArtwork(Context context, boolean small) {
		if (small) {
			return BitmapFactory.decodeResource(context.getResources(), R.drawable.music5);
		}
		return BitmapFactory.decodeResource(context.getResources(), R.drawable.defaultalbum);
	}

	private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
		Bitmap bm = null;
		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException();
		}
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			FileDescriptor fd = null;
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			} else {
				Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
				if (pfd != null) {
					fd = pfd.getFileDescriptor();
				}
			}
			options.inSampleSize = 1;
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(fd, null, options);
			options.inSampleSize = 100;
			options.inJustDecodeBounds = false;
			options.inDither = false;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bm;
	}

	public static Bitmap getArtwork(Context context, long song_id, long album_id, boolean allowdefalut, boolean small) {
		if (album_id < 0) {
			if (song_id < 0) {
				Bitmap bm = getArtworkFromFile(context, song_id, -1);
				if (bm != null) {
					return bm;
				}
			}
			if (allowdefalut) {
				return getDefaultArtwork(context, small);
			}
			return null;
		}
		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 1;
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeStream(in, null, options);
				if (small) {
					options.inSampleSize = computeSampleSize(options, 40);
				} else {
					options.inSampleSize = computeSampleSize(options, 600);
				}
				options.inJustDecodeBounds = false;
				options.inDither = false;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				in = res.openInputStream(uri);
				return BitmapFactory.decodeStream(in, null, options);
			} catch (FileNotFoundException e) {
				Bitmap bm = getArtworkFromFile(context, song_id, album_id);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null && allowdefalut) {
							return getDefaultArtwork(context, small);
						}
					}
				} else if (allowdefalut) {
					bm = getDefaultArtwork(context, small);
				}
				return bm;
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static int computeSampleSize(BitmapFactory.Options options, int target) {
		int w = options.outWidth;
		int h = options.outHeight;
		int candidateW = w / target;
		int candidateH = h / target;
		int candidate = Math.max(candidateW, candidateH);
		if (candidate == 0) {
			return 1;
		}
		if (candidate > 1) {
			if ((w > target) && (w / candidate) < target) {
				candidate -= 1;
			}
		}
		if (candidate > 1) {
			if ((h > target) && (h / candidate) < target) {
				candidate -= 1;
			}
		}
		return candidate;
	}

	public static String getFormatDuration(long duration) {
		int temp = 60000;
		String minute = duration / (temp) + "";
		String second = duration % (temp) + "";
		if (minute.length() < 2) {
			minute = "0" + minute;
		}

		if (second.length() > 3) {
			if (second.length() == 4) {
				second = "0" + second;
			}
			return minute + ":" + second.substring(0, 2);
		}
		return minute + ":00";
	}

	public static String getFormatSize(long size) {
		float temp = 1048576.00f;
		float a = (float) (Math.round((size / temp) * 100)) / 100;
		return a + " MB";
	}
}
