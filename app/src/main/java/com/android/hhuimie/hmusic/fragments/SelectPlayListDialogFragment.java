package com.android.hhuimie.hmusic.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.activities.MainActivity;
import com.android.hhuimie.hmusic.utils.DBHelper;

import java.util.List;

public class SelectPlayListDialogFragment extends DialogFragment {
	private ArrayAdapter<String> mAdapter;
	private Context mContext;
	private View mView;
	private ListView mListView;
	private List<String> mPlayList;
	private String mName;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mContext = getActivity();
		initDialog();
		if (mPlayList == null) {
			return new AlertDialog.Builder(mContext)
					       .setTitle("提醒")
					       .setMessage("暂无播放列表")
					       .setPositiveButton("确定", new DialogInterface.OnClickListener() {
						       @Override
						       public void onClick(DialogInterface dialog, int which) {
							       getDialog().dismiss();
						       }
					       })
					       .create();
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
				                              .setView(mView)
				                              .setTitle("选择播放列表")
				                              .setPositiveButton("确定", new DialogInterface.OnClickListener() {
					                              @Override
					                              public void onClick(DialogInterface dialog, int which) {
						                              ((MainActivity) getActivity()).handlePlayList(mName);
						                              getDialog().dismiss();
					                              }
				                              })
				                              .setNegativeButton("取消", new DialogInterface.OnClickListener() {
					                              @Override
					                              public void onClick(DialogInterface dialog, int which) {
						                              getDialog().dismiss();
					                              }
				                              });
		AlertDialog dialog = builder.show();
		int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
		View divider = dialog.findViewById(dividerId);
		Drawable drawable = getActivity().getResources().getDrawable(R.drawable.line_shape);
		divider.setBackground(drawable);
		return dialog;
	}

	private void resetListView() {
		int totalHeight = 0;
		for (int i = 0; i < mAdapter.getCount(); i++) {
			View listItem = mAdapter.getView(i, null, mListView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = mListView.getLayoutParams();
		params.height = totalHeight + (mListView.getDividerHeight() * (mAdapter.getCount() - 1));
		mListView.setLayoutParams(params);
	}

	private void initDialog() {
		mView = LayoutInflater.from(mContext).inflate(R.layout.dialog_select_play_list, null);
		mListView = (ListView) mView.findViewById(R.id.dialog_select_playlist_listview);
		mPlayList = new DBHelper(mContext).getPlayList();
		if (mPlayList != null) {
			mAdapter = new ArrayAdapter<>(mContext, R.layout.list_item_sort_choice, mPlayList);
			mListView.setAdapter(mAdapter);
			resetListView();
			mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
			mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
					new AlertDialog.Builder(mContext)
							.setMessage("删除该列表?")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									new DBHelper(mContext).deletePlayList(mPlayList.get(position));
									mPlayList.remove(position);
									mAdapter.notifyDataSetChanged();
									resetListView();
								}
							})
							.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									//getDialog().dismiss();
								}
							}).create().show();
					return true;
				}
			});
			mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					((CheckedTextView) view).setChecked(true);
					mName = mPlayList.get(position);
				}
			});
		}
	}
}
