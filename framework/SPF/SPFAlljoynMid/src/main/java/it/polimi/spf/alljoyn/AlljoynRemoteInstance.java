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

import it.polimi.spf.framework.proximity.SPFRemoteInstance;
import it.polimi.spf.framework.security.ContactRequest;
import it.polimi.spf.framework.services.InvocationMarshaller;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFActivity;

/**
 * Subclass of {@link SPFRemoteInstance} based on the Alljoyn middleware.
 * 
 * @author darioarchetti
 * 
 */
/*package*/ class AlljoynRemoteInstance extends SPFRemoteInstance {

	private String mWellKnownName;
	private String mUniqueIdentifier;
	private BusHandler mBusHandler;
	private AlljoynProximityInterface mRemoteAlljoyn;

	public AlljoynRemoteInstance(String wellKnownName, BusHandler busHandler) {
		this.mWellKnownName = wellKnownName;
		this.mUniqueIdentifier = removeWellKnownNamePrefix(wellKnownName);
		this.mBusHandler = busHandler;
	}

	public static String removeWellKnownNamePrefix(String wellKnownName) {
		return wellKnownName.replace(BusHandler.PROXIMITY_WKN_PREFIX + ".", "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.spf.framework.proximity.SPFRemoteInstance#getUniqueIdentifier
	 * ()
	 */
	@Override
	public String getUniqueIdentifier() {
		return mUniqueIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.spf.framework.proximity.SPFRemoteInstance#getProfileBulk
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected String getProfileBulk(String token, String identifierList, String appIdentifier) {
		if (!isConnected()) {
			connect();
		}

		return mRemoteAlljoyn.getProfileBulk(token, appIdentifier, identifierList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.spf.framework.proximity.SPFRemoteInstance#sendNotification
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	protected void sendNotification(String senderIdentifier, String action) {
		if (!isConnected()) {
			connect();
		}
		mRemoteAlljoyn.sendNotification(senderIdentifier, action);
	}

	// Helpers
	private boolean isConnected() {
		return mRemoteAlljoyn != null;
	}

	private synchronized void connect() {
		this.mRemoteAlljoyn = mBusHandler.joinSession(mWellKnownName);
	}

	@Override
	public InvocationResponse executeService(InvocationRequest request) {
		if (!isConnected()) {
			connect();
		}
		String reqToSend = InvocationMarshaller.toJson(request);
		String respReceived = mRemoteAlljoyn.executeService(reqToSend);
		InvocationResponse response = InvocationMarshaller.responsefromJson(respReceived);
		return response;
	}

	@Override
	public InvocationResponse sendActivity(SPFActivity activity) {
		if (!isConnected()) {
			connect();
		}

		String resp = this.mRemoteAlljoyn.sendActivity(InvocationMarshaller.toJson(activity));
		return InvocationMarshaller.responsefromJson(resp);
	}

	@Override
	public void sendContactRequest(ContactRequest request) {
		if (!isConnected()) {
			connect();
		}
		// FIXME Json
		mRemoteAlljoyn.sendFriendshipMessage(request.toJSON());
	}
}