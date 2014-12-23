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
package it.polimi.spf.framework.local;

import android.content.Context;
import android.os.RemoteException;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.Utils;
import it.polimi.spf.framework.profile.SPFProfileManager;
import it.polimi.spf.framework.security.AppAuth;
import it.polimi.spf.framework.security.PermissionDeniedException;
import it.polimi.spf.framework.security.SPFSecurityMonitor;
import it.polimi.spf.framework.security.TokenNotValidException;
import it.polimi.spf.shared.aidl.LocalProfileService;
import it.polimi.spf.shared.model.Permission;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.SPFError;

/**
 * @author aliprax
 * 
 */
public class SPFProfileServiceImpl extends LocalProfileService.Stub {
	private static final String TAG = "LocalProfileService";
	private final SPFSecurityMonitor mSecurityMonitor;
	private final SPFProfileManager mProfile;

	/**
	 * 
	 */
	public SPFProfileServiceImpl(Context ctx) {
		SPF o = SPF.get();
		mProfile = o.getProfileManager();
		mSecurityMonitor = o.getSecurityMonitor();
	}

	@Override
	public ProfileFieldContainer getValueBulk(String accessToken, String[] profileFieldIdentifiers, SPFError err) throws RemoteException {
		Utils.logCall(TAG, "getValueBulk", accessToken, profileFieldIdentifiers, err);
		
		AppAuth appAuth;
		try {
			appAuth = mSecurityMonitor.validateAccess(accessToken, Permission.READ_LOCAL_PROFILE);
			return mProfile.getProfileFieldBulk(profileFieldIdentifiers, appAuth.getPersona());
		} catch (TokenNotValidException e) {
			err.setCode(SPFError.TOKEN_NOT_VALID_ERROR_CODE);
			return null;
		} catch (PermissionDeniedException e) {
			err.setCode(SPFError.PERMISSION_DENIED_ERROR_CODE);
			return null;
		}
	}

	@Override
	public void setValueBulk(String accessToken, ProfileFieldContainer container, SPFError err) throws RemoteException {
		Utils.logCall(TAG, "setValueBulk", accessToken, container, err);
		
		AppAuth appAuth;
		try {
			appAuth = mSecurityMonitor.validateAccess(accessToken, Permission.WRITE_LOCAL_PROFILE);
			mProfile.setProfileFieldBulk(container, appAuth.getPersona());
		} catch (TokenNotValidException e) {
			err.setCode(SPFError.TOKEN_NOT_VALID_ERROR_CODE);
		} catch (PermissionDeniedException e) {
			err.setCode(SPFError.PERMISSION_DENIED_ERROR_CODE);
		}
	}

}