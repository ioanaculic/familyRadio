package com.familyradio_station_connection;


import org.json.JSONException;
import org.json.JSONObject;

public class JSONParser {
	static JSONObject jObj = null;
	
	public JSONParser(){}
	
	public JSONObject getJSONFromString(String s)
	{
		try{
			jObj = new JSONObject(s);
		}	catch (JSONException e){
		
		}
		
		return jObj;
	}
}
