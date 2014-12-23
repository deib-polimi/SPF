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
package it.polimi.spf.lib;

import it.polimi.spf.lib.profile.SPFRemoteProfile.RemoteProfile;
import it.polimi.spf.lib.profile.SPFRemoteProfile;
import it.polimi.spf.lib.services.SPFServiceExecutor;
import it.polimi.spf.shared.model.BaseInfo;
import it.polimi.spf.shared.model.SPFActivity;

/**
 * Instances of the {@code Person} class represent people found in the proximity
 * of the device with whom the interaction is possible.
 * 
 */
public class SPFPerson {

	public static final String SELF_IDENTIFIER = "#SELF";
	private String mIdentifier;
	private BaseInfo mBaseInfo;

	// TODO @hide
	public SPFPerson(String uniqueIdentifier) {
		this(uniqueIdentifier, null);
	}

	// TODO @hide
	public SPFPerson(String uniqueIdentifier, BaseInfo baseInfo) {
		this.mIdentifier = uniqueIdentifier != null ? uniqueIdentifier : SELF_IDENTIFIER;
		this.mBaseInfo = baseInfo;
	}

	/**
	 * Returns the identifier of the person. If the person represents the user
	 * himself, the methods returns the value contained in the constant
	 * SELF_IDENTIFIER.
	 * 
	 * @return the identifier of the person
	 */
	public String getIdentifier() {
		return mIdentifier;
	}

	/**
	 * Returns the basic information of the person. If it is unavailable, an
	 * empty string is retrieved.
	 * 
	 * @return the basic information about the person
	 */
	public BaseInfo getBaseInfo() {
		return mBaseInfo;
	}

	/**
	 * @return true if the person is self
	 */
	public boolean isSelf() {
		return mIdentifier.equals(SELF_IDENTIFIER);
	}

	@Override
	public String toString() {
		return getIdentifier();
	}

	/**
	 * Shorthand method to retrieve the RemoteProfile of {@code this} person.
	 * 
	 * @param spf
	 * @return
	 */
	public RemoteProfile getProfile(SPF spf) {
		SPFRemoteProfile rp = (SPFRemoteProfile) spf.getComponent(SPF.REMOTE_PROFILE);
		return rp.getProfileOf(this);
	}

	/**
	 * Shorthand method to create an invocation interface for {@code this} person.
	 * 
	 * @param serviceInterface
	 * @param spf
	 * @return
	 */
	public <E> E getServiceInterface(Class<E> serviceInterface, SPF spf) {
		if (spf == null || serviceInterface == null) {
			throw new NullPointerException();
		}

		return spf
				.<SPFServiceExecutor> getComponent(SPF.SERVICE_EXECUTION)
				.createStub(this, serviceInterface, getClass().getClassLoader());
	}

	public boolean sendActivity(SPF spf, SPFActivity activity) {
		SPFServiceExecutor executor = spf.getComponent(SPF.SERVICE_EXECUTION);
		return executor.sendActivity(mIdentifier, activity);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof SPFPerson)) {
			return false;
		}

		return ((SPFPerson) o).mIdentifier.equals(mIdentifier);
	}

	@Override
	public int hashCode() {
		return mIdentifier.hashCode();
	}
}
