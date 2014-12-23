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

import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.security.ContactRequest;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.SPFActionSendNotification;
import it.polimi.spf.shared.model.SPFActivity;

/**
 * Remote instance of SPF found by the middleware. Offers method to interact
 * according to the SPF model. Each middleware defines its own subclass
 * according to its features.
 * 
 * @author darioarchetti
 * 
 */
public abstract class SPFRemoteInstance {

	/**
	 * Dispatches a {@link SPFActivity} to the remote instance
	 * 
	 * @param activity
	 *            - the activity to dispatch
	 * @return an {@link InvocationResponse} with the result of the invocation
	 */
	public abstract InvocationResponse sendActivity(SPFActivity activity);

	/**
	 * Retrieves the values of a defined set of {@link ProfileField} from the
	 * profile of the remote instance. The value of a field will be returned
	 * only if visible to the current instance.
	 * 
	 * @param fieldIdentifiers
	 *            - the list of identifiers of the profile fields to retrieve
	 * @param appIdentifier
	 *            - the identifier of the app that created the request.
	 * @return a {@link ProfileFieldContainer} with all the retrieved values.
	 */
	public final ProfileFieldContainer getProfileBulk(String[] fieldIdentifiers, String appIdentifier) {
		if (fieldIdentifiers == null || appIdentifier == null) {
			throw new NullPointerException();
		}

		String identifierList = FieldContainerMarshaller.marshallIdentifierList(fieldIdentifiers);
		String token = SPF.get().getSecurityMonitor().getPersonRegistry().getTokenFor(getUniqueIdentifier());

		String containerString = getProfileBulk(token, identifierList, appIdentifier);
		return FieldContainerMarshaller.unmarshallContainer(containerString);
	}

	/**
	 * Sends a contact request to the remote SPF instance.
	 * 
	 * @param request
	 *            - the {@link ContactRequest} to send.
	 */
	public abstract void sendContactRequest(ContactRequest request);

	/**
	 * Sends a notification to the remote SPF instance.
	 * 
	 * @param uniqueIdentifier
	 *            - the identifier of the sender.
	 * @param action
	 *            - the notification action.
	 */
	public final void sendNotification(String uniqueIdentifier, SPFActionSendNotification action) {
		if (uniqueIdentifier == null || action == null) {
			throw new NullPointerException();
		}

		String actionJSON = action.toJSON();
		sendNotification(uniqueIdentifier, actionJSON);
	}

	/**
	 * Provides the unique identifier of the remote SPF instance.
	 * 
	 * @return the unique identifier
	 */
	public abstract String getUniqueIdentifier();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return getClass().getSimpleName() + ":" + getUniqueIdentifier();
	}

	// Abstract method for subclasses to provide middleware-dependent
	// dispatching of messages for higher level methods.
	/**
	 * Dispatches an {@link InvocationRequest} to the remote instance of SPF.
	 * 
	 * @param request
	 *            - the request to dispatch
	 * @return - an {@link InvocationResponse} containing the result of the
	 *         invocation
	 */
	public abstract InvocationResponse executeService(InvocationRequest request);

	protected abstract String getProfileBulk(String token, String identifierList, String appIdentifier);

	protected abstract void sendNotification(String senderIdentifier, String action);

}
