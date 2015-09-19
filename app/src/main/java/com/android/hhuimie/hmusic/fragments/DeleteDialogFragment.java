package com.android.hhuimie.hmusic.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.model.Constant;

public class DeleteDialogFragment extends DialogFragment {
	public static DeleteDialogFragment newInstance(int position) {
		Bundle args = new Bundle();
		args.putInt("delete", position);
		DeleteDialogFragment fragment = new DeleteDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				                              .setTitle("警告")
				                              .setIcon(R.drawable.ic_delete_dialog_warning)
				                              .setMessage("这将删除磁盘中的文件,确定删除?")
				                              .setPositiveButton("确定", new DialogInterface.OnClickListener() {
					                              @Override
					                              public void onClick(DialogInterface dialog, int which) {
						                              Fragment targetFragment = getTargetFragment();
						                              if (targetFragment != null) {
							                              Intent data = new Intent();
							                              data.putExtra(Constant.EXTRA_CURRENT_DELETE, getArguments().getInt("delete"));
							                              targetFragment.onActivityResult(1, 0, data);
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
}
