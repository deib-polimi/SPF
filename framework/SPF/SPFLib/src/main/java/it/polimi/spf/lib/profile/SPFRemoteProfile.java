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
package it.polimi.spf.lib.profile;

import android.content.Context;
import it.polimi.spf.lib.SPF;
import it.polimi.spf.lib.SPFPerson;
import it.polimi.spf.shared.model.ProfileField;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.SPFError;

/**
 * A {@link SPFComponent} that provides access to profiles of remote people to
 * allow read operation.
 * 
 * @author darioarchetti
 * 
 */
public class SPFRemoteProfile {

	private SPF mInterface;

	public SPFRemoteProfile(Context context, SPF iface) {
		this.mInterface = iface;
	}

	/**
	 * Provides a reference to a remote profile. With this reference it is
	 * possible to perform read operations and retrieve information from remote
	 * people.
	 * 
	 * @param p
	 *            - the {@link SPFPerson} whose profile to load.
	 * @return an instance of {@link RemoteProfile} to interact with the remote
	 *         profile.
	 */
	public RemoteProfile getProfileOf(SPFPerson p) {
		if (p == null) {
			throw new NullPointerException();
		}
		
		return new RemoteProfile(p, mInterface);
	}

	
	protected void recycle() {
		mInterface = null;
	}
	
	/**
	 * Reference to the profile of a remote person that allows read operations to
	 * obtain information.
	 * 
	 * @author darioarchetti
	 * 
	 */
	public class RemoteProfile {

		private SPF mInterface;
		private String mPersonIndentifier;

		/* package */public RemoteProfile(SPFPerson p, SPF iface) {
			this.mInterface = iface;
			this.mPersonIndentifier = p.getIdentifier();
		}


		/**
		 * Obtains a bulk of profile fields from a remote profile. The bulk is
		 * retrieved as an instance of {@link ProfileFieldContainer} from which the
		 * single field can be retrieved.
		 * 
		 * @param fields
		 *            - the list of {@link ProfileField} to retrieve from the remote
		 *            profile
		 * @return a container that holds the values of requested fields
		 */
		public ProfileFieldContainer getProfileBulk(ProfileField<?>... fields) {
			String[] fieldIdentifiers = new String[fields.length];
			for (int i = 0; i < fields.length; i++) {
				fieldIdentifiers[i] = fields[i].getIdentifier();
			}
			SPFError err =  new SPFError();
			return mInterface.getProfileBulk(mPersonIndentifier, fieldIdentifiers, err);
		}
	}

}
