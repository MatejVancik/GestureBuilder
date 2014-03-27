package com.mv2studio.gesturerecorder.ui;

import java.util.ArrayList;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.mv2studio.gesturerecorder.R;

public class BaseReaderFragment extends BaseFragment {

	public static final String TYPE_TAG = "TYPE", 
							   GESTURE_DATA_TAG = "DATA";
	
	public static final int SHOW_TYPE = 1, 
							SHOW_IDS = 2, 
							SHOW_GESTURES = 3, 
							SHOW_GESTURES_BY_ID = 4, 
							SHOW_GESTURES_BY_TYPE = 5;

	ArrayAdapter adapter;
	ArrayList<NamedGesture> gestures = new ArrayList<NamedGesture>();
	ArrayList<String> ids = new ArrayList<String>();
	protected String gesturesID;
	
	GestureLibrary store;

	MainActivity activity;
	protected int showType;	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
		
		// get data for fragment
		showType = getArguments().getInt(TYPE_TAG);
		
		// get gesture store
		store = GestureLibraries.fromFile(MainActivity.storeFile);
		((MainActivity)getActivity()).setMenu(MainActivity.MENU_BROWSER);
	}	
	
	protected static class NamedGesture {
		String name;
		Gesture gesture;
		Drawable pic;
	}
	
	
	protected class LoadDataTask extends AsyncTask<Void, Void, Void> {

		ArrayList<NamedGesture> data = new ArrayList<BaseReaderFragment.NamedGesture>();
		
		@Override
		protected void onPreExecute() {
			activity.showProgress(true);
		}

		protected Void doInBackground(Void... params) {
			if (store.load()) {
				for (String name : store.getGestureEntries()) {
					for (Gesture gesture : store.getGestures(name)) {
						final NamedGesture namedGesture = new NamedGesture();
						namedGesture.gesture = gesture;
						namedGesture.name = name;
						
						// need only gestures matching ID!
						if ((showType == SHOW_GESTURES_BY_ID && name.endsWith(gesturesID))
								|| (showType == SHOW_GESTURES_BY_TYPE && name.startsWith(gesturesID))) {

							final Bitmap bitmap = gesture.toBitmap(256, 256, 0, getActivity().getResources().getColor(R.color.HoloRed));
							namedGesture.pic = new BitmapDrawable(bitmap);
							data.add(namedGesture);
							continue;
						}

						gestures.add(namedGesture);
						String id = name.split("_")[1];
						if (!ids.contains(id))
							ids.add(id);
					}
				}
			} else Log.w("", "Could not load gestures file!");

			return null;
		}

		protected void onPostExecute(Void result) {
			adapter.addAll(data);
			adapter.notifyDataSetChanged();
			activity.showProgress(false);
		}
	}
	
	
}
