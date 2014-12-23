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
package it.polimi.spf.alljoyn;

import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.Utils;
import it.polimi.spf.framework.proximity.InboundProximityInterface;

import org.alljoyn.bus.BusAttachment;
import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusListener;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.Mutable;
import org.alljoyn.bus.ProxyBusObject;
import org.alljoyn.bus.SessionListener;
import org.alljoyn.bus.SessionOpts;
import org.alljoyn.bus.SessionPortListener;
import org.alljoyn.bus.SignalEmitter;
import org.alljoyn.bus.Status;
import org.alljoyn.bus.SignalEmitter.GlobalBroadcast;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Handler that performs Alljoyn-related calls.
 */
/*package*/ class BusHandler extends Handler {

	private static final String TAG = "BusHandler";
	private static boolean LOG = true;

	public static final String PROXIMITY_WKN_PREFIX = "it.polimi.spf.instance";

	private static final String PROXIMITY_INTERFACE_PATH = "/proximityInterface";
	private static final short CONTACT_PORT = 42;

	private final static String SERVER = "Server";
	private final static String CLIENT = "Client";

	private static final String SPF_SIGNALS_INTERFACE_PATH = "/spfSignals";
	
	private AlljoynSignalsHandler mSignalEmitterInterface;
	
	private boolean isConnected;

	private static class Actions {
		public static final int CONNECT = 0;
		public static final int ADVERTISE = 1;
		public static final int START_DISCOVERY = 2;
		public static final int FOUND_INSTANCE = 3;
		public static final int LOST_INSTANCE = 4;
		public static final int DISCONNECT = 5;
		public static final int START_SEARCH = 6;
		public static final int SEARCH_RESULT = 7;
		public static final int SPF_ADVERTISE = 8;
		public static final int STOP_SPF_AVERTISING = 9;
	}

	private Context mAppContext;
	private InboundProximityInterface mProximityInterface;
	private BusAttachment mClientAttachment, mServerAttachment;

	// Objects registered on bus
	private AlljoynProximityInterfaceImpl mAlljoynProximityInterface;
	private BusObject mSPFSignalsImpl;
	
	private String mWellKnownName;
	private String mInstanceID;

	/**
	 * Listener for incoming session join requests
	 */
	private final SessionPortListener mSessionPortListener = new SessionPortListener() {

		private final static String TAG = BusHandler.TAG + ".SessionPortListener";

		@Override
		public boolean acceptSessionJoiner(short sessionPort, String joiner, SessionOpts opts) {
			boolean accepted = (sessionPort == CONTACT_PORT);
			log(TAG, SERVER, "Joiner " + joiner + " " + (accepted ? "accepted" : "refused"));
			return accepted;
		}

		@Override
		public void sessionJoined(short sessionPort, int id, String joiner) {
			log(TAG, SERVER, "Session " + id + " joined by " + joiner + " on port " + sessionPort);
		}
	};

	/**
	 * Listener for outbound session join requests
	 */
	private final SessionListener mSessionListener = new SessionListener() {

		private final static String TAG = BusHandler.TAG + ".SessionListener";

		@Override
		public void sessionLost(int sessionId, int reason) {
			log(TAG, CLIENT, "Session " + sessionId + " lost (" + reason + ")");
		}

		@Override
		public void sessionMemberAdded(int sessionId, String uniqueName) {
			log(TAG, CLIENT, uniqueName + " added to session " + sessionId);
		}

		@Override
		public void sessionMemberRemoved(int sessionId, String uniqueName) {
			log(TAG, CLIENT, uniqueName + " removed from session " + sessionId);
		}
	};

	// Bus listener that is notified when peers are found
	private final BusListener mBusListener = new BusListener() {

		private final static String TAG = BusHandler.TAG + ".BusListener";

		// These methods are executed by a thread from Alljoyn's thread pool.
		// Executing a method of this handler on such thread will cause silent
		// failure, thus we need to dispatch a message to our own handler.
		public void foundAdvertisedName(String wellKnownName, short transport, String namePrefix) {
			log(TAG, CLIENT, "Found advertised name " + wellKnownName + " with prefix " + namePrefix);
			Message msg = obtainMessage(Actions.FOUND_INSTANCE, wellKnownName);
			sendMessage(msg);
		}

		public void lostAdvertisedName(String wellKnownName, short transport, String namePrefix) {
			log(TAG, CLIENT, "Lost advertised name " + wellKnownName + " with prefix " + namePrefix);
			Message msg = obtainMessage(Actions.LOST_INSTANCE, wellKnownName);
			sendMessage(msg);
		}
	};

	/**
	 * Initialized a new instance of BusHandler.
	 */
	public BusHandler(Context context, Looper looper, InboundProximityInterface proximityInterface) {
		super(looper);

		if (context == null) {
			throw new NullPointerException("Context must not be null");
		}

		this.mAppContext = context.getApplicationContext();
		this.mProximityInterface = proximityInterface;
		
		this.mClientAttachment = new BusAttachment(mAppContext.getPackageName(), BusAttachment.RemoteMessage.Receive);
		this.mServerAttachment = new BusAttachment(mAppContext.getPackageName(), BusAttachment.RemoteMessage.Receive);
		
		this.mAlljoynProximityInterface = new AlljoynProximityInterfaceImpl(proximityInterface);
		this.mSPFSignalsImpl = new AlljoynSignalsHandlerImpl(proximityInterface);
	}

	@Override
	public void handleMessage(Message msg) {
		try{
		switch (msg.what) {
		case Actions.CONNECT:
			doConnect();
			break;
		case Actions.ADVERTISE:
			doAdvertise((String) msg.obj);
			break;
		case Actions.START_DISCOVERY:
			doStartDiscovery();
			break;
		case Actions.FOUND_INSTANCE:
			doHandleFoundInstance((String) msg.obj);
			break;
		case Actions.LOST_INSTANCE:
			doHandleLostInstance((String) msg.obj);
			break;
		case Actions.DISCONNECT:
			doDisconnect();
			break;
		case Actions.START_SEARCH:
			sendSearchSignal(msg.getData().getString("sender"), msg.getData().getString("queryId"), msg.getData().getString("query"));
			break;
		case Actions.SEARCH_RESULT:
			sendSearchResult(msg.getData().getString("queryId"), msg.getData().getString("uniqueId"),msg.getData().getString("baseInfo"));
			break;
		case Actions.SPF_ADVERTISE:
			doSPFAdvertise(msg.getData().getString("profile"), msg.getData().getLong("period"));
			break;
		case Actions.STOP_SPF_AVERTISING:
			removeMessages(Actions.SPF_ADVERTISE);
			break;
		}
		}catch(Throwable t){
			Utils.logCall(TAG, "handle( Message )", msg);
		}
	}
	
	private void doSPFAdvertise(String profile, long period) {
		try {
			log(TAG,"BusHandler","sending SPFAdvertising alljoyn signal");
			mSignalEmitterInterface.advertisingSignal(profile,SPF.get().getUniqueIdentifier());
		} catch (BusException e) {
			// TODO handle error
			e.printStackTrace();
		}
		sendSPFAdvertisingSignal(profile, period);
	}

	/**
	 * @param profile
	 * @param period
	 */
	private void sendSPFAdvertisingSignal(String profile, long period) {
		log(TAG, "BusHandler", "sending SPFAdvertising signal handler message");
		// Queue message for next signal
		Message msg = obtainMessage(Actions.SPF_ADVERTISE);
		msg.getData().putString("profile", profile);
		msg.getData().putLong("period", period);
		sendMessageDelayed(msg, period);
	}

	// Helper methods
	private void doConnect() {
		// Initialize daemon
		org.alljoyn.bus.alljoyn.DaemonInit.PrepareDaemon(mAppContext);

		Status status;

		mClientAttachment.registerBusListener(mBusListener);

		// Register our proximity interface
		status = mServerAttachment.registerBusObject(mAlljoynProximityInterface, PROXIMITY_INTERFACE_PATH);
		notifyStatus("doConnect", "registerBusObject [server]", status);

		// Register search signals
		status = mServerAttachment.registerBusObject(mSPFSignalsImpl, SPF_SIGNALS_INTERFACE_PATH);
		notifyStatus("doConnect", "Registering signal interface", status);

		// Connect to bus
		status = mServerAttachment.connect();
		notifyStatus("doConnect", "connect [server]", status);

		status = mClientAttachment.connect();
		notifyStatus("doConnect", "connect [client]", status);

		// Register for session-less search signals
		SignalEmitter emitter = new SignalEmitter(mSPFSignalsImpl, 0, GlobalBroadcast.On);
		emitter.setSessionlessFlag(true);
		status = mServerAttachment.addMatch("sessionless='t'");
		notifyStatus("doConnect", "addMatch [server]", status);
		mSignalEmitterInterface = emitter.getInterface(AlljoynSignalsHandler.class);
		status = mServerAttachment.registerSignalHandlers(mSPFSignalsImpl);
		notifyStatus("doConnect", "registersignalHandler", status);

		// Bind session on contact port to allow remote instances to communicate
		// with us
		Mutable.ShortValue contactPort = new Mutable.ShortValue(CONTACT_PORT);
		SessionOpts sessionOpts = new SessionOpts(SessionOpts.TRAFFIC_MESSAGES, true, SessionOpts.PROXIMITY_ANY, SessionOpts.TRANSPORT_ANY);
		status = mServerAttachment.bindSessionPort(contactPort, sessionOpts, mSessionPortListener);
		notifyStatus("doBindSession", "bindSession [server]", status);
	}

	private void doAdvertise(String wkn) {

		mInstanceID = "." + wkn;
		mWellKnownName = PROXIMITY_WKN_PREFIX + mInstanceID;

		Status status = mServerAttachment.requestName(mWellKnownName, BusAttachment.ALLJOYN_REQUESTNAME_FLAG_DO_NOT_QUEUE);
		notifyStatus("doAdvertise", "requestName [server]", status);

		status = mServerAttachment.advertiseName(mWellKnownName, SessionOpts.TRANSPORT_ANY);
		notifyStatus("doAdvertise", "requestName [server]", status);
	}

	private void doStartDiscovery() {
		synchronized (mClientAttachment) {
			Status status = mClientAttachment.findAdvertisedName(PROXIMITY_WKN_PREFIX);
			notifyStatus("doStartDiscovery", "findAdvertisedName [client]", status);
		}
	}

	/*package*/AlljoynProximityInterface joinSession(String wellKnownName) {
		synchronized (mClientAttachment) {
			Mutable.IntegerValue sessionId = new Mutable.IntegerValue();
			SessionOpts sessionOpts = new SessionOpts(SessionOpts.TRAFFIC_MESSAGES, true, SessionOpts.PROXIMITY_ANY, SessionOpts.TRANSPORT_ANY);

			Status status = mClientAttachment.joinSession(wellKnownName, CONTACT_PORT, sessionId, sessionOpts, mSessionListener);
			notifyStatus("doJoinSession", "joinSession", status);

			ProxyBusObject obj = mClientAttachment.getProxyBusObject(wellKnownName, PROXIMITY_INTERFACE_PATH, sessionId.value, new Class[] { AlljoynProximityInterface.class });
			AlljoynProximityInterface rif = obj.getInterface(AlljoynProximityInterface.class);
			log("client", "Remote interface obtained correctly");

			return rif;
		}
	}

	private void doHandleFoundInstance(String wellKnownName) {
		if (wellKnownName.contains(mInstanceID)) {
			// avoid the connection to itself: this would cause joinSession
			// to fail silently
			Log.d(TAG, "[client] dropped connection to self");
			return;
		}

		final AlljoynRemoteInstance instance = new AlljoynRemoteInstance(wellKnownName, BusHandler.this);
		mProximityInterface.onRemoteInstanceFound(instance);
	}

	private void doHandleLostInstance(String wellKnownName) {
		String identifier = AlljoynRemoteInstance.removeWellKnownNamePrefix(wellKnownName);
		mProximityInterface.onRemoteInstanceLost(identifier);
	}

	private void doDisconnect() {
		Status status;
		// TODO: invalidate active SPFProximityInterface by making them call
		// mbusattachment.leave
		status = mServerAttachment.unbindSessionPort(CONTACT_PORT);
		notifyStatus("disconnect", "unbind [server]", status);

		status = mServerAttachment.cancelAdvertiseName(mWellKnownName, SessionOpts.TRANSPORT_ANY);
		notifyStatus("disconnect", "cancelAdvertise [server]", status);

		status = mServerAttachment.releaseName(mWellKnownName);
		notifyStatus("disconnect", "releaseWKN [server]", status);
		
		mServerAttachment.unregisterSignalHandlers(mSPFSignalsImpl);

		mServerAttachment.disconnect();
		
		mServerAttachment.unregisterBusObject(mAlljoynProximityInterface);
		mServerAttachment.unregisterBusObject(mSPFSignalsImpl);

		synchronized (mClientAttachment) {
			status = mClientAttachment.cancelFindAdvertisedName(PROXIMITY_WKN_PREFIX);
			notifyStatus("disconnect", "cancelFind [client]", status);

			mClientAttachment.disconnect();
		}
		
		getLooper().quit();
		
		
	}

	private void notifyStatus(String method, String phase, Status status) {
		if (LOG && status == Status.OK) {
			Log.i(TAG, "Call @ " + phase + " within " + method + "completed successfully");
		} else {
			throw new RuntimeException("Call @ " + phase + " within " + method + "completed with error code " + status);
		}
	}

	private void log(String tag, String component, String message) {
		if (LOG) {
			Log.v(tag, "[" + component + "]" + message);
		}
	}

	private void log(String component, String message) {
		log(TAG, component, message);
	}

	private void sendSearchSignal(String sender, String searchId, String query) {
		try {
			mSignalEmitterInterface.searchSignal(sender, searchId, query);
		} catch (BusException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void sendSearchResult(String queryId, String uniqueIdentifier,String baseInfo) {
		try {
			mSignalEmitterInterface.searchResult(queryId, uniqueIdentifier,baseInfo);
		} catch (BusException e) {
			e.printStackTrace();
		}
	}

	// Public methods

	/*
	 * Note on synchronization: All the actions performed by bus handler are
	 * serialized in the message queue hold by its looper. The only two methods
	 * that requires synchronization are connect and disconnect because we want
	 * to achieve atomicity wrt to the isConnected variable assignment and the
	 * delivery of the message in the looper's queue.
	 */
	/**
	 * Opens a connection with alljoyn and initializes all the needed resources.
	 */
	public synchronized void connect() {
		if (!isConnected) {
			isConnected = true;
			sendEmptyMessage(Actions.CONNECT);
		}
	}

	/**
	 * Closes the connection with alljoyn. After this method call, this bus
	 * handler is invalid and can be released.
	 */
	public synchronized void disconnect() {
		if(isConnected){
			isConnected = false;
			sendEmptyMessage(Actions.DISCONNECT);
		}
	}
	
	/**
	 * Starts advertising the instance with the specified spf identifier
	 * 
	 * @param spfUniqueIdentifier
	 */
	public void advertise(String spfUniqueIdentifier) {
		Message msg = obtainMessage(Actions.ADVERTISE, spfUniqueIdentifier);
		sendMessage(msg);
	}

	/**
	 * Starts discovering new spf instances.
	 */
	public void startDiscovery() {
		sendEmptyMessage(Actions.START_DISCOVERY);
	}
	
	/**
	 * Checks if the bus handler is connected.
	 * @return true if connected
	 */
	public boolean isConnected() {
		
		return isConnected;
	}
	
	/**
	 * Starts advertising the specified profile.
	 * 
	 * @param advertisedProfile
	 * @param sendPeriod
	 */
	public void startSPFAdvertising(String advertisedProfile, long sendPeriod) {
		log(TAG, "BusHandler", "sending SPFAdvertising signal handler message");
		sendSPFAdvertisingSignal(advertisedProfile, sendPeriod);
	}
	
	/**
	 * Stops generating spf advertising messages.
	 */
	public void stopSPFAdvertising() {
		sendEmptyMessage(Actions.STOP_SPF_AVERTISING);
	}
	
	/**
	 * Sends a search signal.
	 * @param sender
	 * @param searchId
	 * @param query
	 */
	public void sendStartSearchMsg(String sender, String searchId, String query) {
		Log.d(TAG, "sending start search message queryId: " + searchId);
		Message msg = this.obtainMessage(Actions.START_SEARCH);
		Bundle data = new Bundle(2);
		data.putString("queryId", searchId);
		data.putString("query", query);
		data.putString("sender", sender);
		msg.setData(data);
		this.handleMessage(msg);
	}
	
	/**
	 * Sends a search result signal.
	 * @param queryId
	 * @param uniqueIdentifier
	 * @param baseInfo
	 */
	public void sendResultSearchMsg(String queryId, String uniqueIdentifier,String baseInfo) {
		Log.d(TAG, "sending search result  message to the bus handler queryId: " + queryId);
		Message msg = this.obtainMessage(Actions.SEARCH_RESULT);
		Bundle data = new Bundle(2);
		data.putString("queryId", queryId);
		data.putString("uniqueId", uniqueIdentifier);
		data.putString("baseInfo", baseInfo);
		msg.setData(data);
		this.handleMessage(msg);
	}
	
}
