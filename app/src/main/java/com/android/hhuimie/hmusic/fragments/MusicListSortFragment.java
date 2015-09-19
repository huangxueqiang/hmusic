package com.android.hhuimie.hmusic.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.model.Constant;

import java.util.ArrayList;
import java.util.List;

public class MusicListSortFragment extends DialogFragment {
	private View mView;
	private ListView mListView;
	private CheckedTextView mCheckedTextView;
	private Context mContext;
	private int mChoice;
	private boolean mIsReverse;

	public static MusicListSortFragment newFragment(int choice, boolean isReverse) {
		Bundle args = new Bundle();
		args.putInt("hehe", choice);
		args.putBoolean("hehe1", isReverse);
		MusicListSortFragment fragment = new MusicListSortFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mChoice = getArguments().getInt("hehe", 0);
		mIsReverse = getArguments().getBoolean("hehe1", false);
		mContext = getActivity();
		initLayout();
		return new AlertDialog.Builder(mContext)
				       .setTitle("选择排序方式")
				       .setView(mView)
				       .setPositiveButton("确定", new DialogInterface.OnClickListener() {
					       @Override
					       public void onClick(DialogInterface dialog, int which) {
						       Fragment targetFragment = getTargetFragment();
						       if (targetFragment != null) {
							       Intent data = new Intent();
							       data.putExtra(Constant.EXTRA_SORT_CHOICE, mChoice);
							       data.putExtra(Constant.EXTRA_SORT_IS_REVERSE, mIsReverse);
							       targetFragment.onActivityResult(0, 0, data);
						       }
					       }
				       })
				       .setNegativeButton("取消", new DialogInterface.OnClickListener() {
					       @Override
					       public void onClick(DialogInterface dialog, int which) {
						       getDialog().dismiss();
					       }
				       })
				       .create();
	}

	private void initLayout() {
		mView = LayoutInflater.from(mContext).inflate(R.layout.dialog_sort_music, null);
		mListView = (ListView) mView.findViewById(R.id.dialog_sort_listview);
		mCheckedTextView = (CheckedTextView) mView.findViewById(R.id.dialog_sort_choice);
		List<String> args = new ArrayList<>(4);
		args.add("歌曲");
		args.add("艺术家");
		args.add("专辑");
		args.add("添加时间");
		ArrayAdapter<String> mAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_sort_choice, args);
		mListView.setAdapter(mAdapter);
		int totalHeight = 0;
		for (int i = 0; i < mAdapter.getCount(); i++) {
			View listItem = mAdapter.getView(i, null, mListView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = mListView.getLayoutParams();
		params.height = totalHeight + (mListView.getDividerHeight() * (mAdapter.getCount() - 1));
		mListView.setLayoutParams(params);
		mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		if (mChoice != 4)
			mListView.setItemChecked(mChoice, true);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mChoice = position;
				((CheckedTextView) view).setChecked(true);
			}
		});
		mCheckedTextView.setText("倒序");
		mCheckedTextView.setChecked(mIsReverse);
		mCheckedTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mCheckedTextView.setChecked(!mIsReverse);
				mIsReverse = !mIsReverse;
			}
		});
	}
}
