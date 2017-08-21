package xinyi.com.camertest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by wxy on 2017/8/15.
 */

public class RectOnCamera extends View {

	private static final String TAG = "CameraSurfaceView";
	private Paint mPaint;
	private float x;
	private float y;

	public RectOnCamera(Context context) {
		this(context, null);
	}

	public RectOnCamera(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RectOnCamera(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		getScreenMetrix(context);

	}

	private void getScreenMetrix(Context context) {
		WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		WM.getDefaultDisplay().getMetrics(outMetrics);
		x = outMetrics.widthPixels/2;
		y = outMetrics.heightPixels/2;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}


	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (changed){
			layout(left,top,right,bottom);
		}
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);// 抗锯齿
		mPaint.setDither(true);// 防抖动
		mPaint.setColor(Color.RED);
		mPaint.setStrokeWidth(2);
		mPaint.setStyle(Paint.Style.STROKE);// 空心
		canvas.drawRect(x -100,y-100,x+100,y+100,mPaint);
		Log.i(TAG, "onDraw");
	}

	public void setXY(float x,float y){
		this.x=x;
		this.y=y;
	}
}
