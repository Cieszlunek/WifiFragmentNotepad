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
	private String fileName;
	private boolean enter_is_pressed;
	private boolean backspaceIsPressed;
	
	private static ThreadInterface threadInterface = null;
	public static boolean pressed_key = true;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.editor_fragment, container, false);
		editText = (EditText) view.findViewById(R.id.editorText);
		enter_is_pressed = false;
		backspaceIsPressed = false;
		LoadFile();
		return view;
	}
		
	private void LoadFile() {
		String abc;
		
        try {
        	abc = getStringFromFile(fileName);
        	editText.setText(abc);
        }
        catch(Exception ex) {
        	editText.setText(ex.toString());
        }
        
        int position = editText.length();
        editText.setSelection(position);
        editText.addTextChangedListener(TW);
        editText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
            	int pos = editText.getSelectionEnd();
            	if (event.getAction() != KeyEvent.ACTION_DOWN) {
            		return false;
            	}
            	else {
	            	if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
	            		return false;
	            	}
	            	else if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
	            		enter_is_pressed = true;
		            	if(threadInterface != null) {
		            		threadInterface.TrySendData("Enter," + pos);
		            	}
		            	else {
		            		Log.i("Key pressed enter", String.valueOf(pos));
		            	}
		            	
		            	if(pos == 0) {
			    			editText.setText(System.getProperty("line.separator") + editText.getText());
			    		}
			    		else if(pos >= editText.getText().length()) {
			    			editText.append(System.getProperty("line.separator"));
			    		}
			    		else {
			    			String pre = String.valueOf( editText.getText().subSequence(0, pos));
			    			pre += System.getProperty("line.separator") + String.valueOf(editText.getText().subSequence(pos, editText.getText().length()));
			    			editText.setText((CharSequence)pre);
			    		}
		            	
		            	return true;	
	        		}
	            	else if(event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
	            		backspaceIsPressed = true;
	            		if(threadInterface != null) {
	        				threadInterface.TrySendData("backspace," + editText.getSelectionStart());
	        			}
	            		else {
	            			Log.i("Key pressed backspace", String.valueOf(pos));
	            		}
	            		return false;
	            	}
            	}
            	
            	return false;
            }});
	}
	
	@Override
	public void onPause() {
		if (1 == saveStringToFile(fileName, editText.getText().toString())) {
			Log.e("saved file", fileName);
		} 
		else {
			Log.e("unsaved file", fileName);
		}
		if(threadInterface != null) {
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
	public void onStop() {
		saveStringToFile(fileName, editText.getText().toString());
		if(threadInterface != null) {
			threadInterface.Stop();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		super.onStop();
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	private TextWatcher TW = new TextWatcher(){

		@Override
		public void afterTextChanged(Editable arg0) {
		}

		@Override //arg1 - start position of cursor, arg2 - number of changed characters, arg3 - length of new inserted text
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}

		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			if(enter_is_pressed) {
				enter_is_pressed = false;
				return;
			}
			if (backspaceIsPressed) {
				backspaceIsPressed = false;
				return;
			}
			if(threadInterface != null && pressed_key) {
				if( ("").equals(arg0) || arg0 == null || (System.getProperty("line.separator")).equals(arg0)) {
					return;
				}
				int temp = arg1 + arg3;
				if(threadInterface != null) {
					threadInterface.TrySendData(arg0.subSequence(temp - 1, temp).toString() +"," + arg1);
				}
				else {
					Log.i("key pressed", arg0.subSequence(temp - 1, temp).toString());
				}
			}
			else {
				Log.e("thread interface", " is null. key: " + pressed_key);
			}
			
		}
		
	};
	
	public static String getStringFromFile (String filePath) {
		String ret = "";
		try {
			File file = new File(filePath);
			InputStream inputStream = new FileInputStream(file);
			if(inputStream != null) {
				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();
				while((receiveString = bufferedReader.readLine()) != null) {
					if(receiveString.trim().isEmpty()) {
						stringBuilder.append(System.getProperty("line.separator"));
					}
					else {
						stringBuilder.append(receiveString + System.getProperty("line.separator"));
					}
				}
				inputStream.close();
				ret = stringBuilder.toString();
				String[] temp = ret.split(System.getProperty("line.separator"));
				int sum = 0;
				for(int i = 0; i < temp.length; ++i) {
					if(threadInterface != null) {
						if(temp[i].isEmpty()) {
							if(sum != 0 && temp.length > 1) {
								threadInterface.TrySendData( "Enter" + "," + sum );
								sum += 1;
							}
						}
						else {
							threadInterface.TrySendData( temp[i] + "," + sum );
							sum += temp[i].length()- 1 ;
							threadInterface.TrySendData( "Enter" + "," + sum );
							sum += 1;
						}
					}
				}
			}
		}
		catch(FileNotFoundException e) {
			Log.e("EditorActivity", "File not found: " + e.toString());
		}
		catch(IOException e) {
			Log.e("EditorActivity", "Can't read file: " + e.toString());
		}
		return ret;
	}

	private int saveStringToFile(String filePath, String text) {
		try {
			File file = new File(filePath);
			PrintWriter writer = new PrintWriter(file);
			writer.print("");
			writer.close();
			OutputStream os = new FileOutputStream(file);
			byte[] arra = text.getBytes();
			
			os.write(arra);
			os.flush();
			os.close();
			return 1;
		}
		catch(Exception e) {
			Log.e("Exception while saving file", e.getMessage());
		}
		return 0;
	}

	@Override
	public void GoToEditorFragment(String filename) {
		fileName = filename;
	}

	@Override
	public void SetConnection(ThreadInterface ti) {
		threadInterface = ti;
	}

	@Override
	public void SendData(String data) {
		final String[] str = data.split(",");
		final int position = Integer.parseInt(str[1]);
	
		getActivity().runOnUiThread(new Runnable() {            
		    @Override
		    public void run() {
		    	Log.e("tag", "Receive data, want to print on screen");
		    	int pos_archaic = editText.getSelectionEnd();
		    	Editable old = editText.getText();
		    	if( ("Enter").equals(str[0]) ) {
		    		if(position == 0) {
		    			editText.setText(System.getProperty("line.separator") + old);
		    		}
		    		else if(position == old.length()) {
		    			editText.append(System.getProperty("line.separator"));
		    		}
		    		else {
		    			String pre = String.valueOf( old.subSequence(0, position));
		    			pre += System.getProperty("line.separator") + String.valueOf(old.subSequence(position, old.length()));
		    			editText.setText((CharSequence)pre);
		    		}
		    		editText.setSelection(pos_archaic+1);
		    	}
		    	else if(("backspace").equals(str[0])) {
		    		String neww = null;
	                if (position - 1 > 0) {
	                	if (old.length() > position) {
		                    neww = String.valueOf(old.subSequence(0, position));
		                    String t = String.valueOf(old.subSequence(position, old.length()));
		                    neww += t;
	                	}
	                	else {
	                		neww = String.valueOf(old.subSequence(0, old.length() - 1));
	                	}
	                	editText.setSelection(pos_archaic-1);
	                }
	                else {
	                    if (old.length() > 1) {
	                        neww = String.valueOf(old.subSequence(1, old.length()));
	                        editText.setSelection(pos_archaic-1);
	                    }
	                    else {
	                        neww = "";
	                    }
	                }
	                editText.setText((CharSequence)neww);
		    	}
		    	else if (position < old.length()) {
	                String neww = String.valueOf(old.subSequence(0, position));
	               
	                String t = String.valueOf(old.subSequence(position, old.length()));
	                neww += str[0] + t;
	                editText.setText( (CharSequence)neww );
	                editText.setSelection(pos_archaic+1);
	            }
	            else {
	                editText.append((CharSequence)str[0]);
	                editText.setSelection(pos_archaic+1);
	            }
		    	editText.refreshDrawableState();
		    	pressed_key = true;
		    	
		    }
		});
	}
}

