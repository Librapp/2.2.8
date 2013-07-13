package com.sumavision.talktv2.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class RecommandLayout extends LinearLayout {

	public RecommandLayout(Context context) {
		super(context);
	}

	public RecommandLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			break;
		default:
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	private void init(Context context) {
		state = STATE_NONE;
		float scale = context.getResources().getDisplayMetrics().density;
		defaultPadding = (int) (120 * scale + 0.5f);
	}

	private int defaultPadding = 0;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			int temp = (int) event.getY();
			int dp = (int) (temp - interceptionStartY);
			paddingTop += dp;
			if (paddingTop <= -defaultPadding) {
				paddingTop = -defaultPadding;// 210到时候得改成VIEW的高度
				state = STATE_HIDE;
			} else if (paddingTop >= 0) {
				paddingTop = 0;
				state = STATE_NONE;
			} else {
				state = STATE_HALF_HIDE;
			}
			this.setPadding(0, paddingTop, 0, 0);
			break;
		case MotionEvent.ACTION_UP:
			if (paddingTop <= (-defaultPadding / 2)) {
				paddingTop = -defaultPadding;// 210到时候得改成VIEW的高度
				state = STATE_HIDE;
			} else {
				paddingTop = 0;
				state = STATE_NONE;
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	private float interceptionStartY;

	private int paddingTop = 0;

	private float startMoveX;
	private float startMoveY;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			interceptionStartY = ev.getY();
			startMoveX = ev.getX();
			startMoveY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float tempX = ev.getX();
			float tempY = ev.getY();
			if (state == STATE_NONE) {
				boolean isMoveHorizontal = isMoveHorizontal(tempX, tempY);
				if (isMoveHorizontal) {
					return false;
				} else {
					if (tempY > startMoveY) {
						// move down
						return false;
					}
					return true;
				}
			}
			if (state == STATE_HALF_HIDE) {
				boolean isMoveHorizontal = isMoveHorizontal(tempX, tempY);
				if (isMoveHorizontal) {
					return false;
				} else {
					return true;
				}
			}
			if (state == STATE_HIDE) {
				boolean isMoveHorizontal = isMoveHorizontal(tempX, tempY);
				if (isMoveHorizontal) {
					return false;
				} else {
					if (tempY < startMoveY) {
						// move up
						return false;
					}
					return true;
				}
			}
			break;
		}
		return false;
	}

	private boolean isMoveHorizontal(float tempX, float tempY) {

		return Math.abs(tempX - startMoveX) >= Math.abs(tempY - startMoveY);
	}

	private int state;
	private static final int STATE_NONE = 0;
	private static final int STATE_HIDE = 1;
	private static final int STATE_HALF_HIDE = 2;

}
