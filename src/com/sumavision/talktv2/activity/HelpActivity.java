package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.umeng.analytics.MobclickAgent;

/**
 * @author jianghao
 * @version 2.2
 * @description 帮助动界面
 * @createTime 2012-1-5
 * @changeLog
 */
public class HelpActivity extends Activity implements OnClickListener,
		OnPageChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		initViews();
	}

	private ViewPager viewPager;
	private TextView closeBtn;

	private void initViews() {
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		LayoutInflater inflater = LayoutInflater.from(this);
		View view1 = inflater.inflate(R.layout.help_pic_frame, null);
		((ImageView) view1.findViewById(R.id.imageView))
				.setImageResource(R.drawable.help_one);
		View view2 = inflater.inflate(R.layout.help_pic_frame, null);
		((ImageView) view2.findViewById(R.id.imageView))
				.setImageResource(R.drawable.help_two);
		View view3 = inflater.inflate(R.layout.help_pic_frame, null);
		((ImageView) view3.findViewById(R.id.imageView))
				.setImageResource(R.drawable.help_three);
		View view4 = inflater.inflate(R.layout.help_pic_frame, null);
		((ImageView) view4.findViewById(R.id.imageView))
				.setImageResource(R.drawable.help_four);
		// View view5 = inflater.inflate(R.layout.help_pic_frame, null);
		// ((ImageView) view4.findViewById(R.id.imageView))
		// .setImageResource(R.drawable.help_five);
		ArrayList<View> listViews = new ArrayList<View>();
		listViews.add(view1);
		listViews.add(view2);
		listViews.add(view3);
		listViews.add(view4);
		// listViews.add(view5);
		view4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				SaveUserData();
				finish();
			}
		});
		viewPager.setAdapter(new AwesomeAdapter(listViews));
		viewPager.setOnPageChangeListener(this);

		findViewById(R.id.tip).setOnClickListener(this);
		closeBtn = (TextView) findViewById(R.id.tip);
		closeBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tip:
			setResult(RESULT_OK);
			SaveUserData();
			finish();
			break;
		default:
			break;
		}
	}

	private void SaveUserData() {

		SharedPreferences sp = getSharedPreferences("otherInfo", MODE_PRIVATE);
		Editor spEd = sp.edit();
		spEd.putBoolean("isShowHelp", false);
		spEd.commit();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPageSelected(int arg0) {
		if (arg0 == 3) {
			closeBtn.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
