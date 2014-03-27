package com.mv2studio.gesturerecorder.ui;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mv2studio.gesturerecorder.R;

public class SurveyFragment extends BaseFragment {
	
	ArrayList<SurveyQuestion> questions = new ArrayList<SurveyFragment.SurveyQuestion>();
	ListView list;
	Button doneButton;
	String id;
	public static final String ID_TAG = "ID", RETAIN_TAG = "RET";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		if(savedInstanceState != null) {
			ArrayList<SurveyQuestion> q = (ArrayList<SurveyQuestion>) savedInstanceState.getSerializable(RETAIN_TAG);
			if(q != null) questions = q;
		} else {
			questions.add(new SurveyQuestion("V ktorom ste ročníku", null, 1, "1. Bc.", "2. Bc.", "3. Bc.", "1. Ing.", "2. Ing", "PhD.", "Nie som študent"));
			questions.add(new SurveyQuestion("Vaše skúsenosti s programovaním", null, 1, "Začiatočník", "Mierne pokročilý", "Pokročilý"));
			questions.add(new SurveyQuestion("Preferovaný jazyk", "Vyberte, prosím, jazyk, v ktorom máte najviac skúsenosti a vaše znalosti práce v ňom odpovedajú úrovni z predošlej otázky.",
					2, "Java", "PHP", "C/C++", "Python", "Ruby", "JavaScript", "Iné"));
			questions.add(new SurveyQuestion("Máte tablet?", null, 1, "Áno", "Nie"));
			questions.add(new SurveyQuestion("Využívate pravidelne mobilný internet?", null, 1, "Áno", "Nie"));
			questions.add(new SurveyQuestion("Použili ste niekedy dotykové zariadenie na programovanie?", null, 1, "Áno", "Nie"));
			questions.add(new SurveyQuestion("Ak áno,  aké editory či vývojové prostredia ste použili?", null, 0));
			questions.add(new SurveyQuestion("Viete si predstaviť programovanie na dotykovom zariadení?", null, 1, "Áno", "Nie"));
			questions.add(new SurveyQuestion("Na aké činnosti súvisiace s programovaním by sa dalo použiť dotykové zariadenie?",
					"V prípade, že na predchádzajúcu otázku ste odpovedali kladne, skúste bližšie opísať vašu predstavu.", 0));
			
		}
		
		id = getArguments().getString(ID_TAG);
		
		View v = inflater.inflate(R.layout.fragment_survey, null);
		
		list = (ListView) v.findViewById(R.id.fragment_survey_list);
		doneButton = (Button) v.findViewById(R.id.fragment_survey_done);
		doneButton.setTypeface(tCondBold);
		
		final TextView title = (TextView) v.findViewById(R.id.fragment_survey_title);
		title.setTypeface(tThin);
		
		
		SurveyAdapter adapter = new SurveyAdapter(getActivity(), 0, questions);
		list.setAdapter(adapter);
		
		final TextView thx = (TextView) v.findViewById(R.id.fragment_survey_thanks);
		thx.setTypeface(tThin);
		
		
		final Animation anim_in  = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
		anim_in.setDuration(200);
		anim_in.setFillAfter(true);
		
		doneButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final String answers = buildAnswer();
				// send request
				new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						
						ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
						pairs.add(new BasicNameValuePair("answers", answers));
						pairs.add(new BasicNameValuePair("ID", id));
						
						try {
							HttpClient httpClient = new DefaultHttpClient();
							HttpPost httpPost = new HttpPost("http://mv2studio.com/gestures/txt.php");
							UrlEncodedFormEntity form = new UrlEncodedFormEntity(pairs);
							form.setContentEncoding(HTTP.UTF_8);
							httpPost.setEntity(form);
							HttpResponse response = httpClient.execute(httpPost);
							Log.e("", response.toString());
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
						return null;
					}
					
					protected void onPostExecute(Void result) {
						try {
							String contentTitle = "Ďakujeme za pomoc.";
							String contentText =  "Teraz môžete aplikáciu odinštalovať";
							NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity())
																 .setContentTitle(contentTitle)
																 .setContentText(contentText)
																 .setVibrate(new long[] {1000})
																 .setSmallIcon(R.drawable.ic_launcher);
							
							NotificationManager mNotificationManager =
							    (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
							
							mNotificationManager.notify(1, builder.build());
						} catch (Exception e) {}
					}
					
				}.execute();
				
				new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						System.out.println("poping!");
						((MainActivity)getActivity()).replaceFragment(new StartFragment(), false);
						
					}
				}, 1500);
				thx.startAnimation(anim_in);
			}
			
		});
		
		doneButton.setEnabled(false);
		doneButton.setBackgroundResource(R.drawable.button_selector_gray);
		((MainActivity)getActivity()).setMenu(MainActivity.MENU_CLEAR);
		return v;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(RETAIN_TAG, questions);
		super.onSaveInstanceState(outState);
	}
	
	private String buildAnswer() {
		String builded = "";
		
		for(int i = 0; i < list.getCount(); i++) {
			SurveyQuestion q = (SurveyQuestion) list.getItemAtPosition(i);
			builded += q.question+"\n";
			switch(q.type) {
			case 0:
			case 1:
				builded += "<<"+q.answeredString+">>\n";
				break;
			case 2:
				for(String str: q.answered) {
					builded += "<<"+str+">>";
				}
			}
			builded += "\n\n===============================================\n\n";
			
		}
		return builded;
	}
	
	private class SurveyAdapter extends ArrayAdapter<SurveyQuestion> {

		LayoutInflater inflater;
		
		public SurveyAdapter(Context context, int resource, List<SurveyQuestion> objects) {
			super(context, resource, objects);
			
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final SurveyQuestion q = getItem(position);
			
			View v = inflater.inflate(R.layout.survey_item, null);
			TextView title = (TextView) v.findViewById(R.id.survey_item_question);
			title.setTypeface(tCondBold);
			
			TextView subtitle = (TextView) v.findViewById(R.id.survey_item_subtitle);
			subtitle.setTypeface(tCondLight);
			
			switch(q.type) {
			case 0:
				EditText text = (EditText) inflater.inflate(R.layout.edittext, null);
				text.setTypeface(tCond);
				text.addTextChangedListener(new TextWatcher() {
					public void onTextChanged(CharSequence s, int start, int before, int count) {}
					public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
					public void afterTextChanged(Editable s) {
						q.answeredString = s.toString();
					}
				});
				try {
					text.setText(q.answeredString);
				} catch (Exception e ) {}
				
				((ViewGroup)v).addView(text);
				break;
				
				
				
			case 1:
				RadioGroup group = (RadioGroup) inflater.inflate(R.layout.radiogroup, null);
				for(String answer: q.answers) {
					RadioButton button = (RadioButton) inflater.inflate(R.layout.radiobutton, null);
					button.setText(answer);
					button.setTypeface(tCond);
					
					button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if(isChecked) {
								q.answeredString = buttonView.getText().toString();
							}	
							updateDoneButton();
						}
					});
					group.addView(button);
					if(q.answeredString.equals(answer)) button.setChecked(true);
					else button.setChecked(false);
				}
				((ViewGroup)v).addView(group);
				break;
				
				
			case 2:
				for(String answer: q.answers) {
					CheckBox box = (CheckBox) inflater.inflate(R.layout.checkbox, null);
					box.setText(answer);
					box.setTypeface(tCond);
					box.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if(isChecked) {
								q.answered.add(buttonView.getText().toString());
							} else {
								q.answered.remove(buttonView.getText().toString());
							}
							updateDoneButton();
						}
					});
					if(q.answered.contains(answer)) box.setChecked(true);
					((ViewGroup)v).addView(box);
				}
				break;
			}
			
			title.setText((position + 1)+". "+q.question);
			if(q.subtitle == null) {
				subtitle.setVisibility(View.GONE);
			} else {
				subtitle.setText(q.subtitle);
			}
			
			return v;
		}
		
		private void updateDoneButton() {
			boolean done = true;
			
			loop:
			for(int i = 0; i < getCount(); i++) {
				SurveyQuestion sq = getItem(i);
				switch(sq.type) {
				case 2:
					if(sq.answered.isEmpty()) {
						System.out.println("question "+i+" empty");
						done = false;
						break loop;
					}
					break;
				case 1:
					if(sq.answeredString.isEmpty()) {
						System.out.println("question "+i+" empty");
						done = false;
						break loop;
					}
				}
			}
			
			// change send button 
			if(done) {
				doneButton.setEnabled(true);
				doneButton.setBackgroundResource(R.drawable.button_selector_green);
			} else {
				doneButton.setEnabled(false);
				doneButton.setBackgroundResource(R.drawable.button_selector_gray);
			}
		}
		
	}
	
	
	private static class SurveyQuestion implements Serializable {
		private static final long serialVersionUID = 1L;
		String question;
		String subtitle;
		int type;
		String[] answers;
		ArrayList<String> answered = new ArrayList<String>();
		String answeredString = "";
		
		public SurveyQuestion(String question, String subtitle, int type, String... answers) {
			this.question = question;
			this.subtitle = subtitle;
			this.type = type;
			this.answers = answers;
		}
	}
}
