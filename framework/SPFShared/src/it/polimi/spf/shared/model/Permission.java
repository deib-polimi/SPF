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

/**
 * Describes a capability of an external application to make use of a service of
 * SPF.
 * 
 * @author darioarchetti
 * 
 */
public enum Permission {

	/**
	 * Allows an application to register services in SPF
	 */
	REGISTER_SERVICES(0x1),

	/**
	 * Allows an application to leverage on the Search API
	 */
	SEARCH_SERVICE(0x2),

	/**
	 * Allows an application to execute services of local applications
	 */
	EXECUTE_LOCAL_SERVICES(0x4),

	/**
	 * Allows an application to execute services of remote applications
	 */
	EXECUTE_REMOTE_SERVICES(0x8),

	/**
	 * Allows an application to read information from the local profile
	 */
	READ_LOCAL_PROFILE(0x10),

	/**
	 * Allows an application to read information from a remote profile
	 */
	READ_REMOTE_PROFILES(0x20),

	/**
	 * Allows an application to modify the information contained in the local
	 * profile
	 */
	WRITE_LOCAL_PROFILE(0x40),

	/**
	 * Allows an application to make use of the SPF Notification API
	 */
	NOTIFICATION_SERVICES(0x80),

	/**
	 * Allows an application to dispatch {@link SPFActivity} to other
	 * applications
	 */
	ACTIVITY_SERVICE(0x100);

	private int mCode;

	private Permission(int code) {
		this.mCode = code;
	}

	/**
	 * @return the code identifying this permission
	 */
	public int getCode() {
		return mCode;
	}

}
