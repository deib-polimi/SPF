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
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.SPFContext;
import it.polimi.spf.framework.Utils;
import it.polimi.spf.framework.profile.SPFPersona;
import it.polimi.spf.framework.security.AppRegistrationHandler;
import it.polimi.spf.framework.security.TokenNotValidException;
import it.polimi.spf.shared.aidl.SPFAppRegistrationCallback;
import it.polimi.spf.shared.aidl.SPFSecurityService;
import it.polimi.spf.shared.model.AppDescriptor;

/**
 * 
 * @author darioarchetti
 * 
 */
public class SPFSecurityServiceImpl extends SPFSecurityService.Stub {

	private static final String TAG = "SPFSecurityServiceImpl";
	private Context mContext;

	public SPFSecurityServiceImpl(Context context) {
		this.mContext = context;
	}

	@Override
	public void registerApp(final AppDescriptor descriptor, final SPFAppRegistrationCallback callback) throws RemoteException {
		Utils.logCall(TAG, "registerApp", descriptor, callback);

		new Handler(Looper.getMainLooper()).post(new Runnable() {

			@Override
			public void run() {
				SPFContext.get().getAppRegistrationHandler().handleRegistrationRequest(mContext, descriptor, new AppRegistrationHandler.Callback() {

					@Override
					public void onRequestRefused() {
						try {
							callback.onRegistrationFailure();
						} catch (RemoteException e) {
							Log.e(TAG, "Remote exception invoking callback method", e);
						}
					}

					@Override
					public void onRequestAccepted(SPFPersona persona) {
						if (persona == null) {
							throw new NullPointerException();
						}

						String token = SPF.get().getSecurityMonitor().registerApplication(descriptor, persona);
						try {
							callback.onRegistrationSuccess(token);
						} catch (RemoteException e) {
							Log.e(TAG, "Remote exception invoking callback method", e);
						}
					}
				});
			}
		});
	}

	@Override
	public void unregisterApp(String accessToken) throws RemoteException {
		Utils.logCall(TAG, "unregisterApp", accessToken);

		String appIdentifier;
		try {
			appIdentifier = SPF.get().getSecurityMonitor().getAppAuthorization(accessToken).getAppIdentifier();
		} catch (TokenNotValidException e) {
			throw new RemoteException("ERR_INVALID_TOKEN");
		}

		SPF spf = SPF.get();
		spf.getSecurityMonitor().unregisterApplication(appIdentifier);
		spf.getServiceRegistry().unregisterAllServicesOfApp(appIdentifier);
	}
}