package com.sumavision.talktv2.activity;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.RecommendData;

public class GalleryAdapter extends BaseAdapter {

	private Context context;

	private ArrayList<RecommendData> list;
	private ImageLoaderHelper imageLoaderHelper;

	public GalleryAdapter(Context context, ArrayList<RecommendData> list) {

		imageLoaderHelper = new ImageLoaderHelper();
		this.list = list;
		this.context = context;
	}

	@Override
	public int getCount() {

		if (list != null)
			return list.size();
		return 0;
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
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.rcmd_pic_item, null);
			viewHolder.picImageView = (ImageView) convertView
					.findViewById(R.id.imageView);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		RecommendData temp = list.get(position);
		String url = temp.pic;
		viewHolder.picImageView.setTag(url);
		imageLoaderHelper.loadImage(viewHolder.picImageView, url,
				R.drawable.recommend_pic_default);
		return convertView;
	}

	public static class ViewHolder {

		public ImageView picImageView;
		public TextView nameTextView;
		public TextView scoreTextView;
		public TextView introView;
	}

}
