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

package it.polimi.spf.shared.aidl;

import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFActivity;

/**
 * Callback interface that allows SPF to communicate with applications that
 * registered services. In particular, SPF uses this interface to dispatch
 * {@link SPFActivity} and {@link InvocationRequest} for services registered by
 * the local application.
 */
interface ClientExecutionService {

	/**
	 * Dispatches an {@link InvocationRequest} to the local application
	 * to perform the execution.
	 * 
	 * @param request
	 *            - the request containing the detail of the invocation
	 * @return an {@link InvocationResponse} containing the result of
	 *         the invocation
	 */
	InvocationResponse executeService(in InvocationRequest request);
	
	/**
	 * Dispatches a {@link SPFActivity} to the SPF Service exposing this
	 * interface
	 * 
	 * @param activity
	 *            - the activity to handle
	 * @return an {@link InvocationResponse} containing the result of
	 *         the invocation, in particular <code>true</code> if the
	 *         Activity was correctly handled, <code>false</code> if the
	 *         activity was not handled but no error occurred, or the
	 *         error in case of failure.
	 */
	InvocationResponse sendActivity(in SPFActivity activity);

}