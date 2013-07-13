package com.sumavision.talktv2.utils;

import java.lang.reflect.Field;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.sumavision.talktv2.R;
import com.sumavision.talktv2.data.EmotionData;
import com.sumavision.talktv2.data.MakeEmotionsList;

/**
 * 
 * @author 郭鹏
 * 
 */
public class Txt2Image {

	public static SpannableString txtToImg(String content, Context c) {
		SpannableString ss = new SpannableString(content);
		int starts = 0;
		int end = 0;

		if (content.indexOf("[", starts) != -1
				&& content.indexOf("]", end) != -1) {
			starts = content.indexOf("[", starts);
			end = content.indexOf("]", end);
			String phrase = content.substring(starts, end + 1);
			String imageName = "";
			List<EmotionData> list = MakeEmotionsList.current().getLe();
			for (EmotionData emotion : list) {

				if (content.equals("[心]")
						&& emotion.getPhraseOther().equals("[心]")) {
					imageName = emotion.getImageName();
				} else if (content.equals("[伤心]")
						&& emotion.getPhrase().equals("[衰]")) {
					imageName = emotion.getImageName();
				} else if (content.equals("[便便]")
						&& emotion.getPhrase().equals("[吐]")) {
					imageName = emotion.getImageName();
				} else if (content.equals("[花]")
						&& emotion.getPhrase().equals("[热吻]")) {
					imageName = emotion.getImageName();
				} else if (emotion.getPhrase().equals(phrase)) {
					imageName = emotion.getImageName();
				}
			}

			try {
				Field f = (Field) R.drawable.class.getDeclaredField(imageName);
				int i = f.getInt(R.drawable.class);
				Drawable drawable = c.getResources().getDrawable(i);
				if (drawable != null) {
					drawable.setBounds(5, 7, drawable.getIntrinsicWidth() + 10,
							drawable.getIntrinsicHeight() + 10);
					ImageSpan span = new ImageSpan(drawable,
							ImageSpan.ALIGN_BASELINE);
					ss.setSpan(span, starts, end + 1,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {

			}

		}
		return ss;
	}

	public static SpannableString text2Emotion(Context context, String text) {

		SpannableString spannable = new SpannableString(text);
		int start = 0;
		int t = 0;
		ImageSpan span;
		Drawable drawable;
		List<EmotionData> le = MakeEmotionsList.current().getLe();

		for (int i = 0; i < le.size(); i++) {

			int l = le.get(i).getPhrase().length();
			for (start = 0; (start + l) <= text.length(); start += l) {

				// String s = le.get(i).getPhraseOther();
				// if (s.equals("")) {
				t = text.indexOf(le.get(i).getPhrase(), start);
				if (t != -1) {

					drawable = context.getResources().getDrawable(
							le.get(i).getId());
					drawable.setBounds(5, 5, drawable.getIntrinsicWidth(),
							drawable.getIntrinsicHeight());
					span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);

					// Log.i("start-------------------------", start + "");
					spannable.setSpan(span, t, t + l,
							Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

				}
				// }

				// else {
				// t = text.indexOf(le.get(i).getPhraseOther(), start);
				//
				// if (t != -1) {
				//
				// drawable = context.getResources().getDrawable(
				// le.get(i).getId());
				// drawable.setBounds(5, 5, drawable.getIntrinsicWidth(),
				// drawable.getIntrinsicHeight());
				// span = new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE);
				//
				// // Log.i("start-------------------------", start + "");
				// spannable.setSpan(span, t, t + l,
				// Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
				//
				// }
				// }
			}
		}
		return spannable;

	}

}
