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
import android.content.Context;
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
	private static final int NOTIFICATION_ID = 0xab01d;
	private static boolean mIsStartedForeground = false;
	private static String ACTION_START_FOREGROUND = "it.polimi.spf.framework.SPFService.START_FOREGROUND";
	private static String ACTION_STOP_FOREGROUND = "it.polimi.spf.framework.SPFService.STOP_FOREGROUND";

	/**
	 * Sends an intent to SPFService, using the {@link Context} provided, that
	 * makes it start in foreground.
	 * 
	 * @param c - the Context used to send the intent;
	 * 
	 * @see Service#startForeground(int, Notification)
	 */
	public static void startForeground(Context c) {
		Intent i = new Intent(c, SPFService.class);
		i.setAction(ACTION_START_FOREGROUND);
		c.startService(i);
	}

	/**
	 * Sends an intent to SPFService, using the {@link Context} provided, that
	 * makes it stop foreground.
	 * 
	 * @param c - the Context used to send the intent;
	 * 
	 * @see Service#stopForeground(boolean)
	 */
	public static void stopForeground(Context c) {
		Intent i = new Intent(c, SPFService.class);
		i.setAction(ACTION_STOP_FOREGROUND);
		c.startService(i);
		c.stopService(i);
	}

	/**
	 * Check whether the service is started or not. Note that this method return
	 * false if the service is bounded.
	 * 
	 * @return true if the service is started, otherwise false
	 */
	public static boolean isStarted() {
		return mIsStartedForeground;
	}

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

	// Triggered by the front end to keep spf service active in foreground
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent == null){
			return START_STICKY;
		}
		
		String action = intent.getAction();

		if (ACTION_START_FOREGROUND.equals(action)) {
			if (!SPF.get().isConnected()) {
				SPF.get().connect();
			}

			startInForeground();
			Log.d(TAG, "Started in foreground");
		} else if (ACTION_STOP_FOREGROUND.equals(action)) {
			stopForeground(true);
			mIsStartedForeground = false;
			Log.d(TAG, "Foreground stopped");
		}

		return START_STICKY;
	}

	private void startInForeground() {
		Notification n = SPFContext.get().getServiceNotification();
		if (n == null) {
			n = mDefaultNotification;
		}
		startForeground(NOTIFICATION_ID, n);
		mIsStartedForeground = true;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		SPF.get().onServerCreated(this);
		// Default notification is empty, so Android will use its own default
		// notification for this app.
		mDefaultNotification = new Notification.Builder(this).build();
		Log.d(TAG, "Service created");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "Service destroyed");
		SPF.get().onServerDestroy();
		SPF.get().disconnect();
	}
}
