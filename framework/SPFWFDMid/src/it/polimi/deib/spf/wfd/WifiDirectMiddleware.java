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

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Looper;
import android.util.Log;

public class WifiDirectMiddleware implements WifiP2pManager.ConnectionInfoListener {

	private final static String TAG = "WFDMiddleware";
	private static final String SERVICE_TYPE = "_presence._tcp";
	protected static final String IDENTIFIER = "identifier";
	protected static final String PORT = "port";

	private final Context mContext;
	private final Map<String, String> mRecordMap;
	private final WfdMiddlewareListener mListener;
	private final WfdBroadcastReceiver mReceiver = new WfdBroadcastReceiver(this);

	private WifiP2pManager mManager;
	private Channel mChannel;
	private WifiP2pDnsSdServiceRequest mServiceRequest;
	private WifiP2pDnsSdServiceInfo mInfo;

	private boolean connected = false;
	private boolean isGroupCreated = false;
	private Set<String> mPeerAddresses = new HashSet<String>();
	private Map<String, Integer> mPorts = new HashMap<String, Integer>();
	private Map<String, String> mIdentifiers = new HashMap<String, String>();
	private Map<String,WifiP2pDevice> mDeviceInfos = new HashMap<String, WifiP2pDevice>();
	private String myIdentifier;
	private final String instanceNamePrefix;

	private GroupActor mGroupActor;
	private ServerSocket mServerSocket;
	private int mPort;

	public WifiDirectMiddleware(Context context, String identifier, String instanceNamePrefix, WfdMiddlewareListener listener) {
		this.mContext = context;
		this.mListener = listener;
		this.mRecordMap = new HashMap<String, String>();
		this.instanceNamePrefix = instanceNamePrefix;
		myIdentifier = identifier;
	}

	public void connect() {
		try {
			mServerSocket = new ServerSocket(0);
		} catch (IOException e) {
			mListener.onError();
			return;
		}
		mPort = mServerSocket.getLocalPort();
		mReceiver.register(mContext);
		mManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(mContext, Looper.getMainLooper(), null);
		setDiscovery();
		setAdvertisement();
		connected = true;
	}

	private void setDiscovery() {
		mManager.setDnsSdResponseListeners(mChannel, mServListener, mRecordListener);
		mServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
		mManager.addServiceRequest(mChannel, mServiceRequest, new ActionListener() {

			@Override
			public void onSuccess() {
				WfdLog.d(TAG, "addServiceRequest success");
			}

			@Override
			public void onFailure(int reason) {
				Log.e(TAG, "addServiceRequest failure: " + reason);
			}
		});

		mManager.discoverServices(mChannel, new ActionListener() {

			@Override
			public void onSuccess() {
				WfdLog.d(TAG, "discoverServices success");
			}

			@Override
			public void onFailure(int reason) {
				Log.e(TAG, "discoverServices failure: " + reason);
			}
		});
	}

	public boolean isConnected() {
		return connected;
	}

	public void setAdvertisement() {
		mRecordMap.put(PORT, Integer.toString(mPort));
		mRecordMap.put(IDENTIFIER, myIdentifier);
		mInfo = WifiP2pDnsSdServiceInfo.newInstance(instanceNamePrefix + myIdentifier, SERVICE_TYPE, mRecordMap);

		mManager.addLocalService(mChannel, mInfo, new ActionListener() {

			@Override
			public void onSuccess() {
				WfdLog.d(TAG, "addLocalService success");
			}

			@Override
			public void onFailure(int reason) {
				Log.e(TAG, "addLocalService failure: " + reason);
			}
		});
	}

	public void disconnect() {
		mReceiver.unregister();
		mManager.removeServiceRequest(mChannel, mServiceRequest, new ActionListener() {

			@Override
			public void onSuccess() {
				WfdLog.d(TAG, "removeServiceRequest success");
			}

			@Override
			public void onFailure(int reason) {
				Log.e(TAG, "removeServiceRequest failure: " + reason);
			}
		});
		mManager.removeLocalService(mChannel, mInfo, new ActionListener() {

			@Override
			public void onSuccess() {
				WfdLog.d(TAG, "removeLocalService succeeded");

			}

			@Override
			public void onFailure(int reason) {
				WfdLog.d(TAG, "removeLocalService failed");

			}
		});

		try {
			mServerSocket.close();
		} catch (IOException e) {
			WfdLog.d(TAG, "IOException when closing server socket", e);
		}
		mManager.cancelConnect(mChannel, new ActionListener() {

			@Override
			public void onSuccess() {
				WfdLog.d(TAG, "cancelConnect success");
			}

			@Override
			public void onFailure(int reason) {
				WfdLog.d(TAG, "cancelConnect failure: " + reason);
			}
		});
		mManager.removeGroup(mChannel, new ActionListener() {

			@Override
			public void onSuccess() {
				WfdLog.d(TAG, "removeGroup success");
			}

			@Override
			public void onFailure(int reason) {
				WfdLog.d(TAG, "reason failure: " + reason);
			}
		});
		connected = false;
		if (mGroupActor != null) {
			mGroupActor.disconnect();
			mGroupActor = null;
		}
		this.mIdentifiers.clear();
		this.mPeerAddresses.clear();
		this.mPorts.clear();

	}

	private DnsSdServiceResponseListener mServListener = new DnsSdServiceResponseListener() {

		@Override
		public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
			WfdLog.d(TAG, "ServiceResponseAvailable: " + instanceName + ", regType: " + registrationType + ", device: " + srcDevice);

			if (!instanceName.equalsIgnoreCase(instanceName)) {
				WfdLog.d(TAG, "Dropped external instance " + instanceName);
			}
		}
	};

	private DnsSdTxtRecordListener mRecordListener = new DnsSdTxtRecordListener() {

		@Override
		public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
			WfdLog.d(TAG, "DnsSdTxtRecord available:\n\n" + fullDomainName + "\n\n" + txtRecordMap + "\n\n" + srcDevice);
			if (!fullDomainName.startsWith(instanceNamePrefix)) {
				return;
			}
			mDeviceInfos.put(srcDevice.deviceAddress, srcDevice);
			mPeerAddresses.add(srcDevice.deviceAddress);
			
			String identifier = txtRecordMap.get(IDENTIFIER);
			String portString = txtRecordMap.get(PORT);
			int port;
			try {
				port = Integer.parseInt(portString);
			} catch (Throwable t) {
				mPeerAddresses.remove(srcDevice.deviceAddress);
				return;
			}
			if (!mIdentifiers.containsKey(srcDevice.deviceAddress)) {
				WfdLog.d(TAG, "peer found: " + identifier);
				mIdentifiers.put(srcDevice.deviceAddress, identifier);
			}
			mPorts.put(srcDevice.deviceAddress, port);
			if (!isGroupCreated) {
				createGroup();
			}

		}

	};
	private void createGroup() {
		WfdLog.d(TAG, "createGroup()");
		if (isGroupCreated||!connected) {
			WfdLog.d(TAG, "group already created or middleware not started");
			return;
		}
		WfdLog.d(TAG, "attempt to create group");
		WifiP2pConfig config = new WifiP2pConfig();
		String deviceAddress = selectDeviceAddess();
		if (deviceAddress == null) {
			WfdLog.d(TAG, "no device address eligible for connection");
			return;
		}
		WfdLog.d(TAG, "connect target device found, device address: " + deviceAddress);
		config.deviceAddress = deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		isGroupCreated = true;
		mManager.connect(mChannel, config, new ActionListener() {

			@Override
			public void onSuccess() {
				WfdLog.d(TAG, "connect() succeded");

			}

			@Override
			public void onFailure(int reason) {
				isGroupCreated = false;
				WfdLog.d(TAG, "connect() failed");
			}
		});
	}

	private String selectDeviceAddess() {
		String eligibleAddress = null;
		String eligibleIdentifier = null;

		//elect a device to connect:
		for (String deviceAddress : mPeerAddresses) {
			if (mPorts.containsKey(deviceAddress) && mIdentifiers.containsKey(deviceAddress)) {
				if(mDeviceInfos.get(deviceAddress).isGroupOwner()){
					return deviceAddress;
				}
				String _eligibleId = mIdentifiers.get(deviceAddress);
				if (_eligibleId.compareTo(myIdentifier) < 0) {
					if (eligibleIdentifier != null && _eligibleId.compareTo(eligibleIdentifier) < 0) {
						eligibleAddress = deviceAddress;
						eligibleIdentifier = _eligibleId;
					} else if (eligibleIdentifier == null) {
						eligibleAddress = deviceAddress;
						eligibleIdentifier = _eligibleId;
					}
				}
			}
		}
		return eligibleAddress;
	}

	@Override
	public void onConnectionInfoAvailable(final WifiP2pInfo info) {
		if (mGroupActor != null) {
			return;
		}
		WfdLog.d(TAG, "connection info available");
		if (!info.groupFormed) {
			createGroup();
			return;
		}
		WfdLog.d(TAG, "group formed");
		if (info.isGroupOwner) {
			instantiateGroupOwner();
		} else {
			mManager.requestGroupInfo(mChannel, new GroupInfoListener() {
				@Override
				public void onGroupInfoAvailable(WifiP2pGroup arg0) {
					if (arg0 == null) {
						// happens when the go goes away and the
						// framework does not have time to update the
						// connection loss
						return;
					}
					WifiP2pDevice groupOwnerDevice = arg0.getOwner();
					Integer destPort = mPorts.get(groupOwnerDevice.deviceAddress);
					if (destPort == null) {
						Log.e(TAG, "null destPort for group owner");
						mManager.removeGroup(mChannel, null);
					}
					instantiateGroupClient(info.groupOwnerAddress, destPort);
				}
			});
		}
	}

	private void instantiateGroupClient(InetAddress groupOwnerAddress, int destPort) {
		WfdLog.d(TAG, "Instantiating group client's logic");
		GroupClientActor gc = new GroupClientActor(groupOwnerAddress, destPort, actorListener, myIdentifier);
		mGroupActor = gc;
		mGroupActor.connect();

	}

	private void instantiateGroupOwner() {
		WfdLog.d(TAG, "Instantiating group owner's logic");
		GroupOwnerActor go = new GroupOwnerActor(mServerSocket, myIdentifier, actorListener);
		mGroupActor = go;
		mGroupActor.connect();
	}

	public void onNetworkConnected() {
		WfdLog.d(TAG, "onNetworkConnected(): requesting connection info");
		mManager.requestConnectionInfo(mChannel, this);

	}

	public void onNetworkDisconnected() {
		WfdLog.d(TAG, "onNetworkDisconnected");
		if (mGroupActor != null) {
			mGroupActor.disconnect();
		}
		isGroupCreated = false;
		mGroupActor = null;
		mManager.removeGroup(mChannel, null);
		mManager.requestConnectionInfo(mChannel, this);
	}

	public void updatePeerList(Collection<WifiP2pDevice> list) {
		Set<String> oldList = new HashSet<String>(mPeerAddresses);
		Set<String> newList = new HashSet<String>();
		for (WifiP2pDevice device : list) {
			//update device info
			if(mDeviceInfos.containsKey(device.deviceAddress)){
				mDeviceInfos.put(device.deviceAddress, device);
			}
			String deviceAddress = device.deviceAddress;
			newList.add(deviceAddress);
		}
		//remove lost device
		Iterator<String> iterator = oldList.iterator();
		while (iterator.hasNext()) {
			String deviceAddress = iterator.next();
			if (!newList.contains(deviceAddress) && mIdentifiers.containsKey(deviceAddress)) {
				mPeerAddresses.remove(deviceAddress);
				mDeviceInfos.remove(deviceAddress);
				String lostId = mIdentifiers.remove(deviceAddress);
				mPorts.remove(deviceAddress);
				iterator.remove();
				WfdLog.d(TAG, "peer removed lost id: " + lostId);
			}
		}
		if(!mPeerAddresses.isEmpty()&&!isGroupCreated){
			createGroup();
		}
	}

	private GroupActorListener actorListener = new GroupActorListener() {

		@Override
		public void onMessageReceived(WfdMessage msg) {
			mListener.onMessageReceived(msg);
		}

		@Override
		public void onError() {
			if (mGroupActor != null) {
				mGroupActor.disconnect();
			}
			isGroupCreated = false;
			mGroupActor = null;
			mManager.removeGroup(mChannel, null);
			mManager.requestConnectionInfo(mChannel, WifiDirectMiddleware.this);
		}

		@Override
		public WfdMessage onRequestMessageReceived(WfdMessage msg) {
			return mListener.onRequestMessageReceived(msg);
		}

		@Override
		public void onInstanceFound(String identifier) {
			mListener.onInstanceFound(identifier);

		}

		@Override
		public void onInstanceLost(String identifier) {
			mListener.onInstanceLost(identifier);
		}
	};

	public void onPeerListChanged() {
		mManager.requestPeers(mChannel, new PeerListListener() {

			@Override
			public void onPeersAvailable(WifiP2pDeviceList peers) {
				Collection<WifiP2pDevice> peersCollection = peers.getDeviceList();
				updatePeerList(peersCollection);
			}
		});
	}

	public void sendMessage(WfdMessage msg, String targetId) throws IOException {
		msg.setSenderId(myIdentifier);
		msg.setReceiverId(targetId);
		GroupActor tmp = mGroupActor;
		if(tmp== null){
			throw new IOException("Group not yet instantiated");
		}
		tmp.sendMessage(msg);

	}

	public void sendMessageBroadcast(WfdMessage msg) throws IOException {
		msg.setSenderId(myIdentifier);
		msg.setReceiverId(WfdMessage.BROADCAST_RECEIVER_ID);
		GroupActor tmp = mGroupActor;
		if(tmp== null){
			throw new IOException("Group not yet instantiated");
		}
		tmp.sendMessage(msg);
	}

	public WfdMessage sendRequestMessage(WfdMessage msg, String targetId) {
		msg.setSenderId(myIdentifier);
		msg.setReceiverId(targetId);
		msg.setType(WfdMessage.TYPE_REQUEST);
		try {
			GroupActor tmp = mGroupActor;
			if(tmp == null){
				return null;
			}
			return tmp.sendRequestMessage(msg);
		} catch (IOException e) {
			WfdLog.d(TAG, "sendRequestMessage( ) error", e);
			return null;
		}
	}

}