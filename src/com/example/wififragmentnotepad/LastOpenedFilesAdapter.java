package com.example.wififragmentnotepad;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LastOpenedFilesAdapter extends BaseAdapter{

	private ArrayList<LastOpenedFile> files = new ArrayList<LastOpenedFile>();
	
	public LastOpenedFilesAdapter(Activity activity)
	{
		DatabaseHelper databaseHelper = new DatabaseHelper(activity);
		//files.add()
		Cursor cursor = databaseHelper.getAllLastOpenedFiles();
		if(cursor.moveToFirst())
		{
			do
			{
				files.add(new LastOpenedFile(cursor.getString(1), cursor.getString(2)));
			}while(cursor.moveToNext());
		}
		if(!cursor.isClosed())
		{
			cursor.close();
		}
	}
	
	@Override
	public int getCount() {
		return files.size();
	}

	@Override
	public Object getItem(int arg0) {
		return files.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int index, View view, ViewGroup parent) {
		if(view == null)
		{
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			view = inflater.inflate(R.layout.list_item, parent, false);
		}
		LastOpenedFile lof = files.get(index);
		TextView tw = (TextView) view.findViewById(R.id.name_view);
		tw.setText(lof.getName());
		return view;
	}

}
