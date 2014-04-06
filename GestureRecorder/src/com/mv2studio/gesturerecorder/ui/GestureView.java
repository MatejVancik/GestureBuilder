package com.mv2studio.gesturerecorder.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

public class GestureView extends View {

	private SparseArray<Path> gesturePaths;
	private long lastUsed;
	private static long TIME_DIFF = 1000;
	private Point p;
	private OnGestureDoneListener gestureDoneListener;
	
	
	private Paint paint;
	private int[] colors = { Color.BLUE, Color.GREEN, Color.MAGENTA, Color.BLACK, Color.CYAN, Color.GRAY, Color.RED, Color.DKGRAY, Color.LTGRAY, Color.YELLOW };

	public GestureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(true);
		setFocusableInTouchMode(true);

		gesturePaths = new SparseArray<Path>();
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLUE);
		paint.setAntiAlias(true);
	    paint.setStrokeWidth(6f);
	    
	    paint.setStyle(Paint.Style.STROKE);
	    paint.setStrokeJoin(Paint.Join.ROUND);

	}
	
	public void setGestureTimeout(long timeInMillis) {
		TIME_DIFF = timeInMillis;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// draw all pointers
		for (int size = gesturePaths.size(), i = 0; i < size; i++) {
			Path path = gesturePaths.valueAt(i);
			if (path != null) {
				paint.setColor(colors[i % 9]);
			}
			canvas.drawPath(path, paint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// get pointer index from the event object
		int pointerIndex = event.getActionIndex();

		// get pointer ID
		int pointerId = event.getPointerId(pointerIndex);

		// get masked (not specific to a pointer) action
		int maskedAction = event.getActionMasked();

		switch (maskedAction) {

		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN: {
			// We have a new pointer. Lets add it to the list of pointers
			float x = event.getX(pointerIndex);
			float y = event.getY(pointerIndex);
			
			Path p = new Path();
			if(TIME_DIFF > (System.currentTimeMillis() - lastUsed)) {
				p = gesturePaths.get(pointerId);
			} else if ((TIME_DIFF < (System.currentTimeMillis() - lastUsed)) && pointerId == 0) gesturePaths.clear();
			
			if(p == null) p = new Path();
			p.moveTo(x, y);
			
			gesturePaths.put(pointerId, p);
			paint.setColor(Color.LTGRAY);
			break;
		}
		case MotionEvent.ACTION_MOVE: { // a pointer was moved
			for (int size = event.getPointerCount(), i = 0; i < size; i++) {
				Path point = gesturePaths.get(event.getPointerId(i));
				
				if (point != null) {
					float x = event.getX(i);
					float y = event.getY(i);
					point.lineTo(x, y);
				}
			}
			break;
		}
		case MotionEvent.ACTION_UP:
			lastUsed = System.currentTimeMillis();
			if(gestureDoneListener != null)
				gestureDoneListener.onDone();
		case MotionEvent.ACTION_POINTER_UP:
			try {
				Path point = gesturePaths.get(pointerId);
				
				if (point != null) {
					point.addCircle(event.getX(pointerIndex), event.getY(pointerIndex), 5, Path.Direction.CW);
				}
			} catch (IllegalArgumentException ex ) {ex.printStackTrace();}
			
		case MotionEvent.ACTION_CANCEL: 
			break;
		}
		invalidate();

		return true;
	}
	
	public Gesture getGesture() {
		Gesture g = new Gesture();
		Bitmap b = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		draw(c);
		
		g.bitmap = Bitmap.createScaledBitmap(b, getWidth()/2, getHeight()/2, true);
		g.paths = gesturePaths.clone();
		return g;
	}
	
	public void refreshGesture() {
		gesturePaths.clear();
		invalidate();
	}
	
	public void setGesture(Gesture gesture) {
		gesturePaths.clear();
		if(gesture == null) {
			invalidate(); return;
		}
		gesturePaths = gesture.paths;
		invalidate();
	}
	
	public void setOnGestureDoneListener(OnGestureDoneListener listener) {
		gestureDoneListener = listener;
	}
	
	public static class Gesture {
		Bitmap bitmap;
		SparseArray<Path> paths;
		String name;
		int rating = 0;
	}
	
	public interface OnGestureDoneListener{
		public void onDone();
	}
}
