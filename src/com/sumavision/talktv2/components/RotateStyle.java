package com.sumavision.talktv2.components;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * 
 * @author 郭鹏
 * 
 */
public class RotateStyle extends Animation {
	private Matrix matrix; // 作用矩阵
	private Camera camera;
	private int fromY, toY; // 翻转角度差
	private int centerX, centerY; // 图片中心点

	public RotateStyle(int fromY, int toY, int deltaZ, int centerX, int centerY) {
		super();
		this.toY = toY;
		this.fromY = fromY;
		this.centerX = centerX;
		this.centerY = centerY;

	}

	@Override
	public void initialize(int width, int height, int parentWidth,
			int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		camera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		final float fromDegrees = fromY;
		float degrees = fromDegrees + ((toY - fromDegrees) * interpolatedTime);
		matrix = t.getMatrix();
		camera.save();
		camera.rotateY(degrees);
		// 设置camera作用矩阵
		camera.getMatrix(matrix);
		camera.restore();
		// 设置翻转中心点
		matrix.preTranslate(-this.centerX, -this.centerY);
		matrix.postTranslate(this.centerX, this.centerY);
	}
}
