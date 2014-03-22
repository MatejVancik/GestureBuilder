package com.mv2studio.gesturerecorder;

import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GestureReaderFragment extends BaseReaderFragment {
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// set up views
		View v = inflater.inflate(R.layout.fragment_gesture_reader, null);
		final ListView list = (ListView) v.findViewById(R.id.fragment_gesture_reader_list);

		// set up adapters
		switch (showType) {
		case SHOW_IDS:
			adapter = new IDsAdapter(getActivity(), 0, ids);
			adapter.setNotifyOnChange(false);
			break;
		case SHOW_TYPE:
			adapter = new TypesAdapter(getActivity(), 0, MainActivity.gestureTasks);
			break;
		}
		
		// add adapter to list
		list.setAdapter(adapter);
		
		// set listener on item click
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Fragment frag = new GestureReaderDetailFragment();
				Bundle args = new Bundle();
				switch (showType) {
				case SHOW_IDS:
					args.putInt(TYPE_TAG, SHOW_GESTURES_BY_ID);
					args.putString(GESTURE_DATA_TAG, (String)adapter.getItem(position));

					break;
				case SHOW_TYPE:
					args.putInt(TYPE_TAG, SHOW_GESTURES_BY_TYPE);
					args.putString(GESTURE_DATA_TAG, ((String[])adapter.getItem(position))[1]);
					break;
				}

				// switch fragment
				if (frag != null) {
					frag.setArguments(args);
					activity.replaceFragment(frag, true);
				}
			}
		});

		// load data in background
		new LoadDataTask().execute();

		return v;
	}

	

	private class TypesAdapter extends ArrayAdapter<String[]> {
		private LayoutInflater inflater;

		public TypesAdapter(Context context, int layout, String[][] data) {
			super(context, layout, data);
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.gestures_item, parent, false);
			}

			TextView label = (TextView) convertView;
			label.setTypeface(tCond);
			
			String name = getItem(position)[0];

			label.setText(name);

			return convertView;
		}

	}

	private class IDsAdapter extends ArrayAdapter<String> {
		private LayoutInflater inflater;

		public IDsAdapter(Context context, int layout, List<String> data) {
			super(context, layout, data);
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.gestures_item, parent, false);
			}

			TextView label = (TextView) convertView;
			label.setTypeface(tCond);

			String name = getItem(position);

			label.setText(name);

			return convertView;
		}
	}



}
