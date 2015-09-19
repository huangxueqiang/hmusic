package com.android.hhuimie.hmusic.ResideMenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.hhuimie.hmusic.R;


public class ResideMenuItem extends LinearLayout {


	private ImageView iv_icon;

	private TextView tv_title;

	public ResideMenuItem(Context context) {
		super(context);
		initViews(context);
	}

	public ResideMenuItem(Context context, int icon, String title) {
		super(context);
		initViews(context);
		iv_icon.setImageResource(icon);
		tv_title.setText(title);
	}

	private void initViews(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.residemenu_item, this);
		iv_icon = (ImageView) findViewById(R.id.iv_icon);
		tv_title = (TextView) findViewById(R.id.tv_title);
	}

}
