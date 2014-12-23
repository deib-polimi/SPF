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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WfdMessage {
	
	private static final String KEY_SENDER_ID = "senderId";
	private static final String KEY_RECEIVER_ID = "receiverId";
	private static final String KEY_MSG_TYPE = "type";
	private static final String KEY_TIMESTAMP = "sequenceNumber";
	private static final String KEY_MSG_CONTENT = "msgContent";

	/**
	 * Used when a message is addressed to the whole group.
	 */
	static final String BROADCAST_RECEIVER_ID = "SEND_TO_ALL";

	/**
	 * Used when no target is needed: e.g. connection does not need receiver and
	 * instance discovery does not need sender.
	 */
	static final String UNKNOWN_RECEIVER_ID = "UNKNOWN_RECEIVER_ID";

	/*
	 * Message types
	 */
	static final String TYPE_CONNECT = "CONNECT";
	static final String TYPE_SIGNAL = "SIGNAL";
	static final String TYPE_REQUEST = "REQUEST";
	static final String TYPE_RESPONSE = "RESPONSE";
	static final String TYPE_RESPONSE_ERROR = "response_error";
	static final String TYPE_INSTANCE_DISCOVERY = "DISCOVERY";

	/*
	 * Discovery messages parameters (used internally).
	 */
	/**
	 * Content name for discovery messages: the identifier of the instance that
	 * was found or lost.
	 */
	static final String ARG_IDENTIFIER = "identifier";
	/**
	 * Content name for discovery messages: boolean that indicates if the
	 * instance is found (true) or lost (false);
	 */
	static final String ARG_STATUS = "status";
	/**
	 * Value for {@link WfdMessage#ARG_STATUS}:
	 */
	static final boolean INSTANCE_FOUND = true;
	/**
	 * Value for {@link WfdMessage#ARG_STATUS}
	 */
	static final boolean INSTANCE_LOST = false;

	String receiverId = UNKNOWN_RECEIVER_ID;// default
	String senderId = UNKNOWN_RECEIVER_ID;
	String type = TYPE_SIGNAL;// default

	/**
	 * Holds the payload of the message.
	 */
	private JsonObject msgContent;

	/*
	 * if the type is request or response , it is used to associate the pair of
	 * messages.
	 */
	private long sequenceNumber = -1;

	/**
	 * Constructs an empty message
	 */
	public WfdMessage() {
		msgContent = new JsonObject();
	}

	/**
	 * Returns the string representation for this message.
	 */
	public String toString() {
		JsonObject msgJSON = new JsonObject();
		msgJSON.addProperty(KEY_SENDER_ID, senderId);
		msgJSON.addProperty(KEY_RECEIVER_ID, receiverId);
		msgJSON.addProperty(KEY_TIMESTAMP, sequenceNumber);
		msgJSON.addProperty(KEY_MSG_TYPE, type);
		msgJSON.add(KEY_MSG_CONTENT, msgContent);
		Gson g = new Gson();
		return g.toJson(msgJSON);
	}

	/**
	 * Returns a {@link WfdMessage} given its string representation. If the
	 * string cannot be parsed returns null.
	 * 
	 * @param str
	 * @return
	 */
	static WfdMessage fromString(String str) {
		JsonObject o = new JsonParser().parse(str).getAsJsonObject();
		WfdMessage msg = new WfdMessage();
		msg.msgContent = o.getAsJsonObject(KEY_MSG_CONTENT);
		msg.type = o.get(KEY_MSG_TYPE).getAsString();
		if (msg.type.equals(TYPE_REQUEST) || msg.type.equals(TYPE_RESPONSE)) {
			msg.sequenceNumber = o.get(KEY_TIMESTAMP).getAsLong();
		} else {
			msg.sequenceNumber = -1;
		}
		msg.receiverId = o.get(KEY_RECEIVER_ID).getAsString();
		msg.senderId = o.get(KEY_SENDER_ID).getAsString();
		return msg;

	}

	/**
	 * 
	 * @param senderId
	 *            - the identifier to set
	 */
	void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	/**
	 * 
	 * @param targetId
	 *            -the identifier to set
	 */
	void setReceiverId(String targetId) {
		receiverId = targetId;
	}

	/**
	 * @return the receiverId
	 */
	String getReceiverId() {
		return receiverId;
	}

	/**
	 * @return the senderId
	 */
	String getSenderId() {
		return senderId;
	}

	/**
	 * Set the type of the message.
	 * 
	 * @param msgType
	 */
	void setType(String msgType) {
		this.type = msgType;
	}

	/**
	 * 
	 * @return the type of the message
	 */
	String getType() {
		return type;
	}

	/**
	 * 
	 * @param clock
	 */
	void setSequenceNumber(long clock) {
		this.sequenceNumber = clock;
	}
	/**
	 * 
	 * @return
	 */
	long getTimestamp() {

		return sequenceNumber;
	}

	/**
	 * Maps name to value.
	 * 
	 * @param name
	 *            - the name of the mapping, not null
	 * @param value
	 *            - the value of the mapping, not null
	 * 
	 */
	public void put(String name, String value) {

		msgContent.addProperty(name, value);

	}

	/**
	 * Return the String associated to the specified name.
	 * 
	 * @param name
	 * @return - the associated string
	 */
	public String getString(String name) {
		return msgContent.get(name).getAsString();

	}

	/**
	 * Maps name to value
	 * 
	 * @param name
	 *            - the name of the mapping
	 * @param value
	 *            - the value of the mapping
	 * 
	 */
	public void put(String name, boolean value) {

		msgContent.addProperty(name, value);

	}

	/**
	 * 
	 * @param name
	 * @return the value, or false if the mapping does not exist or cannot be
	 *         coerced to a boolean
	 */
	public boolean getBoolean(String name) {

		return msgContent.get(name).getAsBoolean();

	}

	/**
	 * Maps name to value
	 * 
	 * @param name
	 *            - the name of the mapping
	 * @param value
	 *            - the value of the mapping
	 * 
	 */
	public void put(String name, int value) {
		msgContent.addProperty(name, value);

	}

	/**
	 * Return the integer mapped to the specified name. If the mappings does not
	 * exists returns the provided defaultValue.
	 * 
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public int getInt(String name, int defaultValue) {

		return msgContent.get(name).getAsInt();

	}

	public JsonObject getJsonObject(String name) {
		return msgContent.get(name).getAsJsonObject();
	}
	
	public void put(String name, JsonElement value){
		msgContent.add(name, value);
	}
}
