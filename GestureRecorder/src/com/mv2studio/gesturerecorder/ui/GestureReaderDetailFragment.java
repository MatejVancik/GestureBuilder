package com.mv2studio.gesturerecorder.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.mv2studio.gesturerecorder.R;
import com.mv2studio.gesturerecorder.R.drawable;
import com.mv2studio.gesturerecorder.R.id;
import com.mv2studio.gesturerecorder.R.layout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class GestureReaderDetailFragment extends BaseReaderFragment {

	int zoom = 2;
	HashMap<String, String> dictionary = new HashMap<String, String>();
	
	private static final String ZOOM_LEVEL_TAG = "ZOOM";
	private ImageButton plusButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_gesture_reader_grid, null);
		final GridView grid = (GridView) v.findViewById(R.id.fragment_gesture_reader_grid_grid);
		
		zoom = getArguments().getInt(ZOOM_LEVEL_TAG, 2);
		grid.setNumColumns(zoom);
		
		for(String[] str: MainActivity.gestureTasks) {
			dictionary.put(str[1], str[0]);
		}
		
		gesturesID = getArguments().getString(GESTURE_DATA_TAG);
		
		adapter = new GesturesAdapter(getActivity());
		adapter.setNotifyOnChange(false);
		grid.setAdapter(adapter);
		
		OnClickListener clickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch(v.getId()) {
				case R.id.fragment_gesture_reader_grid_minus:
					zoom++;
					plusButton.setBackgroundResource(R.drawable.circle_blue_selector);
					break;
				case R.id.fragment_gesture_reader_grid_plus:
					if(1 >= --zoom) { 
						zoom = 1;
						plusButton.setBackgroundResource(R.drawable.circle_gray_selector);
					}
					break;
				}
				grid.setNumColumns(zoom);
				adapter.notifyDataSetChanged();
			}
		};
		
		v.findViewById(R.id.fragment_gesture_reader_grid_minus).setOnClickListener(clickListener);
		plusButton = (ImageButton) v.findViewById(R.id.fragment_gesture_reader_grid_plus);
		plusButton.setOnClickListener(clickListener);
		
		// load data in background
		new LoadDataTask().execute();
		
		return v;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(ZOOM_LEVEL_TAG, zoom);
		super.onSaveInstanceState(outState);
	}
	
	
	private class GesturesAdapter extends ArrayAdapter<NamedGesture> {
		private final LayoutInflater mInflater;
		private final Map<String, Drawable> mThumbnails = Collections.synchronizedMap(new HashMap<String, Drawable>());

		public GesturesAdapter(Context context) {
			super(context, 0);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.gesture_grid_item, parent, false);
			}

			final NamedGesture gesture = getItem(position);
			final ImageView image = (ImageView) convertView.findViewById(R.id.gesture_grid_item_image);
			image.setImageDrawable(gesture.pic);
			
			
			String name = gesture.name;
			String[] splitedName = name.split("_");
			if(showType == SHOW_GESTURES_BY_ID) name = dictionary.get(splitedName[0]+"_");
			else name = splitedName[1];
			
			final TextView label = (TextView) convertView.findViewById(R.id.gesture_grid_item_text);
			label.setTypeface(tCond);
			label.setText(name);

			return convertView;
		}
		
		@Override
		public void notifyDataSetChanged() {
			
			
			super.notifyDataSetChanged();
		}
		
	}
	
}
