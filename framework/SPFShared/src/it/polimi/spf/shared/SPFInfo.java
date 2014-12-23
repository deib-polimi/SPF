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
package it.polimi.spf.shared;

import android.content.ComponentName;

/**
 * Class that holds the configuration parameters that link applications to spf components.
 *
 */
public final class  SPFInfo {
	
	/**
	 * The package name of the application that uses SPF. 
	 * Use the one of the front end application, by default 
	 * it is set to spf official front-end i.e. it.polimi.spf.app.
	 */
	public static String PACKAGE_NAME = "it.polimi.spf.app";
	
	/**
	 * The class name of the service that offers SPF interfaces. 
	 * By default it is set to it.polimi.spf.framework.local.SPFService, 
	 * modify the constant according to the service registered in the front-end application.
	 */
	public static String CLASS_NAME = "it.polimi.spf.framework.local.SPFService";
	
	public static final String ACTION_PROXIMITY_SERVER = "it.polimi.spf.framework.local.SPFServerService";
	public static final String ACTION_PROFILE = "it.polimi.spf.framework.appservice.SPFProfileService";
	public static final String ACTION_SERVICE = "it.polimi.spf.services.LocalServiceExecutor";
	public static final String ACTION_NOTIFICATION = "it.polimi.spf.framework.SPFNotificationService";
	public static final String ACTION_SECURITY = "it.polimi.spf.framework.local.SecurityService";
	
	private SPFInfo(){};
	
	/**
	 * Returns the component name of the service that offers SPF interfaces. 
	 * It is generated according to {@link SPFInfo#PACKAGE_NAME} and {@link SPFInfo#CLASS_NAME}.
	 * @return
	 */
	public static ComponentName getSPFServiceComponentName(){
		return new ComponentName(PACKAGE_NAME, CLASS_NAME);
	}
	
}
