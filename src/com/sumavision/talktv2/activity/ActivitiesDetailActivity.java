package com.sumavision.talktv2.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.OptionData;
import com.sumavision.talktv2.data.PlayNewData;
import com.sumavision.talktv2.net.ActivityDetailParser;
import com.sumavision.talktv2.net.ActivityDetailRequest;
import com.sumavision.talktv2.net.ActivityJoinOptionRequest;
import com.sumavision.talktv2.net.ActivityJoinParser;
import com.sumavision.talktv2.net.ActivityJoinRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.task.GetActivityDetailTask;
import com.sumavision.talktv2.task.JoinActivityTask;
import com.sumavision.talktv2.task.SubmitActivityOptionTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.CommonUtils;
import com.sumavision.talktv2.utils.DialogUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * 
 * @author jianghao 活动详情 2013 1-3
 * 
 */
public class ActivitiesDetailActivity extends Activity implements
		OnClickListener, NetConnectionListener, OnCheckedChangeListener {

	private int id;
	private String name;
	private int from;

	public static final int FROM_ALL = 1;
	public static final int FROM_MY = 2;
	public static final int FROM_PROGRAM = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		id = getIntent().getIntExtra("id", 0);
		name = getIntent().getStringExtra("name");
		PlayNewData.current.id = id;

		if (name != null && !name.equals("")) {
			PlayNewData.current.name = name;
		}

		setContentView(R.layout.activities_detail);
		initOthers();
		initView();
		setListeners();
		GetActivityDetailData();
	}

	private AsyncImageLoader imageLoader;

	private void initOthers() {
		imageLoader = new AsyncImageLoader();
		percentAnimation = AnimationUtils.loadAnimation(this,
				R.anim.vote_percent);
	}

	ImageView imageView;
	TextView countView;
	TextView deadLineView;
	TextView introView;
	TextView scheduleView;
	TextView title;
	private ImageButton takePartInImageView;
	private ImageButton submit;
	private LinearLayout optionLayout;
	// RadioGroup radioGroup;
	TextView errText;
	ProgressBar progressBar;
	private RelativeLayout contentView;

	private void initView() {
		title = (TextView) findViewById(R.id.activity_detail_title);
		imageView = (ImageView) findViewById(R.id.pic);
		countView = (TextView) findViewById(R.id.person_count);
		deadLineView = (TextView) findViewById(R.id.deadline);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		errText = (TextView) findViewById(R.id.err_text);
		contentView = (RelativeLayout) findViewById(R.id.content_Layout);
		introView = (TextView) findViewById(R.id.description);
		takePartInImageView = (ImageButton) findViewById(R.id.takepartin);
		scheduleView = (TextView) findViewById(R.id.schedule);
		if (from == FROM_MY) {
			PlayNewData.current.state = getIntent().getIntExtra("state", 2);
			takePartInImageView.setVisibility(View.GONE);
		}
		optionLayout = (LinearLayout) findViewById(R.id.option_layout);
		// radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
		submit = (ImageButton) findViewById(R.id.submit);
	}

	private void setListeners() {
		findViewById(R.id.back).setOnClickListener(this);
		takePartInImageView.setOnClickListener(this);
		submit.setOnClickListener(this);
	}

	private void updateUI() {
		contentView.setVisibility(View.VISIBLE);
		errText.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);
		String count = String.valueOf(PlayNewData.current.userCount);

		title.setText(PlayNewData.current.name);
		String countStr = "总共" + count + "人领取";
		int firstIndex = 2;
		int lastIndex = countStr.indexOf("人");
		SpannableString spannableString = CommonUtils.getSpannableString(
				countStr, firstIndex, lastIndex, new ForegroundColorSpan(
						Color.RED));
		countView.setText(spannableString);

		String intro = PlayNewData.current.intro;
		if (intro != null) {
			introView.setText(intro);
		}
		if (PlayNewData.current.state == 2) {
			String time = PlayNewData.current.timeDiff;
			deadLineView.setText(time);
			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			lp.setMargins(10, 0, 10, 0);
			MobclickAgent.onEvent(getApplicationContext(), "canjia");
			switch (PlayNewData.current.joinStatus) {
			case 1:
				if ((PlayNewData.current.targetType == 5)
						|| (PlayNewData.current.targetType == 6)) {
					for (int i = 0; i < PlayNewData.current.getOptions().size(); i++) {
						CheckBox r = new CheckBox(getApplicationContext());
						r.setId(i);
						r.setButtonDrawable(R.drawable.option);
						r.setText(PlayNewData.current.getOptions().get(i).content);
						r.setTextColor(Color.parseColor("#7c7c7c"));
						r.setChecked(false);
						r.setLayoutParams(lp);
						r.setOnCheckedChangeListener(ActivitiesDetailActivity.this);
						optionLayout.addView(r, i);
					}
					optionLayout.setVisibility(View.VISIBLE);
					takePartInImageView.setVisibility(View.GONE);
				} else {
					optionLayout.setVisibility(View.GONE);
					takePartInImageView
							.setImageResource(R.drawable.activity_take_part_in);
					takePartInImageView.setClickable(true);
					takePartInImageView.setVisibility(View.VISIBLE);
				}
				break;
			case 2:
				scheduleView.setVisibility(View.VISIBLE);
				scheduleView.setText(PlayNewData.current.schedule);
				takePartInImageView
						.setImageResource(R.drawable.activity_undone);
				takePartInImageView.setClickable(true);
				if (PlayNewData.current.eventTypeCode != 2020) {
					takePartInImageView.setOnClickListener(closeMe);
				}
				takePartInImageView.setVisibility(View.VISIBLE);
				break;
			case 3:
				if (PlayNewData.current.targetType == 5) {
					generateVoteResultView(PlayNewData.current.getOptions());
					takePartInImageView.setVisibility(View.GONE);
				} else {
					takePartInImageView
							.setImageResource(R.drawable.activity_ddone);
					takePartInImageView.setClickable(true);
					takePartInImageView.setVisibility(View.VISIBLE);
				}
				break;
			default:
				break;
			}
		} else if (PlayNewData.current.state == 1) {
			takePartInImageView.setVisibility(View.GONE);
		} else if (PlayNewData.current.state == 3) {
			takePartInImageView.setVisibility(View.GONE);
		} else {
			findViewById(R.id.tip).setVisibility(View.GONE);
			deadLineView.setVisibility(View.GONE);
		}

		String url = PlayNewData.current.pic;
		if (url != null) {
			imageView.setTag(url);
			loadImage(imageView, url);
		}
	}

	private final OnClickListener closeMe = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// finish();
			dialog(TYPE_FINISH);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUEST_LOGIN:
				if ((PlayNewData.current.targetType == 5)
						|| (PlayNewData.current.targetType == 6)) {
					String choosed = "";
					for (int i = 0; i < optionLayout.getChildCount() - 1; i++) {
						try {
							CheckBox c = (CheckBox) optionLayout.getChildAt(i);
							if (c.isChecked())
								if (choosed.equals(""))
									choosed += PlayNewData.current.getOptions()
											.get(i).id;
								else
									choosed += ","
											+ PlayNewData.current.getOptions()
													.get(i).id;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (!choosed.equals("")) {
						submitOption(choosed);
					} else {
						DialogUtil.alertToast(getApplicationContext(),
								"请至少选择一项");
					}
				} else
					joinActivity();
				break;
			default:
				break;
			}
		} else {
			switch (requestCode) {
			case REQUEST_LOGIN:
				break;

			default:
				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private final int REQUEST_LOGIN = 1;

	private void openLoginActivity() {
		Intent intent = new Intent(ActivitiesDetailActivity.this,
				LoginActivity.class);
		startActivityForResult(intent, REQUEST_LOGIN);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			GetActivityDetailData();
			break;
		case R.id.back:
			close();
			break;
		case R.id.submit:
		case R.id.takepartin:
			if (UserNow.current().userID == 0) {
				openLoginActivity();
			} else {
				if (PlayNewData.current.isWeb == PlayNewData.WEB_SHOW) {
					openActivityWebShowActivity(PlayNewData.current.id);
				} else {
					if (PlayNewData.current.joinStatus == 3) {
						finish();
					} else {
						if ((PlayNewData.current.targetType == 5)
								|| (PlayNewData.current.targetType == 6)) {
							String choosed = "";
							for (int i = 0; i < optionLayout.getChildCount() - 1; i++) {
								try {
									CheckBox c = (CheckBox) optionLayout
											.getChildAt(i);
									if (c.isChecked())
										if (choosed.equals(""))
											choosed += PlayNewData.current
													.getOptions().get(i).id;
										else
											choosed += ","
													+ PlayNewData.current
															.getOptions()
															.get(i).id;
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							if (!choosed.equals("")) {
								submitOption(choosed);
							} else {
								DialogUtil.alertToast(getApplicationContext(),
										"请至少选择一项");
							}
						} else
							dialog(TYPE_JOIN);
					}
				}
			}
			break;
		default:
			break;
		}
	}

	private SubmitActivityOptionTask submitActivityOptionTask;

	private void submitOption(String choosed) {
		if (submitActivityOptionTask == null) {
			submitActivityOptionTask = new SubmitActivityOptionTask(this);
			submitActivityOptionTask.execute(this,
					new ActivityJoinOptionRequest(PlayNewData.current.id,
							choosed));
		}
	}

	private JoinActivityTask joinActivityTask;

	private void joinActivity() {
		if (joinActivityTask == null) {
			joinActivityTask = new JoinActivityTask(this);
			joinActivityTask.execute(this, new ActivityJoinRequest(
					PlayNewData.current.id));
		}

	}

	private void jumpToEvent() {
		int eventType = PlayNewData.current.eventTypeCode;
		// 事件类型（决定页面跳转）：
		// 0=不跳转
		// 1003=上传头像（用户信息修改页），2010=节目签到（节目详情页）
		// ，2003=发表评论（节目详情页），2005=转发评论（节目详情页）
		// ，2009=关注好友（好友推荐页），2011=预约节目单（节目详情页），
		// 2020=节目追剧（节目详情页），1004=绑定新浪微博（账号绑定页），
		// 1007=绑定腾讯微博（账号绑定页），1006=绑定人人网（账号绑定页）
		switch (eventType) {
		case 1003:
			startActivity(new Intent(ActivitiesDetailActivity.this,
					UserInfoEditActivity.class));
			break;
		case 1004:
		case 1006:
		case 1007:
			// finish();
			dialog(TYPE_FINISH);
			// TODO 账号绑定页
			break;
		case 2003:
		case 2005:
		case 2010:
		case 2011:
		case 2020:
			// 到节目详情页
			String programId = String.valueOf(PlayNewData.current.programId);
			String topicId = String.valueOf(PlayNewData.current.topicId);
			// VodProgramData.current.cpId = 0;
			openProgramDetailActivity(programId, topicId);
			break;
		case 2009:
			// finish();
			dialog(TYPE_FINISH);
			break;
		default:
			break;
		}
	}

	private void close() {
		if (getActivityDetailTask != null) {
			getActivityDetailTask.cancel(true);
		}
		finish();
	}

	private GetActivityDetailTask getActivityDetailTask;

	private void GetActivityDetailData() {
		if (getActivityDetailTask == null) {
			PlayNewData.current.id = id;
			getActivityDetailTask = new GetActivityDetailTask(this);
			getActivityDetailTask.execute(this, new ActivityDetailRequest());
			errText.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
		}
	}

	private void openProgramDetailActivity(String id, String topicId) {
		Intent intent = new Intent(this, ProgramNewActivity.class);
		intent.putExtra("programId", id);
		intent.putExtra("topicId", topicId);
		intent.putExtra("cpId", 0);
		startActivity(intent);
	}

	private void loadImage(final ImageView imageView, String url) {
		if (url != null) {
			Drawable bitmap = imageLoader.loadDrawable(url,
					new AsyncImageLoader.ImageCallback() {
						@Override
						public void imageLoaded(Drawable imageDrawable,
								String imageUrl) {
							String selfUrl = (String) imageView.getTag();
							if (selfUrl != null && selfUrl.equals(imageUrl)) {
								imageView.setImageDrawable(imageDrawable);
							}
						}
					});
			if (bitmap != null) {
				imageView.setImageDrawable(bitmap);
			} else {
				imageView
						.setImageResource(R.drawable.rcmd_list_item_pic_default);
			}
		}
	}

	@Override
	public void onNetBegin(String method) {

	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("activityDetail".equals(method)) {
			String result = new ActivityDetailParser().parse(msg,
					PlayNewData.current);
			if (result != null && "".equals(result)) {
				updateUI();
			} else {
				progressBar.setVisibility(View.GONE);
				errText.setVisibility(View.VISIBLE);
			}
			getActivityDetailTask = null;
		} else if ("activityJoin".equals(method)) {
			String result = new ActivityJoinParser().parse(msg);
			if (result != null && "".equals(result)) {
				PlayNewData.current.joinStatus = 2;
				takePartInImageView
						.setImageResource(R.drawable.activity_undone);
				takePartInImageView.setClickable(true);
				takePartInImageView.setOnClickListener(closeMe);

				if (UserNow.current().getNewBadge() != null) {
					for (int i = 0; i < UserNow.current().getNewBadge().size(); i++) {
						String name = UserNow.current().getNewBadge().get(i).name;
						if (name != null) {
							DialogUtil.showBadgeAddToast(
									ActivitiesDetailActivity.this, name);
						}
					}
					UserNow.current().setNewBadge(null);
				}
				jumpToEvent();
			} else {
				Toast.makeText(ActivitiesDetailActivity.this, "网络不给力",
						Toast.LENGTH_SHORT).show();
			}
			joinActivityTask = null;
		} else if ("activityJoinOption".equals(method)) {
			String result = new ActivityDetailParser().parse(msg,
					PlayNewData.current);
			if (result != null && "".equals(result)) {
				updateUI();
				// 隐藏投票和原来提交的按钮的布局
				submit.setVisibility(View.GONE);
				optionLayout.setVisibility(View.GONE);
				if (UserNow.current().getNewBadge() != null) {
					for (int i = 0; i < UserNow.current().getNewBadge().size(); i++) {
						String name = UserNow.current().getNewBadge().get(i).name;
						if (name != null) {
							DialogUtil.showBadgeAddToast(
									ActivitiesDetailActivity.this, name);
						}
					}
					UserNow.current().setNewBadge(null);
				}
			} else {
				progressBar.setVisibility(View.GONE);
				errText.setVisibility(View.VISIBLE);
				Toast.makeText(ActivitiesDetailActivity.this, "网络不给力",
						Toast.LENGTH_SHORT).show();
			}
			submitActivityOptionTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

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

	// 参加活动
	private final int TYPE_JOIN = 1;
	// 关闭活动页面
	private final int TYPE_FINISH = 2;

	protected void dialog(final int type) {
		AlertDialog.Builder builder = new Builder(this);
		builder.setIcon(R.drawable.icon_small);
		builder.setTitle("电视粉温馨提示");
		builder.setMessage("您已参加了" + "\"" + PlayNewData.current.name + "\""
				+ "徽章活动，完成后即可获得徽章，收集特定徽章还可参加抽奖哦！");
		builder.setCancelable(false);
		builder.setPositiveButton("确定",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

						if (type == TYPE_JOIN) {

							switch (PlayNewData.current.joinStatus) {
							case 1:
								joinActivity();
								break;
							case 2:
								jumpToEvent();
								break;
							case 3:
								finish();
								break;
							default:
								finish();
								break;
							}
						} else {
							finish();
						}
					}
				});
		builder.create().show();
	}

	private int selectedCount = 0;

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			if (selectedCount < PlayNewData.current.selectCount) {
				selectedCount++;
			} else {
				DialogUtil.alertToast(getApplicationContext(), "最多只能选"
						+ PlayNewData.current.selectCount + "项");
				buttonView.setChecked(false);
			}
		} else {
			selectedCount--;
		}
	}

	/**
	 * 活动的网页展示形式
	 * 
	 * @param activityId
	 */
	private void openActivityWebShowActivity(int activityId) {
		Intent intent = new Intent(this, ActivityWebShowActivity.class);
		intent.putExtra("activityId", activityId);
		startActivity(intent);
	}

	private Animation percentAnimation;

	private void generateVoteResultView(List<OptionData> options) {
		if (options == null || options.size() == 0) {
			return;
		}
		ViewStub viewStub = (ViewStub) findViewById(R.id.result_layout);
		viewStub.setLayoutResource(R.layout.activity_vote_result_layout);
		LinearLayout view = (LinearLayout) viewStub.inflate();
		// 用来标注投票率的不同颜色 4个为一周期
		int j = 0;
		LayoutInflater inflater = LayoutInflater.from(this);
		// 用来记住票数的总额
		long totalCount = 0;
		for (OptionData optionData : options) {
			totalCount += optionData.countUser;
		}
		int maxWith = CommonUtils.dip2px(this, 200);
		// 百分率格式化工具
		for (OptionData optionData : options) {
			View optionLayout = inflater.inflate(
					R.layout.activity_vote_result_item, null);
			CheckBox checkBox = (CheckBox) optionLayout
					.findViewById(R.id.checkBox);
			if (optionData.isChosed == 1) {
				checkBox.setChecked(true);
			}
			if (optionData.content != null)
				checkBox.setText(optionData.content);
			TextView valueView = (TextView) optionLayout
					.findViewById(R.id.percent_value);
			if (totalCount > 0) {
				String percent = String.valueOf(optionData.countUser * 100
						/ totalCount);
				valueView.setText(percent + "%");
				TextView textView = (TextView) optionLayout
						.findViewById(R.id.percent_view);
				setBackground(textView, j);
				int width = (int) (optionData.countUser * maxWith / totalCount);
				textView.setLayoutParams(new LinearLayout.LayoutParams(width,
						android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));
				textView.startAnimation(percentAnimation);
			}
			view.addView(optionLayout, j);
			j++;
		}
	}

	/**
	 * 投票结果VIEW的设置背景方法
	 * 
	 * @param i
	 */
	private void setBackground(TextView textView, int i) {
		int resId = 0;
		switch (i % 5) {
		case 0:
			resId = R.drawable.vote_pct_blue;
			break;
		case 1:
			resId = R.drawable.vote_pct_red;
			break;
		case 2:
			resId = R.drawable.vote_pct_green;
			break;
		case 3:
			resId = R.drawable.vote_pct_grey;
			break;
		case 4:
			resId = R.drawable.vote_cpt_pink;
			break;
		default:
			resId = R.drawable.vote_pct_blue;
			break;
		}
		textView.setBackgroundResource(resId);
	}
}
