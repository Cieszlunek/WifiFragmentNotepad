package com.example.wififragmentnotepad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelper {
	private DatabaseClass dbc;
	private SQLiteDatabase database;
	private Context context;
	
	public DatabaseHelper(Context context)
	{
		this.context = context;
		dbc = new DatabaseClass(context);
		database = dbc.getWritableDatabase();
	}
	
	public void saveLastOpenedFile(String name, String patch)
	{
		Cursor cursor = getAllLastOpenedFiles();
		boolean thisFileExists = false;
		if(cursor.moveToFirst())
		{
			do
			{
				if(name.equals(cursor.getString(1)) && patch.equals(cursor.getString(2)))
				{
					thisFileExists = true;
					break;
				}
			}while(cursor.moveToNext());
		}
		if(!cursor.isClosed())
		{
			cursor.close();
		}
		if(!thisFileExists)
		{
			ContentValues contentValues = new ContentValues();
			contentValues.put("name", name);
			contentValues.put("patch", patch);
			database.insert("last_opened_files", null, contentValues);
		}
	}
	
	public void saveSharedFile(String name, String patch)
	{
		Cursor cursor = getAllSharedFiles();
		boolean thisFileExists = false;
		if(cursor.moveToFirst())
		{
			do
			{
				if(name.equals(cursor.getString(1)) && patch.equals(cursor.getString(2)))
				{
					thisFileExists = true;
					break;
				}
			}while(cursor.moveToNext());
		}
		if(!cursor.isClosed())
		{
			cursor.close();
		}
		if(!thisFileExists)
		{
			ContentValues contentValues = new ContentValues();
			contentValues.put("name", name);
			contentValues.put("patch", patch);
			contentValues.put("time", "not implemented yet");
			database.insert("shared_files", null, contentValues);
		}
	}
	
	public Cursor getAllLastOpenedFiles() {
		return database.rawQuery("select * from last_opened_files", null);
	}
	
	public Cursor getAllSharedFiles() {
		return database.rawQuery("select * from shared_files", null);
	}
	
	public void onPause()
	{
		database.close();
		dbc.close();
	}
	
	public void onResrume()
	{
		dbc = new DatabaseClass(context);
		database = dbc.getWritableDatabase();
	}
}
