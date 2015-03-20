package com.novo.aanufrie;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class LinesApplication extends Application implements
		OnSharedPreferenceChangeListener {
	public SharedPreferences prefs;
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d("lines", "LinesApplication onCreate");
	   	prefs = PreferenceManager.getDefaultSharedPreferences(this); 
    	prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
		Log.d("lines", "LinesApplication onTerminate");
	}

}
