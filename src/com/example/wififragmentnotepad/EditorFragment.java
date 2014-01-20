package com.example.wififragmentnotepad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.widget.EditText;

public class EditorFragment extends Fragment implements EditorFragmentInterface {
	private EditText editText;
	public String fileName;
	private boolean pressed_enter = false;
	
	//private Socket socket = null;
	//private boolean socket_is_not_null = false;
	private TcpipWriteThread sendThread = null;
	//private SendThread sendThread;

	
	private int previous_text_length;
	
	//przy tworzeniu komponentu trzeba podaæ plik Ÿród³owy
	//public Editor(File file)
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.editor_fragment, container, false);
		editText = (EditText) view.findViewById(R.id.editorText);
		
		LoadFile();
		return view;
	}
	
	
	private void LoadFile()
	{
		String abc;
        try
        {
        	abc = getStringFromFile(fileName);
        	editText.setText(abc);
        }
        catch(Exception ex)
        {
        	editText.setText(ex.toString());
        }
        int position = editText.length();
        editText.setSelection(position);
        editText.addTextChangedListener(TW);
        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
            	if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER )
            	{
            		if(!pressed_enter)
            		{
            			int pos = editText.getSelectionStart();
            			editText.append("\n");
            			if(sendThread != null)
            			{
            				sendThread.TrySendData("Enter," + pos);
            			}
            			else
            			{
            				Log.i("Key pressed", "enter");
            			}
            			pressed_enter = true;
            			return true;
            		}
            		else
            		{
            			pressed_enter = false;
            			return false;
            		}
            	}
            	return false;
            }});

        previous_text_length = editText.getText().length();
        
        
	}
	
	
	@Override
	public void onPause() {
		//super onPause();
		saveStringToFile(fileName, editText.getText().toString());
		if(sendThread != null)
		{
			sendThread.Stop();
		}
		super.onPause();
	}
	
	@Override
	public void onResume()
	{
		if(sendThread != null)
		{
			sendThread.run();
		}
		super.onResume();
	}
	
	
	public void udpate_string()
	{
		//stringBuffer.insert(cursor_start, insert, 0, insert.length());
	}
	
	private TextWatcher TW = new TextWatcher(){

		@Override
		public void afterTextChanged(Editable arg0) {
			//saveStringToFile(fileName, stringBuffer.toString());
			
		}

		@Override //arg1 - start position of cursor, arg2 - number of changed characters, arg3 - length of new inserted text
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			//previous_text_length = arg3;
			// TODO Auto-generated method stub
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			if(sendThread != null)
			{
				int temp = arg1 + arg3;
				if(temp > previous_text_length)
				{
					Log.i("key pressed", arg0.subSequence(temp - 1, temp).toString());
					sendThread.TrySendData(arg0.subSequence(temp - 1, temp).toString() +"," + arg1);
				}
				else if(temp == previous_text_length)
				{
					//s³owo zatwierdzone, nic nie robiæ i siê cieszyæ
				}
				else
				{
					if(arg0.equals(null))
					{
					Log.i("Key pressed", "backspace");
					}
					sendThread.TrySendData("backspace," + arg1);
				}
				previous_text_length = temp;
			}
		}
		
	};
	
	public static String getStringFromFile (String filePath)
	{
		String ret = "";
		try
		{
			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filePath);
			InputStream inputStream = new FileInputStream(file);
			if(inputStream != null)
			{
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();
				while((receiveString = bufferedReader.readLine()) != null)
				{
					stringBuilder.append(receiveString);
				}
				inputStream.close();
				ret = stringBuilder.toString();
			}
		}
		catch(FileNotFoundException e)
		{
			Log.e("EditorActivity", "File not found: " + e.toString());
		}
		catch(IOException e)
		{
			Log.e("EditorActivity", "Can't read file: " + e.toString());
		}
		return ret;
	}

	private int saveStringToFile(String filePath, String text)
	{
		try
		{
			File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filePath);
			PrintWriter writer = new PrintWriter(file);
			writer.print("");
			writer.close();
			OutputStream os = new FileOutputStream(file);
			byte[] arra = text.getBytes();
			
			os.write(arra);
			os.flush();
			os.close();
			return 1;//success
		}
		catch(Exception e)
		{
			
		}
		return 0;
	}



	@Override
	public void GoToEditorFragment(String filename) {
		fileName = filename;
		//LoadFile();
	}



	@Override
	public void SetConnection(TcpipWriteThread th) {
		sendThread = th;
	}




}



/*
//thread class to communicate over sockets
class SendThread implements Runnable {

	Thread runner;
	public boolean RUN = true;
	public String DataToSend = "";
	private Object ToLock = new Object();
	public Socket kkSocket;
	
	public SendThread(Socket s) {
		//kkSocket = s;
	}
	public SendThread(String threadName, Socket s) {
		kkSocket = s;
		runner = new Thread(this, threadName); // (1) Create a new thread.
		//System.out.println(runner.getName());
		Log.e("New thread started ", runner.getName());
		runner.start(); // (2) Start the thread.
	}
	public void run() {
		//Display info about this particular thread
		//System.out.println(Thread.currentThread());
		try {
			//kkSocket = new Socket("192.168.1.100", 8888);
			PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
			while(RUN)
			{
				synchronized(ToLock)
				{
					if(!DataToSend.equals(""))
					{
						out.println(DataToSend);
						out.flush();
						DataToSend = "";
					}
						
				}
				Thread.sleep(300); //abyœmy mogli siê wgryŸæ w pêtlê dodaj¹c dane
			}
			out.println("#exit");
			out.close();
			kkSocket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
					
		}
	}
	public void Stop()
	{
		RUN = false;
	}
	
	
	//-----------------------Do funkcji podajesz stringa do wys³ania - je¿eli zwróci true -> dane zosta³y dopisane do wys³ania, false -> nie mo¿na dopisaæ, 
	// trzeba wys³aæ ca³y plik przy nastêpnym po³¹czeniu
	//
	public boolean TrySendData(String str)
	{
		int len;
		synchronized(ToLock)
		{
			len = DataToSend.length();
		}
		if(len > 1000)
		{
			return false;
		}
		
		synchronized(ToLock)
		{
			if(DataToSend.equals(""))
			{
				DataToSend = str;
			}
			else
			{
				DataToSend += "\n" + str;
			}
		}
		
		return true;
	}
	
}
*/