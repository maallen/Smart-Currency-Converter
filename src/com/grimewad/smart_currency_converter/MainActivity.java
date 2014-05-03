package com.grimewad.smart_currency_converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.location.Criteria;
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
	
	private Map<String, String> countriesCurrenciesMap = new HashMap<String, String>();
	
	private double latitude;
	private double longitude;


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
		
		GPSTracker tracker = new GPSTracker(this);
	    if (tracker.canGetLocation() == false) {
	        tracker.showSettingsAlert();
	    } else {
	        latitude = tracker.getLatitude();
	        longitude = tracker.getLongitude();
	        
	        Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + 
	        		"\nLong: " + longitude, Toast.LENGTH_LONG).show(); 
	    }
	
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
			countriesCurrenciesMap.put(countryCode, currencyCode);
		}
	}
	
	  public static class ErrorDialogFragment extends DialogFragment {
	        // Global field to contain the error dialog
	        private Dialog mDialog;
	        // Default constructor. Sets the dialog field to null
	        public ErrorDialogFragment() {
	            super();
	            mDialog = null;
	        }
	        // Set the dialog to display
	        public void setDialog(Dialog dialog) {
	            mDialog = dialog;
	        }
	        // Return a Dialog to the DialogFragment.
	        @Override
	        public Dialog onCreateDialog(Bundle savedInstanceState) {
	            return mDialog;
	        }
	    }

}
