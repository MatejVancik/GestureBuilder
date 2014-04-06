package com.mv2studio.gesturerecorder.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mv2studio.gesturerecorder.CommonHelper;
import com.mv2studio.gesturerecorder.PictureUploadService;
import com.mv2studio.gesturerecorder.Prefs;
import com.mv2studio.gesturerecorder.R;
import com.mv2studio.gesturerecorder.ui.GestureView.Gesture;
import com.mv2studio.gesturerecorder.ui.GestureView.OnGestureDoneListener;

public class GestureScannerFragment extends BaseFragment {

	// Fragment views
	private TextView title, topTitle, step, thx, yourID, sample, sampleTitle, rateTitle;
	private View thxLayout, fragmentLayout, redHighlight;
	private Button finish, plus;
	private ImageButton next, prev;
	private GestureView gestureView;
	private RelativeLayout rateLayout;
	private ArrayList<ImageButton> stars = new ArrayList<ImageButton>();

	private SparseArray<Gesture> gestures = new SparseArray<Gesture>();

	private int items = MainActivity.gestureTasks.length;
	private int currentItem = 0;
	private int customItemIndex = 0;
	private int currentRating;
	private String gestureID = "";
	private boolean[][] gestureDone = new boolean[10][10];
	
	Animation fade_out;
	
	int[] starsID = {R.id.star1, R.id.star2, R.id.star3, R.id.star4, R.id.star5};
	private static final String SAVED_GESTURES = "GESTURES", SAVED_CURRENT_ITEM = "CURRENT", SAVED_CURRENT_ID = "ID", SAVED_RATING = "RATING";

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(SAVED_CURRENT_ITEM, currentItem);
		outState.putString(SAVED_CURRENT_ID, gestureID);
		outState.putInt(SAVED_RATING, currentRating);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// GET SAVED STATE
		if (savedInstanceState != null) {
			currentItem = savedInstanceState.getInt(SAVED_CURRENT_ITEM);
			gestureID = savedInstanceState.getString(SAVED_CURRENT_ID);
			currentRating = savedInstanceState.getInt(SAVED_RATING);
		}

		if (gestureID.isEmpty())
			gestureID = String.valueOf(System.currentTimeMillis());
		
		fade_out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out); 
		fade_out.setDuration(500);
		fade_out.setInterpolator(new DecelerateInterpolator(0.7f));

		final View v = inflater.inflate(R.layout.fragment_gesture, null);
		fragmentLayout = v;
		gestureView = (GestureView) v.findViewById(R.id.fragment_gesture_view);
		int time = Prefs.getIntValue(MainActivity.GESTURE_TIME_MILLIS_TAG, getActivity());
		if(time == -1) time = 1000;
		gestureView.setGestureTimeout(time);
		final RotateAnimation rotate = new RotateAnimation(360, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotate.setDuration(300);

		final Animation anim_in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
		anim_in.setDuration(200);
		anim_in.setFillAfter(true);
		
		gestureView.setOnGestureDoneListener(new OnGestureDoneListener() {
			@Override
			public void onDone() {
				gestureDone[currentItem][0] = true;
				checkDoneState();
			}
		});

		OnClickListener clickListener = new OnClickListener() {

			@Override
			public void onClick(View view) {
				System.out.println("current: "+currentItem+"   items: "+items);
				switch (view.getId()) {
				case R.id.fragment_gesture_next:
					saveGesture();
					// FINISH!
					if (currentItem == items) {
						if(!CommonHelper.isOnline(getActivity()) && Prefs.getBoolValue(MainActivity.INTERNET_SWITCH, getActivity())) {
							Toast.makeText(getActivity(), getString(R.string.connect_to_net_first), Toast.LENGTH_LONG).show();
							return;
						}

						if (Prefs.getBoolValue(MainActivity.SHOW_SURVEY_TAG, getActivity())) {
							sendAnswers();
							Bundle b = new Bundle();
							b.putString(SurveyFragment.ID_TAG, gestureID);
							SurveyFragment f = new SurveyFragment();
							f.setArguments(b);
							((MainActivity) getActivity()).replaceFragment(f, true);
						} else {
							System.out.println("showing thx");
							thxLayout.setVisibility(View.VISIBLE);
							thxLayout.startAnimation(anim_in);
						}
					}
					setGestureView(1);
					checkDoneState();
					break;
				case R.id.save_gesutre_button:
					showInputDialog();
					break;
				case R.id.fragment_gesture_prev:
					saveGesture();
					setGestureView(-1);
					break;
				case R.id.refresh:
					for(int i = 0; i < starsID.length; i++) {
						((ImageButton)v.findViewById(starsID[i])).setImageResource(R.drawable.ic_action_not_important);
					}
					gestureDone[currentItem][0] = false;
					gestureDone[currentItem][1] = false;
					checkDoneState();
					view.startAnimation(rotate);
					gestureView.refreshGesture();
					break;

				case R.id.fragmen_gesture_finish:
					sendAnswers();
					getFragmentManager().popBackStack();
					break;
				}
				
				
				
			}
		};
		
		sample = (TextView) v.findViewById(R.id.fragment_gesture_html);
		sample.setTypeface(tCond);
		
		sampleTitle = (TextView) v.findViewById(R.id.fragment_gesture_html_title);
		sampleTitle.setTypeface(tLight);
		((TextView)v.findViewById(R.id.fragment_gesture_rate_title)).setTypeface(tCond);
		
		if(currentItem < items)
		sample.setText(Html.fromHtml(MainActivity.gestureTasks[currentItem][2]));

		next = (ImageButton) v.findViewById(R.id.fragment_gesture_next);
		next.setOnClickListener(clickListener);
		if(!gestureDone[currentItem][0] || !gestureDone[currentItem][1]) {
			next.setEnabled(false);
			next.setBackgroundResource(R.drawable.circle_gray_selector);
		}

		prev = (ImageButton) v.findViewById(R.id.fragment_gesture_prev);
		prev.setOnClickListener(clickListener);
		if (currentItem == 0)
			prev.setBackgroundResource(R.drawable.circle_gray_selector);

		plus = (Button) v.findViewById(R.id.save_gesutre_button);
		plus.setTypeface(tCondBold);
		plus.setOnClickListener(clickListener);
		plus.setVisibility(View.GONE);

		((MainActivity) getActivity()).setOnRefreshListener(clickListener);

		step = (TextView) v.findViewById(R.id.fragment_gesture_progress);
		step.setTypeface(tCondLight);
		step.setText((1 + currentItem) + "/" + (items + 1));

		title = (TextView) v.findViewById(R.id.fragment_gesture_title_type);
		title.setTypeface(tCondBold);
		if(currentItem < items) {
			title.setText(MainActivity.gestureTasks[currentItem][0]);
		}

		topTitle = (TextView) v.findViewById(R.id.fragment_gesture_title);
		if(currentItem == items) {
			topTitle.setText(R.string.gesture_title_custom);
		}
		topTitle.setTypeface(tCond);
		
		rateTitle = (TextView) v.findViewById(R.id.fragment_gesture_rate_title);
		rateTitle.setTypeface(tCond);

		thx = (TextView) v.findViewById(R.id.fragment_gesture_thanks);
		thx.setTypeface(tCondLight);

		thxLayout = v.findViewById(R.id.fragment_gesture_thanks_layout);

		yourID = (TextView) v.findViewById(R.id.fragment_gesture_your_id);
		yourID.setTypeface(tCondLight);
		yourID.setText("Vaše ID:\n" + gestureID);

		finish = (Button) v.findViewById(R.id.fragmen_gesture_finish);
		finish.setTypeface(tCondBold);
		finish.setOnClickListener(clickListener);

		rateLayout = (RelativeLayout) v.findViewById(R.id.fragment_gesture_rate_layout);
		redHighlight = v.findViewById(R.id.fragment_gesture_red_highlight);
		
		if(currentItem == items) {
			rateLayout.setVisibility(View.INVISIBLE);
			plus.setVisibility(View.VISIBLE);
			title.setVisibility(View.INVISIBLE);
			next.setBackgroundResource(R.drawable.circle_green_selector);
			next.setImageResource(R.drawable.vv);
		}
		
		try {
			((TextView)v.findViewById(R.id.stars_none)).setTypeface(tCond);
			((TextView)v.findViewById(R.id.stars_full)).setTypeface(tCond);
		} catch(NullPointerException e) {}
		
		if(gestures.get(currentItem) != null)
			currentRating = gestures.get(currentItem).rating;
		
		
		stars.clear();
		for(int i = 0; i < starsID.length; i++) {
			ImageButton but = (ImageButton) v.findViewById(starsID[i]);
			stars.add(but);
			if(i < currentRating) but.setImageResource(R.drawable.ic_action_important);
			
			final int j = i;
			but.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					for(int k = 0; k < starsID.length; k++) {
						if(k <= j) ((ImageButton)v.findViewById(starsID[k])).setImageResource(R.drawable.ic_action_important);
						else ((ImageButton)v.findViewById(starsID[k])).setImageResource(R.drawable.ic_action_not_important);
					}
					currentRating = j+1;
					gestureDone[currentItem][1] = true;
					checkDoneState();
				}
			});
		}
		
		// restore gesture if it's possible. Must be delayed to show
		// gesture on right place
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				if(gestures.get(currentItem) != null)
					gestureView.setGesture(gestures.get(currentItem));
			}
		}, 100);

		
		checkDoneState();
		((MainActivity) getActivity()).setMenu(MainActivity.MENU_GESTURE);
		return v;
	}
	
	private void checkDoneState() {
		next.setEnabled(false);
		next.setBackgroundResource(R.drawable.circle_gray_selector);
		plus.setEnabled(false);
		plus.setBackgroundResource(R.drawable.button_selector_gray);
		
		if(currentItem == items) {
			if(gestureDone[currentItem][0]) {
				plus.setEnabled(true);
				plus.setBackgroundResource(R.drawable.button_selector_green);
				
				next.setEnabled(false);
				next.setBackgroundResource(R.drawable.circle_gray_selector);
			} else {
				plus.setEnabled(false);
				plus.setBackgroundResource(R.drawable.button_selector_gray);
				
				next.setEnabled(true);
				next.setBackgroundResource(R.drawable.circle_green_selector);
			}
			return;
		}
		
		if(gestureDone[currentItem][0]) {
			if(gestureDone[currentItem][1]) {
				next.setEnabled(true);
				next.setBackgroundResource(R.drawable.circle_blue_selector);
			} else {
				if(currentItem != items) {
					redHighlight.startAnimation(fade_out);
				}
			}
		}
	}

	private void sendAnswers() {
		new AsyncTask<Void, Void, Void>() {
			Context context;
			
			protected void onPreExecute() {
				context = getActivity().getApplicationContext();
			}
			
			@Override
			protected Void doInBackground(Void... params) {
				try {
					for (int i = 0; i < items; i++) {
						Gesture g = gestures.get(i);
						String path = context.getExternalFilesDir(null).toString() + File.separator + gestureID;
						new File(path).mkdirs();
						path += File.separator + i+"-"+g.rating+".png";
						FileOutputStream fos = new FileOutputStream(new File(path));
						g.bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
						fos.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			@Override
			protected void onPostExecute(Void result) {
				Intent intent = new Intent(context, PictureUploadService.class);
				intent.putExtra(PictureUploadService.ID_TAG, gestureID);
				context.startService(intent);
				
				super.onPostExecute(result);
			}
		}.execute();
	}

	
	private void setGestureView(int step) {
		currentItem += step;
		

		next.setBackgroundResource(R.drawable.circle_blue_selector);
		next.setImageResource(R.drawable.right);
		prev.setBackgroundResource(R.drawable.circle_blue_selector);
		rateLayout.setVisibility(View.VISIBLE);
		plus.setVisibility(View.GONE);
		title.setVisibility(View.VISIBLE);
		topTitle.setText(getString(R.string.gesture_title));

		if (currentItem >= items) {
			currentItem = items;

			// show custom gesture
			rateLayout.setVisibility(View.INVISIBLE);
			topTitle.setText(getString(R.string.gesture_title_custom));
			plus.setVisibility(View.VISIBLE);
			title.setVisibility(View.INVISIBLE);
			sampleTitle.setVisibility(View.INVISIBLE);
			next.setBackgroundResource(R.drawable.circle_green_selector);
			next.setImageResource(R.drawable.vv);
			this.step.setText((currentItem + 1) + "/" + (items + 1));
			gestureView.refreshGesture();
			sample.setText("");
			return;

		} else if (currentItem <= 0) {
			currentItem = 0;
			prev.setEnabled(false);
			prev.setBackgroundResource(R.drawable.circle_gray_selector);
		} else {
			prev.setEnabled(true);
			next.setEnabled(true);
		}
		
		try {
			System.out.println("getting rating: "+gestures.get(currentItem).rating);
			currentRating = gestures.get(currentItem).rating;
		} catch(Exception e) {e.printStackTrace();}
		
		for(int i = 0; i < starsID.length; i++) {
			stars.get(i).setImageResource((i < currentRating) ? R.drawable.ic_action_important : R.drawable.ic_action_not_important);
		}
		
		
		Spanned titleCode = Html.fromHtml(MainActivity.gestureTasks[currentItem][2]);
		sample.setText(titleCode);
		System.out.println(titleCode.length()+" length");
		if(titleCode.length() == 0) sampleTitle.setVisibility(View.INVISIBLE);
		else sampleTitle.setVisibility(View.VISIBLE);
		
		Gesture recreate = gestures.get(currentItem);
		gestureView.setGesture(recreate);

		title.setText(MainActivity.gestureTasks[currentItem][0]);
		this.step.setText((currentItem + 1) + "/" + (items + 1));

	}

	private void showInputDialog() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.dialog_input, null);
		final EditText edit = (EditText) v.findViewById(R.id.dialog_input_edit_text);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Čo reprezentuje nakreslené gesto?");
		builder.setView(v);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String name = edit.getText().toString();
				if(name.length() == 0) {
					Toast.makeText(getActivity(), "Prosím zadajte najprv názov", Toast.LENGTH_SHORT).show();
					return;
				}
				saveCustomGesture(name);
				gestureView.refreshGesture();
				Toast.makeText(getActivity(), "Vlastné gesto uložené", Toast.LENGTH_SHORT).show();
				
				gestureDone[currentItem][0] = false;
				checkDoneState();
			}
		}).setNegativeButton("Zrušiť", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				gestureView.refreshGesture();
				gestureDone[currentItem][0] = false;
				checkDoneState();
			}
		});
		builder.show();

	}
	
	private void saveCustomGesture(final String name) {
		final int item = currentItem + ++customItemIndex;
		final Gesture g = gestureView.getGesture();

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					String path = getActivity().getExternalFilesDir(null).toString() + File.separator + gestureID;
					new File(path).mkdirs();
					path += File.separator + item + "_" + name + "-" + currentRating + ".png";
					FileOutputStream fos = new FileOutputStream(new File(path));
					g.bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
		
	}

	private void saveGesture() {
		Gesture g = gestureView.getGesture();
		System.out.println("setting rating: "+currentRating);
		g.rating = currentRating;
		currentRating = 0;
		gestures.put(currentItem, g);
	}

}
