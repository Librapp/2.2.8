package com.sumavision.talktv2.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.sumavision.talktv2.R;

public class ProgramCommentListView extends ListView {
	public ProgramCommentListView(Context context) {
		super(context);
	}

	public ProgramCommentListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ProgramCommentListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

	}

	private View footerView;

	private void init(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		footerView = inflater.inflate(R.layout.pd_comment_footer_view, null);
		footerView.setVisibility(View.VISIBLE);
	}

	private boolean hasFooter;

	public void setAdapter(BaseAdapter adapter) {
		if (hasFooter) {
			this.removeFooterView(footerView);
		}
		addFooterView(footerView);
		super.setAdapter(adapter);
		hasFooter = true;
	}

}
