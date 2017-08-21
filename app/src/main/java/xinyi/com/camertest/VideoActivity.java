package xinyi.com.camertest;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

/**
 * Created by wxy on 2017/8/15.
 * 录制音视频
 */

 public class VideoActivity extends AppCompatActivity implements SurfaceHolder.Callback {


	private Camera mCamera;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private MediaRecorder mMediaRecorder;
	private Button button1;
	private Button button2;
	private static final SparseIntArray orientations = new SparseIntArray();//手机旋转对应的调整角度

	static {
		orientations.append(Surface.ROTATION_0, 90);
		orientations.append(Surface.ROTATION_90, 0);
		orientations.append(Surface.ROTATION_180, 270);
		orientations.append(Surface.ROTATION_270, 180);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_viedeo);
		button1= (Button) findViewById(R.id.button1);
		button2= (Button) findViewById(R.id.button2);
		mSurfaceView= (SurfaceView) findViewById(R.id.video_view);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		openCamera();

		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mMediaRecorder.stop();
				mMediaRecorder.release();
				mMediaRecorder=null;
			}
		});
		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				prepareVideoRecorder();
			}
		});

	}
	private void openCamera() {
		if (checkCameraHardware(this)) {
			int i = Camera.getNumberOfCameras();
			if (i == 1) {
				Toast.makeText(this, "只有一个后置摄像头", Toast.LENGTH_SHORT).show();
			}
			try {
				mCamera = Camera.open(0); //获取Camera实例
			} catch (Exception e) {
				Log.d("camera", "open");
			}

			Camera.Parameters p=mCamera.getParameters();
			Camera.Size csize = null;
			List<Camera.Size> vSizeList = mCamera.getParameters().getSupportedPreviewSizes();
			for (int num = 0; num < vSizeList.size(); num++) {
				Camera.Size size = vSizeList.get(num);
				if (size.width >= 800 && size.height >= 480) {
					csize = size;
					break;
				}
			}
			p.setPreviewSize(csize.width, csize.height);
			mSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(csize.width, csize.height));

			List<String> focusModesList = p.getSupportedFocusModes();
			//增加对聚焦模式的判断
			if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
				p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
			} else if (focusModesList.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
				p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			}
			mCamera.setParameters(p);

			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			int orientation = orientations.get(rotation);
			mCamera.setDisplayOrientation(orientation);

		}
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
		//camera.setParameters(settingParameters());
		try {
			mCamera.setPreviewDisplay(surfaceHolder);
			mCamera.startPreview();
		} catch (IOException e) {
			Log.d("camera", "videoStart");
		}
		//prepareVideoRecorder

	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

	}

	private boolean prepareVideoRecorder(){

		mMediaRecorder = new MediaRecorder();

		// Step 1: Unlock and set camera to MediaRecorder
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);


		// Step 2: Set sources
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);//不懂
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 不懂

		//mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		//设置文件的输出格式
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//aac_adif， aac_adts， output_format_rtp_avp， output_format_mpeg2ts ，webm
		//设置audio的编码格式
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		//设置video的编码格式
		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

		//设置录制的视频编码比特率
		mMediaRecorder.setVideoEncodingBitRate(1024 * 1024);


		//设置记录会话的最大持续时间（毫秒）
		mMediaRecorder.setMaxDuration(60 * 1000);
		// Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
		//mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
		/*mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);*/

		// Step 4: Set output file
		mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

		// Step 5: Set the preview output
		mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());

		// Step 6: Prepare configured MediaRecorder
		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();
		} catch (IllegalStateException e) {
			Log.d("video", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d("video", "IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		/*File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "MyCameraApp");*/
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		String mediaStorageDir=Environment.getExternalStorageDirectory().getAbsolutePath()+"/MyCameraApp";
		File file=new File(mediaStorageDir);
		if (! file.exists()){
			if (! file.mkdirs()){
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE){
			mediaFile = new File(file.getPath() + File.separator +
					"IMG_"+ timeStamp + ".jpg");
		} else if(type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(file.getPath() + File.separator +
					"VID_"+ timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}
	private void releaseMediaRecorder() {
		mMediaRecorder.stop();
		mMediaRecorder.release();
	}
}
