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

/**
 * Indicates that an error has happened during the execution of the service on
 * the remote device. The error message describes the occurred error.
 */
public class ServiceInvocationException extends Exception {

	private static final long serialVersionUID = 3680399061467176569L;

	public ServiceInvocationException() {
		super();
	}

	public ServiceInvocationException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ServiceInvocationException(String detailMessage) {
		super(detailMessage);
	}

	public ServiceInvocationException(Throwable throwable) {
		super(throwable);
	}

}
