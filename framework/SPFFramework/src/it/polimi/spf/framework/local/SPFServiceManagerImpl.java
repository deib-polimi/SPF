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

import android.os.RemoteException;
import android.util.Log;
import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.Utils;
import it.polimi.spf.framework.security.PermissionDeniedException;
import it.polimi.spf.framework.security.SPFSecurityMonitor;
import it.polimi.spf.framework.security.TokenNotValidException;
import it.polimi.spf.framework.services.ActivityInjector;
import it.polimi.spf.shared.aidl.LocalServiceManager;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.Permission;
import it.polimi.spf.shared.model.SPFActivity;
import it.polimi.spf.shared.model.SPFError;
import it.polimi.spf.shared.model.SPFServiceDescriptor;

/**
 * Component that allows the registration and the execution of local services. A
 * registered service is made available to other applications, both on the local
 * device and on remote ones, which can invoke methods contained in the service.
 * 
 * @author darioarchetti
 * 
 */
/* package */class SPFServiceManagerImpl extends LocalServiceManager.Stub {
	private static final String TAG = "LocalServiceManager";
	private SPFSecurityMonitor mSecurityMonitor = SPF.get().getSecurityMonitor();

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.polimi.spf.framework.local.LocalServiceManager#registerService
	 * (java.lang.String, it.polimi.spf.framework.local.ServiceDescriptor)
	 */
	@Override
	public boolean registerService(String accessToken, SPFServiceDescriptor descriptor, SPFError error) throws RemoteException {
		Utils.logCall(TAG, "registerService", accessToken, descriptor, error);

		try {
			mSecurityMonitor.validateAccess(accessToken, Permission.REGISTER_SERVICES);
		} catch (TokenNotValidException e) {
			error.setCode(SPFError.TOKEN_NOT_VALID_ERROR_CODE);
			return false;
		} catch (PermissionDeniedException e) {
			error.setError(SPFError.PERMISSION_DENIED_ERROR_CODE, "Missing permission: " + Permission.REGISTER_SERVICES);
			return false;
		}

		return SPF.get().getServiceRegistry().registerService(descriptor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.spf.framework.local.LocalServiceManager#executeLocalService
	 * (java.lang.String, it.polimi.spf.framework.local.InvocationRequest)
	 */
	@Override
	public InvocationResponse executeLocalService(String accessToken, InvocationRequest request, SPFError error) throws RemoteException {
		Utils.logCall(TAG, "executeLocalService", accessToken, request, error);

		try {
			mSecurityMonitor.validateAccess(accessToken, Permission.EXECUTE_LOCAL_SERVICES);
		} catch (TokenNotValidException e) {
			error.setCode(SPFError.TOKEN_NOT_VALID_ERROR_CODE);
			return null;
		} catch (PermissionDeniedException e) {
			error.setError(SPFError.PERMISSION_DENIED_ERROR_CODE, "Missing capability: " + Permission.EXECUTE_LOCAL_SERVICES);
			return null;
		}

		try {
			return SPF.get().getServiceRegistry().dispatchInvocation(request);
		} catch (Throwable t) {
			error.setError(SPFError.REMOTE_EXC_ERROR_CODE, t.getClass().getSimpleName());
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.polimi.spf.shared.aidl.LocalServiceManager#unregisterService(java.
	 * lang.String, it.polimi.spf.shared.model.SPFServiceDescriptor,
	 * it.polimi.spf.shared.model.SPFError)
	 */
	@Override
	public boolean unregisterService(String accessToken, SPFServiceDescriptor descriptor, SPFError error) throws RemoteException {
		Utils.logCall(TAG, "unregisterService", accessToken, descriptor, error);

		// try {
		// mSecurityMonitor.validateAccess(accessToken,
		// Permission.REGISTER_SERVICES);
		// } catch (TokenNotValidException e) {
		// error.setCode(SPFError.TOKEN_NOT_VALID_ERROR_CODE);
		// return false;
		// } catch (PermissionDeniedException e) {
		// error.setError(SPFError.PERMISSION_DENIED_ERROR_CODE,
		// "Missing capability: " + Permission.EXECUTE_LOCAL_SERVICES);
		// return false;
		// }
		//
		// // TODO check that the application removing the service is the one
		// that
		// // registered it
		// return SPF.get().getServiceRegistry().unregisterService(descriptor);

		Log.w(TAG, "Services cannot be unregistered by apps due to ActivityStream");
		return false;
	}

	@Override
	public void injectInformationIntoActivity(String token, SPFActivity activity, SPFError err) throws RemoteException {
		Utils.logCall(TAG, "injectInformationIntoActivity", token, activity, err);

		try {
			mSecurityMonitor.validateAccess(token, Permission.ACTIVITY_SERVICE);
		} catch (TokenNotValidException e) {
			err.setCode(SPFError.TOKEN_NOT_VALID_ERROR_CODE);
			return;
		} catch (PermissionDeniedException e) {
			err.setCode(SPFError.PERMISSION_DENIED_ERROR_CODE);
			return;
		}

		ActivityInjector.injectDataInActivity(activity);
	}

	@Override
	public InvocationResponse sendActivityLocally(String accessToken, SPFActivity activity, SPFError err) throws RemoteException {
		Utils.logCall(TAG, "sendActivity", accessToken, activity, err);

		try {
			mSecurityMonitor.validateAccess(accessToken, Permission.ACTIVITY_SERVICE);
		} catch (TokenNotValidException e) {
			err.setCode(SPFError.TOKEN_NOT_VALID_ERROR_CODE);
			return null;
		} catch (PermissionDeniedException e) {
			err.setCode(SPFError.PERMISSION_DENIED_ERROR_CODE);
			return null;
		}

		return SPF.get().getServiceRegistry().sendActivity(activity);
	}

}
