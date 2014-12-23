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
package it.polimi.spf.framework.security;

import it.polimi.spf.framework.SPF;

import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.framework.profile.SPFProfileManager;
import it.polimi.spf.framework.proximity.FieldContainerMarshaller;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.ProfileFieldContainer;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;

public class ContactRequest {

	private static final String TOKEN = "token";
	private static final String CONTAINER = "container";

	private static final String TAG = "FriendshipMessage";

	// Fields to marshall
	private final String mReceiveToken;
	private final ProfileFieldContainer mContainer;

	private ContactRequest(String receiveToken, ProfileFieldContainer container) {
		this.mReceiveToken = receiveToken;
		this.mContainer = container;
	}

	public String getAccessToken() {
		return mReceiveToken;
	}

	public String getUserIdentifier() {
		return mContainer.getFieldValue(ProfileField.IDENTIFIER);
	}

	public String getDisplayName() {
		return mContainer.getFieldValue(ProfileField.DISPLAY_NAME);
	}

	public Bitmap getProfilePicture() {
		return mContainer.getFieldValue(ProfileField.PHOTO);
	}

	public static ContactRequest fromJSON(String friendshipMessageJSON) {
		try {
			JSONObject json = new JSONObject(friendshipMessageJSON);
			String token = json.getString(TOKEN);
			ProfileFieldContainer container = FieldContainerMarshaller.unmarshallContainer(json.getString(CONTAINER));

			return new ContactRequest(token, container);
		} catch (JSONException e) {
			Log.e(TAG, "Error unmarshalling friendship message", e);
			return null;
		}

	}

	public static ContactRequest create(String token) {
		SPFProfileManager profile = SPF.get().getProfileManager();
		ProfileFieldContainer container = profile.getProfileFieldBulk(
				SPFPersona.DEFAULT, ProfileField.IDENTIFIER, ProfileField.DISPLAY_NAME, ProfileField.PHOTO);
		return new ContactRequest(token, container);
	}

	public String toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put(TOKEN, mReceiveToken);
			json.put(CONTAINER, FieldContainerMarshaller.marshallContainer(mContainer));
		} catch (JSONException e) {
			Log.e(TAG, "Json error marshalling friendship message:", e);
		}

		return json.toString();
	}
}
