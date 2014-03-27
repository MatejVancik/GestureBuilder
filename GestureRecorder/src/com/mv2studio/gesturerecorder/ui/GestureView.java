package com.mv2studio.gesturerecorder.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

public class GestureView extends View {

	Context context;
	private static final String TAG = "DrawView";
	private static final int SIZE = 60;

	private SparseArray<Path> mActivePointers;
	private Paint paint;
	private int[] colors = { Color.BLUE, Color.GREEN, Color.MAGENTA, Color.BLACK, Color.CYAN, Color.GRAY, Color.RED, Color.DKGRAY, Color.LTGRAY, Color.YELLOW };

	public GestureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		setFocusable(true);
		setFocusableInTouchMode(true);

		mActivePointers = new SparseArray<Path>();
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		// set painter color to a color you like
		paint.setColor(Color.BLUE);
		paint.setAntiAlias(true);
	    paint.setStrokeWidth(6f);
	    paint.setStyle(Paint.Style.STROKE);
	    paint.setStrokeJoin(Paint.Join.ROUND);

	}

	@Override
	protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);
		// draw all pointers
		for (int size = mActivePointers.size(), i = 0; i < size; i++) {
			Path point = mActivePointers.valueAt(i);
			if (point != null)
				paint.setColor(colors[i % 9]);
			canvas.drawPath(point, paint);
//			canvas.drawLine(point.prevX, point.prevY, point.x, point.y, mPaint);
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
			p.moveTo(x, y);
			
			mActivePointers.put(pointerId, p);
			break;
		}
		case MotionEvent.ACTION_MOVE: { // a pointer was moved
			for (int size = event.getPointerCount(), i = 0; i < size; i++) {
				Path point = mActivePointers.get(event.getPointerId(i));
				
				if (point != null) {
					float x = event.getX(i);
					float y = event.getY(i);
					point.lineTo(x, y);
				}
			}
			break;
		}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_CANCEL: {
//			mActivePointers.remove(pointerId);
//			break;
		}
		}
		invalidate();

		return true;
	}
	

}
