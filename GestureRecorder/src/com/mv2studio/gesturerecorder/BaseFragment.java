package com.mv2studio.gesturerecorder;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

public class BaseFragment extends Fragment {
	protected Typeface tLight, tCond, tCondBold, tCondLight, tThin;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Context c = getActivity();
		tThin = Typeface.createFromAsset(c.getAssets(), "fonts/Roboto-Thin.ttf");
		tCondLight = Typeface.createFromAsset(c.getAssets(), "fonts/Roboto-CondensedLight.ttf");
		tLight = Typeface.createFromAsset(c.getAssets(), "fonts/Roboto-Light.ttf");
		tCond = Typeface.createFromAsset(c.getAssets(), "fonts/Roboto-Condensed.ttf");
		tCondBold = Typeface.createFromAsset(c.getAssets(), "fonts/Roboto-BoldCondensed.ttf");
		super.onCreate(savedInstanceState);
	}
	
	
}
