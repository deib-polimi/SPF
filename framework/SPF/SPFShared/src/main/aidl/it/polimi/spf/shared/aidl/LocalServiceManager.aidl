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

import it.polimi.spf.shared.model.AppDescriptor;
import it.polimi.spf.shared.model.SPFServiceDescriptor;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.SPFError;
import it.polimi.spf.shared.model.SPFActivity;

/**
 * Interface exposed by SPF that allows local applications to register services to
 * be made available for execution, and to execute the services of local
 * applications.
 */
interface LocalServiceManager {

	/**
	 * Dispatches a {@link SPFActivity} to locally. The actual target of
	 * the activity will be established by SPF according to the activity
	 * verb. To perfom this call, the local application must be granted
	 * {@link Permission#ACTIVITY_SERVICE}
	 * 
	 * @param accessToken
	 *            - the token provided to the local app by SPF upon
	 *            registration
	 * @param activity
	 *            - the activity to dispatch
	 * @param error
	 *            - the container for error that may occur during
	 *            execution
	 * @return the {@link InvocationResponse} containing the result of
	 *         the invocation. See
	 *         {@link ClientExecutionService#sendActivity(SPFActivity)}
	 */
	InvocationResponse sendActivityLocally(String accessToken, in SPFActivity activity, out SPFError error);
	
	/**
	 * Dispatches an {@link InvocationRequest} to a service of an
	 * application registered on the local instance of SPF. To perform
	 * this call, the local application needs to be granted
	 * {@link Permission#EXECUTE_LOCAL_SERVICES}
	 * 
	 * @param accessToken
	 *            - the token provided to the local app by SPF upon
	 *            registration
	 * 
	 * @param request
	 *            - the {@link InvocationRequest} to dispatch
	 * @param error
	 *            - the container for error that may occur during
	 *            execution
	 * @return the {@link InvocationResponse} containing the result of
	 *         the invocation. See
	 *         {@link ClientExecutionService#executeService(InvocationRequest)}
	 */
    InvocationResponse executeLocalService(String accessToken, in InvocationRequest request, out SPFError error);
    
	/**
	 * Registers a service in the local instance of SPF. After
	 * registration, external application, both local and remote, will
	 * be allowed to execute the service. To perform this call, the
	 * local application must be granted
	 * {@link Permission#REGISTER_SERVICES}
	 * 
	 * @param accessToken
	 *            - the token provided to the local app by SPF upon
	 *            registration
	 * 
	 * @param descriptor
	 *            - the descriptor of the service to register in SPF
	 * @param error
	 *            - the container for error that may occur during
	 *            execution
	 * @return true if the service was correctly registered
	 */
    boolean registerService(String accessToken, in SPFServiceDescriptor descriptor, out SPFError error);
    
	/**
	 * Unregisters a service from SPF. To perform this call, the local
	 * application must be granted {@link Permission#REGISTER_SERVICES}
	 * 
	 * @param accessToken
	 *            - the token provided to the local app by SPF upon
	 *            registration
	 * 
	 * @param descriptor
	 *            - the descriptor of the service to register in SPF
	 * @param error
	 *            - the container for error that may occur during
	 *            execution
	 * @return true if the service was correctly unregistered
	 */
    boolean unregisterService(String accessToken, in SPFServiceDescriptor descriptor, out SPFError error);
    
	/**
	 * Fill in all the information that should be automatically injected
	 * into an {@link SPFActivity}. To perform this call, the local
	 * application must be granted {@link Permission#ACTIVITY_SERVICE}
	 * 
	 * @param accessToken
	 *            - the token provided to the local app by SPF upon
	 *            registration
	 * 
	 * @param activity
	 *            - the activity to inject the information into.
	 * @param error
	 *            - the container for error that may occur during
	 *            execution
	 */
    void injectInformationIntoActivity(String accessToken, inout SPFActivity activity, out SPFError error);
}