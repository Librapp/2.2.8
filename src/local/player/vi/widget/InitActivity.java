package local.player.vi.widget;

import io.vov.vitamio.Vitamio;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import com.sumavision.talktv2.R;

public class InitActivity extends Activity {
	public static final String FROM_ME = "fromVitamioInitActivity";
	public static final String EXTRA_MSG = "EXTRA_MSG";
	public static final String EXTRA_FILE = "EXTRA_FILE";
	private ProgressDialog mPD;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		new AsyncTask<Object, Object, Object>() {
			protected void onPreExecute() {
				mPD = new ProgressDialog(InitActivity.this);
				mPD.setCancelable(false);
				mPD.setMessage(getText(R.string.init_decoders));
				mPD.show();
		}

			@Override
			protected Object doInBackground(Object... params) {

				if (Vitamio.isInitialized(getApplicationContext()))
					return null;

				try {
					// TOOD:新播放器
					Class c = Class.forName("io.vov.vitamio.Vitamio");
					Method extractLibs = c.getDeclaredMethod("extractLibs",
							new Class[] { android.content.Context.class,
									int.class });
					extractLibs.setAccessible(true);
					extractLibs.invoke(c, new Object[] {
							getApplicationContext(), R.raw.libarm });
				} catch (NoSuchMethodException e) {
					// Log.e("InitActivity", "extractLibs", e);
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				uiHandler.sendEmptyMessage(0);
				return null;
			}
		}.execute();
	}

	private Handler uiHandler = new Handler() {
		public void handleMessage(Message msg) {
			mPD.dismiss();
			Intent src = getIntent();
			Intent i = new Intent();
			i.setClassName(src.getStringExtra("package"),
					src.getStringExtra("className"));
			i.setData(src.getData());
			i.putExtras(src);
			i.putExtra(FROM_ME, true);
			startActivity(i);
			finish();
		}
	};
}
