package com.example.wififragmentnotepad;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseClass extends SQLiteOpenHelper{

	public DatabaseClass(Context context) {
		super(context, "myDatabase.db", null, 3);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(
				"CREATE TABLE last_opened_files " +
				"(id INTEGER PRIMARY KEY, name TEXT, patch TEXT)"
				);
		db.execSQL(
				"CREATE TABLE shared_files " +
				"(id INTEGER PRIMARY KEY, name TEXT, patch TEXT, time TEXT)"
				);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS last_opened_files");
		db.execSQL("DROP TABLE IF EXISTS shared_files");
		onCreate(db);	
	}

	
	
}
