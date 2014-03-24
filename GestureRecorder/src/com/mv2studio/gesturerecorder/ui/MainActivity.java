package com.mv2studio.gesturerecorder.ui;

import java.io.File;

import com.mv2studio.gesturerecorder.Prefs;
import com.mv2studio.gesturerecorder.R;
import com.mv2studio.gesturerecorder.R.drawable;
import com.mv2studio.gesturerecorder.R.id;
import com.mv2studio.gesturerecorder.R.layout;
import com.mv2studio.gesturerecorder.R.menu;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.TaskStackBuilder;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.Window;

public class MainActivity extends Activity {

	public static final File storeFile = new File(Environment.getExternalStorageDirectory(), "gestures");

	public static String[][] gestureTasks = { 
		{ "cyklus", "LOOP_" }, 
		{ "podmienku", "IF_" }, 
		{ "deklaráciu premennej", "DEC_" }, 
		{ "komentár", "COM_" },
		{ "vymazanie riadku", "ROW_" },
		{ "zobrazenie bloku", "BL_" } 
	};

	private boolean menuShowingType;
	private int menuRes = R.menu.empty;
	private boolean firstLoad = true;
	private String FIRST_LOAD_TAG = "LOAD";
	
	public static boolean WORLD_EDITION = true;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(FIRST_LOAD_TAG, false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// put progressbar to action bar 
		// requestWindowFeature MUST BE CALLED BEFORE SETTING CONTENTVIEW!
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		
		// get saved variables from previous activity instance
		if (savedInstanceState != null) {
			firstLoad = savedInstanceState.getBoolean(FIRST_LOAD_TAG);
		}
		

		// set fragment and it's arguments. if it's first time activity loads
		// put fragment, otherwise let system recreate previous fragment
		if (firstLoad)
//			replaceFragment(new SurveyFragment(), false);
			replaceFragment(new StartFragment(), false);
		
		
		menuShowingType = true;
	}

	private void setAdminFrag(int type) {
		GestureReaderFragment fragment = new GestureReaderFragment();
		Bundle args = new Bundle();
		args.putInt(GestureReaderFragment.TYPE_TAG, type);
		fragment.setArguments(args);
		replaceFragment(fragment, true);
	}

	public void replaceFragment(Fragment fragment, boolean toBackStack) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				.replace(R.id.activity_main_content, fragment);
		if (toBackStack)
			transaction.addToBackStack("");
		transaction.commit();
	}

	public void showProgress(boolean toShow) {
		setProgressBarIndeterminateVisibility(toShow);
	}

	public void showMenu(boolean toShow) {
		menuRes = toShow ? R.menu.menu : R.menu.empty;
		invalidateOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(menuRes, menu);
		if (menuRes == R.menu.empty)
			return super.onCreateOptionsMenu(menu);

		MenuItem menuButton = menu.findItem(R.id.menu_gesture_reader_types);
		if (menuShowingType) {
			menuButton.setTitle("podľa id");
			menuButton.setIcon(R.drawable.order_id);
		} else {
			menuButton.setTitle("podľa typu");
			menuButton.setIcon(R.drawable.order_type);
		}

		menuButton.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if (menuShowingType) {
					getFragmentManager().popBackStack();
					setAdminFrag(GestureReaderFragment.SHOW_IDS);
				} else {
					getFragmentManager().popBackStack();
					setAdminFrag(GestureReaderFragment.SHOW_TYPE);
				}
				menuShowingType = !menuShowingType;
				invalidateOptionsMenu();
				return false;
			}
		});

		return super.onCreateOptionsMenu(menu);
	}

}
