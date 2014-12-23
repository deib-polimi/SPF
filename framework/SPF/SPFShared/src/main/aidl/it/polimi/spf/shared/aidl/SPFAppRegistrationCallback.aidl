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

package it.polimi.spf.shared.aidl;

/**
 * Callback interface that allows SPF to notify the local application of the
 * outcome of the registration process involving the user.
 * 
 * @see #onRegistrationSuccess(String)
 * @see #onRegistrationFailure()
 * @author darioarchetti
 * 
 */
interface SPFAppRegistrationCallback {

	/**
	 * Called by SPF if the user allows the local application to interact with
	 * SPF. The Access Token, needed to call the API of SPF, is passed here as a
	 * parameter.
	 * 
	 * @param accessToken
	 *            - the accessToken.
	 */
	void onRegistrationSuccess(String accessToken);
	
	/**
	 * Called when the user prevents the local application from interacting with
	 * SPF.
	 */
	void onRegistrationFailure();
}