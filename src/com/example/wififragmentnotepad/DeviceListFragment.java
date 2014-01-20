package com.example.wififragmentnotepad;

import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class DeviceListFragment extends Fragment implements PeerListListener {

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    ProgressDialog progressDialog = null;
    View mContentView = null;
    ListView lw;
    private WifiP2pDevice device;
    private WiFiPeerListAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //lw = (ListView) findViewById(R.id.paired_devices);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.wifi_direct_fragment, container, false);    //(R.layout.wifi_direct_fragment, null);
        //lw = (ListView) getActivity()
        //this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.device_list_item, peers));
        
        View view = inflater.inflate(R.layout.wifi_direct_fragment, container, false);
		lw = (ListView) view.findViewById(R.id.device_list);
		adapter = new WiFiPeerListAdapter(getActivity(), R.layout.device_list_item, peers);
		lw.setAdapter(adapter);
		lw.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				WifiP2pDevice device = (WifiP2pDevice)  adapter.getItem(position);
				WifiP2pConfig config = new WifiP2pConfig();
				config.deviceAddress = device.deviceAddress;
				config.wps.setup = WpsInfo.PBC;
		        ((DeviceActionListener)getActivity()).connect(config);
				
			}
		
		});
		lw.setOnItemLongClickListener(new OnItemLongClickListener(){

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				((DeviceActionListener)getActivity()).disconnect();
				return true;
			}
			
		});
		return view;
    }


    public WifiP2pDevice getDevice() {
        return device;
    }

    private static String getDeviceStatus(int deviceStatus) {
    	//Log.d(MainActivity.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }


    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {

        private List<WifiP2pDevice> items;



        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                List<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;

        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {
            //View v = convertView;
            if (v == null) {
                //LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                //        Context.LAYOUT_INFLATER_SERVICE);
            	LayoutInflater vi = LayoutInflater.from(parent.getContext());
                v = vi.inflate(R.layout.device_list_item, parent, false);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                   bottom.setText(getDeviceStatus(device.status));
                }
            }
            return v;
        }
    }

    public void updateThisDevice(WifiP2pDevice device) {
        this.device = device;
        TextView view = (TextView) mContentView.findViewById(R.id.list_of_devices);
        view.setText(device.deviceName);
        view = (TextView) mContentView.findViewById(R.id.status_of_device);
        view.setText(getDeviceStatus(device.status));
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        if (progressDialog != null) {
        	if (progressDialog.isShowing()) {
        		progressDialog.dismiss();
        	}
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        adapter.notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(MainActivity.stringHelp, "No devices found");
            return;
        }

    }

    public void clearPeers() {
        peers.clear();
        adapter.notifyDataSetChanged();
    }

    public void onInitiateDiscovery() {
        if (progressDialog != null) {
        	if (progressDialog.isShowing()) {
        		progressDialog.dismiss();
        	}
        }
        Activity act = getActivity();
        if (act != null) {
        	progressDialog = ProgressDialog.show(act, "Press back to cancel", "finding peers", true,
                true, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        
                    }
                });
        }
    }

    public interface DeviceActionListener {

        void showDetails(WifiP2pDevice device);

        void cancelDisconnect();

        void connect(WifiP2pConfig config);

        void disconnect();
        
        void refreshFragment();
    }

}
