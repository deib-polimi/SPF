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

import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFServiceDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

/**
 * Wrapper class for services that eases the invocation of methods. A service is
 * made up of a ServiceInterface (an interface annotated with
 * {@link ServiceInterface}) that describes the service and its methods, and of
 * an implementation of such interface.
 */
public class ServiceWrapper {

	private static class ErrorMsg {
		public final static String METHOD_NOT_FOUND = "Method %s not found in service %s.";
		public final static String ILLEGAL_ARGUMENT = "Illegal argument provided for method invocation.";
	}

	private SPFServiceDescriptor mServiceDescriptor;
	private Object mImplementation;
	private Map<String, Method> mMethodIndex;

	/**
	 * Creates a new wrapper for the given service. The given Service Interface
	 * will be validated (see {@link ServiceValidator} for the constraints),
	 * illegal service interface will cause an unchecked
	 * IllegalServiceException.
	 * 
	 * @param context
	 *            - the Android context
	 * @param serviceInterface
	 *            - the service interface
	 * @param implementation
	 *            - the service implementation
	 */
	public ServiceWrapper(Class<?> serviceInterface, Object implementation) {
		ServiceValidator.validateInterface(serviceInterface, ServiceValidator.TYPE_PUBLISHED);
		ServiceInterface service = serviceInterface.getAnnotation(ServiceInterface.class);

		this.mServiceDescriptor = ServiceInterface.Convert.toServiceDescriptor(service);
		this.mImplementation = implementation;
		this.mMethodIndex = new HashMap<String, Method>();

		for (Method m : serviceInterface.getMethods()) {
			if (m.isAnnotationPresent(ActivityConsumer.class)) {
				continue;
			}

			mMethodIndex.put(m.getName(), m);
		}
	}

	/**
	 * Invokes a method of the service.
	 * 
	 * @param request
	 *            - The invocation request
	 * @return the return value
	 * @throws IllegalInvocationException
	 *             if the request does not match the service
	 * @throws InvocationTargetException
	 *             if an Exception is thrown during execution
	 */
	public InvocationResponse invokeMethod(InvocationRequest request) {
		String methodName = request.getMethodName();

		if (!mMethodIndex.containsKey(methodName)) {
			String msg = String.format(ErrorMsg.METHOD_NOT_FOUND, methodName, mServiceDescriptor.getServiceName());
			return InvocationResponse.error(msg);
		}

		Method m = mMethodIndex.get(methodName);
		Object[] params;
		try {
			params = deserializeParameters(request.getPayload(), m.getParameterTypes());
		} catch (ServiceInvocationException e) {
			return InvocationResponse.error("Error deserializing parameters:" + e.getMessage());
		}

		try {
			Object result = m.invoke(mImplementation, params);
			String json = GsonHelper.gson.toJson(result);
			return InvocationResponse.result(json);
		} catch (IllegalAccessException e) {
			return InvocationResponse.error(e);
		} catch (IllegalArgumentException e) {
			return InvocationResponse.error(ErrorMsg.ILLEGAL_ARGUMENT);
		} catch (InvocationTargetException e) {
			return InvocationResponse.error(e.getCause());
		}
	}

	/**
	 * @return the name of the available methods.
	 */
	public Set<String> getAvailableMethods() {
		return mMethodIndex.keySet();
	}

	/**
	 * @return the {@link ServiceDescriptor} of this service.
	 */
	public SPFServiceDescriptor getServiceDescriptor() {
		return mServiceDescriptor;
	}

	public static <T> String readServiceName(Class<? super T> serviceInterface) {
		ServiceInterface service = serviceInterface.getAnnotation(ServiceInterface.class);
		if (service == null) {
			throw new IllegalArgumentException("Service interface does not have a ServiceInterface annotation.");
		}

		return service.name();
	}

	private Object[] deserializeParameters(String payload, Class<?>[] parameterTypes) throws ServiceInvocationException {
		JsonParser parser = new JsonParser();
		JsonArray array = parser.parse(payload).getAsJsonArray();
		if (array.size() != parameterTypes.length) {
			throw new ServiceInvocationException("Parameter number mismatch");
		}

		Object[] params = new Object[parameterTypes.length];
		for (int i = 0; i < parameterTypes.length; i++) {
			params[i] = GsonHelper.gson.fromJson(array.get(i), parameterTypes[i]);
		}

		return params;
	}
}