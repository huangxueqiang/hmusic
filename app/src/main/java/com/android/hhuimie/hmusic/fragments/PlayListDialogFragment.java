package com.android.hhuimie.hmusic.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.utils.DBHelper;

import java.util.List;

public class PlayListDialogFragment extends DialogFragment {
	private Context mContext;
	private ScrollView mView;
	private ListView mListView;
	private int mPosn = -1;
	private String mName;
	private List<String> mPlayList;
	private ArrayAdapter<String> mAdapter;
	private EditText mEditText;

	public static PlayListDialogFragment newInstance(long id) {
		Bundle args = new Bundle();
		args.putLong("id", id);
		PlayListDialogFragment fragment = new PlayListDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mContext = getActivity();
		initDialog();
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
				                              .setTitle("添加到播放列表")
				                              .setView(mView)
				                              .setPositiveButton("确定", new DialogInterface.OnClickListener() {
					                              @Override
					                              public void onClick(DialogInterface dialog, int which) {
						                              long id = getArguments().getLong("id", -1);
						                              if (id != -1) {
							                              if (mListView.isEnabled()) {
								                              if (new DBHelper(mContext).addToPlayList(mName, id)) {
									                              Toast.makeText(mContext, "添加成功", Toast.LENGTH_SHORT).show();
								                              } else {
									                              Toast.makeText(mContext, "添加失败", Toast.LENGTH_SHORT).show();
								                              }
							                              } else {
								                              if (new DBHelper(mContext).newPlayList(mName, id)) {
									                              Toast.makeText(mContext, "添加成功", Toast.LENGTH_SHORT).show();
								                              } else {
									                              Toast.makeText(mContext, "添加失败", Toast.LENGTH_SHORT).show();
								                              }
							                              }
						                              }
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
		mView = (ScrollView) LayoutInflater.from(mContext).inflate(R.layout.dialog_play_list, null);
		mListView = (ListView) mView.findViewById(R.id.dialog_playlist_listview);
		mEditText = (EditText) mView.findViewById(R.id.dialog_playlist_custom);
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
					mPosn = position;
					mName = mPlayList.get(position);
					((CheckedTextView) view).setChecked(true);
				}
			});
		} else {
			View view = mView.findViewById(R.id.line_horizontal);
			view.setVisibility(View.GONE);
		}
		mEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				mListView.setEnabled(false);
				if (mPosn != -1)
					mListView.setItemChecked(mPosn, false);
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0) {
					if (s.toString().matches("^(\\d|.* )")) {
						mEditText.setText("");
					} else
						mName = s.toString();
				} else {
					mListView.setEnabled(true);
					if (mPosn != -1) {
						mListView.setItemChecked(mPosn, true);
						mName = mPlayList.get(mPosn);
					}
				}
			}
		});
	}
}
