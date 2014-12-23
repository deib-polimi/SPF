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

import it.polimi.spf.shared.SPFInfo;
import it.polimi.spf.shared.aidl.SPFAppRegistrationCallback;
import it.polimi.spf.shared.aidl.SPFSecurityService;
import it.polimi.spf.shared.model.AppDescriptor;
import it.polimi.spf.shared.model.SPFError;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


class AccessTokenManager {

	private final static String TOKEN_FILE_NAME = "accesstoken";
	private final static String TAG = "AccessTokenManager";
	private static AccessTokenManager sSingleton;
	private String mToken;

	private volatile boolean isWaitingForResponse = false;
	private List<RegistrationCallback> mPendingRequests = new ArrayList<RegistrationCallback>();
	private Context mContext;

	public synchronized static AccessTokenManager get(Context context) {
		if (sSingleton == null) {
			File f = new File(context.getFilesDir(), TOKEN_FILE_NAME);
			String token = null;

			try {
				if (!f.exists()) {
					f.createNewFile();
				} else {
					BufferedReader reader = new BufferedReader(new FileReader(f));
					token = reader.readLine();
					reader.close();
				}
			} catch (IOException e) {
				Log.e(TAG, "IO Exception reading token file");
			}

			Log.d(TAG, token == null ? "Token not available" : "Token loaded from file");

			sSingleton = new AccessTokenManager(context, token);
		}

		return sSingleton;
	}

	private AccessTokenManager(Context context, String token) {
		this.mContext = context;
		this.mToken = token;
	}

	public String getAccessToken() {
		return mToken;
	}

	public boolean hasToken() {
		return mToken != null;
	}

	private void setToken(String token) {
		this.mToken = token;

		// Write to file
		File f = new File(mContext.getFilesDir(), TOKEN_FILE_NAME);
		if (token == null) {
			f.delete();
			Log.d(TAG, "Token deleted");
		} else {
			try {
				PrintWriter writer = new PrintWriter(new FileWriter(f, false));
				writer.println(token);
				writer.close();
			} catch (IOException e) {
				Log.e(TAG, "Error saving token to file " + TOKEN_FILE_NAME, e);
			}
			Log.d(TAG, "Token saved");
		}
	}

	public void requireAccessToken(final Context context, final RegistrationCallback callback) {
		synchronized (mPendingRequests) {
			if (hasToken()) {
				callback.onRegistrationSuccessful();
				return;
			}

			// The service has not respond yet.
			mPendingRequests.add(callback);
			if (isWaitingForResponse) {
				// Call to the service has already been done, so just wait for
				// it to respond;
				return;
			} else {
				isWaitingForResponse = true;
			}
		}

		Intent i = new Intent();
		i.setComponent(SPFInfo.getSPFServiceComponentName());
		i.setAction(SPFInfo.ACTION_SECURITY);
		boolean bound = context.bindService(i, new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// Do nothing, the service is used once
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder binder) {
				SPFSecurityService service = SPFSecurityService.Stub.asInterface(binder);
				try {
					service.registerApp(getAppDescriptor(context), mServiceCallback);
				} catch (RemoteException e) {
					callback.onRegistrationError(new SPFError(SPFError.INTERNAL_SPF_ERROR_CODE));
				}

				context.unbindService(this);
			}

		}, Context.BIND_AUTO_CREATE);

		synchronized (mPendingRequests) {
			if (!bound) {
				callback.onRegistrationError(new SPFError(SPFError.SPF_NOT_INSTALLED_ERROR_CODE));
			}
		}
	}

	private AppDescriptor getAppDescriptor(Context context) {
		String identifier = context.getPackageName();

		try {
			String appVersion = context.getPackageManager().getPackageInfo(identifier, 0).versionName;
			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(identifier, 0);
			String appName = context.getPackageManager().getApplicationLabel(appInfo).toString();
			return new AppDescriptor(identifier, appName, appVersion, SPFPermissionManager.get().getRequiredPermission());
		} catch (NameNotFoundException e) {
			// This will never happen
			throw new IllegalStateException(e);
		}

	}

	public void invalidateToken() {
		synchronized (mPendingRequests) {
			if(!hasToken()){
				return;
			}
			
			setToken(null);
			isWaitingForResponse = false;
		}
	}

	public static interface RegistrationCallback {
		public void onRegistrationSuccessful();

		public void onRegistrationError(SPFError errorMsg);
	}

	private SPFAppRegistrationCallback.Stub mServiceCallback = new SPFAppRegistrationCallback.Stub() {

		@Override
		public void onRegistrationSuccess(String accessToken) throws RemoteException {
			synchronized (mPendingRequests) {
				setToken(accessToken);
				isWaitingForResponse = false;
			}

			for (RegistrationCallback callback : mPendingRequests) {
				callback.onRegistrationSuccessful();
			}

			Log.d(TAG, "Registration successful. " + mPendingRequests.size() + " pending callbacks notified");
			mPendingRequests.clear();
		}

		@Override
		public void onRegistrationFailure() throws RemoteException {
			synchronized (mPendingRequests) {
				isWaitingForResponse = false;
			}

			for (RegistrationCallback callback : mPendingRequests) {
				callback.onRegistrationError(new SPFError(SPFError.REGISTRATION_REFUSED_ERROR_CODE));
			}

			Log.d(TAG, "Registration failed. " + mPendingRequests.size() + " pending callbacks notified");
			mPendingRequests.clear();
		}
	};
}