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

public class Conversation {

	private long mId = -1;
	private String mContactIdentifier;
	private String mContactDisplayName;
	private int mUnreadMsgCount;
	private Date mUpdateTime;
	private String mFirstMessage;

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		this.mId = id;
	}

	public String getContactIdentifier() {
		return mContactIdentifier;
	}

	public void setContactIdentifier(String contactIdentifier) {
		this.mContactIdentifier = contactIdentifier;
	}

	public String getContactDisplayName() {
		return mContactDisplayName;
	}

	public void setContactDisplayName(String contactDisplayName) {
		this.mContactDisplayName = contactDisplayName;
	}

	public int getUnreadMsgCount() {
		return mUnreadMsgCount;
	}

	public void setUnreadMsgCount(int unreadMsgCount) {
		this.mUnreadMsgCount = unreadMsgCount;
	}

	public Date getUpdateTime() {
		return mUpdateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.mUpdateTime = updateTime;
	}

	public String getFirstMessage() {
		return mFirstMessage;
	}

	public void setFirstMessage(String firstMessage) {
		this.mFirstMessage = firstMessage;
	}
}