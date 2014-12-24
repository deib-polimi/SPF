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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.Utils;
import it.polimi.spf.framework.notification.SPFNotificationManager;
import it.polimi.spf.framework.security.AppAuth;
import it.polimi.spf.framework.security.TokenNotValidException;
import it.polimi.spf.framework.security.PermissionDeniedException;
import it.polimi.spf.framework.security.SPFSecurityMonitor;
import it.polimi.spf.shared.aidl.SPFNotificationService;
import it.polimi.spf.shared.model.Permission;
import it.polimi.spf.shared.model.SPFError;
import it.polimi.spf.shared.model.SPFTrigger;

/**
 * @author Jacopo Aliprandi
 * 
 *         Implementation of the binder stub for the SPFNotification services.
 *         The AIDL interface is located at
 *         it.polimi.spf.framework.local.SPFNotificationService.aidl
 * 
 */
/* package */class SPFNotificationServiceImpl extends SPFNotificationService.Stub {

	private static final String TAG = "SPFNotificationService";
	private SPFNotificationManager mNotificationManager;
	private SPFSecurityMonitor mSecurityMonitor;

	/**
	 * Constructor for {@link SPFNotificationServiceImpl}.
	 * 
	 * @param ctx
	 *            - the context of the calling component
	 */
	public SPFNotificationServiceImpl(Context ctx) {
		mSecurityMonitor = SPF.get().getSecurityMonitor();
		// do not ask for SPFnotification instance during the interprocess
		// binding
	}

	/*
	 * Returns an instance of SPFNotificationManager or creates it, if it is the
	 * first time that the stub is accessed.
	 */
	private SPFNotificationManager getSPFNotificationManager() {
		if (mNotificationManager == null) {
			mNotificationManager = SPF.get().getNotificationManager();
		}
		return mNotificationManager;
	}

	private AppAuth getAppAuth(String token, SPFError err) throws TokenNotValidException, PermissionDeniedException {
		try {
			return mSecurityMonitor.validateAccess(token, Permission.NOTIFICATION_SERVICES);
		} catch (TokenNotValidException e) {
			err.setCode(e.getSPFErrorCode());
			throw e;
		} catch (PermissionDeniedException e) {
			err.setCode(e.getSPFErrorCode());
			throw e;
		}
	}

	@Override
	public long saveTrigger(SPFTrigger trigger, String token, SPFError err) {
		Utils.logCall(TAG, "saveTrigger", trigger, token, err);

		AppAuth appAuth;
		try {
			appAuth = getAppAuth(token, err);
		} catch (Exception e) {
			return -1;
		}
		return getSPFNotificationManager().saveTrigger(trigger, appAuth.getAppIdentifier());
	}

	@Override
	public boolean deleteTrigger(long triggerId, String token, SPFError err) {
		Utils.logCall(TAG, "deleteTrigger", triggerId, token, err);

		AppAuth appAuth;
		try {
			appAuth = getAppAuth(token, err);
		} catch (Exception e) {
			return false;
		}
		return getSPFNotificationManager().deleteTrigger(triggerId, appAuth.getAppIdentifier());
	}

	@Override
	public boolean deleteAllTrigger(String token, SPFError err) {
		Utils.logCall(TAG, "deleteAllTrigger", token, err);

		AppAuth appAuth;
		try {
			appAuth = getAppAuth(token, err);
		} catch (Exception e) {
			return false;
		}
		return getSPFNotificationManager().deleteAllTrigger(appAuth.getAppIdentifier());
	}

	@Override
	public List<SPFTrigger> listTrigger(String token, SPFError err) {
		Utils.logCall(TAG, "listTrigger", token, err);

		AppAuth appAuth;
		try {
			appAuth = getAppAuth(token, err);
		} catch (Exception e) {
			return new ArrayList<SPFTrigger>(0);
		}
		return getSPFNotificationManager().listTriggers(appAuth.getAppIdentifier());
	}

	@Override
	public SPFTrigger getTrigger(long triggerId, String token, SPFError err) {
		Utils.logCall(TAG, "saveTrigger", triggerId, token, err);

		AppAuth appAuth;
		try {
			appAuth = getAppAuth(token, err);
		} catch (Exception e) {
			return null;
		}
		return getSPFNotificationManager().getTrigger(triggerId, appAuth.getAppIdentifier());
	}

}
