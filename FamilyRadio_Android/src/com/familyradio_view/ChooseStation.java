package com.familyradio_view;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.familyradio.R;
import com.familyradio_station_connection.NsdHelper;
import com.familyradio_station_connection.ServiceListener;
import com.familyradio_station_connection.StationConnection;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;

public class ChooseStation extends ListActivity implements ServiceListener  {
	
	private NsdHelper mNsdHelper;
	private ListView stations_list;
	private ArrayList<String> station_names;
	private ArrayAdapter<String> adapter;
	private Context ctx;
	private SharedPreferences MyPrefs; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_choose_station);
		
		ctx = this;
		mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();
        
        stations_list = (ListView) findViewById(android.R.id.list);
		 
		station_names = new ArrayList<String>();

		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, station_names);
		setListAdapter(adapter);
		
		MyPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		stations_list.setOnItemClickListener(new OnItemClickListener() {
	        @Override
			public void onItemClick(AdapterView<?> parent, View view,
	                int position, long id) {
	        	
	        	Intent i;
	        	if(MyPrefs.getString("username", null) == null)
	        	{
	        		i = new Intent(ChooseStation.this, UserActivity.class);
	        	}
	        	 
	        	else
	        	{
	        		i = new Intent(ChooseStation.this, MusicActivity.class);
	        		i.putExtra("username", MyPrefs.getString("username", null));
	        	}
	        	
	        	i.putExtra("ip", mNsdHelper.ip_address);
	        	i.putExtra("port", mNsdHelper.port);
	        	startActivity(i);
			}
		});
    
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mNsdHelper.discoverServices();
		
		//station request, status
        // check_Station_status(mNsdHelper.ip_address, mNsdHelper.port);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mNsdHelper.stopDiscovery();
	}
	
	 public void check_Station_status(String ip_address, int port)
	    {
		station_names.clear();	
		Map<String, String> params = new TreeMap<String, String>();
		StationConnection.sharedInstance(ip_address, port).getRequest("/status", params, new AsyncHttpResponseHandler()
			{
				@Override
				public void onSuccess(String js)   
				{	
					System.out.println("Get Request Succesfull");
					super.onSuccess(js);
					
					JSONObject json;
					
					try 
					{
						json = new JSONObject(js);
						
						System.out.println(json.toString());
						
						if( json.getString("status").equals("configure") )
						{
							//configure station
							AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
	
							alert.setTitle("Configure station");
							alert.setMessage("Choose a name for your FamilyRadio station.");
	
							// Set an EditText view to get user input 
							final EditText input = new EditText(ctx);
							alert.setView(input);
	
							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									String value = input.getText().toString();
									station_names.add(value);
									adapter.notifyDataSetChanged();
									  
									//post configuration
									post_station_config(station_names.get(station_names.size() - 1));
								  
								  }
							});
	
							alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								  public void onClick(DialogInterface dialog, int whichButton) {
								    // Canceled.
								  }
							});
	
							alert.show();
							
						}
						
						if(json.getString("status").equals("running"))
						{
							station_names.add(json.getString("name"));
							adapter.notifyDataSetChanged();
						}
					}  
					catch (JSONException e) {
						System.out.println(e);
					}		
				}
				
				@SuppressWarnings("deprecation")
				@Override
				public void onFailure(Throwable arg0) {
		
					System.out.println("Get request failed " + arg0.getMessage());
					super.onFailure(arg0);
				}
			});
	    }
	 
	 private void post_station_config(String station_name)
	 {
		 RequestParams params = new RequestParams();
			params.put("name", station_name);
			
			StationConnection.sharedInstance(mNsdHelper.ip_address, mNsdHelper.port).postRequest("/configure", params, new AsyncHttpResponseHandler()
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

	@Override
	public void serviceFound(final NsdServiceInfo info) {
		// TODO Auto-generated method stub
		System.out.println("service found");
		this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				check_Station_status(info.getHost().toString(), info.getPort());
			}
		});
	}

}
