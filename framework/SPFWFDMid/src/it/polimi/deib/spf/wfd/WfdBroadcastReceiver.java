/* 
 * Copyright 2014 Jacopo Aliprandi, Dario Archetti
 * 
 * This file is part of SPF.
 * 
 * SPF is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * SPF is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with SPF.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package it.polimi.deib.spf.wfd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;

public class WfdBroadcastReceiver extends BroadcastReceiver {
	
	private WifiDirectMiddleware mMid;
	private Context mContext;

	public WfdBroadcastReceiver(WifiDirectMiddleware wifiDirectMiddleware) {
		mMid =  wifiDirectMiddleware;
	}

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		String action =  arg1.getAction();
		if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)){
			mMid.onPeerListChanged();
		}else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
			//call on connection info received
			NetworkInfo netInfo = arg1.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
			if (netInfo.isConnected()){
				mMid.onNetworkConnected();
			}else{
				mMid.onNetworkDisconnected();
			}
		}
	}

	public void register(Context mContext) {
		this.mContext = mContext;
		IntentFilter intentFilter = new IntentFilter();
		
	    //  Indicates a change in the Wi-Fi P2P status.
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

	    // Indicates a change in the list of available peers.
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

	    // Indicates the state of Wi-Fi P2P connectivity has changed.
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

	    // Indicates this device's details have changed.
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);	
	    
	    mContext.registerReceiver(this, intentFilter);
	}
	
	public void unregister(){
		mContext.unregisterReceiver(this);
	}

}
