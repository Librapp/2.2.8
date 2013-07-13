package com.sumavision.talktv2.activity;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.utils.CommonUtils;

public class ProgramDetailLayout extends RelativeLayout {

	public ProgramDetailLayout(Context context) {
		super(context);
	}

	float scale;

	public ProgramDetailLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		scale = context.getResources().getDisplayMetrics().density;
		hidePadding = -1 * CommonUtils.dip2px(context, 29);
	}

	public ProgramDetailLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		scale = context.getResources().getDisplayMetrics().density;
		hidePadding = -1 * CommonUtils.dip2px(context, 29);
	}

	private void init() {
		state = visible;
		initPaddingTop = outer.getPaddingTop();
		currentPaddingTop = initPaddingTop;
		outerSpeed = 1;
		imageSpeed = outerSpeed / 3;
		maxImagePadding = (int) (48 * scale + 0.5f);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			startMoveX = ev.getX();
			startMoveY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float tempX = ev.getX();
			float tempY = ev.getY();

			if (state == visible || state == needAnimationVisible) {
				if (!isMoveHorizontal(tempX, tempY)) {
					return true;
				}
			}
			if (state == hide) {
				if (!isMoveHorizontal(tempX, tempY)) {
					if (startMoveY == 0) {
						startMoveY = tempY;
					}
					if (tempY <= startMoveY) {
						return false;
					} else {
						if (ProgramViewEventDispatchedController.WhoFocus == 0) {
							if (ProgramViewEventDispatchedController.listViewNeedEvent) {
								if (OtherCacheData.current().isDebugMode)
									Log.e(TAG, " list children need event");
								return false;
							}
							if (OtherCacheData.current().isDebugMode)
								Log.e(TAG, " list children dont  need event");
							return true;
						} else if (ProgramViewEventDispatchedController.WhoFocus == 1) {
							if (ProgramViewEventDispatchedController.videoViewNeedEvent) {
								if (OtherCacheData.current().isDebugMode)
									Log.e(TAG, "video children need event");
								return false;
							}
							if (OtherCacheData.current().isDebugMode)
								Log.e(TAG, " video children dont  need event");
							return true;
						} else if (ProgramViewEventDispatchedController.WhoFocus == 2) {
							if (ProgramViewEventDispatchedController.detaiViewNeedEvent) {
								// Log.e(TAG,
								// " detail children need event");
								return false;
							}
							return true;
						}
					}
				}
			}

			break;
		case MotionEvent.ACTION_UP:
			break;
		default:
			break;
		}
		return false;
	}

	private float myMoveY;
	private boolean isRecord;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			float tempY = event.getY();
			if (!isRecord) {
				myMoveY = tempY;
				isRecord = true;
				// if (OtherCacheData.current().isDebugMode)
				// Log.e(TAG, "record!!!!!! mMoveY+=" + myMoveY);
			}
			// if (OtherCacheData.current().isDebugMode)
			// Log.e(TAG, "mMoveY+=" + myMoveY + ",tempY=" + tempY);
			setScaledPadding(tempY - myMoveY);
			if (ProgramViewEventDispatchedController.WhoFocus == 2) {
				// Log.e(TAG, " detail children need event");
				if (eventLock == true) {
					executeScroll();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if (state == needAnimationVisible) {
				executeAnimation();
			} else if (state == hide) {
				currentPaddingTop = hidePadding;
				currentImagePaddingTop = (int) (initPaddingTop * imageSpeed * -1);
				outer.setPadding(0, currentPaddingTop, 0, 0);
				imageLayout.setPadding(0, currentImagePaddingTop, 0, -1
						* currentImagePaddingTop);
			}
			isRecord = false;
			break;
		default:
			break;
		}

		return true;
	}

	private static final String TAG = "programDetailLayout";

	private int state;
	private final int hide = 1;
	private final int visible = 2;
	private final int needAnimationVisible = 3;

	private float startMoveX;
	private float startMoveY;

	private RelativeLayout imageLayout;
	private RelativeLayout outer;

	private ScrollView scrollView;
	private float outerSpeed;
	private float imageSpeed;

	private int maxImagePadding;

	public void setChilden(RelativeLayout imageLayout, RelativeLayout outer) {
		this.imageLayout = imageLayout;
		this.outer = outer;

		init();
	}

	public void setChilden(RelativeLayout imageLayout, RelativeLayout outer,
			ScrollView scrollView) {
		this.imageLayout = imageLayout;
		this.outer = outer;
		this.scrollView = scrollView;

		init();
	}

	private int hidePadding;

	private int initPaddingTop;

	private int currentPaddingTop;
	private int currentImagePaddingTop;

	private void setScaledPadding(float dy) {
		int distance = (int) (dy * imageSpeed + 0.5);
		int addtionPadding = (int) (dy * outerSpeed + 0.5);
		int tempPaddingTop = initPaddingTop + addtionPadding;
		if (distance <= maxImagePadding && tempPaddingTop >= 0) {
			currentPaddingTop = tempPaddingTop;
			currentImagePaddingTop = distance;
			imageLayout.setPadding(0, currentImagePaddingTop, 0, 0);// TODO
			outer.setPadding(0, currentPaddingTop, 0, 0);
			if (currentPaddingTop > initPaddingTop) {
				state = needAnimationVisible;
			} else if (currentPaddingTop > hidePadding
					&& currentPaddingTop <= initPaddingTop) {
				state = visible;
			} else {
				state = hide;
			}
		}
		if (distance >= maxImagePadding) {
			distance = maxImagePadding;
			addtionPadding = (int) (distance / imageSpeed);
			currentImagePaddingTop = distance;
			currentPaddingTop = initPaddingTop + addtionPadding;
			imageLayout.setPadding(0, currentImagePaddingTop, 0, 0);// TODO
			outer.setPadding(0, currentPaddingTop, 0, 0);
			if (currentPaddingTop > initPaddingTop) {
				state = needAnimationVisible;
			} else if (currentPaddingTop > hidePadding
					&& currentPaddingTop <= initPaddingTop) {
				state = visible;
			} else {
				state = hide;
			}
		}
		if (tempPaddingTop <= hidePadding) {
			currentPaddingTop = hidePadding;
			currentImagePaddingTop = (int) (initPaddingTop * imageSpeed * -1);
			imageLayout.setPadding(0, currentImagePaddingTop, 0, 0);// TODO
			outer.setPadding(0, currentPaddingTop, 0, 0);
			if (currentPaddingTop > initPaddingTop) {
				state = needAnimationVisible;
			} else if (currentPaddingTop > hidePadding
					&& currentPaddingTop <= initPaddingTop) {
				state = visible;
			} else {
				state = hide;
			}
		}
	}

	private boolean isMoveHorizontal(float tempX, float tempY) {
		return Math.abs(tempX - startMoveX) >= Math.abs(tempY - startMoveY);
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			imageLayout.setPadding(0, currentImagePaddingTop, 0, 0);// TODO
			outer.setPadding(0, currentPaddingTop, 0, 0);
		};
	};

	private class UpAnimation implements Runnable {

		@Override
		public void run() {
			while (true) {
				currentImagePaddingTop -= 5;
				currentPaddingTop -= 15;
				if (currentPaddingTop <= initPaddingTop) {
					currentPaddingTop = initPaddingTop;
					currentImagePaddingTop = 0;
					break;
				}
				handler.sendEmptyMessage(1);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void executeAnimation() {
		new Thread(new UpAnimation()).start();
	}

	// 表示 详情页面的事件锁
	private boolean eventLock;

	public void executeScroll() {
		scrollView.scrollBy(0, 0);
	}
}
