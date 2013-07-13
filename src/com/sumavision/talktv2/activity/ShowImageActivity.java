package com.sumavision.talktv2.activity;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.utils.CommonUtils;
import com.umeng.analytics.MobclickAgent;

/**
 * @author 郭鹏
 * @version 2.0
 * @description 查看评论中图片
 * @createTime 2012-6-8
 * @changeLog
 */
public class ShowImageActivity extends Activity implements OnClickListener {

	private String url;
	// 屏幕高度
	private int height;
	private int width;
	private WindowManager mWindowManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showimage);
		url = getIntent().getStringExtra("url");
		initUtils();
		initViews();
		setListeners();
		// imageView.setVisibility(View.GONE);
		// imageView.setTag(url);
		// loadListImage(imageView, url);
	}

	private AsyncImageLoader imageLoader;

	private void initUtils() {
		imageLoader = new AsyncImageLoader();
		// bitmapLoader = new AsyncImageBitmapLoader();
		OtherCacheData.current().offset = 0;
		OtherCacheData.current().pageCount = 10;
	}

	private ProgressBar progressBar;
	private TextView errTextView;
	private ImageView imageView;
	private StepGallery gallery;

	private void initViews() {
		errTextView = (TextView) findViewById(R.id.err_text);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		// imageView = (ImageView) findViewById(R.id.imageView);
		gallery = (StepGallery) findViewById(R.id.imageView_gallery);
		convertBigPics();
		findViewById(R.id.back).setOnClickListener(this);
		errTextView.setText("点击可返回");
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display defaultDisplay = mWindowManager.getDefaultDisplay();
		height = defaultDisplay.getHeight();
		width = defaultDisplay.getWidth();
	}

	private void convertBigPics() {
		int l = OtherCacheData.current().bigPics.length;
		String[] temp = new String[l];
		for (int i = 0; i < l; i++) {
			temp[i] = OtherCacheData.current().bigPics[i].replace("s.", "b.");
		}

		GalleryAdapter ga = new GalleryAdapter(temp);
		gallery.setAdapter(ga);
		gallery.setSelection(OtherCacheData.current().bigPicPosition);
		// gallery.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		// long arg3) {
		// // TODO Auto-generated method stub
		// finish();
		// }
		//
		// });
	}

	private void setListeners() {
		errTextView.setOnClickListener(this);
		// imageView.setOnClickListener(this);
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
								show();
							} else {
								errTextView.setText("加载失败");
								progressBar.setVisibility(View.GONE);
								errTextView.setVisibility(View.VISIBLE);
							}
						}
					});
			if (bitmap != null) {
				imageView.setImageDrawable(bitmap);
				show();
			} else {
				imageView
						.setImageResource(R.drawable.rcmd_list_item_pic_default);
			}
		} else {
			errTextView.setText("加载失败");
			progressBar.setVisibility(View.GONE);
			errTextView.setVisibility(View.VISIBLE);
		}
	}

	private void show() {
		progressBar.setVisibility(View.GONE);
		errTextView.setVisibility(View.GONE);
		imageView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
		case R.id.err_text:
		case R.id.imageView:
			finish();
			break;
		}
	}

	private class GalleryAdapter extends BaseAdapter {

		private final String[] pics;

		private GalleryAdapter(String[] ps) {
			pics = ps;
		}

		@Override
		public int getCount() {
			return pics.length;
		}

		@Override
		public Object getItem(int position) {
			return pics[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) ShowImageActivity.this
						.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.showpic_gallery_item,
						null);
				viewHolder = new ViewHolder();
				viewHolder.imageView = (ImageView) convertView
						.findViewById(R.id.showpic_imageView);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			String url = pics[position];

			int h = CommonUtils.dip2px(
					getApplicationContext(),
					CommonUtils.px2dip(getApplicationContext(), height - 40
							- CommonUtils.dip2px(getApplicationContext(), 48)));
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					width, h);
			viewHolder.imageView.setLayoutParams(params);
			viewHolder.imageView.setScaleType(ScaleType.CENTER_CROP);

			viewHolder.imageView.setTag(url);
			loadListImage(viewHolder.imageView, url,
					R.drawable.star_list_defalt);

			return convertView;
		}

		private class ViewHolder {
			public ImageView imageView;
		}
	}

	private void loadListImage(final ImageView imageView, String url, int resId) {
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
				imageView.setImageResource(resId);
			}
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
