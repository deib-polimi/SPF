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
import it.polimi.spf.framework.proximity.InboundProximityInterface;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.annotation.BusSignalHandler;

import android.util.Log;

/*package*/ class AlljoynSignalsHandlerImpl implements AlljoynSignalsHandler, BusObject {

	private static String TAG = "AlljoynSPFSignalsHandler";

	private final InboundProximityInterface mProximityInterface;
	private final String mIdentifier;

	public AlljoynSignalsHandlerImpl(InboundProximityInterface proximityInterface) {
		this.mProximityInterface = proximityInterface;
		this.mIdentifier = SPF.get().getUniqueIdentifier();
	}

	@BusSignalHandler(iface = "it.polimi.spf.alljoyn.SPFAlljoynSignalsInterface", signal = "searchSignal")
	public void searchSignal(String sender, String id, String query) throws BusException {
		if (mIdentifier.equals(sender)) {
			return;
		}
		try {
			mProximityInterface.onSearchSignalReceived(id, query);
		} catch (Throwable t) {
			logThrowable("advertisingSignal", t);
		}
	}

	@BusSignalHandler(iface = "it.polimi.spf.alljoyn.SPFAlljoynSignalsInterface", signal = "searchResult")
	public void searchResult(String id, String uniqueIdentifier, String baseInfo) throws BusException {
		if (mIdentifier.equals(uniqueIdentifier)) {
			return;
		}
		try {
			mProximityInterface.onSearchResultReceived(id, uniqueIdentifier, baseInfo);
		} catch (Throwable t) {
			logThrowable("advertisingSignal", t);
		}
	}

	@BusSignalHandler(iface = "it.polimi.spf.alljoyn.SPFAlljoynSignalsInterface", signal = "advertisingSignal")
	public void advertisingSignal(String profile, String uniqueIdentifier) throws BusException {
		if (mIdentifier.equals(uniqueIdentifier)) {
			Log.d(TAG, "dropping self advertising signal");
			return;
		}

		try {
			mProximityInterface.onAdvertisingSignalReceived(profile);
		} catch (Throwable t) {
			logThrowable("advertisingSignal", t);
		}
	}

	private void logThrowable(String methodName, Throwable t) {
		Log.e(TAG, "Throwable caught @ " + methodName, t);
	}
}
