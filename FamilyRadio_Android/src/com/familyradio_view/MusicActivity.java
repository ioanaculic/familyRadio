package com.familyradio_view;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.familyradio.R;
import com.familyradio_station_connection.SendThread;
import com.familyradio_station_connection.StationConnection;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Directory;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MusicActivity extends ListActivity {
	
	int port;
	String ip_address;
	String username;
	private ListView track_list;
	private ArrayList<String> track_name;
	private ArrayList<String> track_id = new ArrayList<String>();
	private TextView current_track;
	private final String FILE_PATH = "/storage/extSdCard/Music";
	Button next, leave;
	String song_name = "";
	ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music);
		
		next = (Button) findViewById(R.id.next_activity_music);
		leave = (Button) findViewById(R.id.leave_activity_music);
		
		Intent intent = getIntent();
		ip_address = intent.getStringExtra("ip");
		port = intent.getIntExtra("port", 0);
		username = intent.getStringExtra("username");
		
		track_list = (ListView) findViewById(android.R.id.list);
		current_track = (TextView) findViewById(R.id.track_activity_music);
		
		File musicDirectory = new File(FILE_PATH);
		String[] songs = musicDirectory.list();
		
		SharedPreferences myPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor myPrefs2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
		
		Thread t1 = new SendThread(songs, myPrefs, myPrefs2, port, username, ip_address, FILE_PATH);
		t1.start();
		 
		track_name = new ArrayList<String>();
 
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, track_name);
		setListAdapter(adapter);
		
		join_service();
		get_current_track();
		get_song_list();
		
		next.setOnClickListener(new OnClickListener() {
			   @Override
			   public void onClick(View v) {
				   play_next_song();
				   get_current_track();
			   }
			  });
		
		leave.setOnClickListener(new OnClickListener() {
			   @Override
			   public void onClick(View v) {
				   leave_service();
			   }
			  });
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.music, menu);
		return true;
	}
	
	private void play_next_song()
	{
		Map<String, String> params = new TreeMap<String, String>();
		String s = "/next/" + username;
		StationConnection.sharedInstance(ip_address, port).getRequest(s, params, new AsyncHttpResponseHandler()
			{
				@Override
				public void onSuccess(String js)   
				{	
					System.out.println("Get Next Request Succesfull");
					super.onSuccess(js);
					
					JSONObject json;
					
					try 
					{
						json = new JSONObject(js);	
						System.out.println(json.toString());
					
					}  
					catch (JSONException e) {
						System.out.println(e);
					}		
				}
				
				@SuppressWarnings("deprecation")
				@Override
				public void onFailure(Throwable arg0) {
		
					System.out.println("Get next request failed " + arg0.getMessage());
					super.onFailure(arg0);
				}
			});
	}
	
	private void get_current_track()
	{
		Map<String, String> params = new TreeMap<String, String>();
		String s = "/get_song/" + username;
		StationConnection.sharedInstance(ip_address, port).getRequest(s, params, new AsyncHttpResponseHandler()
			{
				@Override
				public void onSuccess(String js)   
				{	
					System.out.println("Get Next Request Succesfull");
					super.onSuccess(js);
					
					JSONObject json;
					
					try 
					{
						json = new JSONObject(js);	
						song_name = json.getString("name");
						current_track.setText(song_name);
						System.out.println("nume mel:"+ song_name);
					
					}  
					catch (JSONException e) {
						System.out.println(e);
					}		
				}
				
				@SuppressWarnings("deprecation")
				@Override
				public void onFailure(Throwable arg0) {
		
					System.out.println("Get next request failed " + arg0.getMessage());
					super.onFailure(arg0);
				}
			});
	}
	
	private void get_song_list()
	{
		Map<String, String> params = new TreeMap<String, String>();
		String s = "/list_songs/" + username;
		StationConnection.sharedInstance(ip_address, port).getRequest(s, params, new AsyncHttpResponseHandler()
			{
				@Override
				public void onSuccess(String js)   
				{	
					System.out.println("Get List Request Succesfull");
					super.onSuccess(js);
					 
					JSONObject json;
					
					try 
					{
						JSONArray jsonArray = new JSONArray(js);
						for(int i = 0; i < jsonArray.length(); i++)
						{
							JSONObject auxJson = (JSONObject)jsonArray.get(i);
							track_name.add(auxJson.getString("name"));
							track_id.add(auxJson.getString("id"));
						}

					
					}  
					catch (JSONException e) {
						System.out.println(e);
					}
					adapter.notifyDataSetChanged();
				}
				
				@SuppressWarnings("deprecation")
				@Override
				public void onFailure(Throwable arg0) {
		
					System.out.println("Get List request failed " + arg0.getMessage());
					super.onFailure(arg0);
				}
			});
	}
	
	public void leave_service()
	{
		Map<String, String> params = new TreeMap<String, String>();
		String s = "/offline/" + username;
		StationConnection.sharedInstance(ip_address, port).getRequest(s, params, new AsyncHttpResponseHandler()
			{
				@Override
				public void onSuccess(String js)   
				{	
					System.out.println("Get leave Request Succesfull");
					super.onSuccess(js);
					
						finish();		 
				}
				
				@SuppressWarnings("deprecation")
				@Override
				public void onFailure(Throwable arg0) {
		
					System.out.println("Get leave request failed " + arg0.getMessage());
					super.onFailure(arg0);
				}
			});
		
	}
	
	public void join_service()
	{
		Map<String, String> params = new TreeMap<String, String>();
		String s = "/online/" + username;
		StationConnection.sharedInstance(ip_address, port).getRequest(s, params, new AsyncHttpResponseHandler()
			{
				@Override
				public void onSuccess(String js)   
				{	
					System.out.println("Get join Request Succesfull");
					super.onSuccess(js);
					
					JSONObject json;
					
					try 
					{
						json = new JSONObject(js);	
						System.out.println(json.toString());
					}  
					catch (JSONException e) {
						System.out.println(e);
					}		
				}
				
				@SuppressWarnings("deprecation")
				@Override
				public void onFailure(Throwable arg0) {
		
					System.out.println("Get join request failed " + arg0.getMessage());
					super.onFailure(arg0);
				}
			});
		
	}

}
