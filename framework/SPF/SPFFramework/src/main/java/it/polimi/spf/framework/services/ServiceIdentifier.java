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
package it.polimi.spf.framework.services;

/**
 * The identifier for a SPF service, made up of the identifier of the owner app, and
 * its name
 * 
 * @author darioarchetti
 * 
 * TODO Should this class be used globally?
 * 
 */
public class ServiceIdentifier {

	private final String mAppId;
	private final String mServiceName;

	public ServiceIdentifier(String appId, String serviceName) {
		this.mAppId = appId;
		this.mServiceName = serviceName;
	}

	public String getAppId() {
		return mAppId;
	}

	public String getServiceName() {
		return mServiceName;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof ServiceIdentifier)){
			return false;
		}
		
		ServiceIdentifier other = (ServiceIdentifier) o;
		return other.mAppId.equals(mAppId) &&
				other.mServiceName.equals(mServiceName);
	}
	
	public int hashCode() {
		return (mAppId + mServiceName).hashCode();
	};
}
