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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import android.os.Looper;
import android.util.Log;

import it.polimi.spf.lib.SPFPerson;
import it.polimi.spf.lib.Utils;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFServiceDescriptor;

/**
 * Invocation stub that allows the invocation of the remote methods of a target
 * {@link SPFPerson}. The methods can be invoked by name, providing the list of
 * parameters. This is useful when the interface of the remote service is not
 * available.
 */
public class InvocationStub {

	/**
	 * Interface for components that can receive invocation requests. Used to
	 * abstract the target of an invocation that may be the local instance of
	 * SPF or a remote one, thus making {@link InvocationStub} able to work in
	 * both cases.
	 * 
	 * @author darioarchetti
	 * 
	 */
	/* package */interface Target {

		/**
		 * Perform any needed action on the parameters of an execution
		 * 
		 * @param arguments
		 *            - the arguments that may need to be prepared
		 */
		void prepareArguments(Object[] arguments) throws ServiceInvocationException;

		/**
		 * Low level call to dispatch invocation requests.
		 * 
		 * @param request
		 *            - the request to execute
		 * @return an instance of {@link InvocationResponse} containing the
		 *         result of the invocation.
		 * @throws ServiceInvocationException
		 *             if an instance is thrown during the execution of the
		 *             service.
		 */
		InvocationResponse executeService(InvocationRequest request) throws ServiceInvocationException;

	}

	/**
	 * Creates an invocation stub to send service invocation requests to a
	 * target {@link Target} performing method calls. The stub is created from a
	 * provided service interface, which must be annotated with
	 * {@link ServiceInterface} describing the service. The method returns an
	 * object implementing the aforementioned interface that can be used to
	 * perform method invocation.
	 * 
	 * @param target
	 *            - the target who the service invocation requests will be
	 *            dispatched to.
	 * @param serviceInterface
	 *            - the interface of the service.
	 * @param classLoader
	 *            - the ClassLoader to load classes.
	 * @return an invocation stub to perform method calls.
	 */
	static <E> E from(Class<E> serviceInterface, ClassLoader classLoader, InvocationStub.Target target) {
		Utils.notNull(target, "target must not be null");
		Utils.notNull(serviceInterface, "descriptor must not be null");

		if (classLoader == null) {
			classLoader = Thread.currentThread().getContextClassLoader();
		}

		// Validate service
		ServiceValidator.validateInterface(serviceInterface, ServiceValidator.TYPE_REMOTE);
		ServiceInterface service = serviceInterface.getAnnotation(ServiceInterface.class);
		SPFServiceDescriptor desc = ServiceInterface.Convert.toServiceDescriptor(service);

		InvocationStub stub = InvocationStub.from(desc, target);
		InvocationHandler h = new InvocationHandlerAdapter(stub);
		Object proxy = Proxy.newProxyInstance(classLoader, new Class[] { serviceInterface }, h);

		return serviceInterface.cast(proxy);
	}

	/**
	 * Creates an invocation stub to send service invocation requests to a
	 * target {@link Target} providing the name and the parameter list. The
	 * object is created from a {@link ServiceDescriptor} containing the
	 * required details.
	 * 
	 * @param target
	 *            - the target who the service invocation requests will be
	 *            dispatched to.
	 * @param descriptor
	 *            - the {@link ServiceDescriptor} of the service whose methods
	 *            to invoke.
	 * @return a {@link InvocationStub} to perform invocations of remote
	 *         services.
	 */
	static InvocationStub from(SPFServiceDescriptor descriptor, InvocationStub.Target target) {
		Utils.notNull(target, "target must not be null");
		Utils.notNull(descriptor, "descriptor must not be null");

		return new InvocationStub(descriptor, target);
	}

	private static final String WRONG_THREAD_MSG = "Remote call to %s.%s made on the UI thread. This may hang your application.";
	private static final String TAG = "InvocationStub";

	private Target mInvocationTarget;
	private SPFServiceDescriptor mServiceDescriptor;

	private InvocationStub(SPFServiceDescriptor descriptor, Target target) {
		this.mServiceDescriptor = descriptor;
		this.mInvocationTarget = target;
	}

	/**
	 * Invokes a remote service providing the name of the method to invoke name
	 * and the list of parameters. The invocation is a blocking network request
	 * and thus should not be performed on the main thread.
	 * 
	 * @param methodName
	 *            - the name of the method to invoke.
	 * @param args
	 *            - the array of the parameters to pass to the method.
	 * @return the return value of the method, if any, or null if the method
	 *         returns void.
	 * @throws ServiceInvocationException
	 *             if an invocation is thrown during the execution of the
	 *             service.
	 */
	public Object invokeMethod(String methodName, Object[] args, Type retType) throws ServiceInvocationException {
		checkCurrentThread(methodName);
		Utils.notNull(methodName);
		Utils.notNull(args);

		// Let the target prepare the arguments if needed
		mInvocationTarget.prepareArguments(args);

		// Serialize arguments
		String payload = GsonHelper.gson.toJson(args);
		InvocationRequest request = new InvocationRequest(mServiceDescriptor.getAppIdentifier(), mServiceDescriptor.getServiceName(), methodName, payload);

		// Let the target perform the execution
		InvocationResponse response = mInvocationTarget.executeService(request);

		// Analyze the response
		if (!response.isResult()) {
			throw new ServiceInvocationException(response.getErrorMessage());
		} else if (retType.equals(void.class)) {
			return null;
		} else {
			return GsonHelper.gson.fromJson(response.getPayload(), retType);
		}
	}

	// Checks if the current thread is the main thread, if so it logs a wrning.
	private void checkCurrentThread(String methodName) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			Log.w(TAG, String.format(WRONG_THREAD_MSG, mServiceDescriptor.getServiceName(), methodName));
		}
	}

	// Adapter class to use an InvocationStub as InvocationHandler in
	// Proxy.newProxyInstance
	private static class InvocationHandlerAdapter implements InvocationHandler {

		private InvocationStub mInvocationStub;

		public InvocationHandlerAdapter(InvocationStub mInvocationStub) {
			this.mInvocationStub = Utils.notNull(mInvocationStub);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws ServiceInvocationException {
			return mInvocationStub.invokeMethod(method.getName(), args, method.getGenericReturnType());
		}
	}
}
