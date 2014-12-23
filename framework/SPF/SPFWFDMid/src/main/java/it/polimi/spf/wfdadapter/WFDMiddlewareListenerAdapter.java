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
package it.polimi.spf.wfdadapter;

import com.google.gson.JsonObject;

import android.util.Log;
import it.polimi.spf.framework.services.InvocationMarshaller;
import it.polimi.spf.framework.proximity.InboundProximityInterface;
import it.polimi.spf.framework.proximity.SPFRemoteInstance;
import it.polimi.spf.framework.security.ContactRequest;

import it.polimi.deib.spf.wfd.WfdMessage;
import it.polimi.deib.spf.wfd.WfdMiddlewareListener;

import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFActionSendNotification;
import it.polimi.spf.shared.model.SPFActivity;

public class WFDMiddlewareListenerAdapter implements WfdMiddlewareListener {

	private static final String TAG = "MiddlewareListener";
	private final WFDRemoteInstance.Factory mInstanceFactory;
	private final InboundProximityInterface mProximityInterface;

	public WFDMiddlewareListenerAdapter(InboundProximityInterface proximityInterface, WFDRemoteInstance.Factory factory) {
		this.mProximityInterface = proximityInterface;
		this.mInstanceFactory = factory;
	}

	@Override
	public void onInstanceFound(String identifier) {
		SPFRemoteInstance instance = mInstanceFactory.createRemoteInstance(identifier);
		mProximityInterface.onRemoteInstanceFound(instance);
	}

	@Override
	public void onInstanceLost(String identifier) {
		mProximityInterface.onRemoteInstanceLost(identifier);
	}

	@Override
	public void onMessageReceived(WfdMessage request) {
		Log.d(TAG, "Handling message");
		switch (request.getInt(WFDMessageContract.KEY_METHOD_ID, -1)) {
		case WFDMessageContract.ID_SEND_CONTACT_REQUEST: {
			String contactRequest = request.getString(WFDMessageContract.KEY_REQUEST);
			mProximityInterface.sendContactRequest(ContactRequest.fromJSON(contactRequest));
			break;
		}
		case WFDMessageContract.ID_SEND_NOTIFICATION: {
			String identifier = request.getString(WFDMessageContract.KEY_SENDER_IDENTIFIER);
			String actionjson = request.getString(WFDMessageContract.KEY_ACTION);
			SPFActionSendNotification spfAction = (SPFActionSendNotification) SPFActionSendNotification.fromJSON(actionjson);
			mProximityInterface.sendNotification(identifier, spfAction);
			break;
		}
		case WFDMessageContract.ID_SEND_SEARCH_RESULT: {
			String queryId = request.getString(WFDMessageContract.KEY_QUERY_ID);
			String senderId = request.getString(WFDMessageContract.KEY_SENDER_IDENTIFIER);
			String baseInfo = request.getString(WFDMessageContract.KEY_BASE_INFO);
			mProximityInterface.onSearchResultReceived(queryId, senderId, baseInfo);
			break;
		}
		case WFDMessageContract.ID_SEND_SEARCH_SIGNAL: {
			String searchId = request.getString(WFDMessageContract.KEY_QUERY_ID);
			// String senderId =
			// request.getString(WFDMessageContract.KEY_SENDER_IDENTIFIER);
			String query = request.getString(WFDMessageContract.KEY_QUERY);
			mProximityInterface.onSearchSignalReceived(searchId, query);
			break;
		}
		case WFDMessageContract.ID_SEND_SPF_ADVERTISING:
			String advProfile = request.getString(WFDMessageContract.KEY_ADV_PROFILE);
			mProximityInterface.onAdvertisingSignalReceived(advProfile);
			break;

		default:
			break;
		}
	}

	@Override
	public WfdMessage onRequestMessageReceived(WfdMessage message) {
		Log.d(TAG, "Handling request message");
		WfdMessage response = new WfdMessage();

		switch (message.getInt(WFDMessageContract.KEY_METHOD_ID, -1)) {
		case WFDMessageContract.ID_EXECUTE_SERVICE: {
			JsonObject requestjson = message.getJsonObject(WFDMessageContract.KEY_REQUEST);
			final InvocationRequest request = InvocationMarshaller.requestfromJsonElement(requestjson);
			InvocationResponse invocationResponse = mProximityInterface.executeService(request);
			response.put(WFDMessageContract.KEY_RESPONSE, InvocationMarshaller.toJsonElement(invocationResponse));
			break;
		}
		case WFDMessageContract.ID_SEND_ACTIVITY: {
			JsonObject activity = message.getJsonObject(WFDMessageContract.KEY_ACTIVITY);
			SPFActivity spfActivity;
			spfActivity = InvocationMarshaller.activityFromJsonElement(activity);
			InvocationResponse resp = mProximityInterface.sendActivity(spfActivity);
			response.put(WFDMessageContract.KEY_RESPONSE, InvocationMarshaller.toJsonElement(resp));
			break;
		}
		case WFDMessageContract.ID_GET_PROFILE_BULK: {
			String token = message.getString(WFDMessageContract.KEY_TOKEN);
			String identifiers = message.getString(WFDMessageContract.KEY_FIELD_IDENTIFIERS);
			String appIdentifier = message.getString(WFDMessageContract.KEY_APP_IDENTIFIER);
			String profileBulk = mProximityInterface.getProfileBulk(token, appIdentifier, identifiers);
			response.put(WFDMessageContract.KEY_RESPONSE, profileBulk);
			break;
		}
		default:
			break;
		}

		return response;
	}

	@Override
	public void onError() {
		Log.e("WFDMiddlewareListener", "Error from middleware");
	}
}
