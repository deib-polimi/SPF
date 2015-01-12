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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import it.polimi.spf.shared.aidl.ClientExecutionService;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFActivity;
import it.polimi.spf.shared.model.SPFServiceDescriptor;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * Abstract service that provides an easy way to expose a
 * {@link ServiceInterface} to allow remote execution of methods. Subclasses may
 * implement one or more {@link ServiceInterface}, and the implementations of
 * their methods will be available to remote callers.
 * 
 * @author darioarchetti
 * 
 */
public abstract class SPFServiceEndpoint extends Service {

	private final static boolean DEBUG = true;
	private final String mTag;
	private Map<String, ServiceWrapper> mServiceIndex;
	private Map<String, List<Method>> mActivityConsumerIndex;

	private final ClientExecutionService.Stub mBinder = new ClientExecutionService.Stub() {

		@Override
		public InvocationResponse executeService(InvocationRequest request) throws RemoteException {
			log("Performing execution request of " + request.getMethodName());
			return doExecuteService(request);
		}

		@Override
		public InvocationResponse sendActivity(SPFActivity activity) throws RemoteException {
			log("Dispatching activity " + activity + " to consumers");
			return doSendActivity(activity);
		}

	};

	protected SPFServiceEndpoint() {
		mTag = getClass().getSimpleName();
		mServiceIndex = new Hashtable<String, ServiceWrapper>();
		mActivityConsumerIndex = new HashMap<String, List<Method>>();

		// Lookup in implemented interfaces those annotated with
		// ServiceInterface annotation
		List<Class<?>> serviceInterfaces = new ArrayList<Class<?>>();
		for (Class<?> iface : getClass().getInterfaces()) {
			if (iface.isAnnotationPresent(ServiceInterface.class)) {
				ServiceValidator.validateInterface(iface, ServiceValidator.TYPE_PUBLISHED);
				serviceInterfaces.add(iface);
			}
		}

		if (serviceInterfaces.size() == 0) {
			log("ExecutionEndpointService does not implement any ServiceInterface.");
			return;
		}

		for (Class<?> serviceInterface : serviceInterfaces) {
			ServiceInterface annotation = serviceInterface.getAnnotation(ServiceInterface.class);
			SPFServiceDescriptor d = ServiceInterface.Convert.toServiceDescriptor(annotation);
			ServiceWrapper w = new ServiceWrapper(serviceInterface, this);
			mServiceIndex.put(d.getServiceName(), w);
			for (String v : d.getConsumedVerbs()) {
				if (!mActivityConsumerIndex.containsKey(v)) {
					mActivityConsumerIndex.put(v, new ArrayList<Method>());
				}
			}
		}

		for (Method m : getClass().getMethods()) {
			if (m.isAnnotationPresent(ActivityConsumer.class)) {
				ActivityConsumer c = m.getAnnotation(ActivityConsumer.class);
				List<Method> consumers = mActivityConsumerIndex.get(c.verb());
				if (consumers == null) {
					log("Verb " + c.verb() + " is not declared in service annotation");
				} else {
					consumers.add(m);
				}
			}
		}

	}

	private InvocationResponse doExecuteService(InvocationRequest request) {
		String svcName = request.getServiceName();
		if (!mServiceIndex.containsKey(svcName)) {
			return InvocationResponse.error("Service " + svcName + " not found in index.");
		}

		return mServiceIndex.get(svcName).invokeMethod(request);
	}

	private InvocationResponse doSendActivity(SPFActivity activity) {
		List<Method> consumers = mActivityConsumerIndex.get(activity.getVerb());
		if (consumers == null) {
			log("Received unexpected activity verb: " + activity.getVerb());
			return InvocationResponse.error("Unsupported activity verb");
		}

		if (consumers.size() == 0) {
			log("No consumers for declared verb " + activity.getVerb());
			return InvocationResponse.error("No consumer available for verb " + activity.getVerb());
		}

		Object[] args = { activity };
		for (Method m : consumers) {
			try {
				m.invoke(this, args);
			} catch (Exception e) {
				log("Error invoking consumer method " + m.getName(), e);
			}
		}

		String json = GsonHelper.gson.toJson(Boolean.TRUE);
		return InvocationResponse.result(json);
	}

	private void log(String msg) {
		if (DEBUG) {
			Log.d(mTag, msg);
		}
	}

	private void log(String msg, Throwable t) {
		if (DEBUG) {
			Log.e(mTag, msg, t);
		}
	}

	@Override
	public /*final*/ IBinder onBind(Intent intent) {
		return mBinder;
	}
}