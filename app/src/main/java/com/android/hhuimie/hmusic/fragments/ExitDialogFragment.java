package com.android.hhuimie.hmusic.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.activities.MainActivity;
import com.android.hhuimie.hmusic.model.Constant;

public class ExitDialogFragment extends DialogFragment {
	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
											  .setTitle("退出")
				                              .setMessage("确定退出?")
				                              .setPositiveButton("确定", new DialogInterface.OnClickListener() {
					                              @Override
					                              public void onClick(DialogInterface dialog, int which) {
						                              MainActivity activity = (MainActivity) getActivity();
						                              Intent intent = new Intent(Constant.ACTION_EXIT_SAVE);
						                              activity.sendLocalbroadcast(intent);
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
