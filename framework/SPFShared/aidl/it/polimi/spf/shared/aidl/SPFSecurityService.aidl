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

/**
 * Interface exposed by SPF that allows applications to register themselves and
 * obtain an access token to perform requests to other services of the
 * framework.
 * 
 * @see SPFAppRegistrationCallback
 */
package it.polimi.spf.shared.aidl;

import it.polimi.spf.shared.model.AppDescriptor;
import it.polimi.spf.shared.aidl.SPFAppRegistrationCallback;


interface SPFSecurityService {

	/**
	 * Performs a request for the registration of the local app in SPF.
	 * 
	 * @param descriptor
	 *            - the {@link AppDescriptor} of the local app
	 * @param callback
	 *            - the callback that will be used to notify of the outcome of
	 *            the registration procedure.
	 */
	void registerApp(in AppDescriptor descriptor, SPFAppRegistrationCallback callback);
	
	/**
	 * Unregisters the local app from SPF.
	 * 
	 * @param accessToken
	 *            - the token that was provided to the app by SPF upon
	 *            registration.
	 */
	void unregisterApp(String accessToken);
}