package com.example.wififragmentnotepad;


public interface EditorFragmentInterface {
	
	public void GoToEditorFragment(String filename);
	public void SetConnection(ThreadInterface th);
	public void SendData(String data);
}
