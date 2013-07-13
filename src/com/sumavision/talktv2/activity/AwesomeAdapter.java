package com.sumavision.talktv2.activity;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class AwesomeAdapter extends PagerAdapter {
	private List<View> listViews;

	public AwesomeAdapter(List<View> listViews) {
		this.listViews = listViews;
	}

	@Override
	public int getCount() {
		return listViews.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public Object instantiateItem(View container, int position) {
		((ViewPager) container).addView(listViews.get(position), 0);
		return listViews.get(position);
	}

	@Override
	public void destroyItem(View collection, int position, Object view) {
		((ViewPager) collection).removeView(listViews.get(position));

	}
}
