package com.android.hhuimie.hmusic.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingManager {
	public static final String sNOT_SETTING = "notification";
	public static final String sFLOAT_SETTING = "float_window";
	public static final String sFLOAT_LOCAL_SETTING = "float_window_position";
	public static final String sSENSOR_SETTING = "sensor";
	public static final String sPLUG_SETTING = "plug";
	public static final String sSEARCH_SETTING = "search";
	public static final String sSEARCH_TITLE_SETTING = "search_title";
	public static final String sSEARCH_ARTIST_SETTING = "search_artist";
	public static final String sSEARCH_ALBUM_SETTING = "search_album";

	public static boolean getSingleSetting(Context context, String settingName) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(settingName, true);
	}
}
