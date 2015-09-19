package com.android.hhuimie.hmusic.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Toast;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.model.MusicInfo;
import com.android.hhuimie.hmusic.utils.DBHelper;

public class DeleteInPlayListDialogFragment extends DialogFragment {
	private String mName;
	private MusicInfo mInfo;
	private int mPosn;

	public static DeleteInPlayListDialogFragment newInstance(String name, MusicInfo info,int posn) {
		Bundle args = new Bundle();
		args.putSerializable("info", info);
		args.putString("name", name);
		args.putInt("posn",posn);
		DeleteInPlayListDialogFragment fragment = new DeleteInPlayListDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mName = getArguments().getString("name");
		mInfo = (MusicInfo) getArguments().getSerializable("info");
		mPosn = getArguments().getInt("posn");
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				                              .setTitle("提醒")
				                              .setMessage("确认删除歌曲?这不会删除本地文件")
				                              .setPositiveButton("确定", new DialogInterface.OnClickListener() {
					                              @Override
					                              public void onClick(DialogInterface dialog, int which) {
						                              boolean b = new DBHelper(getActivity()).deleteSongInPlayList(mName, mInfo.getId());
						                              MainFragment fragment = MainFragment.getMainFragment();
						                              fragment.getMusicInfos().remove(mInfo);
						                              fragment.getMusicInfosTemp().remove(mInfo);
						                              fragment.setServiceMusicList();
						                              fragment.checkIfPinned(fragment.getMusicListAdapter(), fragment.getMusicInfos(), mPosn);
						                              fragment.getMusicListAdapter().notifyDataSetChanged();
						                              String msg;
						                              if (b) msg = "删除成功";
						                              else msg = "删除失败";
						                              Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
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
