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
package it.polimi.spf.framework.notification;

import android.os.Parcel;
import android.os.Parcelable;

public class NotificationMessage implements Parcelable {
	//TODO add picture and display name
	private final long mId;
	private final String mSenderId;
	private final String mTitle;
	private final String mMessage;

	public NotificationMessage(long id, String sender, String title, String message) {
		this.mId = id;
		this.mSenderId = sender;
		this.mTitle = title;
		this.mMessage = message;
	}

	public NotificationMessage(String sender, String title, String message) {
		this(-1, sender, title, message);
	}

	private NotificationMessage(Parcel source) {
		mId = source.readLong();
		mSenderId = source.readString();
		mTitle = source.readString();
		mMessage = source.readString();
	}

	public long getId() {
		return mId;
	}

	public String getSenderId() {
		return mSenderId;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getMessage() {
		return mMessage;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mId);
		dest.writeString(mSenderId);
		dest.writeString(mTitle);
		dest.writeString(mSenderId);
	}

	public static final Parcelable.Creator<NotificationMessage> CREATOR = new Creator<NotificationMessage>() {

		@Override
		public NotificationMessage[] newArray(int size) {
			return new NotificationMessage[size];
		}

		@Override
		public NotificationMessage createFromParcel(Parcel source) {
			return new NotificationMessage(source);

		}
	};

}
