package com.novo.aanufrie;

import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
	static final String DB_NAME = "results.db";
	static final int DB_VERSION = 1;
	static final String TABLE = "results";
	
	static final String C_ID = "id";
	static final String C_BALLS_NUMBER = "level";
	static final String C_CREATED_AT = "created_at";
	static final String C_SCORE = "score";
	static final String C_USER = "user";
	Context context;
	
	
	public DbHelper(Context context) {
		super(context,DB_NAME,null,DB_VERSION);
		this.context = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int myScore = 10;
        int nRec = 0;
        Log.d("lines","onCreate -execSQL - CREATE");
		// TODO Auto-generated method stub
        String sql = " CREATE TABLE " + TABLE +
        		"( " + C_ID + " INT PRIMARY KEY, " +
        		//"(" +
         		C_CREATED_AT + " TEXT, " +
         		C_BALLS_NUMBER + " int, " +
         		C_SCORE + " int, " +
        		C_USER + " text); ";
        db.execSQL(sql); 
        /*ContentValues contentValues = new ContentValues();
		contentValues.clear();
		contentValues.put(C_BALLS_NUMBER, 3);
		contentValues.put(C_CREATED_AT,""+mDay+"-"+mMonth+"-"+mYear);
		contentValues.put(C_SCORE, Integer.toString(100));
		contentValues.put(C_USER, "ANDREI");
		
		try {
	      db.insertWithOnConflict(DbHelper.TABLE, null, contentValues,
	      SQLiteDatabase.CONFLICT_IGNORE); //
        } finally {
		     //db.close(); //
	    }
	 
		contentValues.clear();
		contentValues.put(C_BALLS_NUMBER, 4);
		contentValues.put(C_CREATED_AT,""+mDay+"-"+mMonth+"-"+mYear);
		contentValues.put(C_SCORE, Integer.toString(101));
		contentValues.put(C_USER, "ANDREI");
			
		try {
		   db.insertWithOnConflict(DbHelper.TABLE, null, contentValues,
		   SQLiteDatabase.CONFLICT_IGNORE); //
	    } finally {
			     //db.close(); //
		}
		contentValues.clear();
		contentValues.put(C_BALLS_NUMBER, 5);
		contentValues.put(C_CREATED_AT,""+mDay+"-"+mMonth+"-"+mYear);
		contentValues.put(C_SCORE, Integer.toString(102));
		contentValues.put(C_USER, "ANDREI");
			
		try {
		   db.insertWithOnConflict(DbHelper.TABLE, null, contentValues,
		   SQLiteDatabase.CONFLICT_IGNORE); //
	    } finally {
			     //db.close(); //
		}*/
 	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
        db.execSQL("drop table if exists " + TABLE + ";");
        onCreate(db);
	}
}
