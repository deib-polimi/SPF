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
package it.polimi.spf.demo.chat.model;

import java.util.Date;

public class Message {
	
	private String mSenderId;
	private String mSenderDisplayName;
	private String mText;
	private boolean mRead;
	private Date mCreationTime;

	public String getSenderId() {
		return mSenderId;
	}

	public void setSenderId(String senderId) {
		this.mSenderId = senderId;
	}

	public String getSenderDisplayName() {
		return mSenderDisplayName;
	}

	public void setSenderDisplayName(String senderDisplayName) {
		this.mSenderDisplayName = senderDisplayName;
	}

	public String getText() {
		return mText;
	}

	public void setText(String text) {
		this.mText = text;
	}

	public boolean isRead() {
		return mRead;
	}

	public void setRead(boolean read) {
		this.mRead = read;
	}

	public Date getCreationTime() {
		return mCreationTime;
	}

	public void setCreationTime(Date creationTime) {
		this.mCreationTime = creationTime;
	}
}
