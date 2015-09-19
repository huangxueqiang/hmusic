package com.android.hhuimie.hmusic.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.activities.MainActivity;

public class LovePlaylistsDialogFragment extends DialogFragment {
	private int mPosn;

	public static LovePlaylistsDialogFragment newInstance(int choice) {
		Bundle args = new Bundle();
		args.putInt("choice", choice);
		LovePlaylistsDialogFragment fragment = new LovePlaylistsDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mPosn = getArguments().getInt("choice", -1);
		CharSequence[] chars = {"全天", "上午", "下午", "晚上"};
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				                              .setTitle("选择时间段")
				                              .setSingleChoiceItems(chars, mPosn, new DialogInterface.OnClickListener() {
					                              @Override
					                              public void onClick(DialogInterface dialog, int which) {
						                              mPosn = which;
					                              }
				                              })
				                              .setPositiveButton("确定", new DialogInterface.OnClickListener() {
					                              @Override
					                              public void onClick(DialogInterface dialog, int which) {
						                              ((MainActivity) getActivity()).handleLovePlayList(mPosn);
						                              getDialog().dismiss();
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
}
