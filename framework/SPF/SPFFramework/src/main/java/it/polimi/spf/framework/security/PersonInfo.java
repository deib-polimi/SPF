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

public class PersonInfo {

	private final String mDisplayName;
	private PersonAuth mAuth;
	private boolean mHasPicture;

	public PersonInfo(PersonAuth auth, String displayName, boolean hasPicture) {
		if (auth == null) {
			throw new NullPointerException("auth cannot be null");
		}

		this.mAuth = auth;
		this.mDisplayName = displayName;
		this.mHasPicture = hasPicture;
	}

	public String getIdentifier() {
		return mAuth.getUserIdentifier();
	}

	public String getDisplayName() {
		return mDisplayName != null ? mDisplayName : mAuth.getUserIdentifier();
	}

	public PersonAuth getPersonAuth() {
		return mAuth;
	}

	public boolean hasProfilePic() {
		return mHasPicture;
	}
}
