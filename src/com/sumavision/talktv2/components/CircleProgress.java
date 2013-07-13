package com.sumavision.talktv2.components;

import org.cybergarage.util.Debug;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.sumavision.talktv2.R;

public class CircleProgress extends View {

	public static final String UP = "up";
	private static final int DEFAULT_MAX_VALUE = 100; // 默认进度条最大值
	private static final int DEFAULT_PAINT_WIDTH = 16; // 默认画笔宽度
	private static final int DEFAULT_PAINT_COLOR = 0xffFFB128; // 默认画笔颜色
	private static final boolean DEFAULT_FILL_MODE = false; // 默认填充模式
	private static final int DEFAULT_INSIDE_VALUE = 0; // 默认缩进距离
	private static final int DEFAULT_DEGREE = 180;// 默认的角度
	private static final String DEFAULT_Direction = UP; // 默认进度条的方向

	private CircleAttribute mCircleAttribute; // 圆形进度条基本属性

	private int mMaxProgress; // 进度条最大值
	private int mMainCurProgress; // 主进度条当前值
	// private int mSubCurProgress; // 子进度条当前值
	private int r; // 进度小球所在位置的半径
	@SuppressWarnings("unused")
	private float rIn;// 半圆内径
	@SuppressWarnings("unused")
	private float rOut;// 半圆外径
	private int circleWidth;// 左上象限的宽度
	private int circleHeight;
	private float gapDegree;// 圆弧距离左边及右边的角度
	private float sweep;// 进度条已经扫过的角度

	private Drawable mBackgroundPicture; // 背景图
	private Drawable mProgressCircle;
	@SuppressWarnings("unused")
	private Drawable startPoint;
	@SuppressWarnings("unused")
	private Drawable endPoint;

	public UpCircleCallBack callBack;

	public CircleProgress(Context context) {

		super(context);
		defaultParam();
	}

	public CircleProgress(Context context, AttributeSet attrs) {

		super(context, attrs);

		defaultParam();

		TypedArray array = context.obtainStyledAttributes(attrs,
				R.styleable.CircleProgressBar);

		mMaxProgress = array.getInteger(R.styleable.CircleProgressBar_max,
				DEFAULT_MAX_VALUE); // 获取进度条最大值
		boolean bFill = array.getBoolean(R.styleable.CircleProgressBar_fill,
				DEFAULT_FILL_MODE); // 获取填充模式
		int paintWidth = array.getInt(
				R.styleable.CircleProgressBar_Paint_Width, DEFAULT_PAINT_WIDTH); // 获取画笔宽度
		int subPaintWidth = array.getInt(
				R.styleable.CircleProgressBar_SubPaint_Width,
				DEFAULT_PAINT_WIDTH); // 获取画笔宽度
		int bottomPaintWidth = array.getInt(
				R.styleable.CircleProgressBar_BottomPaint_Width,
				DEFAULT_PAINT_WIDTH); // 获取画笔宽度
		mCircleAttribute.mDirection = (array
				.getString(R.styleable.CircleProgressBar_direction));
		mCircleAttribute.mDegree = (array.getInt(
				R.styleable.CircleProgressBar_degree, DEFAULT_DEGREE));
		mCircleAttribute.setFill(bFill);
		if (bFill == false) {
			mCircleAttribute.setPaintWidth(paintWidth, subPaintWidth,
					bottomPaintWidth);
			mCircleAttribute.mPaintWidth = paintWidth;
		}

		// int paintColor = array.getColor(
		// R.styleable.CircleProgressBar_Paint_Color, DEFAULT_PAINT_COLOR); //
		// 获取画笔颜色
		//
		// Log.i("", "paintColor = " + Integer.toHexString(paintColor));
		mCircleAttribute.setPaintColor(Color.rgb(0x24, 0x93, 0xd2),
				Color.argb(0xff, 0xd1, 0xd1, 0xd1),
				Color.argb(0xff, 0xd1, 0xd1, 0xd1));// 设置默认画笔颜色为蓝色

		mCircleAttribute.mSidePaintInterval = array.getInt(
				R.styleable.CircleProgressBar_Inside_Interval,
				DEFAULT_INSIDE_VALUE);// 圆环缩进距离
		Resources r = context.getResources();
		mProgressCircle = r.getDrawable(R.drawable.dlna_progress_thumb);// 进度条的小圆球
		initData();

		array.recycle(); // 一定要调用，否则会有问题

	}

	private void initData() {
		gapDegree = (180 - mCircleAttribute.mDegree) / 2f + 180;

		if (mCircleAttribute.mDirection.equals(UP)) {
			mCircleAttribute.mDrawPos = (int) (gapDegree - 180);
		} else {
			mCircleAttribute.mDrawPos = (int) (180 - gapDegree);
		}
	}

	/**
	 * @description 设置最大进度值
	 * @param progress
	 */
	public synchronized void setMaxProgress(int progress) {

		this.mMaxProgress = progress;
	}

	public synchronized int getMaxProgress() {

		return this.mMaxProgress;
	}

	public float getSweep() {

		return sweep;
	}

	/**
	 * @description 获取当前转过的角度
	 * @return
	 */
	public float getCurrentDegree() {

		return getSweep();
	}

	public float getMaxDegree() {

		return mCircleAttribute.mDegree;
	}

	/**
	 * 默认参数
	 */
	private void defaultParam() {

		mCircleAttribute = new CircleAttribute();

		mMaxProgress = DEFAULT_MAX_VALUE;
		mMainCurProgress = 0;
		// mSubCurProgress = 0;

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { // 设置视图大小

		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);

		// mBackgroundPicture = getBackground();
		if (mBackgroundPicture != null) {
			width = mBackgroundPicture.getMinimumWidth();
			height = mBackgroundPicture.getMinimumHeight();
			if (Debug.isOn()) {
				Log.e("onMeasure height", height + "");
				Log.e("onMeasure width", width + "");
			}
		}

		setMeasuredDimension(resolveSize(width, widthMeasureSpec),
				resolveSize(width, heightMeasureSpec));

	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {

		super.onSizeChanged(w, h, oldw, oldh);
		if (Debug.isOn()) {
			Log.e("onSizeChanged height", h + "");
			Log.e("onSizeChanged width", w + "");
		}
		mCircleAttribute.autoFix(w, h);

	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float rate = (float) mMainCurProgress / mMaxProgress;
		sweep = 0;
		final Drawable progress = mProgressCircle;
		int w = progress.getIntrinsicWidth();
		int h = progress.getIntrinsicHeight();
		if (mCircleAttribute.mDirection.endsWith(UP)) {// 方向为上时
			canvas.drawArc(mCircleAttribute.mRoundOval, gapDegree,
					mCircleAttribute.mDegree,
					mCircleAttribute.mBRoundPaintsFill,
					mCircleAttribute.mBottomPaint);
			canvas.drawArc(mCircleAttribute.mRoundOval, gapDegree,
					mCircleAttribute.mDegree,
					mCircleAttribute.mBRoundPaintsFill,
					mCircleAttribute.mSubPaint);
			sweep = mCircleAttribute.mDegree * rate;
			canvas.drawArc(mCircleAttribute.mRoundOval, gapDegree, sweep,
					mCircleAttribute.mBRoundPaintsFill,
					mCircleAttribute.mMainPaints); // 画进度条

			double y = (circleHeight + (FloatMath.sin((float) Math
					.toRadians(sweep + gapDegree))) * r);
			double x = 0;
			if (sweep > mCircleAttribute.mDegree / 2f) {
				x = circleWidth
						+ Math.sqrt(r * r - (circleHeight - y)
								* (circleHeight - y));

			} else {
				x = circleWidth
						- Math.sqrt(r * r - (circleHeight - y)
								* (circleHeight - y));
			}
			BitmapDrawable d = (BitmapDrawable) progress;

			canvas.drawBitmap(d.getBitmap(), (int) (x - (w / 2)),
					(int) (y - (h / 2)), null);
		} else {
			canvas.drawArc(mCircleAttribute.mRoundOval, gapDegree,
					mCircleAttribute.mDegree,
					mCircleAttribute.mBRoundPaintsFill,
					mCircleAttribute.mBottomPaint);
			sweep = -mCircleAttribute.mDegree * rate;
			canvas.drawArc(mCircleAttribute.mRoundOval,
					mCircleAttribute.mDrawPos, sweep,
					mCircleAttribute.mBRoundPaintsFill,
					mCircleAttribute.mMainPaints); // 画进度条
			double y = (circleHeight + (Math.abs(FloatMath.sin((float) Math
					.toRadians(sweep - gapDegree)))) * r);
			double x = 0;
			if (sweep < -mCircleAttribute.mDegree / 2f) {
				x = circleWidth
						+ Math.sqrt(r * r - (circleHeight - y)
								* (circleHeight - y));
			} else {
				x = circleWidth
						- Math.sqrt(r * r - (circleHeight - y)
								* (circleHeight - y));
			}
			BitmapDrawable d = (BitmapDrawable) progress;
			canvas.drawBitmap(d.getBitmap(), (int) (x - (w / 2)),
					(int) (y - (h / 2)), null);
		}
		// float subRate = (float) mSubCurProgress / mMaxProgress;
		// float subSweep = 180 * subRate;
		// canvas.drawArc(mCircleAttribute.mRoundOval,
		// mCircleAttribute.mDrawPos,
		// subSweep, mCircleAttribute.mBRoundPaintsFill,
		// mCircleAttribute.mSubPaint);

		// Log.e("图片高度", h + "");
		// Log.e("图片宽度", w + "");
		// Log.e("画布左象限高度", circleHeight + "");
		// Log.e("画布左象限宽度", circleWidth + "");
		// Log.e("椭圆半径", r + "");
		// Log.e("椭圆角度", sweep + "");
		// Log.e("椭圆角度 sin",
		// Math.abs(FloatMath.sin((float) Math.toRadians(sweep))) + "");
		// Log.e("图片坐标X", x + "");
		// Log.e("图片坐标y", y + "");
	}

	/**
	 * 设置主进度值
	 */
	public synchronized void setMainProgress(int progress) {

		mMainCurProgress = progress;
		if (mMainCurProgress < 0) {
			mMainCurProgress = 0;
		}

		if (mMainCurProgress > mMaxProgress) {
			mMainCurProgress = mMaxProgress;
		}

		invalidate();
	}

	public synchronized int getMainProgress() {

		return mMainCurProgress;
	}

	/*
	 * 设置子进度值
	 */
	/*
	 * public synchronized void setSubProgress(int progress) { mSubCurProgress =
	 * progress; if (mSubCurProgress < 0) { mSubCurProgress = 0; }
	 * 
	 * if (mSubCurProgress > mMaxProgress) { mSubCurProgress = mMaxProgress; }
	 * 
	 * invalidate(); }
	 * 
	 * public synchronized int getSubProgress() { return mSubCurProgress; }
	 */
	class CircleAttribute {

		public RectF mRoundOval; // 圆形所在矩形区域
		public boolean mBRoundPaintsFill; // 是否填充以填充模式绘制圆形
		public int mSidePaintInterval; // 圆形向里缩进的距离
		public int mPaintWidth; // 圆形画笔宽度（填充模式下无视）
		public int mPaintColor; // 画笔颜色 （即主进度条画笔颜色，子进度条画笔颜色为其半透明值）
		public int mDrawPos; // 绘制圆形的起点（默认为-180度即9点钟方向）

		public Paint mMainPaints; // 主进度条画笔
		public Paint mSubPaint; // 子进度条画笔

		public Paint mBottomPaint; // 无背景图时绘制所用画笔
		public String mDirection;
		public float mDegree;

		public CircleAttribute() {

			mRoundOval = new RectF();
			mBRoundPaintsFill = DEFAULT_FILL_MODE;
			mSidePaintInterval = DEFAULT_INSIDE_VALUE;
			mPaintWidth = 0;
			mPaintColor = DEFAULT_PAINT_COLOR;
			mDrawPos = -180;
			mDirection = DEFAULT_Direction;
			mDegree = DEFAULT_DEGREE;

			mMainPaints = new Paint();
			mMainPaints.setAntiAlias(true);
			mMainPaints.setStyle(Paint.Style.STROKE);
			mMainPaints.setStrokeWidth(mPaintWidth);
			mMainPaints.setColor(mPaintColor);
			// mMainPaints.setMaskFilter(new BlurMaskFilter(8,
			// BlurMaskFilter.Blur.SOLID));

			mSubPaint = new Paint();
			mSubPaint.setAntiAlias(true);
			mSubPaint.setStyle(Paint.Style.STROKE);
			mSubPaint.setStrokeWidth(mPaintWidth);
			mSubPaint.setColor(mPaintColor);

			mBottomPaint = new Paint();
			mBottomPaint.setAntiAlias(true);
			mBottomPaint.setStyle(Paint.Style.STROKE);
			mBottomPaint.setStrokeWidth(mPaintWidth);
			mBottomPaint.setColor(Color.argb(0xff, 0xd1, 0xd1, 0xd1));

		}

		/*
		 * 设置画笔宽度
		 */
		public void setPaintWidth(int width, int subWidth, int bottomWidth) {

			mMainPaints.setStrokeWidth(width);
			mSubPaint.setStrokeWidth(subWidth);
			mBottomPaint.setStrokeWidth(bottomWidth);
		}

		/*
		 * 设置画笔颜色
		 */
		public void setPaintColor(int color, int subColor, int bottomColor) {

			mMainPaints.setColor(color);
			mSubPaint.setColor(subColor);
			mBottomPaint.setColor(bottomColor);
		}

		/*
		 * 设置填充模式
		 */
		public void setFill(boolean fill) {

			mBRoundPaintsFill = fill;
			if (fill) {
				mMainPaints.setStyle(Paint.Style.FILL);
				mSubPaint.setStyle(Paint.Style.FILL);
				mBottomPaint.setStyle(Paint.Style.FILL);
			} else {
				mMainPaints.setStyle(Paint.Style.STROKE);
				mSubPaint.setStyle(Paint.Style.STROKE);
				mBottomPaint.setStyle(Paint.Style.STROKE);
			}
		}

		/**
		 * @description 调整进度条方向
		 * @param direction
		 */
		public void setDirection(String direction) {

			if (direction != null && direction.equals("")) {
				mDirection = direction;
			}

		}

		/*
		 * 自动修正
		 */
		public void autoFix(int w, int h) {

			if (mSidePaintInterval != 0) {
				mRoundOval.set(mPaintWidth / 2 + mSidePaintInterval,
						mPaintWidth / 2 + mSidePaintInterval, w - mPaintWidth
								/ 2 - mSidePaintInterval, h - mPaintWidth / 2
								- mSidePaintInterval);
			} else {

				int sl = getPaddingLeft();
				int sr = getPaddingRight();
				int st = getPaddingTop();
				int sb = getPaddingBottom();

				mRoundOval.set(sl + mPaintWidth / 2, st + mPaintWidth / 2, w
						- sr - mPaintWidth / 2, h - sb - mPaintWidth / 2);
			}
			r = (int) (w / 2f - mCircleAttribute.mSidePaintInterval - mCircleAttribute.mPaintWidth / 2f);
			float d = mCircleAttribute.mSidePaintInterval
					+ mCircleAttribute.mPaintWidth / 2f;
			circleWidth = (int) (w / 2f);
			circleHeight = (int) (h / 2f);
			rIn = circleWidth - mCircleAttribute.mPaintWidth - d;
			rOut = circleWidth - d;
		}

	}

	@SuppressWarnings("unused")
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction();
		float x = Math.abs(event.getX());
		float y = Math.abs(event.getY());

		double r = FloatMath.sqrt((circleWidth - x) * (circleWidth - x)
				+ (circleHeight - y) * (circleHeight - y));

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			this.callBack.upCircleCallBackStartSeek();
			break;
		case MotionEvent.ACTION_UP:
			if (callBack != null) {
				call();
			}
			break;
		default:
			// 由X Y算出角度再算进度
			double rActual = 0;
			double d = Math.sin(Math.PI / 4);
			// 进度条在上半区时
			if (y < circleHeight) {
				if (x > circleWidth) {// 大于90度时
					rActual = Math.sqrt(Math.pow(x - circleWidth, 2)
							+ Math.pow(circleHeight - y, 2));
					// if (rActual > rIn - 5 && rActual < rOut + 5) {
					// 触摸点落在半圆内
					double degree = Math
							.toDegrees(Math.asin((event.getX() - circleWidth)
									/ r));
					mMainCurProgress = (int) ((mCircleAttribute.mDegree / 2f + degree)
							/ mCircleAttribute.mDegree * mMaxProgress);
					// }
				} else {// 小于90度时
					rActual = Math.sqrt(Math.pow(circleWidth - x, 2)
							+ Math.pow(circleHeight - y, 2));
					// if (rActual > rIn - 5 && rActual < rOut + 5) {
					// 触摸点落在半圆内
					mMainCurProgress = (int) ((Math.toDegrees(Math
							.asin((circleHeight - event.getY()) / r)) + 180 - gapDegree)
							/ mCircleAttribute.mDegree * mMaxProgress);
					// }
				}
				setMainProgress(mMainCurProgress);
				this.callBack.upCircleCallBackStartSeek();
				invalidate();
			} else if (circleHeight + r * Math.sin(Math.PI / 4) > y) {
				if (x > circleWidth + r * Math.sin(Math.PI / 4)) {
					// 大于90度时
					rActual = Math.sqrt(Math.pow(x - circleWidth, 2)
							+ Math.pow(circleHeight - y, 2));
					// 触摸点落在半圆内
					double degree = Math
							.toDegrees(Math.asin((event.getX() - circleWidth)
									/ r));
					mMainCurProgress = (int) ((180 + gapDegree - degree)
							/ mCircleAttribute.mDegree * mMaxProgress);
					setMainProgress(mMainCurProgress);
					this.callBack.upCircleCallBackStartSeek();
					invalidate();
				} else if (x < circleWidth - r * Math.sin(Math.PI / 4)) {
					// 小于90度时
					rActual = Math.sqrt(Math.pow(circleWidth - x, 2)
							+ Math.pow(circleHeight - y, 2));
					double degree = Math.toDegrees(Math
							.asin((-circleHeight + event.getY()) / r));
					// 触摸点落在半圆内
					mMainCurProgress = (int) ((-degree + 180 - gapDegree)
							/ mCircleAttribute.mDegree * mMaxProgress);
					setMainProgress(mMainCurProgress);
					this.callBack.upCircleCallBackStartSeek();
					invalidate();
				}
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	public void setCallfuc(UpCircleCallBack cb) {

		this.callBack = cb;
	}

	public void call() {

		this.callBack.upCircleCallBack();
	}

	/**
	 * 实现回调接口，继承此接口，实现callBack方法，并setCallfuc 即可在手指拿起时执行callback方法
	 * 
	 * @author ctbri-ywb
	 * 
	 */
	public interface UpCircleCallBack {

		public void upCircleCallBack();

		public void upCircleCallBackStartSeek();
	}

}
