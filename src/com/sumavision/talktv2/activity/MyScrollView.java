package com.sumavision.talktv2.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {

	public MyScrollView(Context context) {
		super(context);
	}

	public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MyScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private int y;

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		int temp = this.getScrollY();
		if (temp != y) {
			y = temp;
			if (y <= 0) {
				if (onScrollTopListtener != null) {
					onScrollTopListtener.onTop(this);
				}
			} else {
				onScrollTopListtener.onOther(this);
			}
		}

	}

	private OnScrollTopListener onScrollTopListtener;

	public interface OnScrollTopListener {
		public void onTop(MyScrollView myScrollView);

		public void onOther(MyScrollView myScrollView);
	}

	public void setOnScrollTopListener(OnScrollTopListener onScrollTopListtener) {
		this.onScrollTopListtener = onScrollTopListtener;
	}

}
