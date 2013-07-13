package com.sumavision.talktv2.activity;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sumavision.talktv2.data.OtherCacheData;
import com.sumavision.talktv2.net.JSONMessageType;
import com.sumavision.talktv2.utils.BitmapUtils;

public class AsyncImageBitmapLoader {
	private HashMap<String, SoftReference<Bitmap>> imageCache;

	public AsyncImageBitmapLoader() {
		recyle();
		imageCache = new HashMap<String, SoftReference<Bitmap>>();
	}

	private ExecutorService executorService = Executors.newFixedThreadPool(3);

	public Bitmap loadDrawable(final String imageUrl,
			final ImageCallback imageCallback) {
		if (imageCache.containsKey(imageUrl)) {
			SoftReference<Bitmap> softReference = imageCache.get(imageUrl);
			Bitmap drawable = softReference.get();
			if (drawable != null) {
				return drawable;
			}
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Bitmap) message.obj, imageUrl);
			}
		};
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				Bitmap drawable = loadImageFromUrl(imageUrl);
				imageCache.put(imageUrl, new SoftReference<Bitmap>(drawable));
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
			}
		});

		return null;
	}

	/**
	 * 取得文件名
	 */
	private String getFileNname(String str) {

		String name;
		name = str.substring(str.lastIndexOf("/"), str.lastIndexOf("."));

		return name;
	}

	public Bitmap loadImageFromUrl(String url) {

		URL m;
		InputStream i = null;
		try {
			m = new URL(url);
			i = (InputStream) m.getContent();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		BitmapFactory.Options opts = new BitmapFactory.Options();
		if (OtherCacheData.current().isLowAbilityDevice) {
			opts.inSampleSize = 1;
		} else {
			opts.inSampleSize = 1;
		}

		String name = getFileNname(url);

		String fileDir = JSONMessageType.USER_ALL_SDCARD_FOLDER
				+ File.separator + "videoRecommand" + File.separator;
		String filePath = fileDir + name + ".jpg";
		Drawable d = null;
		try {
			d = Drawable.createFromPath(filePath);
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		if (d != null) {
			return BitmapUtils.drawableToBitmap(d);
		} else {

			Bitmap bitmap;
			if (!OtherCacheData.current().isLowSKIAVersion) {
				bitmap = BitmapFactory.decodeStream(i, null, opts);
			} else {
				bitmap = BitmapFactory.decodeStream(new FlushedInputStream(i),
						null, opts);
			}
			SoftReference<Bitmap> soft = new SoftReference<Bitmap>(bitmap);
			imageCache.put(url, soft);
			try {
				i.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			m = null;

			// 写入SD卡，图片质量下降
			boolean sdExists = Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED);
			if (sdExists) {
				try {
					BitmapUtils.saveDrawable(new BitmapDrawable(bitmap),
							fileDir, name);
				} catch (Exception e) {

				}
			}

			return bitmap;
		}

	}

	public interface ImageCallback {
		public void imageLoaded(Bitmap imageDrawable, String imageUrl);
	}

	static class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int b = read();
					if (b < 0) {
						break;
						// we reached EOF
					} else {
						bytesSkipped = 1;
						// we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}

			return totalBytesSkipped;

		}

	}

	public void recyle() {
		if (executorService != null) {
			executorService.shutdownNow();
			executorService = null;
			executorService = Executors.newFixedThreadPool(3);
			if (OtherCacheData.current().isDebugMode) {
				Log.e("AsyncImageLoader-recyle",
						"executorService.shutdownNow()");
			}
		}
		if (imageCache != null) {
			int size = imageCache.size();
			for (int i = 0; i < size; ++i) {
				try {
					Bitmap bitmap = imageCache.get(i).get();
					if (bitmap != null) {
						bitmap.recycle();
						bitmap = null;
					}
				} catch (Exception e) {
				}
			}
			imageCache.clear();
			imageCache = null;
		}
		System.gc();
	}
}
