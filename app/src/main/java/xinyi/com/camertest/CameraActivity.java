package xinyi.com.camertest;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by wxy on 2017/8/14.
 */

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

	private float width = 60;
	private float height = 100;
	private Camera camera;
	private SurfaceView surfaceView;
	private SurfaceHolder surfaceHolder;
	private RectOnCamera rectOnCamera;
	private Button takePhoto;
	private String TAG="camera";

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_camera);
		surfaceView = (SurfaceView) findViewById(R.id.surface);
		rectOnCamera = (RectOnCamera) findViewById(R.id.rect);
		takePhoto= (Button) findViewById(R.id.take);
		initCamera();
		surfaceView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				camera.autoFocus(null);
				return false;
			}
		});

		//拍照
		takePhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				camera.autoFocus(new Camera.AutoFocusCallback() {
					@Override
					public void onAutoFocus(boolean b, Camera camera) {
						try {

							camera.takePicture(shutter, raw, jpeg);

						}catch (Exception e){
							e.printStackTrace();
						}
						//Toast.makeText(CameraActivity.this, "taken", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	private void initCamera() {
		if (checkCameraHardware(this)) {
			int i = Camera.getNumberOfCameras();
			if (i == 1) {
				Toast.makeText(this, "只有一个后置摄像头", Toast.LENGTH_SHORT).show();
			}
			try {
				camera = Camera.open(0); //获取Camera实例
			} catch (Exception e) {
				Log.d("camera", "open");
			}
		}
		surfaceHolder = surfaceView.getHolder();

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


		setCameraDisplayOrientation(this,0,camera);


		/*if(ori == mConfiguration.ORIENTATION_LANDSCAPE){
			camera.setDisplayOrientation(90);
			Toast.makeText(this, "横屏", Toast.LENGTH_SHORT).show();
		}else if(ori == mConfiguration.ORIENTATION_PORTRAIT){
			Toast.makeText(this, "竖屏", Toast.LENGTH_SHORT).show();
		}*/

/*
		Camera.Parameters parameters = camera.getParameters();
		int MAX_WIDTH;
		int MAX_HEIGHT;
		WindowManager wm = this.getWindowManager();
		MAX_WIDTH = wm.getDefaultDisplay().getWidth();
		MAX_HEIGHT = (wm.getDefaultDisplay().getHeight()/6)*5;
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
		surfaceView.setLayoutParams(new FrameLayout.LayoutParams(bestWidth, bestHeight));*/
		//camera.setParameters(parameters);
		try {

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

	/**
	 * 配置相机参数
	 *
	 * @return
	 */
	private Camera.Parameters settingParameters() {
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPictureFormat(PixelFormat.JPEG);
		camera.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		camera.autoFocus(new Camera.AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				if (success) {
				}
			}
		});

		return parameters;
	}



	// 拍照瞬间调用
	private Camera.ShutterCallback shutter = new Camera.ShutterCallback() {
		@Override
		public void onShutter() {
			Log.i(TAG,"shutter");
		}
	};

	// 获得没有压缩过的图片数据
	private Camera.PictureCallback raw = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera Camera) {
			Log.i(TAG, "raw");
		}
	};

	//创建jpeg图片回调数据对象
	private Camera.PictureCallback jpeg = new Camera.PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera Camera) {
			BufferedOutputStream bos = null;
			Bitmap bm = null;
			try {
				// 获得图片
				bm = BitmapFactory.decodeByteArray(data, 0, data.length);
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					Log.i(TAG, "Environment.getExternalStorageDirectory()="+Environment.getExternalStorageDirectory());
					String filePath =Environment.getExternalStorageDirectory().getPath()+"/testcamera/test.jpg";
					File file = new File(filePath);
					if (!file.getParentFile().exists()){
						file.getParentFile().mkdirs();
					}
					if (!file.exists()){
						file.createNewFile();
					}
					bos = new BufferedOutputStream(new FileOutputStream(file));
					bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩到流中
					setPictureDegreeZero(file.getAbsolutePath());
				}else{
					Toast.makeText(CameraActivity.this,"没有检测到内存卡", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					bos.flush();//输出
					bos.close();//关闭
					bm.recycle();// 回收bitmap空间
					camera.stopPreview();// 关闭预览
					camera.startPreview();// 开启预览
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				float x = event.getX();
				float y = event.getY();
				//有问题
				int width = 200;
				int height = 200;
				rectOnCamera.setXY(x,y);
				rectOnCamera.postInvalidate();

				break;
		}
		return true;
	}




	public static void setCameraDisplayOrientation(Activity activity,
												   int cameraId, android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info =
				new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
			case Surface.ROTATION_0: degrees = 0; break;
			case Surface.ROTATION_90: degrees = 90; break;
			case Surface.ROTATION_180: degrees = 180; break;
			case Surface.ROTATION_270: degrees = 270; break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
				result = ( info.orientation- degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
	}

	private void setPictureDegreeZero(String path) {
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			// 修正图片的旋转角度，设置其不旋转。这里也可以设置其旋转的角度，可以传值过去，
			// 例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
			exifInterface.setAttribute(String.valueOf(ExifInterface.ORIENTATION_ROTATE_90), "no");
			exifInterface.saveAttributes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
