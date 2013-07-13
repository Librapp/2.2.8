package com.sumavision.talktv2.dlna;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sumavision.talktv2.R;

public class DeviceAdapter extends BaseAdapter {

	private ArrayList<DeviceDataInSearchList> list;
	private Context context;

	public DeviceAdapter(ArrayList<DeviceDataInSearchList> list, Context context) {
		this.list = list;
		this.context = context;
	}

	@Override
	public int getCount() {
		if (list == null) {
			return 0;
		}
		return list.size();
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
			convertView = inflater.inflate(R.layout.network_device_list_item,
					null);

			viewHolder.textView = (TextView) convertView
					.findViewById(R.id.textView);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		DeviceDataInSearchList temp = list.get(position);
		String name = temp.name;
		if (name != null)
			viewHolder.textView.setText(name);
		return convertView;
	}

	static class ViewHolder {
		public TextView textView;
	}
}
