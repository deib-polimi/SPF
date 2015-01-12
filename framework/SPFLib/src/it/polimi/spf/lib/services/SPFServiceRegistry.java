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
package it.polimi.spf.lib.services;

import it.polimi.spf.lib.Component;
import it.polimi.spf.lib.Utils;
import it.polimi.spf.lib.profile.SPFLocalProfile;

import it.polimi.spf.shared.SPFInfo;
import it.polimi.spf.shared.aidl.LocalServiceManager;
import it.polimi.spf.shared.model.SPFError;
import it.polimi.spf.shared.model.SPFServiceDescriptor;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Allows applications to register and un-registratier services, and responds to
 * execution requests coming from the SPF framework.
 * 
 * @author darioarchetti
 * 
 */
public final class SPFServiceRegistry extends Component<SPFServiceRegistry, LocalServiceManager> {

	private static final Component.Descriptor<SPFServiceRegistry, LocalServiceManager> DESCRIPTOR = new Descriptor<SPFServiceRegistry, LocalServiceManager>() {

		@Override
		public String getActionName() {
			return SPFInfo.ACTION_SERVICE;
		}

		@Override
		public LocalServiceManager castInterface(IBinder binder) {
			return LocalServiceManager.Stub.asInterface(binder);
		}

		@Override
		public SPFServiceRegistry createInstance(Context context, LocalServiceManager serviceInterface, ServiceConnection connection, ConnectionCallback<SPFServiceRegistry> callback) {
			return new SPFServiceRegistry(context, serviceInterface, connection, callback);
		}
	};

	/**
	 * Loads an instance of {@link SPFServiceRegistry} asynchronously. The
	 * callbacks to be called once the connection is ready should be contained
	 * into an instance of {@link SPFLocalProfile.Callback}.
	 * 
	 * @param context
	 *            - the {@link Context} used to bind to SPF.
	 * @param callback
	 *            - the callback code to be executed once the connection is
	 *            ready.
	 */
	public static void load(final Context context, final Callback callback) {
		Component.load(context, DESCRIPTOR, callback);
	}

	protected SPFServiceRegistry(Context context, LocalServiceManager serviceInterface, ServiceConnection connection, ConnectionCallback<SPFServiceRegistry> callback) {
		super(context, serviceInterface, connection, callback);
	}

	/**
	 * Allows to register a service in the service index. Such service is made
	 * available to remote apps.
	 * 
	 * @param serviceInterface
	 * @param implementation
	 */
	@Deprecated
	public <T> void registerService(Class<? super T> serviceInterface) {
		Utils.notNull(serviceInterface, "serviceInterface must not be null");

		ServiceInterface annotation = serviceInterface.getAnnotation(ServiceInterface.class);
		if (annotation == null) {
			throw new IllegalServiceException("Missing annotation");
		}

		String flattenComponentName = annotation.componentName();
		if (flattenComponentName == null) {
			throw new IllegalStateException("Missing componentName");
		}

		ComponentName cn = ComponentName.unflattenFromString(flattenComponentName);
		registerService(serviceInterface, cn);
	}

	/**
	 * Allows to register a service in the service index. Such service is made
	 * available to remote apps.
	 * 
	 * @param serviceInterface
	 * @param implementation
	 */
	public <T> void registerService(Class<? super T> serviceInterface, Class<T> implementationClass) {
		Utils.notNull(serviceInterface, "serviceInterface must not be null");
		Utils.notNull(implementationClass, "implementationClass must not be null");

		ComponentName cn = new ComponentName(getContext(), implementationClass);
		registerService(serviceInterface, cn);
	}

	private <T> void registerService(Class<? super T> serviceInterface, ComponentName cn) {
		ServiceValidator.validateInterface(serviceInterface, ServiceValidator.TYPE_PUBLISHED);
		ServiceValidator.validateServiceImplementation(getContext(), cn, serviceInterface);

		ServiceInterface annotation = serviceInterface.getAnnotation(ServiceInterface.class);
		SPFServiceDescriptor descriptor = ServiceInterface.Convert.toServiceDescriptor(annotation, cn.flattenToString());
		String token = getAccessToken();

		try {
			SPFError error = new SPFError();
			getService().registerService(token, descriptor, error);
			if (!error.isOk()) {
				handleError(error);
			}
		} catch (RemoteException e) {
			catchRemoteException(e);
		}
	}

	/**
	 * Allows to unregister a previously registered service.
	 * 
	 * @param serviceInterface
	 */
	public <T> void unregisterService(Class<? super T> serviceInterface) {
		Utils.notNull(serviceInterface, "serviceInterface must not be null");
		ServiceValidator.validateInterface(serviceInterface, ServiceValidator.TYPE_PUBLISHED);
		ServiceInterface svcInterface = serviceInterface.getAnnotation(ServiceInterface.class);
		SPFServiceDescriptor svcDesc = ServiceInterface.Convert.toServiceDescriptor(svcInterface);
		String token = getAccessToken();

		try {
			SPFError error = new SPFError();
			getService().unregisterService(token, svcDesc, error);
			if (!error.isOk()) {
				handleError(error);
			}
		} catch (RemoteException e) {
			catchRemoteException(e);
		}
	}

	public static interface Callback extends Component.ConnectionCallback<SPFServiceRegistry> {
		// public void onServiceReady(SPFServiceRegistry registry);

		// public void onError(SPFError errorMsg);

		// public void onDisconnect();
	}

	private void catchRemoteException(RemoteException e) {
		disconnect();
		getCallback().onDisconnect();
	}
}