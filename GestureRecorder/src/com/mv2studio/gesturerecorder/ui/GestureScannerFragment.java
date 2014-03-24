package com.mv2studio.gesturerecorder.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mv2studio.gesturerecorder.PictureUploadService;
import com.mv2studio.gesturerecorder.R;

public class GestureScannerFragment extends BaseFragment implements OnGestureListener, OnGesturePerformedListener {

	private static final float LENGTH_THRESHOLD = 120.0f;
	
	// Fragment views
	private TextView title, step, thx, yourID;
	private View thxLayout;
	private Button finish;
	private ImageButton next, prev, refresh;
	private GestureOverlayView overlay;
	
	// Gesutre related things
	private Gesture gesture;
	private GestureLibrary store;
	
	private SparseArray<Gesture> gestures = new SparseArray<Gesture>();
	
	private int items = MainActivity.gestureTasks.length;
	private int currentItem = 0;	
	private String gestureID = "";
	
	private static final String 
		SAVED_GESTURES = "GESTURES",
		SAVED_CURRENT_ITEM = "CURRENT",
		SAVED_CURRENT_ID = "ID";
		
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSparseParcelableArray(SAVED_GESTURES, gestures);
		outState.putInt(SAVED_CURRENT_ITEM, currentItem);
		outState.putString(SAVED_CURRENT_ID, gestureID);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		// GET SAVED STATE
		if(savedInstanceState != null) {
			gestures = savedInstanceState.getSparseParcelableArray(SAVED_GESTURES);
			currentItem = savedInstanceState.getInt(SAVED_CURRENT_ITEM);
			gestureID = savedInstanceState.getString(SAVED_CURRENT_ID);
		}
		
		if(gestureID.isEmpty())
			gestureID = String.valueOf(System.currentTimeMillis());
		
		
		View v = inflater.inflate(R.layout.fragment_gesture, null);
		
		overlay = (GestureOverlayView) v.findViewById(R.id.fragment_gesture_gesture);
		overlay.addOnGestureListener(this);
//		overlay.addOnGesturePerformedListener(this);
		
		final RotateAnimation rotate = new RotateAnimation(360, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotate.setDuration(300);
		
		final Animation anim_in  = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
		anim_in.setDuration(200);
		anim_in.setFillAfter(true);
		
		OnClickListener clickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				overlay.clear(false);
				switch(v.getId()) {
				case R.id.fragment_gesture_next:
					saveGesture();
					
					// FINISH!
					if(currentItem == items-1) {
						
						if(MainActivity.WORLD_EDITION) {
							sendAnswers();
							Bundle b = new Bundle();
							b.putString(SurveyFragment.ID_TAG, gestureID);
							SurveyFragment f = new SurveyFragment();
							f.setArguments(b);
							((MainActivity)getActivity()).replaceFragment(f, true);
						} else {
							thxLayout.startAnimation(anim_in);
						}
					}
					
					setGestureView(1);
					break;
				case R.id.fragment_gesture_prev:
					saveGesture();
					setGestureView(-1);
					break;
				case R.id.fragment_gesture_refresh:
					refresh.startAnimation(rotate);
					gestures.remove(currentItem);
					break;
					
				case R.id.fragmen_gesture_finish:
					sendAnswers();
					
					
					
					getFragmentManager().popBackStack();
					
					
					break;
				}
				gesture = null;
			}
		};
		
//		v.findViewById(R.id.fragmen_gesture_send).setOnClickListener(clickListener);
		
		next = (ImageButton) v.findViewById(R.id.fragment_gesture_next);
		next.setOnClickListener(clickListener);
		
		prev = (ImageButton) v.findViewById(R.id.fragment_gesture_prev);
		prev.setOnClickListener(clickListener);
		if(currentItem == 0)
			prev.setBackgroundResource(R.drawable.circle_gray_selector);
		
		refresh = (ImageButton) v.findViewById(R.id.fragment_gesture_refresh);
		refresh.setOnClickListener(clickListener);
		
		step = (TextView) v.findViewById(R.id.fragment_gesture_progress);
		step.setTypeface(tCondLight);
		step.setText((1+currentItem)+"/"+items);
		
		title = (TextView) v.findViewById(R.id.fragment_gesture_title_type);
		title.setTypeface(tCondBold);
		title.setText(MainActivity.gestureTasks[currentItem][0]);
		
		TextView topTitle = (TextView) v.findViewById(R.id.fragment_gesture_title);
		topTitle.setTypeface(tCond);
		
		thx = (TextView) v.findViewById(R.id.fragment_gesture_thanks);
		thx.setTypeface(tCondLight);
		
		thxLayout = v.findViewById(R.id.fragment_gesture_thanks_layout);
		
		yourID = (TextView) v.findViewById(R.id.fragment_gesture_your_id);
		yourID.setTypeface(tCondLight);
		yourID.setText("Vaše ID:\n"+gestureID);
		
		finish = (Button) v.findViewById(R.id.fragmen_gesture_finish);
		finish.setTypeface(tCondBold);
		finish.setOnClickListener(clickListener);
//		finish.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				getFragmentManager().popBackStack();
//			}
//		});
		
		if(currentItem == items - 1) {
			thxLayout.startAnimation(anim_in);
		}
		
		store = GestureLibraries.fromFile(MainActivity.storeFile);
		store.load();
		
		gesture = gestures.get(currentItem);
		
		// restore gesture if it's possible. Must be delayed to show
		// gesture on right place
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(gesture != null) {
					overlay.setGesture(gesture);
				}
			}
		}, 100)	;
			
		
		return v;
	}
	
	private void sendAnswers() {
		ArrayList<Gesture> gestureList = new ArrayList<Gesture>();
		for(int i = 0; i < items; i++) {
			gestureList.add(gestures.get(i));
		}
		
		Intent intent = new Intent(getActivity(), PictureUploadService.class);
		intent.putParcelableArrayListExtra(PictureUploadService.GESTURES_TAG, gestureList);
		intent.putExtra(PictureUploadService.ID_TAG, gestureID);
		getActivity().startService(intent);
	}
	
	private void setGestureView(int step) {
		currentItem += step;
		
		next.setBackgroundResource(R.drawable.circle_blue_selector);
		next.setImageResource(R.drawable.right);
		prev.setBackgroundResource(R.drawable.circle_blue_selector);

		if(currentItem >= items - 1) {
			currentItem = items - 1;
			next.setBackgroundResource(R.drawable.circle_green_selector);
			next.setImageResource(R.drawable.vv);
		} else if(currentItem <= 0) {
			currentItem = 0;
			prev.setEnabled(false);
			prev.setBackgroundResource(R.drawable.circle_gray_selector);
		} else {
			prev.setEnabled(true);
			next.setEnabled(true);
		}
		
		Gesture recreate = gestures.get(currentItem);
		if(recreate != null) overlay.setGesture(recreate);
		
		title.setText(MainActivity.gestureTasks[currentItem][0]);
		this.step.setText((currentItem+1)+"/"+items);
		
	}
	
	private void saveGesture() {
		System.out.println("going to save gesture");
		if(gesture == null) return;
		
		
		store.addGesture(MainActivity.gestureTasks[currentItem][1]+gestureID, gesture);
		store.save();
		System.out.println("gesture saved");
	}
	

	@Override
	public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
		gesture = null;
	}

	@Override
	public void onGesture(GestureOverlayView overlay, MotionEvent event) {}

	@Override
	public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
		gesture = overlay.getGesture();
		gestures.put(currentItem, gesture);
		 if (gesture.getLength() < LENGTH_THRESHOLD) {
             overlay.clear(false);
         }
	}

	@Override
	public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		gesture = overlay.getGesture();
		
	}
	
}
