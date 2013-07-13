package com.sumavision.talktv2.activity;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sumavision.talktv2.R;


/**
 * @author 郭鹏
 * @version 2.0
 * @description 常用短语适配
 * @createTime 2012-6-8
 * @changeLog
 */
public class PhrasesListAdapter extends BaseAdapter{

		private List<String> str;
		private Context c;

		public PhrasesListAdapter(Context c, List<String> s) {
			this.c = c;
			this.str = s;
		}

		@Override
		public int getCount() {
			return str.size();
		}

		@Override
		public Object getItem(int position) {
			return str.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflate = (LayoutInflater) c
					.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
			View view = inflate.inflate(R.layout.phrase_list_item, null);
			TextView t = (TextView) view.findViewById(R.id.phrase_list_item_txt);
			t.setText(str.get(position));
			return view;
		}

}
