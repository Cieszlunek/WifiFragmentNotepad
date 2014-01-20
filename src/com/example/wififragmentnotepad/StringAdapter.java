package com.example.wififragmentnotepad;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StringAdapter extends BaseAdapter{
	ArrayList<String> lista = new ArrayList<String>();
	
	public void setConnectionList()
	{
		lista.add("TCP/IP");
		lista.add("Wifi Direct");
	}
	
	@Override
	public int getCount() {
		return lista.size();
	}

	@Override
	public Object getItem(int position) {
		return lista.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if(view == null)
		{
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			view = inflater.inflate(R.layout.list_item, parent, false);
		}
		TextView tw = (TextView) view.findViewById(R.id.name_view);
		tw.setText(String.valueOf(lista.get(position)));
		return view;
	}

	public void setMasterOrSlaveList() {
		lista.add("Master");
		lista.add("Slave");
		
	}

}
