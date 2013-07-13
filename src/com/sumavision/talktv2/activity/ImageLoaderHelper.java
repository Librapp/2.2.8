package com.sumavision.talktv2.activity;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class ImageLoaderHelper {

	private ImageLoader imageLoader = ImageLoader.getInstance();

	public void loadImage(ImageView imageView, String url, int defalutPic) {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showStubImage(defalutPic).showImageForEmptyUri(defalutPic)
				.showImageOnFail(defalutPic).cacheInMemory().cacheOnDisc()
				.build();
		imageLoader.displayImage(url, imageView, options, animateFirstListener);
	}

	private static final int animationDuration = 600;
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

	private static class AnimateFirstDisplayListener extends
			SimpleImageLoadingListener {

		static final List<String> displayedImages = Collections
				.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (loadedImage != null) {
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				imageView.setImageBitmap(loadedImage);
				if (firstDisplay) {
					FadeInBitmapDisplayer.animate(imageView, animationDuration);
					displayedImages.add(imageUri);
				}
			}
		}
	}
}
