package com.android.hhuimie.hmusic.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.utils.DBHelper;

public class LoadingActivity extends Activity {
	long mTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mTime = System.currentTimeMillis();
		new MyLoadingTask().execute();
	}

	private class MyLoadingTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected void onPostExecute(Boolean aBoolean) {
			LoadingActivity.this.finish();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			new DBHelper(LoadingActivity.this).myUpgradeDB();
			Intent i = new Intent(LoadingActivity.this, MainActivity.class);
			mTime = System.currentTimeMillis() - mTime;
			Log.d("time1", mTime +"");
			if (mTime > 1499) {
				startActivity(i);
			} else {
				try {
					Thread.sleep(1500- mTime);
					startActivity(i);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return true;
		}
	}
}
