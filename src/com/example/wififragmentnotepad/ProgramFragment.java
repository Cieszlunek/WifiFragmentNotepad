package com.example.wififragmentnotepad;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class ProgramFragment extends Fragment {
	private LastOpenedFilesAdapter adapter;
	private ListView lw;
	private onEditEventListener l;
	
	@Override
	  public void onAttach(Activity activity) {
	    super.onAttach(activity);
	        try {
	          l = (onEditEventListener) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString() + " must implement onSomeEventListener");
	        }
	  }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.program_fragment, container, false);
		lw = (ListView) view.findViewById(R.id.last_opened_files);
		adapter = new LastOpenedFilesAdapter(getActivity());
		lw.setAdapter(adapter);
		lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				LastOpenedFile lof = (LastOpenedFile) adapter.getItem(arg2);
				l.listOpenFile(lof.getPatch());
			}
		});
		return view;
	}
	
	public interface onEditEventListener {
	    public void listOpenFile(String s);
	  }
}
