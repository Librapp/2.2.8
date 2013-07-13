package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.data.StarData;
import com.sumavision.talktv2.data.VodProgramData;
import com.sumavision.talktv2.net.NetConnectionListener;
import com.sumavision.talktv2.net.StarDetailParser;
import com.sumavision.talktv2.net.StarDetailRequest;
import com.sumavision.talktv2.task.GetStarDetailTask;
import com.sumavision.talktv2.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * @author jianghao
 * @version 2.0
 * @createTime 2012-1-8
 * @description明星详情
 * @changeLog
 */
public class StarDetailActivity extends Activity implements OnClickListener,
		NetConnectionListener, OnItemClickListener {

	private int starId;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		starId = getIntent().getIntExtra("starId", 0);
		setContentView(R.layout.star_detail_new);
		initOthers();
		initViews();
		setListeners();
		getStarDetail();
	}

	private AsyncImageLoader imageLoader;

	private void initOthers() {
		imageLoader = new AsyncImageLoader();
		findViewById(R.id.back).setOnClickListener(this);
	}

	private TextView errText;
	private ProgressBar progressBar;
	private ScrollView scrollView;
	private TextView nameText;
	private TextView englishNameText;
	private TextView starTypeText;
	private TextView hobbyText;
	private TextView introText;

	private ImageView headPicImageView;

	private Gallery gallery;
	// 演员相关剧集里列表
	private ListView programList;
	// 演员简介是否展开
	private boolean isOpened = false;

	private void initViews() {
		errText = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		scrollView = (ScrollView) findViewById(R.id.scollView);

		nameText = (TextView) findViewById(R.id.star_name);
		englishNameText = (TextView) findViewById(R.id.star_name_eng);
		starTypeText = (TextView) findViewById(R.id.star_startype);
		hobbyText = (TextView) findViewById(R.id.star_hobby);

		introText = (TextView) findViewById(R.id.star_intro);

		headPicImageView = (ImageView) findViewById(R.id.star_img);
		gallery = (Gallery) findViewById(R.id.sd_pic_gallery_small);
		programList = (ListView) findViewById(R.id.star_programs);
		programList.setSelector(R.color.transparent);

	}

	private void setListeners() {
		errText.setOnClickListener(this);
		introText.setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.star_intro:
			if (!isOpened) {
				introText.setMaxLines(400);
			} else {
				introText.setMaxLines(4);
			}
			isOpened = !isOpened;
			break;
		case R.id.back:
			close();
			finish();
			break;
		case R.id.err_text:
			getStarDetail();
			break;
		default:
			break;
		}
	}

	private GetStarDetailTask getStarDetailTask;

	private boolean hasData;

	private void getStarDetail() {
		if (getStarDetailTask == null) {
			StarData.current().stagerID = starId;
			getStarDetailTask = new GetStarDetailTask(this);
			getStarDetailTask.execute(this, new StarDetailRequest(),
					new StarDetailParser());
			if (!hasData) {
				progressBar.setVisibility(View.VISIBLE);
				errText.setVisibility(View.GONE);
			}
		}
	}

	private void updateUI() {
		progressBar.setVisibility(View.GONE);
		errText.setVisibility(View.GONE);
		scrollView.setVisibility(View.VISIBLE);
		String name = StarData.current().name;
		int color = getResources().getColor(R.color.black);
		if (name != null) {
			String nameStr = "中文名: " + name;
			int firstIndex = 0;
			int lastIndex = 4;
			SpannableString spannableString = CommonUtils.getSpannableString(
					nameStr, firstIndex, lastIndex, new ForegroundColorSpan(
							color));
			nameText.setText(spannableString);
		}
		String englishName = StarData.current().nameEng;
		if (englishName != null) {
			String englishStr = "英文名: " + englishName;
			int firstIndex = 0;
			int lastIndex = 4;
			SpannableString spannableString = CommonUtils.getSpannableString(
					englishStr, firstIndex, lastIndex, new ForegroundColorSpan(
							color));
			englishNameText.setText(spannableString);
		}
		String starType = StarData.current().starType;
		if (starType != null) {
			String starStr = "星座: " + starType;
			int firstIndex = 0;
			int lastIndex = 3;
			SpannableString spannableString = CommonUtils.getSpannableString(
					starStr, firstIndex, lastIndex, new ForegroundColorSpan(
							color));
			starTypeText.setText(spannableString);
		}
		String hobby = StarData.current().hobby;
		if (hobby != null) {
			String hobbyStr = "爱好: " + hobby;
			int firstIndex = 0;
			int lastIndex = 3;
			SpannableString spannableString = CommonUtils.getSpannableString(
					hobbyStr, firstIndex, lastIndex, new ForegroundColorSpan(
							color));
			hobbyText.setText(spannableString);
			hobbyText.setMaxLines(5);
		}
		String intro = StarData.current().intro;
		if (intro != null && !intro.equals("")) {
			introText.setText(intro);
		}
		String url = StarData.current().photoBig_V;
		if (url != null) {
			headPicImageView.setTag(url);
			loadImage(headPicImageView, url);
		}
		String[] temp = StarData.current().photo;
		if (temp != null) {
			gallery.setAdapter(new StarGalleryAdapter(temp));
			// if (temp.length > 2) {
			// gallery.setSelection(1);
			// }
			int c = temp.length;
			if (c > 2) {
				gallery.setSelection(2);
			} else {
				switch (c) {
				case 2:
					gallery.setSelection(1);
					break;
				case 3:
					gallery.setSelection(2);
					break;
				default:
					break;
				}
			}

			gallery.setOnItemClickListener(this);
		}

		ArrayList<VodProgramData> listp = (ArrayList<VodProgramData>) StarData
				.current().getProgram();
		if (listp != null && listp.size() != 0) {
			programList.setAdapter(new StarProgramListAdapter(listp));
			programList.setOnItemClickListener(peogramListItemClick);
			int height = (StarData.current().programCount)
					* CommonUtils.dip2px(this, 80);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, height);
			programList.setLayoutParams(params);
		}
	}

	private OnItemClickListener peogramListItemClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			ArrayList<VodProgramData> listp = (ArrayList<VodProgramData>) StarData
					.current().getProgram();

			String tvId = listp.get(arg2).id;
			String tvTopicId = listp.get(arg2).topicId;
			// VodProgramData.current.cpId = 0;
			openProgramDetailActivity(tvId, tvTopicId);
		}
	};

	private void openProgramDetailActivity(String id, String topicId) {
		Intent intent = new Intent(this, ProgramNewActivity.class);
		intent.putExtra("programId", id);
		intent.putExtra("topicId", topicId);
		intent.putExtra("cpId", 0);
		startActivity(intent);
	}

	private class StarGalleryAdapter extends BaseAdapter {
		private String[] urls;

		public StarGalleryAdapter(String[] urls) {
			this.urls = urls;
		}

		@Override
		public int getCount() {
			if (urls != null) {
				return urls.length;
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return urls[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			ViewHolder viewHolder;
			if (rowView == null) {
				LayoutInflater inflater = (LayoutInflater) StarDetailActivity.this
						.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(
						R.layout.star_detail_gallery_image_item_small, null);
				viewHolder = new ViewHolder();
				viewHolder.imageView = (ImageView) rowView
						.findViewById(R.id.sd_gallery_img_small);
				rowView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) rowView.getTag();
			}
			String url = urls[position];
			viewHolder.imageView.setTag(url);
			loadImage(viewHolder.imageView, url);
			return rowView;
		}

		private class ViewHolder {
			public ImageView imageView;
		}
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
		if ("starDetail".equals(method)) {
			if (msg != null && "".equals(msg)) {
				updateUI();
				hasData = true;
			} else {
				progressBar.setVisibility(View.GONE);
				errText.setVisibility(View.VISIBLE);
			}
			getStarDetailTask = null;
		}
	}

	@Override
	public void onNetEnd(String msg, String method, int type) {

	}

	@Override
	public void onCancel(String method) {

	}

	private void close() {
		if (getStarDetailTask != null) {
			getStarDetailTask.cancel(true);
			getStarDetailTask = null;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		String[] temp = StarData.current().photo;
		String url = temp[position];
		url = url.replace("s.", "b.");
		OtherCacheData.current().bigPics = temp;
		OtherCacheData.current().bigPicPosition = position;
		openShowImageActivity(url);
	}

	private void openShowImageActivity(String url) {
		Intent intent = new Intent(this, ShowImageActivity.class);
		intent.putExtra("url", url);
		startActivity(intent);
	}

	private class StarProgramListAdapter extends BaseAdapter {
		private ArrayList<VodProgramData> list;

		public StarProgramListAdapter(ArrayList<VodProgramData> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			if (list == null) {
				return 0;
			} else {
				return list.size();
			}
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
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
						.from(StarDetailActivity.this);
				convertView = inflater
						.inflate(R.layout.start_p_list_item, null);
				viewHolder.nameTxt = (TextView) convertView
						.findViewById(R.id.star_p_list_name);
				viewHolder.typeTxt = (TextView) convertView
						.findViewById(R.id.star_p_list_update);
				viewHolder.programPic = (ImageView) convertView
						.findViewById(R.id.star_p_list_pic);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			VodProgramData temp = list.get(position);
			String name = temp.name;
			if (name != null) {
				viewHolder.nameTxt.setText(name);
			}
			String typeName = temp.contentTypeName;
			if (typeName != null) {
				viewHolder.typeTxt.setText(typeName);
			}
			String url = temp.pic;
			viewHolder.programPic.setTag(url);
			try {
				loadImage(viewHolder.programPic, url);
			} catch (NullPointerException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			return convertView;
		}

		public class ViewHolder {
			public TextView nameTxt;
			public TextView typeTxt;
			public ImageView programPic;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		StarData.current().setProgram(null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		scrollView.smoothScrollTo(-2000, -2000);
	}

	@Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
