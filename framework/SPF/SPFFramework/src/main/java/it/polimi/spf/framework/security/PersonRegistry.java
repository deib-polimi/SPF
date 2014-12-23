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

import it.polimi.spf.framework.SPFContext;
import it.polimi.spf.framework.SPF;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * @author Jacopo Aliprandi
 * 
 */
public class PersonRegistry {

	private static final String TAG = "PersonRegistry";
	private PersonPermissionTable mTable;
	private ContactsDetailStorage mContactDetail;

	/**
	 * 
	 */
	public PersonRegistry(Context c) {
		if (c == null) {
			throw new NullPointerException();
		}

		mTable = new PersonPermissionTable(c);
		mContactDetail = new ContactsDetailStorage(c);
	}

	/**
	 * 
	 * @param fr
	 * @param accessToken
	 */
	public void onFriendShipMessageReceived(ContactRequest fr) {
		if (fr == null) {
			throw new NullPointerException();
		}

		if (mTable.createEntryForReceivedRequest(fr)) {
			mContactDetail.saveContactDetail(fr.getUserIdentifier(), fr.getDisplayName(), fr.getProfilePicture());
			SPFContext.get().broadcastEvent(SPFContext.EVENT_CONTACT_REQUEST_RECEIVED);
		} else {
			Log.e(TAG, "Cannot create entry for request from " + fr.getUserIdentifier());
		}
	}

	/**
	 * 
	 * @param sender
	 * @param circles
	 */
	public void confirmRequest(PersonInfo sender, String passphrase, List<String> circles) throws TokenCipher.WrongPassphraseException {
		if (sender == null || circles == null) {
			throw new NullPointerException();
		}

		try {
			mTable.confirmRequest(sender.getIdentifier(), passphrase);
		} catch (GeneralSecurityException e) {
			Log.e(TAG, "Decypher error:", e);
			return;
		}

		for (String circle : circles) {
			addPersontoCircle(sender.getIdentifier(), circle);
		}
	}

	/**
	 * 
	 * @param sender
	 */
	public void deleteRequest(PersonInfo sender) {
		if (sender == null) {
			throw new NullPointerException();
		}

		mTable.deleteEntryOf(sender.getIdentifier());
	}

	/**
	 * 
	 * @param targetUID
	 */
	public void sendContactRequestTo(String targetUID, String passphrase, String displayName, Bitmap profilePic, List<String> circles) {
		if (targetUID == null) {
			throw new NullPointerException();
		}

		String token;
		try {
			token = mTable.createEntryForSentRequest(targetUID, passphrase);
		} catch (GeneralSecurityException e) {
			Log.e(TAG, "Error encrypting token", e);
			return;
		}

		Log.d(TAG, "Encrypted token: " + token);

		if (circles != null) {
			for (String circle : circles) {
				addPersontoCircle(targetUID, circle);
			}
		}

		mContactDetail.saveContactDetail(targetUID, displayName, profilePic);
		ContactRequest message = ContactRequest.create(token);
		SPF.get().getPeopleManager().getPerson(targetUID).sendContactRequest(message);
	}

	/**
	 * 
	 * @param token
	 * @return
	 */
	public PersonAuth getPersonAuthFrom(String token) {
		return mTable.getPersonAuthFrom(token);
	}

	/**
	 * 
	 * @param uuid
	 * @return
	 */
	public PersonInfo lookup(String uuid) {
		if (uuid == null) {
			throw new NullPointerException();
		}

		String token = getTokenFor(uuid);
		if (token.equals("")) {
			return null;
		}

		PersonAuth auth = getPersonAuthFrom(token);
		return mContactDetail.getContactInfo(auth);
	}

	/**
	 * 
	 * @param targetUID
	 * @return
	 */
	public String getTokenFor(String targetUID) {
		if (targetUID == null) {
			throw new NullPointerException();
		}

		return mTable.getTokenFor(targetUID);
	}

	/**
	 * TODO Use this method to fetch the friends of the users
	 * 
	 * @return
	 */
	public List<PersonInfo> getAvailableContacts() {
		return getRequestByStatus(PersonPermissionTable.REQUEST_ACCEPTED);
	}

	public List<PersonInfo> getPendingRequests() {
		return getRequestByStatus(PersonPermissionTable.REQUEST_PENDING);
	}

	public int getPendingRequestCount() {
		return mTable.getPendingRequestCount();
	}

	private List<PersonInfo> getRequestByStatus(int status) {
		ArrayList<PersonInfo> result = new ArrayList<PersonInfo>();
		for (PersonAuth auth : mTable.getPersonAuthList(status)) {
			result.add(mContactDetail.getContactInfo(auth));
		}

		return result;
	}

	public void deletePerson(String userUID) {
		mTable.deleteEntryOf(userUID);
	}

	// Circles
	public Collection<String> getCircles() {
		return mTable.getCircles();
	}

	public boolean addCircle(String circle) {
		return mTable.addCircle(circle);
	}

	public boolean removeCircle(String circle) {
		return mTable.removeCircle(circle);
	}

	public boolean addPersontoCircle(String userUID, String circle) {
		return mTable.addPersonToCircle(userUID, circle);
	}

	public boolean removePersonFromCircle(String userUID, String circle) {
		return mTable.removePersonFromCircle(userUID, circle);
	}
}