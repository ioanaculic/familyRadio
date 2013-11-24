package com.familyradio_station_connection;

import java.util.Map;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class StationConnection 
{
	//this class is a Singleton
	
	private AsyncHttpClient client = new AsyncHttpClient();
	public String URL;
	
	static StationConnection instance;
	
	private StationConnection(String ip, int port)
	{
		this.URL = "http:/" + ip + ':' + port;
		System.out.println("url: " + URL);
	} 
	
	public static StationConnection sharedInstance(String ip, int port)
	{
		if( instance == null ) instance = new StationConnection(ip, port);
		
		return instance;
	}
	
	public void getRequest(String path,Map<String,String> params, AsyncHttpResponseHandler handle)
	{
		String requestParams = "?";
		
		for(Map.Entry<String, String> entry : params.entrySet())
		{
			requestParams += entry.getKey() + "=" + entry.getValue() + "&";
		}
		
		Log.d("MY_DEBUG","Making request at :" +URL + path + requestParams);
		client.get(URL + path + requestParams, handle);

	}

	
	public void postRequest(String path,RequestParams params, AsyncHttpResponseHandler handler)
	{
		Log.d("MY_DEBUG","Making request at :" +URL + path);
		client.post(URL + path,params,handler);
	}
	
}
