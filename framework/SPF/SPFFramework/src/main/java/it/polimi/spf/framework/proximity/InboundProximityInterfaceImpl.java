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
package it.polimi.spf.framework.proximity;

import com.google.gson.Gson;

import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.Utils;
import it.polimi.spf.framework.notification.NotificationMessage;
import it.polimi.spf.framework.notification.SPFAdvProfile;
import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.framework.search.SearchResponder;
import it.polimi.spf.framework.search.SearchResult;
import it.polimi.spf.framework.security.ContactRequest;
import it.polimi.spf.framework.security.PersonAuth;
import it.polimi.spf.framework.security.SPFSecurityMonitor;
import it.polimi.spf.shared.model.BaseInfo;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.SPFActionSendNotification;
import it.polimi.spf.shared.model.SPFActivity;

public class InboundProximityInterfaceImpl implements InboundProximityInterface {

	private final static String TAG = "InboundProximityInterface";

	private final SPF mSpf;

	public InboundProximityInterfaceImpl(SPF spf) {
		this.mSpf = spf;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.spf.framework.proximity.ProximityResponder#getProfileBulk
	 * (java.lang.String, java.lang.String, java.lang.String[])
	 */
	@Override
	public String getProfileBulk(String token, String appIdentifier, String fields) {
		Utils.logCall(TAG, "getProfileBulk", token, appIdentifier, fields);

		String[] fieldListArray = FieldContainerMarshaller.unmarshallIdentifierList(fields);

		SPFSecurityMonitor monitor = mSpf.getSecurityMonitor();
		PersonAuth auth = monitor.getPersonRegistry().getPersonAuthFrom(token);
		SPFPersona persona = monitor.getPersonaOf(appIdentifier);
		ProfileFieldContainer container = mSpf.getProfileManager().getProfileFieldBulk(auth, persona, fieldListArray);

		return FieldContainerMarshaller.marshallContainer(container);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.spf.framework.proximity.ProximityResponder#executeService
	 * (it.polimi.spf.framework.local.InvocationRequest)
	 */
	@Override
	public InvocationResponse executeService(InvocationRequest request) {
		Utils.logCall(TAG, "executeService");
		try {
			return SPF.get().getServiceRegistry().dispatchInvocation(request);
		} catch (Throwable t) {
			return InvocationResponse.error("remote exception : " + t.toString());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.spf.framework.proximity.ProximityResponder#sendContactRequest
	 * (it.polimi.spf.framework.security.FriendshipMessage)
	 */
	@Override
	public void sendContactRequest(ContactRequest request) {
		Utils.logCall(TAG, "sendContactRequest");
		mSpf.getSecurityMonitor().getPersonRegistry().onFriendShipMessageReceived(request);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.spf.framework.proximity.ProximityResponder#sendNotification
	 * (java.lang.String, java.lang.String)
	 */
	@Override
	public void sendNotification(String uniqueIdentifier, SPFActionSendNotification action) {
		Utils.logCall(TAG, "sendNotification", uniqueIdentifier, action);
		NotificationMessage message = new NotificationMessage(uniqueIdentifier, action.getTitle(), action.getMessage());
		mSpf.getNotificationManager().onNotificationMessageReceived(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.spf.framework.proximity.InboundProximityInterface#sendActivity
	 * (java.lang.String)
	 */
	@Override
	public InvocationResponse sendActivity(SPFActivity activity) {
		Utils.logCall(TAG, "sendActivity", activity);
		InvocationResponse response;
		try {
			response = SPF.get().getServiceRegistry().sendActivity(activity);
		} catch (Throwable t) {
			response = InvocationResponse.error("remote exception : " + t.getMessage());
		}
		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.spf.framework.proximity.ProximityResponder#
	 * onSearchSignalReceived(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean onSearchSignalReceived(String queryId, String queryJson) {
		Utils.logCall(TAG, "onSearchSignalReceived", queryId, queryJson);

		if (new SearchResponder(mSpf.getContext()).matches(queryJson)) {
			// XXX #SearchRefactor
			// - Merge with trigger query responder
			// - Use the right persona to fetch baseinfo (app name can be found in query json)
			// - Move everything in search maager and search performer classes

			BaseInfo info = mSpf.getProfileManager().getBaseInfo(SPFPersona.getDefault());
			mSpf.sendSearchResult(queryId, info);
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.spf.framework.proximity.ProximityResponder#
	 * onSearchResultReceived(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void onSearchResultReceived(String searchId, String uniqueIdentifier, String baseInfo) {
		Utils.logCall(TAG, "onSearchResultReceived", searchId, uniqueIdentifier, baseInfo);
		BaseInfo info = new Gson().fromJson(baseInfo, BaseInfo.class);
		mSpf.getSearchManager().onSearchResultReceived(new SearchResult(searchId, uniqueIdentifier, info));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.spf.framework.proximity.ProximityResponder#
	 * onAdvertisingSignalReceived(java.lang.String)
	 */
	@Override
	public void onAdvertisingSignalReceived(String profileInfo) {
		Utils.logCall(TAG, "onAdvertisingSignalReceived", profileInfo);

		SPFAdvProfile advProfile = SPFAdvProfile.fromJSON(profileInfo);
		mSpf.getNotificationManager().onAdvertisementReceived(advProfile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.spf.framework.proximity.ProximityResponder#
	 * onRemoteInstanceFound
	 * (it.polimi.spf.framework.interfaces.SPFRemoteInstance)
	 */
	@Override
	public void onRemoteInstanceFound(SPFRemoteInstance instance) {
		Utils.logCall(TAG, "onRemoteInstanceFound", instance);

		mSpf.getPeopleManager().newPerson(instance);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.spf.framework.proximity.ProximityResponder#onRemoteInstanceLost
	 * (java.lang.String)
	 */
	@Override
	public void onRemoteInstanceLost(String uniqueIdentifier) {
		Utils.logCall(TAG, "OnRemoteInstanceLost", uniqueIdentifier);

		mSpf.getPeopleManager().removePerson(uniqueIdentifier);
		mSpf.getSearchManager().onInstanceLost(uniqueIdentifier);
	}
}
