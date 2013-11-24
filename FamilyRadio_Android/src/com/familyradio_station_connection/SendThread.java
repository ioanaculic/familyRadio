package com.familyradio_station_connection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.SharedPreferences;
import android.util.Log;

public class SendThread extends Thread {
	private String[] songs;
	private SharedPreferences myPrefs;
	private SharedPreferences.Editor myPrefs2;
	private int port;
	private String username, ip_address, FILE_PATH;
	
	public SendThread(String[] songs, SharedPreferences myPrefs, SharedPreferences.Editor myPrefs2, int port, String username, String ip_address, String f)
	{
		this.ip_address = ip_address;
		this.myPrefs = myPrefs;
		this.myPrefs2 = myPrefs2;
		this.port = port;
		this.songs = songs;
		this.username = username;
		this.FILE_PATH = f;
	}
	
	public void run()
	{
		for(int i = 0; i < songs.length; i++)
		{
			System.out.println(songs[i]);
			boolean added = myPrefs.getBoolean(songs[i], false);
			if(added == false)
			{
				String strurl =  "http:/" + ip_address + ':' + port + "/upload/" + username;
				File file = new File(FILE_PATH + '/' + songs[i]);
				try {
					if (file.length() < 10*1024*1024)
					{
						doFileUpload(strurl, FILE_PATH + '/' + songs[i]);
					}
					myPrefs2.putBoolean("songs[i]", true);
					
				} catch (Exception e) {
					// TODO: handle exception
				}
				try {
					Thread.sleep(40000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
			}
			
		}
	}
	
	private void doFileUpload(String strurl, String filename){
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary =  "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 10*1024*1024;
        String responseFromServer = "";
        String urlString = strurl;
        try
        {
         //------------------ CLIENT REQUEST
        FileInputStream fileInputStream = new FileInputStream(new File(filename) );
         // open a URL connection to the Servlet
         URL url = new URL(urlString);
         // Open a HTTP connection to the URL
         conn = (HttpURLConnection) url.openConnection();
         // Allow Inputs
         conn.setDoInput(true);
         // Allow Outputs
         conn.setDoOutput(true);
         // Don't use a cached copy.
         conn.setUseCaches(false);
         // Use a post method.
         conn.setRequestMethod("POST");
         conn.setRequestProperty("Connection", "Keep-Alive");
         conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
         dos = new DataOutputStream( conn.getOutputStream() );
         dos.writeBytes(twoHyphens + boundary + lineEnd);
         dos.writeBytes("Content-Disposition: form-data; name=\"songfile\";filename=\"" + filename + "\"" + lineEnd);
         dos.writeBytes(lineEnd);
         // create a buffer of maximum size
         bytesAvailable = fileInputStream.available();
         bufferSize = Math.min(bytesAvailable, maxBufferSize);
         buffer = new byte[bufferSize];
         // read file and write it into form...
         bytesRead = fileInputStream.read(buffer, 0, bufferSize);
         while (bytesRead > 0)
         {
          dos.write(buffer, 0, bufferSize);
          bytesAvailable = fileInputStream.available();
          bufferSize = Math.min(bytesAvailable, maxBufferSize);
          bytesRead = fileInputStream.read(buffer, 0, bufferSize);
         }
         // send multipart form data necesssary after file data...
         dos.writeBytes(lineEnd);
         dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
         // close streams
         Log.e("Debug","File is written");
         fileInputStream.close();
         dos.flush();
         dos.close();
        }
        catch (MalformedURLException ex)
        {
             Log.e("Debug", "error: " + ex.getMessage(), ex);
        }
        catch (IOException ioe)
        {
             Log.e("Debug", "error: " + ioe.getMessage(), ioe);
        }
        //------------------ read the SERVER RESPONSE
        try {
              inStream = new DataInputStream ( conn.getInputStream() );
              String str;
 
              while (( str = inStream.readLine()) != null)
              {
                   Log.e("Debug","Server Response "+str);
              }
              inStream.close();
 
        }
        catch (IOException ioex){
             Log.e("Debug", "error: " + ioex.getMessage(), ioex);
        }
      }

}
