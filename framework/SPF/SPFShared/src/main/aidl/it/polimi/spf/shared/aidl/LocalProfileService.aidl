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

import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.SPFError;

/**
 * Interface exposed by SPF to allow local applications to manage the user
 * profile, allowing to read and write values in bulk. To access methods of this
 * interface, applications need to be granted
 * {@link Permission#READ_LOCAL_PROFILE} and/or
 * {@link Permission#WRITE_LOCAL_PROFILE}
 */
interface LocalProfileService {
	
	/**
	 * Reads a bulk of values from the profile of the local user. To
	 * perform this call, the local application needs to be granted
	 * {@link Permission#READ_LOCAL_PROFILE}
	 * 
	 * @param accessToken
	 *            - the access token granted by SPF to the local
	 *            application
	 * @param profileFieldIdentifiers
	 *            - the identifiers of the fields to retrieve
	 * @param err
	 *            - the container for errors that may occur during the
	 *            execution of the call
	 * @return a {@link ProfileFieldContainer} with the values of the
	 *         fields retrieved from the user profile
	 */
	ProfileFieldContainer getValueBulk(String accessToken, in String[] profileFieldIdentifiers, out SPFError err);

	/**
	 * Writes a bulk of values to the local profile. To perform this
	 * call, the local application needs to be granted
	 * {@link Permission#WRITE_LOCAL_PROFILE}
	 * 
	 * @param accessToken
	 *            - the accessToken granted by SPF to the local
	 *            application
	 * @param container
	 *            - the {@link ProfileFieldContainer} with the
	 *            containing the modified values to write
	 * @param err
	 *            - the container for errors that may occur during the
	 *            execution of the call
	 */
	void setValueBulk(String accessToken, in ProfileFieldContainer container, out SPFError err);

}