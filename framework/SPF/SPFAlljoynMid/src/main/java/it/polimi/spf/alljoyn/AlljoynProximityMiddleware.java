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

import it.polimi.spf.framework.proximity.InboundProximityInterface;
import it.polimi.spf.framework.proximity.ProximityMiddleware;
import android.content.Context;
import android.os.HandlerThread;
import android.util.Log;

public class AlljoynProximityMiddleware implements ProximityMiddleware {

	private static final String THREAD_NAME = "SPF_ALLJOYN_MIDDLEWARE";
	private static final long MIN_SPF_ADVERTISE_PERIOD = 5000;
	private static final String TAG = "AlljoynMiddleware";

	private BusHandler mBusHandler;

	private final Context mContext;
	private final InboundProximityInterface mProximityInterface;
	private final String mIdentifier;

	private String mAdvertisedProfile = null;
	private Long mSPFAdvertisingPeriod = null;

	/**
	 * Factory to create instances of {@link AlljoynProximityMiddleware}
	 */
	public static final ProximityMiddleware.Factory FACTORY = new Factory() {

		@Override
		public ProximityMiddleware createMiddleware(Context context, InboundProximityInterface iface, String identifier) {
			return new AlljoynProximityMiddleware(context, iface, identifier);
		}
	};

	private AlljoynProximityMiddleware(Context context, InboundProximityInterface proximityInterface, String identifier) {
		this.mContext = context;
		this.mProximityInterface = proximityInterface;
		this.mIdentifier = identifier;
	}

	@Override
	public synchronized void connect() {
		if (isConnected()) {
			Log.w(TAG, "start called when isConnected() == true");
			return;
		}

		HandlerThread mMiddlewareThread = new HandlerThread(THREAD_NAME);
		mMiddlewareThread.start();

		mBusHandler = new BusHandler(mContext, mMiddlewareThread.getLooper(), mProximityInterface);
		mBusHandler.connect();
		mBusHandler.advertise(mIdentifier);
		mBusHandler.startDiscovery();

		if (mAdvertisedProfile != null) {
			mBusHandler.startSPFAdvertising(mAdvertisedProfile, mSPFAdvertisingPeriod);
		}
	}

	@Override
	public void disconnect() {
		if (isConnected()) {
			mBusHandler.disconnect();
			mBusHandler = null;
		} else {
			Log.w(TAG, "stop called when isConnected() == false");
		}
	}

	@Override
	public void sendSearchSignal(String sender, String searchId, String query) {
		if (isConnected()) {
			mBusHandler.sendStartSearchMsg(sender, searchId, query);
		}
	}

	@Override
	public void sendSearchResult(String queryId, String uniqueIdentifier, String baseInfo) {
		if (isConnected()) {
			mBusHandler.sendResultSearchMsg(queryId, uniqueIdentifier, baseInfo);
		}
	}

	@Override
	public boolean isConnected() {
		return mBusHandler != null && mBusHandler.isConnected();
	}

	@Override
	public void registerAdvertisement(String advertisedProfile, long sendPeriod) {
		if (isAdvertising()) {
			Log.e(TAG, "Already advertising when startAdvertising() called");
			return;
		}

		if (sendPeriod <= MIN_SPF_ADVERTISE_PERIOD) {
			sendPeriod = MIN_SPF_ADVERTISE_PERIOD;
		}

		mAdvertisedProfile = advertisedProfile;
		mSPFAdvertisingPeriod = sendPeriod;
		Log.d(TAG, "Advertising profile " + advertisedProfile);

		if (isConnected()) {
			mBusHandler.startSPFAdvertising(advertisedProfile, sendPeriod);
		}
	}

	@Override
	public void unregisterAdvertisement() {
		if (!isAdvertising()) {
			Log.w(TAG, "Not advertising when stopAdvertising() called");
			return;
		}

		mAdvertisedProfile = null;
		mSPFAdvertisingPeriod = null;
		Log.d(TAG, "Stopped advertising");

		if (isConnected()) {
			mBusHandler.stopSPFAdvertising();
		}
	}

	@Override
	public boolean isAdvertising() {
		return mAdvertisedProfile != null;
	}
}