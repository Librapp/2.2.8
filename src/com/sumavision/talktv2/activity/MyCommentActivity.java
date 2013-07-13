package com.sumavision.talktv2.activity;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.MyListView.OnRefreshListener;
import com.sumavision.talktv2.data.CommentData;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.UserTalkListParser;
import com.sumavision.talktv2.net.UserTalkListRequest;
import com.sumavision.talktv2.task.GetUserTalkListTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.Txt2Image;
import com.umeng.analytics.MobclickAgent;

public class MyCommentActivity extends Activity implements OnClickListener,
		NetConnectionListener, OnRefreshListener, OnItemClickListener {

	private int userId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// userId = getIntent().getIntExtra("userId", 0);
		userId = UserNow.current().userID;
		setContentView(R.layout.my_comment);
		initOthers();
		initViews();
		setListeners();
		getMyCommentData();
	}

	private ImageLoaderHelper imageLoaderHelper;

	private void initOthers() {
		OtherCacheData.current().offset = 0;
		OtherCacheData.current().pageCount = 10;
		imageLoaderHelper = new ImageLoaderHelper();
	}

	private void setListeners() {
		errText.setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
		myCommentListView.setOnRefreshListener(this);
	}

	private TextView errText;
	private ProgressBar progressBar;
	private MyListView myCommentListView;
	private ArrayList<CommentData> list = new ArrayList<CommentData>();

	private void initViews() {
		errText = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		myCommentListView = (MyListView) findViewById(R.id.listView);
		myCommentListView.setOnItemClickListener(this);
	}

	private GetUserTalkListTask getUserTalkListTask;

	private void getMyCommentData() {
		if (getUserTalkListTask == null) {
			getUserTalkListTask = new GetUserTalkListTask(this);
			getUserTalkListTask.execute(this, new UserTalkListRequest(userId),
					new UserTalkListParser());
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private void getMyCommentData(int start, int count) {
		if (getUserTalkListTask == null) {
			OtherCacheData.current().offset = start;
			OtherCacheData.current().pageCount = count;
			getUserTalkListTask = new GetUserTalkListTask(this);
			getUserTalkListTask.execute(this, new UserTalkListRequest(userId),
					new UserTalkListParser());
			if (list.size() == 0) {
				errText.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}
	}

	private CommentAdapter adapter;

	private void updateUI() {
		ArrayList<CommentData> temp = (ArrayList<CommentData>) UserNow
				.current().getOwnComments();
		if (temp != null) {
			list = temp;
			if (list.size() == 0) {
				errText.setText("您还没发表过评论");
				errText.setVisibility(View.VISIBLE);
			} else {
				errText.setVisibility(View.GONE);
				adapter = new CommentAdapter(list);
				myCommentListView.setAdapter(adapter);
			}
		} else {
			errText.setVisibility(View.VISIBLE);
		}
		progressBar.setVisibility(View.GONE);
	}

	@Override
	public void onRefresh() {
		getMyCommentData();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.err_text:
			getMyCommentData();
			break;
		case R.id.back:
			close();
			break;
		default:
			break;
		}
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
			long start = System.currentTimeMillis();
			if (convertView == null) {
				viewHolder = new ViewHolder();
				LayoutInflater inflater = LayoutInflater
						.from(MyCommentActivity.this);
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
				imageLoaderHelper.loadImage(viewHolder.headpicImageView,
						userHeadPic, R.drawable.list_headpic_default);
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
							MyCommentActivity.this, content);
					viewHolder.contentTextView.setText(contentString);
				}

				if (temp.talkType == 1) {
					String picUrl = temp.contentURL;
					imageLoaderHelper.loadImage(viewHolder.picImageView,
							picUrl, R.drawable.rcmd_list_item_pic_default);
					viewHolder.picImageView.setVisibility(View.VISIBLE);
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
								MyCommentActivity.this, rootContent);
						viewHolder.rootTextView.setText(contentString);
					}
					if (temp.rootTalk.talkType == 1) {
						String rootheadPicUrl = temp.rootTalk.contentURL;
						imageLoaderHelper.loadImage(
								viewHolder.rootPicImageView, rootheadPicUrl,
								R.drawable.list_headpic_default);
						viewHolder.rootPicImageView.setVisibility(View.VISIBLE);
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

			long end = System.currentTimeMillis();
			Log.e(TAG, "duration=" + (end - start));
			return convertView;
		}
	}

	static class ViewHolder {
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
						Toast.makeText(MyCommentActivity.this, "播放失败",
								Toast.LENGTH_SHORT).show();
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

	@Override
	public void onNetBegin(String method) {
	}

	@Override
	public void onNetEnd(String msg, String method) {
		if ("userTalkList".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateUI();
			} else {
				progressBar.setVisibility(View.GONE);
				if (list.size() == 0) {
					errText.setVisibility(View.VISIBLE);
				}
				myCommentListView.onLoadError();
			}
			getUserTalkListTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {
		getUserTalkListTask = null;
		Log.e(TAG, "onCancel callback");
	}

	private void close() {
		if (getUserTalkListTask != null) {
			getUserTalkListTask.cancel(true);
			Log.e(TAG, " call cancel");
		}
		finish();
	}

	@Override
	public void onLoadingMore() {
		// TODO
		int start = 0;
		int count = list.size() + 10;
		getMyCommentData(start, count);
	}

	private static final String TAG = "MyCommentActivity";

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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position != 0) {
			if (position - 1 == list.size() || position - 1 < 0)
				return;
			int mPosistion = position - 1;
			CommentData.current().talkId = list.get(mPosistion).talkId;
			openCommentDetailActivity();
		}
	}

	private void openCommentDetailActivity() {
		Intent i = new Intent(this, CommentDetailNewActivity.class);
		startActivity(i);
	}
}
