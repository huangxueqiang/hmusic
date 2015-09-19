package com.android.hhuimie.hmusic.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.model.Constant;
import com.android.hhuimie.hmusic.model.MusicInfo;

import java.util.Arrays;
import java.util.List;

public class LoveDialogFragment extends DialogFragment {
	private Context mContext;
	private View mView;
	private CheckedTextView mTextView;
	private ListView mListView;
	private EditText mEditText;
	private boolean mToReduce;
	private int mLoveLevel;
	private Integer[] mArgs;
	private int mPosn = -1;

	public static LoveDialogFragment newInstance(MusicInfo info) {
		Bundle args = new Bundle();
		args.putSerializable(Constant.EXTRA_CURRENT_SONG, info);
		LoveDialogFragment fragment = new LoveDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mContext = getActivity();
		initDialog();
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
				                              .setTitle("增加热爱度")
				                              .setView(mView)
				                              .setPositiveButton("确定", new DialogInterface.OnClickListener() {
					                              @Override
					                              public void onClick(DialogInterface dialog, int which) {
						                              Fragment targetFragment = getTargetFragment();
						                              if (targetFragment != null) {
							                              Intent data = new Intent();
							                              data.putExtra(Constant.EXTRA_LOVE_LEVEL, mLoveLevel);
							                              data.putExtra(Constant.EXTRA_CURRENT_SONG, getArguments().getSerializable(Constant.EXTRA_CURRENT_SONG));
							                              data.putExtra(Constant.EXTRA_LOVE_TO_REDUCE, mToReduce);
							                              targetFragment.onActivityResult(2, 0, data);
						                              }
					                              }
				                              })
				                              .setNegativeButton("取消", new DialogInterface.OnClickListener() {
					                              @Override
					                              public void onClick(DialogInterface dialog, int which) {
						                              getDialog().dismiss();
					                              }
				                              });
		AlertDialog dialog= builder.show();
		int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
		View divider = dialog.findViewById(dividerId);
		Drawable drawable = getActivity().getResources().getDrawable(R.drawable.line_shape);
		divider.setBackground(drawable);
		return dialog;
	}

	private void initDialog() {
		mView = LayoutInflater.from(mContext).inflate(R.layout.dialog_love, null);
		mListView = (ListView) mView.findViewById(R.id.dialog_love_listview);
		mTextView = (CheckedTextView) mView.findViewById(R.id.dialog_to_reduce);
		mEditText = (EditText) mView.findViewById(R.id.dialog_love_custom);
		mArgs = new Integer[]{5, 20, 50, 100};
		List<Integer> list = Arrays.asList(mArgs);
		ArrayAdapter<Integer> adapter = new ArrayAdapter<>(mContext, R.layout.list_item_sort_choice, list);
		mListView.setAdapter(adapter);
		int totalHeight = 0;
		for (int i = 0; i < adapter.getCount(); i++) {
			View listItem = adapter.getView(i, null, mListView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = mListView.getLayoutParams();
		params.height = totalHeight + (mListView.getDividerHeight() * (adapter.getCount() - 1));
		mListView.setLayoutParams(params);
		mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				mPosn = position;
				mLoveLevel = mArgs[position];
				((CheckedTextView) view).setChecked(true);
			}
		});
		mTextView.setText("减少");
		mTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mTextView.setChecked(!mToReduce);
				mToReduce = !mToReduce;
			}
		});
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
					if (s.toString().equals("0")) {
						mEditText.setText("");
					} else
						mLoveLevel = Integer.parseInt(s.toString());
				} else {
					mListView.setEnabled(true);
					if (mPosn != -1) {
						mListView.setItemChecked(mPosn, true);
						mLoveLevel = mArgs[mPosn];
					}
				}
			}
		});
	}
}
