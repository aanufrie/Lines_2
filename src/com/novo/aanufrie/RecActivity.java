package com.novo.aanufrie;

import java.util.Calendar;

import com.novo.aanufrie.R;
import com.novo.aanufrie.DbHelper;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class RecActivity extends Activity {
	DbHelper dbHelper;
	public SharedPreferences prefs;
	String newBallsString;
	SQLiteDatabase db;
	Cursor cursor;
	TextView listRec;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int myScore = 10;
        int nRec = 0;
		
		super.onCreate(savedInstanceState);
		Log.d("lines","RecActivity onCreate");
		prefs = PreferenceManager.getDefaultSharedPreferences(this); 
    	//prefs.registerOnSharedPreferenceChangeListener(this);
    	newBallsString = prefs.getString("newballs", "4");
		setContentView(R.layout.activity_rec);
		
		
		listRec = (TextView) findViewById(R.id.listRec);
		dbHelper = new DbHelper(this); 
		//db = dbHelper.getReadableDatabase();
		
		//ContentValues contentValues = new ContentValues();
		db = dbHelper.getWritableDatabase();
		
		cursor = db.query(DbHelper.TABLE, null, null, null, null, null,
				DbHelper.C_CREATED_AT + " DESC"); //
				startManagingCursor(cursor); //
    	while (cursor.moveToNext()) { //
			  nRec++;
    	}
		cursor.close();
    	
        if(nRec == 0) {		
		   ContentValues contentValues = new ContentValues();
		   
		   contentValues.clear();
		   
		   contentValues.put(dbHelper.C_ID, "10");
		   contentValues.put(dbHelper.C_CREATED_AT,Integer.toString(mDay)+"-"+Integer.toString(mMonth)+"-"+Integer.toString(mYear));
		   contentValues.put(dbHelper.C_BALLS_NUMBER, "4");
		   contentValues.put(dbHelper.C_SCORE, "100");
		   contentValues.put(dbHelper.C_USER, "ANDREI");
		
		   try {
		         db.insertWithOnConflict(DbHelper.TABLE, null, contentValues,
		         SQLiteDatabase.CONFLICT_IGNORE); //
		      } finally {
			     Log.d("lines","db.insertWithOnConflict failed");
			  //db.close(); //
		   }
		   contentValues.clear();
		   contentValues.put(dbHelper.C_ID, "20");
		   contentValues.put(dbHelper.C_CREATED_AT,Integer.toString(mDay)+"-"+Integer.toString(mMonth)+"-"+Integer.toString(mYear));
		   contentValues.put(dbHelper.C_BALLS_NUMBER, "5");
		   contentValues.put(dbHelper.C_SCORE, "100");
		   contentValues.put(dbHelper.C_USER, "ANDREI");
		
		   try {
		         db.insertWithOnConflict(DbHelper.TABLE, null, contentValues,
		         SQLiteDatabase.CONFLICT_IGNORE); //
		      } finally {
			     Log.d("lines","db.insertWithOnConflict failed");
			  //db.close(); //
		   }   
		   for(int i =0 ; i< 10; i++ ) {  
		      Log.d("lines","Adding rec to table" + Integer.toString(myScore));
		      contentValues.clear();
		      contentValues.put(dbHelper.C_ID, Integer.toString(i));
		      contentValues.put(dbHelper.C_CREATED_AT,Integer.toString(mDay)+"-"+Integer.toString(mMonth)+"-"+Integer.toString(mYear));
		      contentValues.put(dbHelper.C_BALLS_NUMBER, "3");
		      contentValues.put(dbHelper.C_SCORE, Integer.toString(myScore));
		      contentValues.put(dbHelper.C_USER, "ANDREI");
		
		      try {
		         db.insertWithOnConflict(DbHelper.TABLE, null, contentValues,
		         SQLiteDatabase.CONFLICT_IGNORE); //
		      } finally {
			     Log.d("lines","db.insertWithOnConflict failed");
			  //db.close(); //
		      }
		      myScore = myScore *2;
		   }
		}
		cursor = db.query(DbHelper.TABLE, null, DbHelper.C_BALLS_NUMBER+"="+newBallsString, null, null, null,
		DbHelper.C_SCORE + " DESC"); //
    	//cursor = db.query(DbHelper.TABLE, null, null, null, null, null,
		//DbHelper.C_SCORE + " DESC"); //startManagingCursor(cursor); //
		// Iterate over all the data and print it out
		String level, user, text, output;
		while (cursor.moveToNext()) { //
		level = cursor.getString(cursor.getColumnIndex(DbHelper.C_BALLS_NUMBER));
		user = cursor.getString(cursor.getColumnIndex(DbHelper.C_USER)); //
		text = cursor.getString(cursor.getColumnIndex(DbHelper.C_SCORE));
		output = String.format("%s %s: %s\n", level, user, text); //
		listRec.append(output); //
		}
		Log.d("lines","RecActivity onCreate end");
	}
	public void onDestroy() {
		super.onDestroy();
		// Close the database
		cursor.close();
		db.close(); //
	}
	
	protected void onResume() {
		super.onResume();
		Log.d("lines","RecActivity onResume");
	}
	
	private void restartActivity() {
	    Intent intent = getIntent();
	    finish();
	    startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mainmenu, menu);
		return true;
	}
	

	public boolean onOptionsItemSelected(MenuItem item) {
		//distribute the calls from the menus
		switch (item.getItemId())
		{
			case R.id.action_settings:
				startActivity(new Intent(this, SettingActivity.class));
				
				break;
	    }
		
		return true;
	}
	
	
}
