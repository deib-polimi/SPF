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

import it.polimi.spf.framework.SPF;
import it.polimi.spf.framework.SPFContext;
import it.polimi.spf.shared.SPFInfo;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 *
 */
public class SPFService extends Service {
	// ## WARNING ##
	// Any modification to the package name or class name of this component
	// should be reflected in SPFInfo.

	private static final String TAG = "SPFService";
	private static boolean mIsStartedForeground = false;
	private IBinder mServerBinder;
	private IBinder mProfileBinder;
	private IBinder mLocalServiceBinder;
	private IBinder mNotificationBinder;
	private IBinder mSecurityBinder;

	private Notification mDefaultNotification;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, "External app bound with action " + intent.getAction());

		if (SPFInfo.ACTION_PROXIMITY_SERVER.equalsIgnoreCase(action)) {
			SPF.get().connect();
			// Request the binder that offers access to proximity
			// functionalities
			if (mServerBinder == null) {
				mServerBinder = new SPFProximityServiceImpl();
			}
			return mServerBinder;

		} else if (SPFInfo.ACTION_PROFILE.equals(action)) {
			// Requests the binder that offers the access to the local profile
			if (mProfileBinder == null) {
				mProfileBinder = new SPFProfileServiceImpl(this);
			}
			return mProfileBinder;

		} else if (SPFInfo.ACTION_SERVICE.equalsIgnoreCase(action)) {
			// Request the binder to manage local services
			if (mLocalServiceBinder == null) {
				mLocalServiceBinder = new SPFServiceManagerImpl();
			}
			return mLocalServiceBinder;

		} else if (SPFInfo.ACTION_NOTIFICATION.equalsIgnoreCase(action)) {
			// request the binder to access notification services
			if (mNotificationBinder == null) {
				mNotificationBinder = new SPFNotificationServiceImpl(this);
			}
			return mNotificationBinder;
		} else if (SPFInfo.ACTION_SECURITY.equals(action)) {
			// request the binder to access security services
			if (mSecurityBinder == null) {
				mSecurityBinder = new SPFSecurityServiceImpl(this);
			}
			return mSecurityBinder;
		}

		Log.d(TAG, "Unrecognized action: " + action);
		return null;
	}

	//Triggered by the front end to keep spf service active in foreground
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!SPF.get().isConnected()) {
			SPF.get().connect();
		}
		startInForeground();
		return START_STICKY;
	}

	private void startInForeground() {
		Notification n = SPFContext.get().getServiceNotification();
		if (n == null) {
			n = mDefaultNotification;
		}
		startForeground(001, n);
		mIsStartedForeground=true;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		SPF.get().onServerCreated(this);
		// Default notification is empty, so Android will use its own default
		// notification for this app.
		mDefaultNotification = new Notification.Builder(this).build();
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mIsStartedForeground = false;
		Log.d(TAG, "onDestroy");
		SPF.get().onServerDestroy();
		SPF.get().disconnect();
	}
	
	/**
	 * Check whether the service is started or not.
	 * Note that this method return false if the service is bounded.
	 * 
	 * @return true if the service is started, otherwise false
	 */
	public static boolean isStarted(){
		return mIsStartedForeground;
	}

}
