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

import java.io.IOException;

import com.google.gson.JsonObject;

import android.util.Log;

import it.polimi.spf.framework.services.InvocationMarshaller;
import it.polimi.spf.framework.proximity.SPFRemoteInstance;
import it.polimi.spf.framework.security.ContactRequest;
import it.polimi.deib.spf.wfd.WfdMessage;
import it.polimi.deib.spf.wfd.WifiDirectMiddleware;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFActivity;

public class WFDRemoteInstance extends SPFRemoteInstance {

	private final WifiDirectMiddleware mMiddleware;
	private final String mIdentifier;

	public static interface Factory {
		public SPFRemoteInstance createRemoteInstance(String identifier);
	}

	public WFDRemoteInstance(WifiDirectMiddleware middleware, String identifier) {
		this.mMiddleware = middleware;
		this.mIdentifier = identifier;
	}

	@Override
	public String getUniqueIdentifier() {
		return mIdentifier;
	}

	@Override
	public InvocationResponse executeService(InvocationRequest request) {
		WfdMessage message = new WfdMessage();
		message.put(WFDMessageContract.KEY_METHOD_ID, WFDMessageContract.ID_EXECUTE_SERVICE);
		message.put(WFDMessageContract.KEY_REQUEST, InvocationMarshaller.toJsonElement(request));
		WfdMessage response = mMiddleware.sendRequestMessage(message, mIdentifier);
		JsonObject respJsonO = response.getJsonObject(WFDMessageContract.KEY_RESPONSE);
		return InvocationMarshaller.responsefromJsonElement(respJsonO);
	}

	@Override
	protected String getProfileBulk(String token, String identifierList, String appIdentifier) {
		WfdMessage message = new WfdMessage();
		message.put(WFDMessageContract.KEY_METHOD_ID, WFDMessageContract.ID_GET_PROFILE_BULK);
		message.put(WFDMessageContract.KEY_TOKEN, token);
		message.put(WFDMessageContract.KEY_FIELD_IDENTIFIERS, identifierList);
		message.put(WFDMessageContract.KEY_APP_IDENTIFIER, appIdentifier);
		WfdMessage response = mMiddleware.sendRequestMessage(message, mIdentifier);
		return response.getString(WFDMessageContract.KEY_RESPONSE);
	}

	@Override
	protected void sendNotification(String senderIdentifier, String action) {
		WfdMessage message = new WfdMessage();
		message.put(WFDMessageContract.KEY_METHOD_ID, WFDMessageContract.ID_SEND_NOTIFICATION);
		message.put(WFDMessageContract.KEY_SENDER_IDENTIFIER, senderIdentifier);
		message.put(WFDMessageContract.KEY_ACTION, action);
		try {
			mMiddleware.sendMessage(message, mIdentifier);
		} catch (IOException e) {
			logException("sendNotification", e);
		}
	}

	private void logException(String methodName, IOException e) {
		Log.e("WFDRemoteInstance", "Exception @ " + methodName, e);
	}

	@Override
	public InvocationResponse sendActivity(SPFActivity activity) {
		WfdMessage message = new WfdMessage();
		message.put(WFDMessageContract.KEY_METHOD_ID, WFDMessageContract.ID_SEND_ACTIVITY);
		message.put(WFDMessageContract.KEY_ACTIVITY, InvocationMarshaller.toJsonElement(activity));
		WfdMessage response = mMiddleware.sendRequestMessage(message, mIdentifier);
		JsonObject resp = response.getJsonObject(WFDMessageContract.KEY_RESPONSE);
		return InvocationMarshaller.responsefromJsonElement(resp);
	}

	@Override
	public void sendContactRequest(ContactRequest request) {
		// FIXME JSON
		String requestJson = request.toJSON();
		WfdMessage message = new WfdMessage();
		message.put(WFDMessageContract.KEY_METHOD_ID, WFDMessageContract.ID_SEND_CONTACT_REQUEST);
		message.put(WFDMessageContract.KEY_REQUEST, requestJson);
		try {
			mMiddleware.sendMessage(message, mIdentifier);
		} catch (IOException e) {
			logException("sendContactRequest", e);
		}

	}
}