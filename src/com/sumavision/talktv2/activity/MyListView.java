package com.sumavision.talktv2.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sumavision.talktv2.R;

public class MyListView extends ListView implements OnScrollListener {

	public MyListView(Context context) {
		super(context);
	}

	public MyListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// measureChild(headerView, LinearLayout.LayoutParams.FILL_PARENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT);
		// defaultHeight = headerView.getHeight();
		// Log.e(TAG, "defaultHeight=" + defaultHeight);

	}

	private String pullTip;
	private String refreshTip;
	private String releaseTip;
	private String updateTip;

	private View footerView;

	private void init(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		headerView = inflater.inflate(R.layout.mylistviewheader, null);
		tipTextView = (TextView) headerView.findViewById(R.id.tip);
		lastUpdateView = (TextView) headerView.findViewById(R.id.updateTime);
		progressBar = (ProgressBar) headerView.findViewById(R.id.progressBar);
		arrowImageView = (ImageView) headerView.findViewById(R.id.imageView);
		this.addHeaderView(headerView);

		defaultHeight = 90;
		headerView.setPadding(0, -1 * defaultHeight, 0, 0);
		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(200);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);
		setOnScrollListener(this);

		footerView = inflater.inflate(R.layout.mylistviewfooter, null);
		footerView.setVisibility(View.GONE);
		Resources res = context.getResources();
		pullTip = res.getString(R.string.mylistview_pull);
		releaseTip = res.getString(R.string.mylistview_release);
		updateTip = res.getString(R.string.mylistview_update);
		refreshTip = res.getString(R.string.mylistview_refreshing);
		// measureView(headerView);
		// defaultHeight = headerView.getMeasuredHeight();
	}

	private Animation animation;
	private Animation reverseAnimation;

	private String TAG = "MyListView";

	private View headerView;
	private TextView tipTextView;
	private TextView lastUpdateView;
	private ProgressBar progressBar;
	private ImageView arrowImageView;

	private int defaultHeight;

	private int state;
	private static final int none = 0;
	private static final int refresh = 1;
	private static final int release_to_refresh = 2;
	private static final int pull_to_release = 3;
	private static final int load_more = 4;
	private float startY;
	private int firstVisiblePosition;
	private boolean isRecord;
	private int ratio = 2;
	private boolean isBack;

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// if (!isRecord && firstVisiblePosition == 0) {
			// startY = ev.getY();
			// isRecord = true;
			// }
			break;
		case MotionEvent.ACTION_UP:
			if (state == none || state == pull_to_release) {
				headerView.setPadding(0, -1 * defaultHeight, 0, 0);
				state = none;
				changeHeaderViewByState();
			}
			if (state == refresh) {
				headerView.setPadding(0, 0, 0, 0);

			}
			if (state == release_to_refresh) {
				state = refresh;
				headerView.setPadding(0, 0, 0, 0);
				Log.e(TAG, "up , release to refresh");
				changeHeaderViewByState();
				if (listener != null) {
					listener.onRefresh();
				}
			}
			isBack = false;
			isRecord = false;
			break;
		case MotionEvent.ACTION_MOVE:
			if (!isRecord && firstVisiblePosition == 0 && canPullToRefresh) {
				startY = ev.getY();
				isRecord = true;
			}
			if (lastPosition == currentLastPosition && state != load_more
					&& !isRecordLoadingMore) {
				startY = ev.getY();
				isRecordLoadingMore = true;
			}
			if (isRecordLoadingMore && state != refresh) {
				float tempY = ev.getY();
				if (tempY < startY) {
					if (listener != null && canLoadMore) {
						state = load_more;
						listener.onLoadingMore();
						footerView.setVisibility(View.VISIBLE);
					}
					isRecordLoadingMore = false;
				}
			}
			if (isRecord && state != load_more) {
				float tempY = ev.getY();
				int distance = (int) ((tempY - startY) / ratio);
				if (state == none) {
					if (tempY > startY) {
						setSelection(0);
						state = pull_to_release;
						headerView.setPadding(0, (distance - defaultHeight), 0,
								0);
						changeHeaderViewByState();
					}
				}
				if (state == pull_to_release) {
					setSelection(0);
					if (tempY <= startY) {
						state = none;
						headerView.setPadding(0, -1 * defaultHeight, 0, 0);
						Log.e(TAG, " pull to none");
					} else if (distance >= defaultHeight) {
						state = release_to_refresh;
						changeHeaderViewByState();
					} else {
						headerView.setPadding(0, (distance - defaultHeight), 0,
								0);
					}

				}
				if (state == release_to_refresh) {
					setSelection(0);
					if (distance < defaultHeight) {
						isBack = true;
						state = pull_to_release;
						Log.e(TAG, "release_to_refresh to pull_to_release");
						changeHeaderViewByState();
					}
					headerView.setPadding(0, (distance - defaultHeight), 0, 0);

				}
				if (state == refresh) {
					if (tempY > startY) {
						headerView.setPadding(0, (distance - defaultHeight), 0,
								0);
					}
				}
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	private void changeHeaderViewByState() {
		switch (state) {
		case none:
			progressBar.setVisibility(View.GONE);
			arrowImageView.setVisibility(View.VISIBLE);
			tipTextView.setText(pullTip);
			break;
		case refresh:
			tipTextView.setText(refreshTip);
			progressBar.setVisibility(View.VISIBLE);
			arrowImageView.clearAnimation();
			arrowImageView.setVisibility(View.GONE);
			break;
		case release_to_refresh:
			progressBar.setVisibility(View.GONE);
			arrowImageView.setVisibility(View.VISIBLE);
			tipTextView.setText(releaseTip);
			arrowImageView.clearAnimation();
			arrowImageView.startAnimation(animation);
			break;
		case pull_to_release:
			progressBar.setVisibility(View.GONE);
			arrowImageView.setVisibility(View.VISIBLE);
			if (isBack) {
				isBack = false;
				arrowImageView.clearAnimation();
				arrowImageView.startAnimation(reverseAnimation);
			}
			tipTextView.setText(pullTip);
			break;
		default:
			break;
		}
	}

	private boolean isRecordLoadingMore;

	private OnRefreshListener listener;

	public void setOnRefreshListener(OnRefreshListener listener) {
		this.listener = listener;
	}

	public interface OnRefreshListener {
		public void onRefresh();

		public void onLoadingMore();
	}

	int lastScrollX;
	int lastScrollY;

	public void setAdapter(BaseAdapter adapter) {

		if (state == load_more) {
			state = none;
			footerView.setVisibility(View.GONE);
		} else {
			state = none;
			lastUpdateView.setText(updateTip + getDateString());
			headerView.setPadding(0, -1 * defaultHeight, 0, 0);
			changeHeaderViewByState();
		}
		if (hasFooter) {
			this.removeFooterView(footerView);
		}
		addFooterView(footerView);

		super.setAdapter(adapter);
		setSelectionFromTop(firstPostion, lastPadding);
		hasFooter = true;
	}

	private String getDateString() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
		return sdf.format(new Date());
	}

	private boolean hasFooter;
	// 用来恢复listview位置
	private int firstPostion;
	private int lastPadding;

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_IDLE
				|| scrollState == SCROLL_STATE_FLING) {
			firstPostion = getFirstVisiblePosition();
			try {
				lastPadding = getChildAt(0).getTop();
			} catch (Exception e) {
				lastPadding = 0;
			}
		}
	}

	private int lastPosition;
	private int currentLastPosition;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		firstVisiblePosition = firstVisibleItem;
		lastPosition = totalItemCount;
		if (visibleItemCount != totalItemCount) {
			currentLastPosition = firstVisibleItem + visibleItemCount;
		}
	}

	public void onLoadError() {
		if (state == load_more) {
			state = none;
			footerView.setVisibility(View.GONE);
		} else {
			state = none;
			headerView.setPadding(0, -1 * defaultHeight, 0, 0);
			changeHeaderViewByState();
		}
	}

	private void executeAnimation(int start, int end) {
		TranslateAnimation animation = new TranslateAnimation(0, 0, start, end);
		animation.setDuration(300);
		animation.start();
	}

	public void checkState() {
		if (state == none || state == release_to_refresh
				|| state == pull_to_release) {
			state = none;
			headerView.setPadding(0, -1 * defaultHeight, 0, 0);
			changeHeaderViewByState();
		}
		Log.e(TAG, "state=" + state);
	}

	/**
	 * 需要手动刷新时 需要设置成刷新状态
	 */
	public void setRefreshState() {
		if (state != refresh) {
			state = refresh;
			headerView.setPadding(0, 0, 0, 0);
			changeHeaderViewByState();
			scrollBy(0, 0);
		}
	}

	/**
	 * 是否在联网
	 * 
	 * @return
	 */
	public boolean isGetData() {
		if (state == refresh || state == load_more) {
			return true;
		}
		return false;
	}

	public void onLoadMoreOver() {
		state = none;
		footerView.setVisibility(View.GONE);
		setSelectionFromTop(firstPostion, lastPadding);
	}

	private boolean canLoadMore = true;

	public void setCanLoadMore(boolean canLoadMore) {
		this.canLoadMore = canLoadMore;
	}

	private boolean canPullToRefresh = true;

	public void setPullToRefresh(boolean canPullToRefresh) {
		this.canPullToRefresh = canPullToRefresh;
	}
}
