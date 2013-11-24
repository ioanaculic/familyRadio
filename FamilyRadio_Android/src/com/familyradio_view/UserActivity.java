package com.familyradio_view;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.familyradio.R;
import com.familyradio_station_connection.StationConnection;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class UserActivity extends Activity {
	
	Button done_button;
	EditText username_edittext;
	String username;
	String ip_address;
	int port;
	SharedPreferences.Editor myPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		
		done_button = (Button) findViewById(R.id.done_activity_music);
		username_edittext = (EditText) findViewById(R.id.input_name_activity_music);
		
		Intent intent = getIntent();
		ip_address = intent.getStringExtra("ip");
		port = intent.getIntExtra("port", 0);
		
		myPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
		
		System.out.println("user:" + ip_address + port);
		
		done_button.setOnClickListener(new OnClickListener() {
			   @Override
			   public void onClick(View v) {
				   username = username_edittext.getText().toString();
				   post_user_to_station(username);
				   myPrefs.putString("username", username);
				   myPrefs.commit();
				   Intent i = new Intent(UserActivity.this, MusicActivity.class);
				   i.putExtra("ip", ip_address);
				   i.putExtra("port", port);
		        startActivity(i);	   
			   }
			  });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.user, menu);
		return true;
	}
	
	 private void post_user_to_station(String username)
	 {
		 RequestParams params = new RequestParams();
			params.put("name", username);
			params.put("id", username);
			
			StationConnection.sharedInstance(ip_address, port).postRequest("/add_user", params, new AsyncHttpResponseHandler()
			{
				@Override
				public void onSuccess(String js) 
				{	
					System.out.println("Post Request Succesfull");
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
		
					System.out.println("Post request failed " + arg0.getMessage());
					super.onFailure(arg0);
				}
			});
	 }


}
