package com.mv2studio.gesturerecorder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mv2studio.gesturerecorder.ui.MainActivity;

public class PictureUploadService extends Service {

	public static final String GESTURES_TAG = "GESTURES",
							   ID_TAG = "ID";
	
	String id;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("", "service started");
		id = intent.getStringExtra(ID_TAG);
		new AsyncTask<Void, Void, Void>(){
			
			@Override
			protected Void doInBackground(Void... params) {
				boolean showed = false;
				while(!CommonHelper.isOnline(PictureUploadService.this) && Prefs.getBoolValue(MainActivity.INTERNET_SWITCH, PictureUploadService.this)) {
					if(!showed) createNotification(PictureUploadService.this, "Čakám na odoslanie dát", "Prosím, pripojte sa k internetu");
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				String[] files = new File(PictureUploadService.this.getExternalFilesDir(null).getAbsolutePath()+File.separator+id).list();
				for(int i = 0; i < files.length; i++) {
					Bitmap b = null;
					try {
						FileInputStream fis = new FileInputStream(new File(PictureUploadService.this.getExternalFilesDir(null).toString()+File.separator+id+File.separator+files[i]));
						b = BitmapFactory.decodeStream(fis, null, null);
						fis.close();
					} catch(Exception e) {
						e.printStackTrace();
					}
					Log.e("", "bitmap "+i+" is "+b);
					if(b == null) continue;
					
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					b.compress(Bitmap.CompressFormat.PNG, 100, stream);
					String image_str = Base64.encodeBytes(stream.toByteArray());
					
					ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
					pairs.add(new BasicNameValuePair("image", image_str));
					
					try {
						// get 1 from "1-3.png"
						String name = files[i].split("\\.")[0];
						String[] split = name.split("-");
						int type = Integer.valueOf(split[0]);
						String pairID = MainActivity.gestureTasks[type][1].substring(0, MainActivity.gestureTasks[type][1].length()-1)+"-"+split[1]+"_";
						Log.e("", "PAIR ID: "+pairID);
						pairs.add(new BasicNameValuePair("ID", pairID+id));
					} catch (Exception e ) {
						
						pairs.add(new BasicNameValuePair("ID", files[i].split("\\.")[0].split("_")[1]+"_"+id));
						e.printStackTrace();
					}
					
					
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
				if(Prefs.getBoolValue(MainActivity.SHOW_SURVEY_TAG, PictureUploadService.this))
					createNotification(PictureUploadService.this, "Ďakujeme za pomoc.", "Dáta boli odoslané.");
				try {
					PictureUploadService.this.stopSelf();
				} catch (Exception e) {}
			}
			
		}.execute();
		
		
		return START_NOT_STICKY;
	}
	
	public void createNotification(Context context, String title, String content) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
											 .setContentTitle(title)
											 .setContentText(content)
											 .setVibrate(new long[] {1000})
											 .setSmallIcon(R.drawable.ic_launcher);
		
		NotificationManager mNotificationManager =
		    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		mNotificationManager.notify(1, builder.build());
	}

}
