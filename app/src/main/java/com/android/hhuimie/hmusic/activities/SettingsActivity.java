package com.android.hhuimie.hmusic.activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.view.Menu;
import android.view.MenuItem;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.fragments.RestoreDialogFragment;
import com.android.hhuimie.hmusic.utils.SettingManager;

import java.util.List;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

	private static final boolean ALWAYS_SIMPLE_PREFS = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("设置");
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_setting, menu);
		MenuItem item = menu.findItem(R.id.action_restore);
		item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				RestoreDialogFragment fragment = new RestoreDialogFragment();
				fragment.show(getFragmentManager(), "restore");
				return true;
			}
		});
		return true;
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
	}


	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		addPreferencesFromResource(R.xml.pref_null);

		PreferenceCategory fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_notification);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_notification);

		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_float_window);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_float_window);

		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_sensor);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_sensor);

		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_plug);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_plug);

		fakeHeader = new PreferenceCategory(this);
		fakeHeader.setTitle(R.string.pref_header_search);
		getPreferenceScreen().addPreference(fakeHeader);
		addPreferencesFromResource(R.xml.pref_search);
		Preference preferenceTitle = findPreference(SettingManager.sSEARCH_TITLE_SETTING);
		preferenceTitle.setOnPreferenceClickListener(this);
		Preference preferenceArtist = findPreference(SettingManager.sSEARCH_ARTIST_SETTING);
		preferenceArtist.setOnPreferenceClickListener(this);
		Preference preferenceAlbum = findPreference(SettingManager.sSEARCH_ALBUM_SETTING);
		preferenceAlbum.setOnPreferenceClickListener(this);
		Preference preferenceSearch = findPreference(SettingManager.sSEARCH_SETTING);
		preferenceSearch.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout
				        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		setResult(0);
	}

	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || !isXLargeTablet(context);
	}

	public void restoreToDefault() {
		SharedPreferences.Editor edit = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE).edit();
		edit.clear();
		edit.apply();
		String[] keys = {SettingManager.sNOT_SETTING, SettingManager.sFLOAT_SETTING, SettingManager.sFLOAT_LOCAL_SETTING, SettingManager.sSENSOR_SETTING, SettingManager.sPLUG_SETTING, SettingManager.sSEARCH_SETTING, SettingManager.sSEARCH_TITLE_SETTING, SettingManager.sSEARCH_ARTIST_SETTING, SettingManager.sSEARCH_ALBUM_SETTING};
		CheckBoxPreference preference;
		for (String key : keys) {
			preference = (CheckBoxPreference) findPreference(key);
			preference.setChecked(true);
		}
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String[] searchKeys = {SettingManager.sSEARCH_TITLE_SETTING, SettingManager.sSEARCH_ARTIST_SETTING, SettingManager.sSEARCH_ALBUM_SETTING};
		SharedPreferences preferences = getSharedPreferences(getPackageName() + "_preferences", MODE_PRIVATE);
		if (preference.getKey().equals(SettingManager.sSEARCH_SETTING) && preferences.getBoolean(SettingManager.sSEARCH_SETTING, true)) {
			for (String key : searchKeys) {
				if (preferences.getBoolean(key, true)) {
					return false;
				}
			}
			preferences.edit().putBoolean(SettingManager.sSEARCH_TITLE_SETTING, true).apply();
			((CheckBoxPreference) findPreference(SettingManager.sSEARCH_TITLE_SETTING)).setChecked(true);
		} else {
			for (String key : searchKeys) {
				if (preferences.getBoolean(key, true)) {
					return false;
				}
			}
			preferences.edit().putBoolean(SettingManager.sSEARCH_SETTING, false).apply();
			((CheckBoxPreference) findPreference(SettingManager.sSEARCH_SETTING)).setChecked(false);
		}
		return false;
	}

	@Override
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void onBuildHeaders(List<Header> target) {
		if (!isSimplePreferences(this)) {
			loadHeadersFromResource(R.xml.pref_headers, target);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class NotificationPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_notification);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class FloatWindowPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_float_window);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class SensorPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_sensor);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class PlugPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_plug);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class SearchPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.pref_search);
		}
	}
}
