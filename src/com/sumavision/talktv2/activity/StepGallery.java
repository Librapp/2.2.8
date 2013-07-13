package com.sumavision.talktv2.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Gallery;

/**
 * 
 * @author 郭鹏
 * @version 2.0
 * @createTime
 * @description 步进Gallery
 * @changLog
 */

public class StepGallery extends Gallery {

	public StepGallery(Context context, AttributeSet attrs) {

		super(context, attrs);

	}

	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) {

		return e2.getX() > e1.getX();

	}

	
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,

	float velocityY) {

		int keyCode;
		if (isScrollingLeft(e1, e2)) {
			keyCode = KeyEvent.KEYCODE_DPAD_LEFT;
		} else {
			keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;

		}
		onKeyDown(keyCode, null);

		return true;

	}

}
