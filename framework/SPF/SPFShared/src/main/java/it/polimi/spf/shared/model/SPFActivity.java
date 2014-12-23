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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Interoperable key-value data container describing a potential or completed
 * social action, inspired by the ActivityStreams specification.
 * 
 * Each activity features a set of standard fields:
 * <ul>
 * <li><b>Verb</b>: a string that identifies the type of the social action</li>
 * <li><b>SenderIdentifier</b>: the identifier of the user who sent the activity
 * </li>
 * <li><b>SenderDisplayName</b>: the display name of the user who sent the
 * activity</li>
 * <li><b>ReceivedIdentifier</b>: the identifier of the target of the request</li>
 * <li><b>ReceiverDisplayName</b>: the display name of the target of the
 * activity.</li>
 * </ul>
 * 
 * The personal details of the sender and the receiver are automatically
 * injected by the framework without the need of additional lookups; activities
 * may also contain any custom field needed to describe the action.
 * 
 * @see #get(String)
 * @author darioarchetti
 */
public class SPFActivity implements Parcelable {

	/**
	 * Key for the Verb of the activity.
	 * 
	 * @see #get(String)
	 */
	public static final String VERB = "verb";

	/**
	 * Key for the identifier of the sender
	 * 
	 * @see #get(String)
	 */
	public static final String SENDER_IDENTIFIER = "senderIdentifier";

	/**
	 * Key for the display name of the sender
	 * 
	 * @see #get(String)
	 */
	public static final String SENDER_DISPLAY_NAME = "senderDisplayName";

	/**
	 * Key for the identifier of the receiver
	 * 
	 * @see #get(String)
	 */
	public static final String RECEIVER_IDENTIFIER = "receiverIdentifier";

	/**
	 * Key for the display name of the receiver
	 * 
	 * @see #get(String)
	 */
	public static final String RECEIVER_DISPLAY_NAME = "receiverDisplayName";

	private final Map<String, String> mFields;

	/**
	 * Creates a new activity with a given verb.
	 * 
	 * @param verb
	 *            - the verb of the activity
	 */
	public SPFActivity(String verb) {
		this();
		if (verb == null) {
			throw new NullPointerException();
		}

		mFields.put(VERB, verb);
	}

	private SPFActivity() {
		this.mFields = new HashMap<String, String>();
	}

	/**
	 * Returns the verb of the activity
	 * 
	 * @return
	 */
	public String getVerb() {
		return mFields.get(VERB);
	}

	/**
	 * Puts a detail in <code>this</code> activity
	 * 
	 * @param key
	 *            - the key of the detail
	 * @param value
	 *            - the value of the detail
	 */
	public void put(String key, String value) {
		if (key.equals(VERB)) {
			return;
		}

		mFields.put(key, value);
	}

	/**
	 * Retrieves the value of a detail contained in <code>this</code> activity.
	 * 
	 * @param key
	 *            - the key of the detail
	 * @return the value of the detail, or null if the key is not valid
	 */
	public String get(String key) {
		return mFields.get(key);
	}

	/**
	 * @return the set of the detail keys
	 */
	public Set<String> keySet() {
		return mFields.keySet();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
		return "[SPFActivity " + mFields.toString() + "]";
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Bundle b = new Bundle();

		for (String k : mFields.keySet()) {
			b.putString(k, mFields.get(k));
		}

		b.writeToParcel(dest, 0);
	}

	public static final Creator<SPFActivity> CREATOR = new Creator<SPFActivity>() {

		@Override
		public SPFActivity[] newArray(int size) {
			return new SPFActivity[size];
		}

		@Override
		public SPFActivity createFromParcel(Parcel source) {
			SPFActivity ac = new SPFActivity();
			ac.readFromParcel(source);
			return ac;
		}
	};

	public void readFromParcel(Parcel parcel) {
		Bundle b = parcel.readBundle();
		for (String k : b.keySet()) {
			mFields.put(k, b.getString(k));
		}
	}
}
