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

import it.polimi.spf.shared.model.SPFActivity;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;

import android.content.ComponentName;

/**
 * Static class to validate service interfaces.
 */
class ServiceValidator {

	/**
	 * Indicates to perform the validation of a service to be published.
	 */
	static final int TYPE_PUBLISHED = 0;

	/**
	 * Indicates to perform the validation of a service to be called remotely.
	 */
	static final int TYPE_REMOTE = 1;

	private static class ErrorMsg {
		public final static String INTERFACE_REQUIRED = "Please provide an interface as serviceInterface param.";
		public final static String MISSING_ANNOTATION = "Service is missing required annotation @Service";
		public static final String MISSING_SERVICE_NAME = "Service name is empty.";
		public static final String MISSING_APP_NAME = "App name is required for this service.";
		public static final String MISSING_INTENT = "Intent name is empty";
		public final static String SUPPORTED_TYPES = "Only primitive types are supported.";
		public final static String RET_TYPE_INVALID = "Invalid return type %s (Method %s.%s)." + SUPPORTED_TYPES;
		public final static String PARAM_TYPE_INVALID = "Invalid parameter #%s type %s (Method %s.%s)." + SUPPORTED_TYPES;
		public static final String INVALID_EXCEPTIONS = "Service methods must declare an InvocationException, and nothing else.";
		public static final String CONSUMER_RET_TYPE = "Consumer method %s must return void";
		public static final String CONSUMER_PARAM = "Consumer method %s should have only one parameter of type SPFActivity";
		public static final String CONSUMER_EXCEPTIONS = "Consumer method %s should not declare any exception";
		public static final String MISSING_IMPLEMENTATION = "Service implementation %s not found";
		public static final String INVALID_IMPLEMENTATION = "Service implementation %s does not implement service %s";
		public static final String WRONG_SUPERCLASS = "Service implementation %s is not a sublcass of " + SPFServiceEndpoint.class.getSimpleName();
		public static final String ABSTRACT_IMPLEMENTATION = "Service implementation %s must not be abstract";
	}

	private static Class<?>[] validTypes = new Class<?>[] { String.class, Long.class, Integer.class, Float.class, Double.class };

	/**
	 * Validates the interface of a service. In case of failed validation, an
	 * unchecked {@link IllegalServiceException} is thrown.
	 * 
	 * To be valid, a service interface must comply with a series of
	 * constraints:
	 * <ul>
	 * <li>Being annotated with a complete {@link ServiceInterface} annotation;</li>
	 * <li>Having all methods with supported return type;</li>
	 * <li>Having all methods with parameters whose type is supported</li>
	 * <li>Having all methods throwing {@link ServiceInvocationException}</li>
	 * </ul>
	 * 
	 * Supported types includes only native Java types.
	 * 
	 * @param serviceInterface
	 *            - the interface to be validated;
	 * @param validationType
	 *            - the validation to be performed, either
	 *            <code>TYPE_PUBLISHED</code> or <code>TYPE_REMOTE</code>
	 */
	static void validate(Class<?> serviceInterface, int validationType) {
		// Verify serviceInterface is an interface.
		assertThat(serviceInterface.isInterface(), ErrorMsg.INTERFACE_REQUIRED);

		// Verify serviceInterface has the right annotation.
		ServiceInterface service = serviceInterface.getAnnotation(ServiceInterface.class);
		assertThat(service != null, ErrorMsg.MISSING_ANNOTATION);

		// Verify service name is not empty
		assertThat(!isStringEmpty(service.name()), ErrorMsg.MISSING_SERVICE_NAME);

		// Verify service app name is not empty for remote services
		assertThat(!(validationType == TYPE_REMOTE && isStringEmpty(service.app())), ErrorMsg.MISSING_APP_NAME);

		// Verify intent
		assertThat(!(validationType == TYPE_PUBLISHED && isStringEmpty(service.componentName())), ErrorMsg.MISSING_INTENT);

		if (validationType == TYPE_PUBLISHED) {
			//validateServiceImplementation(serviceInterface, service.componentName());
		}

		// Analyze methods
		for (Method m : serviceInterface.getMethods()) {
			if (m.isAnnotationPresent(ActivityConsumer.class)) {
				validateActivityConsumer(service.name(), m);
			} else {
				validateStandardMethod(service.name(), m);
			}
		}
	}

	static void validateServiceImplementation(Class<?> serviceClass, String implName) throws IllegalServiceException {
		// Verify component name describes a class that can be loaded by
		// classloader
		// TODO Dario change after refactor of registration
		ComponentName componentName = ComponentName.unflattenFromString(implName);
		Class<?> implClass;
		try {
			implClass = Class.forName(componentName.getClassName());
		} catch (ClassNotFoundException e) {
			throw new IllegalServiceException(String.format(ErrorMsg.MISSING_IMPLEMENTATION, implName));
		}

		// Verify that the implementation actually implements the service
		// interface
		assertThat(serviceClass.isAssignableFrom(implClass), e(ErrorMsg.INVALID_IMPLEMENTATION, implName, serviceClass.getCanonicalName()));

		// Verify that the implementation is a subclass of
		// SPFServiceEndpoint
		assertThat(SPFServiceEndpoint.class.equals(implClass.getSuperclass()), e(ErrorMsg.WRONG_SUPERCLASS, implName));

		// Verify that the class is not abstract
		assertThat(!Modifier.isAbstract(implClass.getModifiers()), e(ErrorMsg.ABSTRACT_IMPLEMENTATION, implName));
	}

	private static void validateActivityConsumer(String serviceName, Method m) {
		String methodName = m.getName();
		assertThat(m.getReturnType().equals(Void.TYPE), e(ErrorMsg.CONSUMER_RET_TYPE, methodName));
		assertThat(m.getParameterTypes().length == 1, e(ErrorMsg.CONSUMER_PARAM, methodName));
		assertThat(m.getParameterTypes()[0].equals(SPFActivity.class), e(ErrorMsg.CONSUMER_PARAM, methodName));
		assertThat(m.getExceptionTypes().length == 0, e(ErrorMsg.CONSUMER_EXCEPTIONS, methodName));
	}

	private static void validateStandardMethod(String serviceName, Method m) {
		String methodName = m.getName();

		// Verify return type is valid.
		Class<?> retType = m.getReturnType();
		//assertThat(isTypeValid(retType), e(ErrorMsg.RET_TYPE_INVALID, retType, serviceName, methodName));

		// Verify parameter types are valid.
		Class<?>[] params = m.getParameterTypes();
		for (int i = 0; i < params.length; i++) {
			Class<?> paramType = params[i];
			//assertThat(paramType.equals(SPFActivity.class) || isTypeValid(params[i]), e(ErrorMsg.PARAM_TYPE_INVALID, i, params[i], serviceName, methodName));
		}

		// Verify exception type is valid.
		Type[] exceptions = m.getGenericExceptionTypes();
		assertThat(exceptions.length == 1 && ServiceInvocationException.class.equals(exceptions[0]), ErrorMsg.INVALID_EXCEPTIONS);
	}

	private static void assertThat(boolean value, String message) {
		if (!value) {
			throw new IllegalServiceException(message);
		}
	}

	private static String e(String errorMsg, Object... args) {
		return String.format(errorMsg, args);
	}

	private static boolean isTypeValid(Class<?> type) {
		return type.isPrimitive() || Arrays.asList(validTypes).contains(type);
	}

	private static boolean isStringEmpty(String s) {
		return s == null || s.equals("");
	}
}
