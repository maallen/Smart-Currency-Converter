package com.grimewad.smart_currency_converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Toast;

import org.json.*;

public class MainActivity extends Activity {
	
	private Map<String, String> countriesCurrenciesMap = new HashMap<String, String>();

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
		
		for(int i=0; i <= jsonArray.length(); i++){
			JSONObject country = jsonArray.getJSONObject(i);
			String countryCode = country.getString("cca2");
			String currencyCode = country.getString("currency");
			countriesCurrenciesMap.put(countryCode, currencyCode);
		}
	}

}
