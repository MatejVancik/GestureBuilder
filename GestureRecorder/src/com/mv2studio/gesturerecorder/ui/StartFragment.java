package com.mv2studio.gesturerecorder.ui;

import com.mv2studio.gesturerecorder.R;
import com.mv2studio.gesturerecorder.R.id;
import com.mv2studio.gesturerecorder.R.layout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class StartFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final MainActivity activity = (MainActivity) getActivity();
		
		View v = inflater.inflate(R.layout.fragment_start, null);
		OnClickListener clickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch(v.getId()) {
				case R.id.fragment_start_start:
					activity.replaceFragment(new GestureScannerFragment(), true);
					break;
					
				case R.id.fragment_start_browse:
					GestureReaderFragment fragment = new GestureReaderFragment();
					Bundle args = new Bundle();
					args.putInt(GestureReaderFragment.TYPE_TAG, GestureReaderFragment.SHOW_TYPE);
					fragment.setArguments(args);
					activity.replaceFragment(fragment, true);
					break;
				}
			}
		};
		
		Button start = (Button) v.findViewById(R.id.fragment_start_start);
		start.setOnClickListener(clickListener);
		start.setTypeface(tThin);
		
		Button browse = (Button) v.findViewById(R.id.fragment_start_browse);
		browse.setOnClickListener(clickListener);
		browse.setTypeface(tCondBold);

		if(MainActivity.WORLD_EDITION) browse.setVisibility(View.GONE);
		
		return v;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		((MainActivity)getActivity()).showMenu(false);
	}
	
}
