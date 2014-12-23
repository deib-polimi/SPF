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
import it.polimi.spf.shared.model.SPFActivity;

import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;

/**
 * Component that manages the communication with SPF services of enabled
 * applications to allow the dispatching of {@link InvocationRequest} and
 * {@link SPFActivity } with related responses. <br>
 * SPF Services are actually implemented with an Android {@link Service}
 * subclass available in the library that exposes the
 * {@link ClientExecutionService} binder. The communication thus is implemented
 * by binding to such service using
 * {@link Context#bindService(Intent, ServiceConnection, int)} and obtaining a
 * stub of such binder. The binder is obtained and referenced by an instance of
 * {@link AppServiceProxy}. After the creation, a proxy is kept in cache, ready
 * for future use; it may happen that the remote Service is terminated, so
 * before using a proxy from tha cache, the agent checks if the proxy is still
 * alive.
 * 
 * @author darioarchetti
 * 
 */
/* package */class AppCommunicationAgent {

	private static final String TAG = "AppCommunicationAgent";
	private Map<String, AppServiceProxy> mProxies;
	private Context mContext;
	private boolean mShutdown;

	/**
	 * Creates a new {@link AppCommunicationAgent} that will use the given
	 * context to bind to app services.
	 * 
	 * @param context
	 *            - the context used in binding
	 */
	public AppCommunicationAgent(Context context) {
		mContext = context;
		mProxies = new HashMap<String, AppServiceProxy>();
	}

	/**
	 * Returns an {@link AppServiceProxy} that can be used to communicate with
	 * the remote service identified by the given component name. If an alive
	 * proxy for the given component name is found in cache, it i returned.
	 * Otherwise, a new proxy is created, stored in the cache and returned.
	 * 
	 * @param componentName
	 *            - the component name of the remote service
	 * @return a proxy to communicate with the remote service
	 */
	public AppServiceProxy getProxy(String componentName) {
		synchronized (this) {

			if (mShutdown) {
				throw new IllegalStateException("Communication agent is shutdown");
			}

			if (mProxies.containsKey(componentName)) {
				AppServiceProxy proxy = mProxies.get(componentName);
				Log.d(TAG, "Proxy for " + componentName + " found in cache");
				if (proxy.isConnected()) {
					Log.d(TAG, "Proxy is alive");
					return proxy;
				} else {
					Log.d(TAG, "Proxy is dead: remove it");
					mProxies.remove(proxy);
					// Go on with the creation of new proxy
				}
			}

			AppServiceProxy proxy = new AppServiceProxy(componentName);
			mProxies.put(componentName, proxy);
			Intent serviceIntent = new Intent();
			serviceIntent.setComponent(ComponentName.unflattenFromString(componentName));
			boolean bound = mContext.bindService(serviceIntent, proxy, Context.BIND_AUTO_CREATE);

			if (!bound) {
				Log.e(TAG, "Cannot bound to app service with intent " + componentName);
				return null;
			}

			return proxy;
		}
	}

	/**
	 * Unbinds all proxies available in caches and prevents the creation of new
	 * ones.
	 */
	public void shutdown() {
		synchronized (this) {
			if (mShutdown) {
				return;
			}

			for (AppServiceProxy p : mProxies.values()) {
				if (p.isConnected()) {
					mContext.unbindService(p);
				}
			}

			mShutdown = true;
		}
	}

	/**
	 * @return true if the agent has been shutdown and thus no more proxies can
	 *         be created.
	 */
	public boolean isShutDown() {
		return mShutdown;
	}
}
