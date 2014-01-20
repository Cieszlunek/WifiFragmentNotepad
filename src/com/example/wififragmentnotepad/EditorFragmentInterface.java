package com.example.wififragmentnotepad;

import java.net.Socket;

public interface EditorFragmentInterface {
	
	public void GoToEditorFragment(String filename);
	public void SetConnection(Socket s);
}
