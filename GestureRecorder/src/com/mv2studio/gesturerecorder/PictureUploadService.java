package com.mv2studio.gesturerecorder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mv2studio.gesturerecorder.ui.MainActivity;

public class PictureUploadService extends Service {

	public static final String GESTURES_TAG = "GESTURES",
							   ID_TAG = "ID";
	
	ArrayList<Gesture> gestures;
	String id;
	int items = MainActivity.gestureTasks.length;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		gestures = intent.getParcelableArrayListExtra(GESTURES_TAG);
		id = intent.getStringExtra(ID_TAG);
		
		new AsyncTask<Void, Void, Void>(){
			
			@Override
			protected Void doInBackground(Void... params) {
				Log.e("", "id: "+id);
				Log.e("", "gesuters: "+gestures);
				for(int i = 0; i < items; i++) {
					Gesture g = gestures.get(i);
					if(g == null) continue;
					
					Bitmap bitmap = g.toBitmap(800, 800, 0, getResources().getColor(R.color.HoloRed));
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
					String image_str = Base64.encodeBytes(stream.toByteArray());
					
					ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
					pairs.add(new BasicNameValuePair("image", image_str));
					pairs.add(new BasicNameValuePair("ID", MainActivity.gestureTasks[i][1]+id));
					
					try {
						HttpClient httpClient = new DefaultHttpClient();
						HttpPost httpPost = new HttpPost("http://mv2studio.com/gestures/upload.php");
						httpPost.setEntity(new UrlEncodedFormEntity(pairs));
						HttpResponse response = httpClient.execute(httpPost);
						Log.e("", response.toString());
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
				
				return null;
			}
			
			protected void onPostExecute(Void result) {
				if(!MainActivity.WORLD_EDITION)
					createNotification(PictureUploadService.this);
				try {
					PictureUploadService.this.stopSelf();
				} catch (Exception e) {}
			}
			
		}.execute();
		
		
		return START_NOT_STICKY;
	}
	
	public void createNotification(Context context) {
		String contentTitle = "Ďakujeme za pomoc.";
		String contentText =  "Teraz môžete aplikáciu odinštalovať";
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
											 .setContentTitle(contentTitle)
											 .setContentText(contentText)
											 .setVibrate(new long[] {1000})
											 .setSmallIcon(R.drawable.ic_launcher);
		
		NotificationManager mNotificationManager =
		    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		mNotificationManager.notify(1, builder.build());
	}

}
