package com.mv2studio.gesturerecorder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Fragment;
import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GestureReaderFragment extends BaseFragment {

	public static final String TYPE_TAG = "TYPE", GESTURE_DATA_TAG = "DATA";

	public static final int SHOW_TYPE = 1, SHOW_IDS = 2, SHOW_GESTURES = 3, SHOW_GESTURES_BY_ID = 4, SHOW_GESTURES_BY_TYPE = 5;

	GestureLibrary store;
	ArrayList<String> ids = new ArrayList<String>();
	ArrayList<NamedGesture> gestures = new ArrayList<GestureReaderFragment.NamedGesture>();

	GesturesAdapter gesturesAdapter;
	TypesAdapter typesAdapter;
	IDsAdapter idsAdapter;

	MainActivity activity;

	private int showType;
	private String gesturesID;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		activity = (MainActivity) getActivity();

		// get data for fragment
		Bundle args = getArguments();
		showType = args.getInt(TYPE_TAG);
		gesturesID = args.getString(GESTURE_DATA_TAG);

		// set up views
		View v = inflater.inflate(R.layout.fragment_gesture_reader, null);
		final ListView list = (ListView) v.findViewById(R.id.fragment_gesture_reader_list);

		// get gesture store
		store = GestureLibraries.fromFile(MainActivity.storeFile);

		// set up adapters
		switch (showType) {
		case SHOW_GESTURES_BY_ID:
		case SHOW_GESTURES_BY_TYPE:
		case SHOW_GESTURES:
			gesturesAdapter = new GesturesAdapter(getActivity());
			gesturesAdapter.setNotifyOnChange(false);
			list.setAdapter(gesturesAdapter);
			break;
		case SHOW_IDS:
			idsAdapter = new IDsAdapter(getActivity(), 0, ids);
			idsAdapter.setNotifyOnChange(false);
			list.setAdapter(idsAdapter);
			break;
		case SHOW_TYPE:
			typesAdapter = new TypesAdapter(getActivity(), 0, MainActivity.gestureTasks);
			list.setAdapter(typesAdapter);
			break;
		}

		// set listener on item click
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Fragment frag = null;
				Bundle args = new Bundle();
				switch (showType) {
				case SHOW_GESTURES_BY_ID:
				case SHOW_GESTURES_BY_TYPE:
				case SHOW_GESTURES:
					break;
				case SHOW_IDS:
					frag = new GestureReaderFragment();
					args.putInt(TYPE_TAG, SHOW_GESTURES_BY_ID);
					args.putString(GESTURE_DATA_TAG, idsAdapter.getItem(position));

					break;
				case SHOW_TYPE:
					frag = new GestureReaderFragment();
					args.putInt(TYPE_TAG, SHOW_GESTURES_BY_TYPE);
					args.putString(GESTURE_DATA_TAG, typesAdapter.getItem(position)[1]);
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

	public void onResume() {
		super.onResume();
		activity.showMenu(showType == SHOW_TYPE || showType == SHOW_IDS);
	}

	private class LoadDataTask extends AsyncTask<Void, Void, Void> {

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

							final Bitmap bitmap = gesture.toBitmap(200, 200, 0, getActivity().getResources().getColor(R.color.HoloRed));
							namedGesture.pic = new BitmapDrawable(bitmap);
							gesturesAdapter.add(namedGesture);
							continue;
						}

						gestures.add(namedGesture);
						String id = name.split("_")[1];
						if (!ids.contains(id))
							ids.add(id);
					}
				}
			} else
				Log.w("", "Could not load gestures file!");

			return null;
		}

		protected void onPostExecute(Void result) {
			switch (showType) {
			case SHOW_GESTURES_BY_ID:
			case SHOW_GESTURES_BY_TYPE:
			case SHOW_GESTURES:
				gesturesAdapter.notifyDataSetChanged();
				break;
			case SHOW_IDS:
				idsAdapter.notifyDataSetChanged();
				break;
			case SHOW_TYPE:
				typesAdapter.notifyDataSetChanged();
				break;
			}
			activity.showProgress(false);
		}
	}

	static class NamedGesture {
		String name;
		Gesture gesture;
		Drawable pic;
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
				convertView = mInflater.inflate(R.layout.gestures_item, parent, false);
			}

			final NamedGesture gesture = getItem(position);
			final TextView label = (TextView) convertView;
			label.setTypeface(tCond);

			label.setTag(gesture);
			label.setText(gesture.name);
			label.setCompoundDrawablesWithIntrinsicBounds(gesture.pic, null, null, null);

			return convertView;
		}
	}

}
