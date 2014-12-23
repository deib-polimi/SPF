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

import it.polimi.spf.shared.model.InvocationResponse;
import it.polimi.spf.shared.model.InvocationRequest;
import it.polimi.spf.shared.model.SPFServiceDescriptor;
import it.polimi.spf.shared.model.AppDescriptor;
import it.polimi.spf.shared.model.SPFSearchDescriptor;
import it.polimi.spf.shared.model.ProfileFieldContainer;
import it.polimi.spf.shared.model.SPFError;
import it.polimi.spf.shared.model.SPFActivity;
import it.polimi.spf.shared.aidl.SPFSearchCallback;

/**
 * Interface that allows the interaction between the local application and
 * remote instances of SPF. Interactions include:
 * <ul>
 * <li>Execution of SPF Services</li>
 * <li>Read of information from remote profiles</li>
 * <li>Search for people in proximity</li>
 * </ul>
 */
interface SPFProximityService {
  
    // SPF Service execution
    
    /**
	 * Dispatches a request of invocation of a SPFService to a remote instance of SPF.
	 * 
	 * @param accessToken
	 *            - the access token provided by SPF upon registration
	 * @param target
	 *            - the identifier of the remote instance
	 * @param request
	 *            - an {@link InvocationRequest} with the identifier of the
	 *            service to invoke and the array of parameters
	 * @param err
	 *            - a container to notify errors that may occur
	 * @return an {@link InvocationResponse} with the outcome of the invocation
	 */
    InvocationResponse executeRemoteService(String accessToken, String target, in InvocationRequest request, out SPFError err);
    
	/**
	 * Dispatches a SPFActivity to a remote instance of SPF. To perform this
	 * call, the local application must be granted
	 * {@link Permission#ACTIVITY_SERVICE}
	 * 
	 * @param accessToken
	 *            - the access token provided by SPF upon registratiob
	 * @param target
	 *            - the identifier of the target instance.
	 * @param activity
	 *            - the activity to dispatch
	 * @param err
	 *            - a container to notify errors that may occur
	 * @return an {@link InvocationResponse} with the outcome of the invocation
	 *         (see
	 *         {@link ClientExecutionService#sendActivity(it.polimi.spf.shared.model.SPFActivity)}
	 */
    InvocationResponse sendActivity(String accessToken, String target, in SPFActivity activity, out SPFError err);
    
    /**
	 * Fill in all the information that should be automatically injected into an
	 * {@link SPFActivity}. To perform this call, the local application must be
	 * granted {@link Permission#ACTIVITY_SERVICE}
	 * 
	 * @param accessToken
	 *            - the token provided to the local app by SPF upon registration
	 * @param target
	 *            - the target of the SPFActivity
	 * @param activity
	 *            - the activity to inject the information into.
	 * @param error
	 *            - the container for error that may occur during execution
	 */
    void injectInformationIntoActivity(String accessToken, String target, inout SPFActivity activity, out SPFError error);
    
    // SPF Profile information retrieval
    /**
	 * Retrieves the values of a set of {@link ProfileField} from the remote
	 * instance identified by the given identifier. To perform this call, the
	 * local application must be granted {@link Permission#READ_REMOTE_PROFILES}
	 * 
	 * @param accessToken
	 *            - the access token provided by SPF upon registration
	 * @param target
	 *            - the identifier of the remote instance
	 * @param fieldIdentifiers
	 *            - an array containing the identifier of the
	 *            {@link ProfileField} to read
	 * @param err
	 *            - a container to notify errors that may occur
	 * @return a {@link ProfileFieldContainer} with the value of requested
	 *         fields.
	 */
    ProfileFieldContainer getProfileBulk(String accessToken, String target, in String[] fieldIdentifiers, out SPFError err);
    
    
    // SPF Search 
    
    /**
	 * Starts a new search for people in proximity; events will be dispatched to
	 * a callback previously registered with
	 * {@link #registerCallback(String, SPFSearchCallback, SPFError)}. To
	 * perform this call, the local application must be granted
	 * {@link Permission#SEARCH_SERVICE}
	 * 
	 * @param accessToken
	 *             the access token provided by SPF upon registration
	 * @param searchDescriptor
	 *             the {@link SPFServiceDescriptor} containing the
	 *            configuration of the search
	 * @param err
	 *             a container to notify errors that may occur
	 * @return the identifier of the search, that can be used with
	 *         {@link #stopSearch(String, String, SPFError)}
	 * @see SPFSearchCallback
	 */
    String startNewSearch(in String accessToken, in SPFSearchDescriptor searchDescriptor, SPFSearchCallback callback, out SPFError err);
    
    /**
	 * Stops a search previously started with
	 * {@link #startNewSearch(String, it.polimi.spf.shared.model.SPFSearchDescriptor, SPFError)}
	 * . To perform this call, the local application must be granted
	 * {@link Permission#SEARCH_SERVICE}
	 * 
	 * @param accessToken
	 *             the access token provided by SPF at registration
	 * @param searchId
	 *             the id of the search to stop, as returned by
	 *            {@link #startNewSearch(String, it.polimi.spf.shared.model.SPFSearchDescriptor, SPFError)}
	 * @param err
	 *             a container to notify errors that may occur
	 */
    void stopSearch(in String accessToken, in String queryId, out SPFError err);
    
    /**
	 * Checks if a person previously found is still available in proximity. To
	 * perform this call, the local application must be granted
	 * {@link Permission#SEARCH_SERVICE}
	 * 
	 * @param accessToken
	 *             the access token provided by SPF upon registration
	 * @param personIdentifier
	 *             the identifier of the person to check
	 * @param err
	 *             a container to notify errors that may occur
	 * @return true if the person is still available, false otherwise
	 */
    boolean lookup(in String accessToken, String personIdentifier, out SPFError err);
}