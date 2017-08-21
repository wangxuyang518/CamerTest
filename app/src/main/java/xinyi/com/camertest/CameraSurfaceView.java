package xinyi.com.camertest;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wxy on 2017/8/14.
 */

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	float x;
	float y;
	private Camera camera;
	private SurfaceHolder surfaceHolder;
	private Context context;
	private float width = 60;
	private float height = 100;
	private Canvas canvas;

	public CameraSurfaceView(Context context) {
		super(context);
	}

	public CameraSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initCamera();
	}

	private void initCamera() {
		if (checkCameraHardware(context)) {
			int i = Camera.getNumberOfCameras();
			if (i == 1) {
				Log.d("camera", "1");
			}
			try {
				camera = Camera.open(1); //获取Camera实例
			} catch (Exception e) {
				Log.d("camera", "open");
			}
		}
		surfaceHolder = this.getHolder();
		surfaceHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	private boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		camera.setParameters(settingParameters());
		try {
			camera.setPreviewDisplay(surfaceHolder);
			camera.startPreview();
		} catch (IOException e) {
			Log.d("camera", "setPreviewDisplay");
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
		if (surfaceHolder.getSurface() == null) {
			return;
		}
		try {
			camera.stopPreview();
		} catch (Exception e) {
		}

		Camera.Parameters parameters = camera.getParameters();
		int MAX_WIDTH;
		int MAX_HEIGHT;
		WindowManager wm = ((Activity) context).getWindowManager();
		MAX_WIDTH = wm.getDefaultDisplay().getWidth();
		MAX_HEIGHT = wm.getDefaultDisplay().getHeight();
		int bestWidth = 0;
		int bestHeight = 0;
		List<Camera.Size> sizeList = camera.getParameters().getSupportedPreviewSizes();
		if (sizeList.size() > 1) {
			Iterator<Camera.Size> itor = sizeList.iterator();
			while (itor.hasNext()) {
				Camera.Size cur = itor.next();
				if (cur.width >= bestWidth && cur.height >= bestHeight && cur.width <= MAX_WIDTH && cur.height <= MAX_HEIGHT) {
					bestWidth = cur.width;
					bestHeight = cur.height;
				}
			}
			if (bestWidth != 0 && bestHeight != 0) {
				parameters.setPreviewSize(bestWidth, bestHeight);
			}
		} else {
			parameters.setPreviewSize(sizeList.get(0).width, sizeList.get(0).height);
		}
		this.setLayoutParams(new FrameLayout.LayoutParams(bestWidth, bestHeight));
		camera.setParameters(parameters);
		try {
			camera.setPreviewDisplay(surfaceHolder);
			camera.startPreview();
		} catch (Exception e) {
			Log.d("camera", "Error starting camera preview: " + e.getMessage());
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		if (camera != null) {
			camera.stopPreview();
			camera.release();
		}
	}

	/*@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		paint.setStyle(Paint.Style.STROKE);
		float top = y - height / 2;
		float bottom = y+ height / 2;
		float left=x-width/2;
		float right=x+width/2;
		if (top<=0||left<=0)
			return;
		canvas.drawRect(left,top,right,bottom,paint);
	}*/

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			x = event.getX();
			y = event.getY();
			Paint paint = new Paint();
			paint.setColor(Color.RED);
			paint.setStyle(Paint.Style.STROKE);
			canvas = this.surfaceHolder.lockCanvas(); // 通过lockCanvas加锁并得到該SurfaceView的画布
			float top = y - height / 2;
			float bottom = y+ height / 2;
			float left=x-width/2;
			float right=x+width/2;
			if (top<=0||left<=0)
				return false;
			canvas.drawRect(left,top,right,bottom,paint);
			this.surfaceHolder.unlockCanvasAndPost(canvas); // 释放锁并提交画布进行重绘
			return true;
		}else {
			return false;
		}
	}



	/**
	 * 配置相机参数
	 *
	 * @return
	 */
	private Camera.Parameters settingParameters() {
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPictureFormat(PixelFormat.JPEG);
		camera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_RED_EYE);
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		parameters.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
		camera.autoFocus(new Camera.AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				if (success) {
					Log.d("11111", "1111");
				}
			}
		});

		camera.setParameters(parameters);
		camera.startPreview();
		return parameters;
	}
}
