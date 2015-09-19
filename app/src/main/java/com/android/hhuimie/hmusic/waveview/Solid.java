package com.android.hhuimie.hmusic.waveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

class Solid extends View {

	private Paint mAboveWavePaint;
	private Paint mBlowWavePaint;

	public Solid(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Solid(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		setLayoutParams(params);
	}

	public void setAboveWavePaint(Paint aboveWavePaint) {
		mAboveWavePaint = aboveWavePaint;
	}

	public void setBlowWavePaint(Paint blowWavePaint) {
		mBlowWavePaint = blowWavePaint;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawRect(getLeft(), 0, getRight(), getBottom(), mBlowWavePaint);
		canvas.drawRect(getLeft(), 0, getRight(), getBottom(), mAboveWavePaint);
	}
}
