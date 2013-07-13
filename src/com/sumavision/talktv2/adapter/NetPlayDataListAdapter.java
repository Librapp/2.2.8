package com.sumavision.talktv2.adapter;

import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.activity.ImageLoaderHelper;
import com.sumavision.talktv2.data.NetPlayData;

public class NetPlayDataListAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<NetPlayData> list;
	ImageLoaderHelper imageLoader;

	public NetPlayDataListAdapter(Context context, ArrayList<NetPlayData> list) {
		this.context = context;
		this.list = list;
		imageLoader = new ImageLoaderHelper();
	}

	@Override
	public int getCount() {
		if (list != null) {
			return list.size();
		}
		return 0;
	}

	@Override
	public NetPlayData getItem(int position) {
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
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.pd_netlive_list_item, null);
			viewHolder = new ViewHolder();
			viewHolder.textView = (TextView) convertView
					.findViewById(R.id.name);
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.pic);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		NetPlayData temp = list.get(position);
		String name = list.get(position).name;
		if (name != null) {
			viewHolder.textView.setText(name);
		}
		String url = temp.pic;
		if (url != null) {
			imageLoader.loadImage(viewHolder.imageView, url,
					R.drawable.pd_netlive_tvlogo);
		}
		return convertView;
	}

	static class ViewHolder {
		public TextView textView;
		public ImageView imageView;
	}
}
