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

import java.util.Collection;
import android.content.Context;
import android.util.Log;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFActivity;
import it.polimi.spf.shared.model.SPFServiceDescriptor;

/**
 * Refactored version of {@link ServiceDispatcher}
 * 
 * @author darioarchetti
 * 
 */
public class SPFServiceRegistry {

	private static final String TAG = "ServiceRegistry";
	private ActivityConsumerRouteTable mActivityTable;
	private ServiceRegistryTable mServiceTable;
	private AppCommunicationAgent mCommunicationAgent;

	public SPFServiceRegistry(Context context) {
		mServiceTable = new ServiceRegistryTable(context);
		mActivityTable = new ActivityConsumerRouteTable(context);
		mCommunicationAgent = new AppCommunicationAgent(context);
	}

	/**
	 * Registers a service. The owner application must be already registered.
	 * 
	 * @param descriptor
	 * @return true if the service was registered
	 */
	public boolean registerService(SPFServiceDescriptor descriptor) {
		return mServiceTable.registerService(descriptor) && mActivityTable.registerService(descriptor);
	}

	/**
	 * Unregisters a service.
	 * 
	 * @param descriptor
	 *            - the descriptor of the service to unregister
	 * @return true if the service was removed
	 */
	public boolean unregisterService(SPFServiceDescriptor descriptor) {
		return mServiceTable.unregisterService(descriptor) && mActivityTable.unregisterService(descriptor);
	}

	/**
	 * Unregisters all the service of an application
	 * 
	 * @param appIdentifier
	 *            - the identifier of the app whose service to remove
	 * @return true if all the services where removed.
	 */
	public boolean unregisterAllServicesOfApp(String appIdentifier) {
		return mServiceTable.unregisterAllServicesOfApp(appIdentifier) && mActivityTable.unregisterAllServicesOfApp(appIdentifier);
	}

	/**
	 * Retrieves all the services of an app.
	 * 
	 * @param appIdentifier
	 *            - the id of the app whose service to retrieve
	 * @return the list of its services
	 */
	public SPFServiceDescriptor[] getServicesOfApp(String appIdentifier) {
		return mServiceTable.getServicesOfApp(appIdentifier);
	}

	/**
	 * Dispatches an invocation request to the right application. If the
	 * application is not found, an error response is returned.
	 * 
	 * @param request
	 * @return
	 */
	public InvocationResponse dispatchInvocation(InvocationRequest request) {
		String appName = request.getAppName();
		String serviceName = request.getServiceName();
		String componentName = mServiceTable.getComponentForService(appName, serviceName);

		if (componentName == null) {
			return InvocationResponse.error("Application " + appName + " doesn't have a service named " + serviceName);
		}

		AppServiceProxy proxy = mCommunicationAgent.getProxy(componentName);
		if (proxy == null) {
			return InvocationResponse.error("Cannot bind to service");
		}
		
		try {
			return proxy.executeService(request);
		} catch (Throwable t) {
			Log.e("ServiceRegistry", "Error dispatching invocation: ", t);
			return InvocationResponse.error("Internal error: " + t.getMessage());
		}
	}

	/**
	 * Dispatches an activity to the right application according to {@link
	 * ActivityConsumerRouteTable#}
	 * 
	 * @param activity
	 * @return
	 */
	public InvocationResponse sendActivity(SPFActivity activity) {
		ServiceIdentifier id = mActivityTable.getServiceFor(activity);
		String componentName = mServiceTable.getComponentForService(id);

		if (componentName == null) {
			String msg = "No service to handle " + activity;
			Log.d(TAG, msg);
			return InvocationResponse.error(msg);
		}

		AppServiceProxy proxy = mCommunicationAgent.getProxy(componentName);
		if (proxy == null) {
			String msg = "Can't bind to service " + componentName;
			Log.d(TAG, msg);
			return InvocationResponse.error(msg);
		}

		try {
			InvocationResponse r = proxy.sendActivity(activity);
			Log.v(TAG, "Activity dispatched: " + r);
			return r;
		} catch (Throwable t) {
			Log.e(TAG, "Error dispatching invocation: ", t);
			return InvocationResponse.error("Internal error: " + t.getMessage());
		}
	}

	@Deprecated
	public Collection<ActivityVerb> getVerbSupportList() {
		return mActivityTable.getVerbSupport();
	}
	
	public Collection<ActivityVerb> getSupportedVerbs() {
		return mActivityTable.getVerbSupport();
	}

	public void setDefaultConsumerForVerb(String verb, ServiceIdentifier identifier) {
		mActivityTable.setDefaultServiceForVerb(verb, identifier);
	}
}
