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

import it.polimi.spf.framework.proximity.FieldContainerMarshaller;
import it.polimi.spf.framework.proximity.InboundProximityInterface;
import it.polimi.spf.framework.security.ContactRequest;
import it.polimi.spf.framework.services.InvocationMarshaller;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.SPFActionSendNotification;
import it.polimi.spf.shared.model.SPFActivity;

import org.alljoyn.bus.BusObject;
import org.alljoyn.bus.annotation.BusMethod;

import android.util.Log;

/**
 * Implementation of {@link AlljoynProximityInterface} that dispatches incoming
 * requests to a {@link InboundProximityInterface} instance. It also takes care
 * of catching and logging any throwable to prevent a crash in a alljoyn thread.
 * 
 * @author Dario
 * 
 */
/*package*/ class AlljoynProximityInterfaceImpl implements BusObject, AlljoynProximityInterface {

	private static final String TAG = "ProximityInterfaceImpl";
	private InboundProximityInterface mProximityInterface;

	public AlljoynProximityInterfaceImpl(InboundProximityInterface proximityInterface) {
		this.mProximityInterface = proximityInterface;
	}

	@Override
	public String executeService(String marshalledRequest) {
		try {
			InvocationRequest req = InvocationMarshaller.requestFromJson(marshalledRequest);
			InvocationResponse resp= mProximityInterface.executeService(req);
			return InvocationMarshaller.toJson(resp);
		} catch (Throwable t) {
			logThrowable("executeService", t);
			InvocationResponse resp = InvocationResponse.error(t);
			return InvocationMarshaller.toJson(resp);
		}
	}

	@Override
	@BusMethod
	public String getProfileBulk(String token, String appIdentifier, String fieldList) {
		try {
			return mProximityInterface.getProfileBulk(token, appIdentifier, fieldList);
		} catch (Throwable t) {
			logThrowable("getProfileBulk", t);
			return FieldContainerMarshaller.marshallContainer(new ProfileFieldContainer());
		}
	}

	@Override
	@BusMethod
	public void sendFriendshipMessage(String contactRequestJSON) {
		try {
			ContactRequest cReq = ContactRequest.fromJSON(contactRequestJSON);
			mProximityInterface.sendContactRequest(cReq);
		} catch (Throwable t) {
			logThrowable("sendContactRequest", t);
		}
	}

	@Override
	@BusMethod
	public void sendNotification(String fromUID, String actionSendNotificationJson) {
		try {
			SPFActionSendNotification actionSendNotification = 
					(SPFActionSendNotification) SPFActionSendNotification
					.fromJSON(actionSendNotificationJson);
			mProximityInterface.sendNotification(fromUID, actionSendNotification);
		} catch (Throwable t) {
			logThrowable("sendNotification", t);
		}
	}

	private void logThrowable(String methodName, Throwable t) {
		Log.e(TAG, "Throwable caught @ " + methodName, t);
	}

	@Override
	@BusMethod
	public String sendActivity(String activityjson) {
		try{
			SPFActivity activity = InvocationMarshaller.activityFromJson(activityjson);
			InvocationResponse resp = mProximityInterface.sendActivity(activity);
			return InvocationMarshaller.toJson(resp);
		} catch(Throwable t){
			logThrowable("sendActivity", t);
			InvocationResponse resp = InvocationResponse.error(t);
			return InvocationMarshaller.toJson(resp);
		}
	}
}
