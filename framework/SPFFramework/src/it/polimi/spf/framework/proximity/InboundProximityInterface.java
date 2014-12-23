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

import it.polimi.spf.framework.security.ContactRequest;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFActionSendNotification;
import it.polimi.spf.shared.model.SPFActivity;

/**
 * Interface for {@link ProximityMiddleware} that allows them to interact with
 * SPF
 * 
 * @author darioarchetti
 * 
 */
public interface InboundProximityInterface {

	// ACCESS //
	/**
	 * Retrieves a bulk of profile field values. The fields actually contained
	 * in the result may be less than those requested, as:
	 * <ul>
	 * <li>A field may not be available in the profile</li>
	 * <li>A field may not be visible to the sender of the request</li>
	 * <ul>
	 * 
	 * @param token
	 *            - the token of the person sending the request, null if no
	 *            token is available
	 * @param appIdentifier
	 *            - the identifier of the app used by the sender
	 * @param fields
	 *            - the fields whose value to retrieve
	 * @return a {@link ProfileFieldContainer} with the value of accessible
	 *         fields
	 */
	public String getProfileBulk(String token, String appIdentifier,
			String fieldList);

	/**
	 * Executes a service registered by an application on the local instance of
	 * SPF.
	 * 
	 * @param request
	 *            - an {@link InvocationRequest} describing the service to
	 *            invoke
	 * @return an {@link InvocationResponse} containing the result of the
	 *         execution.
	 */
	public InvocationResponse executeService(InvocationRequest request);

	/**
	 * Sends a contact request to the user of the local instance of SPF.
	 * 
	 * @param request
	 *            - a {@link ContactRequest} containing the details of the
	 *            sender
	 */
	public void sendContactRequest(ContactRequest requestJson);

	/**
	 * Sends an in-app notification to the user of the local instance of the
	 * SPF.
	 * 
	 * @param uniqueIdentifier
	 *            - the identifier of the sender
	 * @param actionSendNotification
	 *            -
	 */
	public void sendNotification(String uniqueIdentifier,
			SPFActionSendNotification actionSendNotification);

	/**
	 * Sends a marshalled SPFActivity to the local SPF instance that will take
	 * care of its dispatching. The result will be a marshalled
	 * {@link InvocationResponse} to return to the caller.
	 * 
	 * @param activity
	 *            - the marshalled activity
	 * @return the marshalled InvocationResponse
	 */
	public InvocationResponse sendActivity(SPFActivity activity);

	// SIGNALS //

	/**
	 * Signal handler to be called upon reception of a search signal
	 * 
	 * @param queryId
	 *            the id of the query being sent
	 * @param query
	 *            the query
	 * @return
	 */
	public boolean onSearchSignalReceived(String queryId, String query);

	/**
	 * Event listener to be called upon reception of a search result signal
	 * 
	 * @param searchId
	 *            the id of the search matched by the result
	 * @param uniqueIdentifier
	 *            the identifier of this spf instance
	 * @param baseInfo
	 *            basic information to be shown in search results
	 */
	public void onSearchResultReceived(String searchId,
			String uniqueIdentifier, String baseInfo);

	/**
	 * Event listener to be called when an andvertising signal is received
	 * 
	 * @param profileInfo
	 */
	public void onAdvertisingSignalReceived(String profileInfo);

	// EVENTS //
	/**
	 * Event listener to be called when a remote instance is found by the
	 * middleware
	 * 
	 * @param instance
	 *            the instance found by the middleware
	 */
	public void onRemoteInstanceFound(SPFRemoteInstance instance);

	/**
	 * Event listener to be called when a remote instance is lost by the
	 * middleware
	 * 
	 * @param uniqueIdentifier
	 *            the identifier of the lost instance
	 */
	public void onRemoteInstanceLost(String uniqueIdentifier);

}
