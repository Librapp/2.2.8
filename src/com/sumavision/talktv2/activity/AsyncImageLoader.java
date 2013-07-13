package com.sumavision.talktv2.activity;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
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
import com.sumavision.talktv2.utils.BitmapUtils;

/**
 * 
 * @author 郭鹏
 * @createTime
 * @description 异步加载图片
 * @changeLog changeBy姜浩 2013-3-17修改图片缓存 优化SD卡存储
 * 
 */
public class AsyncImageLoader {

	private HashMap<String, WeakReference<Drawable>> imageCache;

	public AsyncImageLoader() {
		recyle();
		imageCache = new HashMap<String, WeakReference<Drawable>>();
	}

	private ExecutorService executorService = Executors.newFixedThreadPool(3);

	public Drawable loadDrawable(final String imageUrl,
			final ImageCallback imageCallback) {
		if (imageCache.containsKey(imageUrl)) {
			WeakReference<Drawable> softReference = imageCache.get(imageUrl);
			Drawable drawable = softReference.get();
			if (drawable != null) {
				return drawable;
			}
		}

		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Drawable) message.obj, imageUrl);
			}
		};
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				Drawable drawable = loadImageFromUrl(imageUrl);
				imageCache.put(imageUrl, new WeakReference<Drawable>(drawable));
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
			}
		});

		return null;
	}

	private String getFileNname(String str) {

		String name;
		name = str.substring(str.lastIndexOf("/") + 1, str.length());

		return name;
	}

	private Drawable getSdCardFromDrawable(String url) {
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);

		if (sdCardExist) {
			String fileFolder = Environment.getExternalStorageDirectory()
					+ "/TVFan/temp";
			File dir = new File(fileFolder);
			if (!dir.exists()) {
				dir.mkdirs();
			} else {
				Drawable d = null;
				try {
					String name = getFileNname(url);
					d = Drawable.createFromPath(fileFolder + File.separator
							+ name);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
				}
				if (d != null) {
					return d;
				}
			}
		}
		return null;
	}

	private void saveDrawableToSdCard(Drawable drawble, String url) {
		boolean sdExists = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		if (sdExists) {
			try {
				String fileDir = Environment.getExternalStorageDirectory()
						+ "/TVFan/temp";
				String name = getFileNname(url);
				BitmapUtils.saveDrawableNew(drawble, fileDir, name);
			} catch (Exception e) {

			}
		}
	}

	public Drawable loadImageFromUrl(String url) {
		Drawable d = getSdCardFromDrawable(url);
		if (d != null) {
			imageCache.put(url, new WeakReference<Drawable>(d));
			return d;
		}
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
		Bitmap bitmap;
		if (!OtherCacheData.current().isLowSKIAVersion) {
			bitmap = BitmapFactory.decodeStream(i, null, null);
		} else {
			bitmap = BitmapFactory.decodeStream(new FlushedInputStream(i),
					null, null);
		}

		try {
			i.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		m = null;
		Drawable drawable = new BitmapDrawable(bitmap);
		saveDrawableToSdCard(drawable, url);
		return drawable;

	}

	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, String imageUrl);
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

	public void recyle() throws RuntimeException {
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
			imageCache.clear();
			imageCache = null;
		}
		System.gc();
	}

}
