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
package it.polimi.spf.framework;

import it.polimi.spf.framework.local.SPFService;
import it.polimi.spf.framework.proximity.ProximityMiddleware;
import it.polimi.spf.framework.security.AppRegistrationHandler;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Main entry point for the SPF framework.
 * 
 * @author darioarchetti
 * 
 */
public class SPFContext {

	private static final String NOT_INITIALIZED_MESSAGE = "SPFContext not initialized. Don't forget to call SPFContext.initialize(context) before any call to SPF components";

	// Singleton
	private static SPFContext sInstance;

	// Events
	public static final int EVENT_NOTIFICATION_MESSAGE_RECEIVED = 0;
	public static final int EVENT_CONTACT_REQUEST_RECEIVED = 1;
	public static final int EVENT_ADVERTISING_STATE_CHANGED = 2;

	// Payload extra keys
	public static final String EXTRA_NOTIFICATION_MESSAGE = "notification_message";
	public static final String EXTRA_ACTIVE = "active";

	// Private constants
	private static final String TAG = "EventBroadcaster";

	private Set<OnEventListener> mEventListeners;
	private Handler mHandler;
	private AppRegistrationHandler mRegistrationHandler;
	private Notification mNotification;

	/**
	 * Initializes SPFContext. After this method has been called, you can get
	 * references to SPFContext and SPF.
	 * 
	 * @param context
	 */
	public static synchronized void initialize(Context context, ProximityMiddleware.Factory factory) {
		if (context == null || factory == null) {
			throw new NullPointerException("Arguments cannot be null");
		}

		SPF.initialize(context, factory);
		sInstance = new SPFContext();
	}

	/**
	 * Obtains a reference to the {@link SPFContext} singleton. You need to
	 * initialize SPFContext before calling this method
	 * 
	 * @return the instance
	 * @see #initialize(Context)
	 */
	public static synchronized SPFContext get() {
		assertInitialization();
		return sInstance;
	}

	/* package */static void assertInitialization() {
		if (sInstance == null) {
			throw new IllegalStateException(NOT_INITIALIZED_MESSAGE);
		}
	}

	private SPFContext() {
		mEventListeners = Collections.newSetFromMap(new ConcurrentHashMap<OnEventListener, Boolean>());
		mRegistrationHandler = new AppRegistrationHandler.Default();
		mHandler = new Handler(Looper.getMainLooper());
	}

	/**
	 * Interface for components to listen to events broadcasted by
	 * {@link SPFContext}. It is possible to register a broadcast listener using
	 * {@link SPFContext#registerEventListener(OnEventListener)}.
	 * 
	 * @author darioarchetti
	 */
	public static interface OnEventListener {

		/**
		 * Called when an event is broadcasted through {@link SPFApp}. This call
		 * happens on the main thread.
		 * 
		 * @param eventCode
		 *            - the code of the event.
		 * @param payload
		 *            - a Bundle containing event-specific information.
		 */
		void onEvent(int eventCode, Bundle payload);
	}

	/**
	 * Registers a listener to be notified when an event is broadcasted.
	 * 
	 * @param listener
	 *            - the listener to register.
	 */
	public void registerEventListener(OnEventListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener == null");
		}

		mEventListeners.add(listener);
	}

	/**
	 * Unregisters a listener.
	 * 
	 * @param listener
	 *            - the listener to unregister
	 */
	public void unregisterEventListener(OnEventListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener == null");
		}

		mEventListeners.remove(listener);
	}

	/**
	 * Broadcasts an event to all registered listeners.
	 * 
	 * @param code
	 *            - the event code
	 */
	public void broadcastEvent(int code) {
		broadcastEvent(code, null);
	}
	
	//TODO synchronization: event should not be broadcast after unregistration
	
	/**
	 * Broadcasts an event to all registered listeners. Each listener will
	 * receive a reference to the given bundle.
	 * 
	 * @param code
	 *            - the event code
	 * @param payload
	 *            - the event payload
	 */
	public void broadcastEvent(final int code, final Bundle payload) {
		if (SPFConfig.DEBUG) {
			Log.d(TAG, "Broadcasting event " + code + " with payload " + payload);
		}

		for (final OnEventListener listener : mEventListeners) {//TODO is it thread safe?
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					listener.onEvent(code, payload);
				}
			});
		}
	}

	/**
	 * Obtains a reference to the current {@link AppRegistrationHandler}. If no
	 * handler has been set via
	 * {@link #setAppRegistrationHandler(AppRegistrationHandler)}, an instance
	 * of {@link AppRegistrationHandler.Default} is returned.
	 * 
	 * @return - the app registration handler
	 */
	public AppRegistrationHandler getAppRegistrationHandler() {
		return mRegistrationHandler;
	}

	/**
	 * Sets the {@link AppRegistrationHandler} to handle incoming app
	 * registration request. The instance will be returned by
	 * {@link #getAppRegistrationHandler()}
	 * 
	 * @param registrationHandler
	 */
	public void setAppRegistrationHandler(AppRegistrationHandler registrationHandler) {
		if (registrationHandler == null) {
			throw new NullPointerException();
		}

		this.mRegistrationHandler = registrationHandler;
	}

	/**
	 * Sets the notification that will be shown when {@link SPFService} is run
	 * in foreground.
	 * 
	 * @param notification
	 *            - the notification to show
	 */
	public void setServiceNotification(Notification notification) {
		this.mNotification = notification;
	}

	/**
	 * Gets the notification to show when {@link SPFService} is run in
	 * foreground. It is set with {@link #setServiceNotification(Notification)},
	 * and is null if never set.
	 * 
	 * @return - the notification.
	 */
	public Notification getServiceNotification() {
		return mNotification;
	}
}
