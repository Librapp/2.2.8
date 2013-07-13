package com.sumavision.talktv2.activity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.AccessTokenKeeper;
import com.sumavision.talktv2.data.CommentData;
import com.sumavision.talktv2.data.EmotionData;
import com.sumavision.talktv2.data.MakeEmotionsList;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.ShareData;
import com.sumavision.talktv2.data.SinaData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.ReplyAddParser;
import com.sumavision.talktv2.net.ReplyAddRequest;
import com.sumavision.talktv2.net.ResultParser;
import com.sumavision.talktv2.net.TalkAddRequest;
import com.sumavision.talktv2.net.TalkForwardParser;
import com.sumavision.talktv2.net.TalkForwardRequest;
import com.sumavision.talktv2.task.SendCommentTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.utils.AudioFileRecorderUtils;
import com.sumavision.talktv2.utils.Base64;
import com.sumavision.talktv2.utils.BitmapUtils;
import com.sumavision.talktv2.utils.DialogUtil;
import com.sumavision.talktv2.utils.FileInfoUtils;
import com.sumavision.talktv2.utils.InfomationHelper;
import com.sumavision.talktv2.utils.MediaInfoUtils;
import com.sumavision.talktv2.utils.PicUtils;
import com.sumavision.talktv2.utils.Txt2Image;
import com.umeng.analytics.MobclickAgent;
import com.weibo.sdk.android.Oauth2AccessToken;
import com.weibo.sdk.android.WeiboAuthListener;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;
import com.weibo.sdk.android.api.StatusesAPI;
import com.weibo.sdk.android.net.RequestListener;
import com.weibo.sdk.android.sso.SsoHandler;

/**
 * @author 李梦思
 * @version 2.2
 * @createTime 2013-1-10
 * @description 发表评论
 * @changeLog
 */
public class SendCommentActivity extends Activity implements OnClickListener,
		NetConnectionListener {
	private final int GETIMAGE_BYSDCARD = 15;
	private final int GETIMAGE_BYCAMERA = 16;
	private final int SHOWIMG = 14;
	private final int LOGIN = 13;

	private final int SINA = 1;
	private final int SINA_AUTH_ERROR = 1;
	private final int SINA_AUTH_CANCEL = 2;
	private final int SINA_UPLOAD_OK = 3;
	private final int SINA_UPLOAD_ERROR = 4;

	private int flag = 0;
	private EditText input;
	private TextView txtNumber;
	private ImageView choosedPic;
	private ImageView bg;
	private ImageButton sync;
	private TextView sync_txt;
	private LinearLayout txtCount;
	private RelativeLayout browLayout;
	private RelativeLayout phraseLayout;
	private RelativeLayout picFrame;
	private RelativeLayout loginLayout;
	private List<EmotionData> ids;
	private EmotionImageAdapter eia;
	private PhrasesListAdapter pla;
	private GridView emotionGrid;
	private ListView phraseList;
	private TextView title;

	private Animation a;
	private String thisLarge = null;
	private PicUtils pu;
	private Bitmap result;

	private SsoHandler ssh;

	private boolean isSync = false;
	private boolean hasEmotions = false;
	private boolean hasPhraseList = false;

	public static final int NORMAL = 0;
	public static final int REPLY = 1;
	public static final int FORWARD = 2;
	public static final int SCREENSHOT = 3;
	// 视频页分享到微博
	public static final int SHARE2SINA = 4;
	private int fromWhere = NORMAL;

	// 通信框
	private RelativeLayout connectBg;

	private void hidepb() {
		connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		connectBg.setVisibility(View.VISIBLE);
	}

	// 语音输入提示框
	private ImageView p2tDialog;
	// 语音输入提示框背景
	private ImageView p2tDialogBg;
	private boolean hasSDCard;
	private AudioFileRecorderUtils afru;
	private boolean hasRecord;
	// 语音功能输入框
	private RelativeLayout BottomPress2Talk;
	// 普通功能输入框
	private LinearLayout bottomFunc;
	// 语音输入框关闭按钮
	private ImageButton closeAudioInput;
	// 按住说话按钮
	private ImageButton press2Record;
	private Button audioBtn;

	// 跳过来时的节目信息
	private String programName;
	private String programId;
	private String topicId;

	private void initPress2Talk() {
		press2Record = (ImageButton) findViewById(R.id.audio_input);
		press2Record.setOnClickListener(this);
		closeAudioInput = (ImageButton) findViewById(R.id.pdn_c_input_audioinput);
		closeAudioInput.setOnClickListener(this);
		p2tDialog = (ImageView) findViewById(R.id.program_p2t);
		p2tDialogBg = (ImageView) findViewById(R.id.pdn_c_bg_all);
		p2tDialogBg.setClickable(true);
		p2tDialogBg.setOnClickListener(null);
		BottomPress2Talk = (RelativeLayout) findViewById(R.id.pdn_c_relative_buttom_p2t);
		bottomFunc = (LinearLayout) findViewById(R.id.btn_layout_normal);
		audioBtn = (Button) findViewById(R.id.pdn_c_input_p2t);
		audioBtn.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

				if (hasSDCard) {
					hasRecord = true;
					p2tDialog.setVisibility(View.VISIBLE);
					CommentData.current().audioFileName = UserNow.current().userID
							+ "-" + System.currentTimeMillis();
					try {
						afru = new AudioFileRecorderUtils(
								CommentData.current().audioFileName);
						afru.openRec();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
						Toast.makeText(getApplicationContext(), "请重试!",
								Toast.LENGTH_SHORT).show();
						p2tDialog.setVisibility(View.GONE);
					}
				} else {
					Toast.makeText(getApplicationContext(), "SDCard不存在，请插入后重试",
							Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		});
		audioBtn.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					if (hasSDCard) {
						if (!hasRecord) {
							Toast.makeText(getApplicationContext(),
									"录音时间过短，请重试!", Toast.LENGTH_SHORT).show();
						} else {
							hasRecord = false;
							try {
								if (afru != null) {
									afru.closeRec();
									afru = null;
								}
								getMP3Duration();
								getAudiFileBytes();
								CommentData.current().content = input.getText()
										.toString().trim();
								switch (fromWhere) {
								case NORMAL:
									sendComment();
									break;
								case REPLY:
									sendReply();
									break;
								default:
									break;
								}
							} catch (RuntimeException e) {
								// TODO: handle exception
								e.printStackTrace();
								Toast.makeText(getApplicationContext(),
										"录音时间过短，请重试!", Toast.LENGTH_SHORT)
										.show();

								if (p2tDialog.isShown()) {
									p2tDialog.setVisibility(View.GONE);
								}
							} catch (IOException e) {
								e.printStackTrace();
							}

							if (p2tDialog.isShown()) {
								p2tDialog.setVisibility(View.GONE);
							}
						}
					} else {
						Toast.makeText(getApplicationContext(),
								"SDCard不存在，请插入后重试", Toast.LENGTH_SHORT).show();
					}
					break;
				}
				return false;
			}
		});
	}

	private int getMP3Duration() {
		MediaPlayer m = new MediaPlayer();
		try {
			m.setDataSource(JSONMessageType.AUDIO_SDCARD_FOLDER
					+ CommentData.current().audioFileName
					+ JSONMessageType.MP3_FILE_EXTENTION);
			m.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.e("getMP3Duration", m.getDuration() + "");
		return m.getDuration();
	}

	// 声音文件转化为字节码
	private void getAudiFileBytes() {

		File f = new File(JSONMessageType.AUDIO_SDCARD_FOLDER
				+ CommentData.current().audioFileName
				+ JSONMessageType.MP3_FILE_EXTENTION);
		byte[] b = null;
		try {
			b = FileInfoUtils.getByteFromFile(f);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (b != null) {
			CommentData.current().audio = Base64.encode(b, 0, b.length);
		}
		Log.e("getAudiFileBytes", CommentData.current().audio);
	}

	private void checkSDCard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			hasSDCard = true;
		} else {
			hasSDCard = false;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment_send);
		fromWhere = getIntent().getIntExtra("fromWhere", NORMAL);
		getIntentData();
		initPress2Talk();
		initViews();
		checkSDCard();
	}

	private void initViews() {
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.commit).setOnClickListener(this);
		findViewById(R.id.audio_input).setOnClickListener(this);
		findViewById(R.id.emotion).setOnClickListener(this);
		findViewById(R.id.photo).setOnClickListener(this);
		findViewById(R.id.duanyu).setOnClickListener(this);
		txtCount = (LinearLayout) findViewById(R.id.pdn_c_layout_count);
		txtCount.setOnClickListener(this);

		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);
		sync = (ImageButton) findViewById(R.id.sync);
		sync_txt = (TextView) findViewById(R.id.sync_txt);
		txtNumber = (TextView) findViewById(R.id.text_number);
		input = (EditText) findViewById(R.id.content_text);
		input.setOnClickListener(this);
		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (input.getText().length() > 0) {
					txtCount.setVisibility(View.VISIBLE);
					if (fromWhere == SHARE2SINA)
						txtNumber.setText(input.getText().length() + "/50");
					else
						txtNumber.setText(input.getText().length() + "/110");
				} else {
					txtCount.setVisibility(View.GONE);
				}
			}
		});

		picFrame = (RelativeLayout) findViewById(R.id.sendcomment_layout_pic);
		picFrame.setOnClickListener(this);
		choosedPic = (ImageView) findViewById(R.id.sendcomment_choosed_pic);
		choosedPic.setOnClickListener(this);
		picFrame.findViewById(R.id.sendcomment_chacha_pic).setOnClickListener(
				this);
		picFrame.setOnClickListener(this);

		title = (TextView) findViewById(R.id.sc_title);

		phraseLayout = (RelativeLayout) findViewById(R.id.pdn_c_relative_phrase);
		browLayout = (RelativeLayout) findViewById(R.id.pdn_c_relative_emotion);
		emotionGrid = (GridView) findViewById(R.id.pdn_c_grid_emotion);
		phraseList = (ListView) findViewById(R.id.pdn_c_list_phrase);
		a = AnimationUtils.loadAnimation(this, R.anim.leftright);
		ids = MakeEmotionsList.current().getLe();
		eia = new EmotionImageAdapter(this, ids);
		emotionGrid.setAdapter(eia);
		emotionGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int cursor = input.getSelectionStart();
				EmotionData e = ids.get(arg2);
				input.getText().insert(
						cursor,
						Txt2Image.txtToImg(e.getPhrase(),
								SendCommentActivity.this));
				Log.e("ProgramComment", e.getPhrase());
			}
		});

		makePhraseDate();
		pla = new PhrasesListAdapter(this, CommentData.current().getPhrases());

		phraseList.setAdapter(pla);
		phraseList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int cursor = input.getSelectionStart();
				input.getText().insert(cursor,
						CommentData.current().getPhrases().get(arg2));
			}
		});

		pu = new PicUtils(this, this);

		switch (fromWhere) {
		case NORMAL:
			sync.setVisibility(View.VISIBLE);
			sync_txt.setVisibility(View.VISIBLE);
			sync.setOnClickListener(this);
			CommentData.current().pic = "";
			picSet(false);
			break;
		case FORWARD:
			sync.setVisibility(View.GONE);
			sync_txt.setVisibility(View.GONE);
			press2Record.setVisibility(View.GONE);
			if (CommentData.current().hasRootTalk)
				input.setText("//@" + CommentData.current().userName + ":"
						+ CommentData.current().content);
			input.setSelection(0);
			CommentData.current().pic = "";
			picSet(false);
			break;
		case REPLY:
			press2Record.setVisibility(View.VISIBLE);
			sync.setVisibility(View.GONE);
			sync_txt.setVisibility(View.GONE);
			CommentData.current().pic = "";
			picSet(false);
			// input.setText("回复 " + CommentData.current().userName + ":");
			break;
		case SCREENSHOT:
			sync.setVisibility(View.VISIBLE);
			sync_txt.setVisibility(View.VISIBLE);
			sync.setOnClickListener(this);
			Bitmap bitmap = InfomationHelper.getScaleBitmap(
					SendCommentActivity.this, UserNow.current().picPath);
			result = InfomationHelper.getFinalScaleBitmapBigPic(this,
					UserNow.current().picPath);

			String s1 = new String(com.sumavision.talktv2.utils.Base64.encode(
					BitmapUtils.bitmapToBytes(result), 0,
					BitmapUtils.bitmapToBytes(result).length));
			CommentData.current().pic = s1;
			CommentData.current().picBitMap = result;
			CommentData.current().picAllName = UserNow.current().picPath;
			CommentData.current().setPicLogo(BitmapUtils.bitmapToBytes(result));
			if (bitmap != null) {
				choosedPic.setImageDrawable(new BitmapDrawable(bitmap));
			}
			picSet(true);
			fromWhere = NORMAL;
			break;
		case SHARE2SINA:
			press2Record.setVisibility(View.GONE);
			if (sinaPic != null) {
				CommentData.current().picAllName = sinaPic;
			} else {
				CommentData.current().picAllName = null;
			}
			picSet(false);
			title.setText("分享到新浪微博");
			sync.setImageResource(R.drawable.sina_selected);
			input.setText("真是太精彩了 !");
			break;
		default:
			break;
		}

		loginLayout = (RelativeLayout) findViewById(R.id.pdn_c_login_layout);
		findViewById(R.id.pdn_c_login_btn).setOnClickListener(this);
		findViewById(R.id.pdn_c_send_btn).setOnClickListener(this);
		bg = (ImageView) findViewById(R.id.pdn_c_bg);
		bg.setOnClickListener(this);

	}

	private void makePhraseDate() {
		List<String> ls = new ArrayList<String>();
		for (int i = 0; i < 7; i++) {
			switch (i) {
			case 0:
				ls.add("这个给力啊!");
				break;
			case 1:
				ls.add("超级给力啊!");
				break;
			case 2:
				ls.add("天天向上，有木有!");
				break;
			case 3:
				ls.add("囧rz");
				break;
			case 4:
				ls.add("这个必须顶!");
				break;
			case 5:
				ls.add("这个真可以有!");
				break;
			case 6:
				ls.add("顶顶更健康!");
				break;
			case 7:
				ls.add("签个到，冒个泡!");
				break;
			default:
				break;
			}
		}
		CommentData.current().setPhrases(ls);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pdn_c_input_p2t:

			break;
		case R.id.pdn_c_input_audioinput:
			if (BottomPress2Talk.isShown()) {
				BottomPress2Talk.setVisibility(View.GONE);
				bottomFunc.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.audio_input:
			if (!BottomPress2Talk.isShown()) {
				BottomPress2Talk.setVisibility(View.VISIBLE);
				bottomFunc.setVisibility(View.GONE);
			}
			break;
		case R.id.back:
			MobclickAgent.onEvent(this, "commentback");
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.commit:
			MobclickAgent.onEvent(this, "fabiao");
			if (input.getText().toString().trim().length() <= 0) {
				Toast.makeText(this, "请先说点什么...", Toast.LENGTH_SHORT).show();
				input.startAnimation(a);
			} else {

				if (fromWhere == SHARE2SINA) {
					String share2sinaTxt = "";
					if (programName.equals(""))
						share2sinaTxt = "快来看，"
								+ input.getText().toString().trim()
								+ "，观看地址>>>" + ShareData.shareWeiboText
								+ " (来自@电视粉)" + "\n";
					else
						share2sinaTxt = "快来看" + "#" + programName + "#，"
								+ input.getText().toString().trim()
								+ "，观看地址>>>" + ShareData.shareWeiboText
								+ " (来自@电视粉)" + "\n";

					SinaData.content = share2sinaTxt;
				} else
					SinaData.content = "#我在电视粉评论#" + "《" + programName + "》："
							+ input.getText().toString().trim() + "(来自@电视粉)"
							+ "\n";

				hasEmotions = false;
				browLayout.setVisibility(View.GONE);
				hideSoftPad();
				phraseLayout.setVisibility(View.GONE);
				hasPhraseList = false;
				CommentData.current().content = input.getText().toString()
						.trim();
				CommentData.replyComment().content = input.getText().toString()
						.trim();

				int nowSize = input.getText().toString().trim().length();
				if (nowSize > 110) {
					nowSize -= 110;
					Toast.makeText(getApplicationContext(),
							"请删除" + nowSize + "个字后重试", Toast.LENGTH_SHORT)
							.show();
				} else if (fromWhere == SHARE2SINA && nowSize > 50) {
					nowSize -= 50;
					Toast.makeText(getApplicationContext(),
							"请删除" + nowSize + "个字后重试", Toast.LENGTH_SHORT)
							.show();
				} else

				if (UserNow.current().userID != 0) {
					switch (fromWhere) {
					case NORMAL:
						sendComment();
						break;
					case REPLY:
						sendReply();
						break;
					case FORWARD:
						sendForward();
						break;
					case SHARE2SINA:
						if (!isSync)
							getSinaAuth();
						else
							precessWeibo();
						break;
					default:
						break;
					}
				} else {
					switch (fromWhere) {
					case NORMAL:
						loginLayout.setVisibility(View.VISIBLE);
						break;
					case REPLY:
						loginLayout.setVisibility(View.VISIBLE);
						break;
					case FORWARD:
						loginLayout.setVisibility(View.VISIBLE);
						break;
					case SHARE2SINA:
						if (!isSync)
							getSinaAuth();
						else
							precessWeibo();
						break;
					default:
						break;
					}
				}
			}
			break;
		case R.id.content_text:
			hasEmotions = false;
			browLayout.setVisibility(View.GONE);
			phraseLayout.setVisibility(View.GONE);
			hasPhraseList = false;
			break;
		case R.id.pdn_c_layout_count:
			cleanText();
			break;
		case R.id.emotion:
			MobclickAgent.onEvent(this, "biaoqing");
			hideSoftPad();
			phraseLayout.setVisibility(View.GONE);
			hasPhraseList = false;
			if (!hasEmotions) {
				browLayout.setVisibility(View.VISIBLE);
				hasEmotions = true;
			} else {
				hasEmotions = false;
				browLayout.setVisibility(View.GONE);
			}
			break;
		case R.id.photo:
			MobclickAgent.onEvent(this, "pic");
			CharSequence[] items = { "相册", "拍照" };
			ChooseImage(items);
			break;
		case R.id.duanyu:
			MobclickAgent.onEvent(this, "changyong");
			hideSoftPad();
			hasEmotions = false;
			browLayout.setVisibility(View.GONE);
			if (!hasPhraseList) {
				phraseLayout.setVisibility(View.VISIBLE);
				hasPhraseList = true;
			} else {
				hasPhraseList = false;
				phraseLayout.setVisibility(View.GONE);
			}
			break;
		case R.id.sync:
			MobclickAgent.onEvent(this, "sinaweibo");
			if (isSync) {
				sync.setImageResource(R.drawable.sina);
				isSync = false;
			} else {
				if (SinaData.isSinaBind) {
					isSync = true;
					sync.setImageResource(R.drawable.sina_selected);
				} else {
					getSinaAuth();
				}
			}
			break;
		case R.id.sendcomment_choosed_pic:
		case R.id.sendcomment_layout_pic:
			cleanPic();
			break;
		case R.id.pdn_c_bg:
			bg.setVisibility(View.GONE);
			loginLayout.setVisibility(View.GONE);
			break;
		case R.id.pdn_c_login_btn:
			bg.setVisibility(View.GONE);
			loginLayout.setVisibility(View.GONE);
			Intent i = new Intent(this, LoginActivity.class);
			startActivityForResult(i, LOGIN);
			break;
		case R.id.pdn_c_send_btn:
			MobclickAgent.onEvent(this, "fabiao");
			bg.setVisibility(View.GONE);
			loginLayout.setVisibility(View.GONE);
			switch (fromWhere) {
			case NORMAL:
				sendComment();
				break;
			case REPLY:
				sendReply();
				break;
			case FORWARD:
				sendForward();
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}

	private void getSinaAuth() {
		flag = SINA;
		ssh = new SsoHandler(this, SinaData.weibo());

		ssh.authorize(new WeiboAuthListener() {
			@Override
			public void onWeiboException(WeiboException arg0) {
				Message msg = new Message();
				msg.what = SINA;
				msg.arg1 = SINA_AUTH_ERROR;
				msg.obj = "新浪微博授权失败";
				if (OtherCacheData.current().isDebugMode)
					Log.e("新浪微博授权", arg0.getMessage());
				handler.sendMessage(msg);
			}

			@Override
			public void onError(WeiboDialogError arg0) {
				Message msg = new Message();
				msg.what = SINA;
				msg.arg1 = SINA_AUTH_ERROR;
				msg.obj = "新浪微博授权失败";
				if (OtherCacheData.current().isDebugMode)
					Log.e("新浪微博授权", arg0.getMessage());
				handler.sendMessage(msg);
			}

			@Override
			public void onComplete(Bundle arg0) {
				SinaData.accessToken = arg0.getString("access_token");
				SinaData.expires_in = arg0.getString("expires_in");
				SinaData.weibo().accessToken = new Oauth2AccessToken(
						SinaData.accessToken, SinaData.expires_in);
				AccessTokenKeeper.keepAccessToken(SendCommentActivity.this,
						SinaData.weibo().accessToken);
				isSync = true;
				sync.setImageResource(R.drawable.sina_selected);
				if (fromWhere == SHARE2SINA)
					precessWeibo();
			}

			@Override
			public void onCancel() {
				Message msg = new Message();
				msg.what = SINA;
				msg.arg1 = SINA_AUTH_CANCEL;
				msg.obj = "sina授权取消";
				handler.sendMessage(msg);
			}
		});
	}

	private void sendForward() {
		SendCommentTask sendCommentTask = new SendCommentTask(this);
		sendCommentTask.execute(this, new TalkForwardRequest(),
				new TalkForwardParser());

		precessWeibo();
	}

	private void sendReply() {
		SendCommentTask sendCommentTask = new SendCommentTask(this);
		sendCommentTask.execute(this, new ReplyAddRequest(),
				new ReplyAddParser());
	}

	private void sendComment() {
		if (CommentData.current().audio == null) {
			if (CommentData.current().content.equals("")) {
				Toast.makeText(getApplicationContext(), "请先说点什么...",
						Toast.LENGTH_SHORT).show();
			} else {
				SendCommentTask sendCommentTask = new SendCommentTask(this);
				sendCommentTask.execute(this, new TalkAddRequest(topicId),
						new ResultParser());

				precessWeibo();
			}
		} else {
			SendCommentTask sendCommentTask = new SendCommentTask(this);
			sendCommentTask.execute(this, new TalkAddRequest(topicId),
					new ResultParser());
		}
	}

	private void precessWeibo() {
		if (isSync) {
			StatusesAPI statusesAPI = new StatusesAPI(
					SinaData.weibo().accessToken);
			if (sinaPic != null) {
				CommentData.current().picAllName = sinaPic;
			}
			if (null == CommentData.current().picAllName)
				statusesAPI.update(SinaData.content, null, null,
						new RequestListener() {

							@Override
							public void onIOException(IOException arg0) {
								Message msg = new Message();
								msg.what = SINA;
								msg.arg1 = SINA_UPLOAD_ERROR;
								msg.obj = "新浪微博发表失败";
								if (OtherCacheData.current().isDebugMode)
									Log.e("新浪微博发表", arg0.getMessage());
								handler.sendMessage(msg);
							}

							@Override
							public void onError(WeiboException arg0) {
								Message msg = new Message();
								msg.what = SINA;
								msg.arg1 = SINA_UPLOAD_ERROR;
								msg.obj = "新浪微博发表失败";
								if (OtherCacheData.current().isDebugMode)
									Log.e("新浪微博发表", arg0.getMessage());
								handler.sendMessage(msg);
							}

							@Override
							public void onComplete(String arg0) {
								Message msg = new Message();
								msg.what = SINA;
								msg.arg1 = SINA_UPLOAD_OK;
								msg.obj = "新浪微博发表成功";
								handler.sendMessage(msg);
							}
						});
			else {
				SinaData.pic = CommentData.current().picAllName;
				statusesAPI.upload(SinaData.content, SinaData.pic, null, null,
						new RequestListener() {

							@Override
							public void onIOException(IOException arg0) {
								Message msg = new Message();
								msg.what = SINA;
								msg.arg1 = SINA_UPLOAD_ERROR;
								msg.obj = "新浪微博发表失败";
								if (OtherCacheData.current().isDebugMode)
									Log.e("新浪微博发表", arg0.getMessage());
								handler.sendMessage(msg);
							}

							@Override
							public void onError(WeiboException arg0) {
								Message msg = new Message();
								msg.what = SINA;
								msg.arg1 = SINA_UPLOAD_ERROR;
								msg.obj = "新浪微博发表失败";
								if (OtherCacheData.current().isDebugMode)
									Log.e("新浪微博发表", arg0.getMessage());
								handler.sendMessage(msg);
							}

							@Override
							public void onComplete(String arg0) {
								Message msg = new Message();
								msg.what = SINA;
								msg.arg1 = SINA_UPLOAD_OK;
								msg.obj = "新浪微博发表成功";
								SinaData.pic = null;
								CommentData.current().picAllName = null;
								handler.sendMessage(msg);
							}
						});
			}
		}
	}

	private final Handler handler = new Handler(new Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case SINA:
				Toast.makeText(SendCommentActivity.this, msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				if (msg.arg1 == SINA_UPLOAD_OK)
					finish();
				// switch (msg.arg1) {
				// case SINA_AUTH_CANCEL:
				// Toast.makeText(SendCommentActivity.this,
				// msg.obj.toString(), Toast.LENGTH_SHORT).show();
				// break;
				// case SINA_AUTH_ERROR:
				// Toast.makeText(SendCommentActivity.this,
				// msg.obj.toString(), Toast.LENGTH_SHORT).show();
				// break;
				// case SINA_UPLOAD_CANCEL:
				// Toast.makeText(SendCommentActivity.this,
				// msg.obj.toString(), Toast.LENGTH_SHORT).show();
				// break;
				// case SINA_UPLOAD_OK:
				// Toast.makeText(SendCommentActivity.this,
				// msg.obj.toString(), Toast.LENGTH_SHORT).show();
				// break;
				// case SINA_UPLOAD_ERROR:
				// Toast.makeText(SendCommentActivity.this,
				// msg.obj.toString(), Toast.LENGTH_SHORT).show();
				// break;
				// default:
				// break;
				// }
				break;
			default:
				break;
			}
			return false;
		}
	});

	public void ChooseImage(CharSequence[] items) {
		AlertDialog imageDialog = new AlertDialog.Builder(
				SendCommentActivity.this).setTitle("选择图片")
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {
						if (item == 0) {
							Intent intent = new Intent(
									Intent.ACTION_GET_CONTENT);
							intent.setType("image/*");
							startActivityForResult(intent, GETIMAGE_BYSDCARD);
						} else if (item == 1) {
							Intent intent = new Intent(
									"android.media.action.IMAGE_CAPTURE");

							String camerName = InfomationHelper.getFileName();
							String fileName = "TalkTV" + camerName + ".jpg";

							File fileDir = new File(
									JSONMessageType.USER_PIC_SDCARD_FOLDER);
							if (!fileDir.exists()) {
								fileDir.mkdir();
							}

							File camerFile = new File(
									JSONMessageType.USER_PIC_SDCARD_FOLDER,
									fileName);
							UserNow.current().picPath = camerFile
									.getAbsolutePath();
							Uri originalUri = Uri.fromFile(camerFile);
							intent.putExtra(MediaStore.EXTRA_OUTPUT,
									originalUri);
							startActivityForResult(intent, GETIMAGE_BYCAMERA);
						}
					}
				}).create();
		imageDialog.show();
	}

	private void cleanText() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("清空所有已输入的内容么？");
		builder.setTitle("提示");
		// 添加按钮
		builder.setIcon(R.drawable.icon);
		builder.setPositiveButton("确定",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						input.setText("");
					}
				});
		builder.setNegativeButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private void cleanPic() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle("请选择");
		// 添加按钮
		builder.setIcon(R.drawable.icon);
		builder.setPositiveButton("清空图片",
				new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						picSet(false);
					}
				});
		builder.setNegativeButton("取消",
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		builder.create().show();
	}

	private void picSet(boolean hasPic) {
		if (!hasPic) {
			choosedPic.setVisibility(View.GONE);
			picFrame.setVisibility(View.GONE);
			CommentData.current().pic = "";
			CommentData.current().picBitMap = null;
			CommentData.current().picAllName = null;
		} else {
			choosedPic.setVisibility(View.VISIBLE);
			choosedPic.setAlpha(130);
			picFrame.setVisibility(View.VISIBLE);
		}
	}

	private void hideSoftPad() {
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(SendCommentActivity.this
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void onNetBegin(String method) {
		showpb();
	}

	@Override
	public void onNetEnd(String msg, String method) {
		hidepb();
		if (null == msg || msg.equals("")) {
			Toast.makeText(this, "发表成功", Toast.LENGTH_SHORT).show();
			CommentData.current().content = "";
			CommentData.current().pic = "";
			CommentData.current().audio = null;
			CommentData.current().picAllName = null;
			setResult(RESULT_OK);
			finish();
		} else {

			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
		}
		if (UserNow.current().getNewBadge() != null) {
			for (int i = 0; i < UserNow.current().getNewBadge().size(); i++) {
				String name = UserNow.current().getNewBadge().get(i).name;
				if (name != null) {
					DialogUtil
							.showBadgeAddToast(SendCommentActivity.this, name);
				}
			}
			UserNow.current().setNewBadge(null);
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCancel(String method) {
		Toast.makeText(this, "取消", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (flag == SINA) {
			flag = 0;
			if (ssh != null) {
				ssh.authorizeCallBack(requestCode, resultCode, data);
			}
		} else {
			if (resultCode == RESULT_OK) {
				switch (requestCode) {
				case LOGIN:
					switch (fromWhere) {
					case NORMAL:
						sendComment();
						break;
					case REPLY:
						sendReply();
						break;
					case FORWARD:
						sendForward();
						break;
					default:
						break;
					}
					break;
				case SHOWIMG:
					result = UserNow.current().tempBitmap;
					String str = new String(
							com.sumavision.talktv2.utils.Base64.encode(
									BitmapUtils.bitmapToBytes(result), 0,
									BitmapUtils.bitmapToBytes(result).length));
					CommentData.current().pic = str;
					CommentData.current().picBitMap = result;
					break;
				case GETIMAGE_BYCAMERA:
					picSet(true);
					super.onActivityResult(requestCode, resultCode, data);

					Bitmap bitmap = InfomationHelper
							.getScaleBitmap(SendCommentActivity.this,
									UserNow.current().picPath);
					result = InfomationHelper.getFinalScaleBitmapBigPic(this,
							UserNow.current().picPath);

					String s1 = new String(
							com.sumavision.talktv2.utils.Base64.encode(
									BitmapUtils.bitmapToBytes(result), 0,
									BitmapUtils.bitmapToBytes(result).length));
					CommentData.current().pic = s1;
					CommentData.current().picBitMap = result;
					CommentData.current().picAllName = UserNow.current().picPath;
					CommentData.current().setPicLogo(
							BitmapUtils.bitmapToBytes(result));
					if (bitmap != null) {
						choosedPic.setImageDrawable(new BitmapDrawable(bitmap));
					}
					break;
				case GETIMAGE_BYSDCARD:
					picSet(true);
					if (data == null)
						return;
					Uri thisUri = data.getData();
					String thePath = InfomationHelper
							.getAbsolutePathFromNoStandardUri(thisUri);
					// 如果是标准Uri
					if (com.sumavision.talktv2.utils.StringUtils
							.isBlank(thePath)) {
						thisLarge = pu.getAbsoluteImagePath(thisUri);
					} else {
						thisLarge = thePath;
					}

					String attFormat = FileInfoUtils.getFileFormat(thisLarge);
					if (!"photo".equals(MediaInfoUtils
							.getContentType(attFormat))) {
						Toast.makeText(SendCommentActivity.this, "请选择图片文件！",
								Toast.LENGTH_SHORT).show();
						return;
					}
					String imgName = FileInfoUtils.getFileName(thisLarge);

					if (OtherCacheData.current().isDebugMode) {
						Log.e("PDNComment", imgName);
						Log.e("PDNComment", thisLarge);
					}
					result = InfomationHelper.getFinalScaleBitmapBigPic(this,
							thisLarge);

					String s = new String(
							com.sumavision.talktv2.utils.Base64.encode(
									BitmapUtils.bitmapToBytes(result), 0,
									BitmapUtils.bitmapToBytes(result).length));

					CommentData.current().pic = s;
					CommentData.current().picBitMap = result;
					CommentData.current().picAllName = thisLarge;

					CommentData.current().setPicLogo(
							BitmapUtils.bitmapToBytes(result));

					Bitmap b = pu.loadImgThumbnail(imgName,
							MediaStore.Images.Thumbnails.MICRO_KIND);
					if (b != null) {
						choosedPic.setImageDrawable(new BitmapDrawable(b));
					}
					break;
				default:
					break;
				}
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (browLayout.isShown()) {
				hasEmotions = false;
				browLayout.setVisibility(View.GONE);
				return true;
			} else if (phraseLayout.isShown()) {
				phraseLayout.setVisibility(View.GONE);
				hasPhraseList = false;
				return true;
			} else {
				return super.onKeyDown(keyCode, event);
			}

		} else {
			return super.onKeyDown(keyCode, event);
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

	private void getIntentData() {
		Intent i = getIntent();
		if (i.hasExtra("programName"))
			programName = i.getStringExtra("programName");
		if (i.hasExtra("programId"))
			programId = i.getStringExtra("programId");
		if (i.hasExtra("topicId"))
			topicId = i.getStringExtra("topicId");
		if (i.hasExtra("sinaPic")) {// sina微博自带图片
			sinaPic = i.getStringExtra("sinaPic");
		}
	}

	// 视频播放页面带来的节目剧照
	private String sinaPic;
}
