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
	public Object ToLockEditor = new Object();
	
	//private Socket socket = null;
	//private boolean socket_is_not_null = false;
	private static ThreadInterface threadInterface = null;
	//private SendThread sendThread;

	
	private int previous_text_length;
	
	//przy tworzeniu komponentu trzeba poda� plik �r�d�owy
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
            		//TODO enter
            		if(!pressed_enter)
            		{
            			int pos = editText.getSelectionStart();
            			Editable old = editText.getText();
            			int length = old.length();
            			synchronized(ToLockEditor)
            			{
	            			if(pos == length)
	            			{
	            				editText.append(System.getProperty("line.separator"));
	            			}
	            			else if(pos == 0)
	            			{
	            				editText.setText(System.getProperty("line.separator") + old);
	            			}
	            			else
	            			{
		            			CharSequence pre = old.subSequence(0, pos);
		            			CharSequence after = old.subSequence(pos, old.length());
		            			editText.setText(pre + System.getProperty("line.separator") + after); //   append(System.getProperty("line.separator"));
	            			}
            			}
            			if(threadInterface != null)
            			{
            				threadInterface.TrySendData("Enter," + pos);
            			}
            			else
            			{
            				Log.i("Key pressed enter", String.valueOf(pos));
            			}
            			pressed_enter = true;//aby enter nie odpali� si� dwa razy - niweluje b��d
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
		if (1 == saveStringToFile(fileName, editText.getText().toString()))
		{
			Log.e("saved file", fileName);
		} 
		else
		{
			Log.e("unsaved file", fileName);
		}
		if(threadInterface != null)
		{
			threadInterface.Stop();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		super.onPause();
	}
	
	@Override
	public void onStop()
	{
		saveStringToFile(fileName, editText.getText().toString());
		if(threadInterface != null)
		{
			threadInterface.Stop();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// 
				e.printStackTrace();
			}
		}
		super.onStop();
	}
	
	
	@Override
	public void onResume()
	{
		//if(threadInterface != null)
		//{
		//	threadInterface.Restart();
		//}
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
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			if(threadInterface != null)
			{
				//TODO key
				int temp = arg1 + arg3;
				//int length = 
				if(temp > previous_text_length)
				{
					Log.i("key pressed", arg0.subSequence(temp - 1, temp).toString());
					threadInterface.TrySendData(arg0.subSequence(temp - 1, temp).toString() +"," + arg1);
				}
				else if(temp == previous_text_length)
				{
					//s�owo zatwierdzone, nic nie robi� i si� cieszy�
				}
				else
				{
					if(arg0.equals(null))
					{
					Log.i("Key pressed", "backspace");
					}
					threadInterface.TrySendData("backspace," + arg1);
				}
				previous_text_length = temp;
			}
			else
			{
				Log.e("thread interface", " is null");
			}
		}
		
	};
	
	public static String getStringFromFile (String filePath)
	{
		String ret = "";
		try
		{
			File file = new File(filePath);
			InputStream inputStream = new FileInputStream(file);
			if(inputStream != null)
			{
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();
				while((receiveString = bufferedReader.readLine()) != null)
				{
					if(receiveString.trim().isEmpty())
					{
						stringBuilder.append(System.getProperty("line.separator"));
					}
					else
					{
						stringBuilder.append(receiveString + System.getProperty("line.separator"));
					}
				}
				inputStream.close();
				ret = stringBuilder.toString();
				String[] temp = ret.split(System.getProperty("line.separator"));
				int sum = 0;
				for(int i = 0; i < temp.length; ++i)
				{
					if(threadInterface != null)
					{
						if(temp[i].isEmpty())
						{
							threadInterface.TrySendData( "Enter" + "," + sum );
							sum += 1;
						}
						else
						{
							threadInterface.TrySendData( temp[i] + "," + sum );
							sum += temp[i].length()- 1 ;
							threadInterface.TrySendData( "Enter" + "," + sum );
							sum += 1;
						}
					}
				}
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
			File file = new File(filePath);
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
			Log.e("Exception while saving file", e.getMessage());
		}
		return 0;
	}



	@Override
	public void GoToEditorFragment(String filename) {
		fileName = filename;
		//LoadFile();
	}



	@Override
	public void SetConnection(ThreadInterface ti) {
		threadInterface = ti;
	}


	@Override
	public void SendData(String data) {
		final String[] str = data.split(",");
		final int position;
		position = Integer.parseInt(str[1]);
		
		getActivity().runOnUiThread(new Runnable() {            
		    @Override
		    public void run() {
		    	Editable old = editText.getText();
		    	if( ("Enter").equals(str[0]) )
		    	{
		    		if(position == 0)
		    		{
		    			editText.setText(System.getProperty("line.separator") + old);
		    		}
		    		else if(position == old.length())
		    		{
		    			editText.append(System.getProperty("line.separator"));
		    		}
		    		else
		    		{
		    			String pre = (String) old.subSequence(0, position);
		    			pre += System.getProperty("line.separator") + (String)(old.subSequence(position, old.length()));
		    			editText.setText((CharSequence)pre);
		    		}
		    	}
		    	else if(("backspace").equals(str[0]))
		    	{
		    		String neww = null;
	                if (position - 1 > 0)
	                {
	                    neww = (String) old.subSequence(0, position);
	                    String t = (String) old.subSequence(position + 1, old.length());
	                    neww += t;
	                }
	                else
	                {
	                    if (old.length() > 1)
	                    {
	                        neww = (String) old.subSequence(1, old.length() - 1);
	                    }
	                    else
	                    {
	                        neww = "";
	                    }
	                }
	                editText.setText((CharSequence)neww);
		    	}
		    	else if (position != old.length())
	            {
	                String neww = (String) old.subSequence(0, position);
	               
	                String t = (String) old.subSequence(position, old.length()-1);
	                neww += str[0] + t;
	                editText.setText( (CharSequence)neww );
	            }
	            else
	            {
	                editText.append(str[0]);// += str[0];
	            }
		    }
		});
	}

	//TODO implement receive data


}

