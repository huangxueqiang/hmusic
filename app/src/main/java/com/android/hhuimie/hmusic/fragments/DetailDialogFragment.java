package com.android.hhuimie.hmusic.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.hhuimie.hmusic.R;
import com.android.hhuimie.hmusic.model.MusicInfo;
import com.android.hhuimie.hmusic.utils.MusicInfoUtil;

public class DetailDialogFragment extends DialogFragment {
	private View mView;
	private MusicInfo mInfo;
	private TextView mUrl;
	private TextView mType;
	private TextView mDuration;
	private TextView mSize;
	private TextView mTitle;
	private TextView mTrack;
	private TextView mYear;
	private TextView mArtist;
	private TextView mAlbum;
	private TextView mDate;

	public static DetailDialogFragment newInstance(MusicInfo info) {
		Bundle args = new Bundle();
		args.putSerializable("detail", info);
		DetailDialogFragment fragment = new DetailDialogFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		initView();
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				                              .setTitle("详情")
				                              .setView(mView)
				                              .setPositiveButton("确定", new DialogInterface.OnClickListener() {
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

	private void initView() {
		mView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_detail, null);
		mInfo = (MusicInfo) getArguments().getSerializable("detail");
		findViewById();
		initTextView();
	}

	private void findViewById() {
		mUrl = (TextView) mView.findViewById(R.id.detail_url);
		mType = (TextView) mView.findViewById(R.id.detail_type);
		mDuration = (TextView) mView.findViewById(R.id.detail_duration);
		mSize = (TextView) mView.findViewById(R.id.detail_size);
		mTitle = (TextView) mView.findViewById(R.id.detail_title);
		mTrack = (TextView) mView.findViewById(R.id.detail_track);
		mYear = (TextView) mView.findViewById(R.id.detail_year);
		mArtist = (TextView) mView.findViewById(R.id.detail_artist);
		mAlbum = (TextView) mView.findViewById(R.id.detail_album);
		mDate = (TextView) mView.findViewById(R.id.detail_date);
	}

	private void initTextView() {
		mUrl.setText(mInfo.getUrl());
		mType.setText(mInfo.getType());
		mDuration.setText(MusicInfoUtil.getFormatDuration(mInfo.getDuration()));
		mSize.setText(MusicInfoUtil.getFormatSize(mInfo.getSize()));
		mTitle.setText(mInfo.getTitle());
		mTrack.setText(mInfo.getTrack());
		mYear.setText(mInfo.getYear());
		mArtist.setText(mInfo.getArtist());
		mAlbum.setText(mInfo.getAlbum());
		mDate.setText(mInfo.getDateFormat());
	}
}
