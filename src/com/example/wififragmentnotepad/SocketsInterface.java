package com.example.wififragmentnotepad;

import java.net.Socket;

public interface SocketsInterface {
	public void resetLog(String command);
	public void addFileToLog(String file);
	public void nowEditingFile(String file);
	public void setSocket(Socket s);
}
