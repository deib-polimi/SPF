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

import it.polimi.spf.shared.aidl.ClientExecutionService;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFActivity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/*package*/class AppServiceProxy implements ServiceConnection, ClientExecutionService {

	private final static String TAG = "AppProxy";

	/*
	 * The remote binder to the external service
	 */
	private ClientExecutionService mAppService;

	/*
	 * The component name of the external service.
	 */
	private final String mComponentName;

	public AppServiceProxy(String componentName) {
		mComponentName = componentName;
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		synchronized (this) {
			mAppService = ClientExecutionService.Stub.asInterface(service);
			notifyAll();
			Log.d(getTag(), "Connected to app service");
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		synchronized (this) {
			mAppService = null;
			Log.d(getTag(), "Disconnected from app service");
		}
	}

	public boolean isConnected() {
		return mAppService != null;
	}

	@Override
	public InvocationResponse executeService(InvocationRequest request) {
		try {
			return getAppService().executeService(request);
		} catch (RemoteException e) {
			Log.e(getTag(), "Remote exception @ executeService", e);
			return InvocationResponse.error(e.getCause());
		}
	}

	@Override
	public InvocationResponse sendActivity(SPFActivity activity) {
		try {
			return getAppService().sendActivity(activity);
		} catch (RemoteException e) {
			Log.e(getTag(), "Remmote exception @ sendActivity", e);
			return InvocationResponse.error(e.getCause());
		}
	}

	private ClientExecutionService getAppService() {
		synchronized (this) {
			while (mAppService == null) {
				try {
					wait();
				} catch (InterruptedException e) {
					Log.e(getTag(), "InterruptedException", e);
				}
			}
			return mAppService;
		}

	}

	private String getTag() {
		return TAG + "#" + mComponentName;
	}

	@Override
	public IBinder asBinder() {
		throw new RuntimeException("Do not call asBinder on proxy");
	}
}
