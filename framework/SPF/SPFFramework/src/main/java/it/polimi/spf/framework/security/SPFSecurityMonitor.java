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

import java.util.List;

import android.content.Context;
import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.shared.model.AppDescriptor;
import it.polimi.spf.shared.model.Permission;

/**
 * @author darioarchetti
 * 
 */
public class SPFSecurityMonitor {

	private final ApplicationRegistry mAppRegistry;
	private final PersonRegistry mPersonRegistry;

	public SPFSecurityMonitor(Context context) {
		this.mAppRegistry = new ApplicationRegistry(context);
		this.mPersonRegistry = new PersonRegistry(context);
	}

	public AppAuth getAppAuthorization(String accessToken) throws TokenNotValidException {
		return mAppRegistry.getAppAuthorization(accessToken);
	}

	// Local app security
	public AppAuth validateAccess(String accessToken, Permission permission) throws TokenNotValidException, PermissionDeniedException {
		AppAuth appAuth;
		if (accessToken == null) {
			throw new TokenNotValidException();
		}

		appAuth = mAppRegistry.getAppAuthorization(accessToken);

		if ((appAuth.getPermissionCode() & permission.getCode()) == 0) {
			throw new PermissionDeniedException();
		}

		return appAuth;
	}

	public boolean isAppRegistered(String appIdentifier) {
		return mAppRegistry.isAppRegistered(appIdentifier);
	}

	public String registerApplication(AppDescriptor descriptor, SPFPersona persona) {
		return mAppRegistry.registerApplication(descriptor, persona);
	}

	public boolean unregisterApplication(String appIdentifier) {
		return mAppRegistry.unregisterApplication(appIdentifier);
	}

	public List<AppAuth> getAvailableApplications() {
		return mAppRegistry.getAvailableApplications();
	}

	/**
	 * Returns the SPFPersona associated to the specified application. If the
	 * application does not exist returns the default SPFPersona.
	 * 
	 * @param appIdentifier
	 *            - the identifier of the application
	 * @return a SPFPersona
	 */
	public SPFPersona getPersonaOf(String appIdentifier) {
		return mAppRegistry.getPersonaOf(appIdentifier);
	}

	// Remote instances security
	public PersonRegistry getPersonRegistry() {
		return mPersonRegistry;
	}

}
