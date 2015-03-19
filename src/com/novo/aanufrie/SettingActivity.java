package com.novo.aanufrie;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

//public class SettingActivity extends PreferenceActivity {
public class SettingActivity extends Activity {
    String myChoice = "";
    int nChoice = 0;
    public SharedPreferences prefs;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Button ApplyBtn;
		Button CancelBtn;
		RadioGroup radioGroup;
		RadioButton radioBtn1;
		RadioButton radioBtn2;
		RadioButton radioBtn3;
		
     	super.onCreate(savedInstanceState);
		//addPreferencesFromResource(R.xml.prefs);
		setContentView(R.layout.activity_setting);

		ApplyBtn  = (Button) findViewById(R.id.sApply);
		CancelBtn = (Button) findViewById(R.id.sCancel);
		radioGroup   = (RadioGroup) findViewById(R.id.radioBalls);
	    radioBtn1 = (RadioButton) findViewById(R.id.radioButton1);
	    radioBtn2 = (RadioButton) findViewById(R.id.radioButton2);
	    radioBtn3 = (RadioButton) findViewById(R.id.radioButton3);
	    
		prefs = PreferenceManager.getDefaultSharedPreferences(this); 
    	myChoice = prefs.getString("newballs", "4");
    	nChoice = Integer.parseInt(myChoice);
    	Log.d("lines", "myChoice " + myChoice );
    	if (nChoice == 3) {
    	   	radioBtn1.setChecked(true);
		} else if (nChoice == 4) {
			radioBtn2.setChecked(true);
		} else if (nChoice == 5) {
			radioBtn3.setChecked(true);
			Log.d("lines", "myChoice set" + myChoice );
		}
    	   	
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radioButton1) {
					myChoice = "3";
				} else if (checkedId == R.id.radioButton2) {
					myChoice = "4";
				} else if (checkedId == R.id.radioButton3) {
					myChoice = "5";
				}
			}
		});
			
			
	    ApplyBtn.setOnClickListener(new OnClickListener() {
          	
	        @Override
	        public void onClick(View arg0) {
	           Editor editor = prefs.edit();
	           editor.putString("newballs", myChoice);
	           editor.commit();
	           finish();
	        }
	    });
	    
	    CancelBtn.setOnClickListener(new OnClickListener() {
          	
            @Override
            public void onClick(View arg0) {
               finish();
            }
        });    
	    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.setting, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
