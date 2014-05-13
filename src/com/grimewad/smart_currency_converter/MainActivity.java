package com.grimewad.smart_currency_converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.widget.Toast;

import org.json.*;

public class MainActivity extends Activity  {
	
	private static Map<String, String> COUNTRIES_CURRENCY_MAP = new HashMap<String, String>();
	
	private double latitude;
	private double longitude;
	
	private String currentCountryCode;
	private String currencyCode;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try{
			loadCountryCurrencyMap();
		}catch(JSONException e){
			Toast.makeText(this, "Error loading Currency Codes", Toast.LENGTH_LONG).show();
			this.finish();
		}
		obtainGPSCordinates();
		int count = 0;
		do{
			currentCountryCode = getCountryCode(getApplicationContext(), latitude, longitude);
			currencyCode = getCurrencyCode(currentCountryCode);
			count++;
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}while(currencyCode == null && count <=100);
		
		Toast.makeText(this, "Currency Code is " + currencyCode, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private String loadJSON(){
		String json = null;
		
		try{
			InputStream inputStream = getAssets().open("countries.json");
			int size = inputStream.available();
			
			byte[] bufferArray = new byte[size];
			inputStream.read(bufferArray);
			inputStream.close();
			
			json = new String(bufferArray, "UTF-8");
		}catch(IOException e){
			Toast.makeText(this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
			return json;
		}
		
		return json;
	}
	
	private void loadCountryCurrencyMap() throws JSONException{
		JSONObject json = new JSONObject(loadJSON());
		JSONArray jsonArray = json.getJSONArray("countries");
		
		for(int i=0; i < jsonArray.length(); i++){
			JSONObject country = jsonArray.getJSONObject(i);
			String countryCode = country.getString("cca2");
			String currencyCode = country.getString("currency");
			currencyCode.replace("[\"", "");
			currencyCode.replace("\"]", "");
			COUNTRIES_CURRENCY_MAP.put(countryCode, currencyCode);
		}
	}
	
	private void obtainGPSCordinates(){
		
		GPSTracker tracker = new GPSTracker(this);
	    if (tracker.canGetLocation() == false) {
	        tracker.showSettingsAlert();
	    } else {
	        latitude = tracker.getLatitude();
	        longitude = tracker.getLongitude();      
	    }
	}
	
	public static String getCountryCode(Context context, double latitude, double longitude) {
		
	    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
	    List<Address> addresses = null;
	    try {
	        addresses = geocoder.getFromLocation(latitude, longitude, 1);
	    } catch (IOException e) {
	    	
	    }
	    if (addresses != null && !addresses.isEmpty()) {
	        return addresses.get(0).getCountryCode();
	    }
	    return null;
	}
	
	public static String getCurrencyCode(String countryCode){
		
		return COUNTRIES_CURRENCY_MAP.get(countryCode);
	}

}
