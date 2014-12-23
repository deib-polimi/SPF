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
package it.polimi.spf.alljoyn;

import it.polimi.spf.framework.proximity.InboundProximityInterface;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.SPFActivity;

import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;

/**
 * Inbound proximity interface exposed by {@link AlljoynProximityMiddleware} on
 * the Alljoyn bus to allow the reception of requests from remote interfaces.
 * The actual implementation, {@link AlljoynProximityInterfaceImpl} acts as an
 * adapter that forwards the request to a instance of
 * {@link InboundProximityInterface} created by SPF.
 */

@BusInterface(name = "it.polimi.spf.alljoyn.proximityinterface")
/*package*/ interface AlljoynProximityInterface {

	/**
	 * Dispatch an invocation request for a service registered on SPF
	 * 
	 * @see {@link InboundProximityInterface#executeService(String)}
	 * @param marshalledRequest
	 *            - the json representation of an {@link InvocationRequest}
	 * @return - the json representation of an {@link InvocationResponse}
	 */
	@BusMethod
	public String executeService(String marshalledRequest);

	/**
	 * Retrieves the values of a collection of {@link ProfileField} from the
	 * profile
	 * 
	 * @param token
	 * @param appIdentifier
	 * @param fieldList
	 * @return
	 */
	@BusMethod
	public String getProfileBulk(String token, String appIdentifier, String fieldList);

	/**
	 * 
	 * @param friendshipMessageJSON
	 */
	@BusMethod
	public void sendFriendshipMessage(String friendshipMessageJSON);

	/**
	 * 
	 * @param uniqueIdentifier
	 * @param actionSendNotification
	 */
	@BusMethod
	public void sendNotification(String uniqueIdentifier, String actionSendNotification);

	/**
	 * Sends a marshalled {@link SPFActivity} to this instane.
	 * 
	 * @param activity
	 *            - the marshalled activity
	 */
	@BusMethod
	public String sendActivity(String activity);

}
