package com.example.words;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.graphics.*;
import android.util.AttributeSet;

public class PaintView extends View {
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Paint   mBitmapPaint;
    private Paint       mPaint;
    private float lastX, lastY;

	public PaintView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		Init();
	}
	public PaintView(Context context, AttributeSet attrs)
	{
	    super(context, attrs);
	    Init();
	} 

	private void Init()
	{

		mBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFF000000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(5);
	
	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom)
	{
		Bitmap oldBitmap = mBitmap;
		Rect src = new Rect(0,0, oldBitmap.getWidth(), oldBitmap.getHeight());
		RectF dst = new RectF(0,0,right-left,bottom-top);
		mBitmap = Bitmap.createBitmap(right-left, bottom-top, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawBitmap(oldBitmap, src, dst, mBitmapPaint);
        oldBitmap.recycle();
	}
	@Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(0xFFFFFFFF);
        
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        
    }
	public void Clear(){
		mBitmap.eraseColor(0xffffff);
		invalidate();
	}
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
	        case MotionEvent.ACTION_DOWN:
	        	lastX = x;
	        	lastY = y;
	            break;
	        case MotionEvent.ACTION_MOVE:
	        	mCanvas.drawLine(lastX, lastY, x,y, mPaint);
	        	lastX = x;
	        	lastY = y;
	            invalidate();
	            break;
	        case MotionEvent.ACTION_UP:
	        	mCanvas.drawLine(lastX, lastY, x,y, mPaint);
	            invalidate();
	            break;
        }
        return true;
    }
}
