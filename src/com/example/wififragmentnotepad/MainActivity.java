package com.example.wififragmentnotepad;

import com.example.wififragmentnotepad.DeviceListFragment.DeviceActionListener;
import com.example.wififragmentnotepad.ProgramFragment.onEditEventListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity implements onEditEventListener, ConnectionInterface, SocketsInterface, ConnectionInfoListener, DeviceActionListener, ChannelListener {
	private Spinner spinner1;
    private File mPath;   
    private FileDialog fileDialog;
    private Activity activity;
	private String fileName;
	private String log = "";
	private boolean retryChannel;
	private DeviceListFragment deviceListFragment;
	private ThreadInterface threadInterface;
	private String IP;
	private WifiP2pManager manager;
	private Channel channel;
	private BroadcastReceiver receiver;
	private PeerListListener peerListListener;
	
	private final int PORT = 8988;
	private final IntentFilter intentFilter = new IntentFilter();
	private final static int LOAD_PROGRAM_FRAGMENT_NUMBER = 0;
	private final static int ON_OPEN_FILE_NUMBER = 1;
	private final static int ON_CREATE_FILE_NUMBER = 2;
	private final static int LOAD_CONNECTION_FRAGMENT_NUMBER = 3;
	private final static int LOAD_SETTINGS_FRAGMENT_NUMBER = 4;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        retryChannel = false;
        LoadLog();
        CreateSpinner1();
        FragmentManager fragmentManager = getFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	fragmentTransaction.add(R.id.fragment_layout_1, new ProgramFragment());
    	fragmentTransaction.commit();
        
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void LoadLog() {
    	log = "#file kuba.txt";
    }
    
    @Override
    public void onPause() {
    	if(receiver != null) {
    		unregisterReceiver(receiver);
    	}
    	super.onPause();
    }
    
    @Override
    public void onResume() {
    	if(receiver != null) {
    		registerReceiver(receiver, intentFilter);
    	}
    	super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    private void CreateSpinner1() {
    	spinner1 = (Spinner) findViewById(R.id.spinner1);
        spinner1.setOnItemSelectedListener(new OnItemSelectedListener()
        {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				UpdateFragment(arg2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
        	
        });
    }
    
    private void UpdateFragment(int j) {
		if(j == LOAD_PROGRAM_FRAGMENT_NUMBER) {
			LoadProgramFragment();
		}
		else if(j == ON_OPEN_FILE_NUMBER) {
			OnOpenFile();
		}
		else if(j == ON_CREATE_FILE_NUMBER) {
			OnCreateFile();
		}
		else if(j == LOAD_CONNECTION_FRAGMENT_NUMBER) {
			LoadConnectionFragment();
		}
		else if(j == LOAD_SETTINGS_FRAGMENT_NUMBER) {
			LoadSettingsFragment();
		}
	}
    
    private void LoadProgramFragment() {
    	FragmentManager fragmentManager = getFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	fragmentTransaction.replace(R.id.fragment_layout_1, new ProgramFragment());
    	fragmentTransaction.commit();
    }
    
    private void OnOpenFile() {
    	mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
    	fileDialog = new FileDialog(this, mPath);
        fileDialog.setFileEndsWith(".txt");
        fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
        		fileName = file.getAbsolutePath();
        		DatabaseHelper databaseHelper = new DatabaseHelper(activity);
        		databaseHelper.saveLastOpenedFile(fileName, fileName);
				databaseHelper.saveSharedFile(fileName, fileName);
        		EditFile();
            }
        });
        fileDialog.createFileDialog();
    }
    
    private void OnCreateFile() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Enter file name");
		final EditText input = new EditText(activity);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which){
				String t = input.getText().toString();

				File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()  + "/" + t + ".txt");
				if(!file.exists()) {
				}
				try {
					file.createNewFile();
					fileName = t + ".txt";
					DatabaseHelper databaseHelper = new DatabaseHelper(activity);
					databaseHelper.saveLastOpenedFile(fileName, fileName);
					databaseHelper.saveSharedFile(fileName, fileName);
					EditFile();
				}
				catch(Exception ex) { 
				}
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which){
				dialog.cancel();
			}
		});
		Dialog dialog = builder.create();
		dialog.show();
    }
    
    private void LoadConnectionFragment() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Select connection type");
		final StringAdapter adapter = new StringAdapter();
		adapter.setConnectionList();
		builder.setAdapter(adapter, new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				setConnectionType(String.valueOf(adapter.getItem(arg1)));
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		Dialog dialog = builder.create();
		dialog.show();
    }
    private void LoadSettingsFragment() {
    	FragmentManager fragmentManager = getFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	fragmentTransaction.replace(R.id.fragment_layout_1, new SettingsFragment());
    	fragmentTransaction.commit();
    }
    
    public void EditFile() {
    	FragmentManager fragmentManager = getFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	EditorFragment editorFragment = new EditorFragment();
		editorFragment.GoToEditorFragment(fileName);
		if(threadInterface != null) {
			threadInterface.setEditorFragment((EditorFragmentInterface)editorFragment);
			editorFragment.SetConnection(threadInterface);
			threadInterface.setEditorFragment((EditorFragmentInterface)editorFragment);
		}
		else {
			Log.e("thrad interface is ", "null");
		}
    	fragmentTransaction.replace(R.id.fragment_layout_1, editorFragment);
    	fragmentTransaction.commit();
    }

	@Override
	public void listOpenFile(String s) {
		fileName = s;
		EditFile();
	}
	
	@Override
	public void setConnectionType(String s) {
		if(s == "TCP/IP") {
			TcpIpConnectionType();
		}
		else {
			FragmentManager fragmentManager = getFragmentManager();
	    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
	    	deviceListFragment = new DeviceListFragment();
	    	fragmentTransaction.replace(R.id.fragment_layout_1, deviceListFragment);
	    	fragmentTransaction.commit();
			WifiDirectConnectionType();
		}
	}
    
	private void TcpIpConnectionType() {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Choose mode");
		final StringAdapter adapter = new StringAdapter();
		adapter.setMasterOrSlaveList();
		builder.setAdapter(adapter, new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				SetMasterOrSlaveTcpipConnection(String.valueOf(adapter.getItem(arg1)));
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		Dialog dialog = builder.create();
		dialog.show();
	}
	
	private void SetMasterOrSlaveTcpipConnection(String str) {
		WifiDirectThread th = new WifiDirectThread();
		threadInterface = (ThreadInterface) th;
		if( ("Master").equals(str) ) {
			th.Initialize("169.254.136.231", PORT, true);
		}
		else {
			th.Initialize("169.254.136.231", PORT, false);
		}
	}
	
	private void WifiDirectConnectionType() {
		manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Finding Peers",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(MainActivity.this, "Couldnt find peers",
                        Toast.LENGTH_SHORT).show();
            }
        });
		
		manager.requestPeers(channel, peerListListener);
    	receiver = new WifiDirectBroadcastReceiver(manager, channel, this, deviceListFragment);
        registerReceiver(receiver, intentFilter);
	}

	@Override
	public void resetLog(String command) {
			log = "";
	}

	@Override
	public void addFileToLog(String file) {
		if(("").equals(log)) {
			log = "#file " + file;
		}
		else {
			log +="\n#file " + file;
		}
	}

	@Override
	public void nowEditingFile(String file) {
		if(("").equals(log)) {
			log = "#now " + file;
		}
		else {
			log += "\n#now " + file;
		}
	}
	
	@Override
	public void onStop() {
		if(threadInterface != null)
			threadInterface.Stop();
		
		try {
			if(receiver != null) {
				unregisterReceiver(receiver);
			}
		}
		catch(RuntimeException ex) {
		}
		super.onStop();
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		String infoname = info.groupOwnerAddress.toString();
		IP = infoname.substring(1);
		WifiDirectThread wifiDirectThread = new WifiDirectThread();
    	threadInterface = (ThreadInterface) wifiDirectThread;
    	threadInterface.Initialize(IP, PORT, info.isGroupOwner);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void showDetails(WifiP2pDevice device) {
	}

	@Override
	public void cancelDisconnect() {
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.device_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            }
            else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {
                manager.cancelConnect(channel, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(activity, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(activity,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(activity, "Connect failed. Retry.", Toast.LENGTH_SHORT).show();
            }
        });
    }

	@Override
	public void disconnect() {
		manager.removeGroup(channel, new ActionListener() {
			@Override
			public void onFailure(int arg0) {
				Toast.makeText(activity, "Disconection failed.", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onSuccess() {
				Toast.makeText(activity, "Disconected.", Toast.LENGTH_SHORT).show();
				
			}
		});
	}

    @Override
    public void onChannelDisconnected() {
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        }
        else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

	@Override
	public void refreshFragment() {
		FragmentManager fragmentManager = getFragmentManager();
    	FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    	fragmentTransaction.replace(R.id.fragment_layout_1, deviceListFragment);
    	fragmentTransaction.commit();
	}

	@Override
	public void setSocket(Socket s) {
	}
}

class TcpipWriteThread implements Runnable {
	Thread runner;
	private String log = null;
	private String IP;
	private int port;
	private Socket kkSocket;
	private SocketsInterface si;
	private boolean GO = true;
	public Object ToLock = new Object();
	private String data_to_send = "";
		
	public TcpipWriteThread(String threadName, String logg, String IPP, int portt, Activity a) {
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
		try {
			kkSocket = new Socket(IP, port);
			PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
			if(!log.equals("")) {
				String[] logLine = log.split("\n");
				String[] command;
				for(int q = 0; q < logLine.length; q++) {
					out.println(logLine[q]);
					out.flush();
					command = logLine[q].split(" ");
					if(command[0].equals("#file")) {
						out.println(EditorFragment.getStringFromFile(command[1]));
						out.flush();
						Thread.sleep(10000);
					}
				}
			}
			si.resetLog("reset");
			while(GO) {
				synchronized(ToLock) {
					if( !("").equals(data_to_send) ) {
						out.println(data_to_send);
						out.flush();
						data_to_send = "";
					}
				}
				Thread.sleep(300);
			}
			out.close();
			kkSocket.close();
		}
		catch(Exception ex) {
			Log.e("Error in TcpipWriteThread:", String.valueOf(ex));
		}
		finally {
		}
	}
	
	public void Stop() {
		GO = false;
	}
	
	public void TrySendData(String data) {
		synchronized(ToLock) {
			if( ("").equals(data_to_send) ) {
				data_to_send = data;
			}
			else {
				data_to_send += "\n" + data;
			}
		}
	}
}

class WifiDirectThread implements Runnable, ThreadInterface {
	Thread runner;
	private Socket socket;
	private int port = 8988;
	private String IP;
	private boolean GO = true;
	private boolean isGroupOwner;
	public Object ToLock = new Object();
	private String DataToSend = "";
	private EditorFragmentInterface editorFragmentInterface = null;
	
	public WifiDirectThread() {
	}
	
	@Override
	public void run() {
		if(!isGroupOwner) {
			try {
				socket = new Socket();
				socket.bind(null);

				Thread.sleep(3000);
				socket.connect(new InetSocketAddress(IP, port));
				Log.e("connected", "as slave");
				PrintWriter writer = new PrintWriter(socket.getOutputStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while(GO) {
					String str = reader.readLine();
					if( ("#exit").equals(str) ) {
						GO = false;
						
					}
					else if( ("#noAction").equals(str) ) {
					}
					else if (str != null && !("").equals(str)) {
						if(editorFragmentInterface != null) {
							Log.i("str", str);
							EditorFragment.pressed_key = false;
							editorFragmentInterface.SendData(str);
						}
						else {
							Log.e("tag", str + " " + editorFragmentInterface.toString());
						}
					}
					Thread.sleep(300);
					
					synchronized(ToLock) {
						if(!("").equals(DataToSend)) {
							if(GO) {
								writer.println(DataToSend);
								DataToSend = "";
							}
						}
						else {
							if(GO) {
								writer.println("#noAction");
							}
						}
					}
					writer.flush();
					Thread.sleep(300);
				}
				reader.close();
				writer.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				ServerSocket ss;
				ss = new ServerSocket(port);
				socket = ss.accept();
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter writer = new PrintWriter(socket.getOutputStream());
				while(GO) {
					synchronized(ToLock) {
						if(!("").equals(DataToSend)) {
							writer.println(DataToSend);
							DataToSend = "";
						}
						else {
							writer.println("#noAction");
						}
					}
					writer.flush();
					Thread.sleep(300);
					String str = reader.readLine();
					if( ("#exit").equals(str) ) {
						GO = false;
					}
					else if( ("#noAction").equals(str) ) {
					}
					else if (str != null && !("").equals(str)) {
						if(editorFragmentInterface != null) {
							EditorFragment.pressed_key = false;
							editorFragmentInterface.SendData(str);
						}
						else {
							Log.e("tag", str + " " + editorFragmentInterface.toString());
						}
					}
					Thread.sleep(300);
				}
				reader.close();
				writer.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void Stop() {
		GO = false;
		DataToSend += "#exit";
	}

	@Override
	public void Initialize(String ip, int port, boolean isGroupOwner) {
		this.port = port;
		this.IP = ip;
		this.isGroupOwner = isGroupOwner;
		runner = new Thread(this, "WifiDirectThread");
		runner.start();
	}

	@Override
	public void TrySendData(String data) {
		synchronized(ToLock) {
			if(("").equals(DataToSend)) {
				DataToSend = data;
			}
			else {
				DataToSend += "\r\n" + data;
			}
		}
	}

	@Override
	public void Restart() {
		run();
	}

	@Override
	public void setEditorFragment(EditorFragmentInterface editorFragmentInterface) {
		this.editorFragmentInterface = editorFragmentInterface;
	}
}
