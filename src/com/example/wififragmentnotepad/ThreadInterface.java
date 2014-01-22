package com.example.wififragmentnotepad;

public interface ThreadInterface {
	public void Stop();
	public void Initialize(String ip, int port, boolean isGroupOwner);
	public void TrySendData(String data);
	public void Restart();
	public void setEditorFragment(EditorFragmentInterface editorFragmentInterface);
}
