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
import java.util.concurrent.Semaphore;

public abstract class GroupActor {

	private static final long REQUEST_TIMEOUT = 60000;
	private static final String TAG = "GroupActor";
	private String myIdentifier;
	private GroupActorListener listener;

	public GroupActor(GroupActorListener listener,String identifier) {
		this.myIdentifier=identifier;
		this.listener = listener;
	}

	abstract void disconnect();

	abstract void sendMessage(WfdMessage msg) throws IOException;

	abstract void connect();

	private Semaphore requestSemaphore = new Semaphore(1, true);
	
	private ResponseHolder respHolder = new ResponseHolder(REQUEST_TIMEOUT);
	
	public WfdMessage sendRequestMessage(WfdMessage msg) throws IOException {
		WfdLog.d(TAG, "Sending request message");
		try {
			requestSemaphore.acquire();
			msg.setSequenceNumber(respHolder.assignRequestSequenceId());
			sendMessage(msg);
			WfdMessage response = respHolder.get();
			requestSemaphore.release();
			return response;
		} catch (InterruptedException e) {
			WfdLog.d(TAG, "interrupted when acquiring request semaphore");
		}
		return null;
	}
	
	private void onResponseReceived(WfdMessage msg) {
		WfdLog.d(TAG, "Request received");
		respHolder.set(msg);
	}
	
	protected void handle(WfdMessage msg) {
		String type = msg.getType();
		if (type.equals(WfdMessage.TYPE_INSTANCE_DISCOVERY)){
			onInstanceDiscovery(msg);
		}else if (type.equals(WfdMessage.TYPE_SIGNAL)) {
			deliverToApplication(msg);
		} else if (type.equals(WfdMessage.TYPE_REQUEST)) {
			onRequestReceived(msg);
		} else if (type.equals(WfdMessage.TYPE_RESPONSE)) {
			onResponseReceived(msg);
		}else if (type.equals(WfdMessage.TYPE_RESPONSE_ERROR)){
			onResponseReceived(msg);
		}
	}
	
	private void onInstanceDiscovery(WfdMessage msg) {
		String identifier = msg.getString(WfdMessage.ARG_IDENTIFIER);
		boolean status = msg.getBoolean(WfdMessage.ARG_STATUS);
		if(status == WfdMessage.INSTANCE_FOUND){
			WfdLog.d(TAG, "Instance found: "+ identifier);
			listener.onInstanceFound(identifier);
		}else{
			WfdLog.d(TAG, "Instance lost: "+ identifier);
			listener.onInstanceLost(identifier);
		}
	}

	private void onRequestReceived(WfdMessage msg) {
		WfdLog.d(TAG, "request message received");
		WfdMessage response;
		try {
			response = listener.onRequestMessageReceived(msg);
			response.setSequenceNumber(msg.getTimestamp());
			response.setType(WfdMessage.TYPE_RESPONSE);
			response.setReceiverId(msg.getSenderId());
			response.setSenderId(myIdentifier);
			sendMessage(response);
		} catch (Throwable tr) {
			WfdLog.e(TAG, "onRequestReceived error", tr);
			response = new WfdMessage();
			response.setReceiverId(msg.getSenderId());
			response.setSenderId(myIdentifier);
			response.setType(WfdMessage.TYPE_RESPONSE_ERROR);
			response.setSequenceNumber(msg.getTimestamp());
			try {
				sendMessage(response);
			} catch (IOException e) {
				
			}
		}
	}

	private void deliverToApplication(WfdMessage msg) {
		WfdLog.d(TAG, "deliverying message to application");
		try {
			listener.onMessageReceived(msg);
		} catch (Throwable e) {
			WfdLog.e(TAG, "Delivery to application failed", e);
		}
	}
	
	protected String getIdentifier(){
		return myIdentifier;
	}

	protected void onError() {
		WfdLog.d(TAG, "onError()");
		listener.onError();
	}
	
}
