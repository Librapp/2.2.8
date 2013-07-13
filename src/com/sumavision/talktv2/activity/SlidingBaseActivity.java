package com.sumavision.talktv2.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;
import com.sumavision.talktv2.R;
import com.sumavision.talktv2.fragment.ActivityFragment;
import com.sumavision.talktv2.fragment.ChannelAllFragment;
import com.sumavision.talktv2.fragment.ChannelRankingFragment;
import com.sumavision.talktv2.fragment.ColumnFragment;
import com.sumavision.talktv2.fragment.FriendAllFragment;
import com.sumavision.talktv2.fragment.FriendSearchFragment;
import com.sumavision.talktv2.fragment.RecommendFragment;
import com.sumavision.talktv2.fragment.SpecialFragment;
import com.sumavision.talktv2.fragment.TalkTvMenuFragment;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;
import com.sumavision.talktv2.utils.DialogUtil;

public class SlidingBaseActivity extends SlidingFragmentActivity implements
		OnClickListener {
	public static final int request_login = 1;
	public static final int request_regist = 2;
	TalkTvMenuFragment behideFragment;

	private Button functionBtn;
	private TextView title;
	private Button icon;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.content_view);

		if (recommendFragment == null) {
			recommendFragment = new RecommendFragment();
		}
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, recommendFragment).commit();
		setBehindContentView(R.layout.behind_layout);
		behideFragment = new TalkTvMenuFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, behideFragment).commit();
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setBehindScrollScale(0);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		getSupportActionBar().hide();
		functionBtn = (Button) findViewById(R.id.btn);
		functionBtn.setOnClickListener(this);
		title = (TextView) findViewById(R.id.title);
		icon = (Button) findViewById(R.id.icon);
		icon.setOnClickListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// if (item.getItemId() == android.R.id.home) {
		// toggle();
		// return true;
		// }
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	private RecommendFragment recommendFragment;
	private FriendAllFragment friendAllFragment;
	private FriendSearchFragment friendSearchFragment;
	private ActivityFragment activityFragment;
	private ChannelRankingFragment channelRankingFragment;
	private ChannelAllFragment channelAllFragment;
	private ColumnFragment tvFragment;
	private ColumnFragment overseaFragment;
	private ColumnFragment zongyiFragment;
	private ColumnFragment movieFragment;
	private SpecialFragment specialFragment;

	public void switchFragment(Fragment fragment) {
		// this.fragment = fragment;
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.content_frame, fragment).commit();
		executeSlidingAnimation();
	}

	private int currentType = Constants.type_recommend;

	public void switchFragment(int type) {
		switch (type) {
		case Constants.type_recommend:
			if (recommendFragment == null) {
				recommendFragment = new RecommendFragment();
			}
			switchFragment(recommendFragment);
			break;
		case Constants.type_friend_all:
			if (friendAllFragment == null) {
				friendAllFragment = new FriendAllFragment();
			}
			switchFragment(friendAllFragment);
			break;
		case Constants.type_friend_search:
			if (friendSearchFragment == null) {
				friendSearchFragment = new FriendSearchFragment();
			}
			switchFragment(friendSearchFragment);
			break;
		case Constants.type_medal:
			if (activityFragment == null) {
				activityFragment = new ActivityFragment();
			}
			switchFragment(activityFragment);
			break;
		case Constants.type_live_ranking:
			if (channelRankingFragment == null) {
				channelRankingFragment = new ChannelRankingFragment();
			}
			switchFragment(channelRankingFragment);
			break;
		case Constants.type_live_all:
			if (channelAllFragment == null) {
				channelAllFragment = new ChannelAllFragment();
			}
			switchFragment(channelAllFragment);
			break;
		case Constants.type_oversea:
			if (overseaFragment == null) {
				overseaFragment = new ColumnFragment(
						Constants.column_id_oversea);
			}
			switchFragment(overseaFragment);
			break;
		case Constants.type_tv:
			if (tvFragment == null) {
				tvFragment = new ColumnFragment(Constants.column_id_tv);
			}
			switchFragment(tvFragment);
			break;
		case Constants.type_zongyi:
			if (zongyiFragment == null) {
				zongyiFragment = new ColumnFragment(Constants.column_id_zongyi);
			}
			switchFragment(zongyiFragment);
			break;
		case Constants.type_special:
			if (specialFragment == null) {
				specialFragment = new SpecialFragment();
			}
			switchFragment(specialFragment);
			break;
		case Constants.type_movie:
			if (movieFragment == null) {
				movieFragment = new ColumnFragment(Constants.column_id_movie);
			}
			switchFragment(movieFragment);
			break;
		default:
			break;
		}
		currentType = type;
		setBtn();
		setActionBarTitle(type);
	}

	private Handler h = new Handler();

	private void executeSlidingAnimation() {

		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				getSlidingMenu().showContent();
			}
		}, Constants.animation_duration);
	}

	private void setBtn() {
		Resources res = getResources();

		switch (currentType) {
		case Constants.type_recommend:
		case Constants.type_tv:
		case Constants.type_movie:
		case Constants.type_oversea:
		case Constants.type_zongyi:
		case Constants.type_special:
			functionBtn.setText(res
					.getString(R.string.navigator_btn_playhistory));
			functionBtn.setVisibility(View.VISIBLE);
			break;
		case Constants.type_live_all:
			if (channelAllFragment.getEditState()) {
				functionBtn.setText(res
						.getString(R.string.navigator_channel_complete));
			} else {
				functionBtn.setText(res
						.getString(R.string.navigator_channel_edit));

			}
			functionBtn.setVisibility(View.VISIBLE);
			break;
		case Constants.type_live_ranking:
			if (channelRankingFragment.getEditState()) {
				functionBtn.setText(res
						.getString(R.string.navigator_channel_complete));
			} else {
				functionBtn.setText(res
						.getString(R.string.navigator_channel_edit));
			}
			functionBtn.setVisibility(View.VISIBLE);
			break;
		case Constants.type_friend_search:
			functionBtn.setText(res.getString(R.string.navigator_search_clear));
			functionBtn.setVisibility(View.VISIBLE);
			break;
		default:
			functionBtn.setVisibility(View.GONE);
			break;
		}
	}

	private void setActionBarTitle(int type) {
		int resId = R.string.menu_recommend;
		switch (type) {
		case Constants.type_recommend:
			resId = R.string.menu_recommend;
			break;
		case Constants.type_friend_all:
			resId = R.string.menu_friend_all;
			break;
		case Constants.type_friend_search:
			resId = R.string.menu_friend_search;
			break;
		case Constants.type_medal:
			resId = R.string.menu_medal;
			break;
		case Constants.type_live_ranking:
			resId = R.string.menu_live_ranking;
			break;
		case Constants.type_live_all:
			resId = R.string.menu_live_all;
			break;
		case Constants.type_oversea:
			resId = R.string.menu_oversea;
			break;
		case Constants.type_tv:
			resId = R.string.menu_tv;
			break;
		case Constants.type_zongyi:
			resId = R.string.menu_zongyi;
			break;
		case Constants.type_special:
			resId = R.string.menu_special;
			break;
		case Constants.type_movie:
			resId = R.string.menu_movie;
			break;
		default:
			break;
		}
		title.setText(getResources().getString(resId));
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btn) {
			switch (currentType) {
			case Constants.type_recommend:
			case Constants.type_tv:
			case Constants.type_movie:
			case Constants.type_oversea:
			case Constants.type_zongyi:
			case Constants.type_special:
				openPlayHistoryActivity();
				break;
			case Constants.type_live_all:
				if (UserNow.current().userID == 0) {
					Intent intent = new Intent(this, LoginActivity.class);
					startActivity(intent);
				} else {
					channelAllFragment.changeEditState();
					if (channelAllFragment.getEditState()) {
						functionBtn.setText(getResources().getString(
								R.string.navigator_channel_complete));
					} else {
						functionBtn.setText(getResources().getString(
								R.string.navigator_channel_edit));
						DialogUtil.alertToast(getApplicationContext(),
								"请到“我的”页面去查看您订制的频道吧");
					}
				}
				break;
			case Constants.type_live_ranking:
				if (UserNow.current().userID == 0) {
					Intent intent = new Intent(this, LoginActivity.class);
					startActivity(intent);
				} else {
					channelRankingFragment.changeEditState();
					if (channelRankingFragment.getEditState()) {
						functionBtn.setText(getResources().getString(
								R.string.navigator_channel_complete));
					} else {
						functionBtn.setText(getResources().getString(
								R.string.navigator_channel_edit));
						DialogUtil.alertToast(getApplicationContext(),
								"请到“我的”页面去查看您订制的频道吧");
					}
				}
				break;
			case Constants.type_friend_search:
				friendSearchFragment.onSearchSubmitClick();
				break;
			default:
				break;
			}
		} else if (view.getId() == R.id.icon) {
			if (currentType == Constants.type_friend_search) {
				if (!getSlidingMenu().isMenuShowing()) {
					// hideSoftPad();
				}
			}
			toggle();
		}
	}

	private void hideSoftPad() {
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

	}

	private void openPlayHistoryActivity() {
		Intent intent = new Intent(this, PlayHistoryActivity.class);
		startActivity(intent);
	}

	private long lastTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (getSlidingMenu().isMenuShowing()) {
				return super.onKeyDown(keyCode, event);
			} else {
				long currentTime = System.currentTimeMillis();
				if (currentTime - lastTime >= 0
						&& currentTime - lastTime <= 2000) {
					return super.onKeyDown(keyCode, event);
				} else {
					DialogUtil.alertToast(getApplicationContext(), "再按一次退出电视粉");
					lastTime = currentTime;
				}
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
