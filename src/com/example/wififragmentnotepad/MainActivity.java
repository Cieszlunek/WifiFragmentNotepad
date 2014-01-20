package com.example.wififragmentnotepad;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import com.example.wififragmentnotepad.ProgramFragment.onEditEventListener;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends Activity implements onEditEventListener, ConnectionInterface, SocketsInterface{

	//variables
	private Spinner spinner1;
	private int selected = 0, previous = 0;
    private File mPath;   
    private FileDialog fileDialog;
    private NewFileDialog newFileDialog;
    private Activity activity;
	public String fileName;    
	private EditorFragment editorFragment;
	private boolean editing = false;
	private boolean connected = false;
	private Socket socket = null;
	private String log = "";
	private DatabaseHelper databaseHelper;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        databaseHelper = new DatabaseHelper(this);
        LoadLog();
        CreateSpinner1();
        
    }

    private void LoadLog()
    {
    	log = "#file kuba.txt";
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private void CreateSpinner1()
    {
    	spinner1 = (Spinner) findViewById(R.id.spinner1);
        LoadProgramFragment();
        
        spinner1.setOnItemSelectedListener(new OnItemSelectedListener()
        {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				previous = selected;
				selected = arg2;
				if(selected != previous)
				UpdateFragment(arg2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        });
    }
    
    private void UpdateFragment(int j) {
		// TODO Auto-generated method stub    	
		if(j == 0)
		{
			LoadProgramFragment();
		}
		else if(j == 1)
		{
			OnOpenFile();
		}
		else if(j == 2)
		{
			OnCreateFile();
		}
		else if(j == 3)
		{
			LoadConnectionFragment();
		}
		else if(j == 4)
		{
			LoadSettingsFragment();
		}
	}
    
    private void LoadProgramFragment()
    {
    	FragmentManager fragmentManager = getFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	
    	if(editing)
    	{
    		editorFragment = new EditorFragment();
    		editorFragment.GoToEditorFragment(fileName);
    		if(connected)
    		{
    			editorFragment.SetConnection(socket);
    		}
        	fragmentTransaction.replace(R.id.fragment_layout_1, editorFragment);
        	fragmentTransaction.commit();
    	}
    	else if(selected == previous)
    	{
    		ProgramFragment fragment = new ProgramFragment();
    		fragmentTransaction.add(R.id.fragment_layout_1, fragment);
    		fragmentTransaction.commit();
    	}
    	else
    	{
    		ProgramFragment fragment = new ProgramFragment();
    		fragmentTransaction.replace(R.id.fragment_layout_1, fragment);
        	fragmentTransaction.commit();
    	}
    }
    
    private void OnOpenFile()
    {
    	mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
    	fileDialog = new FileDialog(this, mPath);
        fileDialog.setFileEndsWith(".txt");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
            	
        		fileName = file.getName();
        		databaseHelper.saveLastOpenedFile(fileName, fileName);
				databaseHelper.saveSharedFile(fileName, fileName);
        		EditFile();
            }
        });
        fileDialog.createFileDialog();
        if(previous != 1 && previous != 2)
        	spinner1.setSelection(previous);
    }
    
    private void OnCreateFile()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Enter file name");
		final EditText input = new EditText(activity);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				String t = input.getText().toString();
				//check if file exists / create file
				File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + t + ".txt");
				if(!file.exists())
				{
					//et.setText(t);
				}
				try
				{
					file.createNewFile();
					newFileDialog = new NewFileDialog(activity);
					fileName = t + ".txt";
					databaseHelper.saveLastOpenedFile(fileName, fileName);
					databaseHelper.saveSharedFile(fileName, fileName);
					EditFile();
				}
				catch(Exception ex)
				{
				
				}
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		});
		Dialog dialog = builder.create();
		dialog.show();
					

        if(editing)
        	spinner1.setSelection(previous);
    }
    
    private void LoadConnectionFragment()
    {
    	//FragmentManager fragmentManager = getFragmentManager();
    	//FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	//ConnectionFragment fragment1 = new ConnectionFragment();
    	//fragmentTransaction.add(R.id.fragment_layout_1, fragment);
    	//fragmentTransaction.replace(R.id.fragment_layout_1, fragment1);
    	//fragmentTransaction.commit();
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Select connection type");
		final StringAdapter adapter = new StringAdapter();
		adapter.setConnectionList();
		builder.setAdapter(adapter, new OnClickListener(){
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				setConnectionType(String.valueOf(adapter.getItem(arg1)));
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		});
		Dialog dialog = builder.create();
		dialog.show();
    }
    private void LoadSettingsFragment()
    {
    	FragmentManager fragmentManager = getFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	SettingsFragment fragment = new SettingsFragment();
    	fragmentTransaction.replace(R.id.fragment_layout_1, fragment);
    	fragmentTransaction.commit();
    }
    
    public void EditFile()
    {
    	FragmentManager fragmentManager = getFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	EditorFragment editorFragment = new EditorFragment();
		editorFragment.GoToEditorFragment(fileName);
		editorFragment.SetConnection(socket);
    	fragmentTransaction.replace(R.id.fragment_layout_1, editorFragment);
    	fragmentTransaction.commit();
    	editing = true;
    }

	@Override
	public void listOpenFile(String s) {
		fileName = s;
		EditFile();
	}
	
	@Override
	public void setConnectionType(String s)
	{
		if(s == "TCP/IP")
		{
			TcpIpConnectionType();
		}
		else
		{
			WifiDirectConnectionType();
		}
	}
    
	private void TcpIpConnectionType()
	{
		TcpipWriteThread TcpipThread = new TcpipWriteThread("thread1", log, "192.168.1.100", 8888, activity);
	}
	private void WifiDirectConnectionType()
	{
		
	}

	@Override
	public void resetLog(String command) {
		if(command == "reset")
		{
			log = "";
		}
	}

	@Override
	public void addFileToLog(String file) {
		if(log == "")
		{
			log = "#file " + file;
		}
		else
		{
			log +="\n#file " + file;
		}
	}

	@Override
	public void nowEditingFile(String file) {
		if(log == "")
		{
			log = "#now " + file;
		}
		else
		{
			log += "\n#now " + file;
		}
	}

	@Override
	public void setSocket(Socket s) {
		socket = s;
	}
	
	@Override
	public void onStop()
	{
		if(socket != null)
		{
			try {
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				out.println("#exit");
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		super.onStop();
	}
}





class TcpipWriteThread implements Runnable {
	Thread runner;
	private String log = null;
	private String IP;
	private int port;
	private Socket kkSocket;
	private boolean resetLog = false;
	private SocketsInterface si;
	
	public TcpipWriteThread(String threadName, String logg, String IPP, int portt, Activity a)
	{
		si = (SocketsInterface) a;
		IP = IPP;
		port = portt;
		log = logg;
		runner = new Thread(this, threadName);
		Log.e("New thread started ", runner.getName());
		runner.start();
	}
	
	@Override
	public void run() {
		try
		{
			kkSocket = new Socket(IP, port);
			PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
			if(!log.equals(""))
			{
				String[] logLine = log.split("\n");
				String[] command;
				for(int q = 0; q < logLine.length; q++)
				{
					out.println(logLine[q]);
					out.flush();
					command = logLine[q].split(" ");
					if(command[0].equals("#file"))
					{
						out.println(EditorFragment.getStringFromFile(command[1]));
						out.flush();
						Thread.sleep(10000);//wait for receiver received file
					}
				}
			}
			
			out.close();
			si.setSocket(kkSocket);
			si.resetLog("reset");
		}
		catch(Exception ex)
		{
			Log.e("Error in TcpipWriteThread:", String.valueOf(ex));
		}
		
	}
	
	public Socket getSocket()
	{
		return kkSocket;
	}
	public boolean ResetLog()
	{
		return resetLog;
	}
	
	
}

