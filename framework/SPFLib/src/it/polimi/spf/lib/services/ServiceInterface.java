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

import it.polimi.spf.lib.Utils;

import it.polimi.spf.shared.model.SPFServiceDescriptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for interfaces that describe services. Services are collection of
 * methods that can be invoked by remote instances of SPF. This annotation must
 * be present on interfaces intended:
 * <ul>
 * <li>to be registered in {@link SPFServiceRegistry}</li>
 * <li>to be invoked using {@link SPFServiceExecutor}</li>
 * </ul>
 * 
 * @author darioarchetti
 */

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = { ElementType.TYPE })
public @interface ServiceInterface {

	/**
	 * The identifier of the app who registered the service. It is mandatory
	 * only for services to be executed. In service registration, this
	 * information is ignored.
	 */
	public String app();

	/**
	 * The name of the service. If not available, it is retrieved from the name
	 * of the annotated interface.
	 */
	public String name() default "";

	/**
	 * The description of the service
	 */
	public String description() default "";

	/**
	 * The version of the service. It is always mandatory.
	 */
	public String version();

	/**
	 * The component that implements the service. It will be used by SPF to bind
	 * to the {@link SPFServiceEndpoint} exposed by the application to dispatch
	 * method execution requests. It is mandatory for services that are being
	 * registered.
	 */
	@Deprecated
	public String componentName() default "";

	/**
	 * The list of {@link SPFActivity} verbs supported by the service.
	 * 
	 * @return
	 */
	public String[] consumedVerbs() default {};

	/**
	 * Utility class to convert the an instance of {@link ServiceInterface} into
	 * one of {@link ServiceDescriptor} containing the same information.
	 * 
	 * @author darioarchetti
	 * 
	 */
	public final static class Convert {
		private Convert() {
		}

		/**
		 * Converts an instance of {@link ServiceInterface} into one of
		 * {@link ServiceDescriptor} containing the same interface.
		 * 
		 * @param svcInterface
		 * @return
		 */
		public final static SPFServiceDescriptor toServiceDescriptor(ServiceInterface svc) {
			Utils.notNull(svc, "svcInterface must not be null");
			return new SPFServiceDescriptor(svc.name(), svc.description(), svc.app(), svc.version(), null, svc.consumedVerbs());
		}

		/**
		 * Converts an instance of {@link ServiceInterface} into one of
		 * {@link ServiceDescriptor} containing the same interface, including
		 * its component name
		 * 
		 * @param svcInterface
		 * @return
		 */
		public final static SPFServiceDescriptor toServiceDescriptor(ServiceInterface svc, String flattenComponentName) {
			Utils.notNull(svc, "svcInterface must not be null");
			return new SPFServiceDescriptor(svc.name(), svc.description(), svc.app(), svc.version(), flattenComponentName, svc.consumedVerbs());
		}
	}
}
