package com.example.wififragmentnotepad;


public interface EditorFragmentInterface {
	
	public void GoToEditorFragment(String filename);
	public void SetConnection(TcpipWriteThread th);
}
