package com.elaborandofuturo.animalfitness;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;



public class MyLocationListener extends IntentService implements LocationListener  {
	
	Activity act;
	private LocationManager locationManager;
	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in
	// Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in
	// Milliseconds

	
	public MyLocationListener() {
		super("MyLocationListener");
	}

	public void onLocationChanged(Location location) {
		String message = String.format(
				"New Location \n Longitude: %1$s \n Latitude: %2$s",
				location.getLongitude(), location.getLatitude());
		Toast.makeText(act, message, Toast.LENGTH_LONG).show();
		Console.log(setposition(location.getLongitude(), location.getLatitude()));
	}

	public void onStatusChanged(String s, int i, Bundle b) {
		Toast.makeText(act, "Provider status changed", Toast.LENGTH_LONG)
				.show();
	}

	public void onProviderDisabled(String s) {
		Toast.makeText(act,
				"Provider disabled by the user. GPS turned off",
				Toast.LENGTH_LONG).show();
	}

	public void onProviderEnabled(String s) {
		Toast.makeText(act, "Provider enabled by the user. GPS turned on",
				Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String urlToDownload = intent.getStringExtra("url");
		
		
		locationManager = (LocationManager) act
				.getSystemService(act.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				MINIMUM_TIME_BETWEEN_UPDATES,
				MINIMUM_DISTANCE_CHANGE_FOR_UPDATES, new MyLocationListener());
	}
	
	@SuppressLint("NewApi") 
	private String setposition(double d, double f) {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		StrictMode.setThreadPolicy(policy); 
		// Create a new HttpClient and Post Header
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://animalfitness.co/index.php?option=com_appfitnessmap");

	    try {
	        // Add your data
	    	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("id", Propiedades.id));
	        nameValuePairs.add(new BasicNameValuePair("latitude", d+""));
	        nameValuePairs.add(new BasicNameValuePair("longitud", f+""));
	        nameValuePairs.add(new BasicNameValuePair("type", "recoge"));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);

            // According to the JAVA API, InputStream constructor do nothing. 
            //So we can't initialize InputStream although it is not an interface
            InputStream inputStream = response.getEntity().getContent();

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder stringBuilder = new StringBuilder();

            String bufferedStrChunk = null;

            while((bufferedStrChunk = bufferedReader.readLine()) != null){
                stringBuilder.append(bufferedStrChunk);
            }
            return stringBuilder.toString();
            }
	    	catch (Exception e){
	    		Log.d(Propiedades.TAG, e.toString());
	    		return null;
	    	}
	}

}