package com.mv2studio.gesturerecorder.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mv2studio.gesturerecorder.CommonHelper;
import com.mv2studio.gesturerecorder.Prefs;
import com.mv2studio.gesturerecorder.R;

public class StartFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final MainActivity activity = (MainActivity) getActivity();
		
		View v = inflater.inflate(R.layout.fragment_start, null);
		

		final RotateAnimation rotate = new RotateAnimation(360, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotate.setDuration(300);
		
		
		final View settings = v.findViewById(R.id.fragment_start_settings_layout);
		final CheckBox box = (CheckBox) v.findViewById(R.id.settings_survey);
		box.setChecked(Prefs.getBoolValue(MainActivity.SHOW_SURVEY_TAG, getActivity()));
		box.setTypeface(tCond);
		final CheckBox netBox = (CheckBox) v.findViewById(R.id.settings_internet);
		netBox.setChecked(Prefs.getBoolValue(MainActivity.INTERNET_SWITCH, getActivity()));
		netBox.setTypeface(tCond);
		
		final EditText edit = (EditText) v.findViewById(R.id.settings_time);
		int time = Prefs.getIntValue(MainActivity.GESTURE_TIME_MILLIS_TAG, getActivity());
		if(time == -1) time = 1000;
		edit.setText(""+time);
		edit.setTypeface(tCondBold);
		
		OnClickListener clickListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switch(v.getId()) {
				case R.id.fragment_start_start:
					
					if(!CommonHelper.isOnline(getActivity()) && Prefs.getBoolValue(MainActivity.INTERNET_SWITCH, getActivity())) {
						Toast.makeText(getActivity(), getString(R.string.no_internet), Toast.LENGTH_LONG).show();
						return;
					}
					
					activity.replaceFragment(new GestureScannerFragment(), true);
					break;
					
				case R.id.fragment_start_browse:
					GestureReaderFragment fragment = new GestureReaderFragment();
					Bundle args = new Bundle();
					args.putInt(GestureReaderFragment.TYPE_TAG, GestureReaderFragment.SHOW_TYPE);
					fragment.setArguments(args);
					activity.replaceFragment(fragment, true);
					break;
				case R.id.settings_save:
					int time = 1000;
					try {
						time = Integer.valueOf(edit.getText().toString());
					} catch (Exception e) {}
					Prefs.storeIntValue(MainActivity.GESTURE_TIME_MILLIS_TAG, time, getActivity());
					Prefs.storeBoolValue(MainActivity.SHOW_SURVEY_TAG, box.isChecked(), getActivity());
					Prefs.storeBoolValue(MainActivity.INTERNET_SWITCH, netBox.isChecked(), getActivity());
				case R.id.fragment_start_settings:
					settings.setVisibility(settings.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
					
					break;
				}
			}
		};
		
		((MainActivity)getActivity()).setOnSettingsListener(clickListener);
		((MainActivity)getActivity()).setMenu(MainActivity.MENU_START);
		
		TextView timeTitle = (TextView) v.findViewById(R.id.settings_time_itle);
		timeTitle.setTypeface(tCondBold);
		
		Button save = (Button) v.findViewById(R.id.settings_save);
		save.setOnClickListener(clickListener);
		save.setTypeface(tCondBold);
		
		Button start = (Button) v.findViewById(R.id.fragment_start_start);
		start.setOnClickListener(clickListener);
		start.setTypeface(tThin);
		
		Button browse = (Button) v.findViewById(R.id.fragment_start_browse);
		browse.setOnClickListener(clickListener);
		browse.setTypeface(tCondBold);

		if(MainActivity.WORLD_EDITION) browse.setVisibility(View.GONE);
		return v;
	}
	
}
