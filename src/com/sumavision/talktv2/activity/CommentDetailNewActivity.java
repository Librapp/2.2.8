package com.sumavision.talktv2.activity;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.SpannableString;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.CommentData;
import com.sumavision.talktv2.net.CommentDetailParser;
import com.sumavision.talktv2.net.CommentDetailRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.ReplyAddParser;
import com.sumavision.talktv2.net.ReplyTalkRequest;
import com.sumavision.talktv2.net.TalkForwardListRequest;
import com.sumavision.talktv2.net.TalkForwardParser;
import com.sumavision.talktv2.task.GetCommentDetailTask;
import com.sumavision.talktv2.task.GetForwardListTask;
import com.sumavision.talktv2.task.GetReplyListTask;
import com.sumavision.talktv2.utils.Txt2Image;
import com.umeng.analytics.MobclickAgent;

public class CommentDetailNewActivity extends Activity implements
		OnClickListener, OnPageChangeListener, NetConnectionListener {
	private String programId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment_detail_new);
		Intent i = getIntent();
		programId = i.getStringExtra("programId");
		if (null == programId)
			programId = "";
		initOthers();
		initViews();
		setListener();
		getCommentDetail();
	}

	private GetCommentDetailTask commentDetailTask;

	private void getCommentDetail() {
		if (commentDetailTask == null) {
			commentDetailTask = new GetCommentDetailTask();
			commentDetailTask.execute(this, this, new CommentDetailRequest(),
					new CommentDetailParser());
		}
	}

	private AsyncImageLoader imageLoader;

	private void initOthers() {
		imageLoader = new AsyncImageLoader();
		getMoveStep();
	}

	private void setListener() {
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.my_comment_layout).setOnClickListener(this);
		findViewById(R.id.headpic).setOnClickListener(this);
		replayCountView.setOnClickListener(this);
		zhuanfaCountView.setOnClickListener(this);
		contentImageView.setOnClickListener(this);
		rootPicImageView.setOnClickListener(this);
		bigpic.setOnClickListener(this);
		findViewById(R.id.send_reply_btn).setOnClickListener(this);
		findViewById(R.id.send_forward_btn).setOnClickListener(this);
	}

	private TextView nameTextView;
	private ImageView headpicImageView;
	private TextView contentTextView;
	private RelativeLayout rootLayout;
	private TextView rootTextView;
	private ImageView rootPicImageView;
	private ImageView contentImageView;
	private ImageView bigpic;
	private RelativeLayout audioLayout;
	private ImageView audioBtn;
	private ProgressBar audioProgressBar;
	private RelativeLayout rootAudioLayout;
	private ImageView rootAudioBtn;
	private ProgressBar rootAudioProgressBar;
	private TextView from;
	private TextView timeText;

	private TextView replayCountView;
	private TextView zhuanfaCountView;

	private ViewPager viewPager;

	private ImageView tagIndicator;
	private Animation zoomIn;
	private Animation zoomOut;

	private int step;
	private int currentPosition;

	public void executeAnimation(int position) {
		Animation animation = null;
		switch (position) {
		case 0:
			if (currentPosition == 1) {
				animation = new TranslateAnimation(step, 0, 0, 0);
			}
			break;
		case 1:
			if (currentPosition == 0) {
				animation = new TranslateAnimation(0, step, 0, 0);
			}
			break;

		default:
			break;
		}
		if (animation != null) {
			currentPosition = position;
			animation.setDuration(200);
			animation.setFillAfter(true);
			tagIndicator.startAnimation(animation);
		}
	}

	public void getMoveStep() {
		android.util.DisplayMetrics dm = new android.util.DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;
		step = screenW / 2;

	}

	private void initViews() {

		tagIndicator = (ImageView) findViewById(R.id.tab_imageView);

		nameTextView = (TextView) findViewById(R.id.name);
		headpicImageView = (ImageView) findViewById(R.id.headpic);
		contentTextView = (TextView) findViewById(R.id.content);
		contentImageView = (ImageView) findViewById(R.id.pic);
		bigpic = (ImageView) findViewById(R.id.bigpic);
		rootLayout = (RelativeLayout) findViewById(R.id.root_layout);
		rootTextView = (TextView) findViewById(R.id.root_content);
		rootPicImageView = (ImageView) findViewById(R.id.root_pic);
		from = (TextView) findViewById(R.id.from);
		timeText = (TextView) findViewById(R.id.time);
		audioLayout = (RelativeLayout) findViewById(R.id.cd_audio_layout);
		audioBtn = (ImageView) findViewById(R.id.cd_audio_pic);
		audioProgressBar = (ProgressBar) findViewById(R.id.cd_audio_progressBar);

		rootAudioLayout = (RelativeLayout) findViewById(R.id.root_audio_layout);
		rootAudioBtn = (ImageView) findViewById(R.id.root_audio_pic);
		rootAudioProgressBar = (ProgressBar) findViewById(R.id.root_audio_progressBar);

		replayCountView = (TextView) findViewById(R.id.reply_btn);
		zhuanfaCountView = (TextView) findViewById(R.id.forward_btn);
		viewPager = (ViewPager) findViewById(R.id.viewpager);
		zoomIn = AnimationUtils.loadAnimation(this, R.anim.scalebig);
		zoomOut = AnimationUtils.loadAnimation(this, R.anim.scalesmall);
		initViewPager();
	}

	private void setMyCommentInfo() {
		CommentData temp = CommentData.current();
		String userName = temp.userName;
		if (userName != null) {
			nameTextView.setText(userName);
		}
		String fromString = temp.source;
		if (fromString != null) {
			from.setText(fromString);
		}
		String time = temp.commentTime;
		if (time != null) {
			timeText.setText(time);
		}

		if (temp.talkType != 4) {
			audioLayout.setVisibility(View.GONE);
			String content = temp.content;
			if (content != null) {
				contentTextView.setText(content);
				SpannableString contentString = Txt2Image.text2Emotion(
						CommentDetailNewActivity.this, content);
				contentTextView.setText(contentString);
			}

			if (temp.talkType == 1) {
				String picUrl = temp.contentURL;
				contentImageView.setTag(picUrl);
				loadListImage(contentImageView, picUrl);
				contentImageView.setVisibility(View.VISIBLE);
			} else {
				contentImageView.setVisibility(View.GONE);
			}

			if (temp.hasRootTalk) {
				rootLayout.setVisibility(View.VISIBLE);
				String rootContent = temp.rootTalk.content;
				if (rootContent != null) {
					SpannableString contentString = Txt2Image.text2Emotion(
							CommentDetailNewActivity.this, rootContent);
					rootTextView.setText(contentString);
				}

				if (temp.rootTalk.talkType != 4) {
					rootAudioLayout.setVisibility(View.GONE);
					if (temp.rootTalk.talkType == 1) {
						String rootheadPicUrl = temp.rootTalk.contentURL;
						rootPicImageView.setTag(rootheadPicUrl);
						loadListImage(rootPicImageView, rootheadPicUrl);
						rootPicImageView.setVisibility(View.VISIBLE);
					} else {
						rootPicImageView.setVisibility(View.GONE);
					}
				} else {
					rootTextView.setVisibility(View.GONE);
					rootPicImageView.setVisibility(View.GONE);
					rootAudioLayout.setVisibility(View.VISIBLE);
					rootAudioBtn.setOnClickListener(this);
				}
			} else {
				rootLayout.setVisibility(View.GONE);
			}
		} else {
			contentTextView.setVisibility(View.GONE);
			contentImageView.setVisibility(View.GONE);
			audioLayout.setVisibility(View.VISIBLE);
			audioBtn.setOnClickListener(this);
		}

		String headPicUrl = temp.userURL;
		headpicImageView.setTag(headPicUrl);
		loadListImage(headpicImageView, headPicUrl);

		Resources res = getResources();
		String reply = res.getString(R.string.comment_reply);
		String replayCount = String.valueOf(temp.replyCount);
		replayCountView.setText(reply + replayCount);
		String forward = res.getString(R.string.comment_zhuanfa);
		String zhuanfaCount = String.valueOf(temp.forwardCount);
		zhuanfaCountView.setText(forward + zhuanfaCount);
	}

	private void initViewPager() {
		AwesomeAdapter awesomeAdapter;
		ArrayList<View> views;
		views = new ArrayList<View>();
		LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.comment_detail_reply_page, null);
		initReplyView(view);
		View detailView = inflater.inflate(
				R.layout.comment_detail_forward_page, null);
		initForwardView(detailView);
		views.add(view);
		views.add(detailView);
		awesomeAdapter = new AwesomeAdapter(views);
		viewPager.setAdapter(awesomeAdapter);
		viewPager.setOnPageChangeListener(this);

	}

	private TextView replyErrTxt;
	private ProgressBar replyProgressBar;
	private ListView replyListView;

	private void initReplyView(View view) {
		replyErrTxt = (TextView) view.findViewById(R.id.err_text);
		replyProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		replyListView = (ListView) view.findViewById(R.id.listView);

	}

	private GetReplyListTask getReplyListTask;
	private ArrayList<CommentData> replyList = new ArrayList<CommentData>();

	private void getReplyList() {
		if (getReplyListTask == null)
			getReplyListTask = new GetReplyListTask(this);
		getReplyListTask.execute(this, new ReplyTalkRequest(),
				new ReplyAddParser());
	}

	private void updateReplyView() {
		ArrayList<CommentData> temp = (ArrayList<CommentData>) CommentData
				.current().getReply();
		if ((temp != null) && (temp.size() > 0)) {
			replyList = temp;
			replyErrTxt.setVisibility(View.GONE);
			CommentAdapter adapter = new CommentAdapter(replyList);
			replyListView.setAdapter(adapter);
		} else {
			replyErrTxt.setVisibility(View.VISIBLE);
			if (CommentData.current().replyCount > 0)
				replyErrTxt.setText("加载失败，请重试");
			else
				replyErrTxt.setText("还没有回复");
		}
		Resources res = getResources();
		String reply = res.getString(R.string.comment_reply);
		String replayCount = String.valueOf(CommentData.current().replyCount);
		replayCountView.setText(reply + replayCount);
		replyProgressBar.setVisibility(View.GONE);

	}

	private TextView forwardErrTxt;
	private ProgressBar forwardProgressBar;
	private ListView forwardListView;
	private ArrayList<CommentData> forwardList = new ArrayList<CommentData>();

	private void getForwradList() {
		GetForwardListTask getForwardListTask = new GetForwardListTask(this);
		getForwardListTask.execute(this, new TalkForwardListRequest(),
				new TalkForwardParser());
	}

	private void initForwardView(View view) {
		forwardErrTxt = (TextView) view.findViewById(R.id.err_text);
		forwardProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
		forwardListView = (ListView) view.findViewById(R.id.listView);
	}

	private class CommentAdapter extends BaseAdapter {
		private ArrayList<CommentData> comments;

		public CommentAdapter(ArrayList<CommentData> list) {
			this.comments = list;
		}

		@Override
		public int getCount() {
			if (comments != null) {
				return comments.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return comments.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(CommentDetailNewActivity.this);
				convertView = inflater.inflate(R.layout.comment_list_item_new,
						null);
				viewHolder.nameTextView = (TextView) convertView
						.findViewById(R.id.name);
				viewHolder.headpicImageView = (ImageView) convertView
						.findViewById(R.id.headpic);
				viewHolder.contentTextView = (TextView) convertView
						.findViewById(R.id.content);
				viewHolder.picImageView = (ImageView) convertView
						.findViewById(R.id.pic);
				viewHolder.rootLayout = (RelativeLayout) convertView
						.findViewById(R.id.root_layout);
				viewHolder.rootTextView = (TextView) convertView
						.findViewById(R.id.root_content);
				viewHolder.rootPicImageView = (ImageView) convertView
						.findViewById(R.id.root_pic);
				viewHolder.replayCountView = (TextView) convertView
						.findViewById(R.id.replay);
				viewHolder.zhuanfaCountView = (TextView) convertView
						.findViewById(R.id.zhuanfa);
				viewHolder.from = (TextView) convertView
						.findViewById(R.id.from);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.time);

				// 语音评论
				viewHolder.audioFrame = (RelativeLayout) convertView
						.findViewById(R.id.comment_audio_btn);
				viewHolder.audioBtn = (ImageView) convertView
						.findViewById(R.id.comment_item_content_audio_pic);
				viewHolder.audioPb = (ProgressBar) convertView
						.findViewById(R.id.comment_item_progressBar);

				// 语音评论
				viewHolder.rootAudioFrame = (RelativeLayout) convertView
						.findViewById(R.id.comment_audio_btn_root);
				viewHolder.rootAudioBtn = (ImageView) convertView
						.findViewById(R.id.comment_item_content_audio_pic_root);
				viewHolder.rootAudioPb = (ProgressBar) convertView
						.findViewById(R.id.comment_item_progressBar_root);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			final CommentData temp = comments.get(position);
			String userHeadPic = temp.userURL;
			if (userHeadPic != null) {
				viewHolder.headpicImageView.setTag(userHeadPic);
				loadListImage(viewHolder.headpicImageView, userHeadPic);
			}
			String userName = temp.userName;
			if (userName != null) {
				viewHolder.nameTextView.setText(userName);
			}

			String fromString = temp.source;
			if (fromString != null) {
				viewHolder.from.setText(fromString);
			}
			String time = temp.commentTime;
			if (time != null) {
				viewHolder.time.setText(time);
			}

			String replayCount = String.valueOf(temp.replyCount);
			viewHolder.replayCountView.setText(replayCount);

			String zhuanfaCount = String.valueOf(temp.forwardCount);
			viewHolder.zhuanfaCountView.setText(zhuanfaCount);
			if (temp.talkType != 4) {
				viewHolder.audioFrame.setVisibility(View.GONE);
				String content = temp.content;
				if (content != null) {
					viewHolder.contentTextView.setText(content);
					SpannableString contentString = Txt2Image.text2Emotion(
							CommentDetailNewActivity.this, content);
					viewHolder.contentTextView.setText(contentString);
				}

				if (temp.talkType == 1) {
					final String picUrl = temp.contentURL;
					viewHolder.picImageView.setTag(picUrl);
					loadListImage(viewHolder.picImageView, picUrl);
					viewHolder.picImageView.setVisibility(View.VISIBLE);
					viewHolder.picImageView
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									zoomInPic(picUrl);
								}
							});
				} else {
					viewHolder.picImageView.setVisibility(View.GONE);
				}
			} else {
				final ImageView image = viewHolder.audioBtn;
				final ProgressBar pb = viewHolder.audioPb;
				viewHolder.contentTextView.setVisibility(View.GONE);
				viewHolder.picImageView.setVisibility(View.GONE);
				viewHolder.audioFrame.setVisibility(View.VISIBLE);
				viewHolder.audioBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						playVoice(temp.audioURL, image, pb);
					}
				});
			}

			if (temp.hasRootTalk) {
				viewHolder.rootLayout.setVisibility(View.VISIBLE);
				if (temp.rootTalk.talkType != 4) {
					viewHolder.rootAudioFrame.setVisibility(View.GONE);
					String rootContent = temp.rootTalk.content;
					if (rootContent != null) {
						SpannableString contentString = Txt2Image.text2Emotion(
								CommentDetailNewActivity.this, rootContent);
						viewHolder.rootTextView.setText(contentString);
					}
					if (temp.rootTalk.talkType == 1) {
						final String rootheadPicUrl = temp.rootTalk.contentURL;
						viewHolder.rootPicImageView.setTag(rootheadPicUrl);
						loadListImage(viewHolder.rootPicImageView,
								rootheadPicUrl);
						viewHolder.rootPicImageView.setVisibility(View.VISIBLE);
						viewHolder.rootPicImageView
								.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										zoomInPic(rootheadPicUrl);
									}
								});
					} else {
						viewHolder.rootPicImageView.setVisibility(View.GONE);
					}
				} else {
					final ImageView image = viewHolder.rootAudioBtn;
					final ProgressBar pb = viewHolder.rootAudioPb;
					viewHolder.rootTextView.setVisibility(View.GONE);
					viewHolder.rootPicImageView.setVisibility(View.GONE);
					viewHolder.rootAudioFrame.setVisibility(View.VISIBLE);
					viewHolder.rootAudioBtn
							.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									playVoice(temp.audioURL, image, pb);
								}
							});
				}
			} else {
				viewHolder.rootLayout.setVisibility(View.GONE);
			}

			return convertView;
		}

		private class ViewHolder {
			public TextView nameTextView;
			public ImageView headpicImageView;
			public TextView contentTextView;
			public ImageView picImageView;
			public RelativeLayout rootLayout;
			public TextView rootTextView;
			public ImageView rootPicImageView;
			// 语音评论框
			public RelativeLayout rootAudioFrame;
			// 语音评论按钮
			public ImageView rootAudioBtn;
			// 语音播放时等待
			public ProgressBar rootAudioPb;

			public TextView replayCountView;
			public TextView zhuanfaCountView;
			public TextView from;
			public TextView time;
			// 语音评论框
			public RelativeLayout audioFrame;
			// 语音评论按钮
			public ImageView audioBtn;
			// 语音播放时等待
			public ProgressBar audioPb;
		}
	}

	private void loadListImage(final ImageView imageView, String url) {
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
				imageView.setImageResource(R.drawable.list_headpic_default);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			setResult(RESULT_OK);
			finish();
			break;
		case R.id.reply_btn:
			executeAnimation(0);
			viewPager.setCurrentItem(0);
			break;
		case R.id.forward_btn:
			executeAnimation(1);
			viewPager.setCurrentItem(1);
			break;
		case R.id.send_forward_btn:
			openSendCommentForwardActivity();
			break;
		case R.id.send_reply_btn:
			openSendCommentActivity();
			break;
		case R.id.cd_audio_pic:
			playVoice(CommentData.current().audioURL, audioBtn,
					audioProgressBar);
			break;
		case R.id.root_audio_pic:
			playVoice(CommentData.current().rootTalk.audioURL, rootAudioBtn,
					rootAudioProgressBar);
			break;
		case R.id.my_comment_layout:
			try {
				if ((CommentData.current().objectId != 0)
						&& (!programId.equals(CommentData.current().objectId
								+ "")))
					openProgramActivity();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			break;
		case R.id.pic:
			zoomInPic(CommentData.current().contentURL);
			break;
		case R.id.bigpic:
			bigpic.startAnimation(zoomOut);
			bigpic.setVisibility(View.GONE);
			break;
		case R.id.root_pic:
			zoomInPic(CommentData.current().rootTalk.contentURL);
			break;
		default:
			break;
		}
	}

	private void zoomInPic(String url) {
		String temp = url.replace("s.jpg", "b.jpg");
		bigpic.setTag(temp);
		loadListImage(bigpic, temp);
		bigpic.setVisibility(View.VISIBLE);
		bigpic.startAnimation(zoomIn);
	}

	private void openProgramActivity() {
		Intent i = new Intent(this, ProgramNewActivity.class);
		i.putExtra("programId", CommentData.current().objectId + "");
		i.putExtra("topicId", CommentData.current().topicID + "");
		startActivity(i);
	}

	private String currentUrl;
	private MediaPlayer mediaPlayer;
	private ProgressBar currentProgressBar;
	private ImageView currentImageButton;

	private void playVoice(String voiceUrl, ImageView button,
			ProgressBar progressBar) {
		if (voiceUrl.equals(currentUrl)) {
			if (mediaPlayer != null) {
				if (mediaPlayer.isPlaying()) {
					mediaPlayer.stop();
				}
				mediaPlayer.release();
				progressBar.setVisibility(View.GONE);
				button.setImageResource(R.drawable.pc_switch2audio_big_normal);
				currentUrl = null;
				mediaPlayer = null;
			}
		} else {
			if (mediaPlayer != null) {
				mediaPlayer.stop();
				mediaPlayer.release();
				currentProgressBar.setVisibility(View.GONE);
				currentImageButton
						.setImageResource(R.drawable.pc_switch2audio_big_normal);
			}
			currentProgressBar = progressBar;
			currentImageButton = button;
			currentUrl = voiceUrl;
			mediaPlayer = new MediaPlayer();
			currentProgressBar.setVisibility(View.VISIBLE);

			try {
				mediaPlayer.setDataSource(voiceUrl);
				mediaPlayer.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						currentImageButton
								.setImageResource(R.drawable.pc_switch2audio_big_pressed);
						currentProgressBar.setVisibility(View.GONE);
						mediaPlayer.start();

					}
				});
				mediaPlayer.setOnErrorListener(new OnErrorListener() {
					@Override
					public boolean onError(MediaPlayer mp, int what, int extra) {
						Toast.makeText(CommentDetailNewActivity.this,
								"播放音频文件出错", Toast.LENGTH_SHORT).show();
						return false;
					}
				});
				mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						currentImageButton
								.setImageResource(R.drawable.pc_switch2audio_big_normal);
						currentProgressBar.setVisibility(View.GONE);
						mediaPlayer.release();
						mediaPlayer = null;
						currentUrl = null;
					}
				});
				mediaPlayer.prepareAsync();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void closeMediaPlayer() {
		if (mediaPlayer != null) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
			mediaPlayer.release();
			mediaPlayer = null;
		}
		if (currentProgressBar != null) {
			currentProgressBar.setVisibility(View.GONE);
			currentProgressBar = null;
		}
		if (currentImageButton != null) {
			currentImageButton
					.setImageResource(R.drawable.pc_switch2audio_big_normal);
			currentImageButton = null;
		}
		currentUrl = null;
	}

	@Override
	protected void onPause() {
		closeMediaPlayer();
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		CommentData.current().setForward(null);
		CommentData.current().setReply(null);
		super.onDestroy();
	}

	private void openSendCommentActivity() {
		Intent intent = new Intent(this, SendCommentActivity.class);
		intent.putExtra("fromWhere", SendCommentActivity.REPLY);
		startActivityForResult(intent, SendCommentActivity.REPLY);

	}

	private void openSendCommentForwardActivity() {
		CommentData.current().content = contentTextView.getText().toString();
		Intent intent = new Intent(this, SendCommentActivity.class);
		intent.putExtra("fromWhere", SendCommentActivity.FORWARD);
		startActivityForResult(intent, SendCommentActivity.FORWARD);

	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		executeAnimation(arg0);
		if (arg0 == 0) {
			if (replyList.size() == 0) {
				getReplyList();
			}
		} else if (arg0 == 1) {
			if (forwardList.size() == 0) {
				getForwradList();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SendCommentActivity.FORWARD:
				updateForwardView();
				break;
			case SendCommentActivity.REPLY:
				updateReplyView();
				break;
			default:
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onNetBegin(String method) {

	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("replyList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateReplyView();
			} else {
				replyProgressBar.setVisibility(View.GONE);
			}
			getReplyListTask = null;
		} else if ("forwardList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateForwardView();
			} else {
				forwardProgressBar.setVisibility(View.GONE);
			}
		} else if ("talkDetail".equals(method)) {
			if (msg != null && "".equals(msg)) {
				setMyCommentInfo();
				updateReplyView();
			} else {
				Toast.makeText(CommentDetailNewActivity.this, "网络繁忙，请稍后重试",
						Toast.LENGTH_SHORT).show();
			}
			commentDetailTask = null;
		}
	}

	private void updateForwardView() {
		ArrayList<CommentData> temp = (ArrayList<CommentData>) CommentData
				.current().getForward();
		if ((temp != null) && (temp.size() > 0)) {
			forwardList = temp;
			forwardErrTxt.setVisibility(View.GONE);
			CommentAdapter adapter = new CommentAdapter(forwardList);
			forwardListView.setAdapter(adapter);
		} else {
			forwardErrTxt.setVisibility(View.VISIBLE);
			if (CommentData.current().forwardCount > 0)
				forwardErrTxt.setText("加载失败，请重试");
			else
				forwardErrTxt.setText("还没有转发");

		}
		Resources res = getResources();
		String reply = res.getString(R.string.comment_zhuanfa);
		String replayCount = String.valueOf(CommentData.current().forwardCount);
		zhuanfaCountView.setText(reply + replayCount);
		forwardProgressBar.setVisibility(View.GONE);
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(RESULT_OK);
		}
		return super.onKeyDown(keyCode, event);
	}

}
