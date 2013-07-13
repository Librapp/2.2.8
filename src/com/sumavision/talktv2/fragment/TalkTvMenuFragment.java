package com.sumavision.talktv2.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.ImageLoaderHelper;
import com.sumavision.talktv2.activity.LoginActivity;
import com.sumavision.talktv2.activity.MyActivity;
import com.sumavision.talktv2.activity.RegisterActivity;
import com.sumavision.talktv2.activity.SearchActivity;
import com.sumavision.talktv2.activity.SlidingBaseActivity;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Constants;

public class TalkTvMenuFragment extends Fragment implements OnClickListener {

	private TextView searchView;
	RelativeLayout rootView;

	private Button loginBtn, registBtn;
	private TextView nameView;
	private ImageView headPicView;
	private ImageLoaderHelper imageLoaderHelper;
	private ImageView setting;

	private TextView recommendView, tvView, overseaView, movieView, zongyiView,
			specialView, liveAllView, rankingView, friendSearchView,
			friendAllView, medalView;

	private void initUtils() {
		imageLoaderHelper = new ImageLoaderHelper();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initUtils();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.e("menu", "onCreateView");
		rootView = (RelativeLayout) inflater.inflate(
				R.layout.behind_login_layout, null);
		searchView = (TextView) rootView.findViewById(R.id.menu_search);
		searchView.setOnClickListener(mOnClickListener);

		recommendView = (TextView) rootView.findViewById(R.id.recommend);
		recommendView.setOnClickListener(this);
		recommendView.setTag(Constants.type_recommend);

		friendAllView = (TextView) rootView.findViewById(R.id.friend_all);
		friendAllView.setTag(Constants.type_friend_all);
		friendAllView.setOnClickListener(this);

		friendSearchView = (TextView) rootView.findViewById(R.id.friend_search);
		friendSearchView.setTag(Constants.type_friend_search);
		friendSearchView.setOnClickListener(this);

		liveAllView = (TextView) rootView.findViewById(R.id.live_all);
		liveAllView.setTag(Constants.type_live_all);
		liveAllView.setOnClickListener(this);

		rankingView = (TextView) rootView.findViewById(R.id.live_ranking);
		rankingView.setTag(Constants.type_live_ranking);
		rankingView.setOnClickListener(this);

		tvView = (TextView) rootView.findViewById(R.id.tv);
		tvView.setTag(Constants.type_tv);
		tvView.setOnClickListener(this);

		overseaView = (TextView) rootView.findViewById(R.id.oversea);
		overseaView.setTag(Constants.type_oversea);
		overseaView.setOnClickListener(this);

		zongyiView = (TextView) rootView.findViewById(R.id.zongyi);
		zongyiView.setTag(Constants.type_zongyi);
		zongyiView.setOnClickListener(this);

		specialView = (TextView) rootView.findViewById(R.id.special);
		specialView.setTag(Constants.type_special);
		specialView.setOnClickListener(this);

		medalView = (TextView) rootView.findViewById(R.id.medal);
		medalView.setTag(Constants.type_medal);
		medalView.setOnClickListener(this);

		movieView = (TextView) rootView.findViewById(R.id.movie);
		movieView.setTag(Constants.type_movie);
		movieView.setOnClickListener(this);
		initBg();
		changeSelectionBg();
		loginBtn = (Button) rootView.findViewById(R.id.login);
		registBtn = (Button) rootView.findViewById(R.id.regist);
		nameView = (TextView) rootView.findViewById(R.id.username);
		headPicView = (ImageView) rootView.findViewById(R.id.headpic);
		loginBtn.setOnClickListener(mOnClickListener);
		registBtn.setOnClickListener(mOnClickListener);
		setting = (ImageView) rootView.findViewById(R.id.setting);
		setting.setOnClickListener(mOnClickListener);
		rootView.findViewById(R.id.login_layout).setOnClickListener(
				mOnClickListener);
		changeLogInLayout();
		return rootView;
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.login:
				openLoginActivity();
				break;
			case R.id.regist:
				openRegistActivity();
				break;
			case R.id.menu_search:
				openSearchActivity();
				break;
			case R.id.setting:
			case R.id.login_layout:
				openUserCenterActivity();
				break;
			default:
				break;
			}
		}
	};

	private void openLoginActivity() {
		Intent intent = new Intent(getActivity(), LoginActivity.class);
		startActivityForResult(intent, SlidingBaseActivity.request_login);
	}

	private void openRegistActivity() {
		Intent intent = new Intent(getActivity(), RegisterActivity.class);
		startActivityForResult(intent, SlidingBaseActivity.request_regist);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		changeLogInLayout();

	}

	private void openUserCenterActivity() {
		Intent intent = new Intent(getActivity(), MyActivity.class);
		startActivityForResult(intent, 12);
	}

	public void changeLogInLayout() {
		if (UserNow.current().userID != 0) {
			showLogedInLayout();
		} else {
			showLoginLayout();
		}
	}

	/**
	 * 展示非登录
	 */
	private void showLoginLayout() {
		loginBtn.setVisibility(View.VISIBLE);
		registBtn.setVisibility(View.VISIBLE);
		nameView.setVisibility(View.GONE);
		headPicView.setVisibility(View.GONE);
	}

	/**
	 * 展示登录后布局
	 */
	private void showLogedInLayout() {
		loginBtn.setVisibility(View.GONE);
		registBtn.setVisibility(View.GONE);
		nameView.setVisibility(View.VISIBLE);
		headPicView.setVisibility(View.VISIBLE);
		if (UserNow.current().name != null) {
			nameView.setText(UserNow.current().name);
		}
		imageLoaderHelper.loadImage(headPicView, UserNow.current().iconURL,
				R.drawable.friend_headpic_default);
	}

	private int currentType = Constants.type_recommend;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.recommend:
			if (currentType != Constants.type_recommend) {
				currentType = Constants.type_recommend;
			}
			break;
		case R.id.friend_all:
			if (currentType != Constants.type_friend_all) {
				currentType = Constants.type_friend_all;
			}
			break;
		case R.id.friend_search:
			if (currentType != Constants.type_friend_search) {
				currentType = Constants.type_friend_search;
			}
			break;
		case R.id.live_all:
			if (currentType != Constants.type_live_all) {
				currentType = Constants.type_live_all;
			}
			break;
		case R.id.live_ranking:
			if (currentType != Constants.type_live_ranking) {
				currentType = Constants.type_live_ranking;
			}
			break;
		case R.id.medal:
			if (currentType != Constants.type_medal) {
				currentType = Constants.type_medal;
			}
			break;
		case R.id.tv:
			if (currentType != Constants.type_tv) {
				currentType = Constants.type_tv;
			}
			break;
		case R.id.oversea:
			if (currentType != Constants.type_oversea) {
				currentType = Constants.type_oversea;
			}
			break;
		case R.id.special:
			if (currentType != Constants.type_special) {
				currentType = Constants.type_special;
			}
			break;
		case R.id.zongyi:
			if (currentType != Constants.type_zongyi) {
				currentType = Constants.type_zongyi;
			}
			break;
		case R.id.movie:
			if (currentType != Constants.type_movie) {
				currentType = Constants.type_movie;
			}
			break;
		default:
			break;
		}
		if (lastType != currentType) {
			changeSelectionBg();
			switchFragment(currentType);
		}

	}

	private int lastType = -1;

	private void initBg() {
		recommendView.setBackgroundResource(R.drawable.menu_item_selected);
		friendAllView.setBackgroundResource(R.drawable.menu_item_selected);
		friendSearchView.setBackgroundResource(R.drawable.menu_item_selected);
		medalView.setBackgroundResource(R.drawable.menu_item_selected);
		rankingView.setBackgroundResource(R.drawable.menu_item_selected);
		liveAllView.setBackgroundResource(R.drawable.menu_item_selected);
		overseaView.setBackgroundResource(R.drawable.menu_item_selected);
		tvView.setBackgroundResource(R.drawable.menu_item_selected);
		zongyiView.setBackgroundResource(R.drawable.menu_item_selected);
		specialView.setBackgroundResource(R.drawable.menu_item_selected);
		movieView.setBackgroundResource(R.drawable.menu_item_selected);
		recommendView.setBackgroundResource(R.drawable.menu_item_not_selected);
		friendAllView.setBackgroundResource(R.drawable.menu_item_not_selected);
		friendSearchView
				.setBackgroundResource(R.drawable.menu_item_not_selected);
		medalView.setBackgroundResource(R.drawable.menu_item_not_selected);
		rankingView.setBackgroundResource(R.drawable.menu_item_not_selected);
		liveAllView.setBackgroundResource(R.drawable.menu_item_not_selected);
		overseaView.setBackgroundResource(R.drawable.menu_item_not_selected);
		tvView.setBackgroundResource(R.drawable.menu_item_not_selected);
		zongyiView.setBackgroundResource(R.drawable.menu_item_not_selected);
		specialView.setBackgroundResource(R.drawable.menu_item_not_selected);
		movieView.setBackgroundResource(R.drawable.menu_item_not_selected);
	}

	private void changeSelectionBg() {
		switch (currentType) {
		case Constants.type_recommend:
			recommendView.setBackgroundResource(R.drawable.menu_item_selected);
			break;
		case Constants.type_friend_all:
			friendAllView.setBackgroundResource(R.drawable.menu_item_selected);
			break;
		case Constants.type_friend_search:
			friendSearchView
					.setBackgroundResource(R.drawable.menu_item_selected);
			break;
		case Constants.type_medal:
			medalView.setBackgroundResource(R.drawable.menu_item_selected);
			break;
		case Constants.type_live_ranking:
			rankingView.setBackgroundResource(R.drawable.menu_item_selected);
			break;
		case Constants.type_live_all:
			liveAllView.setBackgroundResource(R.drawable.menu_item_selected);
			break;
		case Constants.type_oversea:
			overseaView.setBackgroundResource(R.drawable.menu_item_selected);
			break;
		case Constants.type_tv:
			tvView.setBackgroundResource(R.drawable.menu_item_selected);
			break;
		case Constants.type_zongyi:
			zongyiView.setBackgroundResource(R.drawable.menu_item_selected);
			break;
		case Constants.type_special:
			specialView.setBackgroundResource(R.drawable.menu_item_selected);
			break;
		case Constants.type_movie:
			movieView.setBackgroundResource(R.drawable.menu_item_selected);
			break;
		default:
			break;
		}
		switch (lastType) {
		case Constants.type_recommend:
			recommendView
					.setBackgroundResource(R.drawable.menu_item_not_selected);
			break;
		case Constants.type_friend_all:
			friendAllView
					.setBackgroundResource(R.drawable.menu_item_not_selected);
			break;
		case Constants.type_friend_search:
			friendSearchView
					.setBackgroundResource(R.drawable.menu_item_not_selected);
			break;
		case Constants.type_medal:
			medalView.setBackgroundResource(R.drawable.menu_item_not_selected);
			break;
		case Constants.type_live_ranking:
			rankingView
					.setBackgroundResource(R.drawable.menu_item_not_selected);
			break;
		case Constants.type_live_all:
			liveAllView
					.setBackgroundResource(R.drawable.menu_item_not_selected);
			break;
		case Constants.type_oversea:
			overseaView
					.setBackgroundResource(R.drawable.menu_item_not_selected);
			break;
		case Constants.type_tv:
			tvView.setBackgroundResource(R.drawable.menu_item_not_selected);
			break;
		case Constants.type_zongyi:
			zongyiView.setBackgroundResource(R.drawable.menu_item_not_selected);
			break;
		case Constants.type_special:
			specialView
					.setBackgroundResource(R.drawable.menu_item_not_selected);
			break;
		case Constants.type_movie:
			movieView.setBackgroundResource(R.drawable.menu_item_not_selected);
			break;
		default:
			break;
		}
		lastType = currentType;

	}

	private void openSearchActivity() {
		Intent intent = new Intent(getActivity(), SearchActivity.class);
		startActivity(intent);
	}

	private void switchFragment(int type) {
		Activity activity = getActivity();
		if (activity != null) {
			if (activity instanceof SlidingBaseActivity) {
				((SlidingBaseActivity) activity).switchFragment(type);
			}
		}
	}

}
