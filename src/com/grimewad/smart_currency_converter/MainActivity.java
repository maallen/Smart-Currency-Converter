package com.grimewad.smart_currency_converter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;

public class MainActivity extends Activity  {
	
	private static Map<String, String> COUNTRIES_CURRENCY_MAP = new HashMap<String, String>();
	private static String GOOGLE_MAPS_API_ADDRESS = "https://maps.googleapis.com/maps/api/geocode/json?latlng=%1$s,%2$s&sensor=true_or_false";

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
		currentCountryCode = getCountryCode(getApplicationContext(), latitude, longitude);
		currencyCode = getCurrencyCode(currentCountryCode);
		
		Toast.makeText(this, "Currency Code is " + currencyCode, Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(COUNTRIES_CURRENCY_MAP.isEmpty()){
			try{
				loadCountryCurrencyMap();
			}catch(JSONException e){
				Toast.makeText(this, "Error loading Currency Codes", Toast.LENGTH_LONG).show();
				this.finish();
			}
		}
		obtainGPSCordinates();
		currentCountryCode = getCountryCode(getApplicationContext(), latitude, longitude);
		currencyCode = getCurrencyCode(currentCountryCode);
		
		Toast.makeText(this, "New Currency Code is " + currencyCode, Toast.LENGTH_LONG).show();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		COUNTRIES_CURRENCY_MAP = null;
		currentCountryCode = null;
		currencyCode = null;
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
	
	public String getCountryCode(Context context, double latitude, double longitude) {
		
	    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
	    List<Address> addresses = null;
	    try {
	        addresses = geocoder.getFromLocation(latitude, longitude, 1);
	    } catch (IOException e) {
	    	
	    }
	    if (addresses != null && !addresses.isEmpty()) {
	        return addresses.get(0).getCountryCode();
	    }else{
	    	/*
	    	 * Geocoder hasn't returned a location so falling back to Google Maps API
	    	 * to retrieve the country code
	    	 */
	    	try {
				return retrieveCountryCodeFromGoogleMapsAPI(latitude, longitude);
			} catch (JSONException e) {
				return null;
			}
	    }

	}
	
	private String retrieveCountryCodeFromGoogleMapsAPI(double latitude, double longitude) throws JSONException{
		
		StringBuilder stringBuilder = new StringBuilder();
		String httpAddress = String.format(GOOGLE_MAPS_API_ADDRESS, latitude, longitude);
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet get = new HttpGet(httpAddress);
		try {
			HttpResponse response = httpClient.execute(get);
			if(response.getStatusLine().getStatusCode() == 200){
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while((line = reader.readLine()) != null){
					stringBuilder.append(line);
				}
			}else{
				Toast.makeText(getApplicationContext(), "Error retrieving JSON from Google Maps API", Toast.LENGTH_LONG).show();
			}
			
		} catch (ClientProtocolException e) {

		} catch (IOException e) {
			
		}
		
		JSONObject json = new JSONObject(stringBuilder.toString());
		JSONArray addressComponents = json.getJSONArray("results").getJSONArray(0);
		for(int i = 0; i < addressComponents.length(); i++){
			if(addressComponents.getJSONObject(i).getJSONArray("types").getString(1).equals("country")){
				return addressComponents.getJSONObject(i).getString("short_name");
			}
		}
		
		return null;
	}
	
	public String getCurrencyCode(String countryCode){
		
		return COUNTRIES_CURRENCY_MAP.get(countryCode);
	}

}
