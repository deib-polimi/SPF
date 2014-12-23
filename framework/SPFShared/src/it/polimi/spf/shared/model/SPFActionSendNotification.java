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
package it.polimi.spf.shared.model;

import android.os.Parcel;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * TODO #Documentation
 * 
 * @author Jacopo Aliprandi
 */
public final class SPFActionSendNotification extends SPFAction {

    private static final String KEY_MESSAGE = "message";

    private static final String KEY_TITLE = "title";

    private static final String TAG = null;

    private final int mType = TYPE_NOTIFICATION;

    /**
     * The title to show in the notification.
     */
    private final String mTitle;

    /**
     * the message to show in the notification.
     */
    private final String mMessage;

	/*
     * other properties can be: icon sound intent when the notification is
	 * pressed ...
	 */

    /**
     * TODO comment
     */
    public SPFActionSendNotification(String title, String message) {
        this.mTitle = title;
        this.mMessage = message;
    }

    private SPFActionSendNotification(Parcel source) {
        this.mTitle = source.readString();
        this.mMessage = source.readString();
    }

    public SPFActionSendNotification(JSONObject source) {
        try {
            mTitle = source.getString(KEY_TITLE);
            mMessage = source.getString(KEY_MESSAGE);
        } catch (JSONException e) {
            throw new IllegalArgumentException("JSON does not represent a valid SPFActionNotification");
        }
    }

    public String getTitle() {
        return mTitle;
    }

    public String getMessage() {
        return mMessage;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mMessage);
    }

    /*
     * (non-Javadoc)
     *
     * @see it.polimi.spf.framework.local.SPFAction#toJSON()
     */
    @Override
    public String toJSON() {
        try {
            JSONObject o = new JSONObject();
            o.put(KEY_TYPE, mType);
            o.put(KEY_TITLE, mTitle);
            o.put(KEY_MESSAGE, mMessage);
            return o.toString();
        } catch (JSONException e) {
            Log.e(TAG, "Error marshalling:", e);
            return "";
        }
    }

    public static Creator<SPFActionSendNotification> CREATOR = new Creator<SPFActionSendNotification>() {

        @Override
        public SPFActionSendNotification[] newArray(int size) {
            return new SPFActionSendNotification[size];
        }

        @Override
        public SPFActionSendNotification createFromParcel(Parcel source) {
            return new SPFActionSendNotification(source);
        }
    };

}
