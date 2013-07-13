package com.sumavision.talktv2.activity;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.AsyncImageLoader.ImageCallback;
import com.sumavision.talktv2.data.EmotionData;
import com.sumavision.talktv2.data.MailData;
import com.sumavision.talktv2.data.MakeEmotionsList;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.net.MailListParser;
import com.sumavision.talktv2.net.MailListRequest;
import com.sumavision.talktv2.net.MailSendParser;
import com.sumavision.talktv2.net.MailSendRequest;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.task.MailTask;
import com.sumavision.talktv2.task.SendMailTask;
import com.sumavision.talktv2.user.UserNow;
import com.sumavision.talktv2.user.UserOther;
import com.sumavision.talktv2.utils.DialogUtil;
import com.sumavision.talktv2.utils.InfomationHelper;
import com.sumavision.talktv2.utils.Txt2Image;
import com.umeng.analytics.MobclickAgent;

/**
 * @author 李梦思
 * @createTime 2012-6-18
 * @description 私信对话界面
 * @changeLog
 */
public class UserMailActivity extends Activity implements OnClickListener,
		OnItemClickListener, NetConnectionListener {

	private MailTask mailTask;
	private SendMailTask sendMailTask;
	private EditText input;
	private TextView txtCount;
	private TextView title;
	private ListView list;
	private RelativeLayout browLayout;
	private LinearLayout layoutCount;
	private boolean hasEmotions = false;
	private List<EmotionData> ids;
	private EmotionImageAdapter eia;
	private GridView emotionGrid;
	private int type;
	private final int REQUEST_CODE_GETIMAGE_BYSDCARD = 15;
	private final int REQUEST_CODE_GETIMAGE_BYCAMERA = 16;
	private final int SEND = 3;
	private final int REFRESH = 4;
	private final int REFRESH_LISY_LOGO = 5;
	private AsyncImageLoader asyncImageLoader;
	private PMListAdapter pmla;
	private Animation a;
	private List<MailData> mList;

	// 与其发私信的目标用户名
	private String otherUserName;
	private String otherUserIconURL;
	private int otherUserId;
	private UserOther uo;

	private int from;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.privatemessage);
		getIntentData();

		connectBg = (RelativeLayout) findViewById(R.id.communication_bg);
		connectBg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				hidepb();
				if (sendMailTask != null) {
					sendMailTask.cancel(true);
					sendMailTask = null;
				}
			}
		});
		// picFrame.setOnClickListener(this);
		findViewById(R.id.privatem_title_back).setOnClickListener(this);
		findViewById(R.id.privatem_input_send).setOnClickListener(this);
		findViewById(R.id.privatem_input_text).setOnClickListener(this);
		findViewById(R.id.privatem_input_brow).setOnClickListener(this);
		txtCount = (TextView) findViewById(R.id.privatem_input_txtcount);
		title = (TextView) findViewById(R.id.privatem_title);
		title.setText("与" + otherUserName + "的私信");
		// title.setText("与" + otherUserName + "的私信");
		input = (EditText) findViewById(R.id.privatem_input);
		input.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (input.getText().length() > 0) {
					layoutCount.setVisibility(View.VISIBLE);
					txtCount.setText(input.getText().length() + "/140");
				} else {
					layoutCount.setVisibility(View.GONE);
				}
			}
		});
		input.setOnClickListener(this);

		list = (ListView) findViewById(R.id.privatem_list);
		list.setSelector(R.drawable.list_transe_selector);

		browLayout = (RelativeLayout) findViewById(R.id.privatem_relative_emotion);
		layoutCount = (LinearLayout) findViewById(R.id.privatem_layout_count);
		layoutCount.setOnClickListener(this);
		emotionGrid = (GridView) findViewById(R.id.privatem_grid_emotion);
		a = AnimationUtils.loadAnimation(this, R.anim.leftright);
		ids = MakeEmotionsList.current().getLe();
		eia = new EmotionImageAdapter(this, ids);
		emotionGrid.setAdapter(eia);
		emotionGrid.setOnItemClickListener(this);
		getMail();
		MailData.current().pic = "";

		uo = new UserOther();
	}

	private void getIntentData() {
		Intent i = getIntent();

		if (i.hasExtra("otherUserName"))
			otherUserName = i.getStringExtra("otherUserName");
		if (i.hasExtra("otherUserIconURL"))
			otherUserIconURL = i.getStringExtra("otherUserIconURL");
		if (i.hasExtra("otherUserId"))
			otherUserId = i.getIntExtra("otherUserId", 0);
		if (i.hasExtra("from")) {
			from = i.getIntExtra("from", 0);
		}
	}

	private RelativeLayout connectBg;

	private void hidepb() {
		connectBg.setVisibility(View.GONE);
	}

	private void showpb() {
		connectBg.setVisibility(View.VISIBLE);
	}

	private void getMail() {
		mailTask = new MailTask(this);
		mailTask.execute(this, new MailListRequest(otherUserId),
				new MailListParser(uo));
	}

	private void sendMail() {
		sendMailTask = new SendMailTask(this);
		sendMailTask.execute(this, new MailSendRequest(otherUserId),
				new MailSendParser(uo));
	}

	private void close() {
		if (from == MyPrivateMsgActivity.PRIVATE_MSG) {
			setResult(RESULT_OK);
		}
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.privatem_layout_count:
			hideSoftPad();
			if (input.getText().toString().length() > 0) {
				cleanText();
			}
			break;
		case R.id.privatem_title_back:
			close();
			break;
		case R.id.privatem_input_send:
			type = SEND;
			if (input.getText().toString().trim().length() <= 0) {
				Toast.makeText(this, "请先说点什么...", Toast.LENGTH_SHORT).show();
				input.startAnimation(a);
			} else {
				MailData.current().content = input.getText().toString().trim();
				hasEmotions = false;
				browLayout.setVisibility(View.GONE);
				hideSoftPad();
				sendMail();
			}
			break;
		case R.id.privatem_input_text:
			hasEmotions = false;
			browLayout.setVisibility(View.GONE);
			// input.requestFocus();
			if (!input.hasFocus()) {
				input.performClick();
				// openSoftPad();
			} else {
				hideSoftPad();
			}
			break;
		case R.id.privatem_input_brow:
			hideSoftPad();
			if (!hasEmotions) {
				browLayout.setVisibility(View.VISIBLE);
				hasEmotions = true;
			} else {
				hasEmotions = false;
				browLayout.setVisibility(View.GONE);
			}
			break;
		case R.id.privatem_input:
			hasEmotions = false;
			browLayout.setVisibility(View.GONE);

			break;
		default:
			break;
		}
	}

	public void ChooseImage(CharSequence[] items) {
		AlertDialog imageDialog = new AlertDialog.Builder(UserMailActivity.this)
				.setTitle("选择图片")
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int item) {

						if (item == 0) {
							Intent intent = new Intent(
									Intent.ACTION_GET_CONTENT);
							intent.setType("image/*");
							startActivityForResult(intent,
									REQUEST_CODE_GETIMAGE_BYSDCARD);
						} else if (item == 1) {
							Intent intent = new Intent(
									"android.media.action.IMAGE_CAPTURE");

							String camerName = InfomationHelper.getFileName();
							String fileName = "TalkTV" + camerName + ".tmp";

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
							startActivityForResult(intent,
									REQUEST_CODE_GETIMAGE_BYCAMERA);
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

	private void hideSoftPad() {
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(UserMailActivity.this
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		int cursor = input.getSelectionStart();
		EmotionData e = ids.get(arg2);
		input.getText().insert(cursor,
				Txt2Image.txtToImg(e.getPhrase(), UserMailActivity.this));
	}

	public class PMListAdapter extends BaseAdapter {
		private List<MailData> lc;
		private Context context;
		public int count;

		public PMListAdapter(Context context, List<MailData> lc) {
			this.lc = lc;
			this.context = context;
			this.count = lc.size();
			asyncImageLoader = new AsyncImageLoader();
		}

		@Override
		public int getCount() {
			return count;
		}

		@Override
		public Object getItem(int position) {
			return lc.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflate = (LayoutInflater) context
					.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
			View view = null;
			ImageView imageView = null;
			ImageView pic = null;
			TextView txt = null;
			TextView time = null;
			String imageUrl = null;
			String picUrl = null;
			if (lc.get(position).isFromSelf) {
				view = inflate.inflate(R.layout.privatemessage_list_item_send,
						null);
				convertView = view;

				PrivateMessageSendViewCache viewCache = new PrivateMessageSendViewCache(
						convertView);
				imageView = viewCache.getImageView();
				txt = viewCache.getTextView();
				time = viewCache.getTime();
				pic = viewCache.getPic();
			} else {
				view = inflate.inflate(
						R.layout.privatemessage_list_item_recieve, null);
				convertView = view;

				PrivateMessageRecViewCache viewCache = new PrivateMessageRecViewCache(
						convertView);
				imageView = viewCache.getImageView();
				txt = viewCache.getTextView();
				time = viewCache.getTime();
				pic = viewCache.getPic();
			}

			time.setText(lc.get(position).timeStemp);
			txt.setText(lc.get(position).content);
			text2Emotion(txt);

			MailData imageAndText = lc.get(position);

			if (lc.get(position).isFromSelf) {
				imageUrl = UserNow.current().iconURL;
			} else {
				imageUrl = otherUserIconURL;
			}

			imageView.setTag(imageUrl);
			Drawable cachedImage = asyncImageLoader.loadDrawable(imageUrl,
					new ImageCallback() {
						@Override
						public void imageLoaded(Drawable imageDrawable,
								String imageUrl) {
							ImageView imageViewByTag = (ImageView) list
									.findViewWithTag(imageUrl);
							if (imageViewByTag != null) {
								imageViewByTag.setImageDrawable(imageDrawable);
							}
						}
					});
			if (cachedImage == null) {
				imageView.setImageResource(R.drawable.usercenter_icon_new_big);
			} else {
				imageView.setImageDrawable(cachedImage);
			}

			picUrl = imageAndText.pic;

			if (!picUrl.equals("")) {
				pic.setVisibility(View.VISIBLE);
				pic.setTag(picUrl);

				Log.e("PMListAdapter", picUrl);

				Drawable cachedImage1 = asyncImageLoader.loadDrawable(picUrl,
						new ImageCallback() {
							@Override
							public void imageLoaded(Drawable imageDrawable,
									String imageUrl) {
								ImageView imageViewByTag = (ImageView) list
										.findViewWithTag(imageUrl);
								if (imageViewByTag != null) {
									imageViewByTag
											.setImageDrawable(imageDrawable);
								}
							}
						});
				if (cachedImage1 == null) {
					pic.setImageResource(R.drawable.fen_status_pic_default);
					// pb.setVisibility(View.VISIBLE);

				} else {
					pic.setImageDrawable(cachedImage1);
					// pb.setVisibility(View.GONE);
				}
			} else {
				pic.setVisibility(View.GONE);
			}
			return view;
		}

		private void text2Emotion(TextView txt) {

			String text = txt.getText().toString();
			SpannableString spannable = new SpannableString(text);
			int start = 0;
			int t = 0;
			ImageSpan span;
			Drawable drawable;
			List<EmotionData> le = MakeEmotionsList.current().getLe();

			for (int i = 0; i < le.size(); i++) {

				int l = le.get(i).getPhrase().length();
				for (start = 0; (start + l) <= text.length(); start += l) {

					t = text.indexOf(le.get(i).getPhrase(), start);
					if (t != -1) {

						drawable = context.getResources().getDrawable(
								le.get(i).getId());
						drawable.setBounds(5, 5, drawable.getIntrinsicWidth(),
								drawable.getIntrinsicHeight());
						span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);

						spannable.setSpan(span, t, t + l,
								Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

					}
				}
			}
			txt.setText(spannable);
		}
	}

	private void updateList() {
		if (uo.getMail() != null) {
			pmla = new PMListAdapter(this, uo.getMail());

			int s = uo.getMail().size();
			list.setAdapter(pmla);
			list.setSelection(s - 1);
			// b = new Button(this);
			// b.setId(JSONMessageType.PRIVATEMESSAGE_BTN_MORE);
			// b.setText("加载更多");
			// b.setTextColor(Color.WHITE);
			// b.setOnClickListener(this);
			// b.setTextSize(18);
			// b.setGravity(Gravity.CENTER);
			// b.setBackgroundResource(R.drawable.playbill_listitem_bg);
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Message msg = new Message();
						msg.what = REFRESH_LISY_LOGO;
						serverHandler.sendMessage(msg);
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}).start();
		} else {

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.e("onKeyDown", "keyCode：" + keyCode);
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (connectBg.isShown()) {
				hidepb();
				if (sendMailTask != null) {
					sendMailTask.cancel(true);
					sendMailTask = null;
				}
				return true;
			} else if (hasEmotions) {
				browLayout.setVisibility(View.GONE);
				hasEmotions = !hasEmotions;
				return false;
			} else {
				setResult(RESULT_OK);
				return super.onKeyDown(keyCode, event);
			}
		} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			Log.e("UserMail", "KeyEvent.KEYCODE_ENTER");
			int cursor = input.getSelectionStart();
			input.getText().insert(cursor, "_  _");
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (uo.getMail() != null)
			uo.getMail().clear();

		uo = null;
		System.gc();
	}

	private Handler serverHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_LISY_LOGO:
				pmla.notifyDataSetChanged();
				break;
			case JSONMessageType.NET_BEGIN:
				showpb();
				break;
			case JSONMessageType.NET_END:
				hidepb();
				if (UserNow.current().errMsg.equals("")) {
					switch (type) {
					case SEND:
						input.setText("");
						updateList();
						list.setAdapter(pmla);
						list.setSelection(uo.getMail().size());
						// picSet(false);
						break;
					case REFRESH:
						Toast.makeText(UserMailActivity.this, "刷新成功",
								Toast.LENGTH_SHORT).show();
						UserNow.current().isMore = false;
						updateList();
						list.setAdapter(pmla);
						list.setSelection(uo.getMail().size());
						break;
					default:
						break;
					}
					String point = "";
					if (UserNow.current().getPoint > 0) {
						point += "TV币 +" + UserNow.current().getPoint + "\n";
						UserNow.current().getPoint = 0;
						if (OtherCacheData.current().isShowExp)
							if (UserNow.current().getExp > 0) {
								point += "经验值 +" + UserNow.current().getExp
										+ "\n";
								UserNow.current().getExp = 0;
							}
						DialogUtil.showScoreAddToast(UserMailActivity.this,
								point);
					}
					saveUserPoint();
				} else if (UserNow.current().isTimeOut) {
					Toast.makeText(UserMailActivity.this, "网络繁忙，请稍后重试",
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(UserMailActivity.this,
							UserNow.current().errMsg, Toast.LENGTH_LONG).show();
					switch (type) {
					case REFRESH:
						uo.setMail(mList);
						break;
					case SEND:

						break;
					default:
						break;
					}
				}
				break;
			default:
				break;
			}
		}
	};

	private void saveUserPoint() {
		SharedPreferences spUser = getSharedPreferences("userInfo", 0);
		Editor ed = spUser.edit();
		ed.putLong("point", UserNow.current().point);
		ed.putLong("exp", UserNow.current().exp);
		ed.putString("level", UserNow.current().level);
		ed.commit();
	}

	// private void cleanPic() {
	// AlertDialog.Builder builder = new Builder(this);
	// builder.setTitle("请选择");
	// // 添加按钮
	// builder.setIcon(R.drawable.icon);
	// builder.setPositiveButton("清空图片",
	// new android.content.DialogInterface.OnClickListener() {
	//
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.dismiss();
	// picSet(false);
	// }
	// });
	// builder.setNegativeButton("查看图片",
	// new android.content.DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.dismiss();
	// Intent i = new Intent(UserMailActivity.this,
	// ShowImageActivity.class);
	// UserNow.current().tempBitmap = result;
	// startActivityForResult(i, REQUESTCODE_SHOWIMG);
	//
	// }
	// });
	// builder.create().show();
	// }

	@Override
	protected void onResume() {
		// if (result != null) {
		// choosedPic.setImageDrawable(new BitmapDrawable(result));
		// }
		MobclickAgent.onResume(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);

	}

	OnKeyListener enter = new OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				Log.e("UserMail", "KeyEvent.KEYCODE_ENTER");
				int cursor = input.getSelectionStart();
				String e = " " + " ";
				input.getText().insert(cursor, e);
			} else if (keyCode == KeyEvent.KEYCODE_ENTER
					&& event.getAction() == KeyEvent.ACTION_UP) {

				Log.e("UserMail", "KeyEvent.KEYCODE_ENTER");
				int cursor = input.getSelectionStart();
				String e = " " + " ";
				input.getText().insert(cursor, e);
				return false;
			}
			return false;
		}
	};

	@Override
	public void onNetBegin(String method) {
		showpb();
	}

	@Override
	public void onNetEnd(String msg, String method) {
		hidepb();
		if (method.equals("mailDetail")) {
			String s = new MailListParser(uo).parse(msg);
			if (s != null && s.equals(""))
				updateList();
			else
				Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
			mailTask = null;
		} else if (method.equals("mailAdd")) {
			if (msg != null && msg.equals(""))
				updateList();
			else
				Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			sendMailTask = null;
		}

	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}
}
