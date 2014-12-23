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

import java.util.HashSet;
import java.util.Set;

/**
 * Lists the services capable of support a given verb, together with the default
 * service. A list of <code>VerbSupport</code> for each verb can be obtained
 * with {@link SPFServiceRegistry#getVerbSupportList()}
 * 
 * @author darioarchetti
 * 
 */
public class VerbSupport {

	private final String mVerb;
	private Set<ServiceIdentifier> mSupportingServices;
	private ServiceIdentifier mDefaultService;

	public VerbSupport(String verb) {
		mSupportingServices = new HashSet<ServiceIdentifier>();
		mVerb = verb;
	}

	/**
	 * @return the verb supported by listed services.
	 */
	public String getVerb() {
		return mVerb;
	}

	public Set<ServiceIdentifier> getSupportingServices() {
		return mSupportingServices;
	}

	/**
	 * @return the default service for the verb of <code>this</code> instance.
	 */
	public ServiceIdentifier getDefaultService() {
		return mDefaultService;
	}

	/* package */void addSupportingService(ServiceIdentifier identifier) {
		mSupportingServices.add(identifier);
	}

	/* package */void setDefaultApp(ServiceIdentifier service) {
		this.mDefaultService = service;
	}
}
